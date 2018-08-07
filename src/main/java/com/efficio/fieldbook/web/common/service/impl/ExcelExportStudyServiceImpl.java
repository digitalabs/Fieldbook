/*******************************************************************************
o * Copyright (c) 2012, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import com.efficio.fieldbook.web.common.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;

@Service
@Transactional
public class ExcelExportStudyServiceImpl extends BaseExportStudyServiceImpl implements ExcelExportStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(ExcelExportStudyServiceImpl.class);

	private static final int PIXEL_SIZE = 250;

	private static final String OCC_8170_LABEL = "8170_LABEL";
	private static final String PLOT = "PLOT";
	public static final String POSSIBLE_VALUES_AS_STRING_DELIMITER = "/";

	protected static final int VARIABLE_NAME_COLUMN_INDEX = 0;
	protected static final int DESCRIPTION_COLUMN_INDEX = 1;
	protected static final int PROPERTY_COLUMN_INDEX = 2;
	protected static final int SCALE_COLUMN_INDEX = 3;
	protected static final int METHOD_COLUMN_INDEX = 4;
	protected static final int DATATYPE_COLUMN_INDEX = 5;
	protected static final int VARIABLE_VALUE_COLUMN_INDEX = 6;
	protected static final int LABEL_COLUMN_INDEX = 7;

	private static final String MAX_ONLY = " and below";
	private static final String MIN_ONLY = " and above";
	private static final String NO_RANGE = "All values allowed";

	@Resource
	private MessageSource messageSource;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	LocationDataManager locationDataManager;

	private static final String BREEDING_METHOD_PROPERTY_NAME = "";

	protected static final List<Integer> STUDY_DETAILS_IDS =
		Arrays.asList(TermId.PM_KEY.getId());

	@Override
	void writeOutputFile(Workbook workbook, List<Integer> visibleColumns, MeasurementRow instanceLevelObservation,
			List<MeasurementRow> plotLevelObservations, String fileNamePath) throws IOException {
		FileOutputStream fos = null;

		final HSSFWorkbook xlsBook = new HSSFWorkbook();
		this.writeDescriptionSheet(xlsBook, workbook, instanceLevelObservation, visibleColumns);
		this.writeObservationSheet(xlsBook, workbook, plotLevelObservations, visibleColumns, BREEDING_METHOD_PROPERTY_NAME);

		try {
			fos = new FileOutputStream(new File(fileNamePath));
			xlsBook.write(fos);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}


	}

	@Override
	String getFileExtension() {
		return AppConstants.EXPORT_XLS_SUFFIX.getString();
	}

	protected void writeDescriptionSheet(final HSSFWorkbook xlsBook, final Workbook workbook, final MeasurementRow trialObservation,
			final List<Integer> visibleColumns) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet xlsSheet = xlsBook.createSheet(this.messageSource.getMessage("export.study.sheet.description", null, locale));
		int currentRowNum = 0;

		currentRowNum = this.writeStudyDetails(currentRowNum, xlsBook, xlsSheet, workbook.getStudyDetails());
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = this.writeConditions(currentRowNum, xlsBook, xlsSheet, workbook.getConditions(), trialObservation, workbook);
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = this.writeFactors(currentRowNum, xlsBook, xlsSheet, workbook.getNonTrialFactors(), visibleColumns, workbook);
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = this.writeConstants(currentRowNum, xlsBook, xlsSheet, workbook.getConstants(), trialObservation, workbook);
		xlsSheet.createRow(currentRowNum++);
		currentRowNum = this.writeVariates(currentRowNum, xlsBook, xlsSheet, workbook.getVariates(), visibleColumns, workbook);

		xlsSheet.setColumnWidth(0, 20 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(1, 24 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(2, 30 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(3, 18 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(4, 18 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(5, 15 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(6, 20 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
		xlsSheet.setColumnWidth(7, 20 * ExcelExportStudyServiceImpl.PIXEL_SIZE);
	}

	protected void writeObservationSheet(final HSSFWorkbook xlsBook, final Workbook workbook, final List<MeasurementRow> observations,
			final List<Integer> visibleColumns, final String breedingMethodPropertyName) {
		LOG.info("Start Export Observation Sheet");
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet xlsSheet = xlsBook.createSheet(this.messageSource.getMessage("export.study.sheet.observation", null, locale));
		int currentRowNum = 0;

		this.writeObservationHeader(currentRowNum++, xlsBook, xlsSheet, workbook.getMeasurementDatasetVariables(), visibleColumns);

		final CellStyle style = this.createCellStyle(xlsBook);

		for (final MeasurementRow dataRow : observations) {
			this.writeObservationRow(currentRowNum++, xlsSheet, dataRow, workbook.getMeasurementDatasetVariables(), xlsBook, style,
					visibleColumns, breedingMethodPropertyName);
		}
		LOG.info("End Export Observation Sheet");

	}

	private CellStyle createCellStyle(final HSSFWorkbook xlsBook) {
		final CellStyle style = xlsBook.createCellStyle();
		final DataFormat format = xlsBook.createDataFormat();
		style.setDataFormat(format.getFormat("0.#"));

		return style;
	}

	private int writeStudyDetails(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
			final StudyDetails studyDetails) {
		int rowNumIndex = currentRowNum;
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.study",
				studyDetails.getStudyName() != null ? HtmlUtils.htmlUnescape(studyDetails.getStudyName()) : "");
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.title",
				studyDetails.getDescription() != null ? HtmlUtils.htmlUnescape(studyDetails.getDescription()) : "");
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.objective",
				studyDetails.getObjective() != null ? HtmlUtils.htmlUnescape(studyDetails.getObjective()) : "");

		String startDate = studyDetails.getStartDate();
		String endDate = studyDetails.getEndDate();

		if (startDate != null) {
			startDate = startDate.replace("-", "");
		}

		if (endDate != null) {
			endDate = endDate.replace("-", "");
		}

		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.startdate", startDate);
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.enddate", endDate);
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.studytype",
				studyDetails.getStudyType().getName());

		return rowNumIndex;
	}

	private PhenotypicType getRoleOfVariableInTrialObservations(final MeasurementVariable variable, final MeasurementRow trialObservation) {
		if (trialObservation != null && variable != null) {
			for (final MeasurementData measurementData : trialObservation.getDataList()) {
				if (measurementData.getMeasurementVariable() != null && measurementData.getMeasurementVariable().getTermId() == variable
						.getTermId()) {
					return measurementData.getMeasurementVariable().getRole();
				}
			}
		}
		return variable != null ? variable.getRole() : null;
	}

	/**
	 * This will write condition variables (i.e. trial instance, setting variables, and experimental design type) to the condition section
	 * of a fieldbook excel description sheet
	 *
	 * @param currentRowNum
	 * @param xlsBook
	 * @param xlsSheet
	 * @param conditions
	 * @param trialObservation
	 * @param workbook
	 * @return
	 */
	private int writeConditions(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
			final List<MeasurementVariable> conditions, final MeasurementRow trialObservation, final Workbook workbook) {
		final List<MeasurementVariable> arrangedConditions = new ArrayList<>();
		List<MeasurementVariable> filteredConditions = new ArrayList<>();
		if (conditions != null) {
			arrangedConditions.addAll(conditions);
			Collections.sort(arrangedConditions, new Comparator<MeasurementVariable>() {

				@Override
				public int compare(final MeasurementVariable var1, final MeasurementVariable var2) {
					return var1.getName().compareToIgnoreCase(var2.getName());
				}
			});

			for (final MeasurementVariable variable : arrangedConditions) {
				if (!ExcelExportStudyServiceImpl.STUDY_DETAILS_IDS.contains(variable.getTermId())) {
					filteredConditions.add(variable);
					if (PhenotypicType.TRIAL_ENVIRONMENT == this.getRoleOfVariableInTrialObservations(variable, trialObservation)) {
						if (variable.getTermId() == TermId.LOCATION_ID.getId()) {
							final String locationAlias = variable.getName();
							variable.setName(TermId.LOCATION_ID.name());
							final Integer locationId = new Integer( trialObservation.getMeasurementDataValue(variable.getTermId()));
							filteredConditions.add(createLocationNameVariable(locationId,locationAlias));

						}
						variable.setValue(trialObservation.getMeasurementDataValue(variable.getTermId()));

						if (variable.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
							variable.setPossibleValues(this.fieldbookService.getAllPossibleValues(variable.getTermId()));
						}

					}
				}
			}
		}
		filteredConditions = workbook.arrangeMeasurementVariables(filteredConditions);
		return this.writeSection(currentRowNum, xlsBook, xlsSheet, filteredConditions, "export.study.description.column.condition", 51, 153,
				102);
	}

	private MeasurementVariable createLocationNameVariable(final Integer locationId, final String locationAlias) {
		final MeasurementVariable locationNameVariable = new MeasurementVariable();

		final Location location = this.locationDataManager.getLocationByID(locationId);
		final StandardVariable standardVariable = this.fieldbookService.getStandardVariable(TermId.TRIAL_LOCATION.getId());
		locationNameVariable.setName(locationAlias);
		locationNameVariable.setDescription(standardVariable.getDescription());
		locationNameVariable.setProperty(standardVariable.getProperty().getName());
		locationNameVariable.setScale(standardVariable.getScale().getName());
		locationNameVariable.setMethod(standardVariable.getMethod().getName());
		locationNameVariable.setDataType(standardVariable.getDataType().getName());
		locationNameVariable.setDataTypeId(standardVariable.getDataType().getId());
		locationNameVariable.setValue(location.getLname());
		locationNameVariable.setLabel("TRIAL");
		locationNameVariable.setTermId(TermId.TRIAL_LOCATION.getId());
		return locationNameVariable;
	}

	/**
	 * This will write factor variables (i.e. germplasm descriptor variables and fieldmap) to the factor section of a fieldbook excel
	 * description sheet
	 *
	 * @param currentRowNum
	 * @param xlsBook
	 * @param xlsSheet
	 * @param factors
	 * @param visibleColumns
	 * @param workbook
	 * @return
	 */
	private int writeFactors(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
			final List<MeasurementVariable> factors, final List<Integer> visibleColumns, final Workbook workbook) {
		List<MeasurementVariable> filteredFactors = new ArrayList<>();
		for (final MeasurementVariable factor : factors) {
			if (factor.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId() && ExportImportStudyUtil
					.isColumnVisible(factor.getTermId(), visibleColumns)) {
				filteredFactors.add(factor);
			}
		}
		filteredFactors = workbook.arrangeMeasurementVariables(filteredFactors);
		return this.writeSection(currentRowNum, xlsBook, xlsSheet, filteredFactors, "export.study.description.column.factor", 51, 153, 102);
	}

	/**
	 * This will write constant variables (i.e. trial condition) to the constant section of a fieldbook excel description sheet
	 *
	 * @param currentRowNum
	 * @param xlsBook
	 * @param xlsSheet
	 * @param constants
	 * @param trialObservation
	 * @param workbook
	 * @return
	 */
	private int writeConstants(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
			final List<MeasurementVariable> constants, final MeasurementRow trialObservation, final Workbook workbook) {

		List<MeasurementVariable> filteredConstants = new ArrayList<>();
		for (final MeasurementVariable variable : constants) {
			filteredConstants.add(variable);
			if (PhenotypicType.VARIATE == variable.getRole()) {
				variable.setValue(trialObservation.getMeasurementDataValue(variable.getName()));
			}
		}
		filteredConstants = workbook.arrangeMeasurementVariables(filteredConstants);
		return this
				.writeSection(currentRowNum, xlsBook, xlsSheet, filteredConstants, "export.study.description.column.constant", 51, 51, 153);
	}

	/**
	 * This will write variates (i.e. trait variables) to the variates section of a fieldbook excel description sheet
	 *
	 * @param currentRowNum
	 * @param xlsBook
	 * @param xlsSheet
	 * @param variates
	 * @param visibleColumns
	 * @param workbook
	 * @return
	 */
	private int writeVariates(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
			final List<MeasurementVariable> variates, final List<Integer> visibleColumns, final Workbook workbook) {

		List<MeasurementVariable> filteredVariates = new ArrayList<>();
		for (final MeasurementVariable variate : variates) {
			if (ExportImportStudyUtil.isColumnVisible(variate.getTermId(), visibleColumns)) {
				filteredVariates.add(variate);
			}
		}
		filteredVariates = workbook.arrangeMeasurementVariables(filteredVariates);
		return this.writeSection(currentRowNum, xlsBook, xlsSheet, filteredVariates, "export.study.description.column.variate", 51, 51, 153,
				true);
	}

	private CellStyle getHeaderStyle(final HSSFWorkbook xlsBook, final int c1, final int c2, final int c3) {
		final HSSFPalette palette = xlsBook.getCustomPalette();
		final HSSFColor color = palette.findSimilarColor(c1, c2, c3);
		final short colorIndex = color.getIndex();

		final HSSFFont whiteFont = xlsBook.createFont();
		whiteFont.setColor(new HSSFColor.WHITE().getIndex());

		final CellStyle cellStyle = xlsBook.createCellStyle();
		cellStyle.setFillForegroundColor(colorIndex);
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyle.setFont(whiteFont);

		return cellStyle;
	}

	private void writeStudyDetailRow(final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum, final String label,
			final String value) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		HSSFCell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, 153, 51, 0));
		cell.setCellValue(this.messageSource.getMessage(label, null, locale));
		cell = row.createCell(1, Cell.CELL_TYPE_STRING);
		cell.setCellValue(value);
	}

	private int writeSection(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
			final List<MeasurementVariable> variables, final String sectionLabel, final int c1, final int c2, final int c3) {

		return this.writeSection(currentRowNum, xlsBook, xlsSheet, variables, sectionLabel, c1, c2, c3, false);
	}

	private int writeSection(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
			final List<MeasurementVariable> variables, final String sectionLabel, final int c1, final int c2, final int c3,
			final boolean isVariate) {
		int rowNumIndex = currentRowNum;
		this.writeSectionHeader(xlsBook, xlsSheet, rowNumIndex++, sectionLabel, c1, c2, c3);
		if (variables != null && !variables.isEmpty()) {
			for (final MeasurementVariable variable : variables) {

				if (isVariate) {
					variable.setLabel(ExcelExportStudyServiceImpl.PLOT);
				}

				this.writeSectionRow(rowNumIndex++, xlsSheet, variable);
			}
		}
		return rowNumIndex;

	}

	private void writeSectionHeader(final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet, final int currentRowNum, final String typeLabel,
			final int c1, final int c2, final int c3) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFRow row = xlsSheet.createRow(currentRowNum);

		HSSFCell cell = row.createCell(VARIABLE_NAME_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage(typeLabel, null, locale));

		cell = row.createCell(DESCRIPTION_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.description", null, locale));

		cell = row.createCell(PROPERTY_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.property", null, locale));

		cell = row.createCell(SCALE_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.scale", null, locale));

		cell = row.createCell(METHOD_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.method", null, locale));

		cell = row.createCell(DATATYPE_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.datatype", null, locale));

		cell = row.createCell(VARIABLE_VALUE_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.value", null, locale));

		// If typeLabel is constant or variate, the label column should be
		// 'SAMPLE LEVEL'
		cell = row.createCell(LABEL_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));

		if ("export.study.description.column.constant".equals(typeLabel) || "export.study.description.column.variate".equals(typeLabel)) {
			cell.setCellValue(this.messageSource.getMessage("export.study.description.column.samplelevel", null, locale));

		} else {
			cell.setCellValue(this.messageSource.getMessage("export.study.description.column.label", null, locale));

		}

	}

	protected void writeSectionRow(final int currentRowNum, final HSSFSheet xlsSheet, final MeasurementVariable measurementVariable) {
		final HSSFRow row = xlsSheet.createRow(currentRowNum);

		measurementVariable.setPossibleValues(this.fieldbookService.getAllPossibleValues(measurementVariable.getTermId()));

		HSSFCell cell = row.createCell(VARIABLE_NAME_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		String occName = measurementVariable.getName();
		final String appConstant8170 = AppConstants.getString(ExcelExportStudyServiceImpl.OCC_8170_LABEL);
		if (appConstant8170 != null && appConstant8170.equalsIgnoreCase(occName)) {
			occName = AppConstants.OCC.getString();
		}
		cell.setCellValue(occName);

		cell = row.createCell(DESCRIPTION_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellValue(measurementVariable.getDescription());

		cell = row.createCell(PROPERTY_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellValue(measurementVariable.getProperty());

		cell = row.createCell(SCALE_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellValue(measurementVariable.getScale());

		cell = row.createCell(METHOD_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellValue(measurementVariable.getMethod());

		cell = row.createCell(DATATYPE_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellValue(measurementVariable.getDataTypeCode());

		cell = row.createCell(VARIABLE_VALUE_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		setContentOfVariableValueColumn(cell, measurementVariable);

		cell = row.createCell(LABEL_COLUMN_INDEX, Cell.CELL_TYPE_STRING);
		cell.setCellValue(this.getLabel(measurementVariable));

	}

	protected String getLabel(final MeasurementVariable measurementVariable) {

		if (measurementVariable.getTreatmentLabel() != null && !"".equals(measurementVariable.getTreatmentLabel())) {
			return measurementVariable.getTreatmentLabel();
		} else {
			return measurementVariable.getLabel();
		}

	}

	protected void setContentOfVariableValueColumn(final HSSFCell cell, final MeasurementVariable measurementVariable) {

		if (StringUtils.isBlank(measurementVariable.getValue()) && (measurementVariable.getVariableType() == VariableType.TRAIT
			|| (measurementVariable.getRole().equals(PhenotypicType.VARIATE)))) {

			/**
			 If the variable is a 'Trait' then the VALUE column for VARIATE table in Description sheet will be:
			 for numerical variables: we will see the Min and Max values (if any) separated by a dash "-", e.g.: 30 - 100 (we should allow decimal values too, e.g.: 0.50 - 23.09)
			 for categorical variables: we will
			 see the Categories values separated by a slash "/", e.g.: 1/2/3/4/5
			 for date variables: will remain empty
			 for character/text variables: will remain empty
			 **/

			cell.setCellValue(getPossibleValueDetailAsStringBasedOnDataType(measurementVariable));

		} else {

			setVariableValueBasedOnDataType(cell, measurementVariable);

		}

	}

	protected void setVariableValueBasedOnDataType(final HSSFCell cell, final MeasurementVariable measurementVariable) {

		if (DataType.NUMERIC_VARIABLE.getId().equals(measurementVariable.getDataTypeId()) && StringUtils
				.isNotBlank(measurementVariable.getValue()) && NumberUtils.isNumber(measurementVariable.getValue())) {

			cell.setCellValue(Double.valueOf(measurementVariable.getValue()));
			cell.setCellType(Cell.CELL_TYPE_NUMERIC);

		} else if (DataType.CATEGORICAL_VARIABLE.getId().equals(measurementVariable.getDataTypeId())) {

			cell.setCellValue(
					ExportImportStudyUtil.getCategoricalCellValue(measurementVariable.getValue(), measurementVariable.getPossibleValues()));

		} else {

			cell.setCellValue(measurementVariable.getValue());
		}

	}

	protected String getPossibleValueDetailAsStringBasedOnDataType(final MeasurementVariable measurementVariable) {

		if (DataType.CATEGORICAL_VARIABLE.getId().equals(measurementVariable.getDataTypeId())) {

			return convertPossibleValuesToString(measurementVariable.getPossibleValues(), POSSIBLE_VALUES_AS_STRING_DELIMITER);

		} else if (DataType.NUMERIC_VARIABLE.getId().equals(measurementVariable.getDataTypeId())) {

			return concatenateMinMaxValueIfAvailable(measurementVariable);

		} else {

			return measurementVariable.getValue();
		}

	}

	protected String convertPossibleValuesToString(final List<ValueReference> possibleValues, final String delimiter) {

		final StringBuilder sb = new StringBuilder();

		final Iterator<ValueReference> iterator = possibleValues.iterator();
		while (iterator.hasNext()) {
			sb.append(iterator.next().getName());
			if (iterator.hasNext()) {
				sb.append(delimiter);
			}
		}

		return sb.toString();

	}

	protected String concatenateMinMaxValueIfAvailable(final MeasurementVariable measurementVariable) {

		if (measurementVariable.getMinRange() == null && measurementVariable.getMaxRange() == null) {
			return NO_RANGE;
		} else if (measurementVariable.getMaxRange() == null) {
			return measurementVariable.getMinRange().toString() + MIN_ONLY;
		} else if (measurementVariable.getMinRange() == null) {
			return measurementVariable.getMaxRange().toString() + MAX_ONLY;
		} else {
			return measurementVariable.getMinRange().toString() + " - " + measurementVariable.getMaxRange().toString();
		}
	}

	private void writeObservationHeader(final int currentRowNum, final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
			final List<MeasurementVariable> variables, final List<Integer> visibleColumns) {
		if (variables != null && !variables.isEmpty()) {
			int currentColNum = 0;
			int rowNumIndex = currentColNum;
			final HSSFRow row = xlsSheet.createRow(rowNumIndex++);
			for (final MeasurementVariable variable : variables) {
				if (ExportImportStudyUtil.isColumnVisible(variable.getTermId(), visibleColumns)) {
					final HSSFCell cell = row.createCell(currentColNum++);
					cell.setCellStyle(this.getObservationHeaderStyle(variable.isFactor(), xlsBook));
					cell.setCellValue(variable.getName());
				}
			}
		}
	}

	protected CellStyle getObservationHeaderStyle(final boolean isFactor, final HSSFWorkbook xlsBook) {
		final CellStyle style;
		if (isFactor) {
			style = this.getHeaderStyle(xlsBook, 51, 153, 102);
		} else {
			style = this.getHeaderStyle(xlsBook, 51, 51, 153);
		}
		return style;
	}

	private void writeObservationRow(final int currentRowNum, final HSSFSheet xlsSheet, final MeasurementRow dataRow,
			final List<MeasurementVariable> variables, final HSSFWorkbook xlsBook, final CellStyle style,
			final List<Integer> visibleColumns, final String propertyName) {

		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		int currentColNum = 0;

		for (final MeasurementVariable variable : variables) {

			final MeasurementData dataCell = dataRow.getMeasurementData(variable.getTermId());
			if (dataCell != null) {
				if (dataCell.getMeasurementVariable() != null
						&& dataCell.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId() || !ExportImportStudyUtil
						.isColumnVisible(dataCell.getMeasurementVariable().getTermId(), visibleColumns)) {
					continue;
				}
				final HSSFCell cell = row.createCell(currentColNum++);

				if (ExportImportStudyUtil.measurementVariableHasValue(dataCell) && !dataCell.getMeasurementVariable().getPossibleValues()
						.isEmpty() && dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE.getId()
						&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE_CODE.getId() && !dataCell
						.getMeasurementVariable().getProperty().equals(propertyName)) {

					cell.setCellValue(ExportImportStudyUtil
							.getCategoricalCellValue(dataCell.getValue(), dataCell.getMeasurementVariable().getPossibleValues()));

				} else {

					if (AppConstants.NUMERIC_DATA_TYPE.getString().equalsIgnoreCase(dataCell.getDataType())) {
						if (dataCell.getValue() != null && !"".equalsIgnoreCase(dataCell.getValue()) && NumberUtils
								.isNumber(dataCell.getValue())) {
							cell.setCellType(Cell.CELL_TYPE_BLANK);
							cell.setCellType(Cell.CELL_TYPE_NUMERIC);
							cell.setCellValue(Double.valueOf(dataCell.getValue()));
						} else {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							cell.setCellValue(dataCell.getValue());
						}
					} else {
						cell.setCellValue(dataCell.getValue());
					}
				}
			}
		}
	}

	protected void setFieldbookService(final com.efficio.fieldbook.service.api.FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}

	protected void setFieldbookMiddlewareService(
			final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
