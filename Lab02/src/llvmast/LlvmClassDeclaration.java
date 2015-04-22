package llvmast;

import java.util.*;

public class LlvmClassDeclaration extends LlvmInstruction {

	public LlvmClassType classType;
	public List<LlvmType> fieldTypes;
	
	public LlvmClassDeclaration(LlvmClassType type, List<LlvmType> fields) {
		classType = type;
		fieldTypes = fields;
	}

	public String toString(){
		//%class.Conta = type { i32, i32 }

		//String f = StringUtils.join(fields, ", ");
		String result = classType + " = type { ";
		int join = 0;
		for (LlvmType type : fieldTypes) {
			result  += type + (join == 1 ? ", " : "");
			join = 1;
		}
		return result + " }";
	}
}