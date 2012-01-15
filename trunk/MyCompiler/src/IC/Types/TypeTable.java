package IC.Types;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import IC.ICVoid;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.Library;
import IC.AST.PrimitiveType;
import IC.AST.UserType;
import IC.SemanticChecks.SemanticError;
import IC.SymbolTable.SymbolTable;

public class TypeTable {
	// Maps element types to array types\
	private static Map<String, ArrayType> uniqueArrayTypes = new LinkedHashMap<>();
	private static Map<String, ClassType> uniqueClassTypes = new LinkedHashMap<>();
	private static Map<String, MethodType> uniqueMethodTypes = new LinkedHashMap<>();
	
	public static Type boolType = new BoolType();
	public static Type intType = new IntType();
	public static Type stringType = new StringType();
	public static Type voidType = new VoidType();
	public static Type nullType = new NullType();
	
	private TypeTable() {
		super();
	}


	public static void addUserType(ICClass icClass) throws SemanticError{
		String className = icClass.getName();
		if ( uniqueClassTypes.containsKey(className) ){
			throw new SemanticError(icClass.getLine(),"Class redefined: " + className);
		}
		uniqueClassTypes.put(className, new ClassType(icClass));
	}
	
	public static ClassType getUserType(String className){
		if ( uniqueClassTypes.containsKey(className) )
			return uniqueClassTypes.get(className);
		return null;
	}
	

	// Returns unique array type object
	public static ArrayType arrayType(Type elemType, int dim) {
		ArrayType arrt = null;
		if ( dim > 1 ){
			arrt = new ArrayType(arrayType(elemType, dim-1),1);
		} else {
			arrt = new ArrayType(elemType, dim);
		}
		if (!uniqueArrayTypes.containsKey(arrt.getName())) {
			// object doesn’t exist – create and return it
			uniqueArrayTypes.put(arrt.getName(), arrt);
			return arrt;
		}
		return uniqueArrayTypes.get(arrt.getName());
	}

	public static Type literalType( IC.LiteralTypes type ){
		switch( type ) {
		case FALSE:
		case TRUE:
			return boolType;
		case INTEGER:
			return intType;
		case STRING:
			return stringType;
		case NULL:
			return nullType;
		}
		return null;
	}
	public static Type astType(IC.AST.Type type) throws SemanticError {
		// TODO : NULL stuff
		Type result = null;
		if ( type instanceof PrimitiveType) {
			switch ( ((PrimitiveType)type).getType() ) {
			case BOOLEAN:
				result = boolType;
				break;
			case INT:
				result = intType;
				break;
			case STRING:
				result = stringType;
				break;
			case VOID:
				result = voidType;
				break;
			default:
				throw new SemanticError(type.getLine(),"Something went horribly wrong!!!");
			}
		} else if ( type instanceof UserType ) {
			String className = ((UserType) type).getName();
			result = getUserType(className);
		} else if ( type instanceof ICVoid ) {
			result =  voidType;
		} else {
			throw new SemanticError(type.getLine(),"Something went horribly wrong!!!");
		}
		if ( type.getDimension() > 0 )
			return arrayType(result, type.getDimension());
		return result;
	}
	
//	public static MethodType methodType(Type[] paramTypes, Type ret) {
	public static MethodType methodType( IC.AST.Method astMethod ){
		List<Formal> fLst = astMethod.getFormals();
		Type[] paramTypes = new Type[fLst.size()];
		try{
			for ( int i = 0; i < fLst.size(); i++ ){
				paramTypes[i] = astType(fLst.get(i).getType());
			}
			Type ret = astType(astMethod.getType());
			MethodType mt = new MethodType(paramTypes, ret);
			if (!uniqueMethodTypes.containsKey(mt.getName())) {
				// object doesn’t exist – insert it
				uniqueMethodTypes.put(mt.getName(), mt);
				return mt;
			}
			return uniqueMethodTypes.get(mt.getName());
		} catch (SemanticError e) {
			System.err.println("Something went horribly wrong..");
			return null;
		}
	}

	public static void printTable(String filename, boolean printLibrary) {
		String str = "Type Table: " + filename + "\n";
		str += "\t" + intType.getID() + ": Primitive type: " + intType + "\n";
		str += "\t" + boolType.getID() + ": Primitive type: " + boolType + "\n";
		str += "\t" + nullType.getID() + ": Primitive type: " + nullType + "\n";
		str += "\t" + stringType.getID() + ": Primitive type: " + stringType + "\n";
		str += "\t" + voidType.getID() + ": Primitive type: " + voidType + "\n";
		
		for(String s : uniqueClassTypes.keySet()){
			ClassType ct = uniqueClassTypes.get(s);
			str += "\t" + ct.getID() + ": Class: " + s ;
			if ( ct.getParent() != null )
				str += ", Superclass ID: " + ct.getParent().getID();
			str += "\n";
		}
		
		for(String s : uniqueArrayTypes.keySet()){
			ArrayType at = uniqueArrayTypes.get(s);
			str += "\t" + at.getID() + ": Array type: " + at + "\n";
		}

		for(String s : uniqueMethodTypes.keySet()){
			MethodType mt = uniqueMethodTypes.get(s);
			if(mt.isLibraryMethod())
				continue;
			str += "\t" + mt.getID() + ": Method type: {" + mt + "}\n";
		}

		System.out.println(str);
	}


	public static SymbolTable getClassSymTab(String className) {
		if ( uniqueClassTypes.containsKey(className)) {
			return uniqueClassTypes.get(className).getSymbolTable();
		}
		return null;
	}
}