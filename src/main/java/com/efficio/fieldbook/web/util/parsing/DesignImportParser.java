package com.efficio.fieldbook.web.util.parsing;

import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.AbstractCsvFileParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.web.common.bean.DesignImportData;

public class DesignImportParser extends AbstractCsvFileParser<DesignImportData> {

	private static final Logger LOG = LoggerFactory.getLogger(DesignImportParser.class);

	@Override
	public DesignImportData parseCsvMap(Map<Integer, List<String>> csvMap)
			throws FileParsingException {
		
		DesignImportData data = new DesignImportData();
		data.setUnmappedHeaders(csvMap.get(0));
		data.setCsvData(csvMap);
		
		return data;
	}

}
