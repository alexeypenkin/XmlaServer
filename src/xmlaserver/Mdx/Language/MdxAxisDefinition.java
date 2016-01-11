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

public class MdxAxisDefinition {
	
	private boolean nonEmpty;
	private MdxExpression expression;
	private List<String> properties;
	private String on;
	
	public MdxAxisDefinition() {
		properties = new ArrayList<String>();
		nonEmpty = false;
	}
	
	public void setNonEmpty(boolean value) {
		nonEmpty = value;
	}
	
	public void setExpression(MdxExpression value) {
		expression = value;
	}
	
	public MdxExpression getExpression() {
		return expression;
	}
	
	public void addProperty(String value) {
		properties.add(value);
	}
	
	public void print(int level, PrintStream out) {
		out.print(Tools.space(level) + "MdxAxisDefinition: ");
		if (nonEmpty) out.print("NON EMPTY; ");
		out.print(properties.toString());
		out.println("; ON " + on);
		expression.printTree(level+1, out);
	}
	
	public void setOn(String value) {
		on = value;
	}

}
