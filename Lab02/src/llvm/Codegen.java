/*****************************************************
Esta classe Codegen é a responsável por emitir LLVM-IR. 
Ela possui o mesmo método 'visit' sobrecarregado de
acordo com o tipo do parâmetro. Se o parâmentro for
do tipo 'While', o 'visit' emitirá código LLVM-IR que 
representa este comportamento. 
Alguns métodos 'visit' já estão prontos e, por isso,
a compilação do código abaixo já é possível.

class a{
    public static void main(String[] args){
    	System.out.println(1+2);
    }
}

O pacote 'llvmast' possui estruturas simples 
que auxiliam a geração de código em LLVM-IR. Quase todas 
as classes estão prontas; apenas as seguintes precisam ser 
implementadas: 

// llvmasm/LlvmBranch.java -> OK? Funciona no If
// llvmasm/LlvmIcmp.java -> OK? Funciona no LessThan e Equal
// llvmasm/LlvmMinus.java -> OK
// llvmasm/LlvmTimes.java -> OK


Todas as assinaturas de métodos e construtores 
necessárias já estão lá. 


Observem todos os métodos e classes já implementados
e o manual do LLVM-IR (http://llvm.org/docs/LangRef.html) 
como guia no desenvolvimento deste projeto. 

****************************************************/
package llvm;

import semant.Env;
import syntaxtree.*;
import llvmast.*;

import java.util.*;

public class Codegen extends VisitorAdapter{
	private List<LlvmInstruction> assembler;
	private Codegen codeGenerator;

  	private SymTab symTab;
	private ClassNode classEnv; 	// Aponta para a classe atualmente em uso em symTab
	private MethodNode methodEnv; 	// Aponta para a metodo atualmente em uso em symTab


	public Codegen(){
		assembler = new LinkedList<LlvmInstruction>();
	}

	// Método de entrada do Codegen
	public String translate(Program p, Env env){	
		codeGenerator = new Codegen();
		
		// Preenchendo a Tabela de Símbolos
		// Quem quiser usar 'env', apenas comente essa linha
		// codeGenerator.symTab.FillTabSymbol(p);
		
		// Formato da String para o System.out.printlnijava "%d\n"
		codeGenerator.assembler.add(new LlvmConstantDeclaration("@.formatting.string", "private constant [4 x i8] c\"%d\\0A\\00\""));	

		// NOTA: sempre que X.accept(Y), então Y.visit(X);
		// NOTA: Logo, o comando abaixo irá chamar codeGenerator.visit(Program), linha 75
		p.accept(codeGenerator);

		// Link do printf
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);
		codeGenerator.assembler.add(new LlvmExternalDeclaration("@printf", LlvmPrimitiveType.I32, pts)); 
		List<LlvmType> mallocpts = new LinkedList<LlvmType>();
		mallocpts.add(LlvmPrimitiveType.I32);
		codeGenerator.assembler.add(new LlvmExternalDeclaration("@malloc", new LlvmPointer(LlvmPrimitiveType.I8),mallocpts)); 


