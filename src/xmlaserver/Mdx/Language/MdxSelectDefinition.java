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

package xmlaserver.Mdx.Language;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MdxSelectDefinition {

	private List<MdxAxisDefinition> axises;
	private List<String> cellProperties;
	private MdxId cube;
	private MdxSelectDefinition subquery;
	private MdxExpression where;
	
	public MdxSelectDefinition() {
		cellProperties = new ArrayList<String>();
		axises = new ArrayList<MdxAxisDefinition>();
		subquery = null;
	}
	
	public void setCube(MdxId value) {
		cube = value;
	}
	
	public MdxId getCube() {
		if (subquery != null)
			return subquery.getCube();
		else
			return cube;
	}
	
	public void setSubquery(MdxSelectDefinition value) {
		subquery = value;
	}
	
	public MdxSelectDefinition getSubquery() {
		return subquery;
	}
	
	public void setWhere(MdxExpression value) {
		where = value;
	}
	
	public MdxExpression getWhere() {
		return where;
	}
	
	public List<MdxAxisDefinition> getAxises() {
		return axises;
	}
	
	public MdxAxisDefinition getAxis(int axisNumber) {
		return axises.get(axisNumber);
	}
	
	
	public void addCellProperty(String value) {
		cellProperties.add(value);
	}
	
	public void addAxis(MdxAxisDefinition value) {
		axises.add(value);
	}	
	
	public void print(int level, PrintStream out) {
		out.print(Tools.space(level));
		out.println("SELECT");
		
		for (MdxAxisDefinition axis : axises)
			axis.print(level+1, out);

		if (subquery != null) {
			out.print(Tools.space(level));
			out.println("FROM");
			subquery.print(level+1, out);
		} else {
			out.print(Tools.space(level));
			out.println("FROM " + cube);
		}
		
		if (where != null) {
			out.print(Tools.space(level));
			out.println("WHERE");
			where.printTree(level+1, out);
		}
		
		out.print(Tools.space(level));
		out.println("CELL PROPERTIES " + cellProperties);
	}
	
	public void print(PrintStream out) {
		print(0, out);
	}
}
