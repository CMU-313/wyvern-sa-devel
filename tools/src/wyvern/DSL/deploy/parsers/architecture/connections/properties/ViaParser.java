package wyvern.DSL.deploy.parsers.architecture.connections.properties;

import wyvern.DSL.deploy.parsers.architecture.connections.ConnectionPropertyParser;
import wyvern.DSL.deploy.typedAST.architecture.properties.ViaProperty;
import wyvern.tools.parsing.ContParser;
import wyvern.tools.parsing.ParseUtils;
import wyvern.tools.parsing.TypeParser;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.util.CompilationContext;
import wyvern.tools.util.Pair;

public class ViaParser extends ConnectionPropertyParser {
	@Override
	public TypedAST parse(TypedAST first, CompilationContext ctx) {
		return null;
	}

	@Override
	public Pair<Environment, ContParser> parseDeferred(final TypedAST first, final CompilationContext ctx) {
		final ViaProperty vp = new ViaProperty(null, null);
		final ParseUtils.LazyEval<Type> preType = TypeParser.parsePartialType(ctx);
		ctx.setEnv(vp.extend(ctx.getEnv()));
		final Pair<Environment, ContParser> preBody = super.iParse(ctx);
		return new Pair<Environment, ContParser>(
				vp.extend(Environment.getEmptyEnvironment()),
				new ContParser() {
                    @Override
					public TypedAST parse(EnvironmentResolver r) {
						Type connType = preType.eval(r.getEnv(vp).getExternalEnv());
						TypedAST body = preBody.second.parse(new ExtensionResolver(r, vp.getBinding()));
						vp.setVars(connType,body);
						return body;
					}
				}
		);
	}
}
