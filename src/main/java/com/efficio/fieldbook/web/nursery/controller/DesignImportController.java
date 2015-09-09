
package com.efficio.fieldbook.web.nursery.controller;

/**
 * Created by cyrus on 5/8/15.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

/**
 * The Class DesignImportController.
 */
@Controller
@RequestMapping(DesignImportController.URL)
public class DesignImportController extends SettingsController {

	public static final String IS_SUCCESS = "isSuccess";

	public static final String ERROR = "error";

	private static final Logger LOG = LoggerFactory.getLogger(DesignImportController.class);

	public static final String URL = "/DesignImport";

	public static final String REVIEW_DETAILS_PAGINATION_TEMPLATE = "/DesignImport/reviewDetailsPagination";

	@Resource
	private DesignImportParser parser;

	@Resource
	private DesignImportService designImportService;

	@Resource
	private SettingsService settingsService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private UserSelection userSelection;

	@Resource
	private OntologyDataManager ontologyDataManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#show(org.springframework.ui.Model)
	 */
	@Override
	@RequestMapping(method = RequestMethod.GET)
	public String show(Model model) {
		return super.showAngularPage(model);
	}

	@Override
	public String getContentName() {
		return String.format("%s/designImportMain", DesignImportController.URL);
	}

	@ResponseBody
	@RequestMapping(value = "/import/{studyType}", method = RequestMethod.POST, produces = "text/plain")
	public String importFile(@ModelAttribute("importDesignForm") ImportDesignForm form, @PathVariable String studyType) {

		Map<String, Object> resultsMap = new HashMap<>();

		try {

			this.initializeTemporaryWorkbook(studyType);

			DesignImportData designImportData = this.parser.parseFile(form.getFile());

			this.performAutomap(designImportData);

			this.userSelection.setDesignImportData(designImportData);

			resultsMap.put(IS_SUCCESS, 1);

		} catch (MiddlewareQueryException | FileParsingException e) {

			DesignImportController.LOG.error(e.getMessage(), e);

			resultsMap.put(IS_SUCCESS, 0);
			// error messages is still in .prop format,
			resultsMap.put(ERROR, new String[] {e.getMessage()});
		}

		// we return string instead of json to fix IE issue rel. DataTable
		return this.convertObjectToJson(resultsMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getMappingData", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public Map<String, List<DesignHeaderItem>> getMappingData() {
		Map<String, List<DesignHeaderItem>> mappingData = new HashMap<>();

		mappingData.put("unmappedHeaders", this.userSelection.getDesignImportData().getUnmappedHeaders());
		mappingData.put("mappedEnvironmentalFactors",
				this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		mappingData
				.put("mappedDesignFactors", this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));
		mappingData
				.put("mappedGermplasmFactors", this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.GERMPLASM));
		mappingData.put("mappedTraits", this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.VARIATE));

