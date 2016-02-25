
package com.efficio.fieldbook.web.common.bean;

import org.generationcp.middleware.domain.dms.DesignTypeItem;

import com.efficio.fieldbook.web.trial.bean.EnvironmentData;

public class GenerateDesignInput {

	private EnvironmentData environmentData;
	private DesignTypeItem selectedDesignType;
	private Integer startingEntryNo;
	private Integer startingPlotNo;
	private Boolean hasNewEnvironmentAdded;

	public GenerateDesignInput() {
		this.environmentData = new EnvironmentData();
		this.selectedDesignType = new DesignTypeItem();
		this.startingEntryNo = 1;
		this.startingPlotNo = 1;
		this.hasNewEnvironmentAdded = false;
	}

	public GenerateDesignInput(final EnvironmentData environmentData, final DesignTypeItem selectedDesignType,
			final Integer startingEntryNo, final Integer startingPlotNo, final Boolean hasNewEnvironmentAdded) {
		super();
		this.environmentData = environmentData;
		this.selectedDesignType = selectedDesignType;
		this.startingEntryNo = startingEntryNo;
		this.startingPlotNo = startingPlotNo;
		this.hasNewEnvironmentAdded = hasNewEnvironmentAdded;
	}

	public EnvironmentData getEnvironmentData() {
		return this.environmentData;
	}

	public void setEnvironmentData(final EnvironmentData environmentData) {
		this.environmentData = environmentData;
	}

	public DesignTypeItem getSelectedDesignType() {
		return this.selectedDesignType;
	}

	public void setSelectedDesignType(final DesignTypeItem selectedDesignType) {
		this.selectedDesignType = selectedDesignType;
	}

	public Integer getStartingEntryNo() {
		return this.startingEntryNo;
	}

	public void setStartingEntryNo(final Integer startingEntryNo) {
		this.startingEntryNo = startingEntryNo;
	}

	public Integer getStartingPlotNo() {
		return this.startingPlotNo;
	}

	public void setStartingPlotNo(final Integer startingPlotNo) {
		this.startingPlotNo = startingPlotNo;
	}

	public Boolean getHasNewEnvironmentAdded() {
		return this.hasNewEnvironmentAdded;
	}

	public void setHasNewEnvironmentAdded(final Boolean hasNewEnvironmentAdded) {
		this.hasNewEnvironmentAdded = hasNewEnvironmentAdded;
	}
}
