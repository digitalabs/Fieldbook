
package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.trial.bean.AdvanceList;
import com.efficio.fieldbook.web.trial.bean.BasicDetails;
import com.efficio.fieldbook.web.trial.bean.CrossesList;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.trial.bean.ExpDesignData;
import com.efficio.fieldbook.web.trial.bean.ExpDesignDataDetail;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.TabInfo;
import com.efficio.fieldbook.web.trial.bean.TreatmentFactorData;
import com.efficio.fieldbook.web.trial.bean.TreatmentFactorTabBean;
import com.efficio.fieldbook.web.trial.bean.TrialSettingsBean;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.DesignTypeItem;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.operation.builder.StudyTypeBuilder;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public abstract class BaseTrialController extends SettingsController {

	@Resource
	protected SampleService sampleService;

	@Resource
	protected StudyDataManager studyDataManager;

	private static final Logger LOG = LoggerFactory.getLogger(BaseTrialController.class);

	public static final String URL_SETTINGS = "TrialManager/templates/trialSettings";
	public static final String URL_GERMPLASM = "TrialManager/templates/germplasmDetails";
	public static final String URL_ENVIRONMENTS = "TrialManager/templates/environments";
	public static final String URL_TREATMENT = "TrialManager/templates/treatment";
	public static final String URL_EXPERIMENTAL_DESIGN = "TrialManager/templates/experimentalDesign";
	public static final String URL_MEASUREMENT = "TrialManager/templates/measurements";
	public static final String URL_DATATABLE = "Common/showAddOrRemoveTraitsPagination";

	protected StudyTypeBuilder studyTypeBuilder = new StudyTypeBuilder();

	protected void createStudyDetails(final Workbook workbook, final BasicDetails detailBean) {
		if (workbook.getStudyDetails() == null) {
			workbook.setStudyDetails(new StudyDetails());
		}

		final StudyDetails studyDetails = workbook.getStudyDetails();

		studyDetails.setId(detailBean.getStudyID());
		studyDetails.setDescription(detailBean.getDescription());
		studyDetails.setObjective(detailBean.getObjective());
		studyDetails.setStudyName(detailBean.getStudyName());
		studyDetails.setStartDate(detailBean.getStartDate());
		studyDetails.setEndDate(detailBean.getEndDate());
		studyDetails.setStudyUpdate(Util.getCurrentDateAsStringValue(Util.DATE_AS_NUMBER_FORMAT));
		studyDetails.setStudyType(studyDataManager.getStudyTypeByName(detailBean.getStudyType().getName()));

		if (detailBean.getCreatedBy() != null) {
			studyDetails.setCreatedBy(detailBean.getCreatedBy());
		}

		if (detailBean.getFolderId() != null) {
			studyDetails.setParentFolderId(detailBean.getFolderId());
		}

		studyDetails.print(1);
	}

	protected void processEnvironmentData(final EnvironmentData data) {
		for (int i = 0; i < data.getEnvironments().size(); i++) {
			final Map<String, String> values = data.getEnvironments().get(i).getManagementDetailValues();
			if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))
					|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null
					|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			}
		}
	}

	protected void populateSettingData(final List<SettingDetail> details, final Map<String, String> values) {
		if (details == null || details.isEmpty()) {
			return;
		}

		for (final SettingDetail detail : details) {
			if (values.containsKey(detail.getVariable().getCvTermId().toString())) {
				detail.setValue(values.get(detail.getVariable().getCvTermId().toString()));
			}
		}
	}

	protected List<List<ValueReference>> convertToValueReference(final List<Environment> environments) {
		final List<List<ValueReference>> returnVal = new ArrayList<>(environments.size());

		for (final Environment environment : environments) {
			final List<ValueReference> valueRefList = new ArrayList<>();

			for (final Map.Entry<String, String> entry : environment.getManagementDetailValues().entrySet()) {
				final ValueReference valueRef = new ValueReference(entry.getKey(), entry.getValue());
				valueRefList.add(valueRef);
			}

			returnVal.add(valueRefList);
		}

		return returnVal;
	}

	protected TabInfo prepareExperimentalDesignTabInfo(final Workbook trialWorkbook, final boolean isUsePrevious) {
		final TabInfo tabInfo = new TabInfo();
		final ExperimentalDesignVariable xpDesignVariable = trialWorkbook.getExperimentalDesignVariables();
		// currently, the saved experimental design information is not loaded up
		// when choosing a previous trial as template
		if (!isUsePrevious && xpDesignVariable != null) {
			final ExpDesignParameterUi data = new ExpDesignParameterUi();

			// default values
			data.setDesignType(null);
			data.setUseLatenized(false);

			data.setBlockSize(this.getExperimentalDesignData(xpDesignVariable.getBlockSize()));

			// set cols per replication
			data.setColsPerReplications(this.getExperimentalDesignData(xpDesignVariable.getNumberOfColsInReps()));
			data.setRowsPerReplications(this.getExperimentalDesignData(xpDesignVariable.getNumberOfRowsInReps()));

			data.setNclatin(this.getExperimentalDesignData(xpDesignVariable.getNumberOfContiguousColsLatinize()));
			data.setNblatin(this.getExperimentalDesignData(xpDesignVariable.getNumberOfContiguousBlocksLatinize()));
			data.setNrlatin(this.getExperimentalDesignData(xpDesignVariable.getNumberOfContiguousRowsLatinize()));

			data.setReplatinGroups(this.getExperimentalDesignData(xpDesignVariable.getNumberOfRepsInCols()));
			final String replicationsMap = this.getExperimentalDesignData(xpDesignVariable.getReplicationsMap());

			if (replicationsMap != null && NumberUtils.isNumber(replicationsMap)) {
				final Integer repArrangementID = Integer.parseInt(replicationsMap);
				if (TermId.REPS_IN_SINGLE_COL.getId() == repArrangementID) {
					data.setReplicationsArrangement(1);
				} else if (TermId.REPS_IN_SINGLE_ROW.getId() == repArrangementID) {
					data.setReplicationsArrangement(2);
				} else if (TermId.REPS_IN_ADJACENT_COLS.getId() == repArrangementID) {
					data.setReplicationsArrangement(3);
				}
			}

			data.setReplicationsCount(this.getExperimentalDesignData(xpDesignVariable.getNumberOfReplicates()));

			data.setFileName(this.getExperimentalDesignData(xpDesignVariable.getExperimentalDesignSource()));

			// FIXME
			// Get starting entry and plot without loading all observations in OpenTrialController.openTrial()
			this.setStartingEntryNoAndPlotNoFromObservations(trialWorkbook, data);

			// Get all entry numbers from workbook, sort it and get first element from entry numbers list
			final List<Integer> entryNumberList = new ArrayList<>();
			for (final MeasurementRow measurementRow : trialWorkbook.getObservations()) {
				final MeasurementData measurementData = measurementRow.getDataList().get(4);
				if (Objects.equals(measurementData.getLabel(), TermId.ENTRY_NO.toString())) {
					entryNumberList.add(Integer.parseInt(measurementData.getValue()));
				}
			}

			if (!entryNumberList.isEmpty()) {
				Collections.sort(entryNumberList);
				data.setStartingEntryNo(String.valueOf(entryNumberList.get(0)));
			}

			final String designTypeString =
					xpDesignVariable.getExperimentalDesign() == null ? null : xpDesignVariable.getExperimentalDesign().getValue();
			if (designTypeString != null && NumberUtils.isNumber(designTypeString)) {
				final Integer designTypeTermID = Integer.parseInt(designTypeString);

				if (TermId.RANDOMIZED_COMPLETE_BLOCK.getId() == designTypeTermID) {
					data.setDesignType(DesignTypeItem.RANDOMIZED_COMPLETE_BLOCK.getId());
					data.setUseLatenized(false);
				} else if (TermId.RESOLVABLE_INCOMPLETE_BLOCK_LATIN.getId() == designTypeTermID) {
					data.setDesignType(DesignTypeItem.RESOLVABLE_INCOMPLETE_BLOCK.getId());
					data.setUseLatenized(true);
				} else if (TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId() == designTypeTermID) {
					data.setDesignType(DesignTypeItem.RESOLVABLE_INCOMPLETE_BLOCK.getId());
					data.setUseLatenized(false);
				} else if (TermId.RESOLVABLE_INCOMPLETE_ROW_COL_LATIN.getId() == designTypeTermID) {
					data.setDesignType(DesignTypeItem.ROW_COL.getId());
					data.setUseLatenized(true);
				} else if (TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId() == designTypeTermID) {
					data.setDesignType(DesignTypeItem.ROW_COL.getId());
					data.setUseLatenized(false);
				} else if (TermId.OTHER_DESIGN.getId() == designTypeTermID) {
					data.setDesignType(DesignTypeItem.CUSTOM_IMPORT.getId());
					data.setUseLatenized(false);
				} else if (TermId.AUGMENTED_RANDOMIZED_BLOCK.getId() == designTypeTermID) {
					data.setDesignType(DesignTypeItem.AUGMENTED_RANDOMIZED_BLOCK.getId());
					data.setUseLatenized(false);
				} else if (TermId.ENTRY_LIST_ORDER.getId() == designTypeTermID) {
					data.setDesignType(DesignTypeItem.ENTRY_LIST_ORDER.getId());
					data.setUseLatenized(false);
				}
			}

			data.setNumberOfBlocks(this.getExperimentalDesignData(xpDesignVariable.getNumberOfBlocks()));
			data.setCheckInsertionManner(this.getExperimentalDesignData(xpDesignVariable.getChecksMannerOfInsertion()));
			data.setCheckSpacing(this.getExperimentalDesignData(xpDesignVariable.getChecksSpacing()));
			data.setCheckStartingPosition(this.getExperimentalDesignData(xpDesignVariable.getChecksStartingPosition()));

			tabInfo.setData(data);
		}

		return tabInfo;
	}

	private void setStartingEntryNoAndPlotNoFromObservations(final Workbook trialWorkbook, final ExpDesignParameterUi data) {
		// Set starting entry and plot number from observations
		Integer startingEntryNo = 0;
		Integer startingPlotNo = 0;
		if (trialWorkbook.getObservations() != null && !trialWorkbook.getObservations().isEmpty()) {

			final List<MeasurementRow> measurementRows = trialWorkbook.getObservations();
			for (final MeasurementRow measurementRow : measurementRows) {

				final Map<Integer, MeasurementData> dataMap =
						Maps.uniqueIndex(measurementRow.getDataList(), new Function<MeasurementData, Integer>() {

							@Override
							public Integer apply(final MeasurementData from) {
								return from.getMeasurementVariable().getTermId();
							}
						});

				final Integer currentEntryNo = Integer.valueOf(dataMap.get(TermId.ENTRY_NO.getId()).getValue());
				if (currentEntryNo < startingEntryNo || startingEntryNo == 0) {
					startingEntryNo = currentEntryNo;
				}
				final Integer currentPlotNo = Integer.valueOf(dataMap.get(TermId.PLOT_NO.getId()).getValue());
				if (currentPlotNo < startingPlotNo || startingPlotNo == 0) {
					startingPlotNo = currentPlotNo;
				}
			}
		} else {
			// set the default starting entry no
			startingEntryNo = 1;

			// set the default starting plot no
			startingPlotNo = 1;
		}
		data.setStartingEntryNo(startingEntryNo.toString());
		data.setStartingPlotNo(startingPlotNo.toString());
	}

	protected String getExperimentalDesignData(final MeasurementVariable var) {
		if (var != null) {
			return var.getValue();
		} else {
			return null;
		}
	}

	protected TabInfo prepareGermplasmTabInfo(final List<MeasurementVariable> measurementVariables, final boolean isUsePrevious) {
		final List<SettingDetail> detailList = new ArrayList<>();
		final List<Integer> requiredIDList = this.buildVariableIDList(AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString());

		for (final MeasurementVariable var : measurementVariables) {
			// this condition is required so that treatment factors are not
			// included in the list of factors for the germplasm tab
			if (var.getTreatmentLabel() != null && !var.getTreatmentLabel().isEmpty()
					|| this.inRequiredExpDesignVar(var.getTermId()) && isUsePrevious) {
				continue;
			}

			final SettingDetail detail =
					this.createSettingDetail(var.getTermId(), var.getName(), VariableType.GERMPLASM_DESCRIPTOR.getRole().name());

			if (var.getRole() != null) {
				detail.setRole(var.getRole());
				detail.getVariable().setRole(var.getRole().name());
			}

			if (requiredIDList.contains(var.getTermId())) {
				detail.setDeletable(false);
			} else {
				detail.setDeletable(true);
			}

			// set all variables with trial design role to hidden
			if (var.getRole() == PhenotypicType.TRIAL_DESIGN) {
				detail.setHidden(true);
				// BMS-1048
				if (var.getTermId() == TermId.COLUMN_NO.getId() || var.getTermId() == TermId.RANGE_NO.getId()) {
					detail.setDeletable(false);
					detail.setHidden(false);
				}
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

		final TabInfo info = new TabInfo();
		info.setSettings(detailList);

		this.userSelection.setPlotsLevelList(detailList);

		return info;
	}

	protected boolean inRequiredExpDesignVar(final int termId) {
		final StringTokenizer token = new StringTokenizer(AppConstants.EXP_DESIGN_REQUIRED_VARIABLES.getString(), ",");

		while (token.hasMoreTokens()) {
			if (Integer.parseInt(token.nextToken()) == termId) {
				return true;
			}
		}
		return false;
	}

	protected TabInfo prepareTreatmentFactorsInfo(final List<TreatmentVariable> treatmentVariables, final boolean isUsePrevious) {
		final Map<Integer, SettingDetail> levelDetails = new HashMap<>();
		final Map<String, TreatmentFactorData> currentData = new HashMap<>();
		final Map<String, List<SettingDetail>> treatmentFactorPairs = new HashMap<>();

		for (final TreatmentVariable treatmentVariable : treatmentVariables) {
			final Integer levelFactorID = treatmentVariable.getLevelVariable().getTermId();
			if (!levelDetails.containsKey(levelFactorID)) {
				final SettingDetail detail = this.createSettingDetail(levelFactorID, treatmentVariable.getLevelVariable().getName(),
						VariableType.TREATMENT_FACTOR.getRole().name());

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				levelDetails.put(levelFactorID, detail);
			}

			final TreatmentFactorData treatmentFactorData;
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

		final TabInfo info = new TabInfo();
		final TreatmentFactorTabBean tabBean = new TreatmentFactorTabBean();
		tabBean.setCurrentData(currentData);
		info.setData(tabBean);

		final List<SettingDetail> detailList = new ArrayList<>(levelDetails.values());
		final Map<String, Object> treatmentFactorSettings = new HashMap<>();
		treatmentFactorSettings.put("details", detailList);
		treatmentFactorSettings.put("treatmentLevelPairs", treatmentFactorPairs);

		this.userSelection.setTreatmentFactors(detailList);
		info.setSettingMap(treatmentFactorSettings);

		return info;
	}

	protected TabInfo prepareMeasurementVariableTabInfo(final List<MeasurementVariable> variatesList, final VariableType variableType,
			final boolean isUsePrevious) {

		final List<SettingDetail> detailList = new ArrayList<>();

		for (final MeasurementVariable var : variatesList) {
			if (var.getVariableType() == variableType) {

				final SettingDetail detail = this.createSettingDetailWithVariableType(var.getTermId(), var.getName(), variableType);

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				detail.setDeletable(true);

				detailList.add(detail);
			}
		}

		if (variableType == VariableType.TRAIT) {
			this.userSelection.setBaselineTraitsList(detailList);
		} else if (variableType == VariableType.SELECTION_METHOD) {
			this.userSelection.setSelectionVariates(detailList);
		}

		final TabInfo info = new TabInfo();
		info.setSettings(detailList);

		return info;
	}

	protected TabInfo prepareEnvironmentsTabInfo(final Workbook workbook, final boolean isUsePrevious) {
		final TabInfo info = new TabInfo();
		final Map<String, Object> settingMap = new HashMap<>();
		final List<SettingDetail> managementDetailList = new ArrayList<>();
		final List<SettingDetail> trialConditionsList = new ArrayList<>();
		final List<Integer> hiddenFields = this.buildVariableIDList(
				AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString() + "," + AppConstants.HIDE_TRIAL_VARIABLE_DBCV_FIELDS.getString());
		final List<Integer> requiredFields = this.buildVariableIDList(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString());
		final List<Integer> filterFields = this.buildVariableIDList(AppConstants.EXP_DESIGN_VARIABLES.getString());
		final Map<String, MeasurementVariable> factorsMap = SettingsUtil.buildMeasurementVariableMap(workbook.getTrialConditions());
		for (final MeasurementVariable var : workbook.getTrialConditions()) {
			final SettingDetail detail =
					this.createSettingDetail(var.getTermId(), var.getName(), VariableType.ENVIRONMENT_DETAIL.getRole().name());

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
			final String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
			if (factorsMap.get(nameTermId) != null) {
				detail.getVariable().setName(factorsMap.get(nameTermId).getName());
			}
		}

		for (final MeasurementVariable var : workbook.getTrialConstants()) {
			final SettingDetail detail =
					this.createSettingDetail(var.getTermId(), var.getName(), VariableType.TRIAL_CONDITION.getRole().name());

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

		final EnvironmentData data = new EnvironmentData();
		final List<MeasurementRow> trialObservations = workbook.getTrialObservations();

		data.setNoOfEnvironments(trialObservations.size());

		// minimum number of environments is 1
		if (data.getNoOfEnvironments() == 0) {
			data.setNoOfEnvironments(1);
		}

		final List<Environment> environments = new ArrayList<>();
		for (final MeasurementRow row : trialObservations) {
			final Environment environment = new Environment();
			if (!isUsePrevious) {
				environment.setExperimentId(row.getExperimentId());
				environment.setLocationId(row.getLocationId());
				environment.setStockId(row.getStockId());
			}

			final Map<String, String> managementDetailValues = new HashMap<>();
			for (final SettingDetail detail : managementDetailList) {

				final MeasurementData mData = row.getMeasurementData(detail.getVariable().getCvTermId());
				if (mData != null) {
					final String value;
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

			final Map<String, String> trialConditionValues = new HashMap<>();
			final Map<String, Integer> phenotypeIDMap = new HashMap<>();
			for (final SettingDetail detail : trialConditionsList) {

				final MeasurementData mData = row.getMeasurementData(detail.getVariable().getCvTermId());
				if (mData != null) {
					final String value;
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

	protected List<AdvanceList> getAdvancedList(final Integer trialId) {
		final List<GermplasmList> germplasmList =
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(trialId, GermplasmListType.ADVANCED);
		final List<AdvanceList> advanceList = new ArrayList<>();

		for (final GermplasmList g : germplasmList) {
			advanceList.add(new AdvanceList(g.getId(), g.getName()));
		}

		return advanceList;
	}

	protected List<CrossesList> getCrossesList(final Integer trialId) {
		final List<GermplasmList> crossList =
			this.fieldbookMiddlewareService.getGermplasmListsByProjectId(trialId, GermplasmListType.CROSSES);

		crossList.addAll(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(trialId, GermplasmListType.IMP_CROSS));
		crossList.addAll(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(trialId, GermplasmListType.CRT_CROSS));

		final List<CrossesList> crossesList = new ArrayList<>();

		for (final GermplasmList g : crossList) {
			crossesList.add(new CrossesList(g.getId(), g.getName(),g.getType().equalsIgnoreCase(GermplasmListType.IMP_CROSS.toString())?GermplasmList.IMP_CROSS:GermplasmList.CRT_CROSS));
		}

		return crossesList;
	}

	public List<SettingDetail> retrieveVariablePairs(final int cvTermId) {
		final List<SettingDetail> output = new ArrayList<>();

		try {

			final StandardVariable variable = this.ontologyService.getStandardVariable(cvTermId, this.contextUtil.getCurrentProgramUUID());

			final List<StandardVariable> pairs = this.fieldbookMiddlewareService.getPossibleTreatmentPairs(variable.getId(),
					variable.getProperty().getId(), AppConstants.CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());

			for (final StandardVariable item : pairs) {
				output.add(this.createSettingDetail(item.getId(), null, VariableType.TREATMENT_FACTOR.getRole().name()));
			}

		} catch (final MiddlewareException e) {
			BaseTrialController.LOG.error(e.getMessage(), e);
		}

		return output;
	}

	protected List<MeasurementVariable> getLatestMeasurements(@ModelAttribute("createTrialForm") final CreateTrialForm form,
			final HttpServletRequest request) {
		Workbook workbook = this.userSelection.getWorkbook();
		if (this.userSelection.getTemporaryWorkbook() != null) {
			workbook = this.userSelection.getTemporaryWorkbook();
		}

		boolean hasSamples = false;
		if (this.userSelection.getWorkbook() != null) {
			hasSamples = this.sampleService.studyHasSamples(this.userSelection.getWorkbook().getStudyDetails().getId());
		}

		List<MeasurementVariable> measurementDatasetVariables = new ArrayList<>();
		measurementDatasetVariables.addAll(workbook.getMeasurementDatasetVariablesView());

		final String listCsv = request.getParameter("variableList");

		if (!measurementDatasetVariables.isEmpty()) {
			final List<MeasurementVariable> newMeasurementDatasetVariables = this.getMeasurementVariableFactor(measurementDatasetVariables);
			if (hasSamples) {
				final MeasurementVariable sample = this.createSampleVariable();
				newMeasurementDatasetVariables.add(sample);
			}
			this.getTraitsAndSelectionVariates(measurementDatasetVariables, newMeasurementDatasetVariables, listCsv);
			measurementDatasetVariables = newMeasurementDatasetVariables;
		}

		FieldbookUtil.setColumnOrderingOnWorkbook(workbook, form.getColumnOrders());
		return workbook.arrangeMeasurementVariables(measurementDatasetVariables);
	}

	private MeasurementVariable createSampleVariable() {
		final MeasurementVariable sample = new MeasurementVariable();
		sample.setName("SAMPLES");
		sample.setTermId(TermId.SAMPLES.getId());
		sample.setFactor(true);

		return sample;
	}

	protected TabInfo prepareBasicDetailsTabInfo(final StudyDetails studyDetails, final boolean isUsePrevious, final int trialID)
		throws ParseException {
		final Map<String, String> basicDetails = new HashMap<>();
		final List<SettingDetail> initialDetailList = new ArrayList<>();

		// find out who created the study
		// if no owner found default to the current user
		final Integer studyOwnerPersonId = this.contextUtil.getCurrentIbdbUserId();

		final String studyOwnerPersonName = this.fieldbookService.getPersonByUserId(studyOwnerPersonId);

		final List<Integer> initialSettingIDs = this.buildVariableIDList(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());

		for (final Integer initialSettingID : initialSettingIDs) {
			try {
				basicDetails.put(initialSettingID.toString(), "");
				final SettingDetail detail = this.createSettingDetail(initialSettingID, null, VariableType.STUDY_DETAIL.getRole().name());

				if (!isUsePrevious) {
					detail.getVariable().setOperation(Operation.UPDATE);
				} else {
					detail.getVariable().setOperation(Operation.ADD);
				}

				initialDetailList.add(detail);
			} catch (final MiddlewareQueryException e) {
				BaseTrialController.LOG.error(e.getMessage(), e);
			}
		}

		final BasicDetails basic = new BasicDetails();

		basic.setBasicDetails(basicDetails);
		basic.setStudyName(studyDetails.getStudyName());
		basic.setStudyID(trialID);
		basic.setDescription(studyDetails.getDescription());
		basic.setStartDate(Util.convertDate(studyDetails.getStartDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT));
		basic.setEndDate(studyDetails.getEndDate() != null && !studyDetails.getEndDate().isEmpty() ?
			Util.convertDate(studyDetails.getEndDate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT) :
			StringUtils.EMPTY);
		basic.setStudyUpdate(studyDetails.getStudyUpdate() != null && !studyDetails.getStudyUpdate().isEmpty() ?
			Util.convertDate(studyDetails.getStudyUpdate(), Util.DATE_AS_NUMBER_FORMAT, Util.FRONTEND_DATE_FORMAT)
			: StringUtils.EMPTY);
		basic.setObjective(studyDetails.getObjective());

		final int folderId = (int) studyDetails.getParentFolderId();
		final String folderName;

		if (folderId == 1) {
			folderName = AppConstants.TRIALS.getString();
		} else {
			folderName = this.fieldbookMiddlewareService.getFolderNameById(folderId);
		}

		basic.setFolderId(folderId);
		basic.setFolderName(folderName);
		basic.setFolderNameLabel(folderName);
		basic.setUserID(studyOwnerPersonId);
		basic.setUserName(studyOwnerPersonName);
		basic.setStudyType(studyDetails.getStudyType());
		final TabInfo tab = new TabInfo();
		tab.setData(basic);

		setUserSelectionBasicInformation(studyDetails, initialDetailList);
		return tab;
	}

	protected void setUserSelectionBasicInformation(final StudyDetails studyDetails, final List<SettingDetail> initialDetailList) {
		this.userSelection.setBasicDetails(initialDetailList);
		this.userSelection.setStudyName(studyDetails.getStudyName());
		this.userSelection.setStudyDescription(studyDetails.getDescription());
		this.userSelection.setStudyStartDate(studyDetails.getStartDate());
		this.userSelection.setStudyEndDate(studyDetails.getEndDate());
		this.userSelection.setStudyUpdate(studyDetails.getStudyUpdate());
		this.userSelection.setStudyObjective(studyDetails.getObjective());
		this.userSelection.setStudyType(studyDetails.getStudyType().getName());
	}

	protected String convertDateStringForUI(final String value) {
		if (value != null && !value.contains("-")) {
			return DateUtil.convertToUIDateFormat(TermId.DATE_VARIABLE.getId(), value);
		} else {
			return value;
		}

	}

	protected TabInfo prepareTrialSettingsTabInfo(final List<MeasurementVariable> measurementVariables, final boolean isUsePrevious) {
		final TabInfo info = new TabInfo();
		final Map<String, String> trialValues = new HashMap<>();
		final List<SettingDetail> details = new ArrayList<>();

		final List<Integer> hiddenFields = this.buildVariableIDList(AppConstants.HIDE_TRIAL_VARIABLE_DBCV_FIELDS.getString());
		final List<Integer> basicDetailIDList = this.buildVariableIDList(AppConstants.HIDE_TRIAL_FIELDS.getString());
		final Map<String, MeasurementVariable> settingsMap = SettingsUtil.buildMeasurementVariableMap(measurementVariables);
		for (final MeasurementVariable var : measurementVariables) {
			if (!basicDetailIDList.contains(var.getTermId())) {
				final SettingDetail detail =
						this.createSettingDetail(var.getTermId(), var.getName(), VariableType.STUDY_DETAIL.getRole().name());
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
				final String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
				if (settingsMap.get(nameTermId) != null) {
					detail.getVariable().setName(settingsMap.get(nameTermId).getName());
				}

				final String value;
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
		final TrialSettingsBean trialSettingsBean = new TrialSettingsBean();
		trialSettingsBean.setUserInput(trialValues);
		info.setData(trialSettingsBean);
		return info;
	}

	protected TabInfo prepareExperimentalDesignSpecialData() {
		final TabInfo info = new TabInfo();
		final ExpDesignData data = new ExpDesignData();
		final List<ExpDesignDataDetail> detailList = new ArrayList<>();

		final List<Integer> ids = this.buildVariableIDList(AppConstants.CREATE_TRIAL_EXP_DESIGN_DEFAULT_FIELDS.getString());
		for (final Integer id : ids) {
			// PLOT, REP, BLOCK, ENTRY NO
			final StandardVariable stdvar =
					this.fieldbookMiddlewareService.getStandardVariable(id, this.contextUtil.getCurrentProgramUUID());
			final SettingVariable svar = new SettingVariable();
			svar.setCvTermId(id);
			svar.setName(stdvar.getName());
			final ExpDesignDataDetail dataDetail =
					new ExpDesignDataDetail(AppConstants.getString(id + AppConstants.LABEL.getString()), svar);
			detailList.add(dataDetail);

		}
		data.setExpDesignDetailList(detailList);
		info.setData(data);

		return info;
	}

	protected void addMeasurementVariablesToTrialObservationIfNecessary(final EnvironmentData environmentData, final Workbook workbook,
			final List<MeasurementRow> trialObservations) {

		if (trialObservations == null) {
			return;
		}

		int x = 0;
		for (final MeasurementRow row : trialObservations) {

			final Map<String, String> trialDetailValues = environmentData.getEnvironments().get(x).getTrialDetailValues();
			final Map<String, String> managementDetailValues = environmentData.getEnvironments().get(x).getManagementDetailValues();

			for (final MeasurementVariable measurementVariable : workbook.getTrialVariables()) {
				final MeasurementData data = row.getMeasurementData(measurementVariable.getTermId());
				if (data == null) {

					String val = "";
					final String trialDetailValue = trialDetailValues.get(String.valueOf(measurementVariable.getTermId()));
					final String managementDetailValue = managementDetailValues.get(String.valueOf(measurementVariable.getTermId()));

					if (trialDetailValue != null) {
						val = trialDetailValue;
					} else if (managementDetailValue != null) {
						val = managementDetailValue;
					}

					final MeasurementData newData = new MeasurementData(measurementVariable.getName(), val, false,
							measurementVariable.getDataType(), measurementVariable);
					row.getDataList().add(newData);
				}

			}

			x++;
		}

	}

	protected List<MeasurementVariable> getMeasurementVariableFactor(final List<MeasurementVariable> measurementDatasetVariables) {
		final List<MeasurementVariable> newMeasurementDatasetVariables = new ArrayList<>();
		for (final MeasurementVariable var : measurementDatasetVariables) {
			if (var.isFactor()) {
				newMeasurementDatasetVariables.add(var);
			}
		}
		return newMeasurementDatasetVariables;
	}

	protected void getTraitsAndSelectionVariates(final List<MeasurementVariable> measurementDatasetVariables,
			final List<MeasurementVariable> newMeasurementDatasetVariables, final String listCsv) {

		if (listCsv != null && !"".equalsIgnoreCase(listCsv)) {
			// Create a map of traits and selection variates from user selection (UI) so their aliases can be retrieved
			final Map<Integer, SettingDetail> traitsMap = this.createMapOfTraitsAndSelectionVariatesFromUserSelection();
			final StringTokenizer token = new StringTokenizer(listCsv, ",");
			while (token.hasMoreTokens()) {
				final int id = Integer.parseInt(token.nextToken());
				final MeasurementVariable currentVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, id);
				if (currentVar == null) {
					final StandardVariable var =
							this.fieldbookMiddlewareService.getStandardVariable(id, this.contextUtil.getCurrentProgramUUID());
					var.setPhenotypicType(PhenotypicType.VARIATE);
					final MeasurementVariable newVar =
							ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, this.fieldbookService);
					newVar.setFactor(false);
					// Get trait's alias, if any, from user selection (UI) so that it will reflect on Measurements tab
					final SettingDetail traitFromSession = traitsMap.get(id);
					if (traitFromSession != null && traitFromSession.getVariable() != null) {
						newVar.setName(traitFromSession.getVariable().getName());
					}
					newMeasurementDatasetVariables.add(newVar);
				} else {
					newMeasurementDatasetVariables.add(currentVar);
				}
			}
		}
	}

	private ImmutableMap<Integer, SettingDetail> createMapOfTraitsAndSelectionVariatesFromUserSelection() {
		final Set<SettingDetail> variates = new HashSet<>();
		if (CollectionUtils.isNotEmpty(this.userSelection.getBaselineTraitsList())) {
			variates.addAll(this.userSelection.getBaselineTraitsList());
		}
		if (CollectionUtils.isNotEmpty(this.userSelection.getSelectionVariates())) {
			variates.addAll(this.userSelection.getSelectionVariates());
		}
		return Maps.uniqueIndex(variates, new Function<SettingDetail, Integer>() {

			@Override
			public Integer apply(final SettingDetail setting) {
				return setting.getVariable().getCvTermId();
			}
		});
	}
}
