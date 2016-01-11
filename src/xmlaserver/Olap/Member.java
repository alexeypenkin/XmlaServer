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

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class Member extends CubeMetadata implements Comparable<Member>  {
	static Logger log = Logger.getLogger(Member.class.getName());
	
	private Member parentMember;
	private Level level;
	private Type type;
	private List<Member> childMembers;
	
	public Member(Level level, String caption, String uniqueName, Type type, Member parentMember) throws Exception {
		super(level.getCube(), caption, uniqueName, level);

		this.parentMember = parentMember;
		this.level = level;
		this.type = type;
		this.childMembers = new ArrayList<Member>();
	}
	
	public boolean isInitialized() {
		return getCaption() != null;
	}
	
	public Dimension getDimension() {
		return level.getDimension();
	}

/*	
	public String getWhereClause() {
		if (type == Type.MDMEMBER_TYPE_ALL)
			return "";
		
		if (parentMember != null) {
			String res = parentMember.getWhereClause();
			if (res != null && !res.isEmpty())
				return res + " AND " + level.getIdAttribute().getWhereClause(getUniqueName());
			else
				return level.getIdAttribute().getWhereClause(getUniqueName());
		}
		return level.getIdAttribute().getWhereClause(getUniqueName());
	}
*/
	
	public Level getLevel() {
		return level;
	}
	
	public Hierarchy getHierarchy() {
		return level.getHierarchy();
	}
	
	public Type getType() {
		return type;
	}
	
	public Member getParentMember() {
		return parentMember;
	}
	
	public void addChild(Member childMember) throws Exception {
		childMembers.add(childMember);
		registerChild(childMember.getUniqueName(), childMember);
	}
	
	public List<Member> getChildMembers() {
		return childMembers;
	}
	
	public int getChildrenCardinality() {
		return childMembers.size();
	}
	
	@Override
	public CubeMetadata resolveMultipartName(List<String> names) throws Exception {
		CubeMetadata cm = super.resolveMultipartName(names);
		
		if (cm != null)
			return cm;
		
		if (names == null)
			return null;
		if (names.isEmpty())
			return null;
		
		if (getLevel().getNextLevel() == null)
			return null;
		
		// Members may not be loaded yet, so let's create an un-initialized member (name = null)
		Member m = new Member(getLevel().getNextLevel(), null, names.get(0), Member.Type.MDMEMBER_TYPE_REGULAR, this);
		log.debug("resolveMultipartName created un-initialized member " + m);
	
		if (names.size() == 1)
			return m;
		else
			return m.resolveMultipartName(names.subList(1, names.size()));
	}
	
	@Override
	public String getFullUniqueName() {
		if (parentMember == null)
			return level.getHierarchy().getFullUniqueName() + ".[" + getUniqueName() + "]";
		else
			if (parentMember.getType().equals(Type.MDMEMBER_TYPE_ALL))
				return level.getHierarchy().getFullUniqueName() + ".[" + getUniqueName() + "]";
			else
				return parentMember.getFullUniqueName() + ".[" + getUniqueName() + "]";
	}
	
	@Override
	public int compareTo(Member m) {
		//  0 - this is equals to argument
		//  1 - this is greater than the argument
		// -1 - this is less than the argument
		
		//log.debug("compareTo this=" + toString() + ", m=" + m);
		if (equals(m))
			return 0;
		
		if (getHierarchy().equals(m.getHierarchy())) {
			int levelCmp = getLevel().compareTo(m.getLevel());
			if (levelCmp == 0) 
				if (getParentMember() != null && m.getParentMember() != null) {
					int parentCmp = getParentMember().compareTo(m.getParentMember());
					if (parentCmp == 0) {
						// TODO: Compare by "order"
						int nameCmp = getCaption().compareTo(m.getCaption());
						if (nameCmp == 0)
							return getUniqueName().compareTo(m.getUniqueName());
						else
							return nameCmp;
					} else
						return parentCmp;
				} else {
					// Same level and not parents
					// TODO: Compare by "order"
					int nameCmp = getCaption().compareTo(m.getCaption());
					if (nameCmp == 0)
						return getUniqueName().compareTo(m.getUniqueName());
					else
						return nameCmp;
				}
			else {
				if (levelCmp > 0 && getParentMember() != null) {
					int cmp = getParentMember().compareTo(m);
					if (cmp >= 0)
						return 1;
					else
						return -1;
				} else if (levelCmp < 0 && m.getParentMember() != null) {
					int cmp = compareTo(m.getParentMember());
					if (cmp > 0)
						return 1;
					else
						return -1;
				} else
					log.warn("Member.compareTo Level.getOrdinal inconsistent with getParentMember() this=" + toString() + ", m=" + m);
			}
		}
		log.warn("compareTo fall back to comparison by getFullUniqueName");	
		return getFullUniqueName().compareTo(m.getFullUniqueName());
	}
	
	// https://github.com/olap4j/olap4j/blob/master/src/org/olap4j/metadata/Member.java
	// https://technet.microsoft.com/en-us/library/ms126046(v=sql.105).aspx
    public enum Type {
    	MDMEMBER_TYPE_UNKNOWN(0),
    	MDMEMBER_TYPE_REGULAR(1),
    	MDMEMBER_TYPE_ALL(2),
        MMDMEMBER_TYPE_EASURE(3),
        MDMEMBER_TYPE_FORMULA(4),
        MDMEMBER_TYPE_NULL(5);

	    private final int value;

	    private Type(int value) {
	    	this.value = value;
	    }
	    
	    public int getValue() {
	    	return value;
	    }	    
    }
}
