package IC.SemanticChecks;

import IC.AST.ICClass;
import IC.AST.Program;
import IC.AST.StaticMethod;
import IC.SymbolTable.Kind;
import IC.SymbolTable.Symbol;
import IC.SymbolTable.SymbolTable;
import IC.Types.MethodType;
import IC.Types.Type;
import IC.Types.TypeTable;

public class SingleMainMethod {
	private boolean mainFound = false;

	/*
	 * checks that only a single 'main' method exists
	 */
	public void checkForSingleMain(Program program) throws SemanticError {

		for ( ICClass icClass : program.getClasses() ) {
			SymbolTable st = icClass.getEnclosingScope();
			Symbol mainSym = st.lookin("main");
			if ( mainSym != null ) {
				if ( checkMain(mainSym) ){
					if ( mainFound  )
						throw new SemanticError(mainSym.getLine(),
								"program can only contain one 'main' method");
					else
						mainFound = true;
				}
			}
		}
		if ( ! mainFound ){
			throw new SemanticError(program.getLine(),
					"program must have a 'static void main(string[])' method");
		}
	}

	private boolean checkMain(Symbol mainSym) {
		if ( mainSym.getKind() != Kind.METHOD )
			return false;
		if ( mainSym.isStatic() == false )
			return false;
		MethodType mt = (MethodType) mainSym.getType();
		if ( mt.getReturnType() != TypeTable.voidType )
			return false;
		Type[] paramTypes = mt.getParamTypes();
		if ( paramTypes.length != 1 )
			return false;
		if ( paramTypes[0] != TypeTable.arrayType(TypeTable.stringType, 1))
			return false;
		return true;
	}

}
