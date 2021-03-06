package wyvern.tools.types.extensions;

import java.util.HashSet;
import java.util.List;

import wyvern.tools.typedAST.core.binding.NameBinding;
import wyvern.tools.types.AbstractTypeImpl;
import wyvern.tools.types.SubtypeRelation;
import wyvern.tools.types.Type;
import wyvern.tools.util.TreeWriter;

public class Tuple extends AbstractTypeImpl {
	private Type[] types;
	
	public Tuple(Type[] types) {
		this.types = types;
	}
	
	public Tuple(List<NameBinding> bindings) {
		this.types = new Type[bindings.size()];
		for (int i = 0; i < bindings.size(); i++) {
			this.types[i] = bindings.get(i).getType();
		}
	}

	public Type[] getTypes() {
		return types;
	}

    public Type getFirst() {
        return types[0];
    }

    public boolean isEmpty() {
        return types.length > 0;
    }

    public Tuple getRest() {
        Type[] newT = new Type[types.length-1];
        for (int i = 1; i < types.length; i++)
            newT[i-1] = types[i];
        return new Tuple(newT);
    }
	
	@Override
	public void writeArgsToTree(TreeWriter writer) {
		writer.writeArgs(types);	
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(types.length + 2);
		
		if (types.length > 1) {
			if (!types[0].isSimple())
				builder.append('(');
			builder.append(types[0].toString());
			if (!types[0].isSimple())
				builder.append(')');
		}
		for (int i = 1; i < types.length; i++) {
			builder.append('*');
			if (!types[0].isSimple())
				builder.append('(');
			builder.append(types[i].toString());
			if (!types[0].isSimple())
				builder.append(')');
		}
		return builder.toString();
	}
	
	@Override
	public boolean equals(Object otherT) {
		if (!(otherT instanceof Tuple))
			return false;
		
		if (((Tuple)otherT).types.length != types.length)
			return false;
		
		for (int i = 0; i < types.length; i++) {
			if (!(((Tuple)otherT).types[i].equals(types[i])))
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = 23;
		for (Type type : types)
			hash = hash*37 + type.hashCode();
		
		return hash;
	}	

	@Override
	public boolean subtype(Type other, HashSet<SubtypeRelation> subtypes) {
		// FIXME: Implement S-RcdWidth, S-RcdDepth, and S-RcdPerm I suppose. (Ben: This is factually wrong)
        if (other == this)
            return true;

        if (!(other instanceof Tuple))
            return false;

        Tuple otherTuple = (Tuple)other;

        //n+k = types.length
        //n = otherTuple.types.length
        if (types.length != otherTuple.types.length) // n+k != n
            return false;
        //=>k=0=>n+k=n

        boolean sat = true;
        for (int i = 0; i < otherTuple.types.length && sat; i++) {
            Type Si = types[i];
            Type Ti = otherTuple.types[i];
            if (!Si.subtype(Ti)) // S_i <: T_i
                sat = false;
        }
        return sat;
	}
	
	@Override
	public boolean isSimple() {
		return false;
	}

}