#include "Liveness.h"
#include <llvm/IR/IntrinsicInst.h>
using namespace llvm;
namespace {
   struct DCE : public FunctionPass {
      static char ID;
    	DCE() : FunctionPass(ID) {}

    	virtual bool runOnFunction(Function &F) {
          errs() << "DCE LIVENESS\n";
          int changed;
          bool isAlive, liveOut;
          Liveness &L = getAnalysis<Liveness>();
          do {
            changed = 0;
            for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
              for (BasicBlock::iterator i = b->begin(), ie = b->end(); i != ie; ++i) {
                L.runOnFunction(F);
                liveOut = L.isLiveOut(&*i, &*i);
                isAlive = (i->mayHaveSideEffects() ||
                    isa<TerminatorInst>(&*i) ||
                    isa<DbgInfoIntrinsic>(&*i) ||
                    isa<LandingPadInst>(&*i) ||
                    liveOut);
                errs() << "INS:" << *i << '\n';
                errs() << "\tlive = " << isAlive << '\n';
                errs() << "\tlout = " << liveOut << '\n';
                if (!isAlive) {
                  Instruction *dead = &*i;
                  i--;
                  dead->eraseFromParent();
                  changed = true;
                }
              }
            }
          } while (changed);
        //...


         return false;
      }

    	virtual void getAnalysisUsage(AnalysisUsage &AU) const {
           AU.addRequired<Liveness>();
		   return;
    	}
   };
}

char DCE::ID = 0;

RegisterPass<DCE> X("DCELive", "DCELive", false, false);

