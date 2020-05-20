
package com.efficio.fieldbook.web.common.service;

import java.util.Map;

import org.generationcp.commons.exceptions.GermplasmListExporterException;

public interface ExportStudyGermplasmListService {

	void exportAsExcelFile(int studyId, String fileNamePath, Map<String, Boolean> visibleColumns) throws GermplasmListExporterException;

	public abstract void exportAsCSVFile(int studyId, String fileNamePath, Map<String, Boolean> visibleColumns)
			throws GermplasmListExporterException;

}
