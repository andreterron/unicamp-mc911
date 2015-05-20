#include "llvm/Pass.h"
#include "llvm/IR/Instructions.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/Value.h"
#include "llvm/Support/raw_ostream.h"

#define INST_LOAD_OP 27

using namespace llvm;

namespace {
  struct GVN : public FunctionPass {
    static char ID;
      int count = 0;
    GVN() : FunctionPass(ID) {}

    virtual bool runOnFunction(Function &F) {
      StoreInst* lastStore = NULL;
      for (Function::iterator blk = F.begin(), e = F.end(); blk != e; ++blk) {
        
        for (BasicBlock::iterator i = blk->begin(), e = blk->end(); i != e; ++i) {
          
          if (StoreInst* s = dynamic_cast<StoreInst*>(&*i)) {
            lastStore = s;
          } else {
            if (LoadInst* l = dynamic_cast<LoadInst*>(&*i)) {
              if (lastStore != NULL && lastStore->getPointerOperand() == l->getPointerOperand()) {
                l->replaceAllUsesWith(lastStore->getValueOperand());
                ++i;
                l->eraseFromParent();
                count++;
              }
            }
            lastStore = NULL;
          }
        }
      }
      
      return (count > 0);
    }
  };
}

char GVN::ID = 0;
static RegisterPass<GVN> X("gvn-p3", "GVN Pass", false, false);