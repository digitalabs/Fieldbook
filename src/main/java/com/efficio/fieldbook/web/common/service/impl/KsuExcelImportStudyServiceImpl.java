package com.efficio.fieldbook.web.common.service.impl;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.service.DataKaptureImportStudyService;

public class KsuExcelImportStudyServiceImpl implements DataKaptureImportStudyService {
	
	private static final Logger LOG = LoggerFactory.getLogger(KsuExcelImportStudyServiceImpl.class);

	@Override
	public ImportResult importWorkbook(Workbook workbook, String filename,
			OntologyService ontologyService,
			FieldbookService fieldbookMiddlewareService)
			throws WorkbookParserException {
		// TODO Auto-generated method stub
		return null;
	}

}
