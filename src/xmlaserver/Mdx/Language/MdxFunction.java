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
import java.util.List;

public class MdxFunction extends MdxExpression {
	
	private MdxFunctionType type;
	private List<MdxExpression> attributes;
	
	public MdxFunction (MdxFunctionType functionType, List<MdxExpression> attributes) {
		this.type = functionType;
		this.attributes = attributes;
	}	
	
	public MdxFunctionType getType() {
		return type;
	}
	
	public List<MdxExpression> getAttributes() {
		return attributes; 
	}
	
	@Override
	public String toString() {
		return type.toString() + "(" +  attributes.toString() + ")";
	}

	@Override
	public void printTree(int level, PrintStream out) {
		out.print(Tools.space(level));
		out.println(type.toString());
		
		for (MdxExpression attr : attributes) {
			attr.printTree(level+1, out);
		}
	}
}
