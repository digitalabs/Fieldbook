package com.efficio.fieldbook.web.common.service;

import java.util.List;
import java.util.Set;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;

import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;

public interface DesignImportService {

	List<MeasurementRow> generateDesign(Workbook workbook, DesignImportData designImportData) throws DesignValidationException;
	
	Set<MeasurementVariable> getDesignMeasurementVariables(Workbook workbook, DesignImportData designImportData);

	void validateDesignData(DesignImportData designImportData) throws DesignValidationException;
	
	boolean areTrialInstancesMatchTheSelectedEnvironments(Workbook workbook, DesignImportData designImportData);
	
}
