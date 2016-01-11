/*******************************************************************************
* Copyright (c) 2015-2016 Alexey Penkin.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Alexey Penkin - initial API and implementation
*******************************************************************************/

package xmlaserver.Data;

public class LogicalCondition extends Condition {
	
	private Type type;
	private Condition condition1;
	private Condition condition2;
	
	public LogicalCondition(Type type, Condition condition1, Condition condition2) {
		this.type = type;
		this.condition1 = condition1;
		this.condition2 = condition2;
	}
	
	public Type getType() {
		return type;
	}
	
	public Condition getCondition1() {
		return condition1;
	}
	
	public Condition getCondition2() {
		return condition2;
	}
	
	@Override
	public String toString() {
		return type + "(" + condition1 + "," + condition2 + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	  
	    final LogicalCondition other = (LogicalCondition)obj;
	    return type.equals(other.getType()) &&
	    	condition1.equals(other.getCondition1()) &&
	    	condition2.equals(other.getCondition2());
	}
	
    @Override
    public int hashCode() {
    	int h = type.hashCode();
    	h = h * 31 + condition1.hashCode();
    	h = h * 31 + condition2.hashCode();
        return h; 
    }
	
	public enum Type {
		AND,
		OR
	}

}
