package IC.SymbolTable;

import java.util.HashMap;
import java.util.Iterator;
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
	  private Kind kind;
	  
	  public SymbolTable(String id, Kind kind) {
	    this.setId(id);
	    entries = new HashMap<String,Symbol>();
	    setKind(kind);
	  }
	  
	  public void insertSymbol(Symbol sym) throws SemanticError{
		  if ( entries.containsKey( sym.getId() ) ){
			  throw new SemanticError(sym.getLine(), "Redecleration of symbol " + sym.getId());
		  }
		  
		  //Enforcing no redcleration of fields or methods which have already been declared by superclass
		  else if(getParentSymbolTable() != null){
			  if(getParentSymbolTable().lookup(sym.getId()) != null){
				  throw new SemanticError(sym.getLine(), "Redecleration of symbol " + sym.getId() + " which appears in superclass");
			  }	  
		  }
		  
		  entries.put(sym.getId(), sym);
		  
	  }
	  
	  public Symbol lookup(String ID) {
		  if(entries.containsKey(ID))
			  return entries.get(ID);
		  if(parentSymbolTable != null)
			  return parentSymbolTable.lookup(ID);
		  else
			  return null;
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


	public void printSymbolTable(String name, SymbolTable st, String libFileName) {
		//System.out.print(name +" Symbol Table");
		if(st.id == "Global"){
			System.out.println("Global Symbol Table: " + libFileName);
			for(String e : entries.keySet()){
				System.out.println("\tClass: " + e);
			}
			System.out.print("Children tables: ");
			for ( Iterator<SymbolTable> iter = childs.iterator(); iter.hasNext(); ){
				SymbolTable syta = iter.next();
				System.out.print(syta.id);
				if ( iter.hasNext() )
					System.out.print(", ");
			}
			System.out.println("");
			System.out.println();
		}
		for(SymbolTable syta : st.childs){
			printTableName(syta);
			printFields(syta);
			printStaticMethods(syta);
			printVirtualMethods(syta);
			printParameters(syta);
			printLocalVariables(syta);
			printChildrenTables(syta);
			
			
		}
		
		for ( Iterator<SymbolTable> iter = st.childs.iterator(); iter.hasNext(); ){
			SymbolTable syta = iter.next();
			printSymbolTable(syta.getId(), syta, null);
		}
		
	}

	private void printLocalVariables(SymbolTable syta) {
		for(Symbol s : syta.entries.values()){
			if(s.getKind() == Kind.VARIABLE){
				System.out.println("\tLocal variable: " + s.getType() + " " + s.getId());
			}
		}	
	}

	private void printParameters(SymbolTable syta) {
		for(Symbol s : syta.entries.values()){
			if(s.getKind() == Kind.PARAMETER){
				System.out.println("\tParameter: " + s.getType() + " " + s.getId());
			}
		}	
		
	}

	private static void printChildrenTables(SymbolTable syta) {
		if ( syta.childs.size() == 0 ){
			System.out.println();
			return;
		}
		System.out.print("Children tables: ");
		for ( Iterator<SymbolTable> iter = syta.childs.iterator(); iter.hasNext(); ){
			SymbolTable syta1 = iter.next();
			System.out.print(syta1.id);
			if ( iter.hasNext() )
				System.out.print(", ");
		}
		System.out.println("\n");		
	}

	private static void printVirtualMethods(SymbolTable syta) {
		for(Symbol s : syta.entries.values()){
			if(s.getKind() == Kind.METHOD && !s.isStatic() ){
				System.out.println("\tVirtual method: " + s.getId() + " {" + s.getType() + "}");
				
			}
		}	
	}

	private static void printStaticMethods(SymbolTable syta) {
		for(Symbol s : syta.entries.values()){
			if(s.getKind() == Kind.METHOD && s.isStatic() ){
				System.out.println("\tStatic method: " + s.getId() + " {" + s.getType() + "}");
				
			}
		}	
	}

	private static void printFields(SymbolTable syta) {
		for(Symbol s : syta.entries.values()){
			if(s.getKind() == Kind.FIELD){
				System.out.println("\tField: " + s.getType() + " " + s.getId());
			}
		}	
	}

	private void printTableName(SymbolTable syta) {
		switch (syta.getKind()){
		case CLASS:
			System.out.println("Class Symbol Table: " + syta.id);
			break;
		case BLOCK:
			System.out.println("Statement Block Symbol Table ( located in "
					+ syta.getParentSymbolTable().getId() + " )");
			break;
		case METHOD:
			System.out.println("Method Symbol Table: " + syta.id);
			break;
		default:
			break;
		}
		
	}

	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}
	
	public Map<String,Symbol> getEntries(){
		return entries;
	}
	

}
