package wyvern.tools.typedAST.extensions;

import wyvern.tools.errors.FileLocation;
import wyvern.tools.typedAST.abs.AbstractTypedAST;
import wyvern.tools.typedAST.abs.AbstractValue;
import wyvern.tools.typedAST.core.Application;
import wyvern.tools.typedAST.interfaces.ApplyableValue;
import wyvern.tools.typedAST.interfaces.CoreAST;
import wyvern.tools.typedAST.interfaces.CoreASTVisitor;
import wyvern.tools.typedAST.interfaces.Executor;
import wyvern.tools.typedAST.interfaces.Value;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.util.TreeWriter;

public class ExternalFunction extends AbstractValue implements ApplyableValue, CoreAST {
	private Type type;
	private Executor exec;
	
	public ExternalFunction(Type type, Executor exec) {
		this.type = type;
		this.exec = exec;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void writeArgsToTree(TreeWriter writer) {
		// no arguments at the moment; also not intended for serialization		
	}

	@Override
	public Value evaluateApplication(Application app, Environment env) {
		Value argValue = app.getArgument().evaluate(env);
		return exec.execute(argValue);
	}

	@Override
	public void accept(CoreASTVisitor visitor) {
		// TODO  Not really sure what to do here.
	}

	private FileLocation location = FileLocation.UNKNOWN;
	public FileLocation getLocation() {
		return this.location;
	}
}
