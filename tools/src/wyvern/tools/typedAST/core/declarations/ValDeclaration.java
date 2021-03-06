package wyvern.tools.typedAST.core.declarations;

import wyvern.tools.errors.ErrorMessage;
import wyvern.tools.errors.FileLocation;
import wyvern.tools.errors.ToolError;
import wyvern.tools.typedAST.abs.Declaration;
import wyvern.tools.typedAST.core.binding.NameBinding;
import wyvern.tools.typedAST.core.binding.NameBindingImpl;
import wyvern.tools.typedAST.core.binding.ValueBinding;
import wyvern.tools.typedAST.interfaces.CoreAST;
import wyvern.tools.typedAST.interfaces.CoreASTVisitor;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.typedAST.interfaces.Value;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.util.TreeWriter;

public class ValDeclaration extends Declaration implements CoreAST {
	TypedAST definition;
	Type definitionType;
	NameBinding binding;

	private boolean isClass;
	public boolean isClass() {
		return isClass;
	}
	
	public ValDeclaration(String name, TypedAST definition, FileLocation location) {
		this.definition=definition;
		binding = new NameBindingImpl(name, null);
		this.location = location;
	}
	
	public ValDeclaration(String name, Type type, TypedAST definition, FileLocation location) {
		this.definition=definition;
		binding = new NameBindingImpl(name, type);
		this.location = location;
	}

	@Override
	public void writeArgsToTree(TreeWriter writer) {
		writer.writeArgs(binding.getName(), definition);
	}

	@Override
	protected Type doTypecheck(Environment env) {
		if (this.definition != null)
			this.definitionType = this.definition.typecheck(env);
		if (binding.getType() == null) {
			this.binding = new NameBindingImpl(binding.getName(), definitionType);
		} else if (this.definitionType != null && !this.definitionType.subtype(binding.getType())){
			ToolError.reportError(ErrorMessage.NOT_SUBTYPE, this.definitionType.toString(), binding.getType().toString(), this);
		}
		
		return binding.getType();
	}

	@Override
	public void accept(CoreASTVisitor visitor) {
		visitor.visit(this);
	}
	
	public NameBinding getBinding() {
		return binding;
	}

	@Override
	public Type getType() {
		return binding.getType();
	}

	@Override
	public String getName() {
		return binding.getName();
	}
	
	public TypedAST getDefinition() {
		return definition;
	}

	@Override
	protected Environment doExtend(Environment old) {
		return old.extend(binding);
	}

	@Override
	public Environment extendWithValue(Environment old) {
		Environment newEnv = old.extend(new ValueBinding(binding.getName(), binding.getType()));
		return newEnv;
		//Environment newEnv = old.extend(new ValueBinding(binding.getName(), defValue));
	}

	@Override
	public void evalDecl(Environment evalEnv, Environment declEnv) {
		if (declEnv.getValue(binding.getName()) != null)
			return;
			
		Value defValue = null;
		if (definition != null)
			defValue = definition.evaluate(evalEnv);
		ValueBinding vb = (ValueBinding) declEnv.lookup(binding.getName());
		vb.setValue(defValue);
	}

	private FileLocation location = FileLocation.UNKNOWN;
	public FileLocation getLocation() {
		return this.location; //TODO
	}
}
