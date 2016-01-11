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

public class Tools {

	public static String space(int level) {
		StringBuffer outputBuffer = new StringBuffer(level*4);
		for (int i = 0; i < level-1; i++){
   			outputBuffer.append(" |  ");
		}
		if (level > 0)
			outputBuffer.append(" +--");
		return outputBuffer.toString();
	}
}
