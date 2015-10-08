
package com.efficio.fieldbook.web.common.service;

import java.io.File;

import org.generationcp.commons.service.GermplasmExportService;

public interface ExportAdvanceListService {

	public File exportAdvanceGermplasmList(String delimitedAdvanceGermplasmListIds, String studyName, GermplasmExportService germplasmExportServiceImpl,
			String formatType);

	File exportStockList(Integer stockListId, GermplasmExportService germplasmExportServiceImpl);
}
