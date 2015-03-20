package com.efficio.fieldbook.web.inventory.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.exception.FileParsingException;
import com.efficio.fieldbook.web.common.service.ImportInventoryService;
import com.efficio.fieldbook.web.inventory.bean.SeedSelection;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;
import com.efficio.fieldbook.web.nursery.bean.ImportedCrossesList;
import com.efficio.fieldbook.web.nursery.bean.ImportedInventoryList;
import com.efficio.fieldbook.web.nursery.form.ImportAdvanceInventoryForm;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
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
@RequestMapping(value = ImportAdvanceInventoryController.URL)
public class ImportAdvanceInventoryController extends AbstractBaseFieldbookController {
	public static final String URL = "/importAdvanceInventory";

	public static final String IS_SUCCESS = "isSuccess";

	public static final String IS_OVERWRITE = "isOverwrite";

	private static final Logger LOG = LoggerFactory.getLogger(ImportAdvanceInventoryController.class);

	@Resource
	private ImportInventoryService importInventoryService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private InventoryService inventoryService;

	@Resource
	private SeedSelection seedSelection;

	@Override public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/importFile", method = RequestMethod.POST)
	public Map<String, Object> importFile(@ModelAttribute ImportAdvanceInventoryForm importAdvanceInventoryForm) {
		Map<String, Object> resultsMap = new HashMap<>();

		try {
			ImportedInventoryList importedData = importInventoryService.parseFile(
					importAdvanceInventoryForm.getFile());

			List<InventoryDetails> listInventoryDetails = inventoryService.getInventoryDetailsByGermplasmList(importAdvanceInventoryForm.getTargetListId(),
								GermplasmListType.ADVANCED.name());

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
	public Map<String, Object> saveInventoryImport(@ModelAttribute ImportAdvanceInventoryForm importAdvanceInventoryForm, HttpServletRequest request) {
		List<InventoryDetails> details = seedSelection.getInventoryList();
		final ContextInfo contextInfo = (ContextInfo) WebUtils
						.getSessionAttribute(request,
								ContextConstants.SESSION_ATTR_CONTEXT_INFO);
		Integer currentUserID = contextInfo.getloggedInUserId();

		Map<String, Object> resultMap = new HashMap<>();
		try {
			importInventoryService.saveUpdatedInventoryDetails(details, currentUserID, importAdvanceInventoryForm.getTargetListId());

			resultMap.put(IS_SUCCESS, 1);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			resultMap.put(IS_SUCCESS, 0);
		}

		return resultMap;
	}

	@RequestMapping(value = "/displayGermplasmDetails/{listId}", method = RequestMethod.GET)
	public String displayAdvanceGermplasmDetails(@PathVariable Integer listId,
			@ModelAttribute("seedStoreForm") SeedStoreForm form, Model model) {

		try {
			List<InventoryDetails> inventoryDetailList = seedSelection.getInventoryList();

			this.getPaginationListSelection()
					.addFinalAdvancedList(listId.toString(), inventoryDetailList);

			form.setListId(listId);
			form.setInventoryList(inventoryDetailList);
			form.setCurrentPage(1);
			form.setGidList(Integer.toString(listId));

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, "/NurseryManager/saveAdvanceInventoryImport");
	}

}