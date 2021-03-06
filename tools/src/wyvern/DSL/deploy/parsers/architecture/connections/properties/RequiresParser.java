package wyvern.DSL.deploy.parsers.architecture.connections.properties;

import wyvern.DSL.deploy.parsers.architecture.connections.ConnectionPropertyParser;
import wyvern.DSL.deploy.typedAST.architecture.properties.RequiresProperty;
import wyvern.tools.parsing.ContParser;
import wyvern.tools.parsing.ParseUtils;
import wyvern.tools.rawAST.ExpressionSequence;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.types.Environment;
import wyvern.tools.util.CompilationContext;
import wyvern.tools.util.Pair;

public class RequiresParser extends ConnectionPropertyParser {
	@Override
	public TypedAST parse(TypedAST first, CompilationContext ctx) {
		return null;
	}

	@Override
	public Pair<Environment, ContParser> parseDeferred(final TypedAST first, final CompilationContext ctx) {
		final RequiresProperty rp = new RequiresProperty(null, null);
		final ExpressionSequence es = ctx.getTokens();

		ctx.setEnv(rp.extend(ctx.getEnv()));

		final Pair<Environment, ContParser> predicate = ParseUtils.parseCondPartial(ctx);
		final Pair<Environment, ContParser> body = RequiresParser.super.iParse(ctx);

		ctx.setTokens(null);
		return new Pair<Environment, ContParser>(
			body.first.extend(rp.extend(Environment.getEmptyEnvironment())),
			new ContParser() {

                @Override
				public TypedAST parse(EnvironmentResolver r) {
					TypedAST bo = body.second.parse(new ExtensionResolver(r, rp.getBinding()));
					rp.setVals(predicate.second.parse(r), bo);
					return bo;
				}
			}
		);
	}
}
