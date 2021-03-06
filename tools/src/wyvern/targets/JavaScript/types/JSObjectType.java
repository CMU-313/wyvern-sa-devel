package wyvern.targets.JavaScript.types;

import static wyvern.tools.errors.ErrorMessage.OPERATOR_DOES_NOT_APPLY;
import static wyvern.tools.errors.ToolError.reportError;
import wyvern.tools.typedAST.abs.Declaration;
import wyvern.tools.typedAST.core.Application;
import wyvern.tools.typedAST.core.Invocation;
import wyvern.tools.typedAST.core.declarations.ClassDeclaration;
import wyvern.tools.types.AbstractTypeImpl;
import wyvern.tools.types.ApplyableType;
import wyvern.tools.types.Environment;
import wyvern.tools.types.OperatableType;
import wyvern.tools.types.Type;
import wyvern.tools.types.extensions.Arrow;
import wyvern.tools.types.extensions.Bool;
import wyvern.tools.types.extensions.Int;
import wyvern.tools.types.extensions.Str;
import wyvern.tools.types.extensions.Unit;
import wyvern.tools.util.TreeWriter;

public class JSObjectType extends AbstractTypeImpl implements OperatableType, ApplyableType {
	public static JSObjectType getInstance() {
		if (instance == null)
			instance = new JSObjectType();
		return instance;
	}
	
	private static JSObjectType instance = null;
	private JSObjectType() {
	}
	
	@Override
	public void writeArgsToTree(TreeWriter writer) {
		// nothing to write		
	}
	
	@Override
	public String toString() {
		return "JSObjectType";
	}

	@Override
	public Type checkApplication(Application application, Environment env) {
		return this;
	}

	@Override
	public Type checkOperator(Invocation opExp, Environment env) {
		return this;
	}
}
