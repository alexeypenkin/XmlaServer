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

package xmlaserver.XmlaServer;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import xmlaserver.Data.AnalysisService;
import xmlaserver.Data.AtomicRequest;
import xmlaserver.Data.Axis;
import xmlaserver.Data.AxisTuple;
import xmlaserver.Data.DataRequest;
import xmlaserver.Data.DataRequestBuilder;
import xmlaserver.Data.DataTuple;
import xmlaserver.Data.MemberCondition;
import xmlaserver.Mdx.JavaCC.MdxParser;
import xmlaserver.Mdx.Language.MdxSelectDefinition;
import xmlaserver.Olap.Cube;
import xmlaserver.Olap.CubeMetadata;
import xmlaserver.Olap.Dimension;
import xmlaserver.Olap.Hierarchy;
import xmlaserver.Olap.Level;
import xmlaserver.Olap.Measure;
import xmlaserver.Olap.Member;


public class XmlaHandler {
	static Logger log = Logger.getLogger(XmlaHandler.class.getName());
		
	public XmlaHandler() {
	}

	public static void handleExecuteRequest(String sessionId, String statement, Map<String,String> propertiesMap, OutputStream outputStream) throws Exception {
		log.info("handleExecuteRequest " + propertiesMap);
		log.info(statement);

		PrintStream ps = new PrintStream(outputStream);
		
		MdxSelectDefinition sel = MdxParser.parse(statement);
		sel.print(System.out); 
		
		String cubeName = sel.getCube().getNames().get(0);
		for (Cube c : Cube.cubes) {
			if (c.getCaption().equals(cubeName)) {
				DataRequestBuilder drb = new DataRequestBuilder(c);
				DataRequest drSelect = drb.build(sel);
								
				Map<DataTuple,Object> dataResultMap= new HashMap<DataTuple,Object>();
				
				for (AtomicRequest ar : drSelect.getAtomicRequestList())
					dataResultMap.putAll(AnalysisService.getInstance().executeAtomicRequest(ar));
				
				for (DataTuple dt : dataResultMap.keySet())
					drSelect.addTupleToAxes(dt);   // TODO: Refactor this. Results should be separate from request
				
				ps.println(TextConstants.getText("xmla/Execute/Execute_header.txt"));
				
				ps.println("<OlapInfo>");
				
				ps.println("<CubeInfo><Cube><CubeName>" + c.getCaption() + "</CubeName></Cube></CubeInfo>");
				
				ps.println("<AxesInfo>");
				for (Axis a : drSelect.getAxes()) {
					ps.println("<AxisInfo name=\"" + a.getName() + "\">");
					for (Hierarchy h : a.getHierarchies()) {
						if (h.getDimension().getType() == Dimension.Type.MD_DIMTYPE_MEASURE)
							ps.println("<HierarchyInfo name=\"Measures\">");
						else
							ps.println("<HierarchyInfo name=\"" + h.getFullUniqueName() + "\">");
						
						ps.println("<UName name=\"[" + h.getUniqueName() + "].[MEMBER_UNIQUE_NAME]\"/>");
						ps.println("<Caption name=\"[" + h.getUniqueName() + "].[MEMBER_CAPTION]\"/>");
						ps.println("<LName name=\"[" + h.getUniqueName() + "].[LEVEL_UNIQUE_NAME]\"/>");
						ps.println("<LNum name=\"[" + h.getUniqueName() + "].[LEVEL_NUMBER]\"/>");
						ps.println("<DisplayInfo name=\"[" + h.getUniqueName() + "].[DISPLAY_INFO]\"/>");
						if (h.getDimension().getType() != Dimension.Type.MD_DIMTYPE_MEASURE)
							ps.println("<PARENT_UNIQUE_NAME name=\"[" + h.getUniqueName() + "].[PARENT_UNIQUE_NAME]\" type=\"xsd:string\"/>");
						ps.println("</HierarchyInfo>");
					}
					ps.println("</AxisInfo>");
				}				
				ps.println("</AxesInfo>");

				ps.println("<CellInfo>");
				ps.println("<Value name=\"VALUE\"/>");
				ps.println("<FormatString name=\"FORMAT_STRING\"/>");
				ps.println("<Language name=\"LANGUAGE\"/>");
				ps.println("<BackColor name=\"BACK_COLOR\"/>");
				ps.println("<ForeColor name=\"FORE_COLOR\"/>");
				ps.println("<FontFlags name=\"FONT_FLAGS\"/>");
				ps.println("</CellInfo>");

				ps.println("</OlapInfo>");
				
				ps.println("<Axes>");
				for (Axis a : drSelect.getAxes()) {
					ps.println("<Axis name=\"" + a.getName() + "\">");
					ps.println("<Tuples>");			
					for (AxisTuple at : a.getTuples()) {
						ps.println("<Tuple>");
						for (Member m : at.getMembers()) {
							if (m.getDimension().getType() == Dimension.Type.MD_DIMTYPE_MEASURE)
								ps.println("<Member Hierarchy=\"Measures\">");
							else
								ps.println("<Member Hierarchy=\"" + m.getHierarchy().getFullUniqueName() + "\">");
							
							printProperty(ps, "UName", m.getFullUniqueName());
							printProperty(ps, "Caption", m.getCaption());
							printProperty(ps, "LName", m.getLevel().getFullUniqueName());
							printProperty(ps, "LNum", Integer.toString(m.getLevel().getOrdinal()));
							printProperty(ps, "DisplayInfo", "66191");  // 131073, 196609, 66191, 0   TODO: what is it???
							// https://msdn.microsoft.com/en-us/library/windows/desktop/ms725398%28v=vs.85%29.aspx
							// Whether on not it can expand
							if (m.getParentMember() != null)
								printProperty(ps, "PARENT_UNIQUE_NAME", m.getParentMember().getFullUniqueName());
							ps.println("</Member>");							
						}
						ps.println("</Tuple>");			
					}
					ps.println("</Tuples>");
					ps.println("</Axis>");
					
				}
				ps.println("</Axes>");
				
				ps.println("<CellData>");
				if (drSelect.getAxes().size() == 1) {   // 1-Dimensional result
					Axis axis0 = drSelect.getAxes().get(0); 
					
					int cellOrdinal = 0;
					for (AxisTuple at0 : axis0.getTuples()) {
						DataTuple t = new DataTuple(at0.getMembers());
						Object value = dataResultMap.get(t);
						if (value != null) {
							ps.println("<Cell CellOrdinal=\"" + Integer.toString(cellOrdinal) + "\">");
							if (value instanceof Double) {  // TODO: java.math.BigDecimal
								Double d = (Double)value;
								ps.println("<Value xsi:type=\"xsd:double\">" + d + "</Value>");  // TODO: handle other data types
								ps.println("<FormatString>#,###</FormatString>");
							} else {
								ps.println("<Value xsi:type=\"xsd:double\">" + value + "</Value>");  
								ps.println("<FormatString>#,###</FormatString>");
								//System.out.println("value - " + value.getClass().getName());
							}
							ps.println("</Cell>");
						}
						cellOrdinal++;
					}
				} else if (drSelect.getAxes().size() == 2) {  // 2-Dimensional result
					Axis axis0 = drSelect.getAxes().get(0); 
					Axis axis1 = drSelect.getAxes().get(1);
					
					int cellOrdinal = 0;
					for (AxisTuple at1 : axis1.getTuples())
						for (AxisTuple at0 : axis0.getTuples()) {
							//System.out.println("at0.getMembers()=" + at0.getMembers());
							//System.out.println("at1.getMembers()=" + at1.getMembers());
							
							DataTuple t = new DataTuple(at0.getMembers());
							t.addAll(at1.getMembers());
							Object value = dataResultMap.get(t);
							if (value != null) {
								ps.println("<Cell CellOrdinal=\"" + Integer.toString(cellOrdinal) + "\">");
								ps.println("<Value xsi:type=\"xsd:double\">" + value + "</Value>");
								ps.println("<FormatString>#,###</FormatString>");
								ps.println("</Cell>");
								
								// Cell Value errors:
								// <Cell CellOrdinal="10">
								//     <Value>
								//         <Error>
								//             <ErrorCode>2148497527</ErrorCode>
								//             <Description>Security Error.</Description>
								//         </Error>
								//     </Value>
								// </Cell>
							}
							cellOrdinal++;
						}
				} else if (drSelect.getAxes().size() == 3) {  // 3-Dimensional result
					Axis axis0 = drSelect.getAxes().get(0); 
					Axis axis1 = drSelect.getAxes().get(1);
					Axis axis2 = drSelect.getAxes().get(2);
					
					int cellOrdinal = 0;
					for (AxisTuple at2 : axis2.getTuples())
						for (AxisTuple at1 : axis1.getTuples())
							for (AxisTuple at0 : axis0.getTuples()) {
								//System.out.println("at0.getMembers()=" + at0.getMembers());
								//System.out.println("at1.getMembers()=" + at1.getMembers());
								
								DataTuple t = new DataTuple(at0.getMembers());
								t.addAll(at1.getMembers());
								t.addAll(at2.getMembers());
								Object value = dataResultMap.get(t);
								if (value != null) {
									ps.println("<Cell CellOrdinal=\"" + Integer.toString(cellOrdinal) + "\">");
									ps.println("<Value xsi:type=\"xsd:double\">" + value + "</Value>");
									ps.println("<FormatString>#,###</FormatString>");
									ps.println("</Cell>");
								}
								cellOrdinal++;
							}
				}
				
				ps.println("</CellData>");
				
				ps.println(TextConstants.getText("xmla/Execute/Execute_footer.txt"));
			}
		}
	}
	
