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

import java.util.ArrayList;

import xmlaserver.Olap.Member;

public class AxisTuple implements Comparable<AxisTuple> {
	
	private ArrayList<Member> members;
	
	public AxisTuple(int size) {
		members = new ArrayList<Member>(size);
		for (int i=0; i<size; i++)
			members.add(null);
	}
	
	public void setMember(int index, Member member) {
		members.set(index, member);
	}
	
	public ArrayList<Member> getMembers() {
		return members;
	}

	@Override
	public int compareTo(AxisTuple o) {
		ArrayList<Member> otherMembers = o.getMembers();
		if (members.size() == otherMembers.size()) {
			for (int i=0; i<members.size(); i++) {
				int cmp = members.get(i).compareTo(otherMembers.get(i));
				if (cmp != 0)
					return cmp;
			}
			return 0;
		}
		return 1;  // something goes wrong here, can't really compare
	}

}
