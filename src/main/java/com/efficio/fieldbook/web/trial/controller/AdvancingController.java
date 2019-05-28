/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import org.generationcp.commons.pojo.AdvanceGermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.common.bean.ChoiceKeyVal;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.AdvanceType;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import com.efficio.fieldbook.web.trial.form.AdvancingStudyForm;
import org.generationcp.commons.constant.AppConstants;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping(AdvancingController.URL)
public class AdvancingController extends AbstractBaseFieldbookController {

	private static final String UNIQUE_ID = "uniqueId";

	/** The Constant URL. */
	protected static final String URL = "/StudyManager/advance/study";

	private static final String MODAL_URL = "StudyManager/advanceStudyModal";

	private static final String SAVE_ADVANCE_STUDY_PAGE_TEMPLATE = "StudyManager/saveAdvanceStudy";

	private static final String TABLE_HEADER_LIST = "tableHeaderList";

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(AdvancingController.class);

	private static final String IS_SUCCESS = "isSuccess";

	private static final String LIST_SIZE = "listSize";

	private static final String MESSAGE = "message";

	/** The fieldbook middleware service. */
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private UserSelection userSelection;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private MessageSource messageSource;

	@Resource
	private OntologyDataManager ontologyDataManager;


	@Override
	public String getContentName() {
		return "StudyManager/advanceStudyModal";
	}

