package wyvern.tools.parsing;

import wyvern.tools.rawAST.ExpressionSequence;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.types.Environment;
import wyvern.tools.util.CompilationContext;
import wyvern.tools.util.Pair;

public interface DeclParser extends LineParser {
	Pair<Environment, ContParser> parseDeferred(TypedAST first, CompilationContext ctx);
}
