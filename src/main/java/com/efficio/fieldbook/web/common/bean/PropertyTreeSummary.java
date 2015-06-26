package com.efficio.fieldbook.web.common.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.domain.ontology.OntologyVariableSummary;
import org.generationcp.middleware.domain.ontology.Property;

/**
 * Created by cyrus on 6/17/15.
 */
public class PropertyTreeSummary implements Serializable {

	private final Property property;
	private final List<OntologyVariableSummary> variableSummaryList;

	public PropertyTreeSummary(Property property, List<OntologyVariableSummary> variableSummaryList) {
		this.property = property;
		this.variableSummaryList = variableSummaryList;
	}

	public Integer getPropertyId() {
		return this.property.getId();
	}

	public String getName() {
		return this.property.getName();
	}

	public String getDescription() {
		return this.property.getDefinition();
	}

	public Set<String> getClasses() {
		return this.property.getClasses();
	}

	public String getClassesStr() {
		return StringUtils.join(this.getClasses(), ",");
	}

	public List<OntologyVariableSummary> getStandardVariables() {
		return variableSummaryList;
	}
}
