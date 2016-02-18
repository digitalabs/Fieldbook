
package com.efficio.fieldbook.web.study;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;

public class CsvWorkbookParser extends AbstractCsvWorkbookParser<CsvWorkbookParser> {

	private final Workbook workbook;
	private final String trialInstanceNo;
	private final Map<String, MeasurementRow> rowsMap;

	public enum CsvRequiredColumnEnum {
		ENTRY_NO(TermId.ENTRY_NO.getId(), "ENTRY_NO"), PLOT_NO(TermId.PLOT_NO.getId(), "PLOT_NO"), GID(TermId.GID.getId(),
				"GID"), DESIGNATION(TermId.DESIG.getId(), "DESIGNATION");

		private final Integer id;
		private final String label;

		private static final Map<Integer, CsvRequiredColumnEnum> LOOK_UP = new HashMap<>();

		static {
			for (final CsvRequiredColumnEnum cl : EnumSet.allOf(CsvRequiredColumnEnum.class)) {
				CsvRequiredColumnEnum.LOOK_UP.put(cl.getId(), cl);
			}
		}

		CsvRequiredColumnEnum(final Integer id, final String label) {
			this.id = id;
			this.label = label;
		}

		public Integer getId() {
			return this.id;
		}

		public String getLabel() {
			return this.label;
		}

		public static CsvRequiredColumnEnum get(final Integer id) {
			return CsvRequiredColumnEnum.LOOK_UP.get(id);
		}
	}

	public CsvWorkbookParser(final Workbook workbook, final String trialInstanceNo, final Map<String, MeasurementRow> rowsMap) {
		super();
		this.workbook = workbook;
		this.trialInstanceNo = trialInstanceNo;
		this.rowsMap = rowsMap;
	}

	@Override
	public CsvWorkbookParser parseCsvMap(final Map<Integer, List<String>> csvMap) throws FileParsingException {

		// validate headers
		final String[] rowHeaders = csvMap.get(0).toArray(new String[csvMap.get(0).size()]);

		if (!this.isValidHeaderNames(rowHeaders)) {
			throw new FileParsingException("error.workbook.import.requiredColumnsMissing");
		}

		// this does the big parsing import task
		this.importDataToWorkbook(csvMap, this.workbook, this.trialInstanceNo, this.rowsMap);

		return this;
	}

	@Override
	public String getLabelFromRequiredColumn(final MeasurementVariable variable) {
		String label = "";

		if (CsvRequiredColumnEnum.get(variable.getTermId()) != null) {
			label = CsvRequiredColumnEnum.get(variable.getTermId()).getLabel();
		}

		if (label.trim().length() > 0) {
			return label;
		}

		return variable.getName();
	}

	boolean isValidHeaderNames(final String[] rowHeaders) {
		final List<String> rowHeadersList = Arrays.asList(rowHeaders);

		for (final CsvRequiredColumnEnum column : CsvRequiredColumnEnum.values()) {
			if (!rowHeadersList.contains(column.getLabel().trim())) {
				return false;
			}
		}
		return true;
	}
}
