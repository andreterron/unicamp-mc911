package llvmast;

import java.util.*;

public class LlvmClassDeclaration extends LlvmInstruction {

	public LlvmClassType classType;
	public LlvmClassType superClass = null;
	public List<LlvmType> fieldTypes;
	
	public LlvmClassDeclaration(LlvmClassType type, List<LlvmType> fields) {
		classType = type;
		fieldTypes = fields;
	}
	
	public LlvmClassDeclaration(LlvmClassType type, List<LlvmType> fields, LlvmClassType superC) {
		classType = type;
		fieldTypes = fields;
		superClass = superC;
	}

	public String toString(){
		//%class.Conta = type { i32, i32 }

		//String f = StringUtils.join(fields, ", ");
		String result = classType + " = type { ";
		int join = 0;
		if (superClass != null) {
			result += superClass;
			join = 1;
		}
		for (LlvmType type : fieldTypes) {
			result  += (join == 1 ? ", " + type : type);
			join = 1;
		}
		return result + " }";
	}
}