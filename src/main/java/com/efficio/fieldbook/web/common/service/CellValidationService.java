package com.efficio.fieldbook.web.common.service;

import org.generationcp.middleware.domain.etl.MeasurementData;

public interface CellValidationService {

	boolean validate(MeasurementData cell);
}
