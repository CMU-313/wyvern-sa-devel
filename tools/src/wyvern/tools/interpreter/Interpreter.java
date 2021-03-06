package wyvern.tools.interpreter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;

import wyvern.DSL.DSL;
import wyvern.stdlib.Globals;
import wyvern.stdlib.Util;
import wyvern.tools.parsing.BodyParser;
import wyvern.tools.rawAST.RawAST;
import wyvern.tools.simpleParser.Phase1Parser;
import wyvern.tools.typedAST.core.values.IntegerConstant;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.typedAST.interfaces.Value;
import wyvern.tools.types.Environment;

public class Interpreter {
	public static void main(String args[]) {
		if (args.length != 1) {
			System.err.println("usage: wyvern file.wyv");
			return;
		}
		
		try {
			Reader reader = new FileReader(args[0]);
			
			Value resultValue = new Interpreter().interpret(reader, args[0]);
			
			System.out.println(resultValue);
			
			if (resultValue instanceof IntegerConstant) {
				System.exit(((IntegerConstant)resultValue).getValue());
			}

		} catch (FileNotFoundException e) {
			System.err.println("Error: cannot open file " + args[0]);
			System.exit(-1);
		}
	}

	public Value interpret(Reader reader, String filename) {
		return Util
				.doCompile(reader, filename, new ArrayList<DSL>())
				.evaluate(Environment.getEmptyEnvironment());
	}
}
