package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.service.impl.ExportServiceImpl;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.util.PoiUtil;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Created by cyrus on 2/10/15.
 */
public class CrossingTemplateExcelExporter extends ExportServiceImpl {
	public static final String EXPORT_FILE_NAME_FORMAT = "CrossingTemplate-%s.xls";

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private FileService fileService;

	private File templateFile;

	public File export(Integer studyId, String studyName)
			throws CrossingTemplateExportException {
		try {
			final Workbook excelWorkbook = retrieveTemplate();
			final Map<String, CellStyle> workbookStyle = this.createStyles(excelWorkbook);

			// 1. parse the workbook to the template file
			List<GermplasmList> crossesList;

			crossesList = retrieveAndValidateIfHasGermplasmList(studyId);

			// 2. update description sheet
			GermplasmList gpList = crossesList.get(0);
			gpList.setType(GermplasmListType.LST.name());

			this.writeListDetailsSection(workbookStyle, excelWorkbook.getSheetAt(0), 1,
					gpList);

			// 3. update observation sheet
			int rowIndex = 1;
			final Sheet obsSheet = excelWorkbook.getSheetAt(1);

			List<ListDataProject> gpListData = fieldbookMiddlewareService
					.getListDataProject(gpList.getId());

			for (ListDataProject gpData : gpListData) {
				PoiUtil.setCellValue(obsSheet, 0, rowIndex, studyName);
				PoiUtil.setCellValue(obsSheet, 1, rowIndex, gpData.getEntryId());
				rowIndex++;
			}

			// 4. return the resulting file back to the user
			return createExcelOutputFile(studyName, excelWorkbook);

		} catch (MiddlewareQueryException | IOException | InvalidFormatException e) {
			throw new CrossingTemplateExportException(e.getMessage(), e);
		}
	}

	protected File createExcelOutputFile(String studyName, Workbook excelWorkbook)
			throws IOException {
		String outputFileName = String
				.format(EXPORT_FILE_NAME_FORMAT, cleanNameValueCommas(studyName));
		try (OutputStream out = new FileOutputStream(outputFileName)) {
			excelWorkbook.write(out);
		}

		return new File(outputFileName);
	}

	List<GermplasmList> retrieveAndValidateIfHasGermplasmList(Integer studyId)
			throws MiddlewareQueryException, CrossingTemplateExportException {
		List<GermplasmList> crossesList = fieldbookMiddlewareService.getGermplasmListsByProjectId(
				studyId,
				GermplasmListType.NURSERY);

		if (0 >= crossesList.size()) {
			throw new CrossingTemplateExportException(
					"study.export.crosses.no.germplasm.list.available");
		}
		return crossesList;
	}

	protected Workbook retrieveTemplate() throws IOException, InvalidFormatException {
		try (InputStream is = new FileInputStream(templateFile)) {
			String tempFile = fileService.saveTemporaryFile(is);

			return fileService.retrieveWorkbook(tempFile);
		}
	}

	public void setTemplateFile(File templateFile) {
		this.templateFile = templateFile;
	}

}
