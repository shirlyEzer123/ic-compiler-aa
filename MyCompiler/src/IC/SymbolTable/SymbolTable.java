package IC.SymbolTable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import IC.SemanticChecks.SemanticError;

public class SymbolTable {
	  /** map from String to Symbol **/
	  private Map<String,Symbol> entries;
	  private String id;
	  private SymbolTable parentSymbolTable;
	  private List<SymbolTable> childs = new LinkedList<>(); 
	  
	  public SymbolTable(String id) {
	    this.setId(id);
	    entries = new HashMap<String,Symbol>();
	  }
	  
	  public void insertSymbol(Symbol sym) throws SemanticError{
		  if ( entries.containsKey( sym.getId() ) ){
			  throw new SemanticError("Redecleration of symbol " + sym.getId());
		  }
		  else {
			  entries.put(sym.getId(), sym);
		  }
	  }
	  
	  public Symbol lookup(String ID) throws SemanticError{
		  if(entries.containsKey(ID))
			  return entries.get(ID);
		  if(parentSymbolTable != null)
			  return parentSymbolTable.lookup(ID);
		  else
			  throw new SemanticError("Unknown symbol " + ID);
	  }

	public SymbolTable getParentSymbolTable() {
		return parentSymbolTable;
	}

	public void setParentSymbolTable(SymbolTable parentSymbolTable) {
		this.parentSymbolTable = parentSymbolTable;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<SymbolTable> getChilds() {
		return childs;
	}

	public void printSymbolTable(SymbolTable st, String libFileName) {
		if(st.id == "Global"){
			System.out.println("Global Symbol Table: " + libFileName);
			for(String e : entries.keySet()){
				System.out.println("\tClass: " + e);
			}
			System.out.print("Children tables: ");
			for(SymbolTable syta : childs){
				System.out.print(syta.id + ", ");
			}
			System.out.println("");
			System.out.println();
		}
		for(SymbolTable syta : st.childs){
			printTableName(syta);
			printFields(syta);
			printStaticMethods(syta);
			printVirtualMethods(syta);
			printChildrenTables(syta);
		}
		
		
	}

	private void printChildrenTables(SymbolTable syta) {
		System.out.print("Children tables: ");
		for(SymbolTable s : syta.childs) {
			System.out.print(s.id + ", ");
		}
		System.out.println("");
		System.out.println("");
		for(SymbolTable s : syta.childs) {
			printSymbolTable(s, "");
		}
		
	}

	private void printVirtualMethods(SymbolTable syta) {
		for(Symbol s : syta.entries.values()){
			if(s.getKind() == Kind.METHOD && !s.isStatic() ){
				System.out.println("\tVirtual method: " + s.getId());
				
			}
		}	
	}

	private void printStaticMethods(SymbolTable syta) {
		for(Symbol s : syta.entries.values()){
			if(s.getKind() == Kind.METHOD && s.isStatic() ){
				System.out.println("\tStatic method: " + s.getId());
				
			}
		}	
	}

	private void printFields(SymbolTable syta) {
		for(Symbol s : syta.entries.values()){
			if(s.getKind() == Kind.FIELD){
				System.out.println("\tField: " + s.getId());
			}
		}	
	}

	private void printTableName(SymbolTable syta) {
		System.out.println("Class Symbol Table: " + syta.id);
	}
	

}
