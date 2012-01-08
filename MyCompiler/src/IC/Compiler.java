package IC;

import java.io.FileNotFoundException;
import java.io.FileReader;

import IC.Parser.LibraryParser;
import IC.Parser.Lexer;
import IC.Parser.Parser;
import IC.Parser.SyntaxError;
import IC.SymbolTable.SymbolTable;
import IC.SymbolTable.SymbolTableConstructor;
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
		String libFileName = "libic.sig";
		for ( int i = 1; i < args.length; i++) {
			if ( ( args[i].charAt(0) == '-') && (args[i].charAt(1) == 'L') ) {
				libFileName = args[i].substring(2);
			} else if ( args[i].equals("-print-ast") ){
				printAST = true;
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
		try {
			Symbol parseSymbol = libparser.parse();
			System.out.println("Parsed " + libFileName + " successfully!");
			
			if ( printAST ) {
				Library root = (Library) parseSymbol.value;
				PrettyPrinter printer = new PrettyPrinter(libFileName);
				//System.out.println( printer.visit(root) );
			}
		} catch (Exception e) {
			//e.printStackTrace();
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
			if ( printAST ) {
				Program root = (Program) parseSymbol.value;
				//PrettyPrinter printer = new PrettyPrinter(args[0]);
				//System.out.println( printer.visit(root) );
				SymbolTableConstructor stc = new SymbolTableConstructor();
				SymbolTable st = (SymbolTable) stc.visit(root);
				st.printSymbolTable(st, args[0]);
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

