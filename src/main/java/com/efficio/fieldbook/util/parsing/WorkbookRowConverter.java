package com.efficio.fieldbook.util.parsing;

import com.efficio.fieldbook.util.parsing.validation.ParseValidationMap;
import com.efficio.fieldbook.util.parsing.validation.ParsingValidator;
import com.efficio.fieldbook.web.common.exception.FileParsingException;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.util.PoiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/26/2015
 * Time: 11:20 AM
 */
public abstract class WorkbookRowConverter<T> {

	private Workbook workbook;
	private int currentIndex;
	private int targetSheetIndex;
	private int columnCount;
	private ParseValidationMap validationMap;
	private String[] columnLabels;

	public WorkbookRowConverter(Workbook workbook, int startingIndex, int targetSheetIndex, int columnCount, String[] columnLabels) {
		this.workbook = workbook;
		this.currentIndex = startingIndex;
		this.targetSheetIndex = targetSheetIndex;
		this.columnCount = columnCount;
		this.columnLabels = columnLabels;

		if (columnLabels == null) {
			throw new IllegalArgumentException("Column labels must not be null");
		} else if (columnCount != columnLabels.length) {
			throw new IllegalArgumentException("There should be a column label for each column to be converted");
		}
	}

	public List<T> convertWorkbookRowsToObject(ContinueExpression continueExpression) throws
			FileParsingException{
		Map<Integer, String> currentRowValue;
		List<T> valueList = new ArrayList<>();

		do {
			if (isRowEmpty(targetSheetIndex, currentIndex, columnCount - 1)) {
				currentRowValue = null;
				continue;
			}

			currentRowValue = new HashMap<>();
			for (int i = 0; i < columnCount - 1; i++) {
				String value = getCellStringValue(targetSheetIndex, currentIndex, i);

				if (value == null) {
					value = "";
				}

				if (validationMap != null && validationMap.getValidations(i) != null) {
					applyValidation(value, columnLabels[i], validationMap.getValidations(i));
				}

				currentRowValue.put(i, value);
			}

			currentIndex++;
			valueList.add(convertToObject(currentRowValue));

		} while (continueExpression.shouldContinue(currentRowValue));

		return valueList;
	}

	public void applyValidation(String value, String columnLabel, List<ParsingValidator> validators) throws FileParsingException{
		for (ParsingValidator validator : validators) {
			if (!validator.isParsedValueValid(value)) {
				// +1 is added to the current index since index is 0 based
				throw new FileParsingException(validator.getValidationErrorMessage(), getCurrentIndex() + 1, value, columnLabel);
			}
		}
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public abstract T convertToObject(Map<Integer, String> rowValues) throws FileParsingException;

	public String getCellStringValue(int sheetNo, int rowNo, Integer columnNo) {
		String out = (null == columnNo) ?
				"" :
				PoiUtil.getCellStringValue(this.workbook, sheetNo, rowNo, columnNo);
		return (null == out) ? "" : out;
	}

	public boolean isRowEmpty(int sheetNo, int rowNo, int maxColumns) {
		return PoiUtil.rowIsEmpty(this.workbook.getSheetAt(sheetNo), rowNo, 0, maxColumns);
	}

	public void setValidationMap(ParseValidationMap validationMap) {
		this.validationMap = validationMap;
	}

	// continue expression used to determine whether converter should continue in parsing row values or not
	public static interface ContinueExpression {
		public boolean shouldContinue(Map<Integer, String> currentRowValue);
	}

	// default implementation, tells the converter to stop when the current row is blank
	public static class ContinueTillBlank implements ContinueExpression {
		@Override public boolean shouldContinue(Map<Integer, String> currentRowValue) {
			return currentRowValue != null && !currentRowValue.isEmpty();
		}
	}
}
