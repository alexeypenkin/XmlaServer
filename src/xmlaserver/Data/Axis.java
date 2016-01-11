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
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import xmlaserver.Olap.Hierarchy;
import xmlaserver.Olap.Member;


public class Axis {
	
	private List<Hierarchy> hierarchies;
	private String name;
	private TreeSet<AxisTuple> tuples;
	
	public Axis(String name) {
		this.name = name;
		this.hierarchies = new ArrayList<Hierarchy>();
		this.tuples = new TreeSet<AxisTuple>();
	}
	
	public void addHierarchy(Hierarchy hierarchy) {
		hierarchies.add(hierarchy);
	}
	
	public void addAllHierarchies(Collection<Hierarchy> hierarchies) {
		this.hierarchies.addAll(hierarchies);
	}
	
	public List<Hierarchy> getHierarchies() {
		return hierarchies;
	}
	
	public String getName() {
		return name;
	}
	
	public void addTuple(DataTuple tuple) {
		AxisTuple at = new AxisTuple(hierarchies.size());
		for (Member m : tuple.getMembers()) {
			int index = hierarchies.indexOf(m.getHierarchy());
			if (index >= 0) 
				at.setMember(index, m);
		}
		tuples.add(at);
	}
	
	public void addAxisTuple(AxisTuple axisTuple) {
		tuples.add(axisTuple);
	}
	
	public void addAllTuples(Collection<AxisTuple> tuples) {
		this.tuples.addAll(tuples);
	}
	
	public TreeSet<AxisTuple> getTuples() {
		return tuples;
	}
	
	public int getHierarchyIndex(Hierarchy hierarchy) {
		return hierarchies.indexOf(hierarchy);
	}
	
}
