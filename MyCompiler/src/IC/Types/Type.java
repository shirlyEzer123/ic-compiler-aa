package IC.Types;

public abstract class Type {
	private String name;
	
	
	
	public Type(String name) {
		super();
		this.name = name;
	}

	boolean subtypeof(Type t) {
		return (this == t);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
