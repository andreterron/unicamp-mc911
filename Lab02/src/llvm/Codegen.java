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

// llvmasm/LlvmBranch.java -> OK
// llvmasm/LlvmIcmp.java -> OK
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
		symTab = new SymTab();
	}

	// Método de entrada do Codegen
	public String translate(Program p, Env env){	
		codeGenerator = new Codegen();
		
		// Preenchendo a Tabela de Símbolos
		// Quem quiser usar 'env', apenas comente essa linha
		codeGenerator.symTab.FillTabSymbol(p);
		
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
		
		ClassNode cn = symTab.classes.get(n.mainClass.className.s);
		assembler.add(new LlvmClassDeclaration(cn, cn.structure));
		for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail) {
			cn = symTab.classes.get(c.head.name.s);
			assembler.add(new LlvmClassDeclaration(cn, cn.structure));
		}
		
		n.mainClass.accept(this);

		for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
			c.head.accept(this);

		return null;
	}

	public LlvmValue visit(MainClass n){
		//className
		//assembler.add(new LlvmClassDeclaration(new LlvmClassType(n.className.s), new LinkedList<LlvmType>()));
		
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
		
		
		//LlvmRegister reg = new LlvmRegister(LlvmPrimitiveType.I32);
		
		LlvmValue result = null;
		if (methodEnv != null) {
			//System.out.println("Searching for local: %" + var.toString());
			if (methodEnv.locals.containsKey("%" + var.toString())) {
				LlvmValue local = methodEnv.locals.get("%" + var.toString());
				//c.head.accept(this);
				//System.out.println("Searching for local %" + var.toString() + "; got: " + local.toString());
				//if (local.toString().equals(var.toString())) {
				//System.out.println("Searching for local SUCCESS!");
					//assembler.add(new LlvmLoad(reg, new LlvmNamedValue("%" + var.toString(), new LlvmPointer(LlvmPrimitiveType.I32))));
					assembler.add(new LlvmStore(exp, new LlvmNamedValue(local.toString(), new LlvmPointer(exp.type)))); 
					return exp;
					//return new LlvmNamedValue(var.toString(), local.type);
				//}
			}
		}
		if (classEnv != null) {
			int i = 0;
			for (LlvmValue c : classEnv.vars) {
				//c.head.accept(this);
				if (c.toString().equals("%" + var.toString())) {
					List<LlvmValue> offsets = new LinkedList<LlvmValue>();
					offsets.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
					offsets.add(new LlvmNamedValue("" + i, LlvmPrimitiveType.I32));
					LlvmRegister ptr = new LlvmRegister(new LlvmPointer(c.type));
					assembler.add(new LlvmGetElementPointer(ptr, new LlvmNamedValue("%this", new LlvmPointer(classEnv)), offsets));
					
					LlvmRegister a = new LlvmRegister(c.type);
					assembler.add(new LlvmBitcast(a, exp, c.type));
					assembler.add(new LlvmStore(a, ptr)); 
					/*if (exp.type instanceof LlvmPointer) {
						if (((LlvmPointer) c.type).content instanceof LlvmArray) {
							List<LlvmValue> offsets2 = new LinkedList<LlvmValue>();
							offsets2.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
							offsets2.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
							LlvmRegister a = new LlvmRegister(c.type);
							assembler.add(new LlvmGetElementPointer(a, exp, offsets2));
							assembler.add(new LlvmStore(a, ptr));
						} else {
							assembler.add(new LlvmStore(exp, ptr)); 
						}
					} else {
						assembler.add(new LlvmStore(exp, ptr)); 
					}*/
					return a;
					//new LlvmNamedValue(var.toString(), c.head.type);
				}
				i++;
			}
		}
		System.out.println("Failed to assign to " + n.var.s);
		return null;

		//LlvmRegister lhs = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I32));
		//List<LlvmValue> offsets = new LinkedList<LlvmValue>();
		//offsets.add(index); // Calcula offset
		//assembler.add(new LlvmGetElementPointer(lhs,var,offsets)); // Pega ponteiro para posicao desejada com offset
		
		//assembler.add(new LlvmStore(exp, new LlvmNamedValue("%" + var.toString(), new LlvmPointer(var.type)))); 
		//return null;
		
		
		
		
		
		
		//LlvmValue var = n.var.accept(this);
		//LlvmValue exp = n.exp.accept(this);
		//methodEnv.locals.put(n.var.s, exp);
		//LlvmRegister reg = new LlvmRegister(exp.type);
		//return exp;
		//assembler.add(new LlvmStore(exp, var));
		//return null;
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
	
	public LlvmValue visit(ArrayLookup n){
	   LlvmValue array = n.array.accept(this);
   	LlvmValue index = n.index.accept(this);

		LlvmRegister lhs = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I32));
		List<LlvmValue> offsets = new LinkedList<LlvmValue>();
		offsets.add(index); // Calcula offset
		assembler.add(new LlvmGetElementPointer(lhs,array,offsets)); // Pega ponteiro para posicao desejada com offset
		
	   return lhs;
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
		List<LlvmValue> vars = new LinkedList();
		LlvmValue val;
		for (util.List<VarDecl> c = n.varList; c != null; c = c.tail) {
			val = c.head.accept(this);
			vars.add(val);
			types.add(val.type);
		}
		classEnv = new ClassNode(n.name.s, new LlvmStructure(types), vars);
		//assembler.add(new LlvmClassDeclaration(new LlvmClassType(n.name.s), types));
		
		
		// Constructor
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		args.add(new LlvmNamedValue("%this", new LlvmPointer(new LlvmClassType(n.name.s))));
		
		assembler.add(new LlvmDefine("@__" + n.name, LlvmPrimitiveType.VOID, args));
		assembler.add(new LlvmLabel(new LlvmLabelValue("entry")));
		assembler.add(new LlvmRet(new LlvmNamedValue("", LlvmPrimitiveType.VOID)));
		assembler.add(new LlvmCloseDefinition());
		
		for (util.List<MethodDecl> c = n.methodList; c != null; c = c.tail) {
			c.head.accept(this);
		}
		return null;
	}
	
	
	public LlvmValue visit(ClassDeclExtends n){
		List<LlvmType> types = new LinkedList();
		List<LlvmValue> vars = new LinkedList();
		LlvmValue val;
		for (util.List<VarDecl> c = n.varList; c != null; c = c.tail) {
			val = c.head.accept(this);
			vars.add(val);
			types.add(val.type);
		}
		classEnv = new ClassNode(n.name.s, new LlvmStructure(types), vars, n.superClass.s);
		// Structure
		//assembler.add(new LlvmClassDeclaration(new LlvmClassType(n.name.s), types, new LlvmClassType(n.superClass.s)));
		
		// Constructor
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		args.add(new LlvmNamedValue("%this", new LlvmPointer(new LlvmClassType(n.name.s))));
		
		assembler.add(new LlvmDefine("@__" + n.name, LlvmPrimitiveType.VOID, args));
		assembler.add(new LlvmLabel(new LlvmLabelValue("entry")));
		assembler.add(new LlvmRet(new LlvmNamedValue("", LlvmPrimitiveType.VOID)));
		assembler.add(new LlvmCloseDefinition());
		
		for (util.List<MethodDecl> c = n.methodList; c != null; c = c.tail) {
			c.head.accept(this);
		}
		return null;
	}

	public LlvmValue visit(VarDecl n) {
		LlvmType typeVal = n.type.accept(this).type;
		LlvmType type = typeVal;
		if (typeVal instanceof LlvmClassType) {
			type = new LlvmPointer(type);
		}
		LlvmNamedValue val = new LlvmNamedValue("%" + n.name.s, type);
		
		
		if (methodEnv != null) {
			assembler.add(new LlvmAlloca(val, type, new LinkedList<LlvmValue>()));
		}
		//assembler.add(
		return val;
	}

	public LlvmValue visit(MethodDecl n){
	
	
		// TODO : alloc memory to method parameters (formals)
		LlvmType retType = n.returnType.accept(this).type;
		LlvmType resultType = retType;
		Map<String, LlvmValue> locals = new HashMap();
		List<LlvmValue> formals = new LinkedList<LlvmValue>();
		formals.add(new LlvmNamedValue("%this", new LlvmPointer(new LlvmClassType(classEnv.name))));
		for (util.List<Formal> c = n.formals; c != null; c = c.tail) {
			LlvmValue var = c.head.accept(this);
			if (var.type instanceof LlvmClassType) {
				//System.out.println("Method 1; var name = " + var.toString());
				//var = new LlvmNamedValue(var.toString(), new LlvmPointer(var.type));
			}
			formals.add(var); //new LlvmNamedValue("%" + c.head.name.s, c.head.type.accept(this).type));
			//locals.put(c.head.name.s, var);
		}
		if (resultType instanceof LlvmClassType) {
			resultType = new LlvmPointer(resultType);
		}
		assembler.add(new LlvmDefine("@__" + n.name.s + "_" + classEnv.name, resultType, formals));
		assembler.add(new LlvmLabel(new LlvmLabelValue("entry")));
		
		LlvmValue reg;
		for (LlvmValue formal : formals) {
			//System.out.println("Method 2; var name = " + formal.toString());
			LlvmType type = formal.type;
			//if (type instanceof LlvmClassType) {
			//	type = new LlvmPointer(type);
			//}
			reg = new LlvmRegister(new LlvmPointer(type));
			assembler.add(new LlvmAlloca(reg, type, new LinkedList<LlvmValue>()));
			assembler.add(new LlvmStore(formal, reg));
			locals.put(formal.toString(), reg);
		}
		
		methodEnv = new MethodNode(n.name.s, locals);
		//System.out.println("Method 3;");
		for (util.List<VarDecl> c = n.locals; c != null; c = c.tail) {
			//assembler.add(c.head.accept(this));
			LlvmValue local = c.head.accept(this);
			LlvmType type = local.type;
			if (type instanceof LlvmClassType) {
				type = new LlvmPointer(type);
			}
			reg = new LlvmRegister(type);
			assembler.add(new LlvmAlloca(reg, type, new LinkedList<LlvmValue>()));
			locals.put(local.toString(), reg);
		}
		
		
		//System.out.println("Method 4;");
		for (util.List<Statement> c = n.body; c != null; c = c.tail) {
			c.head.accept(this);
		}
		LlvmValue v = n.returnExp.accept(this);
		LlvmValue vVar;
		
		List<LlvmValue> offsets = new LinkedList<LlvmValue>();
		offsets.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
		offsets.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
		if (v.type instanceof LlvmPointer && resultType instanceof LlvmPointer) {
			LlvmClassType type = (LlvmClassType) ((LlvmPointer) v.type).content;
			ClassNode cNode = symTab.classes.get(type.name);
			ClassNode sClass = (cNode.superClass == null ? null : symTab.classes.get(cNode.superClass));
			int i = 0;
			while (cNode != null) {
				if (retType.toString().equals(cNode.toString())) {
					break;
				} else {
				
					vVar = new LlvmRegister(new LlvmPointer(sClass));
					assembler.add(new LlvmGetElementPointer(vVar, v, offsets));
					v = vVar;
					cNode = sClass;
					sClass = (cNode.superClass == null ? null : symTab.classes.get(cNode.superClass));
				}
				if (i++ >= 100) {
					System.out.println("FAILED!!!! to create return expression");
					break;
				}
			}
		}
		//System.out.println("Method 5;");
		
		assembler.add(new LlvmRet(v));
		assembler.add(new LlvmCloseDefinition());
		methodEnv = null;
		//System.out.println("Method 6;");
		return null;
	}

	public LlvmValue visit(Formal n) {
		return new LlvmNamedValue("%" + n.name.s, n.type.accept(this).type);
	}

	public LlvmValue visit(IntArrayType n){
	   
/*	   LlvmRegister reg = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I32));
	   	
	   assembler.add(new LlvmAlloca(reg, LlvmPrimitiveType.I32, new LinkedList<LlvmValue>()));
*/	   
		return new LlvmNamedValue("int[]", new LlvmPointer(LlvmPrimitiveType.I32));
	}
	
	public LlvmValue visit(BooleanType n){
		return new LlvmNamedValue("boolean", LlvmPrimitiveType.I1);
	}

	public LlvmValue visit(IntegerType n){
		return new LlvmNamedValue("int", LlvmPrimitiveType.I32);
	}
	
	public LlvmValue visit(IdentifierType n){
		return new LlvmNamedValue(n.name, new LlvmClassType(n.name));
	}	
	
	public LlvmValue visit(IdentifierExp n){
		LlvmValue reg = null;
		// TODO : super classes
		LlvmValue result = null;
		if (methodEnv != null) {
			String localName = "%" + n.name.s;
			if (methodEnv.locals.containsKey(localName) || methodEnv.locals.containsKey(n.name.s)) {
				LlvmValue local = methodEnv.locals.get(n.name.s);
				if (local == null) {
					local = methodEnv.locals.get(localName);
				}
				if (local == null) {
					local = methodEnv.locals.get("%" + localName);
				}
				//c.head.accept(this);
				//if (local.toString().equals(localName)) 
				LlvmType type = local.type;
				if (type instanceof LlvmClassType) {
					type = new LlvmPointer(type);
					//reg = local;
				} //else {
				reg = new LlvmRegister(type);
				assembler.add(new LlvmLoad(reg, new LlvmNamedValue(local.toString(), type)));
				//}
					//return new LlvmNamedValue(n.name.s, type);
				//}
			}
		}
		//if (classEnv != null) {
		ClassNode sclass;
		LlvmValue thisVar = new LlvmNamedValue("%this", new LlvmPointer(classEnv));
		for (ClassNode c = classEnv; c != null; c = sclass) {
			int i = 0;
			for (LlvmValue var : c.vars) {
				//var.head.accept(this);
				String vname = "%" + n.name.s;
				if (var.toString().equals(vname) || vname.equals("%" + var.toString())) {
					List<LlvmValue> offsets = new LinkedList<LlvmValue>();
					offsets.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
					offsets.add(new LlvmNamedValue("" + i, LlvmPrimitiveType.I32));
					LlvmType type = var.type;
					if (type instanceof LlvmClassType) {
						//type = new LlvmPointer(type);
					}
					
					LlvmRegister ptr = new LlvmRegister(new LlvmPointer(type));
					assembler.add(new LlvmGetElementPointer(ptr, thisVar, offsets));
					reg = new LlvmRegister(type);
					assembler.add(new LlvmLoad(reg, ptr));
					break;
					//return reg;
					//new LlvmNamedValue(n.name.s, var.head.type);
				}
				if (i++ >= 100) {
					System.out.println("FAILED!!!! to run expression");
					break;
				}
			}
			sclass = (c.superClass == null ? null : symTab.classes.get(c.superClass));
			if (sclass != null) {
				List<LlvmValue> offsets = new LinkedList<LlvmValue>();
				offsets.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
				offsets.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
				LlvmRegister spr = new LlvmRegister(new LlvmPointer(sclass));
				assembler.add(new LlvmGetElementPointer(spr, thisVar, offsets));
				thisVar = spr;
			}
		}
		return reg;
	}
	
	public LlvmValue visit(Call n){
		List<LlvmValue> actuals = new LinkedList<LlvmValue>();
		LlvmValue obj = n.object.accept(this);
		
		//System.out.println("Call 1;");

		if (!(obj.type instanceof LlvmPointer)) {
			System.out.println("OBJECT " + obj.toString() + " IS OF TYPE " + obj.type.toString());
		}
		LlvmClassType type = (LlvmClassType) ((LlvmPointer) obj.type).content;
		
		
		//System.out.println("Call 2;");
		//System.out.println("--- Method " + n.method.s + " returns " + symTab.classes.get(type.name).methods.get(n.method.s).type.toString());
		//Map<String, ClassNode> classes = symTab.classes;
		//ClassNode c = classes.get(type.name);
		//MethodNode m = c.methods.get(n.method.s);
		MethodNode method = symTab.getClassMethod(type.name, n.method.s);
		LlvmType returnType = method.type; //symTab.classes.get(type.name).methods.get(n.method.s).type;
		ClassNode varC = symTab.classes.get(type.name);
		ClassNode sClass = varC.superClass == null ? null : symTab.classes.get(varC.superClass);
		ClassNode c = method.declaringClass;
		LlvmValue newObj;
		int i = 0;
		//System.out.println("Call 3;");
		while (!obj.type.toString().equals(new LlvmPointer(c).toString()) && obj != null) {
			List<LlvmValue> offsets = new LinkedList<LlvmValue>();
			offsets.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
			offsets.add(new LlvmNamedValue("0", LlvmPrimitiveType.I32));
			newObj = new LlvmRegister(new LlvmPointer(sClass));
			assembler.add(new LlvmGetElementPointer(newObj, obj, offsets));
			obj = newObj;
			varC = sClass;
			sClass = varC.superClass == null ? null : symTab.classes.get(varC.superClass);
			if (i++ >= 100) {
				System.out.println("FAILED!!!! to find super class");
				break;
			}
		}
		//System.out.println("Call 4;");
		
		actuals.add(obj);
		for (; n.actuals != null; n.actuals = n.actuals.tail) {
			actuals.add(n.actuals.head.accept(this));
		}
		
		//System.out.println("Call 5;");
		
		LlvmRegister reg = new LlvmRegister(returnType);
		assembler.add(new LlvmCall(reg, returnType, "@__" + n.method.s + "_" + c.name, actuals));

		//System.out.println("Call 6;");
	   return reg;
	}

	public LlvmValue visit(This n){
	   return new LlvmNamedValue("%this", new LlvmPointer(classEnv));
	}

	public LlvmValue visit(NewArray n){
	
		LlvmValue size = n.size.accept(this);
		
		int s = Integer.parseInt(size.toString());
		LlvmPointer type = new LlvmPointer(new LlvmArray(s, size.type));
		LlvmRegister reg = new LlvmRegister(type);
		
		assembler.add(new LlvmAlloca(reg, new LlvmArray(s, size.type), new LinkedList<LlvmValue>()));
      
		return reg;
	}

	public LlvmValue visit(Identifier n){
		return new LlvmNamedValue(n.s, LlvmPrimitiveType.I32);
	}


	public LlvmValue visit(NewObject n){
		//n.className
		
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		
		LlvmRegister reg = new LlvmRegister(new LlvmPointer(new LlvmClassType(n.className.s)));
		args.add(reg);
		LlvmRegister reg2 = new LlvmRegister(new LlvmPointer(new LlvmClassType(n.className.s)));
		assembler.add(new LlvmAlloca(reg, n.type.accept(this).type, new LinkedList<LlvmValue>()));
		assembler.add(new LlvmCall(
				reg2,
				LlvmPrimitiveType.VOID,
				"@__" + n.className,
				args));
		return reg;
	}
	
	public LlvmValue visit(ArrayLength n){
	   LlvmValue array = n.array.accept(this);
	   
		String tipo = array.type.toString();
		String init = "[\\[]+";
   	String end = "[ ]+";
      String[] tokens1 = tipo.split(init);
      String[] tokens2 = tokens1[1].split(end);

      LlvmValue size = new LlvmNamedValue(tokens2[0], LlvmPrimitiveType.I32);
				
	   return size;
	}

	// Todos os visit's que devem ser implementados	
	//public LlvmValue visit(ClassDeclSimple n){return null;} // OK
	//public LlvmValue visit(ClassDeclExtends n){return null;} // OK
	//public LlvmValue visit(VarDecl n){return null;} // OK
	//public LlvmValue visit(MethodDecl n){return null;} // WIP
	//public LlvmValue visit(Formal n){return null;} // OK
	//public LlvmValue visit(IntArrayType n){return null;} // Falta testar