	/**
	 * Shows the screen.
	 *
	 * @param form the form
	 * @param model the model
	 * @param session the session
	 * @param studyId the study id
     * @param selectedInstances Set of Trial Instances(Optional)
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@RequestMapping(value = "/{studyId}", method = RequestMethod.GET)
	public String show(@ModelAttribute("advancingStudyForm") AdvancingStudyForm form, Model model, HttpServletRequest req,
		HttpSession session, @PathVariable int studyId, @RequestParam(required = false) Set<String> selectedInstances,
		@RequestParam(required = false) String noOfReplications, @RequestParam(required = false) String advanceType)
		throws MiddlewareException {

    	form.setMethodChoice("1");
		form.setLineChoice("1");
		form.setLineSelected("1");
		form.setAllPlotsChoice("1");
        form.setDefaultMethodId(Integer.toString(AppConstants.SINGLE_PLANT_SELECTION_SF.getInt()));
        form.setBreedingMethodUrl(this.fieldbookProperties.getProgramBreedingMethodsUrl());
        form.setSelectedReplications(Sets.newHashSet("1"));
        form.setStudyId(Integer.toString(studyId));

		form.setMethodVariates(this.filterVariablesByProperty(this.userSelection.getSelectionVariates(),
				AppConstants.PROPERTY_BREEDING_METHOD.getString()));
		form.setLineVariates(this.filterVariablesByProperty(this.userSelection.getSelectionVariates(),
				AppConstants.PROPERTY_PLANTS_SELECTED.getString()));
		form.setPlotVariates(form.getLineVariates());

		Date currentDate = DateUtil.getCurrentDate();
		SimpleDateFormat sdf = DateUtil.getSimpleDateFormat("yyyy");
		SimpleDateFormat sdfMonth = DateUtil.getSimpleDateFormat("MM");
		String currentYear = sdf.format(currentDate);
		form.setHarvestYear(currentYear);
		form.setHarvestMonth(sdfMonth.format(currentDate));
		
        form.setSelectedTrialInstances(selectedInstances);

		model.addAttribute("yearChoices", this.generateYearChoices(Integer.parseInt(currentYear)));
		model.addAttribute("monthChoices", this.generateMonthChoices());
        model.addAttribute("replicationsChoices",this.generateReplicationChoice(noOfReplications));
		model.addAttribute("advanceType", advanceType);

		return super.showAjaxPage(model, AdvancingController.MODAL_URL);
	}

    private List<String> generateReplicationChoice(String noOfReplications){
        List<String> replicationChoices = new ArrayList<String>();
        if(noOfReplications != null){
            int replicationCount = Integer.valueOf(noOfReplications);
            for(int i=1; i<=replicationCount; i++){
                replicationChoices.add(i+"");
            }
        }

        return replicationChoices;
    }
	public List<ChoiceKeyVal> generateYearChoices(int currentYear) {
		List<ChoiceKeyVal> yearList = new ArrayList<>();
		int startYear = currentYear - AppConstants.ADVANCING_YEAR_RANGE.getInt();
		currentYear = currentYear + AppConstants.ADVANCING_YEAR_RANGE.getInt();
		for (int i = startYear; i <= currentYear; i++) {
			yearList.add(new ChoiceKeyVal(Integer.toString(i), Integer.toString(i)));
		}
		return yearList;
	}

	public List<ChoiceKeyVal> generateMonthChoices() {
		List<ChoiceKeyVal> monthList = new ArrayList<>();
		DecimalFormat df2 = new DecimalFormat("00");
		for (double i = 1; i <= 12; i++) {
			monthList.add(new ChoiceKeyVal(df2.format(i), df2.format(i)));
		}
		return monthList;
	}

	@ModelAttribute("programLocationURL")
	public String getProgramLocation() {
		return this.fieldbookProperties.getProgramLocationsUrl();
	}

	@ModelAttribute("projectID")
	public String getProgramID() {
		return this.getCurrentProjectId();
	}

	/**
	 * Post advance Study.
	 *
	 * @param form the form
	 * @param result the result
	 * @param model the model
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 * @throws FieldbookException 
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, Object> postAdvanceStudy(@ModelAttribute("advancingStudyForm") AdvancingStudyForm form, BindingResult result,
			Model model) {
		
		Map<String, Object> results = new HashMap<>();
		AdvancingStudy advancingStudy = new AdvancingStudy();
		
		Study study = this.fieldbookMiddlewareService.getStudy(Integer.valueOf(form.getStudyId()));
		advancingStudy.setStudy(study);
		advancingStudy.setMethodChoice(form.getMethodChoice());
		advancingStudy.setBreedingMethodId(form.getAdvanceBreedingMethodId());
		advancingStudy.setLineChoice(form.getLineChoice());
		advancingStudy.setLineSelected(form.getLineSelected() != null ? form.getLineSelected().trim() : null);
		advancingStudy.setHarvestDate(form.getHarvestDate());
		advancingStudy.setHarvestLocationId(form.getHarvestLocationId());
		advancingStudy
			.setHarvestLocationAbbreviation(form.getHarvestLocationAbbreviation() != null ? form.getHarvestLocationAbbreviation() : "");
		advancingStudy.setAllPlotsChoice(form.getAllPlotsChoice());
		advancingStudy.setLineVariateId(form.getLineVariateId());
		advancingStudy.setPlotVariateId(form.getPlotVariateId());
		advancingStudy.setMethodVariateId(form.getMethodVariateId());
		// Set to false till we figure out a more performat way to do unique name checking
		advancingStudy.setCheckAdvanceLinesUnique(false);
        advancingStudy.setSelectedReplications(form.getSelectedReplications());
        advancingStudy.setSelectedTrialInstances(form.getSelectedTrialInstances());
        advancingStudy.setAdvanceType(AdvanceType.fromLowerCaseName(form.getAdvanceType()));
		boolean observationsLoaded = this.fieldbookMiddlewareService.loadAllObservations(this.userSelection.getWorkbook());

		try {

			if (advancingStudy.getMethodChoice() != null && !advancingStudy.getMethodChoice().isEmpty()) {
				Method method = this.fieldbookMiddlewareService.getMethodById(Integer.valueOf(advancingStudy.getBreedingMethodId()));
				if ("GEN".equals(method.getMtype())) {
					form.setErrorInAdvance(this.messageSource.getMessage("study.save.advance.error.generative.method",
							new String[] {}, LocaleContextHolder.getLocale()));
					form.setGermplasmList(new ArrayList<ImportedGermplasm>());
					form.setEntries(0);
					results.put(AdvancingController.IS_SUCCESS, "0");
					results.put(AdvancingController.LIST_SIZE, 0);
					results.put(AdvancingController.MESSAGE, form.getErrorInAdvance());

					return results;
				}
			}


			AdvanceResult advanceResult = this.fieldbookService.advanceStudy(advancingStudy, this.userSelection.getWorkbook());
			List<ImportedGermplasm> importedGermplasmList = advanceResult.getAdvanceList();
			long id = DateUtil.getCurrentDate().getTime();
			this.getPaginationListSelection().addAdvanceDetails(Long.toString(id), form);
			this.userSelection.setImportedAdvancedGermplasmList(importedGermplasmList);
			form.setGermplasmList(importedGermplasmList);
			form.setEntries(importedGermplasmList.size());
			form.changePage(1);
			form.setUniqueId(id);

			List<AdvanceGermplasmChangeDetail> advanceGermplasmChangeDetails = advanceResult.getChangeDetails();

			results.put(AdvancingController.IS_SUCCESS, "1");
			results.put(AdvancingController.LIST_SIZE, importedGermplasmList.size());
			results.put("advanceGermplasmChangeDetails", advanceGermplasmChangeDetails);
			results.put(AdvancingController.UNIQUE_ID, id);

		} catch (MiddlewareException | RuleException | FieldbookException e) {
			AdvancingController.LOG.error(e.getMessage(), e);
			form.setErrorInAdvance(this.messageSource.getMessage(e.getMessage(),
					new String[] {}, LocaleContextHolder.getLocale()));
			form.setGermplasmList(new ArrayList<ImportedGermplasm>());
			form.setEntries(0);
			results.put(AdvancingController.IS_SUCCESS, "0");
			results.put(AdvancingController.LIST_SIZE, 0);
			results.put(AdvancingController.MESSAGE, form.getErrorInAdvance());
		} finally {
			// Important to clear out the observations collection from user session, once we are done with it to keep heap memory under
			// control. For large trials/nurseries the observations collection can be huge.
			if (observationsLoaded) {
				userSelection.getWorkbook().getObservations().clear();
			}
		}
		return results;
	}

	@ResponseBody
	@RequestMapping(value = "/apply/change/details", method = RequestMethod.POST)
	public Map<String, Object> applyChangeDetails(@RequestParam(value = "data") String userResponses) throws IOException {
		Map<String, Object> results = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();
		AdvanceGermplasmChangeDetail[] responseDetails = objectMapper.readValue(userResponses, AdvanceGermplasmChangeDetail[].class);
		List<ImportedGermplasm> importedGermplasmListTemp = this.userSelection.getImportedAdvancedGermplasmList();
		List<Integer> deletedEntryIds = new ArrayList<>();
		for (AdvanceGermplasmChangeDetail responseDetail : responseDetails) {
			if (responseDetail.getIndex() < importedGermplasmListTemp.size()) {
				ImportedGermplasm importedGermplasm = importedGermplasmListTemp.get(responseDetail.getIndex());
				if (responseDetail.getStatus() == 1) {
					// add germplasm name to gid
					// we need to delete
					deletedEntryIds.add(importedGermplasm.getEntryId());
				} else if (responseDetail.getStatus() == 3) {
					// choose gids
					importedGermplasm.setDesig(responseDetail.getNewAdvanceName());
					List<Name> names = importedGermplasm.getNames();
					if (names != null) {
						// set the first value, for now, we're expecting only 1 records.
						// this was a list because in the past, we can have more than 1 names, but this was changed
						names.get(0).setNval(responseDetail.getNewAdvanceName());
					}
				}
			}
		}
		// now we need to delete all marked deleted
		int index = 1;
		for (Iterator<ImportedGermplasm> iterator = importedGermplasmListTemp.iterator(); iterator.hasNext();) {
			ImportedGermplasm germplasm = iterator.next();
			if (deletedEntryIds.contains(germplasm.getEntryId())) {
				iterator.remove();
			} else {
				germplasm.setEntryId(index++);
			}
		}
		this.userSelection.setImportedAdvancedGermplasmList(importedGermplasmListTemp);
		results.put(AdvancingController.IS_SUCCESS, "1");
		results.put(AdvancingController.LIST_SIZE, importedGermplasmListTemp.size());
		return results;
	}

	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showAdvanceStudy(@ModelAttribute("advancingStudyForm") AdvancingStudyForm form, BindingResult result, Model model,
			HttpServletRequest req) throws MiddlewareQueryException {

		try {
			/* The imported germplasm list. */
			List<ImportedGermplasm> importedGermplasmList = this.userSelection.getImportedAdvancedGermplasmList();
			form.setGermplasmList(importedGermplasmList);
			form.setEntries(importedGermplasmList.size());
			form.changePage(1);
			String uniqueId = req.getParameter(AdvancingController.UNIQUE_ID);
			form.setUniqueId(Long.valueOf(uniqueId));

