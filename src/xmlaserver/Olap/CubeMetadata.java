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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CubeMetadata {
	
	private Cube cube;
	private String caption;
	private String uniqueName;
	private String description;
	private CubeMetadata parent;
	private int ordinal;			// Sequential number. First is 1, second is 2, and so on
	private int cardinality;		// Number of members
	private boolean visible;
	
	private Map<String,CubeMetadata> namesMap;
	
	public CubeMetadata(Cube cube, String caption, String uniqueName, CubeMetadata parent) {
		this.cube = cube;
		this.caption = caption;
		this.uniqueName = uniqueName;
		this.description = "";
		this.parent = parent;
		this.ordinal = 0;
		this.cardinality = 1;   // TODO: may need to set it to actual value
		this.visible = true;
		
		namesMap = new HashMap<String,CubeMetadata>();
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
	
	public String getCaption() {
		return caption;
	}
	
	public String getUniqueName() {
		return uniqueName;
	}
	
	public abstract String getFullUniqueName();
	/*
	 {
		if (parent != null)
			return parent.getFullUniqueName() + ".[" + getUniqueName() + "]";
		else
			return "[" + getUniqueName() + "]";
	}
	*/
	
	public void registerChild(String name, CubeMetadata cubeMetadata) throws Exception {
		if (namesMap.containsKey(name))
			throw new Exception("Name already registered: " + namesMap.get(name));
		
		namesMap.put(name, cubeMetadata);
	}
	
	public CubeMetadata resolveName(String name) {
		//System.out.println("resolveName " + getClass().getName() + " " + name);
		return namesMap.get(name);
	}

	public CubeMetadata resolveMultipartName(String multipartName) throws Exception {
		return resolveMultipartName(splitId(multipartName));	
	}
	
	public CubeMetadata resolveMultipartName(List<String> names) throws Exception {
		if (names == null)
			return null;
		if (names.isEmpty())
			return null;
		
		CubeMetadata cm = resolveName(names.get(0));
		
		if (names.size() == 1)
			return cm;
		
		if (cm == null)
			return null;
		
		return cm.resolveMultipartName(names.subList(1, names.size()));
	}
	
	public Cube getCube() {
		return cube;
	}
	
	public String getDescription() {
		return description;
	}
	/*
	public String getCaption2() {
		return caption;
	}
	*/
	public int getOrdinal() {
		return ordinal;
	}
	
	public int getCardinality() {
		return cardinality;
	}
	
	public boolean getVisible() {
		return visible;
	}
	
	@Override
	public String toString() {
		//return getClass().getName() + " " + getFullUniqueName();
		return getFullUniqueName() + " (" + caption + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	  
	    final CubeMetadata other = (Member) obj;
	    return getFullUniqueName().equals(other.getFullUniqueName());
	}
	
    @Override
    public int hashCode() {
        return getFullUniqueName().hashCode();
    }
	
	public static String printProperty(String property, String value) {
		return "<" + property + ">" + value + "</" + property + ">\n";
	}
	
	public static List<String> splitId(String multipartId) throws Exception {
		
		List<String> res = new ArrayList<String>();
		StringBuffer firstPart = new StringBuffer(255);
		
		// Parsing Modes:
		// 0 - Start of the next ID
		// 1 - Identifier enquoted in square brackets like '[Date]'
		// 2 - Normal, un-enquoted identifier 'Date'
		// 3 - expecting '.' after ']'
		
		int mode = 0;
		int ptr = 0;
		for (ptr=0; ptr < multipartId.length(); ptr++) {
			char c = multipartId.charAt(ptr);
			
			if (mode == 0) {
				if (c == '[')
					mode = 2; // Identifier enquoted in square brackets
				else {
					firstPart.append(c);
					mode = 1;
				}

			} else if (mode == 1) {  // Normal, un-enquoted identifier 'Date'
				if ( (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' )
					firstPart.append(c);
				else if (c == '.') {
					res.add(firstPart.toString());
					firstPart.setLength(0);
					mode = 0;
				} else
					throw new Exception("Multipart ID has unexpected character '" + c + "' at position " + ptr + ": " + multipartId);

			} else if (mode == 2) {  // Identifier enquoted in square brackets '[Date]'
				if (c == '[')
					throw new Exception("Multipart ID has unexpected character '" + c + "' at position " + ptr + ": " + multipartId);
				else if (c != ']')
					firstPart.append(c);
				else
					mode = 3;
				
			} else if (mode == 3) {  // expecting '.'
				if (c == '.') {
					res.add(firstPart.toString());
					firstPart.setLength(0);
					mode = 0;
				} else
					throw new Exception("Multipart ID has unexpected character '" + c + "' at position " + ptr + ": " + multipartId);
			}
		}
		
		if (mode == 2 || mode == 0)
			throw new Exception("Multipart ID has unexpected character <EOL> at position " + ptr + ": " + multipartId);
		else if (mode == 1 || mode == 3)
			res.add(firstPart.toString());
		
		return res;
	}
}
