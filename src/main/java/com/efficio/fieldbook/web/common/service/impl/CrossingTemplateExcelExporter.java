
package com.efficio.fieldbook.web.common.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.ExcelCellStyleBuilder;
import org.generationcp.commons.parsing.ExcelWorkbookRow;
import org.generationcp.commons.parsing.GermplasmExportedWorkbook;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.util.PoiUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;
import com.efficio.fieldbook.web.util.AppConstants;

/**
 * The class providing export crossing template as an excel file function
 */
public class CrossingTemplateExcelExporter {

	public static final String EXPORT_FILE_NAME_FORMAT = "CrossingTemplate-%s";
	public static final String PROGRAM_UUID = UUID.randomUUID().toString();
	public static final String FIELDMAP_COLUMN = "FIELDMAP COLUMN";
	public static final String FIELDMAP_RANGE = "FIELDMAP RANGE";

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private FileService fileService;

	@Resource
	protected ContextUtil contextUtil;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private GermplasmDataManager germplasmDataManager;

	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();
	private String templateFile;

	public FileExportInfo export(final Integer studyId, final String studyName, final Integer currentUserId) throws CrossingTemplateExportException {
		try {
			final Workbook excelWorkbook = this.fileService.retrieveWorkbookTemplate(this.templateFile);

			// 1. parse the workbook to the template file
			final List<GermplasmList> crossesList = this.retrieveAndValidateIfHasGermplasmList(studyId);

			// 2. update description sheet
			final GermplasmList gpList = crossesList.get(0);
			gpList.setType(GermplasmListType.LST.name());

			// 3. write details
			this.writeListDetailsSection(excelWorkbook.getSheetAt(0), 1, new ExcelCellStyleBuilder((HSSFWorkbook) excelWorkbook),
					currentUserId, studyName);

			// 4. update codes
			this.updateCodesSection(excelWorkbook.getSheetAt(2));

			// 5. write Study List Sheet
			this.writeStudyListSheet(excelWorkbook.getSheetAt(3), new ExcelCellStyleBuilder((HSSFWorkbook) excelWorkbook), studyId,
					studyName);

			// return the resulting file back to the user
			return this.createExcelOutputFile(studyName, excelWorkbook);

		} catch (MiddlewareException | IOException | InvalidFormatException | NullPointerException e) {
			throw new CrossingTemplateExportException(e.getMessage(), e);
		}
	}

	void writeStudyListSheet(Sheet studyListSheet, final ExcelCellStyleBuilder sheetStyles, final int studyId, final String studyName) {

		final int measurementDataSetId = this.fieldbookMiddlewareService.getMeasurementDatasetId(studyId, studyName);
		final List<Experiment> experiments = this.studyDataManager.getExperimentsOfFirstInstance(measurementDataSetId, 0, Integer.MAX_VALUE);
		int rowIndex = 1;
		int columSheet = 6;
		ArrayList<String> localNameList = new ArrayList<>();

		final CellStyle methodCellStyle = studyListSheet.getWorkbook().createCellStyle();
		methodCellStyle.cloneStyleFrom(studyListSheet.getRow(0).getCell(0).getCellStyle());
		final Row row = studyListSheet.getRow(0);

		if (!experiments.isEmpty()) {
			localNameList = getTermIdList(experiments.get(0).getFactors());
		}

		for (Experiment gpData : experiments) {
			columSheet = 6;
			PoiUtil.setCellValue(studyListSheet, 0, rowIndex, studyName);
			PoiUtil.setCellValue(studyListSheet, 1, rowIndex, Integer.parseInt(gpData.getFactors().findById(TermId.PLOT_NO).getValue()));
			if (gpData.getFactors().findById(TermId.ENTRY_TYPE) != null) {
				int entryType = Integer.parseInt(gpData.getFactors().findById(TermId.ENTRY_TYPE).getValue());
				PoiUtil.setCellValue(studyListSheet, 2, rowIndex, getEntryTypeName(entryType));
			}
			PoiUtil.setCellValue(studyListSheet, 3, rowIndex, gpData.getFactors().findById(TermId.GID).getValue());
			PoiUtil.setCellValue(studyListSheet, 4, rowIndex, gpData.getFactors().findById(TermId.GID).getValue());
			PoiUtil.setCellValue(studyListSheet, 5, rowIndex, gpData.getFactors().findById(TermId.DESIG).getValue());

			if (gpData.getFactors().findById(TermId.CROSS) != null) {
				PoiUtil.setCellValue(studyListSheet, 6, rowIndex, gpData.getFactors().findById(TermId.CROSS).getValue());
			}else{
				PoiUtil.setCellValue(studyListSheet, 6, rowIndex, "-");
			}

			if (gpData.getFactors().findById(TermId.FIELDMAP_COLUMN) != null) {
				if (rowIndex == 1) {
					addHeaderToRow(row, methodCellStyle, FIELDMAP_COLUMN);
				}
				PoiUtil.setCellValue(studyListSheet, 7, rowIndex, gpData.getFactors().findById(TermId.FIELDMAP_COLUMN).getValue());
			}
			if (gpData.getFactors().findById(TermId.FIELDMAP_RANGE) != null) {
				if (rowIndex == 1) {
					addHeaderToRow(row, methodCellStyle, FIELDMAP_RANGE);
				}
				PoiUtil.setCellValue(studyListSheet, 8, rowIndex, gpData.getFactors().findById(TermId.FIELDMAP_RANGE).getValue());
				columSheet = 8;
			}
			for (String localname : localNameList) {
				if (rowIndex == 1) {
					addHeaderToRow(row, methodCellStyle, localname);
				}
				PoiUtil.setCellValue(studyListSheet, ++columSheet, rowIndex, gpData.getFactors().findByLocalName(localname).getValue());
			}
			rowIndex++;
		}
		// AutoSizeColumn
		for (int i = 0; i <= columSheet; i++) {
			studyListSheet.autoSizeColumn(i);
		}
	}

