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
    return false;
}

void Liveness::computeBBDefUse(Function &F){

errs() << "IN1" << '\n';

   for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
   
      // TODO : Criar um liveness para cada bloco
      // LivenessInfo info;
      // bbLivenessMap.insert(make_pair(b, info));
      // s = &((&*bbLivenessMap.find(&*b))->second);
      LivenessInfo s;
      
      for (BasicBlock::iterator i = b->begin(), e = b->end(); i != e; ++i) {

         errs() << "INS:" << *i << '\n';
         
         s.def.insert(i);
         
         for (Instruction::op_iterator o = i->op_begin(), oe = i->op_end(); o != oe; ++o) {
            Value *v = *o;
            
            if(isa<Instruction>(*v)){
                errs() << "\tOPE-I:" << *v << '\n';
                s.use.insert(v);
            }
            
            else if(isa<Argument>(*v)){
              errs() << "\tOPE-A:" << *v << '\n';
               s.use.insert(v);
            }
         }
      }

      bbLivenessMap.insert(std::make_pair(&*b, s));

   }
}

void Liveness::computeBBInOut(Function &F){
errs() << "IN2" << '\n';
  for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {

    LivenessInfo s;
    for (BasicBlock::iterator i = b->begin(), e = b->end(); i != e; ++i) {
    }
  }
}

void Liveness::computeIInOut(Function &F) {
  errs() << "IN3" << '\n';
  
/*  LivenessInfo *s;
  std::set<const Value *> in2, out2;
  int changed;
  
  for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
    s = &((&*bbLivenessMap.find(&*b))->second);
    s->in.clear();
    s->out.clear();
  }
  do {
    changed = 0;
    // This for is going backwards
    for (Function::iterator b = F.end(), e = F.begin(); b != e; --b) {
      s = &((&*bbLivenessMap.find(&*b))->second);
      in2 = s->in;
      out2 = s->out;
      
      // TODO : do something
      // in[n] = use[n] U (out[n] - def[n])
      // out[n] = U(s in succ[n]) in[s]
      
      if (in2 != s->in || out2 != s->out) {
        changed++;
      }
    }
  } while (changed);*/
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





