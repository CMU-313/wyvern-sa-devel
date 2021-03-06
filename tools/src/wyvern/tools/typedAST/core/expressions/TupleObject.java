package wyvern.tools.typedAST.core.expressions;

import wyvern.tools.errors.FileLocation;
import wyvern.tools.typedAST.abs.AbstractValue;
import wyvern.tools.typedAST.abs.CachingTypedAST;
import wyvern.tools.typedAST.core.Invocation;
import wyvern.tools.typedAST.core.values.TupleValue;
import wyvern.tools.typedAST.interfaces.CoreAST;
import wyvern.tools.typedAST.interfaces.CoreASTVisitor;
import wyvern.tools.typedAST.interfaces.InvokableValue;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.typedAST.interfaces.Value;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.types.extensions.Tuple;
import wyvern.tools.util.TreeWriter;

public class TupleObject extends CachingTypedAST implements CoreAST {
	private TypedAST[] objects;
	
	public TupleObject(TypedAST[] objects) {
		this.objects = objects;
	}
	
	public TupleObject(TypedAST first, TypedAST rest, FileLocation commaLine) {
		if (rest instanceof TupleObject) {
			objects = new TypedAST[((TupleObject) rest).objects.length + 1];
			objects[0] = first;
			for (int i = 1; i < ((TupleObject) rest).objects.length + 1; i++) {
				objects[i] = ((TupleObject) rest).objects[i-1];
			}
		} else {
			this.objects = new TypedAST[] { first, rest };
		}
		this.location = commaLine;
	}

	@Override
	public void writeArgsToTree(TreeWriter writer) {
		writer.writeArgs(objects);	
	}
	
	public TypedAST getObject(int index) {
		return objects[index];
	}

	@Override
	public void accept(CoreASTVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Value evaluate(Environment env) {
		Value[] evaluatedResults = new Value[objects.length];
		for (int i = 0; i < objects.length; i++) {
			evaluatedResults[i] = objects[i].evaluate(env);
		}
		return new TupleValue((Tuple)this.getType(), evaluatedResults);
	}

	@Override
	protected Type doTypecheck(Environment env) {
		Type[] newTypes = new Type[objects.length];
		for (int i = 0; i < objects.length; i++) {
			newTypes[i] = objects[i].typecheck(env);
		}
		return new Tuple(newTypes);
	}

	public TypedAST[] getObjects() {
		return objects;
	}

	private FileLocation location;
	public FileLocation getLocation() {
		return this.location;
	}
}