	private void addHeaderToRow(Row row, CellStyle methodCellStyle, String name) {
		int column = row.getLastCellNum();
		row.createCell(column).setCellValue(name);
		row.getCell(column).setCellStyle(methodCellStyle);
		row.setHeight((short) -1);

	}
	
	private ArrayList<String> getTermIdList(VariableList factors) {
		ArrayList<String> termIdList = new ArrayList<>();
		List<Variable> variables = factors.getVariables();
		for (Variable vars : variables) {
			String name = vars.getVariableType().getLocalName();
			int termId = vars.getVariableType().getId();

			if (termId != TermId.TRIAL_INSTANCE_FACTOR.getId() && termId != TermId.ENTRY_NO.getId() && termId != TermId.PLOT_NO.getId()
					&& termId != TermId.ENTRY_TYPE.getId() && termId != TermId.GID.getId() && termId != TermId.DESIG.getId()
					&& termId != TermId.CROSS.getId() && termId != TermId.FIELDMAP_COLUMN.getId()
					&& termId != TermId.FIELDMAP_RANGE.getId()) {
				termIdList.add(name);
			}
		}
		return termIdList;
	}

	private String getEntryTypeName(final int entryType_ID) {

		if (SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId() == entryType_ID) {
			return SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeName();
		} else if (SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeCategoricalId() == entryType_ID) {
			return SystemDefinedEntryType.DISEASE_CHECK.getEntryTypeName();
		} else if (SystemDefinedEntryType.STRESS_CHECK.getEntryTypeCategoricalId() == entryType_ID) {
			return SystemDefinedEntryType.STRESS_CHECK.getEntryTypeName();
		} else if (SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() == entryType_ID) {
			return SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName();

		}

		return "";
	}

	void updateCodesSection(final Sheet codesSheet) {
		int startingRow = codesSheet.getLastRowNum();

		// Users

		final List<User> allProgramMembers =
				this.workbenchDataManager.getUsersByProjectId(this.contextUtil.getProjectInContext().getProjectId());

		final CellStyle userCellStyle = codesSheet.getWorkbook().createCellStyle();
		userCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		userCellStyle.setFillForegroundColor(IndexedColors.VIOLET.getIndex());

		for (final User user : allProgramMembers) {
			final Row row = codesSheet.createRow(++startingRow);
			row.createCell(0).setCellValue(GermplasmExportedWorkbook.CONDITION);
			row.createCell(1).setCellValue(GermplasmExportedWorkbook.USER);
			row.getCell(0).setCellStyle(userCellStyle);
			row.getCell(1).setCellStyle(userCellStyle);

			row.createCell(2).setCellValue(user.getUserid().toString());
			final Person person = this.workbenchDataManager.getPersonById(user.getPersonid());
			row.createCell(3).setCellValue(person.getDisplayName());
		}

		// Methods

		final List<Method> methods = this.germplasmDataManager.getMethodsByType("GEN", this.contextUtil.getCurrentProgramUUID());

		final CellStyle methodCellStyle = codesSheet.getWorkbook().createCellStyle();
		methodCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		methodCellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());

