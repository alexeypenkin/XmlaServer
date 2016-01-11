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
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tools {

	public static String readFile(String fileName) throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(fileName));
		return new String(encoded, StandardCharsets.ISO_8859_1);
	}
	
	public static void copyCompletely(Reader input, Writer output) throws IOException
	{
		char[] buf = new char[8192];
		while (true)
		{
			int length = input.read(buf);
			if (length < 0)
				break;
			output.write(buf, 0, length);
		}
	}
}
