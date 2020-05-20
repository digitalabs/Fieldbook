
package com.efficio.fieldbook.web.common.service;

import java.util.Map;

import org.generationcp.commons.exceptions.GermplasmListExporterException;

public interface ExportGermplasmListService {

	public abstract void exportGermplasmListXLS(String fileNamePath, int listId, Map<String, Boolean> visibleColumns)
			throws GermplasmListExporterException;

	void exportStockListXLS(String fileNamePath, Map<String, Boolean> visibleColumns) throws GermplasmListExporterException;

	public abstract void exportGermplasmListCSV(String fileNamePath, Map<String, Boolean> visibleColumns)
			throws GermplasmListExporterException;

}
