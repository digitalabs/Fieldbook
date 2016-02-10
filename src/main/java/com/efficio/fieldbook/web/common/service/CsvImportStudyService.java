package com.efficio.fieldbook.web.common.service;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;

import com.efficio.fieldbook.web.common.bean.ImportResult;

public interface CsvImportStudyService {
	ImportResult importWorkbook(Workbook workbook, String filename, String originalFilename) throws WorkbookParserException;
}
