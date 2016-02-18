
package com.efficio.fieldbook.web.study;

import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;

import com.efficio.fieldbook.web.util.KsuFieldbookUtil;

public class KsuCsvWorkbookParser extends AbstractCsvWorkbookParser<KsuCsvWorkbookParser> {

	private final Workbook workbook;
	private final String trialInstanceNo;
	private final Map<String, MeasurementRow> rowsMap;

	public KsuCsvWorkbookParser(final Workbook workbook, final String trialInstanceNo, final Map<String, MeasurementRow> rowsMap) {
		super();
		this.workbook = workbook;
		this.trialInstanceNo = trialInstanceNo;
		this.rowsMap = rowsMap;
	}

	@Override
	public KsuCsvWorkbookParser parseCsvMap(final Map<Integer, List<String>> csvMap) throws FileParsingException {
		// validate headers
		final String[] rowHeaders = csvMap.get(0).toArray(new String[csvMap.get(0).size()]);

		if (!KsuFieldbookUtil.isValidHeaderNames(rowHeaders)) {
			throw new FileParsingException("error.workbook.import.requiredColumnsMissing");
		}

		// this does the big parsing import task
		this.importDataToWorkbook(csvMap, this.workbook, this.trialInstanceNo, this.rowsMap);

		return this;
	}

	@Override
	public String getLabelFromRequiredColumn(final MeasurementVariable variable) {
		return KsuFieldbookUtil.getLabelFromKsuRequiredColumn(variable);
	}
}
