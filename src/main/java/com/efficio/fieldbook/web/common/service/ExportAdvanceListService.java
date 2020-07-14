
package com.efficio.fieldbook.web.common.service;

import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.GermplasmExportService;


public interface ExportAdvanceListService {

	public FileExportInfo exportAdvanceGermplasmList(String delimitedAdvanceGermplasmListIds, String studyName, GermplasmExportService germplasmExportServiceImpl,
			String formatType);

}
