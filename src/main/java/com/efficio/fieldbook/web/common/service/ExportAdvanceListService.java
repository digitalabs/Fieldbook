package com.efficio.fieldbook.web.common.service;

import java.io.File;

import org.generationcp.commons.service.ExportService;

public interface ExportAdvanceListService {

	public File exportAdvanceGermplasmList(String delimitedAdvanceGermplasmListIds, String studyName, ExportService exportServiceImpl, String formatType);
	File exportStockList(Integer stockListId, ExportService exportServiceImpl);
}
