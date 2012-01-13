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
import IC.Types.ClassType;
import IC.Types.MethodType;
import IC.Types.Type;
import IC.Types.TypeTable;

public class SymbolTableConstructor implements IC.AST.Visitor{

	private SymbolTable currentTable;
	private SymbolTable global;
	private Map<Call, SymbolTable> unresolved = new HashMap<>();
	//private boolean unresCheck = false;
	private boolean isFieldMethodInserted = false;
	private static int BlockNumber = 0;
	
	private void errorHandler(SemanticError e){
		System.out.println(e.getMessage());
		System.exit(1);
	}
	
	@Override
	public Object visit(Program program) {
		SymbolTable globalTable = new SymbolTable("Global", null);
		setGlobal(globalTable);
		setCurrentTable(globalTable);
		program.setEnclosingScope(globalTable);
		
		// Build Type-Table and global Symbol table entries
		for ( ICClass c : program.getClasses() ){
			try {
				TypeTable.addUserType(c);
				ClassType ct = TypeTable.getUserType(c.getName());
				if ( ct == null )
					errorHandler(new SemanticError(c.getLine(), "Class not found: " + c.getName()));
				Symbol classSymbol = null;
				try {
					classSymbol = new Symbol(c.getName(), ct, Kind.CLASS, c.getLine());
					globalTable.insertSymbol(classSymbol);
				} catch (SemanticError e) {
					errorHandler(e);
				}
				
			} catch (SemanticError e) {
				errorHandler(e);
			}
		}
		
		// Build class symbol tables
		for ( ICClass c : program.getClasses() ){
			c.accept(this);
		}
		
		// Add the library symbol table
		program.getLibrary().accept(this);
		
		// Build symbol tables for methods and code-blocks + scope checking
		isFieldMethodInserted = true;
		for ( ICClass c : program.getClasses() ){
			c.accept(this);
		}

		// Make sure no unresolved calls were made
		try {
			checkUnresolved();
		} catch (SemanticError e) {
			errorHandler(e);
		}

		return globalTable;
	}

	/**
	 * @throws SemanticError 
	 * 
	 */
	private void checkUnresolved() throws SemanticError {
//		setUnresolvedChecking();
//		Map<Call, SymbolTable> oldUnRes = getUnresolved();
//		setUnresolved(new HashMap<Call, SymbolTable>() );
//		for ( Call call : oldUnRes.keySet() ){
//			SymbolTable env = oldUnRes.get(call);
//			setCurrentTable(env);
//			call.accept(this);
//		}
		if ( getUnresolved().size() > 0 ){
			Call unresCall = getUnresolved().keySet().iterator().next();
			throw new SemanticError( unresCall.getLine(), 
					"Unresolved call to: " + unresCall);
		}
	}

//	private void setUnresolvedChecking() {
//		unresCheck  = true;
//	}

	@Override
	public Object visit(ICClass icClass) {
		if ( ! isFieldMethodInserted ) {
			SymbolTable table = new SymbolTable(icClass.getName(), Kind.CLASS);
			TypeTable.getUserType(icClass.getName()).setSymbolTable(table);
			if ( icClass.hasSuperClass() ) {
//				Symbol parentSym = getGlobal().lookup(icClass.getSuperClassName() );
//				if ( parentSym == null )
//					errorHandler(new SemanticError(icClass.getLine(), "Class not defined: " + icClass.getSuperClassName()));
//				SymbolTable pst = parentSym.getRelatedSymTab();
				SymbolTable pst = TypeTable.getClassSymTab(icClass.getSuperClassName());
				if ( pst == null )
					errorHandler(new SemanticError(icClass.getLine(), "Unknown super class: " + icClass.getSuperClassName()));
				pst.getChilds().add(table);
				table.setParentSymbolTable(pst);
			} else {
				getGlobal().getChilds().add(table);
				table.setParentSymbolTable(getGlobal());
			}
			icClass.setEnclosingScope(table);
			setCurrentTable(table);
			for ( Field f : icClass.getFields() ){
				f.accept(this);
			}
			for ( Method m : icClass.getMethods() ){
				m.accept(this);
			}
			setCurrentTable(table.getParentSymbolTable());
			return table;
		} else {
			SymbolTable table = TypeTable.getClassSymTab(icClass.getName());
			setCurrentTable(table);
//			for ( Field f : icClass.getFields() ){
//				f.accept(this);
//			}
			for ( Method m : icClass.getMethods() ){
				m.accept(this);
			}
			setCurrentTable(table.getParentSymbolTable());
			return table;
		}
	}

