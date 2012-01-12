package IC.Types;

public class MethodType extends Type {
	
	public MethodType(Type[] paramTypes, Type returnType) {
		super("");
		this.paramTypes = paramTypes;
		this.returnType = returnType;
		setName(this.toString());
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
	
	@Override
	public String toString() {
		String str = "";
		Type[] ps = getParamTypes();
		for(int i=0; i<ps.length-1; i++){
			str += ps[i].toString() + ", ";
		}
		if(ps.length > 0){
			str += ps[ps.length-1].toString();
		}
			
		str += " -> " + returnType.toString();
		
		return str;
	}
}
