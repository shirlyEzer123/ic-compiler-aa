package IC.Types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import IC.ICVoid;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.PrimitiveType;
import IC.AST.UserType;
import IC.SemanticChecks.SemanticError;

public class TypeTable {
	// Maps element types to array types\
	private static Map<String, ArrayType> uniqueArrayTypes = new HashMap<>();
	private static Map<String, ClassType> uniqueClassTypes = new HashMap<>();
	private static Map<String, MethodType> uniqueMethodTypes = new HashMap<>();
	
	public static Type boolType = new BoolType();
	public static Type intType = new IntType();
	public static Type stringType = new StringType();
	public static Type voidType = new VoidType();
	public static Type nullType = new NullType();
	
	private TypeTable() {
		super();
	}

	// ...

	public static void addUserType(ICClass icClass) throws SemanticError{
		String className = icClass.getName();
		if ( uniqueClassTypes.containsKey(className) ){
			throw new SemanticError("Class redefined: " + className);
		}
		uniqueClassTypes.put(className, new ClassType(icClass));
	}
	
	public static ClassType getUserType(String className) throws SemanticError{
		if ( uniqueClassTypes.containsKey(className) )
			return uniqueClassTypes.get(className);
		throw new SemanticError("No such type: " + className);
	}
	
//	// Returns unique class type object
//	public static ClassType classType(ICClass classObj) {
//		if (uniqueClassTypes.containsKey(classObj.getName())) {
//			// array type object already created – return it
//			return uniqueClassTypes.get(classObj.getName());
//		} else {
//			// object doesn’t exist – create and return it
//			ClassType classt = new ClassType(classObj);
//			uniqueClassTypes.put(classObj.getName(), classt);
//			return classt;
//		}
//	}
//
//	// Returns unique class type object
//	public static ClassType classType(String className) {
//		if (uniqueClassTypes.containsKey(className)) {
//			// array type object already created – return it
//			return uniqueClassTypes.get(className);
//		} else {
//			// object doesn’t exist – create and return it
//			ClassType classt = new ClassType(className);
//			uniqueClassTypes.put(className, classt);
//			return classt;
//		}
//	}

	// Returns unique array type object
	public static ArrayType arrayType(Type elemType, int dim) {
		if ( dim > 1 ){
			arrayType(elemType, dim-1);
		}
		ArrayType arrt = new ArrayType(elemType, dim);
		if (!uniqueArrayTypes.containsKey(arrt.getName())) {
			// object doesn’t exist – create and return it
			uniqueArrayTypes.put(arrt.getName(), arrt);
			return arrt;
		}
		return uniqueArrayTypes.get(arrt.getName());
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
				throw new SemanticError("Something went horribly wrong!!!");
			}
		} else if ( type instanceof UserType ) {
			String className = ((UserType) type).getName();
			result = getUserType(className);
		} else if ( type instanceof ICVoid ) {
			result =  voidType;
		} else {
			throw new SemanticError("Something went horribly wrong!!!");
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

	public static void printTable() {
		String str = "";
		str += intType.getID() + ": Primitive type: " + intType + "\n";
		str += boolType.getID() + ": Primitive type: " + boolType + "\n";
		str += nullType.getID() + ": Primitive type: " + nullType + "\n";
		str += stringType.getID() + ": Primitive type: " + stringType + "\n";
		str += voidType.getID() + ": Primitive type: " + voidType + "\n";
		
		for(String s : uniqueClassTypes.keySet()){
			ClassType ct = uniqueClassTypes.get(s);
			str += ct.getID() + ": Class: " + s ;
			if ( ct.getParent() != null )
				str += ", Superclass ID: " + ct.getParent().getID();
			str += "\n";
		}
		
		for(String s : uniqueArrayTypes.keySet()){
			ArrayType at = uniqueArrayTypes.get(s);
			str += at.getID() + ": Array type: " + at + "\n";
		}

		for(String s : uniqueMethodTypes.keySet()){
			MethodType mt = uniqueMethodTypes.get(s);
			str += mt.getID() + ": Method type: {" + mt + "}\n";
		}

		System.out.println(str);
	}
	
}