package com.efficio.fieldbook.web.ontology.bean;

import org.generationcp.middleware.domain.dms.StandardVariableSummary;


public class OntologyUsage implements Comparable {
	
	private StandardVariableSummary standardVariable;
	
	private Long projectCount;
	
	private Long experimentCount;
	
	public StandardVariableSummary getStandardVariable() {
		return standardVariable;
	}
	
	public void setStandardVariable(StandardVariableSummary standardVariable) {
		this.standardVariable = standardVariable;
	}

	public Long getProjectCount() {
		return projectCount;
	}
	
	public void setProjectCount(Long projectCount) {
		this.projectCount = projectCount;
	}
	
	public Long getExperimentCount() {
		return experimentCount;
	}
	
	public void setExperimentCount(Long experimentCount) {
		this.experimentCount = experimentCount;
	}

	@Override
	public int compareTo(Object other) {
		return this.getExperimentCount() > ((OntologyUsage)other).getExperimentCount() ? 1 : 0;
	}


}
