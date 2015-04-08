package com.efficio.fieldbook.web.inventory.controller;

import com.efficio.fieldbook.web.common.exception.FileParsingException;
import com.efficio.fieldbook.web.common.service.ImportInventoryService;
import com.efficio.fieldbook.web.inventory.bean.SeedSelection;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;
import com.efficio.fieldbook.web.nursery.bean.ImportedInventoryList;
import com.efficio.fieldbook.web.nursery.form.ImportInventoryForm;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/24/2015
 * Time: 4:46 PM
 */

@Controller
@RequestMapping(value = ImportInventoryController.URL)
public class ImportInventoryController extends SeedInventoryTableDisplayingController {
	public static final String URL = "/importInventory";

	public static final String IS_SUCCESS = "isSuccess";

	public static final String IS_OVERWRITE = "isOverwrite";

	public static final String ADVANCE_IMPORT_SOURCE = "advance";

	public static final String CROSSES_IMPORT_SOURCE = "crosses";

	public static final String ADVANCE_IMPORT_RESULT_VIEW = "/NurseryManager/saveAdvanceInventoryImport";

	public static final String CROSS_IMPORT_RESULT_VIEW = "/NurseryManager/saveCrossInventoryImport";

	private static final Logger LOG = LoggerFactory.getLogger(ImportInventoryController.class);

	@Resource
	private ImportInventoryService importInventoryService;

	@Resource
	private InventoryService inventoryService;

	@Resource
	private SeedSelection seedSelection;

	@Override public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/importFile", method = RequestMethod.POST)
	public Map<String, Object> importFile(@ModelAttribute ImportInventoryForm importInventoryForm) {
		Map<String, Object> resultsMap = new HashMap<>();
		String listType = null;

		if (ADVANCE_IMPORT_SOURCE.equals(importInventoryForm.getImportSource())) {
			listType = GermplasmListType.ADVANCED.name();
		} else if (CROSSES_IMPORT_SOURCE.equals(importInventoryForm.getImportSource())) {
			listType = GermplasmListType.CROSSES.name();
		}

		assert listType != null;

		try {
			ImportedInventoryList importedData = importInventoryService.parseFile(
					importInventoryForm.getFile());


			List<InventoryDetails> listInventoryDetails = inventoryService.getInventoryDetailsByGermplasmList(
					importInventoryForm.getTargetListId(),listType);

			boolean warnOfOverwrite = importInventoryService.mergeImportedData(listInventoryDetails, importedData);

			seedSelection.setInventoryList(listInventoryDetails);

			resultsMap.put(IS_SUCCESS, 1);

			if (warnOfOverwrite) {
				resultsMap.put(IS_OVERWRITE, 1);
			}

		} catch (FileParsingException e) {
			resultsMap.put(IS_SUCCESS, 0);
			String message = messageSource.getMessage(e.getMessage(), e.getMessageParameters(), Locale
					.getDefault());
			LOG.debug(message, e);
			resultsMap.put("error", message );
		} catch (MiddlewareQueryException e) {
			resultsMap.put(IS_SUCCESS, 0);
			LOG.error(e.getMessage(), e);
		}

		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value ="/save", method = RequestMethod.POST)
	public Map<String, Object> saveInventoryImport(@ModelAttribute ImportInventoryForm importInventoryForm, HttpServletRequest request) {
		List<InventoryDetails> details = seedSelection.getInventoryList();
		final ContextInfo contextInfo = getContextInfo(request);
		Integer currentUserID = contextInfo.getloggedInUserId();

		Map<String, Object> resultMap = new HashMap<>();
		try {
			importInventoryService.saveUpdatedInventoryDetails(details, currentUserID,
					importInventoryForm
							.getTargetListId());

			resultMap.put(IS_SUCCESS, 1);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			resultMap.put(IS_SUCCESS, 0);
		}

		return resultMap;
	}

	protected ContextInfo getContextInfo(HttpServletRequest request) {
		return (ContextInfo) WebUtils
				.getSessionAttribute(request,
						ContextConstants.SESSION_ATTR_CONTEXT_INFO);
	}

	@RequestMapping(value = "/displayTemporaryAdvanceGermplasmDetails/{listId}", method = RequestMethod.GET)
	public String displayAdvanceGermplasmDetails(@PathVariable Integer listId,
			@ModelAttribute("seedStoreForm") SeedStoreForm form, Model model) {

		return displayDetails(listId, form, model, ADVANCE_IMPORT_RESULT_VIEW);
	}

	@RequestMapping(value = "/displayTemporaryCrossGermplasmDetails/{listId}", method = RequestMethod.GET)
	public String displayCrossGermplasmDetails(@PathVariable Integer listId,
			@ModelAttribute("seedStoreForm") SeedStoreForm form, Model model) {

		return displayDetails(listId, form, model, CROSS_IMPORT_RESULT_VIEW);
	}

	protected String displayDetails(Integer listId, SeedStoreForm form, Model model,
			String targetView) {
		try {
			List<InventoryDetails> inventoryDetailList = seedSelection.getInventoryList();

			this.getPaginationListSelection()
					.addFinalAdvancedList(listId.toString(), inventoryDetailList);

			form.setListId(listId);
			form.setInventoryList(inventoryDetailList);
			form.setCurrentPage(1);
			form.setGidList(Integer.toString(listId));

			model.addAttribute(SeedInventoryTableDisplayingController.TABLE_HEADER_LIST,
					getSeedInventoryTableHeader());

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, targetView);
	}
}