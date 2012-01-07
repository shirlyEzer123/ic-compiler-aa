package IC.Types;

public class MethodType extends Type {
	
	public MethodType(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	private Type[] paramTypes;
	private Type returnType;
	
	public Type[] getParamTypes() {
		return paramTypes;
	}

	public void setParamTypes(Type[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	public Type getReturnType() {
		return returnType;
	}

	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}
}
