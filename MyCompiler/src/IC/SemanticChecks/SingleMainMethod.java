package IC.SemanticChecks;

import IC.AST.StaticMethod;
import IC.SymbolTable.Kind;
import IC.SymbolTable.Symbol;
import IC.SymbolTable.SymbolTable;

public class SingleMainMethod {
	public Symbol sm = null;
	SymbolTable mainST = null;

	/*
	 * checks that only a single 'main' method exists
	 */
	public void checkForSingleMain(SymbolTable st) throws SemanticError {

		if (st.getEntries().containsKey("main")
				&& st.getEntries().get("main").getKind() == Kind.METHOD) {
			if (sm == null) { //first time we see 'main' method
				sm = st.getEntries().get("main");
				mainST = st;
			} else { //already saw 'main' method
				sm = st.getEntries().get("main");
				throw new SemanticError(sm.getLine(),
						"program can only contain one 'main' method");
			}
		} 
		//recursively check child symbol tables
		for (SymbolTable childSymbolTable : st.getChilds()) {
			checkForSingleMain(childSymbolTable);
		}
	}

	/*
	 * checks that 'main' has a correct signature
	 * assuming that this method is called directly after checkForSingleMain
	 */
	public void checkCorrectSignatureMain() throws SemanticError {
		boolean sawOneParam = false;

		if (sm == null) {
			throw new SemanticError(0, "program doesnt contain a 'main' method");
		}
		if (!sm.isStatic()) {
			throw new SemanticError(sm.getLine(),
					"'main' method must be static");
		}
		
		//making sure that 'main' argument is of type string[], and that only one argument exists
		for (SymbolTable childSymTab : mainST.getChilds()) { 
			if (childSymTab.getId().equals("main")) {
				for (Symbol sym : childSymTab.getEntries().values()) {
					if (sym.getKind() == Kind.PARAMETER)
						if (!sawOneParam) {
							sawOneParam = true;
							if (!sym.getType().getName().equals("string[]")) {
								throw new SemanticError(sm.getLine(),
										"wrong type of arguments for 'main' method");
							}
						} else {
							throw new SemanticError(sm.getLine(),
									"wrong number of arguments for 'main' method");
						}
				}
			}
		}
		if(!sawOneParam)
			throw new SemanticError(sm.getLine(),
					"'main method must have string[] argument");
	}
}
