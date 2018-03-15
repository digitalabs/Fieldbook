
package com.efficio.fieldbook.web.common.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ExportGermplasmListForm;
import com.efficio.fieldbook.web.common.service.ExportGermplasmListService;
import com.efficio.fieldbook.web.util.AppConstants;

@Controller
@RequestMapping(ExportGermplasmListController.URL)
@Configurable
public class ExportGermplasmListController extends AbstractBaseFieldbookController {

	protected static final String FILENAME = "filename";

	protected static final String OUTPUT_FILENAME = "outputFilename";

	public static final String URL = "/ExportManager";
	public static final String GERPLASM_TYPE_LST = "LST";

	@Resource
	private UserSelection userSelection;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private ExportGermplasmListService exportGermplasmListService;
	
	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();


	@ResponseBody
	@RequestMapping(value = "/exportGermplasmList/{exportType}/{studyType}", method = RequestMethod.GET,
			produces = "text/plain;charset=UTF-8")
	public String exportGermplasmList(@ModelAttribute("exportGermplasmListForm") ExportGermplasmListForm exportGermplasmListForm,
			@PathVariable int exportType, @PathVariable String studyType, HttpServletRequest req, HttpServletResponse response)
			throws GermplasmListExporterException {

		String[] clientVisibleColumnTermIds = exportGermplasmListForm.getGermplasmListVisibleColumns().split(",");

		Boolean isNursery = StudyType.N.getName().equals(studyType);
		Map<String, Boolean> visibleColumnsMap = this.getVisibleColumnsMap(clientVisibleColumnTermIds);

		return this.doExport(exportType, response, visibleColumnsMap, isNursery);
	}

	protected Map<String, Boolean> getVisibleColumnsMap(String[] termIds) {

		List<String> visibleColumnsInClient = Arrays.asList(termIds);
		Map<String, Boolean> map = new HashMap<>();

		List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();

		for (SettingDetail factor : factorsList) {

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

	protected String doExport(int exportType, HttpServletResponse response, Map<String, Boolean> visibleColumnsMap,
			Boolean isNursery) throws GermplasmListExporterException {

		String outputFileNamePath = "";
		String downloadFileName = "";

		GermplasmList list = null;
		if (this.userSelection.getImportedGermplasmMainInfo() != null) {
			list = this.fieldbookMiddlewareService.getGermplasmListById(this.userSelection.getImportedGermplasmMainInfo().getListId());
		}

		if (list != null) {

			// sanitize the list name to remove illegal characters for Windows filename.
			final String listName = FileUtils.sanitizeFileName(list.getName());

			try {
				// TODO Extract export type "1" and "2" to meaningful constants or export type enum
				if (exportType == 1) {
					downloadFileName = listName + AppConstants.EXPORT_XLS_SUFFIX.getString();
					outputFileNamePath = this.installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(listName,
							AppConstants.EXPORT_XLS_SUFFIX.getString(), this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
					this.exportGermplasmListService.exportGermplasmListXLS(outputFileNamePath, this.userSelection
							.getImportedGermplasmMainInfo().getListId(), visibleColumnsMap, isNursery);
					response.setContentType(FileUtils.MIME_MS_EXCEL);
					
				} else if (exportType == 2) {
					downloadFileName = listName + AppConstants.EXPORT_CSV_SUFFIX.getString();
					outputFileNamePath = this.installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(listName,
							AppConstants.EXPORT_CSV_SUFFIX.getString(), this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
					this.exportGermplasmListService.exportGermplasmListCSV(outputFileNamePath, visibleColumnsMap, isNursery);
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

	protected void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	protected ExportGermplasmListService getExportGermplasmListService() {
		return this.exportGermplasmListService;
	}

	protected void setExportGermplasmListService(ExportGermplasmListService exportGermplasmListService) {
		this.exportGermplasmListService = exportGermplasmListService;
	}

	protected FieldbookService getFieldbookMiddlewareService() {
		return this.fieldbookMiddlewareService;
	}

	protected void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
