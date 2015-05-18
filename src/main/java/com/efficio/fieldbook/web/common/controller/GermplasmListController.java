package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.service.api.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.common.bean.TableHeader;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 4/22/2015
 * Time: 12:44 PM
 */

@Controller
@RequestMapping(GermplasmListController.URL)
public class GermplasmListController {
	private static final String GERMPLASM_LIST_DUPLICATE = "germplasm.list.duplicate";

	private static final String NURSERY_MANAGER_SAVED_FINAL_LIST = "/NurseryManager/savedFinalList";

	public static final String URL = "/germplasm/list";

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListController.class);
	public static final String TABLE_HEADER_LIST = "tableHeaderList";
	public static final String GERMPLASM_LIST = "germplasmList";

	@Resource
	private GermplasmListManager germplasmListManager;

	@Resource
	private MessageSource messageSource;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private InventoryService inventoryService;

	@RequestMapping(value = "/advance/{listId}", method = RequestMethod.GET)
	public String displayAdvanceGermplasmList(@PathVariable Integer listId, HttpServletRequest req,
			Model model) {
		processGermplasmList(listId, GermplasmListType.ADVANCED.name(), req, model);
		return NURSERY_MANAGER_SAVED_FINAL_LIST;
	}
	
	@RequestMapping(value = "/crosses/{listId}", method = RequestMethod.GET)
	public String displayCrossGermplasmList(@PathVariable Integer listId, HttpServletRequest req,
			Model model) {
		processGermplasmList(listId, GermplasmListType.CROSSES.name(), req, model);
		return NURSERY_MANAGER_SAVED_FINAL_LIST;
	}

	@RequestMapping(value = "/stock/{listId}", method = RequestMethod.GET)
	public String displayStockList(@PathVariable Integer listId, HttpServletRequest req, Model model) {
		try {
			GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
			List<InventoryDetails> detailList = inventoryService.getInventoryListByListDataProjectListId(listId,
					GermplasmListType.valueOf(germplasmList.getType()));

			model.addAttribute("totalNumberOfGermplasms", detailList.size());
			model.addAttribute("listId", listId);
			model.addAttribute("listNotes", germplasmList.getNotes());
			model.addAttribute("listType", GermplasmListType.STOCK.name());
			model.addAttribute("subListType", germplasmList.getType());
			model.addAttribute("listName", germplasmList.getName());
			model.addAttribute(GERMPLASM_LIST, detailList);
			List<TableHeader> tableHeaderList = null;

			boolean hasCompletedBulking = false;
			if (germplasmList.getType().equals(GermplasmListType.ADVANCED.name())) {
				tableHeaderList = getAdvancedStockListTableHeaders();
			} else if (germplasmList.getType().equals(GermplasmListType.CROSSES.name())) {
				tableHeaderList = getCrossStockListTableHeaders();
				hasCompletedBulking = stockHasCompletedBulking(listId);
			}
			model.addAttribute(TABLE_HEADER_LIST, tableHeaderList);
			model.addAttribute("hasCompletedBulking", hasCompletedBulking);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}

		return NURSERY_MANAGER_SAVED_FINAL_LIST;
	}

	private boolean stockHasCompletedBulking(Integer listId) throws MiddlewareQueryException {
		return inventoryService.stockHasCompletedBulking(listId);
	}

	protected void processGermplasmList(Integer listId, String germplasmListType,
			HttpServletRequest req, Model model) {
		try {
			GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
			List<ListDataProject> listData = getListDataProjectByListType(listId, germplasmListType);
			
			model.addAttribute(TABLE_HEADER_LIST, getGermplasmListTableHeaders(germplasmListType));
			model.addAttribute(GERMPLASM_LIST, listData);
			model.addAttribute("totalNumberOfGermplasms", listData.size());
			model.addAttribute("listId", listId);
			model.addAttribute("listName", germplasmList.getName());
			model.addAttribute("listNotes", germplasmList.getNotes());
			model.addAttribute("listType", germplasmList.getType());

			if (germplasmListType.equals(GermplasmListType.CROSSES.name())) {
				boolean pedigreeDupeFound = false;
				boolean pedigreeRecipFound = false;
				boolean plotDupeFound = false;
				boolean plotRecipFound = false;

				for (ListDataProject dataProject : listData) {
					pedigreeDupeFound |= dataProject.isPedigreeDupe();
					pedigreeRecipFound |= dataProject.isPedigreeRecip();
					plotDupeFound |= dataProject.isPlotDupe();
					plotRecipFound |= dataProject.isPlotRecip();
				}

				model.addAttribute("hasPedigreeDupe", pedigreeDupeFound);
				model.addAttribute("hasPedigreeRecip", pedigreeRecipFound);
				model.addAttribute("hasPlotDupe", plotDupeFound);
				model.addAttribute("hasPlotRecip", plotRecipFound);
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	protected List<ListDataProject> getListDataProjectByListType(Integer listId,
			String germplasmListType) {
		List<ListDataProject> listData = new ArrayList<>();
		
		try {	
			if(germplasmListType.equals(GermplasmListType.ADVANCED.name())){
				listData = germplasmListManager.retrieveSnapshotListData(listId);
			} else if(germplasmListType.equals(GermplasmListType.CROSSES.name())){
				listData = germplasmListManager.retrieveSnapshotListDataWithParents(listId);
			}
		
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
		}
		
		return listData;
	}

	protected List<TableHeader> getCrossStockListTableHeaders() {
		Locale locale = LocaleContextHolder.getLocale();
		List<TableHeader> tableHeaderList = new ArrayList<>();

		tableHeaderList.add(new TableHeader(
				ColumnLabels.ENTRY_ID.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("seed.entry.number", null, locale)));
		tableHeaderList.add(new TableHeader(
				ColumnLabels.DESIGNATION.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("seed.entry.designation", null, locale)));
		tableHeaderList.add(new TableHeader(
				ColumnLabels.PARENTAGE.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("seed.entry.parentage", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.GID.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("seed.inventory.gid", null, locale)));
		tableHeaderList.add(new TableHeader(
				ColumnLabels.STOCKID.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("germplasm.stock.list.stockid", null, locale)));
		tableHeaderList.add(new TableHeader(
				ColumnLabels.SEED_SOURCE.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("seed.inventory.source", null, locale)));
		tableHeaderList.add(new TableHeader(
				messageSource.getMessage(GERMPLASM_LIST_DUPLICATE, null, locale),
				messageSource.getMessage(GERMPLASM_LIST_DUPLICATE, null, locale)));

		tableHeaderList.add(new TableHeader(
				messageSource.getMessage("germplasm.list.bulk.with", null, locale),
				messageSource.getMessage("germplasm.list.bulk.with", null, locale)));

		tableHeaderList.add(new TableHeader(
				messageSource.getMessage("germplasm.list.bulk.complete", null, locale),
				messageSource.getMessage("germplasm.list.bulk.complete", null, locale)));

		tableHeaderList.add(new TableHeader(
				ColumnLabels.LOT_LOCATION.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("germplasm.list.storage.location", null, locale)));

		tableHeaderList.add(new TableHeader(
				ColumnLabels.AMOUNT.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("germplasm.list.amount", null, locale)));

		tableHeaderList.add(new TableHeader(
				ColumnLabels.SCALE.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("germplasm.list.scale", null, locale)));

		tableHeaderList.add(new TableHeader(
				ColumnLabels.COMMENT.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("germplasm.list.comment", null, locale)));

		return tableHeaderList;
	}

	protected List<TableHeader> getAdvancedStockListTableHeaders() {

		List<TableHeader> tableHeaderList = getGermplasmListTableHeaders(GermplasmListType.ADVANCED.name());
		Locale locale = LocaleContextHolder.getLocale();

		tableHeaderList.add(new TableHeader(
				ColumnLabels.LOT_LOCATION.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("germplasm.list.storage.location", null, locale)));

		tableHeaderList.add(new TableHeader(
				ColumnLabels.AMOUNT.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("germplasm.list.amount", null, locale)));

		tableHeaderList.add(new TableHeader(
				ColumnLabels.SCALE.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("germplasm.list.scale", null, locale)));

		tableHeaderList.add(new TableHeader(
				ColumnLabels.STOCKID.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("germplasm.stock.list.stockid", null, locale)));

		tableHeaderList.add(new TableHeader(
				ColumnLabels.COMMENT.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("germplasm.list.comment", null, locale)));

		return tableHeaderList;
	}

	protected List<TableHeader> getGermplasmListTableHeaders(String germplasmListType) {
		Locale locale = LocaleContextHolder.getLocale();
		List<TableHeader> tableHeaderList = new ArrayList<>();

		tableHeaderList.add(new TableHeader(
				ColumnLabels.ENTRY_ID.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("seed.entry.number", null, locale)));
		tableHeaderList.add(new TableHeader(
				ColumnLabels.DESIGNATION.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("seed.entry.designation", null, locale)));
		tableHeaderList.add(new TableHeader(
				ColumnLabels.PARENTAGE.getTermNameFromOntology(ontologyDataManager),
				messageSource.getMessage("seed.entry.parentage", null, locale)));
		
		if(germplasmListType.equals(GermplasmListType.CROSSES.name())){
			tableHeaderList.add(new TableHeader(
					ColumnLabels.FEMALE_PARENT.getTermNameFromOntology(ontologyDataManager),
					messageSource.getMessage("germplasm.list.female.parent", null, locale)));
			
			tableHeaderList.add(new TableHeader(
					ColumnLabels.FGID.getTermNameFromOntology(ontologyDataManager),
					messageSource.getMessage("germplasm.list.fgid", null, locale)));
			
			tableHeaderList.add(new TableHeader(
					ColumnLabels.MALE_PARENT.getTermNameFromOntology(ontologyDataManager),
					messageSource.getMessage("germplasm.list.male.parent", null, locale)));
			
			tableHeaderList.add(new TableHeader(
					ColumnLabels.MGID.getTermNameFromOntology(ontologyDataManager),
					messageSource.getMessage("germplasm.list.mgid", null, locale)));
		}
		
		tableHeaderList.add(new TableHeader(ColumnLabels.GID.getTermNameFromOntology(ontologyDataManager),
						messageSource.getMessage("seed.inventory.gid", null, locale)));
		tableHeaderList.add(new TableHeader(
						ColumnLabels.SEED_SOURCE.getTermNameFromOntology(ontologyDataManager),
						messageSource.getMessage("seed.inventory.source", null, locale)));
		
		
		if(germplasmListType.equals(GermplasmListType.CROSSES.name())){
			tableHeaderList.add(new TableHeader(
					messageSource.getMessage(GERMPLASM_LIST_DUPLICATE, null, locale),
					messageSource.getMessage(GERMPLASM_LIST_DUPLICATE, null, locale)));
		}

		return tableHeaderList;
	}
}