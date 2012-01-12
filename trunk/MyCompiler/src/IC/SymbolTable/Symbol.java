package IC.SymbolTable;

import IC.Types.Type;

public class Symbol {
	private String id;
	private Type type;
	private Kind kind;
	private boolean isStatic = false;
	private boolean unresolved = false;
	private SymbolTable relatedSymTab = null;
	
	public Symbol(String id, Type type, Kind kind) {
		super();
		this.id = id;
		this.setType(type);
		this.setKind(kind);
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


	public Kind getKind() {
		return kind;
	}


	public void setKind(Kind kind) {
		this.kind = kind;
	}


	public Type getType() {
		return type;
	}


	public void setType(Type type) {
		this.type = type;
	}


	public SymbolTable getRelatedSymTab() {
		return relatedSymTab ;
	}


	public void setRelatedSymTab(SymbolTable relatedSymTab) {
		this.relatedSymTab = relatedSymTab;
	}
}
