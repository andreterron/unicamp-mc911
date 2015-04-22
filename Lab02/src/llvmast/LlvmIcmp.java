package llvmast;
public  class LlvmIcmp extends LlvmInstruction{
   public LlvmRegister lhs;
   int conditionCode;
   public LlvmType type;
   public LlvmValue op1, op2;
   String condition;
       
    public LlvmIcmp(LlvmRegister lhs, int conditionCode, LlvmType type, LlvmValue op1, LlvmValue op2){
	   this.lhs = lhs;
	   this.type = type;
	   this.conditionCode = conditionCode;
	   this.op1 = op1;
	   this.op2 = op2;
	
	   switch(this.conditionCode) {
         case EQ:  {this.condition = "eq" ;}
         case NE:  {this.condition = "ne" ;}
         case UGT: {this.condition = "ugt";}
         case UGE: {this.condition = "uge";}
         case ULT: {this.condition = "ult";}
         case ULE: {this.condition = "ule";}
         case SGT: {this.condition = "sgt";}
         case SGE: {this.condition = "sge";}
         case SLT: {this.condition = "slt";}
         case SLE: {this.condition = "sle";}
         default:  {this.condition =  null;}
      }   
    }  
   
    public String toString(){
	   return "  " + lhs + " = icmp " + condition + " " + type + " " + op1 + ", " + op2;
	   
	   // Reference
	   // <result> = icmp <cond> <ty> <op1>, <op2>
    }
    
    public static final int EQ  = 1;
    public static final int NE  = 2;
    public static final int UGT = 3;
    public static final int UGE = 4;
    public static final int ULT = 5;
    public static final int ULE = 6;
    public static final int SGT = 7;
    public static final int SGE = 8;
    public static final int SLT = 9;
    public static final int SLE = 10;
    
}
