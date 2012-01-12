package IC;

import java.io.FileNotFoundException;
import java.io.FileReader;

import IC.Parser.LibraryParser;
import IC.Parser.Lexer;
import IC.Parser.Parser;
import IC.Parser.SyntaxError;
import IC.SymbolTable.SymbolTable;
import IC.SymbolTable.SymbolTableConstructor;
import IC.Types.TypeTable;
import java_cup.runtime.Symbol;
import IC.AST.Library;
import IC.AST.PrettyPrinter;
import IC.AST.Program;

/**
 * <H1>Tel Aviv University</H1>
 * <H2>Compilation course</H2>
 * <H3>Compiler project</H3>
 * This is the main compiler class for the IC language.
 * 
 * @author Asaf Bruner, Aviv Goll
 */

public class Compiler {
	/**
	 * parses the program file and the Library file and constructs the proper AST.
	 * 
	 * @param args - as stated in Usage.
	 */
    public static void main(String[] args)
    {
    	
		if (args.length == 0) {
			System.out.println("Error: Missing input file argument!");
			printUsage();
			System.exit(-1);
		}
		
		// handle arguments parsing
		boolean printAST = false;
		boolean dumpSymTab = false;
		String libFileName = "libic.sig";
		for ( int i = 1; i < args.length; i++) {
			if ( ( args[i].charAt(0) == '-') && (args[i].charAt(1) == 'L') ) {
				libFileName = args[i].substring(2);
			} else if ( args[i].equals("-print-ast") ){
				printAST = true;
			}
			else if (args[i].equals("-dump-symtab") ){
				dumpSymTab = true;
			}
		}
		
		// Parse the library file
		FileReader libFile = null;
		try {
			libFile = new FileReader(libFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Lexer libscanner = new Lexer(libFile);
		LibraryParser libparser = new LibraryParser(libscanner);
		Symbol libSym = null;
		try {
			libSym = libparser.parse();
			System.out.println("Parsed " + libFileName + " successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		Library libRoot = (Library) libSym.value;
		if ( printAST ) {
			PrettyPrinter printer = new PrettyPrinter(libFileName);
			System.out.println( printer.visit(libRoot) );
		}
		
		// Parse the input file
		FileReader txtFile = null;
		try {
			txtFile = new FileReader(args[0]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Lexer scanner = new Lexer(txtFile);
		Parser parser = new Parser(scanner);
		try {
			Symbol parseSymbol = parser.parse();
			System.out.println("Parsed " + args[0] + " successfully!");
			System.out.println("");
			Program root = (Program) parseSymbol.value;
			root.setLibrary(libRoot);
			if ( printAST ) {
				PrettyPrinter printer = new PrettyPrinter(args[0]);
				System.out.println( printer.visit(root) );
			}	
			SymbolTableConstructor stc = new SymbolTableConstructor();
			SymbolTable st = (SymbolTable) stc.visit(root);
		
			if(dumpSymTab){
				st.printSymbolTable("Global",st, args[0]);
				TypeTable.printTable();
			}
		} catch (SyntaxError e) {
			System.err.println("Syntax Error: Line " + e.getLine() + ": " + e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/** 
	 * Prints usage information about this application to System.out
	 */
	public static void printUsage() {
		System.out.println("Usage: java IC.Compiler <file.ic> [ -L</path/to/libic.sig> ]");
	}
}

