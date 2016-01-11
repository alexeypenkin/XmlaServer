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

public class Hierarchy extends CubeMetadata {
	static Logger log = Logger.getLogger(Hierarchy.class.getName());
	
	private Dimension dimension;
	private Member defaultMember = null;
	private Level levelAll;
	private Member memberAll;
	
	private List<Level> levels;
	
	// hasAll="true" allMemberName="All Dates"
	// TODO: add allMemberName or move "hasAll" logic outside
	public Hierarchy(Dimension dimension, String caption, String uniqueName, boolean hasAll) throws Exception {
		
		super(dimension.getCube(), caption, uniqueName, dimension);
		setDescription(getCube().getCaption() + " Cube - " + caption + " Hierarchy");
		this.dimension = dimension;
		levels = new ArrayList<Level>();
		
		if (hasAll) {
			levelAll = addLevel("(All)", "(All)", Level.Type.MDLEVEL_TYPE_ALL, null, null, null);
			memberAll = levelAll.addMember("Member All", "Member All", Member.Type.MDMEMBER_TYPE_ALL, null);
			defaultMember = memberAll; 
		}
	}
	
	public Dimension getDimension() {
		return dimension;
	}
	
	public void setDefaultMember(Member member) {
		defaultMember = member;
	}
	
	public List<Level> getLevels() {
		return levels;
	}
	
	public Level getLevel(int index) {
		return levels.get(index);
	}
	
	public void addLevel(Level level) throws Exception {
		level.setOrdinal(levels.size());
		if (levels.size() > 0) {
			Level lastLevel = levels.get(levels.size() - 1);
			lastLevel.setNextLevel(level);
			level.setPreviousLevel(lastLevel);
		}
		levels.add(level);
		registerChild(level.getUniqueName(), level);
	}
	
	public Level addLevel(String caption, String uniqueName, Level.Type type, Attribute nameAttribute, Attribute idAttribute, Attribute orderAttribute) throws Exception {
		Level level = new Level(this, caption, uniqueName, type, nameAttribute, idAttribute, orderAttribute);
		addLevel(level);
		return level;
	}
	
	public Member getDefaultMember() {
		return defaultMember;
	}
	
	public Level getLevelAll() {
		return levelAll;
	}
	
	public Member getMemberAll() {
		return memberAll;
	}
	
	@Override
	public String getFullUniqueName() {
		return getDimension().getFullUniqueName() + ".[" + getUniqueName() + "]";
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
		
		// Members may not be loaded yet, so let's create an un-initialized member (name = null)
		// as we have enough information with identifiers for all levels
		// [Region].[Region].[2].[21].[211]
		if (levels.size() == 0)
			return null;
		
		Level l = getLevel(0);
		if (l.getType() == Level.Type.MDLEVEL_TYPE_ALL) 
			l = l.getNextLevel();
		
		if (l == null)
			return null;
		
		Member m = new Member(l, null, names.get(0), Member.Type.MDMEMBER_TYPE_REGULAR, null);
		log.debug("resolveMultipartName created un-initialized member " + m);
	
		if (names.size() == 1)
			return m;
		else
			return m.resolveMultipartName(names.subList(1, names.size()));
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	  
	    final Hierarchy other = (Hierarchy) obj;
	    return getFullUniqueName().equals(other.getFullUniqueName());
	}
	
    @Override
    public int hashCode() {
        return getFullUniqueName().hashCode();
    }
}
