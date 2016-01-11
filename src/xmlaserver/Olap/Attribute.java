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

package xmlaserver.Olap;

public class Attribute {
	
	private String name;
	
	public Attribute(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	  
	    final Attribute other = (Attribute)obj;
	    return name.equals(other.getName());
	}
	
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
