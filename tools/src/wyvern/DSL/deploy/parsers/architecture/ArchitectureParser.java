package wyvern.DSL.deploy.parsers.architecture;

import wyvern.DSL.deploy.Deploy;
import wyvern.DSL.deploy.typedAST.architecture.Architecture;
import wyvern.DSL.deploy.typedAST.architecture.Endpoint;
import wyvern.tools.parsing.*;
import wyvern.tools.typedAST.core.Sequence;
import wyvern.tools.typedAST.core.declarations.DeclSequence;
import wyvern.tools.typedAST.core.declarations.TypeDeclaration;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.types.Environment;
import wyvern.tools.util.CompilationContext;
import wyvern.tools.util.Pair;

import java.util.Iterator;
import java.util.LinkedList;

public class ArchitectureParser implements DeclParser {
	private LinkedList<Endpoint> endpoints = new LinkedList<>();

	public void addEndpoint(Endpoint endpoint) {
		endpoints.add(endpoint);
	}

	@Override
	public TypedAST parse(TypedAST first, CompilationContext ctx) {
		String name = ParseUtils.parseSymbol(ctx).name;
		TypedAST body = null;

		if (ctx.getTokens() != null)
			body = new BodyParser(ctx).visit(ctx.getTokens(), Deploy.getArchInnerEnv(this));
		return new Architecture(name, body);
	}

	@Override
	public Pair<Environment, ContParser> parseDeferred(TypedAST first, CompilationContext ctx) {
		endpoints = new LinkedList<>();
		String name = ParseUtils.parseSymbol(ctx).name;
		final Architecture innerArch = new Architecture(name, null);

		Pair<Environment, ContParser> parsePartialI = null;

		if (ctx.getTokens() != null) {
			parsePartialI = ParseUtils.extractLines(ctx).accept(new DeclarationParser(ctx), ctx.getEnv().setInternalEnv(Deploy.getArchInnerEnv(this)));
		}

		final Pair<Environment, ContParser> parsePartial = parsePartialI;


		Environment output = ctx.getEnv();
		for (Endpoint endpoint : endpoints) {
			output = endpoint.getDecl().extend(output);
		}

		return new Pair<Environment, ContParser>(output,
				new ArchContParser(innerArch, parsePartial));
	}

	private class ArchContParser implements ContParser {
		private final Architecture innerArch;
		private final Pair<Environment, ContParser> parsePartial;

		public ArchContParser(Architecture innerArch, Pair<Environment, ContParser> parsePartial) {
			this.innerArch = innerArch;
			this.parsePartial = parsePartial;
		}

        @Override
		public TypedAST parse(EnvironmentResolver r) {
			Environment inner = r.getEnv(innerArch);
			TypedAST body = null;
			if (parsePartial != null)
				body = parsePartial.second.parse(new SimpleResolver(inner.setInternalEnv(parsePartial.first)));

			if (body instanceof Sequence) {
				Iterator<TypedAST> flat = ((Sequence) body).flatten();
				while (flat.hasNext()) {

					flat.next();
				}
			}

			innerArch.setBody(body);

			LinkedList<TypeDeclaration> tds = new LinkedList<>();
			for (Endpoint endpoint : ArchitectureParser.this.endpoints)
				tds.add(endpoint.resolve());
			return new DeclSequence(tds);
		}
	}
}
