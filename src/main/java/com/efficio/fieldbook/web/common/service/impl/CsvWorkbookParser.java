package com.efficio.fieldbook.web.common.service.impl;

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

public class CsvWorkbookParser extends AbstractCsvWorkbookParser<CsvWorkbookParser>{
	private Workbook workbook;
	private String trialInstanceNo;
	private Map<String, MeasurementRow> rowsMap;
	
	public enum CsvRequiredColumnEnum {
		ENTRY_NO(TermId.ENTRY_NO.getId(), "ENTRY_NO"), PLOT_NO(TermId.PLOT_NO.getId(), "PLOT_NO"), GID(TermId.GID.getId(),
				"GID"), DESIGNATION(TermId.DESIG.getId(), "DESIGNATION");

		private final Integer id;
		private final String label;

		private static final Map<Integer, CsvRequiredColumnEnum> LOOK_UP = new HashMap<>();

		static {
			for (CsvRequiredColumnEnum cl : EnumSet.allOf(CsvRequiredColumnEnum.class)) {
				CsvRequiredColumnEnum.LOOK_UP.put(cl.getId(), cl);
			}
		}

		CsvRequiredColumnEnum(Integer id, String label) {
			this.id = id;
			this.label = label;
		}

		public Integer getId() {
			return this.id;
		}

		public String getLabel() {
			return this.label;
		}

		public static CsvRequiredColumnEnum get(Integer id) {
			return CsvRequiredColumnEnum.LOOK_UP.get(id);
		}
	}
	
	public CsvWorkbookParser(Workbook workbook, String trialInstanceNo, Map<String, MeasurementRow> rowsMap) {
		super();
		this.workbook = workbook;
		this.trialInstanceNo = trialInstanceNo;
		this.rowsMap = rowsMap;
	}
	
	@Override
	public	CsvWorkbookParser parseCsvMap(Map<Integer, List<String>> csvMap) throws FileParsingException {
		
		// validate headers
		String[] rowHeaders = csvMap.get(0).toArray(new String[csvMap.get(0).size()]);

		if (!this.isValidHeaderNames(rowHeaders)) {
			throw new FileParsingException("error.workbook.import.requiredColumnsMissing");
		}

		// this does the big parsing import task
		this.importDataToWorkbook(csvMap, workbook, trialInstanceNo, rowsMap);

		return this;
	}
	
	@Override
	public String getLabelFromRequiredColumn(MeasurementVariable variable){
		String label = "";

		if (CsvRequiredColumnEnum.get(variable.getTermId()) != null) {
			label = CsvRequiredColumnEnum.get(variable.getTermId()).getLabel();
		}

		if (label.trim().length() > 0) {
			return label;
		}

		return variable.getName();
	}

	boolean isValidHeaderNames(String[] rowHeaders) {
		List<String> rowHeadersList = Arrays.asList(rowHeaders);
		
		for (CsvRequiredColumnEnum column : CsvRequiredColumnEnum.values()) {
		 	if (!rowHeadersList.contains(column.getLabel().trim())) {
				return false;
			}
		}
		return true;
	}
}
