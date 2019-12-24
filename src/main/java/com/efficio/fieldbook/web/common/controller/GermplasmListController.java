package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.TableHeader;
import org.generationcp.middleware.constant.ColumnLabels;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 4/22/2015 Time: 12:44 PM
 */
@Transactional
@Controller
@RequestMapping(GermplasmListController.URL)
public class GermplasmListController {

	private static final String GERMPLASM_LIST_DUPLICATE = "germplasm.list.duplicate";

	private static final String STUDY_MANAGER_SAVED_FINAL_LIST = "/StudyManager/savedFinalList";

	public static final String URL = "/germplasm/list";

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListController.class);
	public static final String TABLE_HEADER_LIST = "tableHeaderList";
	public static final String GERMPLASM_LIST = "germplasmList";
	public static final String INVENTORY_VIEW_TOGGLED = "inventoryViewToggled";
	public static final String TOTAL_NUMBER_OF_GERMPLASMS = "totalNumberOfGermplasms";

	@Resource
	private GermplasmListManager germplasmListManager;

	@Resource
	private MessageSource messageSource;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private InventoryService inventoryService;

	@Resource
	private PlatformTransactionManager transactionManager;

	/** The fieldbook middleware service. */
	@Resource
	protected org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@RequestMapping(value = "/advance/{listId}", method = RequestMethod.GET)
	public String displayAdvanceGermplasmList(@PathVariable Integer listId, Model model) {
		this.processGermplasmList(listId, GermplasmListType.ADVANCED.name(), model);
		return GermplasmListController.STUDY_MANAGER_SAVED_FINAL_LIST;
	}

	@RequestMapping(value = "/crosses/{listId}", method = RequestMethod.GET)
	public String displayCrossGermplasmList(@PathVariable Integer listId, Model model) {
		this.processGermplasmList(listId, GermplasmListType.CROSSES.name(), model);
		return GermplasmListController.STUDY_MANAGER_SAVED_FINAL_LIST;
	}

	@RequestMapping(value = "/stock/{listId}", method = RequestMethod.GET)
	public String displayStockList(@PathVariable final Integer listId, HttpServletRequest req, final Model model) {
		try {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					GermplasmList germplasmList = GermplasmListController.this.germplasmListManager.getGermplasmListById(listId);
					List<InventoryDetails> detailList =
						GermplasmListController.this.inventoryService.getInventoryListByListDataProjectListId(listId);
					model.addAttribute(GermplasmListController.INVENTORY_VIEW_TOGGLED, false);
					GermplasmListController.this.prepareStockList(model, listId, detailList, germplasmList);
				}

			});
		} catch (MiddlewareQueryException e) {
			GermplasmListController.LOG.error(e.getMessage(), e);
		}

		return GermplasmListController.STUDY_MANAGER_SAVED_FINAL_LIST;
	}

	@RequestMapping(value = "/stockinventory/{listId}", method = RequestMethod.GET)
	public String displayStockInventoryList(@PathVariable Integer listId, HttpServletRequest req, Model model) {
		try {
			GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(listId);
			List<InventoryDetails> detailList = this.inventoryService
				.getSummedInventoryListByListDataProjectListId(listId, GermplasmListType.valueOf(germplasmList.getType()));

			model.addAttribute(GermplasmListController.INVENTORY_VIEW_TOGGLED, true);

			this.prepareStockList(model, listId, detailList, germplasmList);
		} catch (MiddlewareQueryException e) {
			GermplasmListController.LOG.error(e.getMessage(), e);
		}

		return GermplasmListController.STUDY_MANAGER_SAVED_FINAL_LIST;
	}

	protected void prepareStockList(Model model, Integer listId, List<InventoryDetails> detailList, GermplasmList germplasmList) {

		model.addAttribute(TOTAL_NUMBER_OF_GERMPLASMS, detailList.size());
		model.addAttribute("listId", listId);
		model.addAttribute("listNotes", germplasmList.getNotes());
		model.addAttribute("listType", GermplasmListType.STOCK.name());
		model.addAttribute("sourceListType", germplasmList.getType());
		model.addAttribute("listName", germplasmList.getName());
		model.addAttribute(GermplasmListController.GERMPLASM_LIST, detailList);
		List<TableHeader> tableHeaderList = null;

		boolean hasCompletedBulking = false;
		if (germplasmList.getType().equals(GermplasmListType.ADVANCED.name())) {
			tableHeaderList = this.getAdvancedStockListTableHeaders(GermplasmListType.STOCK.name());
		} else if (GermplasmListType.isCrosses(germplasmList.getType())) {
			tableHeaderList = this.getCrossStockListTableHeaders();
			hasCompletedBulking = this.stockHasCompletedBulking(listId);
		}
		model.addAttribute(GermplasmListController.TABLE_HEADER_LIST, tableHeaderList);
		model.addAttribute("hasCompletedBulking", hasCompletedBulking);
	}

	private boolean stockHasCompletedBulking(Integer listId) {
		return this.inventoryService.stockHasCompletedBulking(listId);
	}

	protected void processGermplasmList(Integer listId, String germplasmListType, Model model) {
		try {
			List<ListDataProject> listData = null;
			String name;
			String notes;
			String type;

			GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(listId);
			listData = this.getListDataProjectByListType(listId, germplasmListType);
			notes = germplasmList.getNotes();
			name = germplasmList.getName();
			type = germplasmList.getType();
			model.addAttribute(GermplasmListController.GERMPLASM_LIST, listData);
			model.addAttribute(TOTAL_NUMBER_OF_GERMPLASMS, listData.size());
			model.addAttribute(GermplasmListController.TABLE_HEADER_LIST, this.getGermplasmListTableHeaders(germplasmListType));

			model.addAttribute("listId", listId);
			model.addAttribute("listName", name);
			model.addAttribute("listNotes", notes);
			model.addAttribute("listType", type);

			if (GermplasmListType.isCrosses(germplasmListType)) {
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

				if (GermplasmListType.IMP_CROSS.name().equals(type)) {
					model.addAttribute("listTypeLabel", GermplasmList.IMP_CROSS);
				} else if (GermplasmListType.CRT_CROSS.name().equals(type)) {
					model.addAttribute("listTypeLabel", GermplasmList.CRT_CROSS);
				}
			}
		} catch (MiddlewareQueryException e) {
			GermplasmListController.LOG.error(e.getMessage(), e);
		}
	}

	protected List<ListDataProject> getListDataProjectByListType(Integer listId, String germplasmListType) {
		List<ListDataProject> listData = new ArrayList<>();

		try {
			if (germplasmListType.equals(GermplasmListType.ADVANCED.name())) {
				listData = this.germplasmListManager.retrieveSnapshotListData(listId);
			} else if (GermplasmListType.isCrosses(germplasmListType)) {
				listData = this.germplasmListManager.retrieveSnapshotListDataWithParents(listId);
			}

		} catch (MiddlewareQueryException e) {
			GermplasmListController.LOG.error(e.getMessage(), e);
		}

		return listData;
	}

	protected List<TableHeader> getCrossStockListTableHeaders() {
		Locale locale = LocaleContextHolder.getLocale();
		List<TableHeader> tableHeaderList = new ArrayList<>();

		this.getCommonHeaders(locale, tableHeaderList);
		tableHeaderList.add(new TableHeader(ColumnLabels.PARENTAGE.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("seed.entry.parentage", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.GID.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("seed.inventory.gid", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.STOCKID_INVENTORY.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.stock.list.stockid", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.SEED_SOURCE.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("seed.inventory.source", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.GROUP_ID.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.group.id", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.DUPLICATE.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage(GermplasmListController.GERMPLASM_LIST_DUPLICATE, null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.BULK_WITH.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.bulk.with", null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.BULK_COMPL.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.bulk.complete", null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.LOT_LOCATION.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.storage.location", null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.AMOUNT.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.amount", null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.UNITS.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.scale", null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.COMMENT.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.comment", null, locale)));

		return tableHeaderList;
	}

	protected List<TableHeader> getAdvancedStockListTableHeaders(String germplasmListType) {

		List<TableHeader> tableHeaderList = this.getGermplasmListTableHeaders(germplasmListType);
		Locale locale = LocaleContextHolder.getLocale();

		tableHeaderList.add(new TableHeader(ColumnLabels.LOT_LOCATION.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.storage.location", null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.AMOUNT.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.amount", null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.UNITS.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.scale", null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.STOCKID_INVENTORY.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.stock.list.stockid", null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.COMMENT.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.comment", null, locale)));

		return tableHeaderList;
	}

	protected List<TableHeader> getGermplasmListTableHeaders(String germplasmListType) {
		Locale locale = LocaleContextHolder.getLocale();
		List<TableHeader> tableHeaderList = new ArrayList<>();

		this.getCommonHeaders(locale, tableHeaderList);
		tableHeaderList.add(new TableHeader(ColumnLabels.PARENTAGE.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("seed.entry.parentage", null, locale)));

		if (GermplasmListType.isCrosses(germplasmListType)) {
			tableHeaderList.add(new TableHeader(ColumnLabels.FEMALE_PARENT.getTermNameFromOntology(this.ontologyDataManager),
				this.messageSource.getMessage("germplasm.list.female.parent", null, locale)));

			tableHeaderList.add(new TableHeader(ColumnLabels.FGID.getTermNameFromOntology(this.ontologyDataManager),
				this.messageSource.getMessage("germplasm.list.fgid", null, locale)));

			tableHeaderList.add(new TableHeader(ColumnLabels.MALE_PARENT.getTermNameFromOntology(this.ontologyDataManager),
				this.messageSource.getMessage("germplasm.list.male.parent", null, locale)));

			tableHeaderList.add(new TableHeader(ColumnLabels.MGID.getTermNameFromOntology(this.ontologyDataManager),
				this.messageSource.getMessage("germplasm.list.mgid", null, locale)));
		}

		tableHeaderList.add(new TableHeader(ColumnLabels.GID.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("seed.inventory.gid", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.SEED_SOURCE.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("seed.inventory.source", null, locale)));

		tableHeaderList.add(new TableHeader(ColumnLabels.GROUP_ID.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("germplasm.list.group.id", null, locale)));

		if (GermplasmListType.isCrosses(germplasmListType)) {
			tableHeaderList.add(new TableHeader(ColumnLabels.DUPLICATE.getTermNameFromOntology(this.ontologyDataManager),
					this.messageSource.getMessage(GermplasmListController.GERMPLASM_LIST_DUPLICATE, null, locale)));
		}

		return tableHeaderList;
	}

	private void getCommonHeaders(final Locale locale, final List<TableHeader> tableHeaderList) {
		tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_ID.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("seed.entry.number", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage("seed.entry.designation", null, locale)));
	}
}
