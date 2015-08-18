package com.efficio.fieldbook.web.common.service;

		import com.efficio.fieldbook.web.common.bean.ImportResult;
		import org.generationcp.middleware.domain.etl.Workbook;
		import org.generationcp.middleware.exceptions.WorkbookParserException;

public interface KsuCsvImportStudyService {
	ImportResult importWorkbook(Workbook workbook, String filename, String originalFilename) throws WorkbookParserException;
}
