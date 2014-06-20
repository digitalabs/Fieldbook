/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.nursery.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

public class PossibleValuesCache implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<Integer, List<ValueReference>> possibleValuesMap;
	
	public void clear() {
		this.possibleValuesMap = null;
	}
	
	public Map<Integer, List<ValueReference>> getPossibleValuesMap() {
		if (this.possibleValuesMap == null) {
			this.possibleValuesMap = new HashMap<Integer, List<ValueReference>>();
		}
		return this.possibleValuesMap;
	}
	
	public List<ValueReference> getPossibleValues(int id) {
		return getPossibleValuesMap().get(id); 
	}
	
	public void addPossibleValues(int id, List<ValueReference> possibleValues) {
		getPossibleValuesMap().put(id, possibleValues);
	}

}
