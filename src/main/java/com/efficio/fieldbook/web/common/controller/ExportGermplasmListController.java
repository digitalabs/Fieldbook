package com.efficio.fieldbook.web.common.controller;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.middleware.dao.GermplasmListDAO;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.efficio.fieldbook.web.util.FieldbookProperties;

@Controller
@RequestMapping(ExportGermplasmListController.URL)
@Configurable
public class ExportGermplasmListController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(ExportGermplasmListController.class);
	
	public static final String URL = "/ExportManager";
	
	@Resource
	private UserSelection userSelection;
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private ExportGermplasmListService exportGermplasmListService;
	
	public static final String GERPLASM_TYPE_LST = "LST";

	@ResponseBody
	@RequestMapping(value = "/exportGermplasmList/{exportType}/{studyType}", method = RequestMethod.GET)
	public String exportGermplasmList(
			@ModelAttribute("exportGermplasmListForm") ExportGermplasmListForm exportGermplasmListForm,
			@PathVariable int exportType,
			@PathVariable String studyType,
			HttpServletRequest req,
			HttpServletResponse response) throws GermplasmListExporterException {

		String[] clientVisibleColumnTermIds = exportGermplasmListForm
				.getGermplasmListVisibleColumns().split(",");

		Boolean isNursery = "N".equals(studyType);
		Map<String, Boolean> visibleColumnsMap = getVisibleColumnsMap(clientVisibleColumnTermIds, isNursery);
		
		return doExport(exportType, response, req, visibleColumnsMap, isNursery);
	}
	
	protected Map<String, Boolean> getVisibleColumnsMap(String[] termIds, Boolean isNursery) {

		List<String> visibleColumnsInClient = Arrays.asList(termIds);
		Map<String, Boolean> map = new HashMap<>();

		List<SettingDetail> factorsList = userSelection.getPlotsLevelList();
		
		if (isNursery){
			
			map.put(String.valueOf(TermId.GID.getId()), true);
			map.put(String.valueOf(TermId.CROSS.getId()), true);
			map.put(String.valueOf(TermId.ENTRY_NO.getId()), true);
			map.put(String.valueOf(TermId.DESIG.getId()), true);
			
		}else{
			for (SettingDetail factor : factorsList) {
				
				if (!factor.isHidden() && !"0".equals(visibleColumnsInClient.get(0))
						&& (factor.getVariable().getCvTermId().equals(TermId.GID.getId())
								|| factor.getVariable().getCvTermId().equals(TermId.ENTRY_NO.getId()) 
								|| factor.getVariable().getCvTermId().equals(TermId.DESIG.getId()))) {

					map.put(factor.getVariable().getCvTermId().toString(), true);

				} else if (!factor.isHidden() && !"0".equals(visibleColumnsInClient.get(0))
						&& !visibleColumnsInClient.contains(factor.getVariable().getCvTermId()
								.toString())) {

					map.put(factor.getVariable().getCvTermId().toString(), false);

				} else if (!factor.isHidden()) {

					map.put(factor.getVariable().getCvTermId().toString(), true);
				}

			}
		}
		
		return map;
	}
	protected void setExportListTypeFromOriginalGermplasm(GermplasmList list) throws MiddlewareQueryException{
		if(list != null && list.getListRef() != null){
			GermplasmList origList = fieldbookMiddlewareService.getGermplasmListById(list.getListRef());
			
			if(origList != null){
				if(origList.getStatus() != GermplasmListDAO.STATUS_DELETED){
					list.setType(origList.getType());
				}else{
					list.setType(GERPLASM_TYPE_LST);
				}
			}
		}
	}
	protected String doExport(int exportType, HttpServletResponse response,
			HttpServletRequest req, Map<String, Boolean> visibleColumnsMap, Boolean isNursery)
			throws GermplasmListExporterException {

		String outputFileNamePath = "";
		String fileName = "";
		String listName = "GermplasmList";
		
		GermplasmList list = null;
		try {
			if (userSelection.getImportedGermplasmMainInfo() != null) {
				list = fieldbookMiddlewareService.getGermplasmListById(userSelection.getImportedGermplasmMainInfo().getListId());
				setExportListTypeFromOriginalGermplasm(list);
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		
		if (list != null){
			listName = list.getName();
			
			if (exportType == 1) {

				fileName = listName + ".xls";
				outputFileNamePath = getFieldbookProperties().getUploadDirectory() + File.separator  + fileName;
				exportGermplasmListService.exportGermplasmListXLS(outputFileNamePath, userSelection.getImportedGermplasmMainInfo().getListId(),
						visibleColumnsMap, isNursery);
				response.setContentType("application/vnd.ms-excel");

			} else if (exportType == 2) {

				fileName = listName + ".csv";
				outputFileNamePath = getFieldbookProperties().getUploadDirectory() + File.separator + fileName;
				exportGermplasmListService.exportGermplasmListCSV(outputFileNamePath, visibleColumnsMap, isNursery);
				response.setContentType("text/csv");
			}
		}

		Map<String, Object> results = new HashMap<String, Object>();
		results.put("outputFilename", outputFileNamePath);
		results.put("filename", fileName);
		results.put("contentType", response.getContentType());

		return super.convertObjectToJson(results);
	}

	@Override
	public String getContentName() {
		
		return null;
	}
	
	protected UserSelection getUserSelection() {
		return userSelection;
	}

	protected void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
	}
	
	protected FieldbookProperties getFieldbookProperties(){
		return super.fieldbookProperties;
	}
	
	protected ExportGermplasmListService getExportGermplasmListService() {
		return exportGermplasmListService;
	}

	protected void setExportGermplasmListService(ExportGermplasmListService exportGermplasmListService) {
		this.exportGermplasmListService = exportGermplasmListService;
	}
	
	protected FieldbookService getFieldbookMiddlewareService() {
		return fieldbookMiddlewareService;
	}

	protected void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