	@Override
	public Object visit(Library library) {
		SymbolTable table = new SymbolTable("Library", Kind.CLASS);
		if ( getGlobal().lookup("Library") != null )
			errorHandler(new SemanticError(library.getLine(),"Can't override Library class"));
		getGlobal().getChilds().add(table);
		table.setParentSymbolTable(getGlobal());
		setCurrentTable(table);
		library.setEnclosingScope(table);
		for ( LibraryMethod m : library.getMethodList() ){
			m.accept(this);
		}
		setCurrentTable(table.getParentSymbolTable());
		return table;
	}


	@Override
	public Object visit(Field field) {
//		if ( ! isFieldMethodInserted ) {
			Symbol fSymbol = null;
			try {
				fSymbol = new Symbol(field.getName(), TypeTable.astType(field.getType()), Kind.FIELD, field.getLine());
				getCurrentTable().insertSymbol(fSymbol);		
			} catch (SemanticError e) {
				errorHandler(e);
			}
//		}
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		if ( ! isFieldMethodInserted ) {
			Symbol fSymbol = null;
			try {
				MethodType mt = TypeTable.methodType(method);
				fSymbol = new Symbol(method.getName(), mt, Kind.METHOD, method.getLine());
				getCurrentTable().insertSymbol(fSymbol);		
			} catch (SemanticError e) {
				errorHandler(e);
			}
		} else {
			SymbolTable table = new SymbolTable(method.getName(), Kind.METHOD);
			//fSymbol.setRelatedSymTab(table);
			method.setEnclosingScope(table);
			getCurrentTable().getChilds().add(table);
			table.setParentSymbolTable(currentTable);
			String thisTypeName = getCurrentTable().getId();
			setCurrentTable(table);
			Type thisType;
			try {
				thisType = TypeTable.getUserType(thisTypeName);
				table.insertSymbol(new Symbol("this", thisType, Kind.AUTOMATIC, method.getLine()));
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
		}
		return null;
	}

	@Override
	public Object visit(StaticMethod method) {
		if ( ! isFieldMethodInserted ) {
			Symbol fSymbol = null;
			try {
				MethodType mt = TypeTable.methodType(method);
				fSymbol = new Symbol(method.getName(), mt, Kind.METHOD, method.getLine());
				fSymbol.setStatic(true);
				getCurrentTable().insertSymbol(fSymbol);		
			} catch (SemanticError e) {
				errorHandler(e);
			}
		} else {
			SymbolTable table = new SymbolTable(method.getName(), Kind.METHOD);
			//fSymbol.setRelatedSymTab(table);
			method.setEnclosingScope(table);
			getCurrentTable().getChilds().add(table);
			table.setParentSymbolTable(getGlobal());
			SymbolTable oldCurrentTable = getCurrentTable();
			setCurrentTable(table);
			for ( Formal f : method.getFormals() ){
				f.accept(this);
			}
			
			for(Statement s : method.getStatements()){
				s.accept(this);
			}
			setCurrentTable(oldCurrentTable);
		}
		return null;
	}

	@Override
	public Object visit(LibraryMethod method) {
		if ( ! isFieldMethodInserted ) {
			Symbol fSymbol = null;
			try {
				MethodType mt = TypeTable.methodType(method);
				fSymbol = new Symbol(method.getName(), mt, Kind.METHOD, method.getLine());
				fSymbol.setStatic(true);
				getCurrentTable().insertSymbol(fSymbol);		
			} catch (SemanticError e) {
				errorHandler(e);
			}
		} else {
			SymbolTable table = new SymbolTable(method.getName(), Kind.METHOD);
			//fSymbol.setRelatedSymTab(table);
			method.setEnclosingScope(table);
			getCurrentTable().getChilds().add(table);
			table.setParentSymbolTable(getGlobal());
			SymbolTable oldCurrentTable = getCurrentTable();
			setCurrentTable(table);
			for ( Formal f : method.getFormals() ){
				f.accept(this);
			}
			setCurrentTable(oldCurrentTable);
		}
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		formal.setEnclosingScope(getCurrentTable());
		Symbol sym = null;
		try {
			sym = new Symbol(formal.getName(), TypeTable.astType(formal.getType()), 
					Kind.PARAMETER, formal.getLine());
			getCurrentTable().insertSymbol(sym);
		} catch (SemanticError e) {
			errorHandler(e);
		}
		return sym;
	}

	@Override
	public Object visit(PrimitiveType type) {
//		if ( type.getDimension() > 0 ){
//			try {
//				TypeTable.arrayType(TypeTable.astType(type), type.getDimension());
//			} catch (SemanticError e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		return null;
	}

	@Override
	public Object visit(UserType type) {
//		if ( type.getDimension() > 0 ){
//			try {
//				TypeTable.arrayType(TypeTable.astType(type), type.getDimension());
//			} catch (SemanticError e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
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
		SymbolTable table = new SymbolTable("statement block in " + currentTable.getId(), Kind.BLOCK);
		statementsBlock.setEnclosingScope(table);
		getCurrentTable().getChilds().add(table);
		table.setParentSymbolTable(getCurrentTable());
		try {
			Symbol sym = new Symbol("Block_"+(++BlockNumber), null, Kind.BLOCK, statementsBlock.getLine());
			getCurrentTable().insertSymbol(sym);
		} catch (SemanticError e) {
			errorHandler(e);
		}
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
			sym = new Symbol(localVariable.getName(), TypeTable.astType(localVariable.getType()), 
					Kind.VARIABLE, localVariable.getLine());
			getCurrentTable().insertSymbol(sym);
		} catch (SemanticError e) {
			errorHandler(e);
		}
		return sym;
	}

