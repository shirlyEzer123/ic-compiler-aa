package IC.Types;

public class ArrayType extends Type {
	Type elemType;
	
	public ArrayType(Type eType) {
		super(eType.getName() + "_arr");
		elemType = eType;
	}
}
