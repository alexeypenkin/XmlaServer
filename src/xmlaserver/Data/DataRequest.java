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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import xmlaserver.Olap.Hierarchy;
import xmlaserver.Olap.Level;
import xmlaserver.Olap.Member;


public class DataRequest {
	
	private List<AtomicRequest> atomicRequestList;
	private List<Axis> axes;
	
	public DataRequest () {
		atomicRequestList = new ArrayList<AtomicRequest>();
		axes = new ArrayList<Axis>();
	}
	
	public DataRequest (DataRequest dataRequest) {
		atomicRequestList = new ArrayList<AtomicRequest>(dataRequest.getAtomicRequestList());
		axes = new ArrayList<Axis>(dataRequest.getAxes());
	}

	public DataRequest (AtomicRequest atomicRequest) {
		atomicRequestList = new ArrayList<AtomicRequest>();
		atomicRequestList.add(atomicRequest);
		axes = new ArrayList<Axis>();
	}
	
	public void addAxis(Axis axis) {
		axes.add(axis);
	}
	
	public void setAxes(List<Axis> axesList) {
		this.axes = axesList;  
	}
	
	public List<Axis> getAxes() {
		return axes;
	}
	
	public void addTupleToAxes(DataTuple tuple) {
		for (Axis a: axes)
			a.addTuple(tuple);
	}
	
	public void add(AtomicRequest atomicRequest) {
		atomicRequestList.add(atomicRequest);
	}

	public void addAll(DataRequest dataRequest) {
		atomicRequestList.addAll(dataRequest.getAtomicRequestList());
	}
	
	public AtomicRequest getSingleAtomicRequest() {
		if (atomicRequestList.size() != 1)
			return null;
		
		return atomicRequestList.get(0);
	}
	
	public Level getSingleLevel() {
		// Expecting only a single AtomicRequest
		if (atomicRequestList.size() != 1)
			return null;
		
		return atomicRequestList.get(0).getSingleLevel();
	}	
	
	public void addLevel(Level level) {
		for(AtomicRequest ar : atomicRequestList)
			ar.addLevel(level);
	}
	
	public void clearLevels() {
		for(AtomicRequest ar : atomicRequestList) {
			ar.clearLevels();
		}
	}
	
	public void clearMeasures() {
		for(AtomicRequest ar : atomicRequestList) {
			ar.clearMeasures();
		}
	}
	
	public Set<Hierarchy> getHierarchies() {
		Set<Hierarchy> hierarchies = null;
		for(AtomicRequest ar : atomicRequestList) {
			if (hierarchies == null)
				hierarchies = ar.getHierarchies();
			else
				hierarchies.addAll(ar.getHierarchies());
		}
		return hierarchies;
	}
	
	public static DataRequest intersect(DataRequest dataRequest, AtomicRequest atomicRequest) {
		//System.out.println("------------------------- DataRequest.intersect(AtomicRequest)");
		//print(System.out);
		//atomicRequest.print(System.out);		
		//   this       AR
		// (A + B + C) x D = AxD + BxD + CxD
		
		DataRequest res = new DataRequest();
		for(AtomicRequest ar : dataRequest.getAtomicRequestList()) {
			res.add(AtomicRequest.intersect(ar, atomicRequest));
		}
		return res;
	}
	
	public static DataRequest intersect(DataRequest dataRequest1, DataRequest dataRequest2) {
		//System.out.println("------------------------- DataRequest.intersect(DataRequest)");
		//print(System.out);
		//dataRequest.print(System.out);
		//   this      dataRequest 
		// (A + B + C) x (D + E) = AxD + BxD + CxD + AxE + BxE + CxE
		
		DataRequest res = new DataRequest();
		for(AtomicRequest ar : dataRequest2.getAtomicRequestList()) {
			res.addAll(intersect(dataRequest1, ar));
		}
		return res;
	}
	
	public static DataRequest union(DataRequest dataRequest, AtomicRequest atomicRequest) {
		//System.out.println("------------------------- DataRequest.intersect(AtomicRequest)");
		//print(System.out);
		//atomicRequest.print(System.out);		
		//   this       AR
		// (A + B + C) x D = AxD + BxD + CxD
		
		DataRequest res = new DataRequest();
		for(AtomicRequest ar : dataRequest.getAtomicRequestList()) {
			res.add(AtomicRequest.union(ar, atomicRequest));
		}
		return res;
	}
	
	public static DataRequest union(DataRequest dataRequest1, DataRequest dataRequest2) {
		//System.out.println("------------------------- DataRequest.intersect(DataRequest)");
		//print(System.out);
		//dataRequest.print(System.out);
		// dataRequest1   dataRequest2 
		//  (A + B + C)  x  (D + E) = (A + B + C) x D + (A + B + C) x E
		
		DataRequest res = new DataRequest();
		for(AtomicRequest ar : dataRequest2.getAtomicRequestList()) {
			res.addAll(union(dataRequest1, ar));
		}
		return res;
	}
	
	public List<AtomicRequest> getAtomicRequestList() {
		return atomicRequestList;
	}
	
	public void print(PrintStream out) {
		for (AtomicRequest ar : atomicRequestList) {
			ar.print(out);
			out.println();
		}
	}

	public void addDefaultFilter(Member defaultMember) {
		for (AtomicRequest ar : atomicRequestList)
			ar.addDefaultFilter(defaultMember);
	}
}
