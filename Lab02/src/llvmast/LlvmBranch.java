package llvmast;
public  class LlvmBranch extends LlvmInstruction{

    public LlvmLabelValue brTrue = null; 
    public LlvmLabelValue brFalse = null;
    public LlvmValue cond = null;
    
    public LlvmBranch(LlvmLabelValue label){
   	this.brTrue = label;
    }
    
    public LlvmBranch(LlvmValue cond,  LlvmLabelValue brTrue, LlvmLabelValue brFalse){
   	this.cond = cond;
   	this.brTrue = brTrue;
   	this.brFalse = brFalse;
    }

    public String toString(){
      if(cond != null) {
         // Conditional branch

   	   return "  " + "br i1 " + cond + ", label %" + brTrue + ", label %" + brFalse;

         // Reference
         // br i1 <cond>, label <iftrue>, label <iffalse>   	   
   	   
      }
      else if (brTrue != null) {
         // Unconditional branch

         return "  " + "br label %" + brTrue;

         // Reference
         // br label <dest>
      }
      else {
         return null;
      }
    }
}
