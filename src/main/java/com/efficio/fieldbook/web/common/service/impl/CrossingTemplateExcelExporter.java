
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.util.PoiUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;

/**
 * The class providing export crossing template as an excel file function
 */
public class CrossingTemplateExcelExporter {

	public static final String EXPORT_FILE_NAME_FORMAT = "CrossingTemplate-%s.xls";
	public static final String PROGRAM_UUID = UUID.randomUUID().toString();

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

	private String templateFile;

	public File export(final Integer studyId, final String studyName, final Integer currentUserId) throws CrossingTemplateExportException {
		try {
			final Workbook excelWorkbook = this.fileService.retrieveWorkbookTemplate(this.templateFile);

			// 1. parse the workbook to the template file
			final List<GermplasmList> crossesList = this.retrieveAndValidateIfHasGermplasmList(studyId);

			// 2. update description sheet
			final GermplasmList gpList = crossesList.get(0);
			gpList.setType(GermplasmListType.LST.name());

			// 3. write details
			this.writeListDetailsSection(excelWorkbook.getSheetAt(0), 1, gpList, new ExcelCellStyleBuilder((HSSFWorkbook) excelWorkbook),
					currentUserId, studyName);

			// 4. update codes
			this.updateCodesSection(excelWorkbook.getSheetAt(2));
			
			// 5. write Nursery List Sheet
			this.writeNurseryListSheet(excelWorkbook.getSheetAt(3),new ExcelCellStyleBuilder((HSSFWorkbook) excelWorkbook), studyId,
					studyName);
			

			// return the resulting file back to the user
			return this.createExcelOutputFile(studyName, excelWorkbook);

		} catch (MiddlewareException | IOException | InvalidFormatException e) {
			throw new CrossingTemplateExportException(e.getMessage(), e);
		}
	}

	
	private void writeNurseryListSheet(Sheet nurseryListSheet, final ExcelCellStyleBuilder sheetStyles, final int studyId,
			final String studyName) {

		final int measurementDataSetId = this.fieldbookMiddlewareService.getMeasurementDatasetId(studyId, studyName);
		final List<Experiment> experiments = this.studyDataManager.getExperiments(measurementDataSetId, 0, Integer.MAX_VALUE, null);

		int rowIndex = 1;

		for (Experiment gpData : experiments) {
			PoiUtil.setCellValue(nurseryListSheet, 0, rowIndex, studyName);
			PoiUtil.setCellValue(nurseryListSheet, 1, rowIndex, Integer.parseInt(gpData.getFactors().findById(TermId.PLOT_NO).getValue()));
			PoiUtil.setCellValue(nurseryListSheet, 3, rowIndex, gpData.getFactors().findById(TermId.GID).getValue());
			PoiUtil.setCellValue(nurseryListSheet, 4, rowIndex, gpData.getFactors().findById(TermId.GID).getValue());
			PoiUtil.setCellValue(nurseryListSheet, 5, rowIndex, gpData.getFactors().findById(TermId.DESIG).getValue());
			PoiUtil.setCellValue(nurseryListSheet, 6, rowIndex, gpData.getFactors().findById(TermId.CROSS).getValue());
			if (gpData.getFactors().findById(TermId.FIELDMAP_COLUMN) != null) {
				PoiUtil.setCellValue(nurseryListSheet, 7, rowIndex, gpData.getFactors().findById(TermId.FIELDMAP_COLUMN).getValue());
			}
			if (gpData.getFactors().findById(TermId.FIELDMAP_RANGE) != null) {
				PoiUtil.setCellValue(nurseryListSheet, 8, rowIndex, gpData.getFactors().findById(TermId.FIELDMAP_RANGE).getValue());
			}
			rowIndex++;
		}
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

	int writeListDetailsSection(final Sheet descriptionSheet, final int startingRow, final GermplasmList germplasmList,
			final ExcelCellStyleBuilder sheetStyles, final Integer currentUserId, final String studyName) {
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

		new ExcelWorkbookRow((HSSFRow) descriptionSheet.createRow(++actualRow)).writeListDetailsRow(descriptionSheet,
				GermplasmExportedWorkbook.LIST_DATE, germplasmList.getDate(),
				"Accepted formats: YYYYMMDD or YYYYMM or YYYY or blank", labelStyle, textStyle);

		final String currentExportingUserName = this.fieldbookMiddlewareService.getOwnerListName(currentUserId);
		descriptionSheet.getRow(5).getCell(6).setCellValue(currentExportingUserName); //G6 cell with the Username
		descriptionSheet.getRow(6).getCell(6).setCellValue(studyName); //G7 cell with the name of the Nursery

		return ++actualRow;
	}

	public void setTemplateFile(final String templateFile) {
		this.templateFile = templateFile;
	}

	private File createExcelOutputFile(final String studyName, final Workbook excelWorkbook) throws IOException {
		String outputFileName =
				String.format(CrossingTemplateExcelExporter.EXPORT_FILE_NAME_FORMAT, StringUtil.cleanNameValueCommas(studyName));

        outputFileName = FileUtils.sanitizeFileName(outputFileName);

		try (OutputStream out = new FileOutputStream(outputFileName)) {
			excelWorkbook.write(out);
		}

		return new File(outputFileName);
	}

	List<GermplasmList> retrieveAndValidateIfHasGermplasmList(Integer studyId) throws MiddlewareQueryException,
			CrossingTemplateExportException {
		List<GermplasmList> crossesList = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.NURSERY);

		if (crossesList.isEmpty()) {
			throw new CrossingTemplateExportException("study.export.crosses.no.germplasm.list.available");
		}
		return crossesList;
	}

}
