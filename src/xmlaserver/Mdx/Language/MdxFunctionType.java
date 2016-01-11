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

// https://technet.microsoft.com/en-us/library/aa216769(v=sql.80).aspx
// https://docs.oracle.com/cd/E12825_01/epm.111/esb_techref/frameset.htm?mdx_grammar_rules.htm
public enum MdxFunctionType {
	TUPLE,      // ( <member> [,<member>].. ) 
	SET,        // { <tuple>|<set> [, <tuple>|<set>].. } 
	
	CROSSJOIN,  
	DRILLDOWNLEVEL,
	DRILLDOWNMEMBER,  // DrillDownMember(<set>, <set>) - Drills down the members in a specified set that are present in a second specified set
	HIERARCHIZE,
	MEMBERS
}