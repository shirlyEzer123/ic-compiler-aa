package IC.SymbolTable;

import IC.Types.Type;

public class Symbol {
	private String id;
	private Type type;
	private Kind kind;
	private boolean isStatic = false;
	private boolean unresolved = false;
	
	public Symbol(String id, Type type, Kind kind) {
		super();
		this.id = id;
		this.type = type;
		this.kind = kind;
	}

	
	public String getId() {
		return id;
	}


	public boolean isUnresolved() {
		return unresolved;
	}


	public void setUnresolved(boolean unresolved) {
		this.unresolved = unresolved;
	}


	public boolean isStatic() {
		return isStatic;
	}


	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}
}
