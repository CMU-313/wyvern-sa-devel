package wyvern.tools.typedAST.core.binding;

import wyvern.tools.types.Type;

public class TypeBinding extends AbstractBinding {
	public TypeBinding(String name, Type type) {
		super(name, type);
	}

	public Type getUse() {
		return getType(); 
	}
}
