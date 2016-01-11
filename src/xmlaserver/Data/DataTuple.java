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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import xmlaserver.Olap.Member;


public class DataTuple {
	
	private Set<Member> members;
	
	public DataTuple() {
		members = new HashSet<Member>();
	}
	
	public DataTuple(Collection<Member> members) {
		this.members = new HashSet<Member>(members);
	}
	
	public DataTuple(DataTuple dt) {
		members = new HashSet<Member>(dt.getMembers());
	}
	
	public void addMember(Member member) {
		members.add(member);
	}
	
	public void addAll(Collection<Member> members) {
		this.members.addAll(members);
	}
	
	public Set<Member> getMembers() {
		return members;
	}
	
	@Override
	public String toString() {
		return members.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	  
	    final DataTuple other = (DataTuple) obj;
	    return members.equals(other.getMembers());
	}
	
    @Override
    public int hashCode() {
        return members.hashCode();
    }	
}
