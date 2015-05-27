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

void Liveness::computeBBDefUse(Function &F){

errs() << "IN1" << '\n';

   for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
   
      // TODO : Criar um liveness para cada bloco
      // LivenessInfo info;
      // bbLivenessMap.insert(make_pair(b, info));
      // bbInfo = &((&*bbLivenessMap.find(&*b))->second);
      LivenessInfo bbInfo;
      
      for (BasicBlock::iterator i = b->begin(), e = b->end(); i != e; ++i) {

         LivenessInfo iInfo;
        
         errs() << "INS:" << *i << '\n';
         
         bbInfo.def.insert(i);
         iInfo.def.insert(i);
         
         for (Instruction::op_iterator o = i->op_begin(), oe = i->op_end(); o != oe; ++o) {
            Value *v = *o;
            
            if(isa<Instruction>(*v)){
                errs() << "\tOPE-I:" << *v << '\n';
                bbInfo.use.insert(v);
                iInfo.use.insert(v);
            }
            
            else if(isa<Argument>(*v)){
              errs() << "\tOPE-A:" << *v << '\n';
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
  errs() << "IN2" << '\n';
  
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
    for (Function::iterator b = F.end(), e = F.begin(); b != e; --b) {
      info = &((&*bbLivenessMap.find(&*b))->second);
      in2 = info->in;
      out2 = info->out;
      diff.clear();
      
      // TODO : do something
      
      // out[n] = U(s in succ[n]) in[s]
      info->out.clear();
      for (Function::iterator s = F.end(); b != s; --s) {
        sInfo = &((&*bbLivenessMap.find(&*s))->second);
        info->out.insert(sInfo->in.begin(), sInfo->in.end());
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
      
      if (in2 != info->in || out2 != info->out) {
        changed++;
      }
    }
  } while (changed);
}

void Liveness::computeIInOut(Function &F) {
  errs() << "IN3" << '\n';
  for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {

    LivenessInfo s;
    for (BasicBlock::iterator i = b->begin(), e = b->end(); i != e; ++i) {
    }
  }
  
  LivenessInfo *info, *sInfo;
  std::set<const Value *> in2, out2, diff;
  //std::set<const Value *>::iterator it;
  int changed;
  
  
  for (Function::iterator b = F.begin(), e = F.end(); b != e; ++b) {
    for (BasicBlock::iterator i = b->begin(), ie = b->end(); i != ie; ++i) {
      info = &((&*iLivenessMap.find(&*i))->second);
      info->in.clear();
      info->out.clear();
    }
  }
  do {
    changed = 0;
    // This for is going backwards
    for (Function::iterator b = F.end(), e = F.begin(); b != e; --b) {
      for (BasicBlock::iterator i = b->end(), ie = b->begin(); i != ie; --i) {
        info = &((&*iLivenessMap.find(&*i))->second);
        in2 = info->in;
        out2 = info->out;
        diff.clear();
        
        // TODO : do something
        
        // out[n] = U(s in succ[n]) in[s]
        info->out.clear();
        for (Function::iterator s = F.end(); ; --s) {
          for (BasicBlock::iterator i2 = b->end(); i != i2; --i2) {
            sInfo = &((&*iLivenessMap.find(&*i2))->second);
            info->out.insert(sInfo->in.begin(), sInfo->in.end());
          }
          if (s == b) {
            break;
          }
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
        
        if (in2 != info->in || out2 != info->out) {
          changed++;
        }
      }
    }
  } while (changed);
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





