package IC.lir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import IC.BinaryOps;
import IC.ICVoid;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.Expression;
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
	private int maxReg = 0;
	
	private String currentClass;
	private Map<String, String> stringMap;
	private String globalTestLabel = null;
	private String globalEndLabel = null;
	
	public Translator(Map<String, String> stringMap) {
		this.stringMap = stringMap;
	}

	private static String generateLabel(String name) {
		return "_" + name + "_" +(++maxLabel);
	}

	private String methodLabel(Method method) {
		return "_" + currentClass + "_" + method.getName() + ":\n";
	}

	private String curMaxReg() {
		return "R" + maxReg;
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
		String methodName = "\n" + methodLabel(method);
		
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
		if ( method.isProgramMain() )
			methodName = "_ic_main:\n";
		
		// Method LIR code
		String methodLir = "";
		for ( Statement st : method.getStatements() ) { 
			methodLir += st.accept(this);
		}
		
		// FailSafe for methods without return
		if ( method.getType() instanceof ICVoid ) {
			if ( method.isProgramMain() )
				methodLir += "Library __exit(0),Rdummy\n";
			else
				methodLir += "Return Rdummy\n";
		}
		
		return methodName + methodLir;
	}

	@Override
	public Object visit(LibraryMethod method) {

		return null;
	}

	@Override
	public Object visit(Formal formal) {
		
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		
		return null;
	}

	@Override
	public Object visit(UserType type) {
		
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		String resReg = curMaxReg();
		String assignemntLir = "" + assignment.getExp().accept(this);
//		maxReg++;
//		assignemntLir += prepareLval(assignment.getVariable());
//		maxReg--; // TODO
		assignment.getVariable().setLvalue(true);
		String locTR = "" + assignment.getVariable().accept(this);
		assignemntLir += "Move " + resReg + ", " + locTR + "\n";
		
		return assignemntLir;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}

	@Override
	public Object visit(Return returnStatement) {
		if ( returnStatement.hasValue() ) {
			String resReg = curMaxReg() ;
			String result =""+ returnStatement.getValue().accept(this);
			result += "Return " + resReg + "\n";
			return (result);
		}
		else { 
			return "Return Rdummy\n";
		}
	}

	@Override
	public Object visit(If ifStatement) {
		String lir = "";
		String condReg = curMaxReg();
		String falseLabel = generateLabel("false_label");
		String endLabel = generateLabel("end_label");
		
		lir += ifStatement.getCondition().accept(this);
		lir += "Compare 0, " + condReg + "\n";
		lir += "JumpTrue " + falseLabel + "\n";
		lir += ifStatement.getOperation().accept(this);
		if ( ifStatement.hasElse() ) {
			lir += "Jump " + endLabel + "\n";
		}
		lir += falseLabel + ":\n";
		if ( ifStatement.hasElse() ) {
			lir += ifStatement.getElseOperation().accept(this);
			lir += endLabel + ":\n";
		}
		
		
		return lir;
	}

	@Override
	public Object visit(While whileStatement) {
		String lir = "";
		String condReg = curMaxReg();
		
		String testLabel = generateLabel("test_label");
		String endLabel = generateLabel("end_label");
		String oldTestLabel = globalTestLabel;
		String oldEndLabel = globalEndLabel;
		globalTestLabel = testLabel;
		globalEndLabel = endLabel;
		
		lir += testLabel + ":\n";
		lir += whileStatement.getCondition().accept(this);
		lir += "Compare 0, " + condReg + "\n";
		lir += "JumpTrue " + endLabel + "\n";
		lir += whileStatement.getOperation().accept(this);
		lir += "Jump " + testLabel + "\n";
		lir += endLabel + ":\n";
		
		globalTestLabel = oldTestLabel;
		globalEndLabel = oldEndLabel;
		
		return lir;
	}

	@Override
	public Object visit(Break breakStatement) {		
		return "Jump " + globalEndLabel + "\n";
	}

	@Override
	public Object visit(Continue continueStatement) {
		return "Jump " + globalTestLabel + "\n";
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		String lir = "";
		for ( Statement s : statementsBlock.getStatements() ) {
			lir += s.accept(this);
		}
		return lir;
	}

	@Override	
	public Object visit(LocalVariable localVariable) {
		String lir = "";
		if ( localVariable.hasInitValue() ) {
			// Evaluate the init value
			lir += localVariable.getInitValue().accept(this);
			lir += "Move " + curMaxReg() + ", " + localVariable.getName() + "\n";
		}
		return lir;
	}

	@Override
	public Object visit(VariableLocation location) {
		
		// We need to know if this is lvalue or rvalue
		// is it  "x = ..." (lvalue)
		// or     "... = x" (rvalue)
		// 
		// Maybe use 2 functions?
		
		if ( location.isExternal() ) {
			// TODO
			return null;
		} else {
			if ( location.isLvalue() )
				// variable is assignment target
				return location.getName();
			else {
				// variable is a part of an expression
				String lir = "Move " + location.getName() + ", " + curMaxReg() + "\n";
				return lir;
			}
		}
	}

	@Override
	public Object visit(ArrayLocation location) {
			// TODO RunTime checks:
			//		1. Null dereference
			//		2. index < array size
			
		if ( location.isLvalue() ) {
			// TODO
		} else {
			String lir = "";

			// Set R_T to contain the result
			String resReg = curMaxReg();
			
			// index to R_T+1
			maxReg++;
			String indexReg = curMaxReg();
			lir += location.getIndex().accept(this);
			
			// array to R_T+2
			maxReg++;
			String arrReg = curMaxReg();
			lir += location.getArray().accept(this);
			
			// Write result to target register
			lir += "MoveArray " + arrReg + "[" + indexReg + "], " + resReg + "\n"; 
			
			maxReg -= 2;
			
			return lir;
			
		}
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		String lir = "";
		int startMax = maxReg;

		String resReg = curMaxReg();
		
		// R_T+1, R_T+2, ...   <--   evaluate arguments
		List<String> paramRegs = new ArrayList<>(call.getArguments().size());
		for ( Expression exp : call.getArguments() ) {
			maxReg++;
			paramRegs.add(curMaxReg());
			lir += exp.accept(this);
		}
			
		// R_T <- call the method
		if ( call.getClassName().equals("Library") ) {
			lir += "Library __" + call.getName() + "(";
			for ( int i = 0; i < paramRegs.size()-1; i++ )
				lir += paramRegs.get(i) + ",";
			if ( paramRegs.size() > 0 )
				lir += paramRegs.get(paramRegs.size()-1);
		} else {
			lir += "StaticCall _" + call.getClassName() + "_" + call.getName() + "(";
//			for ( int i = 0; i < paramRegs.size()-1; i++ )
//				lir += call.getparamRegs.get(i) + ",";
//			if ( paramRegs.size() > 0 )
//				lir += paramRegs.get(paramRegs.size()-1);
			// TODO create code of the form param1=R12,param2=R3,...
		}

		lir += "), " + resReg + "\n";
			
		maxReg = startMax;
		return lir;
	}

	@Override
	public Object visit(VirtualCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		String resReg = curMaxReg();
		String lir = "Move this, " + resReg + "\n";
		return lir;
	}

	@Override
	public Object visit(NewClass newClass) {
		String lir = "";
		String resReg = curMaxReg();
		String DVName = DVCreator.getDVName(newClass.getName());
		int sizeOfObject = DVCreator.getNumFields(newClass.getName()) *4 + 4;
		lir += "Library __allocateObject(" + sizeOfObject + "), " + resReg + "\n";
		lir += "MoveField " + DVName + ", " + resReg + ".0\n";
		return lir;
	}

	@Override
	public Object visit(NewArray newArray) {
		// TODO Add runtime check for index > 0
		String lir = "";
		
		// R_T+1 <- array size
		maxReg++;
		String sizeReg = curMaxReg();
		lir += newArray.getSize().accept(this);
		maxReg--;
		
		// R_T <- new array of size (R_T+1)*4
		String resReg = curMaxReg();
		lir += "Mul 4, " + sizeReg + "\n";
		lir += "Library __allocateArray(" + sizeReg + "), " + resReg + "\n";		
		
		return lir;
	}

	@Override
	public Object visit(Length length) {
		
		// TODO add runtime checks for null dereference
		
		String lir = "";
		
		// R_T+1 <- the array
		maxReg++;
		String arrReg = curMaxReg();
		lir += length.getArray().accept(this);
		maxReg--;
		
		// R_T <- array length
		String resReg = curMaxReg();
		lir += "ArrayLength " + arrReg + ", " + resReg + "\n";
		
		return lir;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		String firstReg = curMaxReg();
		String binaryLir = "" + binaryOp.getFirstOperand().accept(this);
		
		maxReg++;
		String secReg = curMaxReg();
		binaryLir += binaryOp.getSecondOperand().accept(this);
		
		// add runtime divide by zero check
		if ( binaryOp.getOperator() == BinaryOps.DIVIDE ) {
			binaryLir += "Library __checkZero(" + secReg + ")\n";
		}
		
		if ( binaryOp.isStrCat() ) {
			binaryLir += "Library __stringCat(" + firstReg  + "," + secReg + "), " + firstReg + "\n";
		}
		else {
			binaryLir += binaryOp.getOperator().getLirOp()  + " " + secReg + ", " + firstReg  + "\n";
		}
		
		maxReg--;
		
		return binaryLir;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		
		int oldMaxReg = maxReg;
		
		String binaryLir = "";
		if ( ( binaryOp.getOperator() == BinaryOps.LAND ) ||
				( binaryOp.getOperator() == BinaryOps.LOR ) )
			binaryLir += andOrCode(binaryOp);
		else
			binaryLir += comparrisonCode(binaryOp);
		
		maxReg = oldMaxReg;
		return binaryLir;
	}
	
		
		
	private String comparrisonCode(LogicalBinaryOp binaryOp) {
		
		String testEnd = generateLabel("logical_op_end");
		String binaryLir = "";

		String resReg = curMaxReg();
		binaryLir += "Move 0, " + resReg + "\n"; // default is false
		maxReg++;
		
		
		// generate code to evaluate the 1st operand
		String firstReg = curMaxReg();
		binaryLir += binaryOp.getFirstOperand().accept(this);
		
		// generate code to evaluate the 2nd operand
		maxReg++;
		String secReg = curMaxReg();
		binaryLir += binaryOp.getSecondOperand().accept(this);
		
		binaryLir += "Compare " + firstReg + ", " + secReg + "\n";
		binaryLir += binaryOp.getOperator().getLirOp() + " " + testEnd + "\n";
		binaryLir += "Move 1, " + resReg + "\n";
		binaryLir += testEnd + ":\n";
		
		return binaryLir;
	}

	private String andOrCode(LogicalBinaryOp binaryOp) {

		String testEnd = generateLabel("logical_op_end");
		String binaryLir = "";

		String firstReg = curMaxReg();

		// generate code to evaluate the 1st operand
		binaryLir += binaryOp.getFirstOperand().accept(this);
		
		
		if ( binaryOp.getOperator() == BinaryOps.LAND ) { // lazy "&&" evaluation
			binaryLir += "Compare 0, " + firstReg + "\n" +
					"JumpTrue " + testEnd + "\n" ;
		} else if ( binaryOp.getOperator() == BinaryOps.LOR ) { // lazy "||" evaluation
			binaryLir += "Compare 0, " + firstReg + "\n" +
					"JumpFalse " + testEnd + "\n";
		}
		
		// generate code to evaluate the 2nd operand
		maxReg++;
		binaryLir += binaryOp.getSecondOperand().accept(this);
		
		String secReg = curMaxReg();
		
		// Do actual operation and save the result in the 1st register
		binaryLir += binaryOp.getOperator().getLirOp() + " " + secReg + "," + firstReg + "\n";
		
		binaryLir += testEnd + ":\n"; // add a point to jump to in case of lazy eval.

		return binaryLir;
	}




	@Override
	public Object visit(MathUnaryOp unaryOp) {
		// Only negation of numeric type expression
		
		String reg = curMaxReg();
		String lir = "" + unaryOp.getOperand().accept(this);
				
		lir += "Neg " + reg  + "\n";
		
		return lir;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		// Only negation of boolean type expression
		
		String reg = curMaxReg();
		String lir = "" + unaryOp.getOperand().accept(this);
				
		lir += "Xor 1," + reg  + "\n";
		
		return lir;
	}

	@Override
	public Object visit(Literal literal) {
		switch ( literal.getType() ) {
		case INTEGER:
			return "Move " + literal.getValue() + ", " + curMaxReg() + "\n";
		case NULL:
			return "Move 0, " + curMaxReg() + "\n";
		case FALSE:
			return "Move 0, " + curMaxReg() + "\n";
		case TRUE:
			return "Move 1, " + curMaxReg() + "\n";
		case STRING:
			return "Move " + stringMap.get(literal.getValue()) + ", " + curMaxReg() + "\n";
		}
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		
		return expressionBlock.getExpression().accept(this);
	}

	@Override
	public Object visit(Library library) {
		
		return null;
	}

	@Override
	public Object visit(ICVoid icVoid) {
		
		return null;
	}

}
