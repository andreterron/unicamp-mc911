package llvmast;
public class LlvmClassType extends LlvmType {

	public String name;
	
	public LlvmClassType(String n) {
		name = n;
	}

	public String toString(){
	//%class.Conta = type { i32, i32 }
		return "%class." + name;
	}
}