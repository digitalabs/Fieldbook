package com.efficio.fieldbook.util.parsing;

import com.efficio.fieldbook.web.common.exception.FileParsingException;
import com.efficio.fieldbook.web.nursery.bean.ImportedCondition;
import com.efficio.fieldbook.web.nursery.bean.ImportedConstant;
import com.efficio.fieldbook.web.nursery.bean.ImportedFactor;
import com.efficio.fieldbook.web.nursery.bean.ImportedVariate;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.util.PoiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by cyrus on 4/24/15.
 */
public class DescriptionSheetParser<T extends ImportedDescriptionDetails> {
	public static final int DESCRIPTION_SHEET_NO = 0;
	public static final int CONDITION_ROW_NO = 5;
	public static final int DESCRIPTION_SHEET_COL_SIZE = 8;

	private static final Logger LOG = LoggerFactory.getLogger(DescriptionSheetParser.class);
	public static final String TEMPLATE_LIST_TYPE = "LST";

	private T importedList;

	private int currentRow = 0;
	private boolean importFileIsValid = true;
	private Workbook workbook;

	public DescriptionSheetParser(T importedList,Workbook workbook) {
		this.importedList = importedList;
		this.workbook = workbook;
	}

	/**
	 * You may overide this method to costumize parsing behavior, by default, it parses
	 * ListDetails, Conditions, Factors, Constants and Variate
	 * @throws FileParsingException
	 * @throws ParseException
	 */
	public void parseDescriptionSheet() throws FileParsingException, ParseException {
		parseListDetails();
		parseConditions();
		parseFactors();
		parseConstants();
		parseVariate();
	}

	protected void parseListDetails() throws FileParsingException, ParseException {
		String listName = getCellStringValue(DESCRIPTION_SHEET_NO, 0, 1);
		String listTitle = getCellStringValue(DESCRIPTION_SHEET_NO, 1, 1);

		String labelId = getCellStringValue(DESCRIPTION_SHEET_NO, 2, 0);

		int listDateColNo = AppConstants.LIST_DATE.getString().equalsIgnoreCase(labelId) ? 2 : 3;
		int listTypeColNo = AppConstants.LIST_TYPE.getString().equalsIgnoreCase(labelId) ? 2 : 3;

		Date listDate = DateUtil.parseDate(
				getCellStringValue(DESCRIPTION_SHEET_NO, listDateColNo, 1));
		String listType = getCellStringValue(DESCRIPTION_SHEET_NO, listTypeColNo, 1);

		if (!TEMPLATE_LIST_TYPE.equalsIgnoreCase(listType)) {
			throw new FileParsingException("Error parsing details : List type is invalid");
		}

		importedList.setName(listName);
		importedList.setTitle(listTitle);
		importedList.setType(listType);
		importedList.setDate(listDate);
	}

