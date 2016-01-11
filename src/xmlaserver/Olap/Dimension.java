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

public class Dimension extends CubeMetadata {
	
	private Type type;
	private Hierarchy defaultHierarchy;
	
	private List<Hierarchy> hierarchies;
	
	public Dimension(Cube cube, String caption, String uniqueName, Type type) throws Exception {
		super(cube, caption, uniqueName, null);
		
		setDescription(getCube().getCaption() + " Cube - " + caption + " Dimension");
		this.type = type;
		this.defaultHierarchy = null;
		hierarchies = new ArrayList<Hierarchy>();
	}
	
	public void addHierarchy(Hierarchy hierarchy) throws Exception {
		hierarchy.setOrdinal(hierarchies.size());
		hierarchies.add(hierarchy);
		if (defaultHierarchy == null)
			defaultHierarchy = hierarchy;
		registerChild(hierarchy.getUniqueName(), hierarchy);
	}
	
	public Hierarchy addHierarchy(String caption, String uniqueName, boolean hasAll) throws Exception {
		Hierarchy hierarchy = new Hierarchy(this, caption, uniqueName, hasAll);
		addHierarchy(hierarchy);
		return hierarchy;
	}
	
	public void addDimensionAtribute(String caption, String uniqueName, boolean hasAll, Attribute nameAttribute, Attribute idAttribute, Attribute orderAttribute) throws Exception {
		Hierarchy h = addHierarchy(caption, uniqueName, hasAll);
		h.addLevel(caption, uniqueName, Level.Type.MDLEVEL_TYPE_REGULAR, nameAttribute, idAttribute, orderAttribute);
	}
	
	public Type getType() {
		return type;
	}
	
	public Hierarchy getDefaultHierarchy() {
		return defaultHierarchy;
	}
	
	public List<Hierarchy> getHierarchies() {
		return hierarchies;
	}
	
	@Override
	public String getFullUniqueName() {
		//return getCube().getFullUniqueName() + ".[" + getUniqueName() + "]";
		return "[" + getUniqueName() + "]";
	}
	
    public enum Type {
    	MD_DIMTYPE_UNKNOWN(0),
    	MD_DIMTYPE_TIME(1),
    	MD_DIMTYPE_MEASURE(2),
    	MD_DIMTYPE_OTHER(3),
    	MD_DIMTYPE_QUANTITATIVE(5),
    	MD_DIMTYPE_ACCOUNTS(6),
    	MD_DIMTYPE_CUSTOMERS(7),
    	MD_DIMTYPE_PRODUCTS(8),
    	MD_DIMTYPE_SCENARIO(9),
    	MD_DIMTYPE_UTILIY(10),   // Note mis-spelling
    	MD_DIMTYPE_CURRENCY(11),
    	MD_DIMTYPE_RATES(12),
    	MD_DIMTYPE_CHANNEL(13),
    	MD_DIMTYPE_PROMOTION(14),
    	MD_DIMTYPE_ORGANIZATION(15),
    	MD_DIMTYPE_BILL_OF_MATERIALS(16),
    	MD_DIMTYPE_GEOGRAPHY(17);

        private final int value;

        private Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }	
}
