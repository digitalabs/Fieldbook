
package com.efficio.fieldbook.web.common.bean;

import org.generationcp.middleware.domain.dms.DesignTypeItem;

import com.efficio.fieldbook.web.trial.bean.EnvironmentData;

public class GeneratePresetDesignInput {

	private EnvironmentData environmentData;
	private DesignTypeItem selectedDesignType;

	public GeneratePresetDesignInput() {
		this.environmentData = new EnvironmentData();
		this.selectedDesignType = new DesignTypeItem();
	}

	public GeneratePresetDesignInput(final EnvironmentData environmentData, final DesignTypeItem selectedDesignType) {
		super();
		this.environmentData = environmentData;
		this.selectedDesignType = selectedDesignType;
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

}
