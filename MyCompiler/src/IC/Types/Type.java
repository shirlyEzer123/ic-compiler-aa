package IC.Types;

public abstract class Type {
	private String name;
	private int ID;
	
	private static int NextID = 0;
	
	public Type(String name) {
		super();
		this.name = name;
		this.ID = NextID++;
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
	
	public int getID() {
		return ID;
	}

	@Override
	public String toString() {
		return name;
	}

}