			List<Map<String, Object>> dataTableDataList = this.setupAdvanceReviewDataList(importedGermplasmList);

			model.addAttribute("advanceDataList", dataTableDataList);
			model.addAttribute(AdvancingController.TABLE_HEADER_LIST, this.getAdvancedStudyTableHeader());
		} catch (Exception e) {
			AdvancingController.LOG.error(e.getMessage(), e);
			form.setErrorInAdvance(e.getMessage());
			form.setGermplasmList(new ArrayList<ImportedGermplasm>());
			form.setEntries(0);
		}

		return super.showAjaxPage(model, AdvancingController.SAVE_ADVANCE_STUDY_PAGE_TEMPLATE);
	}

	@RequestMapping(value = "/delete/entries", method = RequestMethod.POST)
	public String deleteAdvanceStudyEntries(@ModelAttribute("advancingStudyForm") AdvancingStudyForm form, BindingResult result,
			Model model, HttpServletRequest req) throws MiddlewareQueryException {

		try {
			/* The imported germplasm list. */
			List<ImportedGermplasm> importedGermplasmList = this.userSelection.getImportedAdvancedGermplasmList();

			String entryNumbers = req.getParameter("entryNums");
			String[] entries = entryNumbers.split(",");
			importedGermplasmList = this.deleteImportedGermplasmEntries(importedGermplasmList, entries);
			this.userSelection.setImportedAdvancedGermplasmList(importedGermplasmList);

			form.setGermplasmList(importedGermplasmList);
			form.setEntries(importedGermplasmList.size());
			form.changePage(1);
			String uniqueId = req.getParameter(AdvancingController.UNIQUE_ID);
			form.setUniqueId(Long.valueOf(uniqueId));

			List<Map<String, Object>> dataTableDataList = this.setupAdvanceReviewDataList(importedGermplasmList);
			// remove the entry numbers

			model.addAttribute("advanceDataList", dataTableDataList);
			model.addAttribute(AdvancingController.TABLE_HEADER_LIST, this.getAdvancedStudyTableHeader());
		} catch (Exception e) {
			AdvancingController.LOG.error(e.getMessage(), e);
			form.setErrorInAdvance(e.getMessage());
			form.setGermplasmList(new ArrayList<ImportedGermplasm>());
			form.setEntries(0);
		}

		return super.showAjaxPage(model, AdvancingController.SAVE_ADVANCE_STUDY_PAGE_TEMPLATE);
	}

	protected List<ImportedGermplasm> deleteImportedGermplasmEntries(List<ImportedGermplasm> importedGermplasmList, String[] entries) {
		for (int j = 0; j < entries.length; j++) {
			// we remove the matching entries from the germplasm list
			String entryNumber = entries[j];
			boolean isFound = false;
			int i = 0;
			for (i = 0; i < importedGermplasmList.size(); i++) {
				ImportedGermplasm germplasm = importedGermplasmList.get(i);
				if (germplasm.getEntryId().toString().equalsIgnoreCase(entryNumber)) {
					isFound = true;
					break;
				}
			}
			if (isFound) {
				importedGermplasmList.remove(i);
			}
		}
		// now we need to set the entry id again
		for (int i = 0; i < importedGermplasmList.size(); i++) {
			Integer newEntryId = i + 1;
			importedGermplasmList.get(i).setEntryId(newEntryId);
			importedGermplasmList.get(i).setEntryCode(FieldbookUtil.generateEntryCode(newEntryId));

		}

		return importedGermplasmList;
	}

	protected List<Map<String, Object>> setupAdvanceReviewDataList(List<ImportedGermplasm> importedGermplasmList) {
		List<Map<String, Object>> dataTableDataList = new ArrayList<>();
		for (ImportedGermplasm germplasm : importedGermplasmList) {
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("desig", germplasm.getDesig());
			dataMap.put("gid", ImportedGermplasm.GID_PENDING);
			dataMap.put("entry", germplasm.getEntryId());
			dataMap.put("source", germplasm.getSource());
			dataMap.put("parentage", germplasm.getCross());


			dataMap.put("trialInstanceNumber", germplasm.getTrialInstanceNumber());
			dataMap.put("replicationNumber", germplasm.getReplicationNumber());

			dataTableDataList.add(dataMap);
		}
		return dataTableDataList;
	}

	protected List<TableHeader> getAdvancedStudyTableHeader() {
		List<TableHeader> tableHeaderList = new ArrayList<>();

		tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_ID.getTermNameFromOntology(this.ontologyDataManager), "entry"));
		tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager), "desig"));
		tableHeaderList.add(new TableHeader(ColumnLabels.PARENTAGE.getTermNameFromOntology(this.ontologyDataManager), "parentage"));
		tableHeaderList.add(new TableHeader(ColumnLabels.GID.getTermNameFromOntology(this.ontologyDataManager), "gid"));
		tableHeaderList.add(new TableHeader(ColumnLabels.SEED_SOURCE.getTermNameFromOntology(this.ontologyDataManager), "source"));

		tableHeaderList.add(new TableHeader(ColumnLabels.TRIAL_INSTANCE.getTermNameFromOntology(this.ontologyDataManager),
				"trialInstanceNumber"));
		tableHeaderList.add(new TableHeader(ColumnLabels.REP_NO.getTermNameFromOntology(this.ontologyDataManager), "replicationNumber"));

		return tableHeaderList;
	}

	private List<StandardVariableReference> filterVariablesByProperty(List<SettingDetail> variables, String propertyName) {
		List<StandardVariableReference> list = new ArrayList<>();
		if (variables != null && !variables.isEmpty()) {
			for (SettingDetail detail : variables) {
				if (detail.getVariable() != null && detail.getVariable().getProperty() != null
						&& propertyName.equalsIgnoreCase(detail.getVariable().getProperty())) {
					list.add(new StandardVariableReference(detail.getVariable().getCvTermId(), detail.getVariable().getName(), detail
							.getVariable().getDescription()));
				}
			}
		}
		return list;
	}

	@ResponseBody
	@RequestMapping(value = "/countPlots/{ids}", method = RequestMethod.GET)
	public int countPlots(@PathVariable String ids) throws MiddlewareQueryException {
		String[] idList = ids.split(",");
		List<Integer> idParams = new ArrayList<>();
		for (String id : idList) {
			if (id != null && NumberUtils.isNumber(id)) {
				idParams.add(Double.valueOf(id).intValue());
			}
		}
		return this.fieldbookMiddlewareService.countPlotsWithRecordedVariatesInDataset(this.userSelection.getWorkbook()
				.getMeasurementDatesetId(), idParams);

	}

	@ResponseBody
	@RequestMapping(value = "/checkMethodTypeMode/{methodVariateId}", method = RequestMethod.GET)
	public String checkMethodTypeMode(@PathVariable int methodVariateId) throws MiddlewareQueryException {
		List<MeasurementRow> observations = this.userSelection.getWorkbook().getObservations();
		if (observations != null && !observations.isEmpty()) {
			Set<Integer> methodIds = new HashSet<>();
			for (MeasurementRow row : observations) {
				String value = row.getMeasurementDataValue(methodVariateId);
				if (value != null && NumberUtils.isNumber(value)) {
					methodIds.add(Double.valueOf(value).intValue());
				}
			}
			if (!methodIds.isEmpty()) {
				List<Method> methods = this.germplasmDataManager.getMethodsByIDs(new ArrayList<>(methodIds));
				boolean isBulk = false;
				boolean isLine = false;
				for (Method method : methods) {
					if (method.isBulkingMethod() != null && method.isBulkingMethod()) {
						isBulk = true;
					} else if (method.isBulkingMethod() != null && !method.isBulkingMethod()) {
						isLine = true;
					}
					if (isBulk && isLine) {
						return "MIXED";
					}
				}
				if (isBulk) {
					return "BULK";
				} else {
					return "LINE";
				}
			}
		}
		Locale locale = LocaleContextHolder.getLocale();
		String name = "";
		for (MeasurementVariable variable : this.userSelection.getWorkbook().getAllVariables()) {
			if (variable.getTermId() == methodVariateId) {
				name = variable.getName();
				break;
			}
		}
		return this.messageSource.getMessage("error.advancing.study.empty.method", new String[] {name}, locale);
	}
}
