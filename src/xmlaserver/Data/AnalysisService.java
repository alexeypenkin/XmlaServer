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

package xmlaserver.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import xmlaserver.Data.AtomicRequest;
import xmlaserver.Data.DataTuple;
import xmlaserver.Olap.Attribute;


public abstract class AnalysisService {
	static Logger log = Logger.getLogger(AnalysisService.class.getName());
	
	private static AnalysisService analysisService = null;
	
	private Map<String,Attribute> attributes;
	private Map<String,Attribute> measureAttributes;
	
	public AnalysisService() throws Exception {
		attributes = new HashMap<String,Attribute>();
		measureAttributes = new HashMap<String,Attribute>();
		
		if (analysisService != null)
			log.warn("AnalysisService instance has been redefined");
		
		analysisService = this;
	}
	
	public static AnalysisService getInstance() {
		return analysisService;
	}
	
	public void putAttribute(Attribute a) {
		attributes.put(a.getName().toUpperCase(), a);
	}
	
	public Attribute getAttribute(String name) {
		return attributes.get(name.toUpperCase());
	}
	
	public Collection<Attribute> getAttributes() {
		return attributes.values();
	}

	public void putMeasureAttribute(Attribute a) {
		measureAttributes.put(a.getName().toUpperCase(), a);
	}
	
	public Attribute getMeasureAttribute(String name) {
		return measureAttributes.get(name.toUpperCase());
	}
	
	public Collection<Attribute> getMeasureAttributes() {
		return measureAttributes.values();
	}
	
	public abstract Map<DataTuple,Object> executeAtomicRequest(AtomicRequest atomicRequest) throws Exception;
}
