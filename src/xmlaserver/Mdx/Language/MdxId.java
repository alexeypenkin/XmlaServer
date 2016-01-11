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

public class MdxId extends MdxExpression {
	
	private List<String> names;
	private MdxIdType type;
	
	public MdxId (List<String> names, MdxIdType type) {
		this.names = names;
		this.type = type;
	}
	
	public List<String> getNames() {
		return names;
	}
	
	@Override
	public String toString() {
		return names.toString();
	}

	@Override
	public void printTree(int level, PrintStream out) {
		out.print(Tools.space(level));
		out.println(names);
	}
}
