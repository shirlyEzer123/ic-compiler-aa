package IC.Types;

import IC.AST.ICClass;

public class ClassType extends Type {
	private ICClass classAST;
	private boolean unresolved = false;
	
	public ClassType(ICClass classObj) {
		super(classObj.getName());
		this.classAST = classObj;
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

	private ClassType parent;
	
	@Override
	boolean subtypeof(Type t) {
		if (super.subtypeof(t))
			return true;
		else if ( getParent() != null )
			return getParent().subtypeof(t);
		else
			return false;
	}

	public boolean isUnresolved() {
		return unresolved;
	}

	public void setUnresolved(boolean unresolved) {
		this.unresolved = unresolved;
	}

}
