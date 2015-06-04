
package com.efficio.fieldbook.web.common.service;

import java.util.List;

import org.generationcp.middleware.domain.etl.Workbook;

public interface CsvExportStudyService extends ExportStudyService {

	String export(Workbook workbook, String filename, List<Integer> instances, List<Integer> visibleColumns);
}
