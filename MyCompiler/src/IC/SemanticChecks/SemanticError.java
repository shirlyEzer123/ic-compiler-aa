package IC.SemanticChecks;

public class SemanticError extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8781278017645798621L;
	
	public SemanticError(int line, String string) {
		super("semantic error at line " + line + ": " + string);
	}

}
