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
import java.util.Arrays;
import java.util.List;

import xmlaserver.Olap.Dimension.Type;


public class Cube extends CubeMetadata {

	// Catalog
	//   Schema
	//     Cube
	//       Dimension
	//         Hierarchy
	//           Level
	//             Member
	// Property

	private String catalogName;
	private String schemaName;
	private boolean drillthroughEnabled;
	
	private List<Dimension> dimensions;
	private List<Measure> measures;
	
	private Dimension dimensionMeasures;
	private Hierarchy hierarchyMeasures;
	private Level levelMeasures;
	
	public static List<Cube> cubes = new ArrayList<Cube>();
	
	public Cube(String catalogName, String schemaName, String caption, String uniqueName) throws Exception {
		
		super(null, caption, uniqueName, null);
		setDescription(caption + " Cube");
		
		this.catalogName = catalogName;
		this.schemaName = schemaName;
		this.drillthroughEnabled = true;
		
		dimensions = new ArrayList<Dimension>();
		measures = new ArrayList<Measure>();
		
		dimensionMeasures = addDimension("Measures", "Measures", Dimension.Type.MD_DIMTYPE_MEASURE);
		hierarchyMeasures = dimensionMeasures.addHierarchy("Measures", "Measures", false);
		levelMeasures = hierarchyMeasures.addLevel("Measures", "Measures", Level.Type.MDLEVEL_TYPE_REGULAR, null, null, null);
		
		registerChild(caption, this);
		cubes.add(this);
	}
	
	public void setDrillthroughEnabled(boolean drillthroughEnabled) {
		this.drillthroughEnabled = drillthroughEnabled;
	}
	
	public void addDimension(Dimension dimension) throws Exception {
		dimension.setOrdinal(dimensions.size());
		dimensions.add(dimension);
		registerChild(dimension.getUniqueName(), dimension);
	}
	
	public Dimension addDimension(String caption, String uniqueName, Dimension.Type type) throws Exception {
		Dimension dimension = new Dimension(this, caption, uniqueName, type);
		addDimension(dimension);
		return dimension;
	}
	
	public void addMeasure(Measure measure) throws Exception {
		measure.setOrdinal(measures.size());
		measures.add(measure);
		levelMeasures.addMember(measure);
		//registerName(measure.getFullUniqueName(), measure);
	}
	
	public Measure addMeasure(String caption, String uniqueName, String defaultFormatString, Attribute attribute) throws Exception {
		Measure measure = new Measure(levelMeasures, caption, uniqueName, defaultFormatString, attribute);
		addMeasure(measure);
		return measure;
	}
	
	public List<Measure> getMeasures() {
		return measures;
	}
	
	public String getCatalogName() {
		return catalogName;
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	
	public boolean getDrillthroughEnabled() {
		return drillthroughEnabled;
	}
	
	public List<Dimension> getDimensions() {
		return dimensions;
	}
	
	@Override
	public String getFullUniqueName() {
		return "[" + getUniqueName() + "]";
	}
	
}
