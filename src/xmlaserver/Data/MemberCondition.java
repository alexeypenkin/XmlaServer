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

import org.apache.log4j.Logger;
import xmlaserver.Olap.Member;

public class MemberCondition extends Condition {
	static Logger log = Logger.getLogger(MemberCondition.class.getName());
	
	private Member member;
	
	public MemberCondition(Member member) {
		this.member = member;
	}
	
	public Member getMember() {
		return member;
	}

	@Override
	public String toString() {
		return member.toString();
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	  
	    final MemberCondition other = (MemberCondition)obj;
	    return member.equals(other.getMember());
	}
	
    @Override
    public int hashCode() {
        return member.hashCode();
    }
}
