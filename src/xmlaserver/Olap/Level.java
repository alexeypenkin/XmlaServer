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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class Level extends CubeMetadata implements Comparable<Level> {
	static Logger log = Logger.getLogger(Level.class.getName());
	
	private Hierarchy hierarchy;
	private Level previousLevel = null;
	private Level nextLevel = null;
	private Type type;
	
	private Attribute nameAttribute; 
	private Attribute idAttribute; 
	private Attribute orderAttribute;

	private List<Member> members;
	
	public Level(Hierarchy hierarchy, String caption, String uniqueName, Type type, Attribute nameAttribute, Attribute idAttribute, Attribute orderAttribute) throws Exception {	
		super(hierarchy.getCube(), caption, uniqueName, hierarchy);
		
		setDescription(getCube().getCaption() + " Cube - " + hierarchy.getCaption() + " Hierarchy - " + caption + " Level");
		this.hierarchy = hierarchy;
		//this.upperLevel = upperLevel;
		this.type = type;
		this.nameAttribute = nameAttribute;
		this.idAttribute = idAttribute;
		this.orderAttribute = orderAttribute;
		
		members = new ArrayList<Member>();
	}
	
	public Attribute getIdAttribute() {
		return idAttribute;
	}
	
	public Attribute getNameAttribute() {
		return nameAttribute;
	}
	
	public Attribute getOrderAttribute() {
		return orderAttribute;
	}
	
	public Dimension getDimension() {
		return hierarchy.getDimension();
	}
	
	public Set<Attribute> getAttributes() {
		Set<Attribute> res;
		if (previousLevel != null && previousLevel.getType() != Type.MDLEVEL_TYPE_ALL)
			res = previousLevel.getAttributes();
		else
			res = new HashSet<Attribute>();
		
		if (nameAttribute != null)
			res.add(nameAttribute);
		if (idAttribute != null)
			res.add(idAttribute);
		if (orderAttribute != null)
			res.add(orderAttribute);
		
		return res;
	}
	
	public void setPreviousLevel(Level previousLevel) {
		this.previousLevel = previousLevel;
	}
	
	public Level getPreviousLevel() {
		return previousLevel;
	}
	
	public void setNextLevel(Level nextLevel) {
		this.nextLevel = nextLevel;
	}
	
	public Level getNextLevel() {
		return nextLevel;
	}
	
	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	public Type getType() {
		return type;
	}
	
	public void addMember(Member member) throws Exception {
		member.setOrdinal(members.size());
		members.add(member);
		registerChild(member.getUniqueName(), member);
		
		// Register names of the top level with Hierarchy so name resolver works fine for
		// [Region].[Region].[1].[11].[112]
		if (getOrdinal() == 0 || previousLevel.getType() == Level.Type.MDLEVEL_TYPE_ALL ) 
			hierarchy.registerChild(member.getUniqueName(), member);
	}
	
	public List<Member> getMembers() {
		return members;
	}
	
	public Member addMember(String name, String uniqueName, Member.Type type, Member upperMember) throws Exception {
		Member member = new Member(this, name, uniqueName, type, upperMember);
		addMember(member);
		if (upperMember != null)
			upperMember.addChild(member);

		return member;
	}
	
	@Override
	public String getFullUniqueName() {
		return getHierarchy().getFullUniqueName() + ".[" + getUniqueName() + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	  
	    final Level other = (Level) obj;
	    return getFullUniqueName().equals(other.getFullUniqueName());
	}
	
    @Override
    public int hashCode() {
        return getFullUniqueName().hashCode();
    }
    
	@Override
	public int compareTo(Level l) {
		//  0 - this is equals to argument
		//  1 - this is greater than the argument
		// -1 - this is less than the argument
		
		//log.debug("compareTo this=" + toString() + ", l=" + l);
		if (getHierarchy().equals(l.getHierarchy())) {
			if (equals(l))
				return 0; 
			else {
				if (getOrdinal() > l.getOrdinal())
					return 1;
				else if (getOrdinal() < l.getOrdinal())
					return -1;
				else
					log.warn("Level.compareTo compares 2 levels from the same Hierarchy and the same Ordinal but not equals this=" + this + ", l=" + l);
			}
		} else
			log.warn("Level.compareTo compares 2 levels from different hierarchies this=" + this + ", l=" + l);
		
		return getFullUniqueName().compareTo(l.getFullUniqueName());
	}
	
	// https://github.com/olap4j/olap4j/blob/master/src/org/olap4j/metadata/Level.java
	// https://technet.microsoft.com/en-us/library/ms126038(v=sql.105).aspx
	public enum Type  {

		MDLEVEL_TYPE_REGULAR(0x0000),
		MDLEVEL_TYPE_ALL(0x0001),
		MDLEVEL_TYPE_NULL(-1),
		MDLEVEL_TYPE_TIME_YEARS(0x0014),
		MDLEVEL_TYPE_TIME_HALF_YEAR(0x0024),
		MDLEVEL_TYPE_TIME_QUARTERS(0x0044),
		MDLEVEL_TYPE_TIME_MONTHS(0x0084),
		MDLEVEL_TYPE_TIME_WEEKS(0x0104),
		MDLEVEL_TYPE_TIME_DAYS(0x0204),
		MDLEVEL_TYPE_TIME_HOURS(0x0304),
		MDLEVEL_TYPE_TIME_MINUTES(0x0404),
		MDLEVEL_TYPE_TIME_SECONDS(0x0804),
		MDLEVEL_TYPE_TIME_UNDEFINED(0x1004),
		MDLEVEL_TYPE_GEO_CONTINENT(0x2001),
		MDLEVEL_TYPE_GEO_REGION(0x2002),
		MDLEVEL_TYPE_GEO_COUNTRY(0x2003),
		MDLEVEL_TYPE_GEO_STATE_OR_PROVINCE(0x2004),
		MDLEVEL_TYPE_GEO_COUNTY(0x2005),
		MDLEVEL_TYPE_GEO_CITY(0x2006),
		MDLEVEL_TYPE_GEO_POSTALCODE(0x2007),
		MDLEVEL_TYPE_GEO_POINT(0x2008),
		MDLEVEL_TYPE_ORG_UNIT(0x1011),
		MDLEVEL_TYPE_BOM_RESOURCE(0x1012),
		MDLEVEL_TYPE_QUANTITATIVE(0x1013),
		MDLEVEL_TYPE_ACCOUNT(0x1014),
		MDLEVEL_TYPE_CUSTOMER(0x1021),
		MDLEVEL_TYPE_CUSTOMER_GROUP(0x1022),
		MDLEVEL_TYPE_CUSTOMER_HOUSEHOLD(0x1023),
		MDLEVEL_TYPE_PRODUCT(0x1031),
		MDLEVEL_TYPE_PRODUCT_GROUP(0x1032),
		MDLEVEL_TYPE_SCENARIO(0x1015),
		MDLEVEL_TYPE_UTILITY(0x1016),
		MDLEVEL_TYPE_PERSON(0x1041),
		MDLEVEL_TYPE_COMPANY(0x1042),
		MDLEVEL_TYPE_CURRENCY_SOURCE(0x1051),
		MDLEVEL_TYPE_CURRENCY_DESTINATION(0x1052),
		MDLEVEL_TYPE_CHANNEL(0x1061),
		MDLEVEL_TYPE_REPRESENTATIVE(0x1062),
		MDLEVEL_TYPE_PROMOTION(0x1071); 
		 
	    private final int value;

	    private Type(int value) {
	    	this.value = value;
	    }

	    public int getValue() {
	    	return value;
	    }

		public boolean isTime() {
			switch (this) {
				case MDLEVEL_TYPE_TIME_YEARS:
	            case MDLEVEL_TYPE_TIME_HALF_YEAR:
	            case MDLEVEL_TYPE_TIME_QUARTERS:
	            case MDLEVEL_TYPE_TIME_MONTHS:
	            case MDLEVEL_TYPE_TIME_WEEKS:
	            case MDLEVEL_TYPE_TIME_DAYS:
	            case MDLEVEL_TYPE_TIME_HOURS:
	            case MDLEVEL_TYPE_TIME_MINUTES:
	            case MDLEVEL_TYPE_TIME_SECONDS:
	            case MDLEVEL_TYPE_TIME_UNDEFINED:
	                return true;
	            default:
	                return false;
			}
	    }
	}
}
