package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.TableHeader;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 4/22/2015
 * Time: 12:44 PM
 */

@Controller
@RequestMapping(GermplasmListController.URL)
public class GermplasmListController {
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
		return "/NurseryManager/savedFinalAdvanceList";
	}

	protected void processGermplasmList(Integer listId, String germplasmListType,
			HttpServletRequest req, Model model) {
		try {
			List<ListDataProject> listData = germplasmListManager.retrieveSnapshotListData(listId);
			model.addAttribute(TABLE_HEADER_LIST, getGermplasmListTableHeaders());
			model.addAttribute(GERMPLASM_LIST, listData);
			model.addAttribute("totalNumberOfGermplasms", listData.size());
			model.addAttribute("listId", listId);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	protected List<TableHeader> getGermplasmListTableHeaders() {
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
		tableHeaderList
				.add(new TableHeader(ColumnLabels.GID.getTermNameFromOntology(ontologyDataManager),
						messageSource.getMessage("seed.inventory.gid", null, locale)));
		tableHeaderList.add(new TableHeader(
						ColumnLabels.SEED_SOURCE.getTermNameFromOntology(ontologyDataManager),
						messageSource.getMessage("seed.inventory.source", null, locale)));
		return tableHeaderList;
	}
}