		for (final Method method : methods) {
			final Row row = codesSheet.createRow(++startingRow);
			row.createCell(0).setCellValue(GermplasmExportedWorkbook.VARIATE);
			row.createCell(1).setCellValue(GermplasmExportedWorkbook.BREEDING_METHOD);

			row.getCell(0).setCellStyle(methodCellStyle);
			row.getCell(1).setCellStyle(methodCellStyle);

			row.createCell(2).setCellValue(method.getMcode());
			row.createCell(3).setCellValue(method.getMname());
		}

	}

	int writeListDetailsSection(final Sheet descriptionSheet, final int startingRow, final ExcelCellStyleBuilder sheetStyles,
			final Integer currentUserId, final String studyName) {
		final CellStyle labelStyle = sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.LABEL_STYLE);
		final CellStyle textStyle = sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.NUMERIC_STYLE);

		int actualRow = startingRow - 1;

		descriptionSheet.setZoom(1, 1); //100% magnification
		descriptionSheet.setDefaultRowHeight((short)360); //0.25 inch = 360.0181426466 twips

		new ExcelWorkbookRow((HSSFRow) descriptionSheet.createRow(actualRow)).writeListDetailsRow(descriptionSheet,
				GermplasmExportedWorkbook.LIST_NAME, "", "Enter a list name here, or add it when saving in the BMS", labelStyle, textStyle);

		new ExcelWorkbookRow((HSSFRow) descriptionSheet.createRow(++actualRow)).writeListDetailsRow(descriptionSheet,
				GermplasmExportedWorkbook.LIST_DESCRIPTION, "", "Enter a list description here, or add it when saving in the BMS",
				labelStyle, textStyle);

		final Date todaysDate = new Date();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		final String todaysDateText = dateFormat.format(todaysDate);

		new ExcelWorkbookRow((HSSFRow) descriptionSheet.createRow(++actualRow)).writeListDetailsRow(descriptionSheet,
				GermplasmExportedWorkbook.LIST_DATE, Long.parseLong(todaysDateText),
				"Accepted formats: YYYYMMDD or YYYYMM or YYYY or blank", labelStyle, textStyle);

		final String currentExportingUserName = this.fieldbookMiddlewareService.getOwnerListName(currentUserId);
		descriptionSheet.getRow(5).getCell(6).setCellValue(currentExportingUserName); //G6 cell with the Username
		descriptionSheet.getRow(6).getCell(6).setCellValue(studyName); //G7 cell with the name of the Nursery

		return ++actualRow;
	}

	public void setTemplateFile(final String templateFile) {
		this.templateFile = templateFile;
	}

	private FileExportInfo createExcelOutputFile(final String studyName, final Workbook excelWorkbook) throws IOException {
		String downloadFilename = String.format(CrossingTemplateExcelExporter.EXPORT_FILE_NAME_FORMAT,
				StringUtil.replaceInvalidChacaracterFileName(studyName, "_"));
		downloadFilename = FileUtils.sanitizeFileName(downloadFilename);
		final String outputFilepath = this.installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(downloadFilename,
				AppConstants.EXPORT_XLS_SUFFIX.getString(), this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);

		try (OutputStream out = new FileOutputStream(outputFilepath)) {
			excelWorkbook.write(out);
		}

		return new FileExportInfo(outputFilepath, downloadFilename + AppConstants.EXPORT_XLS_SUFFIX.getString());
	}

	List<GermplasmList> retrieveAndValidateIfHasGermplasmList(Integer studyId) throws CrossingTemplateExportException {
		List<GermplasmList> crossesList = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.NURSERY);

		if (crossesList.isEmpty()) {
			throw new CrossingTemplateExportException("study.export.crosses.no.germplasm.list.available");
		}
		return crossesList;
	}

}
