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

}