	public static void handleDiscoverRequest(String sessionId, String requestType, Map<String,String> restictionsMap, Map<String,String> propertiesMap, OutputStream outputStream) throws Exception {
		log.info("handleDiscoverRequest " + requestType + "; " + restictionsMap + "; " + propertiesMap);

		PrintStream ps = new PrintStream(outputStream);
				
		if (requestType.equalsIgnoreCase("DISCOVER_PROPERTIES")) {
			String propertyName = restictionsMap.get("PropertyName"); 
			ps.println(TextConstants.getText("xmla/Discover/DISCOVER_PROPERTIES/" + propertyName + ".xml"));
			// TODO: 
			
		} else if (requestType.equalsIgnoreCase("DISCOVER_SCHEMA_ROWSETS")) {
			ps.println(TextConstants.getText("xmla/Discover/DISCOVER_SCHEMA_ROWSETS.xml"));
			
		} else if (requestType.equalsIgnoreCase("DISCOVER_LITERALS")) {
			ps.println(TextConstants.getText("xmla/Discover/DISCOVER_LITERALS.xml"));
			
		} else if (requestType.equalsIgnoreCase("DBSCHEMA_CATALOGS")) {
			ps.println(TextConstants.getText("xmla/Discover/DBSCHEMA/DBSCHEMA_CATALOGS.xml"));
			
		} else if (requestType.equalsIgnoreCase("MDSCHEMA_CUBES")) {
			// https://msdn.microsoft.com/en-US/library/ms126271(v=sql.90).aspx
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_CUBES_header.txt"));
			for (Cube c : Cube.cubes) {
				ps.println("<row>");
				printProperty(ps, "CATALOG_NAME", c.getCatalogName());
				printProperty(ps, "SCHEMA_NAME", c.getSchemaName());
				printProperty(ps, "CUBE_NAME", c.getCaption());
				printProperty(ps, "CUBE_TYPE", "CUBE");			// CUBE or DIMENSION 
				printProperty(ps, "CREATED_ON", "2015-11-06T06:39:53");  // Not supported
				printProperty(ps, "LAST_SCHEMA_UPDATE", "2015-11-06T06:39:53");  // 
				printProperty(ps, "DESCRIPTION", c.getDescription());
				printProperty(ps, "IS_DRILLTHROUGH_ENABLED", Boolean.toString(c.getDrillthroughEnabled()).toLowerCase());
				printProperty(ps, "IS_LINKABLE", "false");
				printProperty(ps, "IS_WRITE_ENABLED", "false");
				printProperty(ps, "IS_SQL_ENABLED", "false");
				printProperty(ps, "CUBE_CAPTION", c.getCaption());
				ps.println("</row>");
			}
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_CUBES_footer.txt"));
			
		} else if (requestType.equalsIgnoreCase("DBSCHEMA_TABLES")) {
			ps.println(TextConstants.getText("xmla/Discover/DBSCHEMA/DBSCHEMA_TABLES_header.txt"));
			// Let's try an empty response (seems to be working)
			ps.println(TextConstants.getText("xmla/Discover/DBSCHEMA/DBSCHEMA_TABLES_footer.txt"));
			
		} else if (requestType.equalsIgnoreCase("MDSCHEMA_PROPERTIES")) {
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_PROPERTIES.xml"));
			
		} else if (requestType.equalsIgnoreCase("MDSCHEMA_DIMENSIONS")) {
			// https://msdn.microsoft.com/en-US/library/ms126180(v=sql.90).aspx
			// https://docs.oracle.com/cloud/farel9/financialscs_gs/FAAPI/ch28s03s03.html
			String restrictionCatalogName = restictionsMap.get("CATALOG_NAME");
			String restrictionCubeName = restictionsMap.get("CUBE_NAME");
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_DIMENSIONS_header.txt"));
			for (Cube c : Cube.cubes) {
				if (c.getCatalogName().equals(restrictionCatalogName) && c.getCaption().equals(restrictionCubeName)) {
								
					for (Dimension d : c.getDimensions()) {
						ps.println("<row>");
						printProperty(ps, "CATALOG_NAME", c.getCatalogName());
						printProperty(ps, "SCHEMA_NAME", c.getSchemaName());
						printProperty(ps, "CUBE_NAME", c.getCaption());
						printProperty(ps, "DIMENSION_NAME", d.getCaption());
						printProperty(ps, "DIMENSION_UNIQUE_NAME", d.getUniqueName());
						printProperty(ps, "DIMENSION_CAPTION", d.getCaption());
						printProperty(ps, "DIMENSION_ORDINAL", Integer.toString(d.getOrdinal()));
						printProperty(ps, "DIMENSION_TYPE", Integer.toString(d.getType().getValue()));
						printProperty(ps, "DIMENSION_CARDINALITY", Integer.toString(d.getCardinality())); // Number of members in the dimension
						if ( d.getDefaultHierarchy() != null)
							printProperty(ps, "DEFAULT_HIERARCHY", d.getDefaultHierarchy().getUniqueName());
						printProperty(ps, "DESCRIPTION", d.getDescription());
						printProperty(ps, "IS_VIRTUAL", "false");   // Always FALSE
						printProperty(ps, "IS_READWRITE", "false");
						printProperty(ps, "DIMENSION_UNIQUE_SETTINGS", "0");
						printProperty(ps, "DIMENSION_IS_VISIBLE", Boolean.toString(d.getVisible()).toLowerCase());
						ps.println("</row>");
					}
				}
			}
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_DIMENSIONS_footer.txt"));
			
		} else if (requestType.equalsIgnoreCase("MDSCHEMA_HIERARCHIES")) {
			// https://msdn.microsoft.com/en-US/library/ms126062(v=sql.90).aspx
			String restrictionCatalogName = restictionsMap.get("CATALOG_NAME");
			String restrictionCubeName = restictionsMap.get("CUBE_NAME");
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_HIERARCHIES_header.txt"));
			for (Cube c : Cube.cubes) {
				if (c.getCatalogName().equals(restrictionCatalogName) && c.getCaption().equals(restrictionCubeName)) {
					for (Dimension d : c.getDimensions()) {
						for (Hierarchy h : d.getHierarchies()) {
							ps.println("<row>");
							printProperty(ps, "CATALOG_NAME", c.getCatalogName());
							printProperty(ps, "SCHEMA_NAME", c.getSchemaName());
							printProperty(ps, "CUBE_NAME", c.getCaption());
							printProperty(ps, "DIMENSION_UNIQUE_NAME", d.getUniqueName());
							printProperty(ps, "HIERARCHY_NAME", h.getCaption());
							printProperty(ps, "HIERARCHY_UNIQUE_NAME", h.getFullUniqueName());
							printProperty(ps, "HIERARCHY_CAPTION", h.getCaption());
							printProperty(ps, "DIMENSION_TYPE", Integer.toString(d.getType().getValue()));
							printProperty(ps, "HIERARCHY_CARDINALITY", Integer.toString(h.getCardinality()));
							if (h.getDefaultMember() != null)
								printProperty(ps, "DEFAULT_MEMBER", h.getDefaultMember().getFullUniqueName());
							if (h.getMemberAll() != null)
								printProperty(ps, "ALL_MEMBER", h.getMemberAll().getFullUniqueName());
							printProperty(ps, "DESCRIPTION", h.getDescription());
							printProperty(ps, "STRUCTURE", "0");       // MD_STRUCTURE_FULLYBALANCED (0), MD_STRUCTURE_RAGGEDBALANCED (1), MD_STRUCTURE_UNBALANCED (2), MD_STRUCTURE_NETWORK (3)
							printProperty(ps, "IS_VIRTUAL", "false");  // Always returns False.
							printProperty(ps, "IS_READWRITE", "false");
							printProperty(ps, "DIMENSION_UNIQUE_SETTINGS", "1");   // Always returns MDDIMENSIONS_MEMBER_KEY_UNIQUE (1).
							printProperty(ps, "DIMENSION_IS_VISIBLE", Boolean.toString(d.getVisible()).toLowerCase());
							printProperty(ps, "HIERARCHY_ORDINAL", Integer.toString(h.getOrdinal()));
							printProperty(ps, "DIMENSION_IS_SHARED", "true");  // Always returns TRUE.
							printProperty(ps, "HIERARCHY_IS_VISIBLE", Boolean.toString(h.getVisible()).toLowerCase());
							
							// Display hierarchy as a simple attribute
							if (h.getLevels().size() == 1 || (h.getLevels().size() == 2 && h.getLevelAll() != null))
								printProperty(ps, "HIERARCHY_ORIGIN", "2");
							//    0x0000001 (MD_USER_DEFINED) identifies user defined hierarchies
							//    0x0000002 (MD_SYSTEM_ENABLED) identifies attribute hierarchies
							//    0x0000004 (MD_SYSTEM_INTERNAL) identifies attributes with no attribute hierarchies
							
							if (h.getCaption().equals("Region"))
								printProperty(ps, "HIERARCHY_DISPLAY_FOLDER", "Test"); // The path to be used when displaying the hierarchy in the user interface. Folder names will be separated by a semicolon (;). Nested folders are indicated by a backslash (\).
							
							printProperty(ps, "PARENT_CHILD", "false");
							ps.println("</row>");
						}
					}
				}
			}
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_HIERARCHIES_footer.txt"));

		} else if (requestType.equalsIgnoreCase("MDSCHEMA_LEVELS")) {
			// https://msdn.microsoft.com/en-US/library/ms126038(v=sql.90).aspx
			// https://docs.oracle.com/cd/E51367_01/financialsop_gs/FAPSA/ch04s03s10.html
			String restrictionCatalogName = restictionsMap.get("CATALOG_NAME");
			String restrictionCubeName = restictionsMap.get("CUBE_NAME");
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_LEVELS_header.txt"));
			for (Cube c : Cube.cubes) {
				if (c.getCatalogName().equals(restrictionCatalogName) && c.getCaption().equals(restrictionCubeName)) {
					for (Dimension d : c.getDimensions()) {
						for (Hierarchy h : d.getHierarchies()) {
							for (Level l : h.getLevels()) {
								ps.println("<row>");
								printProperty(ps, "CATALOG_NAME", c.getCatalogName());
								printProperty(ps, "SCHEMA_NAME", c.getSchemaName());
								printProperty(ps, "CUBE_NAME", c.getCaption());
								printProperty(ps, "DIMENSION_UNIQUE_NAME", d.getUniqueName());
								printProperty(ps, "HIERARCHY_UNIQUE_NAME", h.getFullUniqueName());
								printProperty(ps, "LEVEL_NAME", l.getCaption());
								printProperty(ps, "LEVEL_UNIQUE_NAME", l.getFullUniqueName());
								printProperty(ps, "LEVEL_CAPTION", l.getCaption());
								printProperty(ps, "LEVEL_NUMBER", Integer.toString(l.getOrdinal()));
								printProperty(ps, "LEVEL_CARDINALITY", Integer.toString(l.getCardinality()));  // Number of members in the level
								printProperty(ps, "LEVEL_TYPE", Integer.toString(l.getType().getValue()));
								printProperty(ps, "CUSTOM_ROLLUP_SETTINGS", "0");
								printProperty(ps, "LEVEL_UNIQUE_SETTINGS", "0");
								printProperty(ps, "LEVEL_IS_VISIBLE", Boolean.toString(l.getVisible()).toLowerCase());
								printProperty(ps, "DESCRIPTION", l.getDescription());
								ps.println("</row>");
							}
						}
					}
				}
			}
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_LEVELS_footer.txt"));
/*
 * In Excel measures can be arranged in folders using MEASURE_DISPLAY_FOLDER property in MDSCHEMA_MEASURES 
 * MDSCHEMA_MEASUREGROUPS does not seems to be particulary useful
 * If it is required please uncomment a related section in xmla\Discover\DISCOVER_SCHEMA_ROWSETS.xml

		} else if (requestType.equalsIgnoreCase("MDSCHEMA_MEASUREGROUPS")) {
			// https://msdn.microsoft.com/en-US/library/ms126178(v=sql.90).aspx
			// GUID: https://msdn.microsoft.com/en-us/library/microsoft.analysisservices.adomdclient.adomdschemaguid.measuregroups(v=sql.90).aspx
			String restrictionCubeName = restictionsMap.get("CUBE_NAME");
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_MEASUREGROUPS_header.txt"));
			for (Cube c : Cube.cubes) {
				if (c.getName().equals(restrictionCubeName)) {
					ps.println("<row>");
					printProperty(ps, "CATALOG_NAME", c.getCatalogName());  // The name of the catalog to which this measure group belongs. NULL if the provider does not support catalogs.
					printProperty(ps, "SCHEMA_NAME", c.getSchemaName());    // Not supported
					printProperty(ps, "CUBE_NAME", c.getName());
					printProperty(ps, "MEASUREGROUP_NAME", "MEASUREGROUP name");
					printProperty(ps, "DESCRIPTION", "MEASUREGROUP description");
					printProperty(ps, "IS_WRITE_ENABLED", "false");
					printProperty(ps, "MEASUREGROUP_CAPTION", "MEASUREGROUP caption");
					ps.println("</row>");
				}
			}
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_MEASUREGROUPS_footer.txt"));
*/
			
		} else if (requestType.equalsIgnoreCase("MDSCHEMA_MEASURES")) {
			// https://msdn.microsoft.com/en-us/library/ms126250.aspx
			// String restrictionCatalogName = restictionsMap.get("CATALOG_NAME");  // Excel does not supply CATALOG_NAME for some reason
			String restrictionCubeName = restictionsMap.get("CUBE_NAME");
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_MEASURES_header.txt"));
			for (Cube c : Cube.cubes) {
				if (c.getCaption().equals(restrictionCubeName)) {
					for (Measure m : c.getMeasures()) {
						ps.println("<row>");
						printProperty(ps, "CATALOG_NAME", c.getCatalogName());
						printProperty(ps, "SCHEMA_NAME", c.getSchemaName());
						printProperty(ps, "CUBE_NAME", c.getCaption());
						printProperty(ps, "MEASURE_NAME", m.getCaption());
						printProperty(ps, "MEASURE_UNIQUE_NAME", m.getFullUniqueName());
						printProperty(ps, "MEASURE_CAPTION", m.getCaption());
						printProperty(ps, "MEASURE_AGGREGATOR", "1");	// TODO: are there any other options?
						printProperty(ps, "DATA_TYPE", "5"); 			// TODO: may need to implement a method
						printProperty(ps, "MEASURE_IS_VISIBLE", Boolean.toString(m.getVisible()).toLowerCase());
						printProperty(ps, "LEVELS_LIST", "[Date].[Date]");  // TODO: revisit this
						printProperty(ps, "DESCRIPTION", m.getDescription());
						
						//printProperty(ps, "MEASUREGROUP_NAME", "MEASUREGROUP name");  // Does not seems to be particulary useful
						
						// The path to be used when displaying the measure in the user interface. Folder names will be separated by a semicolon. Nested folders are indicated by a backslash (\).
						if (m.getCaption().equals("Income x 10"))
							printProperty(ps, "MEASURE_DISPLAY_FOLDER", "Calculated\\Test");
						
						printProperty(ps, "DEFAULT_FORMAT_STRING", m.getDefaultFormatString());
						ps.println("</row>");
					}
				}
			}
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_MEASURES_footer.txt"));

		} else if (requestType.equalsIgnoreCase("MDSCHEMA_SETS")) {
			ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_SETS.xml"));

		} else if (requestType.equalsIgnoreCase("MDSCHEMA_MEMBERS")) {
			handleDiscoverMdschemaMembrs(sessionId, restictionsMap, propertiesMap, ps);			
		}
	}
	
