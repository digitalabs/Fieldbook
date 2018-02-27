package com.efficio.fieldbook.web.common.service;

import org.generationcp.middleware.domain.sample.SampleDetailsDTO;

import com.efficio.fieldbook.util.FileExportInfo;

import java.io.IOException;
import java.util.List;

public interface CsvExportSampleListService {

	FileExportInfo export(final List<SampleDetailsDTO> sampleDetailsDTOs, final String filename, final List<String> visibleColumns)
		throws IOException;
}