		return mappingData;
	}

	@RequestMapping(value = "/showDetails", method = RequestMethod.GET)
	public String showDetails(Model model) {

		Workbook workbook = this.userSelection.getTemporaryWorkbook();
		DesignImportData designImportData = this.userSelection.getDesignImportData();

		Set<MeasurementVariable> measurementVariables =
				this.designImportService.getDesignMeasurementVariables(workbook, designImportData, false);

		model.addAttribute("measurementVariables", measurementVariables);

		return super.showAjaxPage(model, DesignImportController.REVIEW_DETAILS_PAGINATION_TEMPLATE);

	}

	@ResponseBody
	@RequestMapping(value = "/showDetails/data", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public List<Map<String, Object>> showDetailsData(@RequestBody EnvironmentData environmentData, Model model,
			@ModelAttribute("importDesignForm") ImportDesignForm form) {

		this.processEnvironmentData(environmentData);

		Workbook workbook = this.userSelection.getTemporaryWorkbook();
		DesignImportData designImportData = this.userSelection.getDesignImportData();

		List<MeasurementRow> measurementRows = new ArrayList<>();

		try {
			measurementRows = this.designImportService.generateDesign(workbook, designImportData, environmentData, false);
		} catch (DesignValidationException e) {
			DesignImportController.LOG.error(e.getMessage(), e);
		}

		List<Map<String, Object>> masterList = new ArrayList<>();

		for (MeasurementRow row : measurementRows) {

			Map<String, Object> dataMap = this.generateDatatableDataMap(row, null);
			masterList.add(dataMap);

		}

		return masterList;

	}

	@ResponseBody
	@RequestMapping(value = "/postSelectedNurseryType")
	public Boolean postSelectedNurseryType(@RequestBody String nurseryTypeId) {
		if (StringUtils.isNumeric(nurseryTypeId)) {
			Integer value = Integer.valueOf(nurseryTypeId);
			this.userSelection.setNurseryTypeForDesign(value);
		}

		return true;
	}

	@ResponseBody
	@RequestMapping(value = "/validateAndSaveNewMapping/{noOfEnvironments}", method = RequestMethod.POST)
	public Map<String, Object> validateAndSaveNewMapping(@RequestBody Map<String, List<DesignHeaderItem>> mappedHeaders,
			@PathVariable Integer noOfEnvironments) {

		Map<String, Object> resultsMap = new HashMap<>();
		try {
			this.updateDesignMapping(mappedHeaders);

			this.designImportService.validateDesignData(this.userSelection.getDesignImportData());

			if (!this.designImportService.areTrialInstancesMatchTheSelectedEnvironments(noOfEnvironments,
					this.userSelection.getDesignImportData())) {
				resultsMap.put("warning",
						this.messageSource.getMessage("design.import.warning.trial.instances.donotmatch", null, Locale.ENGLISH));
			}

			boolean hasConflict = false;

			if (this.userSelection.getWorkbook() != null && this.userSelection.getWorkbook().getMeasurementDatasetVariables() != null) {
				hasConflict =
						this.hasConflict(this.designImportService.getMeasurementVariablesFromDataFile(
								this.userSelection.getTemporaryWorkbook(), this.userSelection.getDesignImportData()), new HashSet<>(
								this.userSelection.getWorkbook().getMeasurementDatasetVariables()));
			}

			resultsMap.put("success", Boolean.TRUE);
			resultsMap.put("hasConflict", hasConflict);
		} catch (MiddlewareQueryException | DesignValidationException e) {

			DesignImportController.LOG.error(e.getMessage(), e);

			resultsMap.put("success", Boolean.FALSE);
			resultsMap.put(ERROR, e.getMessage());
			resultsMap.put("message", e.getMessage());
		}

		return resultsMap;
	}

	boolean hasConflict(Set<MeasurementVariable> setA, Set<MeasurementVariable> setB) {
		Set<MeasurementVariable> a;
		Set<MeasurementVariable> b;

		if (setA.size() <= setB.size()) {
			a = setA;
			b = setB;
		} else {
			a = setB;
			b = setA;
		}

		for (MeasurementVariable e : a) {
			if (b.contains(e)) {
				return true;
			}
		}
		return false;
	}

	protected void updateDesignMapping(Map<String, List<DesignHeaderItem>> mappedHeaders) {
		Map<PhenotypicType, List<DesignHeaderItem>> newMappingResults = new HashMap<>();

		for (Map.Entry<String, List<DesignHeaderItem>> item : mappedHeaders.entrySet()) {
			for (DesignHeaderItem mappedHeader : item.getValue()) {

				StandardVariable stdVar = this.ontologyDataManager.getStandardVariable(mappedHeader.getId());
				mappedHeader.setVariable(stdVar);

			}

			if ("mappedEnvironmentalFactors".equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.TRIAL_ENVIRONMENT, item.getValue());
			} else if ("mappedDesignFactors".equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.TRIAL_DESIGN, item.getValue());
			} else if ("mappedGermplasmFactors".equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.GERMPLASM, item.getValue());
			} else if ("mappedTraits".equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.VARIATE, item.getValue());
			}
		}

		this.userSelection.getDesignImportData().setMappedHeaders(newMappingResults);
	}

	@ResponseBody
	@RequestMapping(value = "/generate", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public Map<String, Object> generateMeasurements(@RequestBody EnvironmentData environmentData) {

		Map<String, Object> resultsMap = new HashMap<>();

		this.processEnvironmentData(environmentData);

		try {

			this.checkTheDeletedSettingDetails(this.userSelection, this.userSelection.getDesignImportData());

			this.initializeTemporaryWorkbook(this.userSelection.getTemporaryWorkbook().getStudyDetails().getStudyType().name());

			Workbook workbook = this.userSelection.getTemporaryWorkbook();
			DesignImportData designImportData = this.userSelection.getDesignImportData();

			this.removeExperimentDesignVariables(workbook.getFactors());

			List<MeasurementRow> measurementRows;
			Set<MeasurementVariable> measurementVariables;
			Set<StandardVariable> expDesignVariables;
			Set<MeasurementVariable> experimentalDesignMeasurementVariables;

			measurementRows = this.designImportService.generateDesign(workbook, designImportData, environmentData, false);

			workbook.setObservations(measurementRows);

			measurementVariables = this.designImportService.getDesignMeasurementVariables(workbook, designImportData, false);

			workbook.setMeasurementDatasetVariables(new ArrayList<>(measurementVariables));

			expDesignVariables = this.designImportService.getDesignRequiredStandardVariables(workbook, designImportData);

			workbook.setExpDesignVariables(new ArrayList<>(expDesignVariables));

			experimentalDesignMeasurementVariables =
					this.designImportService.getDesignRequiredMeasurementVariable(workbook, designImportData);

			this.userSelection.setExperimentalDesignVariables(new ArrayList<>(experimentalDesignMeasurementVariables));

			// Only for Trial
			this.addFactorsIfNecessary(workbook, designImportData);

			// Only for Nursery
			this.addConditionsIfNecessary(workbook, designImportData);

			this.addVariates(workbook, designImportData);

			this.addExperimentDesign(workbook, experimentalDesignMeasurementVariables);

			// Only for Trial
			this.populateTrialLevelVariableListIfNecessary(workbook);

			// Only for Nursery
			this.populateStudyLevelVariableListIfNecessary(workbook, environmentData, designImportData);

			this.createTrialObservations(environmentData, workbook, designImportData);

			resultsMap.put(IS_SUCCESS, 1);
			resultsMap.put("environmentData", environmentData);
			resultsMap.put("environmentSettings", this.userSelection.getTrialLevelVariableList());

		} catch (Exception e) {

			DesignImportController.LOG.error(e.getMessage(), e);

			resultsMap.put(IS_SUCCESS, 0);
			// error messages is still in .prop format,
			resultsMap.put(ERROR, new String[] {e.getMessage()});
		}

		return resultsMap;
	}

	void checkTheDeletedSettingDetails(UserSelection userSelection, DesignImportData designImportData) {

		/**
		 * If the user deleted the Environment variables (i.e. SITE_NAME, SITE_CODE) in the Trial, and then attempted to re-import a custom
		 * design with variables that the user just deleted, the system should re-add these deleted variables.
		 */

		Map<String, String> idNameMap = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
		Map<String, String> nameIdMap = this.switchKey(idNameMap);

		for (MeasurementVariable mvar : this.designImportService.getMeasurementVariablesFromDataFile(null, designImportData)) {

			if (userSelection.getDeletedTrialLevelVariables() != null) {

				Iterator<SettingDetail> deletedTrialLevelVariables = userSelection.getDeletedTrialLevelVariables().iterator();
				while (deletedTrialLevelVariables.hasNext()) {
					SettingDetail deletedSettingDetail = deletedTrialLevelVariables.next();

					if (deletedSettingDetail.getVariable().getCvTermId().intValue() == mvar.getTermId()) {

						deletedSettingDetail.getVariable().setOperation(Operation.UPDATE);
						userSelection.getTrialLevelVariableList().add(deletedSettingDetail);

						deletedTrialLevelVariables.remove();

					}

					String termIdOfName = idNameMap.get(String.valueOf(deletedSettingDetail.getVariable().getCvTermId()));
					if (termIdOfName != null) {

						this.updateOperation(Integer.valueOf(termIdOfName), userSelection.getTrialLevelVariableList(), Operation.UPDATE);

						deletedSettingDetail.getVariable().setOperation(Operation.UPDATE);
						userSelection.getTrialLevelVariableList().add(deletedSettingDetail);

						deletedTrialLevelVariables.remove();
					}

					String termIdOfId = nameIdMap.get(String.valueOf(deletedSettingDetail.getVariable().getCvTermId()));
					if (termIdOfId != null) {
						this.updateOperation(Integer.valueOf(termIdOfId), userSelection.getTrialLevelVariableList(), Operation.UPDATE);

						deletedSettingDetail.getVariable().setOperation(Operation.UPDATE);
						userSelection.getTrialLevelVariableList().add(deletedSettingDetail);

						deletedTrialLevelVariables.remove();
					}
				}

			}

		}

	}

	void updateOperation(int termId, List<SettingDetail> settingDetails, Operation operation) {

		for (SettingDetail sd : settingDetails) {
			if (sd.getVariable().getCvTermId().intValue() == termId) {
				sd.getVariable().setOperation(operation);
				break;
			}
		}

	}

	public void initializeTemporaryWorkbook(String studyType) {

		List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
		// transfer over data from user input into the list of setting details stored in the session
		List<SettingDetail> combinedList = new ArrayList<>();

		if (basicDetails != null) {
			combinedList.addAll(basicDetails);
		}

		if (studyLevelConditions != null) {
			combinedList.addAll(studyLevelConditions);
		}

		String name = "";

		Workbook workbook;
		StudyDetails details = new StudyDetails();

		if ("T".equalsIgnoreCase(studyType)) {

			workbook = this.createWorkbookFromUserSelection(combinedList, name, false);

			details.setStudyType(StudyType.T);

		} else {

			List<SettingDetail> variatesList = new ArrayList<>();

			if (this.userSelection.getBaselineTraitsList() != null) {
				variatesList.addAll(this.userSelection.getBaselineTraitsList());
			}

			if (this.userSelection.getSelectionVariates() != null) {
				variatesList.addAll(this.userSelection.getSelectionVariates());
			}

			workbook = this.createWorkbookFromUserSelection(combinedList, name, true);

			details.setStudyType(StudyType.N);

		}

		workbook.setStudyDetails(details);

		this.userSelection.setTemporaryWorkbook(workbook);

	}

	Workbook createWorkbookFromUserSelection(List<SettingDetail> combinedList, String name, boolean isNursery) {
		Workbook workbook;
		Dataset dataset =
				(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList,
						this.userSelection.getPlotsLevelList(), this.userSelection.getBaselineTraitsList(), this.userSelection,
						this.userSelection.getTrialLevelVariableList(), this.userSelection.getTreatmentFactors(), null, null,
						this.userSelection.getNurseryConditions(), isNursery);

		workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, isNursery);
		return workbook;
	}

	void addExperimentDesign(Workbook workbook, Set<MeasurementVariable> experimentalDesignMeasurementVariables) {

		ExpDesignParameterUi designParam = new ExpDesignParameterUi();
		designParam.setDesignType(3);

		List<Integer> expDesignTermIds = new ArrayList<>();
		expDesignTermIds.add(TermId.EXPERIMENT_DESIGN_FACTOR.getId());

		this.userSelection.setExpDesignParams(designParam);
		this.userSelection.setExpDesignVariables(expDesignTermIds);

		TermId termId = TermId.getById(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
		SettingsUtil.addTrialCondition(termId, designParam, workbook, this.fieldbookMiddlewareService);

		workbook.getFactors().addAll(experimentalDesignMeasurementVariables);

	}

	void addFactorsIfNecessary(Workbook workbook, DesignImportData designImportData) {

		if (workbook.getStudyDetails().getStudyType() == StudyType.T) {

			Set<MeasurementVariable> uniqueFactors = new HashSet<>(workbook.getFactors());
			uniqueFactors.addAll(this.designImportService.extractMeasurementVariable(PhenotypicType.TRIAL_ENVIRONMENT,
					designImportData.getMappedHeaders()));

			for (MeasurementVariable mvar : uniqueFactors) {
				MeasurementVariable tempMvar = this.getMeasurementVariableInListByTermId(mvar.getTermId(), workbook.getConditions());
				if (tempMvar != null) {
					mvar.setOperation(tempMvar.getOperation());
					mvar.setName(tempMvar.getName());
				}
			}

			workbook.getFactors().clear();
			workbook.getFactors().addAll(new ArrayList<>(uniqueFactors));

		}

	}

	void addConditionsIfNecessary(Workbook workbook, DesignImportData designImportData) {

		if (workbook.getStudyDetails().getStudyType() == StudyType.N) {

			Set<MeasurementVariable> uniqueConditions = new HashSet<>(workbook.getConditions());
			uniqueConditions.addAll(this.designImportService.extractMeasurementVariable(PhenotypicType.TRIAL_ENVIRONMENT,
					designImportData.getMappedHeaders()));

			workbook.getConditions().clear();
			workbook.getConditions().addAll(new ArrayList<>(uniqueConditions));

		}

	}

	void addVariates(Workbook workbook, DesignImportData designImportData) {
		Set<MeasurementVariable> uniqueVariates = new HashSet<>(workbook.getVariates());
		uniqueVariates.addAll(this.designImportService.extractMeasurementVariable(PhenotypicType.VARIATE,
				designImportData.getMappedHeaders()));

		workbook.getVariates().clear();
		workbook.getVariates().addAll(new ArrayList<>(uniqueVariates));
	}

	void populateTrialLevelVariableListIfNecessary(Workbook workbook) {

		// retrieve all trial level factors and convert them to setting details
		Set<MeasurementVariable> trialLevelFactors = new HashSet<>();
		for (MeasurementVariable factor : workbook.getFactors()) {
			if (PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().contains(factor.getLabel())) {
				trialLevelFactors.add(factor);
			}
		}

		List<SettingDetail> newDetails = new ArrayList<>();

		for (MeasurementVariable mvar : trialLevelFactors) {
			SettingDetail newDetail =
					this.settingsService.createSettingDetail(mvar.getTermId(), mvar.getName(), this.userSelection,
							this.getCurrentIbdbUserId(), this.getCurrentProject().getUniqueID());
			newDetail.setDeletable(true);
			newDetails.add(newDetail);
		}

		this.addNewSettingDetailsIfNecessary(newDetails);

	}

	void populateStudyLevelVariableListIfNecessary(Workbook workbook, EnvironmentData environmentData, DesignImportData designImportData) {
		if (workbook.getStudyDetails().getStudyType() == StudyType.N) {

			Map<String, String> managementDetailValues = environmentData.getEnvironments().get(0).getManagementDetailValues();

			List<SettingDetail> newDetails = new ArrayList<>();

			for (MeasurementVariable mvar : workbook.getConditions()) {

				SettingDetail newDetail =
						this.settingsService.createSettingDetail(mvar.getTermId(), mvar.getName(), this.userSelection,
								this.getCurrentIbdbUserId(), this.getCurrentProject().getUniqueID());

				String value = managementDetailValues.get(String.valueOf(newDetail.getVariable().getCvTermId()));
				if (value != null) {
					newDetail.setValue(value);
				} else {
					newDetail.setValue("");
				}

				newDetail.getVariable().setOperation(mvar.getOperation());
				newDetails.add(newDetail);

			}

			this.resolveTheEnvironmentFactorsWithIDNamePairing(environmentData, designImportData, newDetails);

			this.userSelection.getStudyLevelConditions().clear();
			this.userSelection.getStudyLevelConditions().addAll(newDetails);
		}

	}

	void addNewSettingDetailsIfNecessary(List<SettingDetail> newDetails) {

		if (this.userSelection.getTrialLevelVariableList() == null) {
			this.userSelection.setTrialLevelVariableList(new ArrayList<SettingDetail>());
		}

		for (SettingDetail settingDetail : newDetails) {

			boolean isExisting = false;

			for (SettingDetail settingDetailFromUserSelection : this.userSelection.getTrialLevelVariableList()) {
				if (settingDetail.getVariable().getCvTermId().intValue() == settingDetailFromUserSelection.getVariable().getCvTermId()
						.intValue()) {
					isExisting = true;
					break;
				}
			}

			if (!isExisting) {
				this.userSelection.getTrialLevelVariableList().add(settingDetail);
			}

		}

	}

	void createTrialObservations(EnvironmentData environmentData, Workbook workbook, DesignImportData designImportData) {

		// get the Experiment Design MeasurementVariable
		Set<MeasurementVariable> trialVariables = new HashSet<>(workbook.getTrialFactors());

		trialVariables.addAll(workbook.getConstants());

		for (MeasurementVariable trialCondition : workbook.getTrialConditions()) {
			if (trialCondition.getTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId()) {
				trialVariables.add(trialCondition);
			}
		}

		this.resolveTheEnvironmentFactorsWithIDNamePairing(environmentData, designImportData, trialVariables);

		List<MeasurementRow> trialEnvironmentValues = this.createTrialEnvironmentValues(environmentData, trialVariables);

		workbook.setTrialObservations(trialEnvironmentValues);

		if (workbook.getStudyDetails().getStudyType() == StudyType.T) {
			this.fieldbookService.addConditionsToTrialObservationsIfNecessary(workbook);
		}

	}

	protected List<MeasurementRow> createTrialEnvironmentValues(EnvironmentData environmentData, Set<MeasurementVariable> trialVariables) {
		List<MeasurementRow> trialEnvironmentValues =
				WorkbookUtil.createMeasurementRowsFromEnvironments(environmentData.getEnvironments(), new ArrayList<>(trialVariables),
						this.userSelection.getExpDesignParams());
		return trialEnvironmentValues;
	}

	Map<String, Object> generateDatatableDataMap(MeasurementRow row, String suffix) {
		Map<String, Object> dataMap = new HashMap<>();
		// the 4 attributes are needed always
		for (MeasurementData data : row.getDataList()) {
			String displayVal = data.getDisplayValue();
			if (suffix != null) {
				displayVal += suffix;
			}

			if (data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())
					|| data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())) {
				Object[] categArray = new Object[] {displayVal, data.isAccepted()};
				dataMap.put(data.getMeasurementVariable().getName(), categArray);
			} else {
				dataMap.put(data.getMeasurementVariable().getName(), displayVal);
			}
		}
		return dataMap;
	}

	void performAutomap(DesignImportData designImportData) {
		Map<PhenotypicType, List<DesignHeaderItem>> result =
				this.designImportService.categorizeHeadersByPhenotype(designImportData.getUnmappedHeaders());

		designImportData.setMappedHeaders(result);
		designImportData.setUnmappedHeaders(result.get(null));

	}

	void processEnvironmentData(EnvironmentData data) {
		for (int i = 0; i < data.getEnvironments().size(); i++) {
			Map<String, String> values = data.getEnvironments().get(i).getManagementDetailValues();
			if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			} else if (values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null
					|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			}
		}
	}

	void resolveTheEnvironmentFactorsWithIDNamePairing(EnvironmentData environmentData, DesignImportData designImportData,
			Set<MeasurementVariable> trialVariables) {

		Map<String, String> idNameMap = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
		Map<String, String> nameIdMap = this.switchKey(idNameMap);

		for (Environment environment : environmentData.getEnvironments()) {

			Map<String, String> copyOfManagementDetailValues = new HashMap<>();
			copyOfManagementDetailValues.putAll(environment.getManagementDetailValues());

			for (Entry<String, String> managementDetail : environment.getManagementDetailValues().entrySet()) {

				String headerName =
						this.getLocalNameFromSettingDetails(Integer.valueOf(managementDetail.getKey()),
								this.userSelection.getTrialLevelVariableList());
				String standardVariableName =
						this.getVariableNameFromSettingDetails(Integer.valueOf(managementDetail.getKey()),
								this.userSelection.getTrialLevelVariableList());

				if ("".equals(headerName)) {

					headerName =
							this.getHeaderName(Integer.valueOf(managementDetail.getKey()),
									designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
				}

				if ("".equals(standardVariableName)) {
					standardVariableName =
							this.getStandardVariableName(Integer.valueOf(managementDetail.getKey()), designImportData.getMappedHeaders()
									.get(PhenotypicType.TRIAL_ENVIRONMENT));
				}

				// For TRIAL_LOCATION (Location Name)
				if (Integer.valueOf(managementDetail.getKey()) == TermId.TRIAL_LOCATION.getId()) {
					String termId = nameIdMap.get(managementDetail.getKey());
					if (termId != null) {

						if (this.isTermIdExisting(Integer.valueOf(managementDetail.getKey()),
								designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT))) {

							Location location =
									this.fieldbookMiddlewareService.getLocationByName(managementDetail.getValue(), Operation.EQUAL);
							if (location == null) {
								copyOfManagementDetailValues.put(termId, "");
							} else {
								copyOfManagementDetailValues.put(termId, String.valueOf(location.getLocid()));
							}

						}

						SettingDetail settingDetail = this.createSettingDetail(Integer.valueOf(termId), headerName);
						this.addSettingDetailToTrialLevelVariableListIfNecessary(settingDetail);

						MeasurementVariable measurementVariable = null;
						StandardVariable var = this.ontologyDataManager.getStandardVariable(Integer.valueOf(termId));
						measurementVariable =
								ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, this.fieldbookService);
						measurementVariable.setName(standardVariableName + AppConstants.ID_SUFFIX.getString());
						trialVariables.add(measurementVariable);

						copyOfManagementDetailValues.remove(managementDetail.getKey());

						SettingsUtil.hideVariableInSession(this.userSelection.getTrialLevelVariableList(),
								Integer.valueOf(managementDetail.getKey()));
					}
				} else if (Integer.valueOf(managementDetail.getKey()) == 8373) {
					String termId = nameIdMap.get(managementDetail.getKey());
					if (termId != null) {

						copyOfManagementDetailValues.put(termId, String.valueOf(this.getCurrentIbdbUserId()));

						SettingDetail settingDetail = this.createSettingDetail(Integer.valueOf(termId), headerName);
						this.addSettingDetailToTrialLevelVariableListIfNecessary(settingDetail);

						MeasurementVariable measurementVariable = null;
						StandardVariable var = this.ontologyDataManager.getStandardVariable(Integer.valueOf(termId));
						measurementVariable =
								ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, this.fieldbookService);
						measurementVariable.setName(standardVariableName + AppConstants.ID_SUFFIX.getString());
						trialVariables.add(measurementVariable);

						copyOfManagementDetailValues.remove(managementDetail.getKey());

						SettingsUtil.hideVariableInSession(this.userSelection.getTrialLevelVariableList(),
								Integer.valueOf(managementDetail.getKey()));
					}

				} else {

					MeasurementVariable measurementVariable = null;
					StandardVariable var = this.ontologyDataManager.getStandardVariable(Integer.valueOf(managementDetail.getKey()));
					measurementVariable =
							ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, this.fieldbookService);
					measurementVariable.setName(var.getName());
					trialVariables.add(measurementVariable);

				}

				// For Categorical Variables
				StandardVariable tempStandardVarable =
						this.ontologyDataManager.getStandardVariable(Integer.valueOf(managementDetail.getKey()));
				if (tempStandardVarable != null && tempStandardVarable.hasEnumerations()) {

					Enumeration enumeration = this.findInEnumeration(managementDetail.getValue(), tempStandardVarable.getEnumerations());
					if (enumeration != null) {
						copyOfManagementDetailValues.put(managementDetail.getKey(), String.valueOf(enumeration.getId()));
					}
				}

			}

			environment.getManagementDetailValues().clear();
			environment.getManagementDetailValues().putAll(copyOfManagementDetailValues);

		}

	}

	void resolveTheEnvironmentFactorsWithIDNamePairing(EnvironmentData environmentData, DesignImportData designImportData,
			List<SettingDetail> newDetails) {

		Map<String, String> idNameMap = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
		Map<String, String> nameIdMap = this.switchKey(idNameMap);

		Environment environment = environmentData.getEnvironments().get(0);

		Map<String, String> copyOfManagementDetailValues = new HashMap<>();
		copyOfManagementDetailValues.putAll(environment.getManagementDetailValues());

		for (Entry<String, String> managementDetail : environment.getManagementDetailValues().entrySet()) {

			// For TRIAL_LOCATION (Location Name)
			if (Integer.valueOf(managementDetail.getKey()) == TermId.TRIAL_LOCATION.getId()) {
				String termId = nameIdMap.get(managementDetail.getKey());
				if (termId != null) {

					Location location = this.fieldbookMiddlewareService.getLocationByName(managementDetail.getValue(), Operation.EQUAL);
					if (location == null) {
						copyOfManagementDetailValues.put(termId, "");
					} else {
						copyOfManagementDetailValues.put(termId, String.valueOf(location.getLocid()));
					}

					String headerName =
							this.getHeaderName(Integer.valueOf(managementDetail.getKey()),
									designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

					SettingDetail settingDetail = this.createSettingDetail(Integer.valueOf(termId), headerName);
					settingDetail.getVariable().setOperation(Operation.ADD);
					settingDetail.setValue("");
					if (location != null) {
						settingDetail.setValue(String.valueOf(location.getLocid()));
					}

					this.addSettingOrUpdateDetailToTargetListIfNecessary(settingDetail, newDetails);

					copyOfManagementDetailValues.remove(managementDetail.getKey());

				}
			}

			// For COOPERATOR and PI_NAME
			if (Integer.valueOf(managementDetail.getKey()) == 8373 || Integer.valueOf(managementDetail.getKey()) == 8100) {
				String termId = nameIdMap.get(managementDetail.getKey());
				if (termId != null) {

					copyOfManagementDetailValues.put(termId, String.valueOf(this.getCurrentIbdbUserId()));

					String headerName =
							this.getHeaderName(Integer.valueOf(managementDetail.getKey()),
									designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

					SettingDetail settingDetail = this.createSettingDetail(Integer.valueOf(termId), headerName);
					settingDetail.getVariable().setOperation(Operation.ADD);
					settingDetail.setValue(String.valueOf(this.getCurrentIbdbUserId()));

					this.addSettingOrUpdateDetailToTargetListIfNecessary(settingDetail, newDetails);

					copyOfManagementDetailValues.remove(managementDetail.getKey());

				}

			}

			// For Categorical Variables
			StandardVariable tempStandardVarable = this.ontologyDataManager.getStandardVariable(Integer.valueOf(managementDetail.getKey()));
			if (tempStandardVarable != null && tempStandardVarable.hasEnumerations()) {

				Enumeration enumeration = this.findInEnumeration(managementDetail.getValue(), tempStandardVarable.getEnumerations());
				if (enumeration != null) {
					copyOfManagementDetailValues.put(managementDetail.getKey(), String.valueOf(enumeration.getId()));
				}
			}

		}

		environment.getManagementDetailValues().clear();
		environment.getManagementDetailValues().putAll(copyOfManagementDetailValues);

	}

	protected Enumeration findInEnumeration(String value, List<Enumeration> enumerations) {
		for (Enumeration enumeration : enumerations) {
			if (enumeration.getName().equalsIgnoreCase(value)) {
				return enumeration;
			} else if (enumeration.getDescription().equalsIgnoreCase(value)) {
				return enumeration;
			}
		}
		return null;
	}

	protected void addSettingDetailToTrialLevelVariableListIfNecessary(SettingDetail settingDetail) {

		for (SettingDetail sd : this.userSelection.getTrialLevelVariableList()) {
			if (sd.getVariable().getCvTermId().intValue() == settingDetail.getVariable().getCvTermId().intValue()) {
				return;
			}
		}
		this.userSelection.getTrialLevelVariableList().add(settingDetail);
	}

	protected void addSettingOrUpdateDetailToTargetListIfNecessary(SettingDetail settingDetail, List<SettingDetail> targetList) {

		Iterator<SettingDetail> iterator = targetList.iterator();
		while (iterator.hasNext()) {
			SettingDetail sd = iterator.next();
			if (sd.getVariable().getCvTermId().intValue() == settingDetail.getVariable().getCvTermId().intValue()) {
				settingDetail.getVariable().setOperation(Operation.UPDATE);
				iterator.remove();
			}
		}

		targetList.add(settingDetail);

	}

	protected boolean isTermIdExisting(int termId, List<DesignHeaderItem> items) {
		for (DesignHeaderItem item : items) {
			if (item.getId() == termId) {
				return true;
			}
		}
		return false;
	}

	protected String getHeaderName(int termId, List<DesignHeaderItem> items) {
		for (DesignHeaderItem item : items) {
			if (item.getId() == termId) {
				return item.getName();
			}
		}
		return "";
	}

	protected String getStandardVariableName(Integer termId, List<DesignHeaderItem> items) {
		for (DesignHeaderItem item : items) {
			if (item.getId() == termId) {
				return item.getVariable().getName();
			}
		}
		return "";
	}

	protected Map<String, String> switchKey(Map<String, String> map) {
		Map<String, String> newMap = new HashMap<>();
		for (Entry<String, String> entry : map.entrySet()) {
			newMap.put(entry.getValue(), entry.getKey());
		}
		return newMap;
	}

	protected MeasurementVariable getMeasurementVariableInListByTermId(int termid, List<MeasurementVariable> list) {
		for (MeasurementVariable mvar : list) {
			if (termid == mvar.getTermId()) {
				return mvar;
			}
		}

		return null;
	}

	protected void removeExperimentDesignVariables(List<MeasurementVariable> variables) {
		Iterator<MeasurementVariable> iterator = variables.iterator();
		while (iterator.hasNext()) {
			MeasurementVariable variable = iterator.next();
			if (variable.getStoredIn() == TermId.TRIAL_DESIGN_INFO_STORAGE.getId()) {
				iterator.remove();
			}
		}

	}

	protected String getLocalNameFromSettingDetails(int termId, List<SettingDetail> settingDetails) {
		for (SettingDetail detail : settingDetails) {
			if (detail.getVariable().getCvTermId().intValue() == termId) {
				if (detail.getDisplayValue() == null) {
					return detail.getVariable().getName();
				} else {
					return detail.getDisplayValue();
				}

			}
		}
		return "";
	}

	protected String getVariableNameFromSettingDetails(int termId, List<SettingDetail> settingDetails) {
		for (SettingDetail detail : settingDetails) {
			if (detail.getVariable().getCvTermId().intValue() == termId) {
				return detail.getVariable().getName();
			}
		}
		return "";
	}

}