	public static void handleDiscoverMdschemaMembrs(String sessionId, Map<String,String> restictionsMap, Map<String,String> propertiesMap, PrintStream ps) throws Exception {
		// https://msdn.microsoft.com/en-us/library/ms126046(v=sql.90).aspx
		// http://docs.oracle.com/cd/E12825_01/epm.111/aps_admin/frameset.htm?ch03s03s07.html
		
		String restrictionCubeName = restictionsMap.get("CUBE_NAME");
		String memberUniqueName = restictionsMap.get("MEMBER_UNIQUE_NAME");
		String levelUniqueName = restictionsMap.get("LEVEL_UNIQUE_NAME");
		
		ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_MEMBERS_header.txt"));
		for (Cube c : Cube.cubes) {
			if (c.getCaption().equals(restrictionCubeName)) {
				// We need to have a sorted result
				TreeSet<Member> memberList = new TreeSet<Member>();
				
				if (memberUniqueName != null) {
					String restrictionTreeOp = restictionsMap.get("TREE_OP");
					if (restrictionTreeOp == null)
						throw new Exception("TREE_OP is not provided for Discover MDSCHEMA_MEMBERS MEMBER_UNIQUE_NAME request");
					
					int treeOp = Integer.parseInt(restrictionTreeOp);
					
					Member restrictionMember = null;
					CubeMetadata cm = c.resolveMultipartName(memberUniqueName);
					
					if (cm != null && cm instanceof Member)
						restrictionMember = (Member)cm;
					else
						throw new Exception("Unable to resolve " + memberUniqueName + " to a Member");
					
					if (treeOp == TreeOp.MDTREEOP_SELF.getValue()) { // 8
						memberList.add(restrictionMember);  // TODO: handle isInitialized
						
					} else if (treeOp == TreeOp.MDTREEOP_DESCENDANTS.getValue()) { 
						if (restrictionMember.getParentMember() != null) {
							AtomicRequest ar = new AtomicRequest();
							ar.addLevel(restrictionMember.getLevel());
							if (restrictionMember.getParentMember().getType() != Member.Type.MDMEMBER_TYPE_ALL)
								ar.putFilter(restrictionMember.getDimension(), new MemberCondition(restrictionMember.getParentMember()));
							AnalysisService.getInstance().executeAtomicRequest(ar);  // This would populate all required members
							
							memberList.addAll(restrictionMember.getParentMember().getChildMembers());
						} else {
							if (restrictionMember.getType() != Member.Type.MDMEMBER_TYPE_ALL) {
								AtomicRequest ar = new AtomicRequest();
								ar.addLevel(restrictionMember.getLevel());
								AnalysisService.getInstance().executeAtomicRequest(ar);  // This would populate all required members
							}
							
							memberList.addAll(restrictionMember.getLevel().getMembers());
						}
						
					} else if (treeOp == TreeOp.MDTREEOP_PARENT.getValue()) {
						if (restrictionMember.getParentMember() != null)
							memberList.add(restrictionMember.getParentMember());
						
					} else if (treeOp == TreeOp.MDTREEOP_CHILDREN.getValue()) {
						if (restrictionMember.getLevel().getNextLevel() != null) {
							AtomicRequest ar = new AtomicRequest();
							ar.addLevel(restrictionMember.getLevel().getNextLevel());
							ar.putFilter(restrictionMember.getDimension(), new MemberCondition(restrictionMember));
							AnalysisService.getInstance().executeAtomicRequest(ar);  // This would populate all required members
							
							memberList.addAll(restrictionMember.getChildMembers());
						}
					} else 
						throw new Exception("Discover MDSCHEMA_MEMBERS with unsupported TREE_OP " + treeOp);
					
				} else if (levelUniqueName != null) {
					Level restrictionLevel = null;
					CubeMetadata cm = c.resolveMultipartName(levelUniqueName);
					
					if (cm != null && cm instanceof Level)
						restrictionLevel = (Level)cm;
					else
						throw new Exception("Unable to resolve " + levelUniqueName + " to a Level");
					
					if (restrictionLevel.getType() != Level.Type.MDLEVEL_TYPE_ALL) {
						AtomicRequest ar = new AtomicRequest();
						ar.addLevel(restrictionLevel);
						AnalysisService.getInstance().executeAtomicRequest(ar);  // This would populate all required members
					}
					
					memberList.addAll(restrictionLevel.getMembers());
				}
				
				for (Member m : memberList) {
					ps.println("<row>");
					printProperty(ps, "CATALOG_NAME", c.getCatalogName());
					printProperty(ps, "SCHEMA_NAME", c.getSchemaName());
					printProperty(ps, "CUBE_NAME", c.getCaption());
					printProperty(ps, "DIMENSION_UNIQUE_NAME", m.getDimension().getUniqueName());
					printProperty(ps, "HIERARCHY_UNIQUE_NAME", m.getHierarchy().getFullUniqueName());
					printProperty(ps, "LEVEL_UNIQUE_NAME", m.getLevel().getFullUniqueName());
					printProperty(ps, "LEVEL_NUMBER", Integer.toString(m.getLevel().getOrdinal()));
					printProperty(ps, "MEMBER_ORDINAL", Integer.toString(m.getOrdinal()));
					printProperty(ps, "MEMBER_NAME", m.getCaption());
					printProperty(ps, "MEMBER_UNIQUE_NAME", m.getFullUniqueName());
					printProperty(ps, "MEMBER_TYPE", Integer.toString(m.getType().getValue()));
					printProperty(ps, "MEMBER_CAPTION", m.getCaption());
					if (m.getLevel().getNextLevel() != null)
						printProperty(ps, "CHILDREN_CARDINALITY", Integer.toString(m.getChildrenCardinality() + 2));
					else
						printProperty(ps, "CHILDREN_CARDINALITY", "0"); 
					
					printProperty(ps, "PARENT_LEVEL", "0");   			// The distance of the member's parent from the root level of the hierarchy. The root level is zero (0).
					if (m.getParentMember() != null) {
						printProperty(ps, "PARENT_UNIQUE_NAME", m.getParentMember().getFullUniqueName());
						printProperty(ps, "PARENT_COUNT", "1");				// TODO: calculate???
					} else
						printProperty(ps, "PARENT_COUNT", "0");				// TODO: calculate???
					printProperty(ps, "DEPTH", "1");					// Not on TechNet
					ps.println("</row>");
				}
			}
		}
		ps.println(TextConstants.getText("xmla/Discover/MDSCHEMA/MDSCHEMA_MEMBERS_footer.txt"));
	
	}
	
	public static void printProperty(PrintStream ps, String property, String value) {
		ps.println("<" + property + ">" + value + "</" + property + ">");
	}
	
    public enum TreeOp {
    	MDTREEOP_ANCESTORS(20),
    	MDTREEOP_CHILDREN(1),
    	MDTREEOP_SIBLINGS(2),
    	MDTREEOP_PARENT(4),
    	MDTREEOP_SELF(8),
    	MDTREEOP_DESCENDANTS(10);
    	
	    private final int value;

	    private TreeOp(int value) {
	    	this.value = value;
	    }
	    
	    public int getValue() {
	    	return value;
	    }	    
    }
}