	protected void parseConditions() {
		// condition headers start at row = 5 (+ 1 : count starts from 0 )
		currentRow = CONDITION_ROW_NO;

		if (!isConditionHeadersInvalid(CONDITION_ROW_NO) && importFileIsValid) {
			currentRow++;

			while (!isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
				this.importedList.addImportedCondition(
						new ImportedCondition(
								getCellStringValue(DESCRIPTION_SHEET_NO,
										currentRow, 0)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 1)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 2)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 3)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 4)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 5)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 6),
								""
						)
				);

				currentRow++;
			}
		}

		while (isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
			currentRow++;
		}
	}

	protected void parseFactors() throws FileParsingException {

		if (!isFactorHeadersInvalid(currentRow) && importFileIsValid) {
			currentRow++;

			while (!isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
				final ImportedFactor factor = new ImportedFactor(
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								0)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								1)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								2)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								3)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								4)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								5)
						, "");

				importedList.addImportedFactor(factor);

				currentRow++;
			}

			currentRow++;

		} else {
			throw new FileParsingException("Error parsing on factors header: Incorrect headers for factors.");
		}

		while (isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
			currentRow++;
		}
	}

	protected void parseConstants() throws FileParsingException{
		if (!isConstantsHeaderInvalid(currentRow) && importFileIsValid) {
			currentRow++;
			while (!isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
				importedList.addImportedConstant(new ImportedConstant(
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								0)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								1)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								2)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								3)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								4)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								5)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								6)));

				currentRow++;
			}
			currentRow++;

		} else {
			// Incorrect headers for factors.
			throw new FileParsingException("Error parsing on constants header: Incorrect headers for constants.");
		}

		while (isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
			currentRow++;
		}
	}

	protected void parseVariate() throws FileParsingException{
		if (!isVariateHeaderInvalid(currentRow) && importFileIsValid) {
			currentRow++;
			while (!isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
				importedList.addImportedVariate(
						new ImportedVariate(
								getCellStringValue(DESCRIPTION_SHEET_NO,
										currentRow, 0)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 1)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 2)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 3)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 4)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 5)));
				currentRow++;
			}

		} else {
			throw new FileParsingException("Error parsing on variates header: Incorrect headers for variates.");
		}
	}

	protected boolean isConditionHeadersInvalid(int conditionHeaderRowNo) {
		String[] headers = {
				AppConstants.CONDITION.getString(),
				AppConstants.DESCRIPTION.getString(),
				AppConstants.PROPERTY.getString(),
				AppConstants.SCALE.getString(),
				AppConstants.METHOD.getString(),
				AppConstants.DATA_TYPE.getString(),
				AppConstants.VALUE.getString()
		};

		return isHeaderInvalid(conditionHeaderRowNo, DESCRIPTION_SHEET_NO, headers);
	}

	protected boolean isFactorHeadersInvalid(int factorHeaderRowNo) {
		String[] headers = {
				AppConstants.FACTOR.getString(),
				AppConstants.DESCRIPTION.getString(),
				AppConstants.PROPERTY.getString(),
				AppConstants.SCALE.getString(),
				AppConstants.METHOD.getString(),
				AppConstants.DATA_TYPE.getString()
		};

		return isHeaderInvalid(factorHeaderRowNo,DESCRIPTION_SHEET_NO, headers);
	}

	protected boolean isConstantsHeaderInvalid(int constantHeaderRowNo) {
		String[] headers = {
				AppConstants.CONSTANT.getString(),
				AppConstants.DESCRIPTION.getString(),
				AppConstants.PROPERTY.getString(),
				AppConstants.SCALE.getString(),
				AppConstants.METHOD.getString(),
				AppConstants.DATA_TYPE.getString(),
				AppConstants.VALUE.getString()
		};

		return isHeaderInvalid(constantHeaderRowNo, DESCRIPTION_SHEET_NO,headers);
	}

	protected boolean isVariateHeaderInvalid(int variateHeaderRowNo) {
		String headers[] = {
				AppConstants.VARIATE.getString(),
				AppConstants.DESCRIPTION.getString(),
				AppConstants.PROPERTY.getString(),
				AppConstants.SCALE.getString(),
				AppConstants.METHOD.getString(),
				AppConstants.DATA_TYPE.getString()
		};

		return isHeaderInvalid(variateHeaderRowNo, DESCRIPTION_SHEET_NO, headers);
	}

	protected boolean isHeaderInvalid(int headerNo, int sheetNumber, String[] headers) {
		boolean isInvalid = false;

		for (int i = 0; i < headers.length; i++) {
			isInvalid = isInvalid || !headers[i].equalsIgnoreCase(
					getCellStringValue(sheetNumber, headerNo, i));
		}

		return isInvalid;
	}

	/**
	 * Wrapper to PoiUtil.getCellStringValue static call so we can stub the methods on unit tests
	 *
	 * @param sheetNo
	 * @param rowNo
	 * @param columnNo
	 * @return
	 */
	public String getCellStringValue(int sheetNo, int rowNo, Integer columnNo) {
		String out = (null == columnNo) ?
				"" :
				PoiUtil.getCellStringValue(workbook, sheetNo, rowNo, columnNo);
		return (null == out) ? "" : out;
	}

	/**
	 * Wrapper to PoiUtil.rowIsEmpty static call so we can stub the methods on unit tests
	 *
	 * @param sheetNo
	 * @param rowNo
	 * @param colCount
	 * @return
	 */
	public boolean isRowEmpty(int sheetNo, int rowNo, int colCount) {
		return PoiUtil.rowIsEmpty(workbook.getSheetAt(sheetNo), rowNo, 0, colCount - 1);
	}
}
