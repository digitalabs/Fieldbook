package com.efficio.fieldbook.web.common.service.impl;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.common.service.CellValidationService;

public class CellValidationServiceImpl implements CellValidationService {

	@Override
	public boolean validate(MeasurementData cell) {
		if (cell != null && cell.getMeasurementVariable() != null && cell.isEditable() 
				&& (cell.getValue() != null && !cell.getValue().isEmpty() 
					|| cell.getcValueId() != null && !cell.getcValueId().isEmpty())) {
			
			String value = cell.getValue() != null && !cell.getValue().isEmpty() ? cell.getValue() : cell.getcValueId();
			MeasurementVariable variable = cell.getMeasurementVariable();
			if (variable.getDataTypeId() != null) { 
				if (TermId.CATEGORICAL_VARIABLE.getId() == variable.getDataTypeId())  {
					
					
				} else if (TermId.NUMERIC_VARIABLE.getId() == variable.getDataTypeId()) {
					
				}
				
			}
		}
		return false;
	}

}