		String r = new String();
		for(LlvmInstruction instr : codeGenerator.assembler)
			r += instr+"\n";
		return r;
	}

	public LlvmValue visit(Program n){
		n.mainClass.accept(this);

		for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
			c.head.accept(this);

		return null;
	}

	public LlvmValue visit(MainClass n){
		
		// definicao do main 
		assembler.add(new LlvmDefine("@main", LlvmPrimitiveType.I32, new LinkedList<LlvmValue>()));
		assembler.add(new LlvmLabel(new LlvmLabelValue("entry")));
		LlvmRegister R1 = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I32));
		assembler.add(new LlvmAlloca(R1, LlvmPrimitiveType.I32, new LinkedList<LlvmValue>()));
		assembler.add(new LlvmStore(new LlvmIntegerLiteral(0), R1));

		// Statement é uma classe abstrata
		// Portanto, o accept chamado é da classe que implementa Statement, por exemplo,  a classe "Print". 
		n.stm.accept(this);  

		// Final do Main
		LlvmRegister R2 = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmLoad(R2,R1));
		assembler.add(new LlvmRet(R2));
		assembler.add(new LlvmCloseDefinition());
		return null;
	}
	
	public LlvmValue visit(Plus n){
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmPlus(lhs,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	
	public LlvmValue visit(Print n){

		LlvmValue v =  n.exp.accept(this);

		// getelementptr:
		LlvmRegister lhs = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I8));
		LlvmRegister src = new LlvmNamedValue("@.formatting.string",new LlvmPointer(new LlvmArray(4,LlvmPrimitiveType.I8)));
		List<LlvmValue> offsets = new LinkedList<LlvmValue>();
		offsets.add(new LlvmIntegerLiteral(0));
		offsets.add(new LlvmIntegerLiteral(0));
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		args.add(lhs);
		args.add(v);
		assembler.add(new LlvmGetElementPointer(lhs,src,offsets));

		pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);
		
		// printf:
		assembler.add(new LlvmCall(new LlvmRegister(LlvmPrimitiveType.I32),
				LlvmPrimitiveType.I32,
				pts,				 
				"@printf",
				args
				));
		return null;
	}
	
	public LlvmValue visit(IntegerLiteral n){
		return new LlvmIntegerLiteral(n.value);
	};
	
	public LlvmValue visit(If n){
		int line = n.line;
		LlvmValue cmp = n.condition.accept(this);
		LlvmLabelValue trueLabel = new LlvmLabelValue("true" + line);
		//LlvmLabelValue elseLabel = new LlvmLabelValue("else" + line);
		LlvmLabelValue endLabel = new LlvmLabelValue("end" + line);
		assembler.add(new LlvmBranch(cmp, trueLabel, endLabel));
		assembler.add(new LlvmLabel(trueLabel));
		n.thenClause.accept(this);
		assembler.add(new LlvmBranch(null, endLabel, null));
		assembler.add(new LlvmLabel(endLabel));
		//n.elseClause.accept(this);
		//assembler.add(new LlvmLabel(endLabel));
		return cmp;
	}

   public LlvmValue visit(While n){
		int line = n.line;
		LlvmLabelValue whileLabel = new LlvmLabelValue("while" + line);
		LlvmLabelValue doLabel = new LlvmLabelValue("do" + line);
		LlvmLabelValue endLabel = new LlvmLabelValue("end" + line);
		assembler.add(new LlvmBranch(null, whileLabel, null));
		assembler.add(new LlvmLabel(whileLabel));
		LlvmValue cmp = n.condition.accept(this);
		assembler.add(new LlvmBranch(cmp, doLabel, endLabel));
      assembler.add(new LlvmLabel(doLabel));
  		n.body.accept(this);
		assembler.add(new LlvmBranch(null, whileLabel, null));
		assembler.add(new LlvmLabel(endLabel));
		return null;
	}
	
	public LlvmValue visit(And n){
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmBitwiseBinary(lhs,LlvmBitwiseBinary.AND,LlvmPrimitiveType.I1,v1,v2));
		return lhs;
	}	

	public LlvmValue visit(LessThan n){
	   LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmIcmp(lhs,LlvmIcmp.ULT,LlvmPrimitiveType.I32,v1,v2)); // Conferir se entrada ser unsigned ou signed pode ser um problema
		return lhs;
	}
	
	public LlvmValue visit(Equal n){
	   LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmIcmp(lhs,LlvmIcmp.EQ,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	
	public LlvmValue visit(Minus n){
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmMinus(lhs,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	
	public LlvmValue visit(Times n){
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmTimes(lhs,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	
	public LlvmValue visit(True n){
		return new LlvmBool(LlvmBool.TRUE);
	}
	public LlvmValue visit(False n){
		return new LlvmBool(LlvmBool.FALSE);
	}
	
	public LlvmValue visit(Not n){
		LlvmValue v = n.exp.accept(this);
		LlvmBool t = new LlvmBool(LlvmBool.TRUE);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmBitwiseBinary(lhs,LlvmBitwiseBinary.XOR,LlvmPrimitiveType.I1,t,v));
		return lhs;
	}
		
	/**
	 * Not tested yet
	 */
	 
	public LlvmValue visit(Assign n){
   	LlvmValue var = n.var.accept(this);
   	LlvmValue exp = n.exp.accept(this);
		assembler.add(new LlvmStore(exp, var));
	   return null;
	}
	
	public LlvmValue visit(ArrayAssign n){
	   LlvmValue var = n.var.accept(this);
   	LlvmValue index = n.index.accept(this);
   	LlvmValue value = n.value.accept(this);

		LlvmRegister lhs = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I32));
		List<LlvmValue> offsets = new LinkedList<LlvmValue>();
		offsets.add(index); // Calcula offset
		assembler.add(new LlvmGetElementPointer(lhs,var,offsets)); // Pega ponteiro para posicao desejada com offset
		
		assembler.add(new LlvmStore(value, lhs)); 
	   return null;
	}

	public LlvmValue visit(Block n){
		for (util.List<Statement> c = n.body; c != null; c = c.tail) {
		//for (Statement stmt : n.body.toArray()) {
		//for (int i = 0; i < n.body.size(); i++) {
			//Statement stmt = n.body.get(i);
			c.head.accept(this);
		}
		return null;
	}
	
	public LlvmValue visit(ClassDeclSimple n){
		
		List<LlvmType> types = new LinkedList();
		for (util.List<VarDecl> c = n.varList; c != null; c = c.tail) {
			types.add(c.head.accept(this).type);
		}
		
		assembler.add(new LlvmClassDeclaration(new LlvmClassType(n.name.s), types));
		for (util.List<MethodDecl> c = n.methodList; c != null; c = c.tail) {
		//for (Statement stmt : n.body.toArray()) {
		//for (int i = 0; i < n.body.size(); i++) {
			//Statement stmt = n.body.get(i);
			c.head.accept(this);
		}
		//for (MethodDecl method : n.methodList) {
		//	method.accept(this);
		//}
		return null;
	}
	
	public LlvmValue visit(VarDecl n){
		return new LlvmNamedValue(n.name.s, n.type.accept(this).type);
	}
	
	public LlvmValue visit(MethodDecl n){
		LlvmType resultType = n.returnType.accept(this).type;
		
		assembler.add(new LlvmDefine("@__" + n.name.s, resultType, new LinkedList()));
		assembler.add(new LlvmCloseDefinition());
		return null;
	}
	
	public LlvmValue visit(BooleanType n){
		return new LlvmNamedValue("boolean", LlvmPrimitiveType.I1);
	}
	
	public LlvmValue visit(IntegerType n){
		return new LlvmNamedValue("int", LlvmPrimitiveType.I32);
	}
	
	// Todos os visit's que devem ser implementados	
	//public LlvmValue visit(ClassDeclSimple n){return null;}
	public LlvmValue visit(ClassDeclExtends n){return null;}
	//public LlvmValue visit(VarDecl n){return null;}
	//public LlvmValue visit(MethodDecl n){return null;}
	public LlvmValue visit(Formal n){return null;}
	public LlvmValue visit(IntArrayType n){return null;}
//	public LlvmValue visit(BooleanType n){return null;}
//	public LlvmValue visit(IntegerType n){return null;}
	public LlvmValue visit(IdentifierType n){return null;}
//	public LlvmValue visit(Block n){return null;} // Falta testar
//	public LlvmValue visit(If n){return null;} // OK
//	public LlvmValue visit(While n){return null;} // OK
//	public LlvmValue visit(Assign n){return null;} // Falta testar
//	public LlvmValue visit(ArrayAssign n){return null;} // Falta testar
//	public LlvmValue visit(And n){return null;} // OK
//	public LlvmValue visit(LessThan n){return null;} OK
//	public LlvmValue visit(Equal n){return null;} OK
//	public LlvmValue visit(Minus n){return null;} // OK
//	public LlvmValue visit(Times n){return null;} // OK
	public LlvmValue visit(ArrayLookup n){return null;}
	public LlvmValue visit(ArrayLength n){return null;}
	public LlvmValue visit(Call n){return null;}
//	public LlvmValue visit(True n){return null;} // OK
//	public LlvmValue visit(False n){return null;} // OK
	public LlvmValue visit(IdentifierExp n){return null;}
	public LlvmValue visit(This n){return null;}
	public LlvmValue visit(NewArray n){return null;}
	public LlvmValue visit(NewObject n){return null;}
//	public LlvmValue visit(Not n){return null;} // OK
	public LlvmValue visit(Identifier n){return null;}
}


/**********************************************************************************/
/* === Tabela de Símbolos ==== 
 * 
 * 
 */
/**********************************************************************************/

class SymTab extends VisitorAdapter{
    public Map<String, ClassNode> classes;     
    private ClassNode classEnv;    //aponta para a classe em uso

    public LlvmValue FillTabSymbol(Program n){
	n.accept(this);
	return null;
}
public LlvmValue visit(Program n){
	n.mainClass.accept(this);

	for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
		c.head.accept(this);

	return null;
}

public LlvmValue visit(MainClass n){
	classes.put(n.className.s, new ClassNode(n.className.s, null, null));
	return null;
}

public LlvmValue visit(ClassDeclSimple n){
	List<LlvmType> typeList = null;
	// Constroi TypeList com os tipos das variáveis da Classe (vai formar a Struct da classe)
	
	List<LlvmValue> varList = null;
	// Constroi VarList com as Variáveis da Classe

	classes.put(n.name.s, new ClassNode(n.name.s, 
										new LlvmStructure(typeList), 
										varList)
      			);
    	// Percorre n.methodList visitando cada método
	return null;
}

	public LlvmValue visit(ClassDeclExtends n){return null;}
	public LlvmValue visit(VarDecl n){return null;}
	public LlvmValue visit(Formal n){return null;}
	public LlvmValue visit(MethodDecl n){return null;}
	public LlvmValue visit(IdentifierType n){return null;}
	public LlvmValue visit(IntArrayType n){return null;}
	public LlvmValue visit(BooleanType n){return null;}
	public LlvmValue visit(IntegerType n){return null;}
}

class ClassNode extends LlvmType {
	ClassNode (String nameClass, LlvmStructure classType, List<LlvmValue> varList){
	}
}

class MethodNode {
}




