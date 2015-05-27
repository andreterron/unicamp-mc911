//===- Analysis.cpp - Example code from "Writing an LLVM Pass" ---------------===//
//
//                     The LLVM Compiler Infrastructure
//
// This file is distributed under the University of Illinois Open Source
// License. See LICENSE.TXT for details.
//
//
//===----------------------------------------------------------------------===//

#define DEBUG_TYPE "Analysis"
#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/ADT/Statistic.h"
#include "llvm/Support/Debug.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/Support/InstIterator.h"
#include "llvm/Analysis/ConstantFolding.h"
#include "llvm/ADT/BitVector.h"
#include "llvm/ADT/ValueMap.h"
#include "llvm/Support/CFG.h"
#include "llvm/ADT/SmallVector.h"
#include <cstdio>

using namespace llvm;

namespace {

struct BasicBlockLivenessInfo {
	BitVector *use;
	BitVector *def;
	BitVector *in;
	BitVector *out;
	BasicBlock *block;

	/**
	 * Constructor that builds block information out of LLVM basic block
	 * and definition count.
	 * 
	 * @param block LLVM basic block.
	 * @param defcount Definition count.
	 * 
	 */
	BasicBlockLivenessInfo(BasicBlock *block, unsigned defcount) {
		this->block = block;
		use = new BitVector(defcount);
		def = new BitVector(defcount);
		in = new BitVector(defcount);
		out = new BitVector(defcount);
	}
};

struct Analysis: public FunctionPass {
	static char ID; // Pass identification, replacement for typeid

	Analysis() :
		FunctionPass(ID) {
	}

	typedef ValueMap<Value *, unsigned> LatticeEncoding;

	/**
	 * Assign lattice bits to definitions.
	 * 
	 * @param instToLatticeBit Mapping from definitions to lattice bits.
	 * @param F LLVM function object.
	 * 
	 */
	void initInstrToLatticeBit(LatticeEncoding &instToLatticeBit, Function &F) {
		unsigned instIdx = 0;

		// Arguments count as definitions as well - first F.arg_size() bits.
		for (Function::arg_iterator argIt = F.arg_begin(); argIt != F.arg_end(); argIt++) {
			instToLatticeBit.insert(std::make_pair(&*argIt, instIdx));
			instIdx++;
		}
		// Don't bother filtering out non-definition instructions. Their
		// corresponding use / in / out bits will be always zero and the
		// resulting in / out sets will look right.
		for (inst_iterator instIt = inst_begin(F); instIt != inst_end(F); instIt++) {
			instToLatticeBit.insert(std::make_pair(&*instIt, instIdx));
			instIdx++;
		}
	}

	typedef ValueMap<BasicBlock *, BasicBlockLivenessInfo *> BlockInfoMapping;

	/**
	 * Assign information to basic blocks.
	 * 
	 * @param blockToInfo Mapping from LLVM basic blocks to liveness information.
	 * @param F LLVM function object.
	 * 
	 */
	void initBlocksInfo(BlockInfoMapping &blockToInfo, unsigned defcount, Function &F) {
		for (Function::iterator blockIt = F.begin(); blockIt != F.end(); blockIt++) {
			BasicBlockLivenessInfo *info = new BasicBlockLivenessInfo(&*blockIt, defcount);
			blockToInfo.insert(std::make_pair(blockIt, info));
		}
	}

	/**
	 * Set defs for a given block information object.
	 * 
	 * @param blockInfo Block information object to be filled with def bits.
	 * @param instToLatticeBit Lattice bit encoding for the current function.
	 * 
	 */
	void initBlockDef(BasicBlockLivenessInfo *blockInfo, LatticeEncoding &instToLatticeBit) {
		BasicBlock *block = blockInfo->block;
		for (BasicBlock::iterator instIt = block->begin(); instIt != block->end(); instIt++) {
			blockInfo->def->set(instToLatticeBit[instIt]);
		}
	}

	/**
	 * Set additional defs for the entry block information object.
	 * 
	 * @param blockInfo Entry block information object to be filled with
	 *                  argument def bits.
	 * @param argCount Current function argument count.
	 * 
	 */
	void initEntryArgDef(BasicBlockLivenessInfo *blockInfo, unsigned argCount) {
		for (unsigned argBit = 0; argBit < argCount; argBit++)
			blockInfo->def->set(argBit);
	}

