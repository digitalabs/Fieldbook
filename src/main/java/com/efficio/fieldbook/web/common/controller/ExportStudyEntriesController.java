
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ExportGermplasmListForm;
import com.efficio.fieldbook.web.common.service.ExportStudyEntriesService;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(ExportStudyEntriesController.URL)
@Configurable
@Transactional
public class ExportStudyEntriesController extends AbstractBaseFieldbookController {

	public static final String EXPORTED_GERMPLASM_LIST = "Exported Germplasm List";
	protected static final String FILENAME = "filename";

	protected static final String OUTPUT_FILENAME = "outputFilename";

	public static final String URL = "/ExportManager";
	public static final String GERPLASM_TYPE_LST = "LST";

	@Resource
	private UserSelection userSelection;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private ExportStudyEntriesService exportStudyEntriesService;

	@Resource
	private StudyEntryService studyEntryService;

	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	@ResponseBody
	@RequestMapping(value = "/exportGermplasmList/{exportType}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public String exportStudyEntries(@ModelAttribute("exportGermplasmListForm") final ExportGermplasmListForm exportGermplasmListForm,
									 @PathVariable final int exportType, final HttpServletResponse response) throws GermplasmListExporterException {

		final String[] clientVisibleColumnTermIds = exportGermplasmListForm.getGermplasmListVisibleColumns().split(",");
		final Map<String, Boolean> visibleColumnsMap = this.getVisibleColumnsMap(clientVisibleColumnTermIds);
		return this.doExport(exportType, response, visibleColumnsMap);
	}

	protected Map<String, Boolean> getVisibleColumnsMap(final String[] termIds) {

		final List<String> visibleColumnsInClient = Arrays.asList(termIds);
		final Map<String, Boolean> map = new HashMap<>();

		final List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();

		for (final SettingDetail factor : factorsList) {

			if (!factor.isHidden()
				&& !"0".equals(visibleColumnsInClient.get(0))
				&& (factor.getVariable().getCvTermId().equals(TermId.GID.getId())
				|| factor.getVariable().getCvTermId().equals(TermId.ENTRY_NO.getId()) || factor.getVariable().getCvTermId()
				.equals(TermId.DESIG.getId()))) {

				map.put(factor.getVariable().getCvTermId().toString(), true);

			} else if (!factor.isHidden() && !"0".equals(visibleColumnsInClient.get(0))
				&& !visibleColumnsInClient.contains(factor.getVariable().getCvTermId().toString())) {
				map.put(factor.getVariable().getCvTermId().toString(), false);

			} else if (!factor.isHidden()) {
				map.put(factor.getVariable().getCvTermId().toString(), true);
			}

		}

		return map;
	}

	protected String doExport(final int exportType, final HttpServletResponse response, final Map<String, Boolean> visibleColumnsMap)
		throws GermplasmListExporterException {

		String outputFileNamePath = "";
		String downloadFileName = "";

		final int studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
		final boolean studyHasEntries =
			this.studyEntryService.countStudyEntries(studyId) > 0l;

		if (studyHasEntries) {

			try {
				// TODO Extract export type "1" and "2" to meaningful constants or export type enum
				final String directory = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
				if (exportType == 1) {
					downloadFileName = FileNameGenerator.generateFileName(directory, AppConstants.EXPORT_XLS_SUFFIX.getString(), EXPORTED_GERMPLASM_LIST);
					outputFileNamePath =
						this.installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(FileNameGenerator.removeExtension(downloadFileName),
							AppConstants.EXPORT_XLS_SUFFIX.getString(), this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);

					this.exportStudyEntriesService.exportAsExcelFile(studyId, outputFileNamePath, visibleColumnsMap);
					response.setContentType(FileUtils.MIME_MS_EXCEL);

				} else if (exportType == 2) {
					downloadFileName = FileNameGenerator.generateFileName(directory, AppConstants.EXPORT_CSV_SUFFIX.getString(), EXPORTED_GERMPLASM_LIST);
					outputFileNamePath = this.installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(
						FileNameGenerator.removeExtension(downloadFileName),
						AppConstants.EXPORT_CSV_SUFFIX.getString(), this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
					this.exportStudyEntriesService.exportAsCSVFile(studyId, outputFileNamePath, visibleColumnsMap);
					response.setContentType(FileUtils.MIME_CSV);
				}
			} catch (final IOException e) {
				throw new GermplasmListExporterException(e.getMessage(), e);
			}
		}

		final Map<String, Object> results = new HashMap<>();
		results.put(OUTPUT_FILENAME, outputFileNamePath);
		results.put(FILENAME, downloadFileName);
		results.put("contentType", response.getContentType());

		return super.convertObjectToJson(results);
	}

	@Override
	public String getContentName() {

		return null;
	}

	protected UserSelection getUserSelection() {
		return this.userSelection;
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	protected ExportStudyEntriesService getExportStudyEntriesService() {
		return this.exportStudyEntriesService;
	}

	protected void setExportStudyEntriesService(final ExportStudyEntriesService exportStudyEntriesService) {
		this.exportStudyEntriesService = exportStudyEntriesService;
	}

	protected FieldbookService getFieldbookMiddlewareService() {
		return this.fieldbookMiddlewareService;
	}

	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