//	public LlvmValue visit(BooleanType n){return null;} // OK
//	public LlvmValue visit(IntegerType n){return null;} // OK
//	public LlvmValue visit(IdentifierType n){return null;} // Falta testar
//	public LlvmValue visit(Block n){return null;} // Falta testar
//	public LlvmValue visit(If n){return null;} // OK
//	public LlvmValue visit(While n){return null;} // OK
//	public LlvmValue visit(Assign n){return null;} // Falta testar
//	public LlvmValue visit(ArrayAssign n){return null;} // Falta testar
//	public LlvmValue visit(And n){return null;} // OK
//	public LlvmValue visit(LessThan n){return null;} // OK
//	public LlvmValue visit(Equal n){return null;} // OK
//	public LlvmValue visit(Minus n){return null;} // OK
//	public LlvmValue visit(Times n){return null;} // OK
//	public LlvmValue visit(ArrayLookup n){return null;} // Falta testar
//	public LlvmValue visit(ArrayLength n){return null;}
//	public LlvmValue visit(Call n){return null;} // Falta testar
//	public LlvmValue visit(True n){return null;} // OK
//	public LlvmValue visit(False n){return null;} // OK
//	public LlvmValue visit(IdentifierExp n){return null;} // WIP
//	public LlvmValue visit(This n){return null;} // Falta testar
//	public LlvmValue visit(NewArray n){return null;} // Verificar retorno
//	public LlvmValue visit(NewObject n){return null;} // Falta testar mais
//	public LlvmValue visit(Not n){return null;} // OK
//	public LlvmValue visit(Identifier n){return null;} // WIP
}


