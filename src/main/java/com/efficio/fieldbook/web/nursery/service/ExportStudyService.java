package com.efficio.fieldbook.web.nursery.service;

import org.generationcp.middleware.domain.etl.Workbook;


public interface ExportStudyService {

	void export(Workbook workbook, String filename);
}