	/**
	 * Set corresponding use bit for all blocks using a given definition.
	 * 
	 * @param use_begin First use of the given definition.
	 * @param use_end One-past-last use of the given definition.
	 * @param blockToInfo Mapping from LLVM basic blocks to liveness information.
	 * @param defInst Definition for which use bits are set
	 *                (NULL if argument, not instruction).
	 * @param defBit Corresponding bit for the definition in the lattice.
	 * 
	 */
	void markUses(Value::use_iterator use_begin, Value::use_iterator use_end,
	              BlockInfoMapping &blockToInfo,
	              Instruction *defInst,
	              unsigned defBit) {
		for (Value::use_iterator useIt = use_begin; useIt != use_end; useIt++) {
			if (Instruction *useInst = dyn_cast<Instruction>(*useIt)) {
				BasicBlock *useBlock = useInst->getParent();
				// Avoid setting use bit for uses in the same block as the
				// definition. This is equivalent with the way use sets are
				// defined for the liveness analysis when working with SSA
				// form (thus having a textual-unique, dominant definition
				// for every use of a value).
				if (defInst == NULL || (defInst != NULL && defInst->getParent() != useBlock)) {
					BasicBlockLivenessInfo *useBlockInfo = blockToInfo[useBlock];
					useBlockInfo->use->set(defBit);
				}
			}
		}
	}

	/**
	 * Set use sets for all the blocks.
	 * 
	 * @param blockToInfo Mapping from LLVM basic blocks to liveness information.
	 * @param instToLatticeBit Mapping from definitions to lattice bits.
	 * @param F LLVM function object.
	 */
	void initBlocksUse(BlockInfoMapping &blockToInfo,
	                   LatticeEncoding &instToLatticeBit,
	                   Function &F) {
		for (inst_iterator defIt = inst_begin(F); defIt != inst_end(F); defIt++) {
			Instruction *defInst = &*defIt;
			markUses(defIt->use_begin(), defIt->use_end(), blockToInfo, defInst, instToLatticeBit[defInst]);
		}
		for (Function::arg_iterator defIt = F.arg_begin(); defIt != F.arg_end(); defIt++) {
			markUses(defIt->use_begin(), defIt->use_end(), blockToInfo, NULL, instToLatticeBit[&*defIt]);
		}
	}

	typedef std::map<std::pair<BasicBlock *, BasicBlock *>, BitVector *> FlowMask;

	/**
	 * Set flow mask for the current function.
	 * 
	 * Liveness analysis is tricky using the SSA form because of the phi nodes.
	 * A phi node appears to use more than one value, but this is actually
	 * flow-sensitive. Basically, a phi node looks - data-flow wise - as a
	 * different instruction (using a single value) from each incoming block.
	 * Therefore, when computing out set for each incoming block, the in sets
	 * for the successors containing phi nodes will be adjusted accordingly by
	 * using this mask. 
	 * 
	 * @param mask The flow mask between blocks containing phi-nodes and the
	 *             incoming blocks.
	 * @param blockToInfo Mapping from LLVM basic blocks to liveness information.
	 * @param instToLatticeBit Mapping from definitions to lattice bits.
	 * @param F LLVM function object. 
	 */
	void initMask(FlowMask &mask,
	              BlockInfoMapping &blockToInfo,
	              LatticeEncoding &instToLatticeBit,
	              Function &F) {
		for (inst_iterator instIt = inst_begin(F); instIt != inst_end(F); instIt++) {
			if (!isa<PHINode>(&*instIt))
				continue;
			PHINode *phiNode = dyn_cast<PHINode>(&*instIt);
			BasicBlock *phiBlock = phiNode->getParent();
			for (unsigned crtIncomingIdx = 0; crtIncomingIdx < phiNode->getNumIncomingValues(); crtIncomingIdx++) {
				BasicBlock *crtBlock = phiNode->getIncomingBlock(crtIncomingIdx);
				for (unsigned otherIncomingIdx = 0; otherIncomingIdx < phiNode->getNumIncomingValues(); otherIncomingIdx++) {
					if (otherIncomingIdx == crtIncomingIdx)
						continue;
					Value *otherValue = phiNode->getIncomingValue(otherIncomingIdx);
					unsigned otherBit = instToLatticeBit[otherValue];
					std::pair<BasicBlock *, BasicBlock *> maskKey = std::make_pair(crtBlock, phiBlock);
					std::map<std::pair<BasicBlock *, BasicBlock *>, BitVector *>::iterator maskIt =
						mask.find(maskKey);
					if (maskIt == mask.end()) {
						mask[maskKey] = new BitVector(instToLatticeBit.size(), true);
					}
					mask[maskKey]->reset(otherBit);
				}
			}
		}
	}

