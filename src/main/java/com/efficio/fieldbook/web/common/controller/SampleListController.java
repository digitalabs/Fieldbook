package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.TableHeader;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.service.api.SampleListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Transactional
@Controller
@RequestMapping(SampleListController.URL)
public class SampleListController {

	private static final Logger LOG = LoggerFactory.getLogger(SampleListController.class);

	public static final String URL = "/sample/list";
	private static final String SAVED_FINAL_LIST = "/StudyManager/savedFinalList";
	private static final String TABLE_HEADER_LIST = "tableHeaderList";
	private static final String SAMPLE_LIST = "sampleList";
	private static final String SAMPLE_NAME = "sample.list.sample.name";
	private static final String TAKEN_BY = "sample.list.taken.by";
	private static final String SAMPLING_DATE = "sample.list.sampling.date";
	private static final String SAMPLE_UID = "sample.list.sample.uid";
	private static final String PLANT_UID = "sample.list.plant.uid";
	private static final String PLANT_NO = "sample.list.plant.no";
	private static final String PLOT_ID = "sample.list.plot.id";
	private static final String PLOT_NO = "sample.list.plot.no";
	private static final String DESIGNATION = "seed.entry.designation";
	private static final String TOTAL_NUMBER_OF_GERMPLASMS = "totalNumberOfGermplasms";
	private static final String SAMPLE_ENTRY = "sample.list.sample.entry.no";

	@Resource
	private MessageSource messageSource;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private SampleListService sampleListService;

	@RequestMapping(value = "/sampleList/{listId}", method = RequestMethod.GET)
	public String displaySampleList(@PathVariable Integer listId, HttpServletRequest req, Model model) {
		this.processSampleList(listId, req, model);
		return SampleListController.SAVED_FINAL_LIST;
	}

	private void processSampleList(Integer listId, HttpServletRequest req, Model model) {
		try {

			final SampleList sampleList = this.sampleListService.getSampleList(listId);
			final String name = sampleList.getListName();
			final String notes = sampleList.getNotes();
			final String type = sampleList.getType().name();
			final List<SampleDetailsDTO> sampleDetailsDTOs = this.sampleListService.getSampleDetailsDTOs(listId);
			model.addAttribute(SampleListController.SAMPLE_LIST, sampleDetailsDTOs);
			model.addAttribute(TOTAL_NUMBER_OF_GERMPLASMS, sampleDetailsDTOs.size());
			model.addAttribute(SampleListController.TABLE_HEADER_LIST, this.getSampleListTableHeaders());

			model.addAttribute("listId", listId);
			model.addAttribute("listName", name);
			model.addAttribute("listNotes", notes);
			model.addAttribute("listType", type);

		} catch (MiddlewareQueryException e) {
			SampleListController.LOG.error(e.getMessage(), e);
		}
	}

	private List<TableHeader> getSampleListTableHeaders() {
		Locale locale = LocaleContextHolder.getLocale();
		List<TableHeader> tableHeaderList = new ArrayList<>();


		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.SAMPLE_ENTRY, null, locale),
			this.messageSource.getMessage(SampleListController.SAMPLE_ENTRY, null, locale)));
		this.getCommonHeaders(locale, tableHeaderList);
		tableHeaderList.add(new TableHeader(ColumnLabels.PLOT_NO.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage(SampleListController.PLOT_NO, null, locale)));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.PLANT_NO, null, locale),
			this.messageSource.getMessage(SampleListController.PLANT_NO, null, locale)));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.SAMPLE_NAME, null, locale),
			this.messageSource.getMessage(SampleListController.SAMPLE_NAME, null, locale)));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.TAKEN_BY, null, locale),
			this.messageSource.getMessage(SampleListController.TAKEN_BY, null, locale)));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.SAMPLING_DATE, null, locale),
			this.messageSource.getMessage(SampleListController.SAMPLING_DATE, null, locale)));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.SAMPLE_UID, null, locale),
			this.messageSource.getMessage(SampleListController.SAMPLE_UID, null, locale), false));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.PLANT_UID, null, locale),
			this.messageSource.getMessage(SampleListController.PLANT_UID, null, locale), false));
		tableHeaderList.add(new TableHeader(this.messageSource.getMessage(SampleListController.PLOT_ID, null, locale),
			this.messageSource.getMessage(SampleListController.PLOT_ID, null, locale), false));

		return tableHeaderList;
	}

	private void getCommonHeaders(final Locale locale, final List<TableHeader> tableHeaderList) {
		tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager),
			this.messageSource.getMessage(SampleListController.DESIGNATION, null, locale)));
	}

}