/**********************************************************************************/
/* === Tabela de Símbolos ==== 
 * 
 * 
 */
/**********************************************************************************/

class SymTab extends VisitorAdapter{
	public Map<String, ClassNode> classes = new HashMap<String, ClassNode>();
	private ClassNode classEnv;    //aponta para a classe em uso

	
	public MethodNode getClassMethod(String cName, String name) {
		MethodNode method;
		for (ClassNode c = classes.get(cName);
					c != null;
					c = (c.superClass == null ? null : classes.get(c.superClass))) {
			method = c.methods.get(name);
			if (method != null) {
				return method;
			}
		}
		return null;
	}
	
	
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
		classes.put(n.className.s, new ClassNode(n.className.s, new LlvmStructure(new LinkedList<LlvmType>()), null));
		return null;
	}

	public LlvmValue visit(ClassDeclSimple n){
		List<LlvmType> typeList = new LinkedList<LlvmType>();
		
		// Constroi TypeList com os tipos das variáveis da Classe (vai formar a Struct da classe)
		
		List<LlvmValue> varList = new LinkedList<LlvmValue>();
		// Constroi VarList com as Variáveis da Classe
		
		for (util.List<VarDecl> vars = n.varList; vars != null; vars = vars.tail) {
			LlvmValue var = vars.head.accept(this);
			varList.add(var);
			typeList.add(var.type);
		}

		classEnv = new ClassNode(n.name.s, 
								new LlvmStructure(typeList), 
								varList);
		classes.put(n.name.s, classEnv);
					
		
		
		for (util.List<MethodDecl> methods = n.methodList; methods != null; methods = methods.tail) {
			LlvmValue method = methods.head.accept(this);
			// TODO : add method to class
		}
			// Percorre n.methodList visitando cada método
		return null;
	}

	public LlvmValue visit(ClassDeclExtends n){
		List<LlvmType> typeList = new LinkedList<LlvmType>();
		
		// Constroi TypeList com os tipos das variáveis da Classe (vai formar a Struct da classe)
		
		List<LlvmValue> varList = new LinkedList<LlvmValue>();
		// Constroi VarList com as Variáveis da Classe
		
		typeList.add(new LlvmClassType(n.superClass.s));
		for (util.List<VarDecl> vars = n.varList; vars != null; vars = vars.tail) {
			LlvmValue var = vars.head.accept(this);
			varList.add(var);
			typeList.add(var.type);
		}

		classEnv = new ClassNode(n.name.s, 
								new LlvmStructure(typeList), 
								varList,
								n.superClass.s);
		classes.put(n.name.s, classEnv);
					
		
		
		for (util.List<MethodDecl> methods = n.methodList; methods != null; methods = methods.tail) {
			LlvmValue method = methods.head.accept(this);
			// TODO : add method to class
		}
			// Percorre n.methodList visitando cada método
		return null;
	}
	
	
	public LlvmValue visit(VarDecl n) {
		LlvmType typeVal = n.type.accept(this).type;
		LlvmType type = typeVal;
		if (typeVal instanceof LlvmClassType) {
			type = new LlvmPointer(type);
		}
		LlvmNamedValue val = new LlvmNamedValue(n.name.s, type);
		
		return val;
	}
	
	public LlvmValue visit(Formal n) {
		return new LlvmNamedValue(n.name.s, n.type.accept(this).type);
	}
	
	public LlvmValue visit(MethodDecl n){
		//String name = n.name.s;
		// Map<String, LlvmValue> locals
		 
		// List<LlvmValue> formals
		//MethodNode node = new MethodNode(name, formals);
		//classEnv.methods.put(name, node);
		
		
		
		// TODO : alloc memory to method parameters (formals)
		LlvmType resultType = n.returnType.accept(this).type;
		Map<String, LlvmValue> locals = new HashMap();
		List<LlvmValue> formals = new LinkedList<LlvmValue>();
		formals.add(new LlvmNamedValue("%this", new LlvmPointer(new LlvmClassType(classEnv.name))));
		for (util.List<Formal> c = n.formals; c != null; c = c.tail) {
			LlvmValue var = c.head.accept(this);
			formals.add(var); //new LlvmNamedValue("%" + c.head.name.s, c.head.type.accept(this).type));
			locals.put(var.toString(), var);
		}
		if (resultType instanceof LlvmClassType) {
			resultType = new LlvmPointer(resultType);
		}
		
		
		MethodNode method = new MethodNode(n.name.s, resultType, locals, formals, classEnv);
		for (util.List<VarDecl> c = n.locals; c != null; c = c.tail) {
			locals.put(c.head.accept(this).toString(), null);
		}
		
		
		classEnv.methods.put(n.name.s, method);
		
		return null;
	}
	
	public LlvmValue visit(IntArrayType n){
		return new LlvmNamedValue("int[]", new LlvmPointer(LlvmPrimitiveType.I32));
	}
	
	public LlvmValue visit(BooleanType n){
		return new LlvmNamedValue("boolean", LlvmPrimitiveType.I1);
	}

	public LlvmValue visit(IntegerType n){
		return new LlvmNamedValue("int", LlvmPrimitiveType.I32);
	}
	
	public LlvmValue visit(IdentifierType n){
		return new LlvmNamedValue(n.name, new LlvmClassType(n.name));
	}
}

