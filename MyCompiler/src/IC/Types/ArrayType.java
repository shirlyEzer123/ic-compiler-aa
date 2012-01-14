package IC.Types;

public class ArrayType extends Type {
	private Type elemType;
	
	public ArrayType(Type eType, int dim) {
		super("");
		String arrName = eType.getName();
		for ( int i = 0; i < dim ; i++ ){
			arrName += "[]";
		}
		setElemType(eType);
		setName(arrName);
	}

	public Type getElemType() {
		return elemType;
	}

	public void setElemType(Type elemType) {
		this.elemType = elemType;
	}
}
