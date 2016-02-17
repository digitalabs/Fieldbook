package com.efficio.fieldbook.web.common.service.impl;

import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;

import com.efficio.fieldbook.web.util.KsuFieldbookUtil;

public class KsuCsvWorkbookParser extends AbstractCsvWorkbookParser<KsuCsvWorkbookParser>{
	private Workbook workbook;
	private String trialInstanceNo;
	private Map<String, MeasurementRow> rowsMap;
	
	public KsuCsvWorkbookParser(Workbook workbook, String trialInstanceNo,	Map<String, MeasurementRow> rowsMap) {
		super();
		this.workbook = workbook;
		this.trialInstanceNo = trialInstanceNo;
		this.rowsMap = rowsMap;
	}
	
	@Override
	public	KsuCsvWorkbookParser parseCsvMap(Map<Integer, List<String>> csvMap) throws FileParsingException {
		// validate headers
		String[] rowHeaders = csvMap.get(0).toArray(new String[csvMap.get(0).size()]);

		if (!KsuFieldbookUtil.isValidHeaderNames(rowHeaders)) {
			throw new FileParsingException("error.workbook.import.requiredColumnsMissing");
		}

		// this does the big parsing import task
		this.importDataToWorkbook(csvMap, workbook, trialInstanceNo, rowsMap);

		return this;
	}
	
	@Override
	public String getLabelFromRequiredColumn(MeasurementVariable variable){
		return KsuFieldbookUtil.getLabelFromKsuRequiredColumn(variable);
	}
}
