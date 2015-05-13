#include "llvm/IR/Instructions.h"
#include "Liveness.h"

#include <unistd.h>
#include <stdio.h>

using namespace std;

void print_elem(const Value* i) {
  errs() << i->getName() << " ";
}

bool Liveness::isLiveOut(Instruction *I, Value *V){
    return false;
}

void Liveness::computeBBDefUse(Function &F){

errs() << "IN1" << '\n';

   for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
   
      LivenessInfo s;
      for (BasicBlock::iterator i = b->begin(), e = b->end(); i != e; ++i) {

         errs() << "INS:" << *i << '\n';
         for (Instruction::op_iterator o = i->op_begin(), oe = i->op_end(); o != oe; ++o) {
            Value *v = *o;
            /*if(isa<Instruction>(*v) || isa<Argument>(*v)){
         		errs() << "\tOPE:" << *v << '\n';
         	}*/
         	if(isa<Instruction>(*v)){
         		errs() << "\tOPE-I:" << *v << '\n';
         	}
         	else if(isa<Argument>(*v)){
         		errs() << "\tOPE-A:" << *v << '\n';
         	}
         }
      }
          
/* GEN
          unsigned n = i->getNumOperands();
          for (unsigned j = 0; j < n; j++) {
            Value *v = i->getOperand(j);
            if (isa<Instruction>(v)) {
              Instruction *op = cast<Instruction>(v);
              if (!s.kill.count(op))
                s.gen.insert(op);
            }
          }
          // KILL
          s.kill.insert(&*i);

      }
      bbMap.insert(std::make_pair(&*b, s));
*/

   }
}

void Liveness::computeBBInOut(Function &F){
errs() << "IN2" << '\n';
}

void Liveness::computeIInOut(Function &F) {
errs() << "IN3" << '\n';
}

bool Liveness::runOnFunction(Function &F) {
errs() << "IN0" << '\n';
    computeBBDefUse(F);
    computeBBInOut(F);
    computeIInOut(F);
	return false;
}

char Liveness::ID = 0;

RegisterPass<Liveness> X("liveness", "Live vars analysis", false, false);





