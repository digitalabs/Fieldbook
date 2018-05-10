/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package com.efficio.fieldbook.web.trial.bean;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.ontology.DataType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PossibleValuesCache implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<Integer, List<ValueReference>> possibleValuesMap;
	private Map<DataType, List<ValueReference>> possibleValuesByDataTypeMap;
	private Map<Boolean, List<ValueReference>> locationsCacheMap;

	public void clear() {
		this.possibleValuesMap = null;
	}

	public Map<Integer, List<ValueReference>> getPossibleValuesMap() {
		if (this.possibleValuesMap == null) {
			this.possibleValuesMap = new HashMap<>();
		}
		return this.possibleValuesMap;
	}

	public Map<DataType, List<ValueReference>> getPossibleValuesByDataTypeMap() {
		if (this.possibleValuesByDataTypeMap == null) {
			this.possibleValuesByDataTypeMap = new HashMap<>();
		}
		return this.possibleValuesByDataTypeMap;
	}

	public List<ValueReference> getPossibleValues(int id) {
		return this.getPossibleValuesMap().get(id);
	}

	public Map<Boolean, List<ValueReference>> getLocationsCacheMap() {
		if (this.locationsCacheMap == null) {
			this.locationsCacheMap = new HashMap<>();
		}

		return this.locationsCacheMap;
	}

	public List<ValueReference> getLocationsCache(boolean isBreedingLocationsOnly) {
		return this.getLocationsCacheMap().get(isBreedingLocationsOnly);
	}

	public List<ValueReference> getPossibleValuesByDataType(DataType dataType) {
		return this.getPossibleValuesByDataTypeMap().get(dataType);
	}

	public void addPossibleValues(int id, List<ValueReference> possibleValues) {
		this.getPossibleValuesMap().put(id, possibleValues);
	}

	public void addPossibleValuesByDataType(DataType dataType, List<ValueReference> possibleValues) {
		this.getPossibleValuesByDataTypeMap().put(dataType, possibleValues);
	}

	public void addLocations(boolean isBreedingLocationsOnly, List<ValueReference> possibleValues) {
		this.getLocationsCacheMap().put(Boolean.valueOf(isBreedingLocationsOnly), possibleValues);
	}
}
