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

public class Measure extends Member {
	
	// MEASURE_AGGREGATOR = 1
	// DATA_TYPE = 5
	private String defaultFormatString;
	private Attribute attribute;

	public Measure(Level levelMeasures, String caption, String uniqueName, String defaultFormatString, Attribute attribute) throws Exception {
		super(levelMeasures, caption, uniqueName, Member.Type.MDMEMBER_TYPE_UNKNOWN, null);
		
		setDescription(getCube().getCaption() + " Cube - " + caption + " Measure");
		this.defaultFormatString = defaultFormatString;
		this.attribute = attribute;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	
	public String getDefaultFormatString() {
		return defaultFormatString;
	}
}
