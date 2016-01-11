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
import java.util.List;

import xmlaserver.Mdx.Language.MdxAxisDefinition;
import xmlaserver.Mdx.Language.MdxExpression;
import xmlaserver.Mdx.Language.MdxFunction;
import xmlaserver.Mdx.Language.MdxFunctionType;
import xmlaserver.Mdx.Language.MdxId;
import xmlaserver.Mdx.Language.MdxSelectDefinition;
import xmlaserver.Olap.Cube;
import xmlaserver.Olap.CubeMetadata;
import xmlaserver.Olap.Dimension;
import xmlaserver.Olap.Level;
import xmlaserver.Olap.Measure;
import xmlaserver.Olap.Member;


public class DataRequestBuilder {
	
	private Cube cube;
	
	public DataRequestBuilder(Cube cube) {
		this.cube = cube;
	}
	
	public DataRequest buildMdxFunction(MdxFunction mdxFunction) throws Exception{
		//System.out.println("MdxFunction.getDataRequest " + functionType.toString());
		DataRequest res = null;
		
		MdxFunctionType type = mdxFunction.getType();
		List<MdxExpression> attributes = mdxFunction.getAttributes();
				
		if (type == MdxFunctionType.TUPLE) {
			// TUPLE intersection ( <member> [,<member>].. )
			for (MdxExpression attribute : attributes) 
				if (res == null)
					res = buildMdxExpression(attribute);
				else
					res = DataRequest.intersect(res, buildMdxExpression(attribute));
			
		} else if (type == MdxFunctionType.SET) {
			// SET union { <tuple>|<set> [, <tuple>|<set>].. } 
			for (MdxExpression attribute : attributes) 
				if (res == null)
					res = buildMdxExpression(attribute);
				else
					res = DataRequest.union(res, buildMdxExpression(attribute));
			
		} else if (type == MdxFunctionType.HIERARCHIZE) {
			// HIERARCHIZE(<set>) - does not affect data request. Only used in post-processing stage
			if (attributes.size() != 1)
				throw new Exception("Incorrect number of attributes for HIERARCHIZE(<set>) function: " + attributes.size());
			res = buildMdxExpression(attributes.get(0));
			
		} else if (type == MdxFunctionType.MEMBERS) {
			// MEMBERS(<level>) - does not affect data request. Only used in post-processing stage
			if (attributes.size() != 1)
				throw new Exception("Incorrect number of attributes for DRILLDOWNLEVEL(<set>) function: " + attributes.size());
			res = buildMdxExpression(attributes.get(0));
			
		} else if (type == MdxFunctionType.DRILLDOWNLEVEL) {
			// DRILLDOWNLEVEL(<set>) - Drills down the members of a set to one level below the lowest level represented in the set, or to one level below an optionally specified level of a member represented in the set.
			// We will only support DRILLDOWNLEVEL(<member>)
			if (attributes.size() != 1)
				throw new Exception("Incorrect number of attributes for DRILLDOWNLEVEL(<set>) function: " + attributes.size());
			res = buildMdxExpression(attributes.get(0));
			
			Level l = res.getSingleLevel();
			if (l == null)
				throw new Exception("DRILLDOWNLEVEL(<set>) support set on the same level only");

			Level nextLevel = l.getNextLevel();
			if (nextLevel == null)
				throw new Exception("Can't DRILLDOWNLEVEL(<set>) for the last level in the Hierarchy");
			
			AtomicRequest ar = res.getSingleAtomicRequest(); 
			AtomicRequest ar2 = new AtomicRequest(ar);
			ar2.clearLevels();
			ar2.addLevel(nextLevel);
			res.add(ar2);
			
		} else if (type == MdxFunctionType.DRILLDOWNMEMBER) {
			// DrillDownMember(<set>, <set>) - Drills down the members in a specified set that are present in a second specified set
			if (attributes.size() != 2)
				throw new Exception("Incorrect number of attributes for DRILLDOWNMEMBER(<set>, <set>) function: " + attributes.size());
			res = buildMdxExpression(attributes.get(0));

			//System.out.println("------------------ DRILLDOWNMEMBER");
			//res.print(System.out);
			//System.out.println("------------------ DRILLDOWNMEMBER <END>");
			
			DataRequest memberRequest = buildMdxExpression(attributes.get(1));
			//memberRequest.print(System.out);
			Level l = memberRequest.getSingleLevel();
			if (l == null)
				throw new Exception("DRILLDOWNMEMBER(<set1>, <set2>) support set2 with members on the same level only");
			
			Level nextLevel = l.getNextLevel();
			if (nextLevel == null)
				throw new Exception("Can't DRILLDOWNMEMBER(<set>, <set>) for the last level in the Hierarchy");
				
			memberRequest.clearLevels();
			memberRequest.addLevel(nextLevel);
			res.addAll(memberRequest);
			
		} else if (type == MdxFunctionType.CROSSJOIN) {
			// CROSSJOIN(<set>, <set>, ... ) - Drills down the members in a specified set that are present in a second specified set
			if (attributes.size() < 2)
				throw new Exception("Incorrect number of attributes for CROSSJOIN(<set>, <set>, ... ) function: " + attributes.size());
			
			for (MdxExpression attribute : attributes) {
				DataRequest attributeDataRequest = buildMdxExpression(attribute);
				//System.out.println("CROSSJOIN Attribute Hierarchies: " + attributeDataRequest.getHierarchies());
				if (res == null)
					res = attributeDataRequest;
				else
					res = DataRequest.union(res, attributeDataRequest);
			}
			
		} else
			throw new Exception("Unsupported function " + type);

		return res;
	}
	
