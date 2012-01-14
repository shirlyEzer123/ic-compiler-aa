package IC.Types;

import IC.AST.ICClass;
import IC.SemanticChecks.SemanticError;
import IC.SymbolTable.SymbolTable;

public class ClassType extends Type {
	private ICClass classAST;
	private ClassType parent = null;
	private SymbolTable symbolTable = null;
	

	public ClassType(ICClass classObj) throws SemanticError {
		super(classObj.getName());
		this.classAST = classObj;
		if ( classObj.hasSuperClass() ){
			this.parent = TypeTable.getUserType(classObj.getSuperClassName());
			if ( this.parent == null )
				throw new SemanticError(classObj.getLine(), "Undefined: " + classObj.getSuperClassName() + " which stated as super class");
		}
	}

	public ClassType(String className) {
		super(className);
	}

	public ICClass getClassAST() {
		return classAST;
	}

	public void setClassAST(ICClass classAST) {
		this.classAST = classAST;
	}

	public ClassType getParent() {
		return parent;
	}

	public void setParent(ClassType parent) {
		this.parent = parent;
	}

	
	@Override
	public boolean subtypeof(Type t) {
		if (super.subtypeof(t))
			return true;
		else if ( getParent() != null )
			return getParent().subtypeof(t);
		else
			return false;
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

}
