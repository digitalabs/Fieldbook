
package com.efficio.fieldbook.web.inventory.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.inventory.bean.SeedSelection;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;

/**
 * The Class SeedStoreManagerController.
 */
@Controller
@RequestMapping({SeedStoreManagerController.URL})
public class SeedStoreManagerController extends SeedInventoryTableDisplayingController {

	private static final Logger LOG = LoggerFactory.getLogger(SeedStoreManagerController.class);

	/** The Constant URL. */
	public static final String URL = "/SeedStoreManager";
	public static final String PAGINATION_TEMPLATE = "/Inventory/seedInventoryPagination";

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private InventoryService inventoryMiddlewareService;

	/** The user selection. */
	@Resource
	private SeedSelection seedSelection;

	/** The ontology service. */
	@Resource
	private OntologyService ontologyService;

	/**
	 * Shows the manage nurseries screen
	 *
	 * @param form the manage nurseries form
	 * @param model the model
	 * @param session the session
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("seedStoreForm") SeedStoreForm form, Model model, HttpSession session) {
		return super.show(model);
	}

	@RequestMapping(value = "/advance/displayGermplasmDetails/{listId}", method = RequestMethod.GET)
	public String displayAdvanceGermplasmDetails(@PathVariable Integer listId, @ModelAttribute("seedStoreForm") SeedStoreForm form,
			HttpServletRequest req, Model model) {

		return this.getInventoryGermplasmDetailsPage(form, listId, model, GermplasmListType.ADVANCED.name(),
				"/StudyManager/savedFinalList");
	}

	@RequestMapping(value = "/crosses/displayGermplasmDetails/{listId}", method = RequestMethod.GET)
	public String displayCrossesGermplasmDetails(@PathVariable Integer listId, @ModelAttribute("seedStoreForm") SeedStoreForm form,
			HttpServletRequest req, Model model) {
		return this.getInventoryGermplasmDetailsPage(form, listId, model, GermplasmListType.CROSSES.name(),
				"/StudyManager/savedFinalList");

	}

	protected String getInventoryGermplasmDetailsPage(SeedStoreForm form, Integer listId, Model model, String germplasmListType, String page) {
		try {
			List<InventoryDetails> inventoryDetailList =
					this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId, germplasmListType);
			this.getPaginationListSelection().addFinalAdvancedList(listId.toString(), inventoryDetailList);

			this.getSeedSelection().setInventoryList(inventoryDetailList);
			form.setListId(listId);
			form.setInventoryList(inventoryDetailList);
			form.setCurrentPage(1);
			form.setGidList(Integer.toString(listId));

			model.addAttribute(SeedInventoryTableDisplayingController.TABLE_HEADER_LIST, this.getSeedInventoryTableHeader());

		} catch (Exception e) {
			SeedStoreManagerController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "Inventory/main";
	}

	/**
	 * Gets the form.
	 *
	 * @return the form
	 */
	@ModelAttribute("form")
	public SeedStoreForm getForm() {
		return new SeedStoreForm();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getUserSelection()
	 */
	public SeedSelection getSeedSelection() {
		return this.seedSelection;
	}

	public void setInventoryMiddlewareService(InventoryService inventoryMiddlewareService) {
		this.inventoryMiddlewareService = inventoryMiddlewareService;
	}

	public void setSeedSelection(SeedSelection seedSelection) {
		this.seedSelection = seedSelection;
	}

	public void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	public void setOntologyService(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	public void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
