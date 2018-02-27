
package com.efficio.fieldbook.web.common.service;

import org.generationcp.commons.service.GermplasmExportService;

import com.efficio.fieldbook.util.FileExportInfo;

public interface ExportAdvanceListService {

	public FileExportInfo exportAdvanceGermplasmList(String delimitedAdvanceGermplasmListIds, String studyName, GermplasmExportService germplasmExportServiceImpl,
			String formatType);

	FileExportInfo exportStockList(Integer stockListId, GermplasmExportService germplasmExportServiceImpl);
}
