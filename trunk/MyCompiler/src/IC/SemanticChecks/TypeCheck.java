package IC.SemanticChecks;

import java.util.LinkedList;
import java.util.List;

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
import IC.SymbolTable.Symbol;
import IC.SymbolTable.SymbolTable;
import IC.Types.ArrayType;
import IC.Types.Type;
import IC.Types.TypeTable;

public class TypeCheck implements Visitor {

	private List<SymbolTable> scopeStack = new LinkedList<>();
	
	@Override
	public Object visit(Program program) {
		for (ICClass icClass : program.getClasses())
			icClass.accept(this);
		return null;
	}

	@Override
	public Object visit(ICClass icClass) {
		pushScope(icClass.getEnclosingScope());
		for (Method method : icClass.getMethods())
			method.accept(this);
		popScope();
		return null;
	}

	@Override
	public Object visit(Field field) {
		// We've been through this...
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		pushScope(method.getEnclosingScope());
		for (Statement statement : method.getStatements())
			statement.accept(this);
		popScope();
		return null;
	}

	@Override
	public Object visit(StaticMethod method) {
		pushScope(method.getEnclosingScope());
		for (Statement statement : method.getStatements())
			statement.accept(this);
		popScope();
		return null;
	}

	@Override
	public Object visit(LibraryMethod method) {
		// We've been through this...
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		// We've been through this...
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		// We've been through this...
		return null;
	}

	@Override
	public Object visit(UserType type) {
		// We've been through this...
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		Type lht = (Type) assignment.getVariable().accept(this);
		Type rht = (Type) assignment.getExp().accept(this);
		if ( ! rht.subtypeof(lht) )
			typeError(assignment.getLine(), "Can't assign " + rht.getName() + " to " + lht.getName());
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}

	@Override
	public Object visit(Return returnStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		if ( ifStatement.getCondition().accept(this) != TypeTable.boolType )
			typeError(ifStatement.getLine(), "if statment must have boolean type as a condition");
		ifStatement.getOperation().accept(this);
		if ( ifStatement.hasElse() )
			ifStatement.getElseOperation().accept(this);
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		if ( whileStatement.getCondition().accept(this) != TypeTable.boolType )
			typeError(whileStatement.getLine(), "while statment must have boolean type as a condition");
		whileStatement.getOperation().accept(this);
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		// been here...
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		// been here...
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		pushScope(statementsBlock.getEnclosingScope());
		for (Statement statement : statementsBlock.getStatements())
			statement.accept(this);
		popScope();
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		// been here...
		return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		Symbol locSym ;
		if ( location.isExternal() ){
			Type leftType = (Type) location.getLocation().accept(this);
			SymbolTable leftSymTab = TypeTable.getClassSymTab(leftType.getName());
			locSym = leftSymTab.lookup(location.getName()); // will not be null because of scope check
		} else {
			locSym = getCurrentScope().lookup(location.getName());// will not be null because of scope check
		}
		return locSym.getType();
	}

	@Override
	public Object visit(ArrayLocation location) {
		Object arrType = location.getArray().accept(this);
		if ( ! (arrType instanceof ArrayType ) )
			typeError(location.getLine(), "Not an array");
		if ( location.getIndex().accept(this) != TypeTable.intType ) {
			typeError(location.getLine(), "Array index must be an integer expression");
		}
		return ((ArrayType) arrType).getElemType();
	}

	@Override
	public Object visit(StaticCall call) {
		// TODO
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
		return TypeTable.literalType(literal.getType());
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
	
	private void typeError(int line, String msg) {
		System.err.println("Semantic error at line " + line + ": " + msg);
		System.exit(1);
	}

	private void popScope() {
		scopeStack.remove(0);
	}

	private void pushScope(SymbolTable scope) {
		scopeStack.add(0, scope);
	}

	public SymbolTable getCurrentScope() {
		return scopeStack.get(0);
	}

}
