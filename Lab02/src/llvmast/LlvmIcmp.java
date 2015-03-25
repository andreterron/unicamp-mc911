package llvmast;
public  class LlvmIcmp extends LlvmInstruction{
   public LlvmRegister lhs;
   int conditionCode;
   public LlvmType type;
   public LlvmValue op1, op2;
   String condition;
       
    public LlvmIcmp(LlvmRegister lhs,  int conditionCode, LlvmType type, LlvmValue op1, LlvmValue op2){
	this.lhs = lhs;
	this.type = type;
	this.conditionCode = conditionCode;
	this.op1 = op1;
	this.op2 = op2;
	switch(conditionCode) {
      case 1: this.condition = "eq";
             break;
      case 2: this.condition = "ne";
             break;
      case 3: this.condition = "ugt";
             break;
      case 4: this.condition = "uge";
             break;
      case 5: this.condition = "ult";
             break;
      case 6: this.condition = "ule";
             break;
      case 7: this.condition = "sgt";
             break;
      case 8: this.condition = "sge";
             break;
      case 9: this.condition = "slt";
             break;
      case 10: this.condition = "sle";
             break;
      default: this.condition = "NULL"; 
             break;
    }   
   }
   
    public String toString(){
	return "  " +lhs + " = icmp " + condition + " " + type + " " + op1 + ", " + op2;
    }
}
