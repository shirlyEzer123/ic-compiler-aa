package IC.SymbolTable;

import java.util.HashMap;
import java.util.Map;

import IC.ICVoid;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.Call;
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
import IC.AST.While;
import IC.SemanticChecks.SemanticError;
import IC.Types.MethodType;
import IC.Types.Type;
import IC.Types.TypeTable;

public class SymbolTableConstructor implements IC.AST.Visitor{

	private SymbolTable currentTable;
	private SymbolTable global;
	private int annonymousBlocks = 0;
	private Map<Call, SymbolTable> unresolved = new HashMap<>();
	
	private void errorHandler(SemanticError e){
		System.out.println(e.getMessage());
		System.exit(1);
	}
	
	@Override
	public Object visit(Program program) {
		SymbolTable globalTable = new SymbolTable("Global");
		setGlobal(globalTable);
		setCurrentTable(globalTable);
		program.setEnclosingScope(globalTable);
		for ( ICClass c : program.getClasses() ){
			try {
				TypeTable.addUserType(c);
			} catch (SemanticError e) {
				errorHandler(e);
			}
		}
		for ( ICClass c : program.getClasses() ){
			Symbol classSymbol = null;
			try {
				classSymbol = new Symbol(c.getName(), 
						TypeTable.getUserType(c.getName()), Kind.CLASS );
			} catch (SemanticError e1) {
				errorHandler(e1);
			}
			try {
				globalTable.insertSymbol(classSymbol);
			} catch (SemanticError e) {
				errorHandler(e);
			}
			/*SymbolTable classSymTab = (SymbolTable)*/c.accept(this);
//			classSymTab.setParentSymbolTable(globalTable);
		}
		return globalTable;
	}

	@Override
	public Object visit(ICClass icClass) {
		SymbolTable table = new SymbolTable(icClass.getName());
		getCurrentTable().getChilds().add(table);
		table.setParentSymbolTable(getCurrentTable());
		setCurrentTable(table);
		icClass.setEnclosingScope(table);
		for ( Field f : icClass.getFields() ){
			f.accept(this);
		}
		for ( Method m : icClass.getMethods() ){
			m.accept(this);
		}
		setCurrentTable(table.getParentSymbolTable());
		return table;
	}

	@Override
	public Object visit(Field field) {
		Symbol fSymbol = null;
		try {
			fSymbol = new Symbol(field.getName(), TypeTable.astType(field.getType()), Kind.FIELD);
			getCurrentTable().insertSymbol(fSymbol);		
		} catch (SemanticError e) {
			errorHandler(e);
		}
		return fSymbol;
	}

	@Override
	public Object visit(VirtualMethod method) {
		Symbol fSymbol = null;
		try {
			Type mt = TypeTable.astType(method.getType());
			//TODO: above line should get a MethodType and insert it into symbol
			fSymbol = new Symbol(method.getName(), mt, Kind.METHOD);
			getCurrentTable().insertSymbol(fSymbol);		
		} catch (SemanticError e) {
			errorHandler(e);
		}
		SymbolTable table = new SymbolTable(method.getName());
		method.setEnclosingScope(table);
		getCurrentTable().getChilds().add(table);
		table.setParentSymbolTable(currentTable);
		setCurrentTable(table);
		String thisTypeName = table.getParentSymbolTable().getId();/*.subSequence(0, 
				table.getParentSymbolTable().getId().length() - 4).toString();*/
		Type thisType;
		try {
			thisType = TypeTable.getUserType(thisTypeName);
			table.insertSymbol(new Symbol("this", thisType, Kind.VARIABLE));
		} catch (SemanticError e) {
			errorHandler(e);
		}
		for ( Formal f : method.getFormals() ){
			f.accept(this);
		}
		
		for(Statement s : method.getStatements()){
			s.accept(this);
		}
		setCurrentTable(table.getParentSymbolTable());
		return table;
	}

	@Override
	public Object visit(StaticMethod method) {
		Symbol fSymbol = null;
		try {
			fSymbol = new Symbol(method.getName(), TypeTable.astType(method.getType()), Kind.METHOD);
			fSymbol.setStatic(true);
			getCurrentTable().insertSymbol(fSymbol);		
		} catch (SemanticError e) {
			errorHandler(e);
		}
		SymbolTable table = new SymbolTable(method.getName());
		method.setEnclosingScope(table);
		getCurrentTable().getChilds().add(table);
		table.setParentSymbolTable(getGlobal());
		SymbolTable currentClassTable = getCurrentTable();
		setCurrentTable(table);
		for ( Formal f : method.getFormals() ){
			f.accept(this);
		}
		
		for(Statement s : method.getStatements()){
			s.accept(this);
		}
		setCurrentTable(currentClassTable);
		return table;
	}