	public DataRequest buildMdxId(MdxId mdxId) throws Exception {
		AtomicRequest ar = new AtomicRequest();
		
		CubeMetadata meta = cube.resolveMultipartName(mdxId.getNames());
		if (meta == null)
			throw new Exception("Unable to resolve identifier " + mdxId);
		
		if (meta instanceof Member) {
			if (meta instanceof Measure) {
				Measure measure = (Measure) meta;
				ar.addMeasure(measure);
				ar.addLevel(measure.getLevel());
			} else {
				Member m = (Member) meta;
				ar.putFilter(m.getDimension(), new MemberCondition(m));
				ar.addLevel(m.getLevel());
			} 
		} else if (meta instanceof Level) {
			Level l = (Level)meta;
			ar.addLevel(l);
		} else 
			throw new Exception("Identifier is expected for " + mdxId + " but " + meta.getClass().getCanonicalName() + " found");
		
		return new DataRequest(ar);
	}
	
	public DataRequest buildMdxExpression(MdxExpression mdxExpression) throws Exception {
		
		if (mdxExpression instanceof MdxId) {
			return buildMdxId((MdxId)mdxExpression);
		} if (mdxExpression instanceof MdxFunction) {
			return buildMdxFunction((MdxFunction)mdxExpression);
		} else
			throw new Exception("Unsupported MdxExpression " + mdxExpression);
	}
	
	public DataRequest build(MdxSelectDefinition mdxSelectDefinition) throws Exception {
		DataRequest res = null;
		
		if (mdxSelectDefinition.getSubquery() != null) {
			res = build(mdxSelectDefinition.getSubquery());
			res.clearLevels();
			res.clearMeasures();
		}
		
		List<Axis> axesList = new ArrayList<Axis>();
		int axisNumber = 0;
		for (MdxAxisDefinition axis : mdxSelectDefinition.getAxises()) {
			Axis axisInfo = new Axis("Axis" + Integer.toString(axisNumber));
			DataRequest axisDataRequest = buildMdxExpression(axis.getExpression());
			axisInfo.addAllHierarchies(axisDataRequest.getHierarchies());
			axesList.add(axisInfo);
			axisNumber++;
			
			//System.out.println("AxisHierarchy: " + axisDataRequest.getHierarchies());
			if (res == null)
				res = axisDataRequest;
			else
				res = DataRequest.intersect(res, axisDataRequest);		
		}
		
		DataRequest whereDataRequest = null;
		if (mdxSelectDefinition.getWhere() != null) {
			Axis axisInfo = new Axis("SlicerAxis");
			whereDataRequest = buildMdxExpression(mdxSelectDefinition.getWhere());
			axisInfo.addAllHierarchies(whereDataRequest.getHierarchies());
			axesList.add(axisInfo);
			//System.out.println("WhereHierarchy: " + whereDataRequest.getHierarchies());
		}
		
		if (res != null) {
			if (whereDataRequest != null) {
				//System.out.println("---------------------- res");
				//res.print(System.out);
				//System.out.println("---------------------- where");
				//where.print(System.out);
				//System.out.println("----------------------");
				res = DataRequest.intersect(res, whereDataRequest);
			}
		} else {
			res = whereDataRequest;
		}
			
		// Add default members
		for (Dimension d : cube.getDimensions())
			if (d.getDefaultHierarchy() != null && d.getDefaultHierarchy().getDefaultMember() != null)
				res.addDefaultFilter(d.getDefaultHierarchy().getDefaultMember());
		
		
		res.setAxes(axesList);
		
		return res;
	}

}
