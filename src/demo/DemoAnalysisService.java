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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;

import xmlaserver.Data.AnalysisService;
import xmlaserver.Data.AtomicRequest;
import xmlaserver.Data.Condition;
import xmlaserver.Data.DataTuple;
import xmlaserver.Data.LogicalCondition;
import xmlaserver.Data.MemberCondition;
import xmlaserver.Olap.Attribute;
import xmlaserver.Olap.CubeMetadata;
import xmlaserver.Olap.Dimension;
import xmlaserver.Olap.Level;
import xmlaserver.Olap.Measure;
import xmlaserver.Olap.Member;


public class DemoAnalysisService extends AnalysisService {
	static Logger log = Logger.getLogger(DemoAnalysisService.class.getName());

	private Connection conn = null;
	private PreparedStatement insertPreparedStatement = null;
	private Map<AtomicRequest,Map<DataTuple,Object>> resultsCache;
	
	public DemoAnalysisService() throws Exception {
		
		resultsCache = new HashMap<AtomicRequest,Map<DataTuple,Object>>();
		
		String jdbcDriverName = "org.sqlite.JDBC";
		String connectionString = "jdbc:sqlite:DB/test.db";
		
		log.info("Loading JDBC driver " + jdbcDriverName);
		Class.forName(jdbcDriverName);
		log.info("Connecting to  " + connectionString);
		conn = DriverManager.getConnection(connectionString);
		
		prepareData();


		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM testCube WHERE 1>2");
		
		ResultSetMetaData metaData = rs.getMetaData();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			String columnName = metaData.getColumnName(i);
			log.debug("Registering new SqlAttribute for " + columnName);
			if (columnName.equalsIgnoreCase("income") || columnName.equalsIgnoreCase("expences"))
				putMeasureAttribute(new SqlAttribute(columnName, metaData.getColumnType(i)));
			else
				putAttribute(new SqlAttribute(columnName, metaData.getColumnType(i)));
		}
		rs.close();
	}
	
	private void prepareData() throws Exception {
		log.info("Creating table testCube ... ");
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("DROP TABLE testCube");
	    String sql = "CREATE TABLE testCube (" +
		                   "DATE_ID      INT," +
		                   "DATE_CAPTION VARCHAR(255)," + 
		                   "REGION_NAME  VARCHAR(255)," + 
		                   "REGION_ID    INT, " + 
		                   "COUNTRY_NAME VARCHAR(255)," + 
		                   "COUNTRY_ID   INT, " + 
		                   "CITY_NAME    VARCHAR(255)," + 
		                   "CITY_ID      INT, " + 
		                   "INCOME       DOUBLE," +
		                   "EXPENCES     DOUBLE)"; 
		stmt.executeUpdate(sql);
		stmt.close();
		
		String insertTableSQL = "INSERT INTO testCube"
			+ "(DATE_ID, DATE_CAPTION, REGION_NAME, REGION_ID, COUNTRY_NAME, COUNTRY_ID, CITY_NAME, CITY_ID, INCOME, EXPENCES)"
			+ "VALUES (?,?,?,?,?,?,?,?,?,?)";
		insertPreparedStatement = conn.prepareStatement(insertTableSQL);

		log.info("Preparing test data ... ");
		insertRow(5698, "07-Aug-2015", "Europe", 1, "UK", 11, "London", 111, 239.071632329988, 448.415660303818);
		insertRow(5698, "07-Aug-2015", "Europe", 1, "UK", 11, "Edinburgh", 112, 458.018062855715, -283.894815557211);
		insertRow(5698, "07-Aug-2015", "Europe", 1, "UK", 11, "Cardiff", 113, 487.379205254145, -292.38020270286);
		insertRow(5698, "07-Aug-2015", "Europe", 1, "France", 12, "Paris", 121, 357.185123307163, 44.8732495030715);
		insertRow(5698, "07-Aug-2015", "Europe", 1, "France", 12, "Marseille", 122, 407.205673262799, 418.474251784805);
		insertRow(5698, "07-Aug-2015", "Europe", 1, "France", 12, "Lyon", 123, 571.818043140843, -11.8770553211302);
		insertRow(5698, "07-Aug-2015", "Europe", 1, "Germany", 13, "Berlin", 131, 33.92570876946, 148.130351928833);
		insertRow(5698, "07-Aug-2015", "Europe", 1, "Germany", 13, "Frankfurt", 132, 428.743379765251, 265.977171153864);
		insertRow(5698, "07-Aug-2015", "Europe", 1, "Germany", 13, "Jena", 133, -137.561276947137, 209.778270230596);
		insertRow(5698, "07-Aug-2015", "US", 2, "Michigan", 21, "Detroit", 211, 344.323399550359, 239.893880664167);
		insertRow(5698, "07-Aug-2015", "US", 2, "Michigan", 21, "Grand Rapids", 212, 240.392047181209, -28.8296209764065);
		insertRow(5698, "07-Aug-2015", "US", 2, "Michigan", 21, "Lansing", 213, -83.7950715829878, 255.859874005931);
		insertRow(5698, "07-Aug-2015", "US", 2, "Texas", 22, "Dallas", 221, 71.6568886503664, -30.3621620817031);
		insertRow(5698, "07-Aug-2015", "US", 2, "Texas", 22, "Houston", 222, 639.847778638884, 542.546068171806);
		insertRow(5698, "07-Aug-2015", "US", 2, "Texas", 22, "San Antonio", 223, 615.432319730409, 651.576313592502);
		insertRow(5698, "07-Aug-2015", "US", 2, "California", 23, "Los Angeles", 231, -278.829152811766, 127.746770718702);
		insertRow(5698, "07-Aug-2015", "US", 2, "California", 23, "San Diego", 232, 406.840425462929, 297.817158014322);
		insertRow(5698, "07-Aug-2015", "US", 2, "California", 23, "Paris", 233, 420.495421881471, -249.169971416832);
		insertRow(5698, "07-Aug-2015", "Asia", 3, "India", 31, "Mumbai", 311, 242.309385693287, -164.646188846596);
		insertRow(5698, "07-Aug-2015", "Asia", 3, "India", 31, "Chennai", 312, 275.006920490984, 661.674971850721);
		insertRow(5698, "07-Aug-2015", "Asia", 3, "India", 31, "Delhi", 313, -196.71086557068, 379.024719773191);
		insertRow(5698, "07-Aug-2015", "Asia", 3, "China", 32, "Beijing", 321, 437.865505151266, 244.884127675957);
		insertRow(5698, "07-Aug-2015", "Asia", 3, "China", 32, "Shanghai", 322, 381.140848677103, 80.5151600755492);
		insertRow(5698, "07-Aug-2015", "Asia", 3, "China", 32, "Lyon", 323, 64.7064415414736, 546.052008020195);
		insertRow(5698, "07-Aug-2015", "Asia", 3, "Malasia", 33, "Kuala Lumpur", 331, 225.487389350148, -146.649831544622);
		insertRow(5698, "07-Aug-2015", "Asia", 3, "Malasia", 33, "Taiping", 332, 253.664022930628, 456.248988546079);
		insertRow(5698, "07-Aug-2015", "Asia", 3, "Malasia", 33, "Johor", 333, -101.347334543135, 322.144095853335);
		insertRow(5699, "08-Aug-2015", "Europe", 1, "UK", 11, "London", 111, -294.887928899423, 417.134580385775);
		insertRow(5699, "08-Aug-2015", "Europe", 1, "UK", 11, "Edinburgh", 112, 2.35435463828253, 335.396950874109);
		insertRow(5699, "08-Aug-2015", "Europe", 1, "UK", 11, "Cardiff", 113, -168.363121769483, 5.77674387489952);
		insertRow(5699, "08-Aug-2015", "Europe", 1, "France", 12, "Paris", 121, 656.547061192465, 375.845937160116);
		insertRow(5699, "08-Aug-2015", "Europe", 1, "France", 12, "Marseille", 122, -208.89747053575, -13.6375925780039);
		insertRow(5699, "08-Aug-2015", "Europe", 1, "France", 12, "Lyon", 123, 88.9469101439919, 270.396128249596);
		insertRow(5699, "08-Aug-2015", "Europe", 1, "Germany", 13, "Berlin", 131, -183.527545694528, -223.205016071133);
		insertRow(5699, "08-Aug-2015", "Europe", 1, "Germany", 13, "Frankfurt", 132, 464.111049529263, -242.36135320459);
		insertRow(5699, "08-Aug-2015", "Europe", 1, "Germany", 13, "Jena", 133, -3.03358802450691, 649.718276010974);
		insertRow(5699, "08-Aug-2015", "US", 2, "California", 23, "Paris", 233, -96.2831932922005, -40.3553671269547);
		insertRow(5699, "08-Aug-2015", "US", 2, "California", 23, "Los Angeles", 231, -81.3806918591783, 534.844429557148);
		insertRow(5699, "08-Aug-2015", "US", 2, "California", 23, "San Diego", 232, -178.029177044594, 672.323197471576);
		insertRow(5699, "08-Aug-2015", "Asia", 3, "India", 31, "Mumbai", 311, 699.657805878139, 525.208027621429);
		insertRow(5699, "08-Aug-2015", "Asia", 3, "India", 31, "Chennai", 312, -177.827092450505, 518.230220616072);
		insertRow(5699, "08-Aug-2015", "Asia", 3, "India", 31, "Delhi", 313, 321.261658667727, 404.545243742162);
		insertRow(5699, "08-Aug-2015", "Asia", 3, "China", 32, "Lyon", 323, 403.943261416479, 648.722363072815);
		insertRow(5699, "08-Aug-2015", "Asia", 3, "China", 32, "Beijing", 321, 338.57745685492, 9.42060594969337);
		insertRow(5699, "08-Aug-2015", "Asia", 3, "China", 32, "Shanghai", 322, 187.345835314437, 233.929998514708);
		insertRow(5699, "08-Aug-2015", "Asia", 3, "Malasia", 33, "Kuala Lumpur", 331, 561.397199051988, 101.390936538216);
		insertRow(5699, "08-Aug-2015", "Asia", 3, "Malasia", 33, "Taiping", 332, -129.780809476297, -219.364150228334);
		insertRow(5699, "08-Aug-2015", "Asia", 3, "Malasia", 33, "Johor", 333, -19.6183684859747, -292.114951675158);
		insertRow(5700, "09-Aug-2015", "Europe", 1, "UK", 11, "London", 111, -215.776948691039, -193.967863812396);
		insertRow(5700, "09-Aug-2015", "Europe", 1, "UK", 11, "Edinburgh", 112, 453.584917876162, 145.990223057445);
		insertRow(5700, "09-Aug-2015", "Europe", 1, "UK", 11, "Cardiff", 113, 246.903852265426, 22.2989062728745);
		insertRow(5700, "09-Aug-2015", "Europe", 1, "France", 12, "Paris", 121, 307.068478051932, -50.928913938961);
		insertRow(5700, "09-Aug-2015", "Europe", 1, "France", 12, "Marseille", 122, -107.80540939427, 524.788064159922);
		insertRow(5700, "09-Aug-2015", "Europe", 1, "France", 12, "Lyon", 123, 418.826317916791, -211.203308968467);
		insertRow(5700, "09-Aug-2015", "Europe", 1, "Germany", 13, "Berlin", 131, 698.094555696024, -0.00537833459452486);
		insertRow(5700, "09-Aug-2015", "Europe", 1, "Germany", 13, "Frankfurt", 132, 15.9615647146854, 201.356666390709);
		insertRow(5700, "09-Aug-2015", "Europe", 1, "Germany", 13, "Jena", 133, -87.3762725750566, -161.318947178389);
		insertRow(5700, "09-Aug-2015", "US", 2, "Michigan", 21, "Detroit", 211, 139.062488868013, 155.78304407662);
		insertRow(5700, "09-Aug-2015", "US", 2, "Michigan", 21, "Grand Rapids", 212, -114.369777511814, 1.85203593164812);
		insertRow(5700, "09-Aug-2015", "US", 2, "Michigan", 21, "Lansing", 213, 227.131486049408, 606.826262007795);
		insertRow(5700, "09-Aug-2015", "US", 2, "Texas", 22, "Dallas", 221, -103.343087294492, 301.325746130385);
		insertRow(5700, "09-Aug-2015", "US", 2, "Texas", 22, "Houston", 222, 426.01475149497, -82.0595249872215);
		insertRow(5700, "09-Aug-2015", "US", 2, "Texas", 22, "San Antonio", 223, -248.218159315963, 200.884191689285);
		insertRow(5700, "09-Aug-2015", "US", 2, "California", 23, "Paris", 233, 120.944934373107, 279.418407646243);
		insertRow(5700, "09-Aug-2015", "US", 2, "California", 23, "Los Angeles", 231, -141.413242032801, 147.224722714175);
		//insertRow(5700, "09-Aug-2015", "US", 2, "California", 23, "San Diego", 232, -284.856708301518, 382.000813219491);
		insertRow(5700, "09-Aug-2015", "US", 2, "California", 23, null, 0, -284.856708301518, 382.000813219491);
		log.info("Data ready");
		
		insertPreparedStatement.close();
	}
	
	private void insertRow(int dateId, String dateCaption, String regionName, int regionId, String countryName, int countryId, String cityName, int cityId, double income, double expences) throws SQLException {
		insertPreparedStatement.setInt(1, dateId);
		insertPreparedStatement.setString (2, dateCaption);
		insertPreparedStatement.setString(3, regionName);
		insertPreparedStatement.setInt(4, regionId);
		insertPreparedStatement.setString(5, countryName);
		insertPreparedStatement.setInt(6, countryId);
		insertPreparedStatement.setString(7, cityName);
		
		if (cityId != 0)
			insertPreparedStatement.setInt(8, cityId);
		else
			insertPreparedStatement.setNull(8, Types.INTEGER);
		
		insertPreparedStatement.setDouble(9, income);
		insertPreparedStatement.setDouble(10, expences);
		insertPreparedStatement.executeUpdate();
	}
	
	private String getWhereClause(Condition c) throws Exception {
		if (c instanceof MemberCondition) {
			MemberCondition mc = (MemberCondition) c;
			Member m = mc.getMember();
			
			if (m.getType() == Member.Type.MDMEMBER_TYPE_ALL)
				return "";
			
			Attribute a = m.getLevel().getIdAttribute();
			if (! (a instanceof SqlAttribute) )
				throw new Exception("getWhereClause only support " + SqlAttribute.class.getName() + " while got " + a.getClass().getCanonicalName() + " for Atribute " + a.getName() + " and Member " + m.getFullUniqueName());
			
			SqlAttribute sa = (SqlAttribute)a;
			
			if (m.getParentMember() != null) {
				String parentClause = getWhereClause(new MemberCondition(m.getParentMember()));
				if (parentClause != null && !parentClause.isEmpty())
					return parentClause + " AND " + sa.getWhereClause(m.getUniqueName());
				else
					return sa.getWhereClause(m.getUniqueName());
			} else
				return sa.getWhereClause(m.getUniqueName());
			
		} else if (c instanceof LogicalCondition) {
			LogicalCondition lc = (LogicalCondition) c;
			
			String c1 = getWhereClause(lc.getCondition1());
			String c2 = getWhereClause(lc.getCondition2());
			
			if (c1 == null || c1.isEmpty())
				return c2;
			
			if (c2 == null || c2.isEmpty())
				return c1;
			
			switch (lc.getType()) {
			case AND: return "(" + c1 + ") AND (" + c2 + ")";
			case OR: return "(" + c1 + ") OR (" + c2 + ")";
			default: throw new Exception("getWhereClause got unsupported LogicalCondition type " + lc.getType());
			}	
		} else
			throw new Exception("getWhereClause got unsupported Condition type " + c.getClass().getCanonicalName());
	}
		
	public Map<DataTuple,Object> executeAtomicRequest(AtomicRequest atomicRequest) throws Exception {
		
		// TODO: There must be a mechanism to check that the data source was not changed
		// Otherwise reset cache
		Map<DataTuple,Object> res = resultsCache.get(atomicRequest);
		if (res != null) {
			log.debug("executeAtomicRequest got result from cache");
			return res;
		}
		
		res = new HashMap<DataTuple,Object>();
		
		// Translate AtomicRequest.Levels into Attributes
		Set<Attribute> groupByAttributes = new HashSet<Attribute>();
		for (Level l : atomicRequest.getLevels())
			groupByAttributes.addAll(l.getAttributes());
		//for (Level l : atomicRequest.getParentLevels())
		//	groupByAttributes.addAll(l.getAttributes());

		// Translate DataRequest.Measures into Attributes
		Set<Attribute> measureAttributes = new HashSet<Attribute>();
		for (Measure m : atomicRequest.getMeasures())
			measureAttributes.add(m.getAttribute());
		
		String group = null;
		for (Attribute a : groupByAttributes)
			if (group == null)
				group = "  " + a.getName() + "\n";
			else
				group +=  "  ," + a.getName() + "\n";
		
		String select = group;
		for (Attribute a : measureAttributes)
			if (select == null)
				select = "  SUM(" + a.getName() + ") " + a.getName() + "\n";
			else
				select +=  "  ,SUM(" + a.getName() + ") " + a.getName() + "\n";
		
		String where = "";
		for (Condition c : atomicRequest.getFilters().values()) {
			String condSQL = getWhereClause(c);
			if (where == null || where.isEmpty())
				where = condSQL;
			else if (condSQL != null && !condSQL.isEmpty())
				where = "(" + where + ") AND (" + condSQL + ")"; 
		}
		
		StringBuilder sqlSB = new StringBuilder();
		sqlSB.append("SELECT\n");
		sqlSB.append(select);
		sqlSB.append("FROM \n  testCube\n");
		if (where != null && !where.isEmpty())
			sqlSB.append("WHERE\n  " + where + "\n");
		if (group != null && !group.isEmpty())
			sqlSB.append("GROUP BY\n" + group);
		
		log.debug("executeAtomicRequest executing SQL:\n" + sqlSB);
		
		Statement stmt = conn.createStatement();
		tone(1000, 100, 1.0); // TODO: remove this
		ResultSet rs = stmt.executeQuery(sqlSB.toString());
		Thread.sleep(1000);  // TODO: remove this
		
		/*
		ResultSetMetaData metaData = rs.getMetaData();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			if (i > 1)
				System.out.print("|");
			System.out.print(metaData.getColumnName(i));
		}
		System.out.println();
		*/
		
	    while (rs.next()) {
	    	/*
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				if (i > 1)
					System.out.print("|");
				System.out.print(rs.getString(i));
			}
			System.out.println();
			*/
			
			DataTuple dataTuple = new DataTuple();
			for (Level l : atomicRequest.getLevels())
				if (l.getDimension().getType() != Dimension.Type.MD_DIMTYPE_MEASURE)
					if (l.getType() != Level.Type.MDLEVEL_TYPE_ALL)
						dataTuple.addMember(findOrCreateMember(l, rs));
					else
						dataTuple.addMember(l.getHierarchy().getMemberAll());
			
			for (Measure mes : atomicRequest.getMeasures()) {
				DataTuple dataTupleWithMeasure = new DataTuple(dataTuple);
				dataTupleWithMeasure.addMember(mes);  // Add measure member
				if (mes.getCaption().equalsIgnoreCase("Income x 10"))
					res.put(dataTupleWithMeasure, new Double(rs.getDouble(mes.getAttribute().getName()) * 10.0));
				else
					res.put(dataTupleWithMeasure, rs.getObject(mes.getAttribute().getName()));
				//log.debug("res.put " + dataTupleWithMeasure + " = " + rs.getObject(mes.getAttribute().getName()));
			}
	    }
	    rs.close();	
	    
	    resultsCache.put(atomicRequest, res);
	    log.debug("resultsCache has " + resultsCache.size() + " elements");
	    return res;
	}
		
	private Member findOrCreateMember(Level l, ResultSet rs) throws Exception {
		String id = rs.getString(l.getIdAttribute().getName());
		if (id == null)
			id = "#null";
		
		String name = rs.getString(l.getNameAttribute().getName());
		if (name == null)
			name = "#null";   // Not sure if it is required for names, may be better to show an empty result. 
		
		Member m;
		CubeMetadata meta = l.resolveName(id);
		if (meta != null && meta instanceof Member) {
			m = (Member)meta;
		} else {
			Level previousLevel = l.getPreviousLevel(); 
			Member parentMember = null;
			if (previousLevel != null) {
				if (previousLevel.getType() == Level.Type.MDLEVEL_TYPE_ALL)
					parentMember = previousLevel.getHierarchy().getMemberAll();
				else
					parentMember = findOrCreateMember(previousLevel, rs);
			}
			m = l.addMember(name, id, Member.Type.MDMEMBER_TYPE_REGULAR, parentMember);
		}
		
		return m;
	}
	
	public static void tone(int hz, int msecs, double vol) throws LineUnavailableException {
		byte[] buf = new byte[1];
		AudioFormat af = new AudioFormat(
				8000f, 			// sampleRate
				8,           	// sampleSizeInBits
				1,           	// channels
				true,        	// signed
				false);     	// bigEndian
		SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
		sdl.open(af);
		sdl.start();
		for (int i=0; i < msecs*8; i++) {
			double angle = i / (8000f / hz) * 2.0 * Math.PI;
			buf[0] = (byte)(Math.sin(angle) * 127.0 * vol);
			sdl.write(buf,0,1);
		}
		sdl.drain();
		sdl.stop();
		sdl.close();
	}
}