	@Override
	public Object visit(LibraryMethod method) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		formal.setEnclosingScope(getCurrentTable());
		Symbol sym = null;
		try {
			sym = new Symbol(formal.getName(), TypeTable.astType(formal.getType()), Kind.VARIABLE);
			getCurrentTable().insertSymbol(sym);
		} catch (SemanticError e) {
			errorHandler(e);
		}
		return sym;
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
		assignment.setEnclosingScope(getCurrentTable());
		assignment.getExp().accept(this);
		assignment.getVariable().accept(this);
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		callStatement.setEnclosingScope(getCurrentTable());
		callStatement.getCall().accept(this);
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		returnStatement.setEnclosingScope(getCurrentTable());
		
		if ( returnStatement.hasValue() ) {
			Expression exp = returnStatement.getValue();
			exp.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		ifStatement.setEnclosingScope(getCurrentTable());
		ifStatement.getCondition().accept(this);
		ifStatement.getOperation().accept(this);
		if ( ifStatement.hasElse() )
			ifStatement.getElseOperation().accept(this);
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		whileStatement.setEnclosingScope(getCurrentTable());
		whileStatement.getCondition().accept(this);
		whileStatement.getOperation().accept(this);
		
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		breakStatement.setEnclosingScope(getCurrentTable());
		// We've been through this... 
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		continueStatement.setEnclosingScope(getCurrentTable());
		// We've been through this... 
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		SymbolTable table = new SymbolTable(currentTable.getId() +"sub_block_" + (++annonymousBlocks ));
		statementsBlock.setEnclosingScope(table);
		getCurrentTable().getChilds().add(table);
		table.setParentSymbolTable(currentTable);
		setCurrentTable(table);
		for(Statement s : statementsBlock.getStatements()){
			s.accept(this);
		}
		setCurrentTable(table.getParentSymbolTable());
		return table;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		localVariable.setEnclosingScope(getCurrentTable());
		Symbol sym = null;
		try {
			sym = new Symbol(localVariable.getName(), TypeTable.astType(localVariable.getType()), Kind.VARIABLE);
			getCurrentTable().insertSymbol(sym);
		} catch (SemanticError e) {
			errorHandler(e);
		}
		return sym;
	}

	@Override
	public Object visit(VariableLocation location) {
		location.setEnclosingScope(getCurrentTable());
		if ( location.isExternal() ) {
			location.getLocation().accept(this);
			//TODO
		} else {
			try {
				getCurrentTable().lookup(location.getName());
			} catch (SemanticError e) {
				errorHandler(e);
			}
		}
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		location.setEnclosingScope(getCurrentTable());
		location.getArray().accept(this);
		location.getIndex().accept(this);
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		call.setEnclosingScope(getCurrentTable());
		for ( Expression exp : call.getArguments() )
			exp.accept(this);
		try {
			currentTable.lookup(call.getClassName());
			currentTable.lookup(call.getName());
		} catch (SemanticError e) {
			getUnresolved().put(call, currentTable);
		}
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		call.setEnclosingScope(getCurrentTable());
		for ( Expression exp : call.getArguments() )
			exp.accept(this);
		try {
			currentTable.lookup(call.getName());
		} catch (SemanticError e) {
			getUnresolved().put(call, currentTable);
		}
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		thisExpression.setEnclosingScope(getCurrentTable());
		try {
			getCurrentTable().lookup("this");
		} catch (SemanticError e) {
			errorHandler(e);
		}
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		newClass.setEnclosingScope(getCurrentTable());
		try {
			TypeTable.getUserType(newClass.getName());
		} catch (SemanticError e) {
			errorHandler(e);
		}
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		newArray.setEnclosingScope(getCurrentTable());
		try {
			TypeTable.arrayType(TypeTable.astType(newArray.getType()));
		} catch (SemanticError e) {
			errorHandler(e);
		}
		return null;
	}

	@Override
	public Object visit(Length length) {
		length.setEnclosingScope(getCurrentTable());
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		binaryOp.setEnclosingScope(getCurrentTable());
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		binaryOp.setEnclosingScope(getCurrentTable());
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		unaryOp.setEnclosingScope(getCurrentTable());
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		unaryOp.setEnclosingScope(getCurrentTable());
		unaryOp.getOperand().accept(this);
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		literal.setEnclosingScope(getCurrentTable());
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		expressionBlock.setEnclosingScope(getCurrentTable());
		expressionBlock.getExpression().accept(this);
		return null;
	}

	@Override
	public Object visit(Library library) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ICVoid icVoid) {
		icVoid.setEnclosingScope(getCurrentTable());
		return null;
	}

	public SymbolTable getCurrentTable() {
		return currentTable;
	}

	public void setCurrentTable(SymbolTable currentTable) {
		this.currentTable = currentTable;
	}

	public SymbolTable getGlobal() {
		return global;
	}

	public void setGlobal(SymbolTable global) {
		this.global = global;
	}

	public Map<Call, SymbolTable> getUnresolved() {
		return unresolved;
	}

	public void setUnresolved(Map<Call, SymbolTable> unresolved) {
		this.unresolved = unresolved;
	}

}
