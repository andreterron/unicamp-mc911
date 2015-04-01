package llvmast;
public class LlvmBitwiseBinary extends LlvmInstruction{
	
	public LlvmRegister lhs;
	int operationCode;
	public LlvmType type;
	public LlvmValue op1, op2;
	public String operation;
	
    public LlvmBitwiseBinary(LlvmRegister lhs,  int operationCode, LlvmType type, LlvmValue op1, LlvmValue op2){
	   this.lhs = lhs;
	   this.type = type;
	   this.operationCode = operationCode;
	   this.op1 = op1;
	   this.op2 = op2;
	   
    	switch (this.operationCode){
    	case AND : {this.operation = "and";}
    	case XOR : {this.operation = "xor";}
    		
    	}
    }
    
    public String toString(){
	   return "  " + lhs + " = " + operation + " " + type + " " + op1 + ", " + op2;
	   
	   // Reference
		// <result> = and <ty> <op1>, <op2>   ; yields ty:result
    }
    
    public static final int AND  = 1;
    public static final int XOR  = 2;
    
}