#include "Liveness.h"
using namespace llvm;
namespace {
   struct DCE : public FunctionPass {
      static char ID;
    	DCE() : FunctionPass(ID) {}

    	virtual bool runOnFunction(Function &F) {
        // Liveness &L = getAnalysis<Liveness>();

        //...


         return false;
      }

    	virtual void getAnalysisUsage(AnalysisUsage &AU) const {
         //AU.addRequired<Liveness>();
		   return;
    	}
   };
}

char DCE::ID = 0;

RegisterPass<DCE> X("DCELive", "DCELive", false, false);