	@Override
	public Object visit(VariableLocation location) {
		//location.setEnclosingScope(getCurrentTable());
		Symbol locSym = null;
		if ( location.isExternal() ) {
			location.getLocation().accept(this);
			SymbolTable leftSymTab = location.getLocation().getEnclosingScope();
			if ( leftSymTab == null ) {
				return null;
			}
			locSym = location.getLocation().getEnclosingScope().lookup(location.getName());
		} else {
			locSym = getCurrentTable().lookup(location.getName());
		}
		if ( locSym == null )
			errorHandler(new SemanticError(location.getLine(), "location not found: " + location.getName()));
		SymbolTable locationTypeSymTab = TypeTable.getClassSymTab(locSym.getType().getName());
		location.setEnclosingScope(locationTypeSymTab);
		return locationTypeSymTab;
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
//		if ( ! isInCheckUnRes() ) { // in the second round of resolving forward ref. don't check arguments
			for ( Expression exp : call.getArguments() )
				exp.accept(this);
//		}
		
		if ( getGlobal().lookup(call.getClassName()) == null ) {
			//errorHandler(new SemanticError(call.getLine(), "Class not found: " + call.getClassName()));
			getUnresolved().put(call, currentTable);
			return null;
		}
		
		SymbolTable classST = TypeTable.getClassSymTab(call.getClassName());
		
		if ( (classST == null ) || (classST.lookup(call.getName()) == null) ) {
			getUnresolved().put(call, currentTable);
		} else 
			if( (classST.lookup(call.getName()) != null) 
					&& (!classST.lookup(call.getName()).isStatic())){
			errorHandler(new SemanticError(call.getLine(), "Could find static method: " + call.getClassName() + "." +call.getName()));
		}
		
		return null;
	}

//	private boolean isInCheckUnRes() {
//		return unresCheck;
//	}

	@Override
	public Object visit(VirtualCall call) {
		call.setEnclosingScope(getCurrentTable());
//		if ( ! isInCheckUnRes() ) { // in the second round of resolving forward ref. don't check arguments
			if ( call.isExternal() )
				call.getLocation().accept(this);
			for ( Expression exp : call.getArguments() )
				exp.accept(this);
//		}
		if ( call.isExternal() ) {
//			SymbolTable classTable = call.getLocation().getEnclosingScope();
			String callLocationName = ((VariableLocation)call.getLocation()).getName();
			SymbolTable classTable = TypeTable.getClassSymTab(getCurrentTable().lookup(callLocationName).getType().getName());
			if ( classTable.lookup(call.getName()) == null ){
				getUnresolved().put(call, currentTable);
				return null;
			}
			if( classTable.lookup(call.getName()).isStatic())
				errorHandler(new SemanticError(call.getLine(), "calling a static function from an external call"));
		}
		else{ 
			if ( currentTable.lookup(call.getName()) == null )
				getUnresolved().put(call, currentTable);
			if( currentTable.lookup(call.getName()).isStatic()){
				errorHandler(new SemanticError(call.getLine(), "calling a static function within a virtual call"));
			}
		}
		
		
		
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		thisExpression.setEnclosingScope(getCurrentTable());
		if ( getCurrentTable().lookup("this") == null )
			errorHandler(new SemanticError(thisExpression.getLine(), "'this' object is undefined here"));
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		newClass.setEnclosingScope(getCurrentTable());
		if ( TypeTable.getUserType(newClass.getName()) == null )
			errorHandler(new SemanticError(newClass.getLine(), "No such type: " + newClass.getName()));
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		newArray.setEnclosingScope(getCurrentTable());
		try {
			TypeTable.arrayType(TypeTable.astType(newArray.getType()), newArray.getType().getDimension());
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
