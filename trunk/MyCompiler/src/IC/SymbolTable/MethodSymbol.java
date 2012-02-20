package IC.SymbolTable;

import java.util.List;

import IC.AST.Formal;
import IC.Types.MethodType;

public class MethodSymbol extends Symbol {

	private List<Formal> formals;

	public MethodSymbol(String name, MethodType mt, Kind method, int line,
			List<Formal> formals) {
		super(name, mt, method, line);
		this.setFormals(formals);
		
	}

	public List<Formal> getFormals() {
		return formals;
	}

	private void setFormals(List<Formal> formals) {
		this.formals = formals;
	}

}
