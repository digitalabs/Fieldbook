package com.efficio.fieldbook.web.nursery.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;


@Service
public class MeasurementsGeneratorServiceImpl implements MeasurementsGeneratorService {

	//TODO: currently used for generating test data.. 
	//but in the future can be used to call a Middleware service that will 
	//generate the measurements row
	public List<MeasurementRow> generateMeasurementRows(UserSelection userSelection) {
		Workbook workbook = userSelection.getWorkbook();
		List<MeasurementRow> rows = new ArrayList<MeasurementRow>();
		List<ImportedGermplasm> germplasms = null;
		if (userSelection.getImportedGermplasmMainInfo() != null
				&& userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList() != null
				&& userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null 
				&& userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().size() > 0) {
			germplasms = userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		}
				
		int count = germplasms != null ? germplasms.size() : 20;
		for (int i = 0; i < count; i++) {
			MeasurementRow row = new MeasurementRow();
			List<MeasurementData> rowCells = new ArrayList<MeasurementData>();
			row.setDataList(rowCells);
			
			if (workbook.getFactors() != null && workbook.getFactors().size() > 0) {
				for (MeasurementVariable factor : workbook.getFactors()) {
					if (germplasms != null) {
						if (factor.getName().equals("DESIG")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getDesig()));
						} else if (factor.getName().equals("GID")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getGid()));
						} else if (factor.getName().equals("SOURCE")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getSource()));
						} else if (factor.getName().equals("CROSS")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getCross()));
						} else if (factor.getName().startsWith("ENTRY")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getEntryId().toString()));
						} else if (factor.getName().startsWith("CHECK")) {
							rowCells.add(new MeasurementData(factor.getName(), germplasms.get(i).getEntryCode()));
						} else {
							rowCells.add(new MeasurementData(factor.getName(), String.valueOf(i)));
						}
					} else {
						rowCells.add(new MeasurementData(factor.getName(), String.valueOf(i)));
					}
				}
			}
			
			if (workbook.getVariates() != null && workbook.getVariates().size() > 0) {
				for (MeasurementVariable variate : workbook.getVariates()) {
					rowCells.add(new MeasurementData(variate.getName(), String.valueOf(i)));
				}
			}
			rows.add(row);
		}
		
		return rows;
	}
	

}
