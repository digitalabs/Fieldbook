package com.efficio.fieldbook.web.common.form;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementVariable;

import com.efficio.fieldbook.web.util.SettingsUtil;

public class ReviewDetailsOutOfBoundsForm {
	
	private List<MeasurementVariable> measurementVariables;
	
	private MeasurementVariable measurementVariable;
	
	private int traitIndex;
	
	private int traitSize;
	
	private int traitTermId;

	public int getTraitIndex() {
		return traitIndex;
	}

	public void setTraitIndex(int traitIndex) {
		this.traitIndex = traitIndex;
	}
	
	public MeasurementVariable getMeasurementVariable() {
		return measurementVariable;
	}

	public void setMeasurementVariable(MeasurementVariable measurementVariable) {
		this.measurementVariable = measurementVariable;
		this.traitTermId = measurementVariable.getTermId();
	}

	public int getTraitTermId() {
		return traitTermId;
	}

	public void setTraitTermId(int traitTermId) {
		this.traitTermId = traitTermId;
	}
	
	public List<MeasurementVariable> getArrangeMeasurementVariables(){
		return getMeasurementVariables();
	}

	public List<MeasurementVariable> getMeasurementVariables() {
		return measurementVariables;
	}

	public void setMeasurementVariables(List<MeasurementVariable> measurementVariables) {
		this.measurementVariables = measurementVariables;
	}

	public int getTraitSize() {
		return traitSize;
	}

	public void setTraitSize(int traitSize) {
		this.traitSize = traitSize;
	}

	

}
