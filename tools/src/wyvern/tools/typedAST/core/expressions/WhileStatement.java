package wyvern.tools.typedAST.core.expressions;

import wyvern.tools.errors.ErrorMessage;
import wyvern.tools.errors.FileLocation;
import wyvern.tools.errors.ToolError;
import wyvern.tools.parsing.LineParser;
import wyvern.tools.parsing.LineSequenceParser;
import wyvern.tools.typedAST.core.values.BooleanConstant;
import wyvern.tools.typedAST.core.values.UnitVal;
import wyvern.tools.typedAST.interfaces.CoreAST;
import wyvern.tools.typedAST.interfaces.CoreASTVisitor;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.typedAST.interfaces.Value;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.types.extensions.Bool;
import wyvern.tools.types.extensions.Unit;
import wyvern.tools.util.TreeWriter;

public class WhileStatement implements TypedAST, CoreAST {

	private TypedAST conditional;
	private TypedAST body;
	private FileLocation location;

	public WhileStatement(TypedAST conditional, TypedAST body,
			FileLocation location) {
		this.conditional = conditional;
		this.body = body;
		this.location = location;
	}

	@Override
	public void writeArgsToTree(TreeWriter writer) {
	}

	@Override
	public FileLocation getLocation() {
		return location;
	}

	@Override
	public Type getType() {
		return Unit.getInstance();
	}

	@Override
	public Type typecheck(Environment env) {
		if (!(conditional.typecheck(env) instanceof Bool))
			ToolError.reportError(ErrorMessage.TYPE_CANNOT_BE_APPLIED, conditional);
		
		body.typecheck(env);
		return Unit.getInstance();	
	}
	
	private boolean evaluateConditional(Environment env) {
		return ((BooleanConstant)conditional.evaluate(env)).getValue();
	}

	@Override
	public Value evaluate(Environment env) {
		while (evaluateConditional(env)) {
			body.evaluate(env);
		}
		return UnitVal.getInstance(this.getLocation());
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
	public void accept(CoreASTVisitor visitor) {
		visitor.visit(this);
	}

	public TypedAST getConditional() {
		return conditional;
	}
	
	public TypedAST getBody() {
		return body;
	}
}
