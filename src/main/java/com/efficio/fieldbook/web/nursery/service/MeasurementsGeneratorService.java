package com.efficio.fieldbook.web.nursery.service;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementRow;

import com.efficio.fieldbook.web.nursery.bean.UserSelection;

public interface MeasurementsGeneratorService {

	List<MeasurementRow> generateMeasurementRows(UserSelection userSelection);
	
}
