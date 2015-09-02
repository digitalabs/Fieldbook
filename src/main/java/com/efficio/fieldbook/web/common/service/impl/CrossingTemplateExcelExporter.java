
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.service.impl.ExportServiceImpl;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.util.PoiUtil;

import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;

/**
 * Created by cyrus on 2/10/15.
 */
public class CrossingTemplateExcelExporter extends ExportServiceImpl {

	public static final String EXPORT_FILE_NAME_FORMAT = "CrossingTemplate-%s.xls";

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private StudyDataManager studyDataManager;

	public File export(Integer studyId, String studyName) throws CrossingTemplateExportException {
		try {
			final Workbook excelWorkbook = this.retrieveTemplate();
			final Map<String, CellStyle> workbookStyle = this.createStyles(excelWorkbook);

			// 1. parse the workbook to the template file
			List<GermplasmList> crossesList;

			crossesList = this.retrieveAndValidateIfHasGermplasmList(studyId);

			// 2. update description sheet
			GermplasmList gpList = crossesList.get(0);
			gpList.setType(GermplasmListType.LST.name());

			this.writeListDetailsSection(workbookStyle, excelWorkbook.getSheetAt(0), 1, gpList);

			// 3. update observation sheet
			int rowIndex = 1;
			final Sheet obsSheet = excelWorkbook.getSheetAt(1);

			int measurementDataSetId = this.fieldbookMiddlewareService.getMeasurementDatasetId(studyId, studyName);
			List<Experiment> experiments = this.studyDataManager.getExperiments(measurementDataSetId, 0, Integer.MAX_VALUE, null);

			for (Experiment gpData : experiments) {
				PoiUtil.setCellValue(obsSheet, 0, rowIndex, studyName);
				PoiUtil.setCellValue(obsSheet, 1, rowIndex, gpData.getFactors().findById(TermId.PLOT_NO).getValue());
				rowIndex++;
			}

			// 4. return the resulting file back to the user
			return this.createExcelOutputFile(studyName, excelWorkbook);

		} catch (MiddlewareQueryException | IOException | InvalidFormatException e) {
			throw new CrossingTemplateExportException(e.getMessage(), e);
		}
	}

	@Override
	public int writeListDetailsSection(Map<String, CellStyle> styles, Sheet descriptionSheet, int startingRow, GermplasmList germplasmList) {
		int actualRow = startingRow - 1;

		this.writeListDetailsRow(descriptionSheet, styles, actualRow, ExportServiceImpl.LIST_NAME, "",
				"Enter a list name here, or add it when saving in the BMS");

		this.writeListDetailsRow(descriptionSheet, styles, ++actualRow, ExportServiceImpl.LIST_DESCRIPTION,
				"", "Enter a list description here, or add it when saving in the BMS");

		this.writeListDetailsRow(descriptionSheet, styles, ++actualRow, ExportServiceImpl.LIST_TYPE,
				germplasmList.getType(), "See valid list types on Codes sheet for more options");

		this.writeListDetailsRow(descriptionSheet, styles, ++actualRow, ExportServiceImpl.LIST_DATE, String.valueOf(
				germplasmList.getDate()), "Accepted formats: YYYYMMDD or YYYYMM or YYYY or blank");

		return ++actualRow;
	}

	protected File createExcelOutputFile(String studyName, Workbook excelWorkbook) throws IOException {
		String outputFileName = String.format(CrossingTemplateExcelExporter.EXPORT_FILE_NAME_FORMAT, this.cleanNameValueCommas(studyName));
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

	protected VariableTypeList createPlotVariableTypeList() throws MiddlewareQueryException {
		StandardVariable plotStandardVariable = this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId());
		VariableType plotVariableType = new VariableType("PLOT_NO", "Plot", plotStandardVariable, 1);
		VariableTypeList plotVariableTypeList = new VariableTypeList();
		plotVariableTypeList.add(plotVariableType);

		return plotVariableTypeList;

	}

}
