
package com.efficio.fieldbook.web.trial.bean;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 7/9/2014 Time: 5:04 PM
 */
public class TrialData {

	private BasicDetails basicDetails;
	private TrialSettingsBean trialSettings;
	private InstanceInfo instanceInfo;
	private TreatmentFactorTabBean treatmentFactors;
	private ExpDesignParameterUi experimentalDesign;
	private String columnOrders;

	public TrialData() {
	}

	public BasicDetails getBasicDetails() {
		return this.basicDetails;
	}

	public void setBasicDetails(BasicDetails basicDetails) {
		this.basicDetails = basicDetails;
	}

	public TrialSettingsBean getTrialSettings() {
		return this.trialSettings;
	}

	public void setTrialSettings(TrialSettingsBean trialSettings) {
		this.trialSettings = trialSettings;
	}

	public InstanceInfo getInstanceInfo() {
		return this.instanceInfo;
	}

	public void setInstanceInfo(InstanceInfo instanceInfo) {
		this.instanceInfo = instanceInfo;
	}

	public TreatmentFactorTabBean getTreatmentFactors() {
		return this.treatmentFactors;
	}

	public void setTreatmentFactors(TreatmentFactorTabBean treatmentFactors) {
		this.treatmentFactors = treatmentFactors;
	}

	public ExpDesignParameterUi getExperimentalDesign() {
		return this.experimentalDesign;
	}

	public void setExperimentalDesign(ExpDesignParameterUi experimentalDesign) {
		this.experimentalDesign = experimentalDesign;
	}

	public String getColumnOrders() {
		return this.columnOrders;
	}

	public void setColumnOrders(String columnOrders) {
		this.columnOrders = columnOrders;
	}

}