class ClassNode extends LlvmClassType {
	public LlvmStructure structure;
	public List<LlvmValue> vars;
	public LlvmNamedValue value;
	public Map<String, MethodNode> methods;
	public String superClass = null;
	ClassNode (String nameClass, LlvmStructure classType, List<LlvmValue> varList){
		super(nameClass);
		//name = nameClass;
		structure = classType;
		vars = varList;
		methods = new HashMap<String, MethodNode>();
		value = new LlvmNamedValue(nameClass, new LlvmClassType(nameClass));
	}
	ClassNode (String nameClass, LlvmStructure classType, List<LlvmValue> varList, String sclass){
		super(nameClass);
		//name = nameClass;
		structure = classType;
		vars = varList;
		methods = new HashMap<String, MethodNode>();
		value = new LlvmNamedValue(nameClass, new LlvmClassType(nameClass));
		superClass = sclass;
	}
}

class MethodNode {

	public String name;
	public Map<String, LlvmValue> locals;
	public List<LlvmValue> formals;
	public LlvmType type;
	public ClassNode declaringClass;
	
	MethodNode(String nameMethod, Map<String, LlvmValue> localList) {
		name = nameMethod;
		locals = localList;
	}
	
	
	MethodNode(String nameMethod, LlvmType retType, Map<String, LlvmValue> localList, List<LlvmValue> formalList, ClassNode decClass) {
		name = nameMethod;
		locals = localList;
		formals = formalList;
		type = retType;
		declaringClass = decClass;
	}

}

class LlvmInt extends LlvmType {

	public String name;
	
	public LlvmInt(String n) {
		name = n;
	}

	public String toString(){
		return name;
	}
}

class LlvmArrayValue extends LlvmValue{
    public LlvmValue length;
    public LlvmType content;
    
    public LlvmArrayValue(LlvmValue length, LlvmType content){
	this.length = length;
	this.content = content;
    }

    public String toString(){
	return "[" + length + " x " + content + "]";
    }
}

