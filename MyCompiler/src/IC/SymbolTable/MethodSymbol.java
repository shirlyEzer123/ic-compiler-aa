package IC.SymbolTable;

import java.util.ArrayList;
import java.util.List;

import IC.AST.Formal;
import IC.Types.MethodType;

public class MethodSymbol extends Symbol {

	private List<Formal> formals;

	public MethodSymbol(String name, MethodType mt, Kind method, int line,
			List<Formal> formals, String uid) {
		super(name, mt, method, line, uid);
		this.setFormals(formals);
		
	}

	public List<Formal> getFormals() {
		return formals;
	}

	private void setFormals(List<Formal> formals) {
		this.formals = formals;
	}

	public List<String> getFormalUIDs() {
		List<String> uids = new ArrayList<>(formals.size());
		for ( Formal f : formals )
			uids.add(f.getLirName());
		return uids;
	}

}
