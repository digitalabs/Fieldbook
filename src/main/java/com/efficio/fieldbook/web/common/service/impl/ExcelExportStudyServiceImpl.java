/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

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
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.OntologyService;
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
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.ZipUtil;

@Service
@Transactional
public class ExcelExportStudyServiceImpl implements ExcelExportStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(ExcelExportStudyServiceImpl.class);

	private static final int PIXEL_SIZE = 250;

	private static final String OCC_8170_LABEL = "8170_LABEL";
	private static final String PLOT = "PLOT";

	@Resource
	private MessageSource messageSource;

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	protected static final List<Integer> STUDY_DETAILS_IDS = Arrays.asList(TermId.STUDY_NAME.getId(), TermId.STUDY_TITLE.getId(),
			TermId.PM_KEY.getId(), TermId.STUDY_OBJECTIVE.getId(), TermId.START_DATE.getId(), TermId.END_DATE.getId(),
			TermId.STUDY_TYPE.getId(), TermId.STUDY_UID.getId(), TermId.STUDY_STATUS.getId());
	private String breedingMethodPropertyName = "";

	@Override
	public String export(final Workbook workbook, final String filename, final List<Integer> instances) throws IOException {
		return this.export(workbook, filename, instances, null);
	}

	@Override
	public String export(final Workbook workbook, final String filename, final List<Integer> instances, final List<Integer> visibleColumns)
			throws IOException {
		FileOutputStream fos = null;
		final List<String> filenameList = new ArrayList<String>();
		String outputFilename = null;

		this.breedingMethodPropertyName = this.ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getTerm().getName();

		for (final Integer trialInstanceNo : instances) {
			final List<Integer> indexes = new ArrayList<Integer>();
			indexes.add(trialInstanceNo);

			final List<MeasurementRow> plotLevelObservations =
					ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getExportArrangedObservations(), indexes);

			try {
				final MeasurementRow instanceLevelObservation = workbook.getTrialObservationByTrialInstanceNo(trialInstanceNo);

				final HSSFWorkbook xlsBook = new HSSFWorkbook();
				this.writeDescriptionSheet(xlsBook, workbook, instanceLevelObservation, visibleColumns);
				this.writeObservationSheet(xlsBook, workbook, plotLevelObservations, visibleColumns);

				final String filenamePath =
						ExportImportStudyUtil.getFileNamePath(trialInstanceNo, instanceLevelObservation, instances, filename,
								workbook.isNursery(), this.fieldbookProperties, this.fieldbookMiddlewareService);

				fos = new FileOutputStream(new File(filenamePath));
				xlsBook.write(fos);

				outputFilename = filenamePath;
				filenameList.add(filenamePath);

			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}

		// multiple instances
		if (instances != null && instances.size() > 1) {
			outputFilename =
					this.fieldbookProperties.getUploadDirectory() + File.separator
							+ filename.replaceAll(AppConstants.EXPORT_XLS_SUFFIX.getString(), "")
							+ AppConstants.ZIP_FILE_SUFFIX.getString();
			ZipUtil.zipIt(outputFilename, filenameList);
		}

		return outputFilename;
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
			final List<Integer> visibleColumns) {
		final Locale locale = LocaleContextHolder.getLocale();
		final HSSFSheet xlsSheet = xlsBook.createSheet(this.messageSource.getMessage("export.study.sheet.observation", null, locale));
		int currentRowNum = 0;

		this.writeObservationHeader(currentRowNum++, xlsBook, xlsSheet, workbook.getMeasurementDatasetVariables(), visibleColumns);

		final CellStyle style = this.createCellStyle(xlsBook);

		for (final MeasurementRow dataRow : observations) {
			this.writeObservationRow(currentRowNum++, xlsSheet, dataRow, workbook.getMeasurementDatasetVariables(), xlsBook, style,
					visibleColumns);
		}
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
				studyDetails.getTitle() != null ? HtmlUtils.htmlUnescape(studyDetails.getTitle()) : "");
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
		this.writeStudyDetailRow(xlsBook, xlsSheet, rowNumIndex++, "export.study.description.details.studytype", studyDetails
				.getStudyType().name());

		return rowNumIndex;
	}

	private PhenotypicType getRoleOfVariableInTrialObservations(final MeasurementVariable variable, final MeasurementRow trialObservation) {
		if (trialObservation != null && variable != null) {
			for (final MeasurementData measurementData : trialObservation.getDataList()) {
				if (measurementData.getMeasurementVariable() != null
						&& measurementData.getMeasurementVariable().getTermId() == variable.getTermId()) {
					return measurementData.getMeasurementVariable().getRole();
				}
			}
		}
		return variable.getRole();
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
		final List<MeasurementVariable> arrangedConditions = new ArrayList<MeasurementVariable>();
		List<MeasurementVariable> filteredConditions = new ArrayList<MeasurementVariable>();
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
						variable.setValue(trialObservation.getMeasurementDataValue(variable.getTermId()));
						if (variable.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
							variable.setPossibleValues(this.fieldbookService.getAllPossibleValues(variable.getTermId()));
						}
					}
				}
			}
		}
		filteredConditions = workbook.arrangeMeasurementVariables(filteredConditions);
		return this.writeSection(currentRowNum, xlsBook, xlsSheet, filteredConditions, "export.study.description.column.condition", 51,
				153, 102);
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
		List<MeasurementVariable> filteredFactors = new ArrayList<MeasurementVariable>();
		for (final MeasurementVariable factor : factors) {
			if (factor.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()
					&& ExportImportStudyUtil.isColumnVisible(factor.getTermId(), visibleColumns)) {
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

		List<MeasurementVariable> filteredConstants = new ArrayList<MeasurementVariable>();
		for (final MeasurementVariable variable : constants) {
			filteredConstants.add(variable);
			if (PhenotypicType.VARIATE == variable.getRole()) {
				variable.setValue(trialObservation.getMeasurementDataValue(variable.getName()));
			}
		}
		filteredConstants = workbook.arrangeMeasurementVariables(filteredConstants);
		return this.writeSection(currentRowNum, xlsBook, xlsSheet, filteredConstants, "export.study.description.column.constant", 51, 51,
				153);
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

		List<MeasurementVariable> filteredVariates = new ArrayList<MeasurementVariable>();
		for (final MeasurementVariable variate : variates) {
			if (ExportImportStudyUtil.isColumnVisible(variate.getTermId(), visibleColumns)) {
				filteredVariates.add(variate);
			}
		}
		filteredVariates = workbook.arrangeMeasurementVariables(filteredVariates);
		return this.writeSection(currentRowNum, xlsBook, xlsSheet, filteredVariates, "export.study.description.column.variate", 51, 51,
				153, true);
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

		HSSFCell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage(typeLabel, null, locale));

		cell = row.createCell(1, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.description", null, locale));

		cell = row.createCell(2, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.property", null, locale));

		cell = row.createCell(3, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.scale", null, locale));

		cell = row.createCell(4, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.method", null, locale));

		cell = row.createCell(5, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.datatype", null, locale));

		cell = row.createCell(6, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));
		cell.setCellValue(this.messageSource.getMessage("export.study.description.column.value", null, locale));

		// If typeLabel is constant or variate, the label column should be
		// 'SAMPLE LEVEL'
		cell = row.createCell(7, Cell.CELL_TYPE_STRING);
		cell.setCellStyle(this.getHeaderStyle(xlsBook, c1, c2, c3));

		if ("export.study.description.column.constant".equals(typeLabel) || "export.study.description.column.variate".equals(typeLabel)) {
			cell.setCellValue(this.messageSource.getMessage("export.study.description.column.samplelevel", null, locale));

		} else {
			cell.setCellValue(this.messageSource.getMessage("export.study.description.column.label", null, locale));

		}

	}

	private void writeSectionRow(final int currentRowNum, final HSSFSheet xlsSheet, final MeasurementVariable variable) {
		final HSSFRow row = xlsSheet.createRow(currentRowNum);

		HSSFCell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
		String occName = variable.getName();
		final String appConstant8170 = AppConstants.getString(ExcelExportStudyServiceImpl.OCC_8170_LABEL);
		if (appConstant8170 != null && appConstant8170.equalsIgnoreCase(occName)) {
			occName = AppConstants.OCC.getString();
		}
		cell.setCellValue(occName);

		cell = row.createCell(1, Cell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getDescription());

		cell = row.createCell(2, Cell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getProperty());

		cell = row.createCell(3, Cell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getScale());

		cell = row.createCell(4, Cell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getMethod());

		cell = row.createCell(5, Cell.CELL_TYPE_STRING);
		cell.setCellValue(variable.getDataTypeDisplay());

		cell = row.createCell(6, Cell.CELL_TYPE_STRING);
		this.cleanupValue(variable);

		variable.setPossibleValues(this.fieldbookService.getAllPossibleValues(variable.getTermId()));

		if (variable != null && variable.getPossibleValues() != null && !variable.getPossibleValues().isEmpty()
				&& variable.getTermId() != TermId.BREEDING_METHOD_VARIATE.getId()
				&& variable.getTermId() != TermId.BREEDING_METHOD_VARIATE_CODE.getId()
				&& !variable.getProperty().equals(this.breedingMethodPropertyName) && variable.getTermId() != TermId.PI_ID.getId()
				&& variable.getTermId() != Integer.parseInt(AppConstants.COOPERATOR_ID.getString())
				&& variable.getTermId() != TermId.LOCATION_ID.getId()) {
			cell.setCellValue(ExportImportStudyUtil.getCategoricalCellValue(variable.getValue(), variable.getPossibleValues()));
		} else if (variable.getDataTypeId() != null && variable.getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())) {
			if (variable.getValue() != null && !"".equalsIgnoreCase(variable.getValue())) {
				cell.setCellType(Cell.CELL_TYPE_BLANK);
				cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(Double.valueOf(variable.getValue()));
			} else {
				cell.setCellValue(variable.getValue());
			}
		} else {
			cell.setCellValue(variable.getValue());
		}

		cell = row.createCell(7, Cell.CELL_TYPE_STRING);
		if (variable.getTreatmentLabel() != null && !"".equals(variable.getTreatmentLabel())) {
			cell.setCellValue(variable.getTreatmentLabel());
		} else {
			cell.setCellValue(variable.getLabel());
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
		CellStyle style;
		if (isFactor) {
			style = this.getHeaderStyle(xlsBook, 51, 153, 102);
		} else {
			style = this.getHeaderStyle(xlsBook, 51, 51, 153);
		}
		return style;
	}

	private void writeObservationRow(final int currentRowNum, final HSSFSheet xlsSheet, final MeasurementRow dataRow,
			final List<MeasurementVariable> variables, final HSSFWorkbook xlsBook, final CellStyle style, final List<Integer> visibleColumns) {

		final HSSFRow row = xlsSheet.createRow(currentRowNum);
		int currentColNum = 0;

		for (final MeasurementVariable variable : variables) {

			final MeasurementData dataCell = dataRow.getMeasurementData(variable.getTermId());
			if (dataCell != null) {
				if (dataCell.getMeasurementVariable() != null
						&& dataCell.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()
						|| !ExportImportStudyUtil.isColumnVisible(dataCell.getMeasurementVariable().getTermId(), visibleColumns)) {
					continue;
				}
				final HSSFCell cell = row.createCell(currentColNum++);

				if (ExportImportStudyUtil.measurementVariableHasValue(dataCell)
						&& !dataCell.getMeasurementVariable().getPossibleValues().isEmpty()
						&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE.getId()
						&& dataCell.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE_CODE.getId()
						&& !dataCell.getMeasurementVariable().getProperty()
								.equals(ExportImportStudyUtil.getPropertyName(this.ontologyService))) {

					cell.setCellValue(ExportImportStudyUtil.getCategoricalCellValue(dataCell.getValue(), dataCell.getMeasurementVariable()
							.getPossibleValues()));

				} else {

					if (AppConstants.NUMERIC_DATA_TYPE.getString().equalsIgnoreCase(dataCell.getDataType())) {
						if (dataCell.getValue() != null && !"".equalsIgnoreCase(dataCell.getValue())
								&& NumberUtils.isNumber(dataCell.getValue())) {
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

	private void cleanupValue(final MeasurementVariable variable) {
		if (variable.getValue() != null) {
			variable.setValue(variable.getValue().trim());
			final List<Integer> specialDropdowns = this.getSpecialDropdownIds();
			if (specialDropdowns.contains(variable.getTermId()) && "0".equals(variable.getValue())) {
				variable.setValue("");
			} else if (variable.getDataTypeId().equals(TermId.DATE_VARIABLE.getId()) && "0".equals(variable.getValue())) {
				variable.setValue("");
			}
		}
	}

	private List<Integer> getSpecialDropdownIds() {
		final List<Integer> ids = new ArrayList<Integer>();

		final String idNameCombo = AppConstants.ID_NAME_COMBINATION.getString();
		final String[] idNames = idNameCombo.split(",");
		for (final String idName : idNames) {
			ids.add(Integer.valueOf(idName.substring(0, idName.indexOf("|"))));
		}

		return ids;
	}

	protected void setFieldbookService(final com.efficio.fieldbook.service.api.FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}

	protected void setFieldbookMiddlewareService(final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}
}