	void printBitVector(BitVector *bv) {
		for (unsigned bit = 0; bit < bv->size(); bit++)
			dbgs() << (bv->test(bit) ? '1' : '0');
	}

	virtual bool runOnFunction(Function &F) {
		// initialize encoding
		LatticeEncoding instToLatticeBit;
		initInstrToLatticeBit(instToLatticeBit, F);
		unsigned defcount = instToLatticeBit.size();

		// initialize block to info mapping
		BlockInfoMapping blockToInfo;
		initBlocksInfo(blockToInfo, defcount, F);

		// initialize def and use sets
		initEntryArgDef(blockToInfo[F.begin()], F.arg_size());
		for (BlockInfoMapping::iterator it = blockToInfo.begin(); it != blockToInfo.end(); it++) {
			initBlockDef(it->second, instToLatticeBit);
		}
		initBlocksUse(blockToInfo, instToLatticeBit, F);

		// initialize flow mask
		FlowMask mask;
		initMask(mask, blockToInfo, instToLatticeBit, F);

		// compute fixed-point liveness information - no sorting of
		// blocks in quasi-topological order, works anyway
		bool inChanged = true;
		while (inChanged) {
			inChanged = false;
			// out[B] = U(in[S] & mask[B][S]) where B < S
			for (Function::iterator B = F.begin(); B != F.end(); B++) {
				(blockToInfo[B]->out)->reset();
				for (succ_iterator succIt = succ_begin(B); succIt != succ_end(B); succIt++) {
					BasicBlock *S = *succIt;
					std::pair<BasicBlock *, BasicBlock *> key =
						std::make_pair(B, S);
					if (mask.find(key) != mask.end()) {
						*(blockToInfo[B]->out) |= (*(blockToInfo[S]->in) & *(mask[key]));
					} else {
						*(blockToInfo[B]->out) |= *(blockToInfo[S]->in);
					}
				}
				// in[B] = use[B] U (out[B] - def[B])
				BitVector oldIn = *(blockToInfo[B]->in);
				*(blockToInfo[B]->in) = (*(blockToInfo[B]->use) | (*(blockToInfo[B]->out) & ~(*(blockToInfo[B]->def))));
				if (*(blockToInfo[B]->in) != oldIn) {
					inChanged = true;
				}
			}
		}

		// dump information to stderr
		dbgs() << "Function name: " << F.getName() << "\n";
		dbgs() << "-------------------\n";
		dbgs() << "#blocks = " << blockToInfo.size() << '\n';
		dbgs() << "#insts = " << defcount << "\n\n";
		for (Function::iterator blockIt = F.begin(); blockIt != F.end(); blockIt++) {
			dbgs() << "[ " << blockIt->getName() << " ]\n";
			for (BasicBlock::iterator instIt = blockIt->begin(); instIt != blockIt->end(); instIt++) {
				dbgs() << *instIt << "\n";
			}
			BasicBlockLivenessInfo *info = blockToInfo[blockIt];
			BitVector *def = info->def;
			BitVector *use = info->use;
			BitVector *in = info->in;
			BitVector *out = info->out;
			dbgs() << "Def:\t";
			printBitVector(def);
			dbgs() << '\n';
			dbgs() << "Use:\t";
			printBitVector(use);
			dbgs() << '\n';
			dbgs() << "In:\t";
			printBitVector(in);
			dbgs() << '\n';
			dbgs() << "Out:\t";
			printBitVector(out);
			dbgs() << '\n';
		}
		dbgs() << "\nMask size: " << mask.size() << '\n';
		for (std::map<std::pair<BasicBlock *, BasicBlock *>, BitVector *>::iterator maskIt = mask.begin(); maskIt != mask.end(); maskIt++) {
			dbgs() << (maskIt->first).first->getName() << " -> " << (maskIt->first).second->getName() << ":\t";
			printBitVector(maskIt->second);
			dbgs() << '\n';
		}
		dbgs() << "===================\n";

		// TODO: free memory

		return true;
	}

	virtual void getAnalysisUsage(AnalysisUsage &AU) const {
		AU.setPreservesCFG();
	}

};
}

char Analysis::ID = 0;
INITIALIZE_PASS(Analysis, "analysis", "Analysis World Pass", false, false);

FunctionPass* llvm::createAnalysisPass() {
	return new Analysis();
}
