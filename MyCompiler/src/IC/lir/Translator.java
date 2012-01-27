package IC.lir;

import IC.ICVoid;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.ExpressionBlock;
import IC.AST.Field;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Length;
import IC.AST.Library;
import IC.AST.LibraryMethod;
import IC.AST.Literal;
import IC.AST.LocalVariable;
import IC.AST.LogicalBinaryOp;
import IC.AST.LogicalUnaryOp;
import IC.AST.MathBinaryOp;
import IC.AST.MathUnaryOp;
import IC.AST.Method;
import IC.AST.NewArray;
import IC.AST.NewClass;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.Visitor;
import IC.AST.While;

public class Translator implements Visitor {

	private static int maxLabel = 0;
	private int maxReg = 1;
	
	private String currentClass;

	private static String generateLabel(String name) {
		return "_" + name + "_" +(++maxLabel) + ":\n";
	}

	private String methodLabel(Method method) {
		return "_" + currentClass + "_" + method.getName() + ":\n";
	}

	@Override
	public Object visit(Program program) {
		
		String lirProgram = "";
		for(ICClass icc : program.getClasses()){
			lirProgram += icc.accept(this);
		}
		return lirProgram;
	}

	@Override
	public Object visit(ICClass icClass) {
		String classLir = "";
		
		currentClass = icClass.getName();
		
		for(Method m : icClass.getMethods()){
			classLir += m.accept(this);
		}
		return classLir;
	}

	@Override
	public Object visit(Field field) {
		// Doesn't require visit
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		// Method Label
		String methodName = methodLabel(method);
		
		// Method LIR code
		String methodLir = "";
		for ( Statement st : method.getStatements() ) { 
			methodLir += st.accept(this);
		}
		
		// FailSafe for methods without return
		if ( method.getType() instanceof ICVoid )
			methodLir += "Return Rdummy\n";
		
		return methodName + methodLir;
	}



	@Override
	public Object visit(StaticMethod method) {
		// Method Label
		String methodName = methodLabel(method);
		
		// Method LIR code
		String methodLir = "";
		for ( Statement st : method.getStatements() ) { 
			methodLir += st.accept(this);
		}
		
		// FailSafe for methods without return
		if ( method.getType() instanceof ICVoid )
			methodLir += "Return Rdummy\n";
		
		return methodName + methodLir;
	}

	@Override
	public Object visit(LibraryMethod method) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(UserType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		String resReg = "R" + maxReg++;
		String assignemntLir = "" + assignment.getExp().accept(this);
		String locTR = "" + assignment.getVariable().accept(this);
		assignemntLir += "Move " + resReg + ", " + locTR + "\n";
		
		return assignemntLir;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		if ( returnStatement.hasValue() ) {
			String resReg = "R" + maxReg ;
			String result =""+ returnStatement.getValue().accept(this);
			result += "return " + resReg + "\n";
			return (result);
		}
		else { 
			return "Return Rdummy\n";
		}
	}

	@Override
	public Object visit(If ifStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Length length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		switch ( literal.getType() ) {
		case INTEGER:
			return "Move " + literal.getValue() + ", R" + maxReg + "\n";
			// TODO Other literal types
		}
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Library library) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ICVoid icVoid) {
		// TODO Auto-generated method stub
		return null;
	}

}
