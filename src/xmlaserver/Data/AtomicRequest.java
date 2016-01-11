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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import xmlaserver.Olap.Dimension;
import xmlaserver.Olap.Hierarchy;
import xmlaserver.Olap.Level;
import xmlaserver.Olap.Measure;
import xmlaserver.Olap.Member;


public class AtomicRequest {
	
	private Set<Measure> measures;
	private Map<Dimension,Condition> filters;  // represent splicer (or where)
	private Set<Level> levels;
	
	public AtomicRequest() {
		measures = new HashSet<Measure>();
		filters = new HashMap<Dimension,Condition>();
		levels = new LinkedHashSet<Level>();
	}
	
	public AtomicRequest(AtomicRequest ar) {
		measures = new HashSet<Measure>(ar.getMeasures());
		filters = new HashMap<Dimension,Condition>(ar.getFilters());
		levels = new LinkedHashSet<Level>(ar.getLevels());
	}
	
	public Level getSingleLevel() {
		// Expecting only a single filter
		if (levels.size() != 1)
			return null;
		
		return levels.iterator().next();
	}
	
	public Set<Measure> getMeasures() {
		return measures;
	}
	
	public void addMeasure(Measure measure) {
		measures.add(measure);
	}
	
	public void clearMeasures() {
		measures.clear();
	}
	
	public void unionAllMeasures(Set<Measure> measures) {
		this.measures.addAll(measures);
	}
	
	public void intersectAllMeasures(Set<Measure> measures) {
		this.measures.retainAll(measures);
	}
	
	public void unionAllFilters(Map<Dimension,Condition> newFilters) {
		Set<Dimension> allDimensions = new HashSet<Dimension>(filters.keySet());
		allDimensions.addAll(newFilters.keySet());
				
		for (Dimension d : allDimensions) {
			if (!filters.containsKey(d))
				filters.put(d, newFilters.get(d));
			else if (newFilters.containsKey(d))
				filters.put(d, new LogicalCondition(LogicalCondition.Type.OR, filters.get(d), newFilters.get(d)));
		}
	}
	
	public void intersectAllFilters(Map<Dimension,Condition> newFilters) {
		Set<Dimension> allDimensions = new HashSet<Dimension>(filters.keySet());
		allDimensions.addAll(newFilters.keySet());
				
		for (Dimension d : allDimensions) {
			if (!filters.containsKey(d))
				filters.put(d, newFilters.get(d));
			else if (newFilters.containsKey(d))
				filters.put(d, new LogicalCondition(LogicalCondition.Type.AND, filters.get(d), newFilters.get(d)));
		}
	}
	
	public Map<Dimension,Condition> getFilters() {
		return filters;
	}
	
	public void putFilter(Dimension dimension, Condition condition) {
		// TODO: check for the same dimension - combine lists ???
		this.filters.put(dimension, condition);
	}
	
	public void putAllFilters(Map<Dimension,Condition> filters) {
		// TODO: check for the same dimension - combine lists ???
		this.filters.putAll(filters);
	}
	
	public Set<Level> getLevels() {
		return levels;
	}
	
	public void addLevel(Level level) {
		levels.add(level);
	}
	
	public void addAllLevels(Set<Level> levels) {
		this.levels.addAll(levels);
	}
	
	public void clearLevels() {
		levels.clear();
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	  
	    final AtomicRequest other = (AtomicRequest)obj;
	    return measures.equals(other.getMeasures()) &&
	    	filters.equals(other.getFilters()) &&
	    	levels.equals(other.getLevels());
	}
	
    @Override
    public int hashCode() {
    	int h = measures.hashCode();
    	h = h * 31 + filters.hashCode();
    	h = h * 31 + levels.hashCode();
        return h; 
    }
	
	public static AtomicRequest intersect(AtomicRequest ar1, AtomicRequest ar2) {
		//System.out.println("------------------------- AtomicRequest.combine(AtomicRequest)");
		//print(System.out);
		//ar.print(System.out);	
		
		AtomicRequest res = new AtomicRequest(ar1);
		//System.out.println("------------------------- ");
		//res.print(System.out);
		
		res.unionAllMeasures(ar2.getMeasures());   // intersectAllMeasures is incorrect here
		res.intersectAllFilters(ar2.getFilters());
		res.addAllLevels(ar2.getLevels());
		//res.addAllParentLevels(ar2.getParentLevels());

		//System.out.println("------------------------- 222 ");
		//res.print(System.out);
		
		return res;
	}
	
	public static AtomicRequest union(AtomicRequest ar1, AtomicRequest ar2) {
		//System.out.println("------------------------- AtomicRequest.combine(AtomicRequest)");
		//print(System.out);
		//ar.print(System.out);	
		
		AtomicRequest res = new AtomicRequest(ar1);
		//System.out.println("------------------------- ");
		//res.print(System.out);
		
		res.unionAllMeasures(ar2.getMeasures());
		res.unionAllFilters(ar2.getFilters());
		res.addAllLevels(ar2.getLevels());
		//res.addAllParentLevels(ar2.getParentLevels());

		//System.out.println("------------------------- 222 ");
		//res.print(System.out);
		
		return res;
	}
	
	public void print(PrintStream out) {
		out.println("measures: " + measures);
		out.println("filters: " + filters);
		out.println("levels: " + levels);
	}

	public Set<Hierarchy> getHierarchies() {
		Set<Hierarchy> hierarchies = new LinkedHashSet<Hierarchy>();
		for (Level l : levels) {
			hierarchies.add(l.getHierarchy());
		}
		return hierarchies;
	}

	public void addDefaultFilter(Member defaultMember) {
		if (!filters.containsKey(defaultMember.getDimension()))
			filters.put(defaultMember.getDimension(), new MemberCondition(defaultMember));
	}
}
