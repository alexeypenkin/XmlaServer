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

package xmlaserver.XmlaServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextConstants {
	
	public static Map<String,String> textCache;
	
	public static synchronized String getText(String fileName) {
		if (textCache.containsKey(fileName))
			return textCache.get(fileName);

		try {
			String text = Tools.readFile(fileName);
			textCache.put(fileName, text);
			return text;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	static {
		textCache = new HashMap<String,String>();
	}
	

}
