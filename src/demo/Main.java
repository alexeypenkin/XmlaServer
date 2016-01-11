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

package demo;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import xmlaserver.Data.AnalysisService;
import xmlaserver.Olap.Cube;
import xmlaserver.Olap.Dimension;
import xmlaserver.Olap.Hierarchy;
import xmlaserver.Olap.Level;
import xmlaserver.Olap.Member;
import xmlaserver.XmlaServer.XmlaServlet;

public class Main {
	// TODO:
	// - Measure format
	// x Caching
	// x Default members
	// ? Add default members to SlicerAxis (DataRequestBuilder)
	// x Sub-queries(to support Excel filters)
	// x Measure groups
	// x Define attributes and measures(?) at Analysis Service level
	// x move toSQL from Condition to elsewhere :)
	// - Synchronization
	// x handleDiscoverRequest MDSCHEMA_MEMBERS LEVEL_UNIQUE_NAME
	// - Error reporting
	// x "Invisible" hierarchies (i.e. attributes on dimensions)
	// - Order by field
	// x Sort order - sort by ID if names are the same
	// - Support for other functions required for filtering (InStr, etc)
	// o DIME compression (X-Transport-Caps-Negotiation-Flags)
	//      http://sqlblog.com/blogs/mosha/archive/2005/12/02/analysis-services-2005-protocol-xmla-over-tcp-ip.aspx
	// - DRILLDOWNMEMBER(S, m) to check that Members [m] belongs to set [S]   see DataRequestBuilder.buildMdxFunction
	//      DrillDownMember(<set>, <set>) - Drills down the members in a specified set that are present in a second specified set
	// - populate DisplayInfo correctly in XmlaHandler.handleExecuteRequest
	// - Split DataRequest and Axis data results (addTupleToAxes) see XmlaHandler.handleExecuteRequest
	// - Folders for Measures and Hierarchies
	// x refactor Caption + UniqueName
	// x NULL handling
	// - Empty result set causing errors (when filters causing result set to be empty)
	
	static Logger log = Logger.getLogger(Main.class.getName());
	
	public static AnalysisService as;
	
	public static void main(String[] args) throws Exception {
		log.info("XmlaServer by Alexey Penkin");
		
		as = new DemoAnalysisService();
		
		// DEFINE CUBE:			
		
		Cube demoCube = new Cube("Test Catalog", "Test Schema", "DemoCube", "Demo Cube");
		demoCube.setDescription("Demo OLAP Cube");
		demoCube.addMeasure("Income", "Income", "#,###", as.getMeasureAttribute("income"));   
		demoCube.addMeasure("Income x 10", "Income x 10", "#,###", as.getMeasureAttribute("income"));  
		demoCube.addMeasure("Expences", "Expences", "#,###", as.getMeasureAttribute("expences"));    
		
		Dimension dimensionDate = demoCube.addDimension("Date", "Date", Dimension.Type.MD_DIMTYPE_OTHER);
		Hierarchy hierarchyDate = dimensionDate.addHierarchy("Date", "Date", false);
		Level levelDate = hierarchyDate.addLevel("Date", "Date", Level.Type.MDLEVEL_TYPE_REGULAR, as.getAttribute("date_caption"), as.getAttribute("date_id"), as.getAttribute("date_caption"));
		Member date20150807 = levelDate.addMember("07-AUG-2015", "5698", Member.Type.MDMEMBER_TYPE_REGULAR, hierarchyDate.getMemberAll());
		hierarchyDate.setDefaultMember(date20150807); // Excel crashes without it (Dimensions without All require a default member)
		
		Dimension dimensionRegion = demoCube.addDimension("Region", "Region", Dimension.Type.MD_DIMTYPE_OTHER);
		Hierarchy hierarchyRegion = dimensionRegion.addHierarchy("Region Hierarchy", "H1", true);
		hierarchyRegion.addLevel("Region", "L1", Level.Type.MDLEVEL_TYPE_REGULAR, as.getAttribute("region_name"), as.getAttribute("region_id"), as.getAttribute("region_name"));
		hierarchyRegion.addLevel("Country", "L2", Level.Type.MDLEVEL_TYPE_REGULAR, as.getAttribute("country_name"), as.getAttribute("country_id"), as.getAttribute("country_name"));
		hierarchyRegion.addLevel("City", "L3", Level.Type.MDLEVEL_TYPE_REGULAR, as.getAttribute("city_name"), as.getAttribute("city_id"), as.getAttribute("city_name"));
		
		dimensionRegion.addDimensionAtribute("Region", "Region", true, as.getAttribute("region_name"), as.getAttribute("region_id"), as.getAttribute("region_name"));
		dimensionRegion.addDimensionAtribute("Country", "Country", true, as.getAttribute("country_name"), as.getAttribute("country_id"), as.getAttribute("country_name"));
		dimensionRegion.addDimensionAtribute("City", "City", true, as.getAttribute("city_name"), as.getAttribute("city_id"), as.getAttribute("city_name"));

		runServer();
	}
	
	public static void runServer() throws Exception 
    {
		// http://localhost:8888/xmla
		Server server = new Server(8888);
 
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
 
        context.addServlet(new ServletHolder(new XmlaServlet()),"/xmla");
 
        server.start();
        server.join();
    }
}
