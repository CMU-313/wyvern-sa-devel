package wyvern.tools.typedAST.core.values;

import wyvern.tools.errors.FileLocation;
import wyvern.tools.typedAST.abs.AbstractTypedAST;
import wyvern.tools.typedAST.abs.AbstractValue;
import wyvern.tools.typedAST.core.Invocation;
import wyvern.tools.typedAST.interfaces.CoreAST;
import wyvern.tools.typedAST.interfaces.CoreASTVisitor;
import wyvern.tools.typedAST.interfaces.InvokableValue;
import wyvern.tools.typedAST.interfaces.Value;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.types.extensions.Str;
import wyvern.tools.util.TreeWriter;

public class StringConstant extends AbstractValue implements InvokableValue, CoreAST {
	private String value;
	
	public StringConstant(String s) { this.value = s; }

	@Override
	public Type getType() {
		return Str.getInstance();
	}

	@Override
	public void writeArgsToTree(TreeWriter writer) {
		writer.writeArgs(this.value);
	}
	
	public String getValue() {
		return value;
	}
	

	@Override
	public Value evaluateInvocation(Invocation exp, Environment env) {
		String operator = exp.getOperationName();
		if (!operator.equals("+")) {
			throw new RuntimeException("forgot to typecheck!");
		}
		
		Value argValue =  exp.getArgument().evaluate(env);
		if (argValue instanceof StringConstant) {
			return new StringConstant(this.value + ((StringConstant) argValue).value);
		} else	if (argValue instanceof IntegerConstant) {
			return new StringConstant(this.value + ((IntegerConstant) argValue).getValue());
		} else
		{
//			shoudn't get here.
			throw new RuntimeException("forgot to typecheck!");
		}
	}

	@Override
	public void accept(CoreASTVisitor visitor) {
		visitor.visit(this);
	}

	private FileLocation location = FileLocation.UNKNOWN;
	public FileLocation getLocation() {
		return this.location;
	}
}
