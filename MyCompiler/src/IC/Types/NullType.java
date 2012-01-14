package IC.Types;

public class NullType extends Type {
	public NullType() {
		super("null");
	}
	
	@Override
	boolean subtypeof(Type t) {
		
		if( (t == TypeTable.intType) ||
				(t == TypeTable.boolType ) )
			return false;
				
		return true;
	}
}
