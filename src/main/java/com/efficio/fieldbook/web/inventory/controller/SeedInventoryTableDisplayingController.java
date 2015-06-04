
package com.efficio.fieldbook.web.inventory.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.TableHeader;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 3/24/2015 Time: 4:55 PM
 */
public abstract class SeedInventoryTableDisplayingController extends AbstractBaseFieldbookController {

	protected static final String TABLE_HEADER_LIST = "tableHeaderList";
	/**
	 * The ontology manager.
	 */
	@Resource
	protected OntologyDataManager ontologyDataManager;

	/**
	 * The message source.
	 */
	@Autowired
	public MessageSource messageSource;

	protected List<TableHeader> getSeedInventoryTableHeader() {
		Locale locale = LocaleContextHolder.getLocale();
		List<TableHeader> tableHeaderList = new ArrayList<TableHeader>();

		tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_ID.getTermNameFromOntology(this.ontologyDataManager), this.messageSource
				.getMessage("seed.entry.number", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager), this.messageSource
				.getMessage("seed.entry.designation", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.PARENTAGE.getTermNameFromOntology(this.ontologyDataManager), this.messageSource
				.getMessage("seed.entry.parentage", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.GID.getTermNameFromOntology(this.ontologyDataManager), this.messageSource
				.getMessage("seed.inventory.gid", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.SEED_SOURCE.getTermNameFromOntology(this.ontologyDataManager), this.messageSource
				.getMessage("seed.inventory.source", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.LOT_LOCATION.getTermNameFromOntology(this.ontologyDataManager), this.messageSource
				.getMessage("seed.inventory.table.location", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.AMOUNT.getTermNameFromOntology(this.ontologyDataManager), this.messageSource
				.getMessage("seed.inventory.amount", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.SCALE.getTermNameFromOntology(this.ontologyDataManager), this.messageSource
				.getMessage("seed.inventory.table.scale", null, locale)));
		tableHeaderList.add(new TableHeader(ColumnLabels.COMMENT.getTermNameFromOntology(this.ontologyDataManager), this.messageSource
				.getMessage("seed.inventory.comment", null, locale)));

		return tableHeaderList;
	}
}
