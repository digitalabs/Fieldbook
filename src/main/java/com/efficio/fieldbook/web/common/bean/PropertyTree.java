
package com.efficio.fieldbook.web.common.bean;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.StandardVariableSummary;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.TraitClassReference;

public class PropertyTree {

	private TraitPojo traitClass;

	private final PropertyReference propertyReference;

	private List<StandardVariableSummary> standardVariables = new ArrayList<StandardVariableSummary>();

	public PropertyTree(PropertyReference propertyReference) {
		this.propertyReference = propertyReference;
	}

	public Integer getPropertyId() {
		return this.propertyReference.getId();
	}

	public String getName() {
		return this.propertyReference.getName();
	}

	public String getDescription() {
		return this.propertyReference.getDescription();
	}

	public TraitPojo getTraitClass() {
		return this.traitClass;
	}

	public void setTraitClass(TraitClassReference traitClassReference) {
		this.traitClass = new TraitPojo();
		this.traitClass.setTraitClassId(traitClassReference.getId());
		this.traitClass.setTraitClassName(traitClassReference.getName());
	}

	public List<StandardVariableSummary> getStandardVariables() {
		return this.standardVariables;
	}

	public void setStandardVariables(List<StandardVariableSummary> standardVariables) {
		this.standardVariables = standardVariables;
	}
}
