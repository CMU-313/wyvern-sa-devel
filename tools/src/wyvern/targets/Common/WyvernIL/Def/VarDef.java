package wyvern.targets.Common.WyvernIL.Def;

import wyvern.targets.Common.WyvernIL.Expr.Expression;
import wyvern.targets.Common.WyvernIL.Imm.Operand;
import wyvern.targets.Common.WyvernIL.Stmt.Pure;
import wyvern.targets.Common.WyvernIL.visitor.DefVisitor;

public class VarDef implements Definition {

	private String name;
	private Operand exn;

	public VarDef(String name, Operand exn) {
		this.name = name;
		this.exn = exn;
	}

	@Override
	public <R> R accept(DefVisitor<R> visitor) {
		return visitor.visit(this);
	}

	public Operand getExn() {
		return exn;
	}

	public String getName() {
		return name;
	}
}