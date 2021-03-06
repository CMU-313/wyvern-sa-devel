package wyvern.tools.typedAST.extensions.interop.java.typedAST;

import wyvern.tools.errors.FileLocation;
import wyvern.tools.parsing.LineParser;
import wyvern.tools.parsing.LineSequenceParser;
import wyvern.tools.typedAST.abs.CachingTypedAST;
import wyvern.tools.typedAST.core.binding.NameBindingImpl;
import wyvern.tools.typedAST.core.binding.TypeBinding;
import wyvern.tools.typedAST.interfaces.EnvironmentExtender;
import wyvern.tools.typedAST.interfaces.Value;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.util.TreeWriter;

public class JavaType implements EnvironmentExtender {
	private final Type equivType;
	private final String name;

	public JavaType(String name, Type equivType) {
		this.name = name;
		this.equivType = equivType;
	}

	@Override
	public Type getType() {
		return equivType;
	}

	@Override
	public Type typecheck(Environment env) {
		return equivType;
	}

	@Override
	public Value evaluate(Environment env) {
		return null;
	}

	@Override
	public LineParser getLineParser() {
		return null;
	}

	@Override
	public LineSequenceParser getLineSequenceParser() {
		return null;
	}

	@Override
	public FileLocation getLocation() {
		return FileLocation.UNKNOWN;
	}

	@Override
	public void writeArgsToTree(TreeWriter writer) {
	}

	@Override
	public Environment extend(Environment env) {
		return env
				.extend(new TypeBinding(name, equivType))
				.extend(new NameBindingImpl(name, equivType));
	}

	@Override
	public Environment evalDecl(Environment env) {
		return env;
	}
}
