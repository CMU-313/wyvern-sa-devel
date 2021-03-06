package wyvern.tools.rawAST;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Sequence implements RawAST, Iterable<RawAST> {
	public final List<RawAST> children;
	
	public Sequence(List<RawAST> children) {
		this.children = children;
	}
	
	public Sequence() {
		this(new ArrayList<RawAST>());
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Sequence))
			return false;
		Sequence otherData = (Sequence) other; 
		return otherData.children.equals(children) && getOpenChar().equals(otherData.getOpenChar());
	}
	
	@Override
	public int hashCode() {
		return children.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getOpenChar());
		String beforeChar = "";
		
		for(RawAST n : children) {
			buf.append(beforeChar);
			buf.append(n.toString());
			beforeChar = " ";
		}

		buf.append(getCloseChar());
		
		return buf.toString();
	}


	@Override
	public Iterator<RawAST> iterator() {
		return children.iterator();
	}
	
	abstract protected String getOpenChar();
	abstract protected String getCloseChar();
}
