#include "llvm/IR/Instructions.h"
#include "Liveness.h"
#include <utility>
#include <unistd.h>
#include <stdio.h>

using namespace std;

void print_elem(const Value* i) {
  errs() << i->getName() << " ";
}

bool Liveness::isLiveOut(Instruction *I, Value *V){
    LivenessInfo *info = &((&*iLivenessMap.find(I))->second);
    return (info->out.find(V) != info->out.end());
}

bool Liveness::isUsed(Instruction *I){
    int* countUse = &((&*instMap.find(I)))->second;
    if(*countUse > 0) {
      return true;
    }
    return false;
}

void Liveness::computeBBDefUse(Function &F){

   int* countUse;

   for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
      for (BasicBlock::iterator i = b->begin(), ie = b->end(); i != ie; ++i) {
        Instruction *inst = dyn_cast<Instruction>(i);
        instMap.insert(std::make_pair(&*inst, 0));
      }
   }
   
   for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
      for (BasicBlock::iterator i = b->begin(), ie = b->end(); i != ie; ++i) {
         Instruction *inst = dyn_cast<Instruction>(i);
         countUse = &((&*instMap.find(inst)))->second;
         *countUse = 0;
      }
   }


   for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
   
      LivenessInfo bbInfo;

      
      for (BasicBlock::iterator i = b->begin(), ie = b->end(); i != ie; ++i) {

         LivenessInfo iInfo;

         
         bbInfo.def.insert(i);
         iInfo.def.insert(i);
         
         for (Instruction::op_iterator o = i->op_begin(), oe = i->op_end(); o != oe; ++o) {
            Value *v = *o;
            Instruction *inst = dyn_cast<Instruction>(v);
            
            if(isa<Instruction>(*v)){
                bbInfo.use.insert(v);
                iInfo.use.insert(v);
                countUse = &((&*instMap.find(inst)))->second;
//                errs() << "\tPior used = " << *countUse << '\n';
                *countUse = *countUse + 1;
//                errs() << "INS:" << *inst << '\n';
//                errs() << "\tUpdate used = " << *countUse << '\n';
            }
            
            else if(isa<Argument>(*v)){
               bbInfo.use.insert(v);
               iInfo.use.insert(v);
            }
         }
         iLivenessMap.insert(std::make_pair(&*i, iInfo));
      }

      bbLivenessMap.insert(std::make_pair(&*b, bbInfo));

   }
}

void Liveness::computeBBInOut(Function &F){
  
  LivenessInfo *info, *sInfo;
  std::set<const Value *> in2, out2, diff;
  //std::set<const Value *>::iterator it;
  int changed;
  
  for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
    info = &((&*bbLivenessMap.find(&*b))->second);
    info->in.clear();
    info->out.clear();
  }
  do {
    changed = 0;
    // This for is going backwards
    Function::iterator b = F.end(), e = F.begin();
    do {
      --b;
      info = &((&*bbLivenessMap.find(&*b))->second);
      in2 = info->in;
      out2 = info->out;
      diff.clear();
           
      // out[n] = U(s in succ[n]) in[s]
      info->out.clear();
      for (llvm::succ_iterator s = succ_begin(b), se = succ_end(b); se != s; s++) {
        sInfo = &((&*bbLivenessMap.find(*s))->second);
        info->out.insert(sInfo->in.begin(), sInfo->in.end());
      }
      // in[n] = use[n] U (out[n] - def[n])
      info->in.clear();
      //for (set<const Value *>::iterator vi = info->use.end(), ve = info->use.begin(); vi != ve; --vi) {
      //  errs() << "Value: " << *vi << '\n';
      //}
      info->in.insert(info->use.begin(), info->use.end());
      set_difference(info->out.begin(),
                               info->out.end(),
                               info->def.begin(),
                               info->def.end(),
                               std::inserter(diff, diff.end()));
      //diff.resize(it - diff.begin());
      info->in.insert(diff.begin(), diff.end());
      
      if (in2 != info->in || out2 != info->out) {
        changed++;
      }
    } while (b != e);
  } while (changed);
}

void Liveness::computeIInOut(Function &F) {
  LivenessInfo *info, *bInfo, *nextInfo = NULL;
  std::set<const Value *> in2, out2, diff;
  //std::set<const Value *>::iterator it;
  
  
  // This for is going backwards
  Function::iterator b = F.end(), e = F.begin();
  do {
    --b;
    
    nextInfo = NULL;
    BasicBlock::iterator i = b->end(), ie = b->begin();
    do {
      --i;
      
      info = &((&*iLivenessMap.find(&*i))->second);
      in2 = info->in;
      out2 = info->out;
      diff.clear();
      
      if (nextInfo == NULL) {
        
        bInfo = &((&*bbLivenessMap.find(&*b))->second);
        info->out.clear();
        info->out.insert(bInfo->out.begin(), bInfo->out.end());
      } else {
      
        // out[n] = U(s in succ[n]) in[s]
        info->out.clear();
        info->out.insert(nextInfo->in.begin(), nextInfo->in.end());
      }
      
      // in[n] = use[n] U (out[n] - def[n])
      info->in.clear();
      info->in.insert(info->use.begin(), info->use.end());
      set_difference(info->out.begin(),
                              info->out.end(),
                              info->def.begin(),
                              info->def.end(),
                              std::inserter(diff, diff.end()));
      //diff.resize(it - diff.begin());
      info->in.insert(diff.begin(), diff.end());
      
      nextInfo = info;
    } while (i != ie);
  } while (b != e);
  //printInAndOut(F);
}

void Liveness::printValueSet(std::set<const Value *> *s) {
  for (std::set<const Value *>::iterator i = s->begin(), e = s->end(); i != e; ++i) {
    errs() << "\t\t" << **i << '\n';
  }
}

void Liveness::printInAndOut(Function &F) {
  LivenessInfo *info;
  for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
    errs() << "BLOCK:" << *b << '\n';
    for (BasicBlock::iterator i = b->begin(), ie = b->end(); i != ie; ++i) {
      info = &((&*iLivenessMap.find(&*i))->second);
      errs() << "INS:" << *i << '\n';
      errs() << "\tdef =\n";
      printValueSet(&info->def);
      errs() << "\tuse =\n";
      printValueSet(&info->use);
      errs() << "\tin  =\n";
      printValueSet(&info->in);
      errs() << "\tout =\n";
      printValueSet(&info->out);
    }
    errs() << "END BLOCK\n";
  }

}


bool Liveness::runOnFunction(Function &F) {
    computeBBDefUse(F);
    computeBBInOut(F);
    computeIInOut(F);
	return false;
}

char Liveness::ID = 0;

RegisterPass<Liveness> X("liveness", "Live vars analysis", false, false);





