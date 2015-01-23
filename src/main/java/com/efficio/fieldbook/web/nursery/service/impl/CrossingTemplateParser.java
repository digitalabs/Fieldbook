package com.efficio.fieldbook.web.nursery.service.impl;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.nursery.bean.*;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.util.PoiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by cyrus on 1/22/15.
 */
public class CrossingTemplateParser {

	public static final int DESCRIPTION_SHEET_NO = 0;
	public static final int CONDITION_ROW_NO = 5;
	/**
	 * The Constant FILE_INVALID.
	 */
	public final static String FILE_INVALID = "common.error.invalid.file";
	/**
	 * The Constant FILE_TYPE_INVALID.
	 */
	public final static String FILE_TYPE_INVALID = "common.error.invalid.file.type";
	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CrossingTemplateParser.class);
	private ImportedCrossesList importedCrossesList;
	private boolean importFileIsValid = true;

	private FileService fileService;

	// TODO this needs to be initialized
	private Workbook workbook;
	private int currentRow = 0;
	private String originalFilename;

	public CrossingTemplateParser(FileService fileService) {
		this.fileService = fileService;
	}

	public ImportedCrossesList parseFile(MultipartFile multipartFile) {
		try {

			this.workbook = storeImportGermplasmWorkbook(multipartFile);

			parseDescriptionSheet();

		} catch (IOException e) {
			addParseErrorMsg(e.getMessage());
		} catch (ParseException e) {
			addParseErrorMsg(e.getMessage());
		}

		return importedCrossesList;

	}

	protected Workbook storeImportGermplasmWorkbook(MultipartFile multipartFile)
			throws IOException {
		String serverFilename = fileService.saveTemporaryFile(multipartFile.getInputStream());
		this.originalFilename = multipartFile.getOriginalFilename();

		return fileService.retrieveWorkbook(serverFilename);
	}

	public void parseDescriptionSheet() throws ParseException {
		parseCrossingListDetails();
		parseConditions();
		parseFactors();
		parseConstants();
		parseVariate();

	}

	public void parseObservationSheet(String fileName) throws ParseException {
	}

	public void parseCrossingListDetails() throws ParseException {
		String listName = PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, 0, 1);
		String listTitle = PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, 1, 1);

		String labelId = PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, 2, 0);

		int listDateColNo = AppConstants.LIST_DATE.getString().equalsIgnoreCase(labelId) ? 2 : 3;
		int listTypeColNo = AppConstants.LIST_TYPE.getString().equalsIgnoreCase(labelId) ? 2 : 3;

		Date listDate = DateUtil.parseDate(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, listDateColNo, 1));
		String listType = PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, listTypeColNo, 1);

		this.importedCrossesList = new ImportedCrossesList(this.originalFilename, listName,
				listTitle, listType, listDate);
	}

	public void parseConditions() {
		// condition headers start at row = 5 (+ 1 : count starts from 0 )
		currentRow = CONDITION_ROW_NO;

		if (!isConditionHeadersInvalid(CONDITION_ROW_NO) && importFileIsValid) {
			currentRow++;

			while (!PoiUtil
					.rowIsEmpty(workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0, 7)) {
				this.importedCrossesList.addImportedCondition(
						new ImportedCondition(
								PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
										currentRow, 0)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 1)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 2)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 3)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 4)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 5)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 6),
								""
						)
				);

				currentRow++;
			}
		}

		while (PoiUtil.rowIsEmpty(workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0, 7)) {
			currentRow++;
		}
	}

	public void parseFactors() {

		if (!isFactorHeadersInvalid(currentRow) && importFileIsValid) {
			currentRow++;

			while ((!PoiUtil
					.rowIsEmpty(this.workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0,
							7))) {
				final ImportedFactor factor = new ImportedFactor(
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								0)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								1)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								2)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								3)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								4)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								5)
						, "");

				importedCrossesList.addImportedFactor(factor);

				currentRow++;
			}

			currentRow++;

		} else {
			// Incorrect headers for factors.
			addParseErrorMsg(FILE_INVALID);
			LOG.debug("Error parsing on factors header: Incorrect headers for factors.");
		}

		while (PoiUtil.rowIsEmpty(workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0, 7)) {
			currentRow++;
		}
	}

	public void parseConstants() {
		if (!isConstantsHeaderInvalid(currentRow) && importFileIsValid) {
			currentRow++;
			while ((!PoiUtil
					.rowIsEmpty(this.workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0,
							7))) {
				importedCrossesList.addImportedConstant(new ImportedConstant(
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								0)
						,PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								1)
						,PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								2)
						,PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								3)
						,PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								4)
						,PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								5)
						,PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								6)));

				currentRow++;
			}
			currentRow++;

		} else {
			// Incorrect headers for factors.
			addParseErrorMsg(FILE_INVALID);
			LOG.debug("Error parsing on constants header: Incorrect headers for constants.");
		}

		while (PoiUtil.rowIsEmpty(workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0, 7)) {
			currentRow++;
		}
	}

	public void parseVariate() {
		if (!isVariateHeaderInvalid(currentRow) && importFileIsValid) {
			currentRow++;
			while (!PoiUtil
					.rowIsEmpty(this.workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0,
							7)) {
				importedCrossesList.addImportedVariate(
						new ImportedVariate(
								PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
										currentRow, 0)
								, PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								currentRow, 1)
								, PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								currentRow, 2)
								, PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								currentRow, 3)
								, PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								currentRow, 4)
								, PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								currentRow, 5)));
				currentRow++;
			}

		} else {
			addParseErrorMsg(FILE_INVALID);
			LOG.debug("Error parsing on variates header: Incorrect headers for variates.");
		}
	}

	private boolean isConditionHeadersInvalid(int conditionHeaderRowNo) {
		return !AppConstants.CONDITION.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 0))
				|| !AppConstants.DESCRIPTION.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 1))
				|| !AppConstants.PROPERTY.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 2))
				|| !AppConstants.SCALE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 3))
				|| !AppConstants.METHOD.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 4))
				|| !AppConstants.DATA_TYPE.getString()
				.equalsIgnoreCase(PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 5))
				|| !AppConstants.VALUE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 6));
	}

	private boolean isFactorHeadersInvalid(int factorHeaderRowNo) {
		return !AppConstants.FACTOR.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 0))
				|| !AppConstants.DESCRIPTION.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 1))
				|| !AppConstants.PROPERTY.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 2))
				|| !AppConstants.SCALE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 3))
				|| !AppConstants.METHOD.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 4))
				|| !AppConstants.DATA_TYPE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 5));
	}

	private boolean isConstantsHeaderInvalid(int constantHeaderRowNo) {
		return !
				AppConstants.CONSTANT.getString().equalsIgnoreCase(PoiUtil
						.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								constantHeaderRowNo, 0))
				|| !AppConstants.DESCRIPTION.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 1))
				|| !AppConstants.PROPERTY.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 2))
				|| !AppConstants.SCALE.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 3))
				|| !AppConstants.METHOD.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 4))
				|| !AppConstants.DATA_TYPE.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 5))
				|| !AppConstants.VALUE.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 6));
	}

	private boolean isVariateHeaderInvalid(int variateHeaderRowNo) {
		return !AppConstants.VARIATE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						0))
				|| !AppConstants.DESCRIPTION.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						1))
				|| !AppConstants.PROPERTY.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						2))
				|| !AppConstants.SCALE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						3))
				|| !AppConstants.METHOD.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						4))
				|| !AppConstants.DATA_TYPE.getString().replace("_"," ").equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						5));
	}

	private void addParseErrorMsg(String message) {
		if (importFileIsValid) {
			importedCrossesList.addErrorMessages(message);
			importFileIsValid = false;
		}
	}
}
