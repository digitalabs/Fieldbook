package com.efficio.fieldbook.web.trial.controller;

import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.*;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.trial.bean.*;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public abstract class BaseTrialController extends SettingsController {

	private static final Logger LOG = LoggerFactory.getLogger(BaseTrialController.class);

	public static final String URL_SETTINGS = "TrialManager/templates/trialSettings";
	public static final String URL_GERMPLASM = "TrialManager/templates/germplasmDetails";
	public static final String URL_ENVIRONMENTS = "TrialManager/templates/environments";
	public static final String URL_TREATMENT = "TrialManager/templates/treatment";
	public static final String URL_EXPERIMENTAL_DESIGN = "TrialManager/templates/experimentalDesign";
	public static final String URL_MEASUREMENT = "TrialManager/templates/measurements";
	public static final String URL_DATATABLE = "Common/showAddOrRemoveTraitsPagination";

	/** The ontology service. */
	@Resource
	protected OntologyService ontologyService;

	protected void createStudyDetails(Workbook workbook, BasicDetails detailBean) {
		if (workbook.getStudyDetails() == null) {
			workbook.setStudyDetails(new StudyDetails());
		}

		StudyDetails studyDetails = workbook.getStudyDetails();

		studyDetails.setId(detailBean.getStudyID());
		studyDetails.setTitle(detailBean.getBasicDetails().get(Integer.toString(TermId.STUDY_TITLE.getId())));
		studyDetails.setObjective(detailBean.getBasicDetails().get(Integer.toString(TermId.STUDY_OBJECTIVE.getId())));
		studyDetails.setStudyName(detailBean.getBasicDetails().get(Integer.toString(TermId.STUDY_NAME.getId())));
		studyDetails.setStartDate(detailBean.getBasicDetails().get(Integer.toString(TermId.START_DATE.getId())));
		studyDetails.setEndDate(detailBean.getBasicDetails().get(Integer.toString(TermId.END_DATE.getId())));
		studyDetails.setStudyType(StudyType.T);

		if (detailBean.getFolderId() != null) {
			studyDetails.setParentFolderId(detailBean.getFolderId());
		}

		studyDetails.print(1);
	}

	protected void processEnvironmentData(EnvironmentData data) {
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

	protected void populateSettingData(List<SettingDetail> details, Map<String, String> values) {
		if (details == null || details.isEmpty()) {
			return;
		}

		for (SettingDetail detail : details) {
			if (values.containsKey(detail.getVariable().getCvTermId().toString())) {
				detail.setValue(values.get(detail.getVariable().getCvTermId().toString()));
			}
		}
	}

	protected List<List<ValueReference>> convertToValueReference(List<Environment> environments) {
		List<List<ValueReference>> returnVal = new ArrayList<List<ValueReference>>(environments.size());

		for (Environment environment : environments) {
			List<ValueReference> valueRefList = new ArrayList<ValueReference>();

			for (Map.Entry<String, String> entry : environment.getManagementDetailValues().entrySet()) {
				ValueReference valueRef = new ValueReference(entry.getKey(), entry.getValue());
				valueRefList.add(valueRef);
			}

			returnVal.add(valueRefList);
		}

		return returnVal;
	}

	protected TabInfo prepareExperimentalDesignTabInfo(ExperimentalDesignVariable xpDesignVariable, boolean isUsePrevious)
			throws MiddlewareQueryException {
		TabInfo tabInfo = new TabInfo();
		// currently, the saved experimental design information is not loaded up when choosing a previous trial as template
		if (!isUsePrevious && xpDesignVariable != null) {
			ExpDesignParameterUi data = new ExpDesignParameterUi();

			// as per discussion, resolvable is always set to true currently
			data.setIsResolvable(true);

			// default values
			data.setDesignType(0);
			data.setUseLatenized(false);

			data.setBlockSize(this.getExperimentalDesignData(xpDesignVariable.getBlockSize()));

			// set cols per replication
			data.setColsPerReplications(this.getExperimentalDesignData(xpDesignVariable.getNumberOfColsInReps()));
			data.setRowsPerReplications(this.getExperimentalDesignData(xpDesignVariable.getNumberOfRowsInReps()));

			data.setNclatin(this.getExperimentalDesignData(xpDesignVariable.getNumberOfContiguousColsLatinize()));
			data.setNblatin(this.getExperimentalDesignData(xpDesignVariable.getNumberOfContiguousBlocksLatinize()));
			data.setNrlatin(this.getExperimentalDesignData(xpDesignVariable.getNumberOfContiguousRowsLatinize()));

			data.setReplatinGroups(this.getExperimentalDesignData(xpDesignVariable.getNumberOfRepsInCols()));
			String replicationsMap = this.getExperimentalDesignData(xpDesignVariable.getReplicationsMap());

			if (replicationsMap != null && NumberUtils.isNumber(replicationsMap)) {
				Integer repArrangementID = Integer.parseInt(replicationsMap);
				if (TermId.REPS_IN_SINGLE_COL.getId() == repArrangementID) {
					data.setReplicationsArrangement(1);
				} else if (TermId.REPS_IN_SINGLE_ROW.getId() == repArrangementID) {
					data.setReplicationsArrangement(2);
				} else if (TermId.REPS_IN_ADJACENT_COLS.getId() == repArrangementID) {
					data.setReplicationsArrangement(3);
				}
			}

			data.setReplicationsCount(this.getExperimentalDesignData(xpDesignVariable.getNumberOfReplicates()));
			String designTypeString =
					xpDesignVariable.getExperimentalDesign() == null ? null : xpDesignVariable.getExperimentalDesign().getValue();
			if (designTypeString != null && NumberUtils.isNumber(designTypeString)) {
				Integer designTypeTermID = Integer.parseInt(designTypeString);

				if (TermId.RANDOMIZED_COMPLETE_BLOCK.getId() == designTypeTermID) {
					data.setDesignType(0);
					data.setUseLatenized(false);
				} else if (TermId.RESOLVABLE_INCOMPLETE_BLOCK_LATIN.getId() == designTypeTermID) {
					data.setDesignType(1);
					data.setUseLatenized(true);
				} else if (TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId() == designTypeTermID) {
					data.setDesignType(1);
					data.setUseLatenized(false);
				} else if (TermId.RESOLVABLE_INCOMPLETE_ROW_COL_LATIN.getId() == designTypeTermID) {
					data.setDesignType(2);
					data.setUseLatenized(true);
				} else if (TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId() == designTypeTermID) {
					data.setDesignType(2);
					data.setUseLatenized(false);
				} else if (TermId.OTHER_DESIGN.getId() == designTypeTermID) {
					data.setDesignType(3);
				}
			}

			tabInfo.setData(data);
		}

		return tabInfo;
	}

	protected String getExperimentalDesignData(MeasurementVariable var) {
		if (var != null) {
			return var.getValue();
		} else {
			return null;
		}
	}

	protected TabInfo prepareGermplasmTabInfo(List<MeasurementVariable> measurementVariables, boolean isUsePrevious)
			throws MiddlewareException {
		List<SettingDetail> detailList = new ArrayList<SettingDetail>();
		List<Integer> requiredIDList = this.buildVariableIDList(AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString());

		for (MeasurementVariable var : measurementVariables) {
			// this condition is required so that treatment factors are not included in the list of factors for the germplasm tab
			if (var.getTreatmentLabel() != null && !var.getTreatmentLabel().isEmpty() || this.inRequiredExpDesignVar(var.getTermId())
					&& isUsePrevious) {
				continue;
			}

			SettingDetail detail = this.createSettingDetail(var.getTermId(), var.getName(), VariableType.GERMPLASM_DESCRIPTOR.getRole().name());

			if (requiredIDList.contains(var.getTermId())) {
				detail.setDeletable(false);
			} else {
				detail.setDeletable(true);
			}

			// set all variables with trial design role to hidden
			if (var.getRole() == PhenotypicType.TRIAL_DESIGN) {
				detail.setHidden(true);
			} else {
				detail.setHidden(false);
			}

			if (!isUsePrevious) {
				detail.getVariable().setOperation(Operation.UPDATE);
			} else {
				detail.getVariable().setOperation(Operation.ADD);
			}

			detailList.add(detail);
		}

		TabInfo info = new TabInfo();
		info.setSettings(detailList);

		this.userSelection.setPlotsLevelList(detailList);

		return info;
	}

	protected boolean inRequiredExpDesignVar(int termId) {
		StringTokenizer token = new StringTokenizer(AppConstants.EXP_DESIGN_REQUIRED_VARIABLES.getString(), ",");

		while (token.hasMoreTokens()) {
			if (Integer.parseInt(token.nextToken()) == termId) {
				return true;
			}
		}
		return false;
	}

	protected TabInfo prepareTreatmentFactorsInfo(List<TreatmentVariable> treatmentVariables, boolean isUsePrevious)
			throws MiddlewareException {
		Map<Integer, SettingDetail> levelDetails = new HashMap<Integer, SettingDetail>();
		Map<String, TreatmentFactorData> currentData = new HashMap<String, TreatmentFactorData>();
		Map<String, List<SettingDetail>> treatmentFactorPairs = new HashMap<String, List<SettingDetail>>();

		for (TreatmentVariable treatmentVariable : treatmentVariables) {
			Integer levelFactorID = treatmentVariable.getLevelVariable().getTermId();
			if (!levelDetails.containsKey(levelFactorID)) {
				SettingDetail detail = this.createSettingDetail(levelFactorID, treatmentVariable.getLevelVariable().getName(), VariableType.TREATMENT_FACTOR.getRole().name());

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				levelDetails.put(levelFactorID, detail);
			}

			TreatmentFactorData treatmentFactorData;
			if (!currentData.containsKey(levelFactorID.toString())) {
				treatmentFactorData = new TreatmentFactorData();
				treatmentFactorData.setVariableId(treatmentVariable.getValueVariable().getTermId());
				currentData.put(levelFactorID.toString(), treatmentFactorData);
			} else {
				treatmentFactorData = currentData.get(levelFactorID.toString());
			}
			treatmentFactorData.setLabels(treatmentVariable.getValues());
			treatmentFactorData.setLevels(treatmentVariable.getValues().size());
			treatmentFactorPairs.put(levelFactorID.toString(), this.retrieveVariablePairs(levelFactorID));

		}

		TabInfo info = new TabInfo();
		TreatmentFactorTabBean tabBean = new TreatmentFactorTabBean();
		tabBean.setCurrentData(currentData);
		info.setData(tabBean);

		List<SettingDetail> detailList = new ArrayList<SettingDetail>(levelDetails.values());
		Map<String, Object> treatmentFactorSettings = new HashMap<String, Object>();
		treatmentFactorSettings.put("details", detailList);
		treatmentFactorSettings.put("treatmentLevelPairs", treatmentFactorPairs);

		this.userSelection.setTreatmentFactors(detailList);
		info.setSettingMap(treatmentFactorSettings);

		return info;
	}

	protected TabInfo prepareMeasurementsTabInfo(List<MeasurementVariable> variatesList, boolean isUsePrevious)
			throws MiddlewareException {

		List<SettingDetail> detailList = new ArrayList<SettingDetail>();

		for (MeasurementVariable var : variatesList) {
			SettingDetail detail = this.createSettingDetail(var.getTermId(), var.getName(), VariableType.TRAIT.getRole().name());

			if (!isUsePrevious) {
				detail.getVariable().setOperation(Operation.UPDATE);
			} else {
				detail.getVariable().setOperation(Operation.ADD);
			}

			detail.setDeletable(true);

			detailList.add(detail);
		}

		TabInfo info = new TabInfo();
		info.setSettings(detailList);

		this.userSelection.setBaselineTraitsList(detailList);

		return info;
	}

	protected TabInfo prepareEnvironmentsTabInfo(Workbook workbook, boolean isUsePrevious) throws MiddlewareException {
		TabInfo info = new TabInfo();
		Map settingMap = new HashMap();
		List<SettingDetail> managementDetailList = new ArrayList<SettingDetail>();
		List<SettingDetail> trialConditionsList = new ArrayList<SettingDetail>();
		List<Integer> hiddenFields =
				this.buildVariableIDList(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString() + ","
						+ AppConstants.HIDE_TRIAL_VARIABLE_DBCV_FIELDS.getString());
		List<Integer> requiredFields = this.buildVariableIDList(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString());
		List<Integer> filterFields = this.buildVariableIDList(AppConstants.EXP_DESIGN_VARIABLES.getString());
		Map<String, MeasurementVariable> factorsMap = SettingsUtil.buildMeasurementVariableMap(workbook.getTrialConditions());
		for (MeasurementVariable var : workbook.getTrialConditions()) {
			SettingDetail detail = this.createSettingDetail(var.getTermId(), var.getName(), VariableType.ENVIRONMENT_DETAIL.getRole().name());

			if (filterFields.contains(var.getTermId())) {
				continue;
			}

			if (hiddenFields.contains(var.getTermId())) {
				detail.setHidden(true);
			}

			if (!requiredFields.contains(var.getTermId())) {
				detail.setDeletable(true);
			}

			managementDetailList.add(detail);

			if (!isUsePrevious) {
				detail.getVariable().setOperation(Operation.UPDATE);
			} else {
				detail.getVariable().setOperation(Operation.ADD);
			}

			// set local name of id variable to local name of name variable
			String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
			if (factorsMap.get(nameTermId) != null) {
				detail.getVariable().setName(factorsMap.get(nameTermId).getName());
			}
		}

		for (MeasurementVariable var : workbook.getTrialConstants()) {
			SettingDetail detail = this.createSettingDetail(var.getTermId(), var.getName(), VariableType.TRIAL_CONDITION.getRole().name());

			if (!isUsePrevious) {
				detail.getVariable().setOperation(Operation.UPDATE);
			} else {
				detail.getVariable().setOperation(Operation.ADD);
			}

			detail.setDeletable(true);
			trialConditionsList.add(detail);
		}

		settingMap.put("managementDetails", managementDetailList);
		settingMap.put("trialConditionDetails", trialConditionsList);

		info.setSettingMap(settingMap);

		EnvironmentData data = new EnvironmentData();
		List<MeasurementRow> trialObservations = workbook.getTrialObservations();

		data.setNoOfEnvironments(trialObservations.size());

		// minimum number of environments is 1
		if (data.getNoOfEnvironments() == 0) {
			data.setNoOfEnvironments(1);
		}

		List<Environment> environments = new ArrayList<Environment>();
		for (MeasurementRow row : trialObservations) {
			Environment environment = new Environment();
			if (!isUsePrevious) {
				environment.setExperimentId(row.getExperimentId());
				environment.setLocationId(row.getLocationId());
				environment.setStockId(row.getStockId());
			}

			Map<String, String> managementDetailValues = new HashMap<String, String>();
			for (SettingDetail detail : managementDetailList) {

				MeasurementData mData = row.getMeasurementData(detail.getVariable().getCvTermId());
				if (mData != null) {
					String value;
					if ("DATE".equals(detail.getVariable().getWidgetType().getType())) {
						value = this.convertDateStringForUI(mData.getValue());
					} else if (mData.getcValueId() != null) {
						value = mData.getcValueId();
					} else {

						value = mData.getValue();

					}
					managementDetailValues.put(Integer.toString(mData.getMeasurementVariable().getTermId()), value);
				}
			}

			Map<String, String> trialConditionValues = new HashMap<String, String>();
			Map<String, Integer> phenotypeIDMap = new HashMap<String, Integer>();
			for (SettingDetail detail : trialConditionsList) {

				MeasurementData mData = row.getMeasurementData(detail.getVariable().getCvTermId());
				if (mData != null) {
					String value;
					if ("DATE".equals(detail.getVariable().getWidgetType().getType())) {
						value = this.convertDateStringForUI(mData.getValue());
					} else {
						value = mData.getValue();
					}

					if (!isUsePrevious) {
						phenotypeIDMap.put(Integer.toString(mData.getMeasurementVariable().getTermId()), mData.getPhenotypeId());
					}

					trialConditionValues.put(Integer.toString(mData.getMeasurementVariable().getTermId()), value);
				}
			}

			environment.setManagementDetailValues(managementDetailValues);
			environment.setTrialDetailValues(trialConditionValues);
			environment.setPhenotypeIDMap(phenotypeIDMap);
			environments.add(environment);
		}

		data.setEnvironments(environments);
		info.setData(data);

		this.userSelection.setTrialLevelVariableList(managementDetailList);

		this.userSelection.setNurseryConditions(trialConditionsList);

		return info;
	}

	public List<SettingDetail> retrieveVariablePairs(int cvTermId) {
		List<SettingDetail> output = new ArrayList<SettingDetail>();

		try {

			StandardVariable variable = this.ontologyService.getStandardVariable(cvTermId,
					contextUtil.getCurrentProgramUUID());

			List<StandardVariable> pairs =
					this.fieldbookMiddlewareService.getPossibleTreatmentPairs(variable.getId(), variable.getProperty().getId(),
							AppConstants.CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());

			for (StandardVariable item : pairs) {
				output.add(this.createSettingDetail(item.getId(), null, VariableType.TREATMENT_FACTOR.getRole().name()));
			}

		} catch (MiddlewareException e) {
			BaseTrialController.LOG.error(e.getMessage(), e);
		}

		return output;
	}

	protected TabInfo prepareBasicDetailsTabInfo(StudyDetails studyDetails, boolean isUsePrevious, int trialID)
			throws MiddlewareException {
		Map<String, String> basicDetails = new HashMap<String, String>();
		List<SettingDetail> initialDetailList = new ArrayList<SettingDetail>();
		List<Integer> initialSettingIDs = this.buildVariableIDList(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());

		for (Integer initialSettingID : initialSettingIDs) {
			try {
				basicDetails.put(initialSettingID.toString(), "");
				SettingDetail detail = this.createSettingDetail(initialSettingID, null, VariableType.STUDY_DETAIL.getRole().name());

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				initialDetailList.add(detail);
			} catch (MiddlewareQueryException e) {
				BaseTrialController.LOG.error(e.getMessage(), e);
			}
		}

		BasicDetails basic = new BasicDetails();

		basicDetails.put(Integer.toString(TermId.STUDY_NAME.getId()), studyDetails.getStudyName());
		basicDetails.put(Integer.toString(TermId.STUDY_TITLE.getId()), studyDetails.getTitle());
		basicDetails.put(Integer.toString(TermId.STUDY_OBJECTIVE.getId()), studyDetails.getObjective());
		basicDetails.put(Integer.toString(TermId.START_DATE.getId()), this.convertDateStringForUI(studyDetails.getStartDate()));
		basicDetails.put(Integer.toString(TermId.END_DATE.getId()), this.convertDateStringForUI(studyDetails.getEndDate()));
		basic.setBasicDetails(basicDetails);
		basic.setStudyID(trialID);

		int folderId = (int) studyDetails.getParentFolderId();
		String folderName;

		if (folderId == 1) {
			folderName = AppConstants.TRIALS.getString();
		} else {
			folderName = this.fieldbookMiddlewareService.getFolderNameById(folderId);
		}

		basic.setFolderId(folderId);
		basic.setFolderName(folderName);
		basic.setFolderNameLabel(folderName);
		basic.setUserID(this.getCurrentIbdbUserId());
		basic.setUserName(this.fieldbookService.getPersonByUserId(basic.getUserID()));

		TabInfo tab = new TabInfo();
		tab.setData(basic);

		this.userSelection.setBasicDetails(initialDetailList);

		return tab;
	}

	protected String convertDateStringForUI(String value) {
		if (!value.contains("-")) {
			return DateUtil.convertToUIDateFormat(TermId.DATE_VARIABLE.getId(), value);
		} else {
			return value;
		}

	}

	protected TabInfo prepareTrialSettingsTabInfo(List<MeasurementVariable> measurementVariables, boolean isUsePrevious)
			throws MiddlewareException {
		TabInfo info = new TabInfo();
		Map<String, String> trialValues = new HashMap<String, String>();
		List<SettingDetail> details = new ArrayList<SettingDetail>();

		List<Integer> hiddenFields = this.buildVariableIDList(AppConstants.HIDE_TRIAL_VARIABLE_DBCV_FIELDS.getString());
		List<Integer> basicDetailIDList = this.buildVariableIDList(AppConstants.HIDE_TRIAL_FIELDS.getString());
		Map<String, MeasurementVariable> settingsMap = SettingsUtil.buildMeasurementVariableMap(measurementVariables);
		for (MeasurementVariable var : measurementVariables) {
			if (!basicDetailIDList.contains(var.getTermId())) {
				SettingDetail detail = this.createSettingDetail(var.getTermId(), var.getName(), VariableType.STUDY_DETAIL.getRole().name());
				detail.setDeletable(true);
				details.add(detail);

				if (hiddenFields.contains(var.getTermId())) {
					detail.setHidden(true);
				} else {
					detail.setHidden(false);
				}

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				// set local name of id variable to local name of name variable
				String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
				if (settingsMap.get(nameTermId) != null) {
					detail.getVariable().setName(settingsMap.get(nameTermId).getName());
				}

				String value;
				if ("DATE".equals(detail.getVariable().getWidgetType().getType())) {
					value = this.convertDateStringForUI(var.getValue());
				} else {
					value = var.getValue();
				}
				trialValues.put(Integer.toString(var.getTermId()), value);
			}
		}

		this.userSelection.setStudyLevelConditions(details);

		info.setSettings(details);
		TrialSettingsBean trialSettingsBean = new TrialSettingsBean();
		trialSettingsBean.setUserInput(trialValues);
		info.setData(trialSettingsBean);
		return info;
	}

	protected TabInfo prepareExperimentalDesignSpecialData() throws MiddlewareException {
		TabInfo info = new TabInfo();
		ExpDesignData data = new ExpDesignData();
		List<ExpDesignDataDetail> detailList = new ArrayList<ExpDesignDataDetail>();

		List<Integer> ids = this.buildVariableIDList(AppConstants.CREATE_TRIAL_EXP_DESIGN_DEFAULT_FIELDS.getString());
		for (Integer id : ids) {
			// PLOT, REP, BLOCK, ENTRY NO
			StandardVariable stdvar = this.fieldbookMiddlewareService.getStandardVariable(id,
					contextUtil.getCurrentProgramUUID());
			SettingVariable svar = new SettingVariable();
			svar.setCvTermId(id);
			svar.setName(stdvar.getName());
			ExpDesignDataDetail dataDetail = new ExpDesignDataDetail(AppConstants.getString(id + AppConstants.LABEL.getString()), svar);
			detailList.add(dataDetail);

		}
		data.setExpDesignDetailList(detailList);
		info.setData(data);

		return info;
	}
}
