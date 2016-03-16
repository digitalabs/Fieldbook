
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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.ExcelCellStyleBuilder;
import org.generationcp.commons.parsing.ExcelWorkbookRow;
import org.generationcp.commons.parsing.GermplasmExportedWorkbook;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.util.PoiUtil;

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

	private String templateFile;

	public File export(Integer studyId, String studyName) throws CrossingTemplateExportException {
		try {
			final Workbook excelWorkbook = this.fileService.retrieveWorkbookTemplate(this.templateFile);

			// 1. parse the workbook to the template file
			final List<GermplasmList> crossesList = this.retrieveAndValidateIfHasGermplasmList(studyId);

			// 2. update description sheet
			final GermplasmList gpList = crossesList.get(0);
			gpList.setType(GermplasmListType.LST.name());

			this.writeListDetailsSection(excelWorkbook.getSheetAt(0), 1, gpList, new ExcelCellStyleBuilder((HSSFWorkbook) excelWorkbook));

			// 3. update observation sheet
			int rowIndex = 1;
			final Sheet obsSheet = excelWorkbook.getSheetAt(1);

			int measurementDataSetId = this.fieldbookMiddlewareService.getMeasurementDatasetId(studyId, studyName);
			List<Experiment> experiments = this.studyDataManager.getExperiments(measurementDataSetId, 0, Integer.MAX_VALUE, null);

			for (Experiment gpData : experiments) {
				PoiUtil.setCellValue(obsSheet, 0, rowIndex, studyName);
				PoiUtil.setCellValue(obsSheet, 1, rowIndex, Integer.parseInt(gpData.getFactors().findById(TermId.PLOT_NO).getValue()));
                PoiUtil.setCellValue(obsSheet, 2, rowIndex, gpData.getFactors().findById(TermId.DESIG).getValue());
                PoiUtil.setCellValue(obsSheet, 3, rowIndex, gpData.getFactors().findById(TermId.CROSS).getValue());
				rowIndex++;
			}

			// 4. return the resulting file back to the user
			return this.createExcelOutputFile(studyName, excelWorkbook);

		} catch (MiddlewareException | IOException | InvalidFormatException e) {
			throw new CrossingTemplateExportException(e.getMessage(), e);
		}
	}

	int writeListDetailsSection(final Sheet descriptionSheet, final int startingRow, final GermplasmList germplasmList,
			final ExcelCellStyleBuilder sheetStyles) {
		final CellStyle labelStyle = sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.LABEL_STYLE);
		final CellStyle textStyle = sheetStyles.getCellStyle(ExcelCellStyleBuilder.ExcelCellStyle.TEXT_STYLE);

		int actualRow = startingRow - 1;

		new ExcelWorkbookRow((HSSFRow) descriptionSheet.createRow(actualRow)).writeListDetailsRow(descriptionSheet,
				GermplasmExportedWorkbook.LIST_NAME, "", "Enter a list name here, or add it when saving in the BMS", labelStyle, textStyle);

		new ExcelWorkbookRow((HSSFRow) descriptionSheet.createRow(++actualRow)).writeListDetailsRow(descriptionSheet,
				GermplasmExportedWorkbook.LIST_DESCRIPTION, "", "Enter a list description here, or add it when saving in the BMS",
				labelStyle, textStyle);

		new ExcelWorkbookRow((HSSFRow) descriptionSheet.createRow(++actualRow)).writeListDetailsRow(descriptionSheet,
				GermplasmExportedWorkbook.LIST_TYPE, germplasmList.getType(), "See valid list types on Codes sheet for more options",
				labelStyle, textStyle);

		new ExcelWorkbookRow((HSSFRow) descriptionSheet.createRow(++actualRow)).writeListDetailsRow(descriptionSheet,
				GermplasmExportedWorkbook.LIST_DATE, String.valueOf(germplasmList.getDate()),
				"Accepted formats: YYYYMMDD or YYYYMM or YYYY or blank", labelStyle, textStyle);

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
