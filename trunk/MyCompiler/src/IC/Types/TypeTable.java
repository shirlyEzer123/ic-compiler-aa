package IC.Types;

import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import IC.ICVoid;
import IC.AST.ICClass;
import IC.AST.PrimitiveType;
import IC.AST.UserType;
import IC.SemanticChecks.SemanticError;

public class TypeTable {
	// Maps element types to array types\
	private static Map<Type, ArrayType> uniqueArrayTypes = new HashMap<>();
	private static Map<String, ClassType> uniqueClassTypes = new HashMap<>();

	public static Type boolType = new BoolType();
	public static Type intType = new IntType();
	public static Type stringType = new StringType();
	public static Type voidType = new VoidType();
	
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
	public static ArrayType arrayType(Type elemType) {
		if (uniqueArrayTypes.containsKey(elemType)) {
			// array type object already created – return it
			return uniqueArrayTypes.get(elemType);
		} else {
			// object doesn’t exist – create and return it
			ArrayType arrt = new ArrayType(elemType);
			uniqueArrayTypes.put(elemType, arrt);
			return arrt;
		}
	}

	public static Type astType(IC.AST.Type type) throws SemanticError {
		if ( type instanceof PrimitiveType) {
			switch ( ((PrimitiveType)type).getType() ) {
			case BOOLEAN:
				return boolType;
			case INT:
				return intType;
			case STRING:
				return stringType;
			case VOID:
				return voidType;
			default:
				throw new SemanticError("Something went horribly wrong!!!");
			}
		} else if ( type instanceof UserType ) {
			String className = ((UserType) type).getName();
			return getUserType(className);
		} else if ( type instanceof ICVoid ) {
			return voidType;
		} else {
			throw new SemanticError("Something went horribly wrong!!!");
		}
	}
}
