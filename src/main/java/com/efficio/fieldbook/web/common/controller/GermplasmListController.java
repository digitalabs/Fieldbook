package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.common.bean.DuplicateType;
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

	protected void processGermplasmList(Integer listId, String germplasmListType,
			HttpServletRequest req, Model model) {
		try {
			GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);
			List<ListDataProject> listData = getListDataProjectByListType(listId,germplasmListType);
			
			model.addAttribute(TABLE_HEADER_LIST, getGermplasmListTableHeaders(germplasmListType));
			model.addAttribute(GERMPLASM_LIST, listData);
			model.addAttribute("totalNumberOfGermplasms", listData.size());
			model.addAttribute("listId", listId);
			model.addAttribute("listName", germplasmList.getName());
			model.addAttribute("listNotes", germplasmList.getNotes());
			model.addAttribute("listType", germplasmList.getType());
			model.addAttribute("duplicateType", getDuplicateType(listData));
			
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private Map<Integer,DuplicateType> getDuplicateType(List<ListDataProject> listData) {
		 Map<Integer,DuplicateType> duplicateTypeMap = new HashMap<Integer, DuplicateType>();
		 
		 for(ListDataProject ldp : listData){
			 Integer listDataProjectId = ldp.getListDataProjectId();
			 duplicateTypeMap.put(listDataProjectId, new DuplicateType(listDataProjectId,ldp.getDuplicate()));
		 }
		 
		return duplicateTypeMap;
	}

	private List<ListDataProject> getListDataProjectByListType(Integer listId,
			String germplasmListType) {
		List<ListDataProject> listData = new ArrayList<ListDataProject>();
		
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
		
		
		if(germplasmListType.equals(GermplasmListType.CROSSES.name()) ||
				germplasmListType.equals(GermplasmListType.STOCK.name())){
			tableHeaderList.add(new TableHeader(
					messageSource.getMessage("germplasm.list.duplicate", null, locale),
					messageSource.getMessage("germplasm.list.duplicate", null, locale)));
		}
		
		if(germplasmListType.equals(GermplasmListType.STOCK.name())){
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
		}
		
		return tableHeaderList;
	}
}
