package com.efficio.fieldbook.web.common.service;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;

import com.efficio.fieldbook.web.common.exception.DesignValidationException;

public interface DesignImportService {

	List<MeasurementRow> generateDesign() throws DesignValidationException;
	
	List<MeasurementVariable> getDesignMeasurementVariables();
	
}
