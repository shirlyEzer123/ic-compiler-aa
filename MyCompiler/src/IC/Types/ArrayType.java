package IC.Types;

public class ArrayType extends Type {
	Type elemType;
	
	public ArrayType(Type eType, int dim) {
		super("");
		String arrName = eType.getName();
		for ( int i = 0; i < dim ; i++ ){
			arrName += "[]";
		}
		elemType = eType;
		setName(arrName);
	}
}
