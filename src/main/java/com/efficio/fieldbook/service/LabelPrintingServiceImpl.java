package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.util.labelprinting.LabelGeneratorFactory;
import com.efficio.fieldbook.util.labelprinting.comparators.FieldMapLabelComparator;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.pojos.labelprinting.LabelPrintingProcessingParams;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Class LabelPrintingServiceImpl.
 */
@Service
@Transactional
public class LabelPrintingServiceImpl implements LabelPrintingService {

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingServiceImpl.class);
	public static final String BARCODE = "barcode";
	public static final String SELECTED_NAME = "selectedName";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME_KEY = "label.printing.available.fields.field.name";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY = "label.printing.available.fields.plot";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY = "label.printing.available.fields.parentage";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES_KEY = "label.printing.available.fields.plot.coordinates";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY = "label.printing.available.fields.year";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY = "label.printing.available.fields.season";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_STUDY_NAME_KEY = "label.printing.available.fields.study.name";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY = "label.printing.available.fields.location";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME_KEY = "label.printing.available.fields.block.name";

	protected static final Integer[] BASE_LABEL_PRINTING_FIELD_MAP_LABEL_IDS = new Integer[] {
		AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt(), AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt(),
		AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()};
	public static final String INCLUDE_NON_PDF_HEADERS = "1";
	public static final String BARCODE_NEEDED = "1";
	public static final String BARCODE_GENERATED_AUTOMATICALLY = "1";
	private final Comparator<FieldMapLabel> plotNumberEntryNumberAscComparator = new FieldMapLabelComparator();

	/**
	 * The message source.
	 */
	@Resource
	private MessageSource messageSource;

	@Resource
	private LabelGeneratorFactory labelGeneratorFactory;

	@Resource
	private PresetService presetService;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private SettingsService settingsService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private InventoryService inventoryMiddlewareService;

	@Resource
	private PedigreeService pedigreeService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	public LabelPrintingServiceImpl() {
		super();
	}

	@Override
	public String generateLabels(final String labelType, final List<StudyTrialInstanceInfo> trialInstances,
		final UserLabelPrinting userLabelPrinting) throws LabelPrintingException {
		// sort the labels contained inside the trial instances so that they are arranged from highest to lowest by entry number
		this.sortTrialInstanceLabels(trialInstances);
		return this.labelGeneratorFactory.retrieveLabelGenerator(labelType).generateLabels(trialInstances, userLabelPrinting);
	}

	private void sortTrialInstanceLabels(final List<StudyTrialInstanceInfo> trialInstances) {
		for (final StudyTrialInstanceInfo trialInstance : trialInstances) {
			Collections.sort(trialInstance.getTrialInstance().getFieldMapLabels(),
				this.plotNumberEntryNumberAscComparator);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void populateUserSpecifiedLabelFields(final List<FieldMapTrialInstanceInfo> trialFieldMap, final Workbook workbook,
		final String selectedFields, final UserLabelPrinting userLabelPrinting) {

		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setVariableMap(this.convertToMap(workbook.getConditions(), workbook.getFactors()));
		params.setSelectedFieldIDs(SettingsUtil.parseFieldListAndConvertToListOfIDs(selectedFields));

		params.setAllFieldIDs(this.convertToListInteger(this.getAvailableLabelFieldsForStudy(true, Locale.ENGLISH, workbook
			.getStudyDetails().getId())));

		final Map<String, List<MeasurementRow>> measurementData;
		final Map<String, MeasurementRow> environmentData;
		measurementData = this.extractMeasurementRowsPerTrialInstance(workbook.getObservations());
		environmentData = this.extractEnvironmentMeasurementDataPerTrialInstance(workbook);

		this.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, params, measurementData, environmentData);
	}

	void checkAndSetFieldMapInstanceInfo(final List<FieldMapTrialInstanceInfo> trialFieldMap, final Workbook workbook,
		final LabelPrintingProcessingParams params,
		final Map<String, List<MeasurementRow>> measurementData, final Map<String, MeasurementRow> environmentData) {

		for (final FieldMapTrialInstanceInfo instanceInfo : trialFieldMap) {
			params.setInstanceInfo(instanceInfo);
			params.setInstanceMeasurements(measurementData.get(instanceInfo.getTrialInstanceNo()));
			params.setEnvironmentData(environmentData.get(instanceInfo.getTrialInstanceNo()));
			this.processUserSpecificLabelsForInstance(params, workbook);
			this.processInventorySpecificLabelsForInstance(params, workbook);
		}
	}

	private List<Integer> convertToListInteger(final List<LabelFields> availableLabelFields) {
		final List<Integer> list = new ArrayList<>();
		for (final LabelFields field : availableLabelFields) {
			list.add(field.getId());
		}
		return list;
	}

	private void processInventorySpecificLabelsForInstance(final LabelPrintingProcessingParams params, final Workbook workbook) {
		final Integer studyId = workbook.getStudyDetails().getId();
		final Map<Integer, InventoryDetails> inventoryDetailsMap = this.retrieveInventoryDetailsMap(studyId);

		if (!inventoryDetailsMap.isEmpty()) {
			for (final MeasurementRow measurement : params.getInstanceMeasurements()) {
				final FieldMapLabel label = params.getInstanceInfo().getFieldMapLabel(measurement.getExperimentId());

				final InventoryDetails inventoryDetails = inventoryDetailsMap.get(label.getGid());
				if (inventoryDetails != null) {
					label.setInventoryAmount(inventoryDetails.getAmount());
					label.setScaleName(inventoryDetails.getScaleName());
					label.setLotId(inventoryDetails.getLotId());
				}
			}
		}
	}

	private Map<Integer, InventoryDetails> retrieveInventoryDetailsMap(final Integer studyId) {
		Map<Integer, InventoryDetails> inventoryDetailsMap = new HashMap<>();
		try {
			inventoryDetailsMap = this.inventoryMiddlewareService.getInventoryDetails(studyId).stream()
				.filter(inventoryDetails -> inventoryDetails.getLotId() != null)
				.collect(Collectors.toMap(InventoryDetails::getGid, inventoryDetails -> inventoryDetails,
					(inventoryDetails1, inventoryDetails2) -> inventoryDetails2));
		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}
		return inventoryDetailsMap;
	}

	@Override
	public void deleteProgramPreset(final Integer programPresetId) {

		this.presetService.deleteProgramPreset(programPresetId);

	}

	protected Map<String, MeasurementRow> extractEnvironmentMeasurementDataPerTrialInstance(final Workbook workbook) {
		final Map<String, MeasurementRow> data = new HashMap<>();

		for (final MeasurementRow row : workbook.getTrialObservations()) {
			final String trialInstance = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
			data.put(trialInstance, row);
		}

		return data;
	}

	protected void processUserSpecificLabelsForInstance(final LabelPrintingProcessingParams params, final Workbook workbook) {

		params.setLabelHeaders(new HashMap<Integer, String>());
		boolean firstEntry = true;

		for (final MeasurementRow measurement : params.getInstanceMeasurements()) {
			final FieldMapLabel label = params.getInstanceInfo().getFieldMapLabel(measurement.getExperimentId());

			final Map<Integer, String> userSpecifiedLabels =
				this.extractDataForUserSpecifiedLabels(params, measurement, firstEntry, workbook);

			params.setUserSpecifiedLabels(userSpecifiedLabels);

			label.setUserFields(userSpecifiedLabels);

			if (firstEntry) {
				firstEntry = false;
			}

			params.getInstanceInfo().setLabelHeaders(params.getLabelHeaders());
		}

	}

	private void setCross(final InventoryDetails entry) {
		// get the original gid as when the inventory is bulked and is a bulking donor, the gid becomes null
		final Integer gid = entry.getOriginalGid();
		try {
			final String cross = this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties);
			entry.setCross(cross);
		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}
	}

	protected Map<Integer, MeasurementVariable> convertToMap(final List<MeasurementVariable>... variables) {
		final Map<Integer, MeasurementVariable> map = new HashMap<>();

		for (final List<MeasurementVariable> variableList : variables) {
			for (final MeasurementVariable variable : variableList) {
				map.put(variable.getTermId(), variable);
			}
		}

		return map;
	}

	protected Map<Integer, String> extractDataForUserSpecifiedLabels(final LabelPrintingProcessingParams params,
		final MeasurementRow measurementRow, final boolean populateHeaders,
		final Workbook workbook) {

		final Map<Integer, String> values = new HashMap<>();

		for (final Integer termID : params.getAllFieldIDs()) {
			if (!this.populateValuesFromMeasurement(params, measurementRow, termID, values, populateHeaders)) {
				this.populateValuesForStudy(params, termID, values, populateHeaders, workbook);
			}
		}
		return values;

	}

	protected Boolean populateValuesFromMeasurement(final LabelPrintingProcessingParams params, final MeasurementRow measurementRow,
		final Integer termID, final Map<Integer, String> values, final boolean populateHeaders) {

		try {

			final MeasurementData data = measurementRow.getMeasurementData(termID);

			if (data != null) {

				final String value = data.getDisplayValue();
				values.put(termID, value);

				if (populateHeaders) {
					params.getLabelHeaders().put(termID, data.getMeasurementVariable().getName());
				}
				return true;
			}

		} catch (final NumberFormatException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}

		return false;
	}

	protected Integer getCounterpartTermId(final Integer termId) {

		final String nameTermId = SettingsUtil.getNameCounterpart(termId, AppConstants.ID_NAME_COMBINATION.getString());

		if (!StringUtils.isEmpty(nameTermId)) {
			return Integer.valueOf(nameTermId);
		} else {
			return termId;
		}
	}

	protected void populateValuesForStudy(final LabelPrintingProcessingParams params, final Integer termID,
		final Map<Integer, String> values, final boolean populateHeaders, final Workbook workbook) {

		final List<MeasurementVariable> variables = new ArrayList<>();
		variables.addAll(workbook.getFactors());
		variables.addAll(workbook.getConditions());
		variables.addAll(workbook.getConstants());

		final Integer newTermId = this.getCounterpartTermId(termID);

		final MeasurementVariable factorVariable = this.getMeasurementVariableByTermId(newTermId, variables);

		if (factorVariable != null) {
			values.put(newTermId, factorVariable.getValue());

			if (populateHeaders) {
				params.getLabelHeaders().put(newTermId, factorVariable.getName());
			}
		}

		final MeasurementVariable conditionData = params.getVariableMap().get(newTermId);

		if (conditionData != null) {
			values.put(newTermId, conditionData.getValue());

			if (populateHeaders) {
				params.getLabelHeaders().put(newTermId, conditionData.getName());
			}
		}

		if (params.getEnvironmentData() == null) {
			return;
		}

		final MeasurementData enviromentData = params.getEnvironmentData().getMeasurementData(newTermId);

		if (enviromentData != null) {
			values.put(newTermId, enviromentData.getValue());

			if (populateHeaders) {
				params.getLabelHeaders().put(newTermId, enviromentData.getLabel());
			}
		}
	}

	private MeasurementVariable getMeasurementVariableByTermId(final Integer termId, final List<MeasurementVariable> measumentVariables) {
		for (final MeasurementVariable measurementVariable : measumentVariables) {
			if (measurementVariable.getTermId() == termId) {
				return measurementVariable;
			}
		}
		return null;
	}

	protected Map<String, List<MeasurementRow>> extractMeasurementRowsPerTrialInstance(final List<MeasurementRow> dataRows) {
		// sort the observations by instance number, and then by experiment ID to simplify later process
		Collections.sort(dataRows, new Comparator<MeasurementRow>() {

			@Override
			public int compare(final MeasurementRow o1, final MeasurementRow o2) {
				final String instanceID1 = o1.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
				final String instanceID2 = o2.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();

				if (instanceID1.equals(instanceID2)) {
					return Integer.compare(o1.getExperimentId(), o2.getExperimentId());
				} else {
					return instanceID1.compareTo(instanceID2);
				}
			}
		});

		final Map<String, List<MeasurementRow>> measurements = new HashMap<>();

		for (final MeasurementRow row : dataRows) {
			final String trialInstance = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
			List<MeasurementRow> list = measurements.get(trialInstance);

			if (list == null) {
				list = new ArrayList<>();
				measurements.put(trialInstance, list);
			}

			list.add(row);
		}

		return measurements;
	}

	/**
	 * Gets the available label fields.
	 *
	 * @param hasFieldMap the has field map
	 * @param locale      the locale
	 * @return
	 */
	@Override
	public List<LabelFields> getAvailableLabelFieldsForFieldMap(final boolean hasFieldMap, final Locale locale) {
		final List<LabelFields> labelFieldsList = new ArrayList<>();

		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.entry.num", null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.gid", null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.germplasm.name", null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt(), true));
		labelFieldsList.add(new LabelFields(
			this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY, null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt(), true));
		labelFieldsList.add(
			new LabelFields(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt(), false));
		labelFieldsList.add(new LabelFields(
			this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY, null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt(), false));
		labelFieldsList.add(new LabelFields(
			this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY, null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt(), false));

		labelFieldsList.add(new LabelFields(
			this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_STUDY_NAME_KEY, null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_STUDY_NAME.getInt(), false));
		labelFieldsList.add(
			new LabelFields(this.messageSource.getMessage("label.printing.available.fields.trial.instance.num", null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt(), false));

		labelFieldsList.add(
			new LabelFields(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt(), false));

		this.addAvailableFieldsForFieldMap(hasFieldMap, locale, labelFieldsList);

		return labelFieldsList;
	}

	@Override
	public List<LabelFields> getAvailableLabelFieldsForStudy(final boolean hasFieldMap, final Locale locale,
		final int studyID) {
		final List<LabelFields> labelFieldsList = new ArrayList<>();

		Workbook workbook;

		labelFieldsList.add(new LabelFields(
			this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_STUDY_NAME_KEY, null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_STUDY_NAME.getInt(), false));

		labelFieldsList
			.add(new LabelFields(ColumnLabels.REP_NO.getTermNameFromOntology(this.ontologyDataManager), TermId.REP_NO.getId(), true));

		workbook = this.fieldbookMiddlewareService.getStudyDataSet(studyID);

		labelFieldsList.addAll(this.settingsService.retrieveTrialSettingsAsLabels(workbook));
		labelFieldsList.addAll(this.settingsService.retrieveTrialEnvironmentConditionsAsLabels(workbook));
		labelFieldsList.addAll(this.settingsService.retrieveExperimentalDesignFactorsAsLabels(workbook));
		labelFieldsList.addAll(this.settingsService.retrieveGermplasmDescriptorsAsLabels(workbook));

		labelFieldsList.add(new LabelFields(
			this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY, null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt(), true));
		labelFieldsList.add(
			new LabelFields(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt(), false));
		labelFieldsList.add(new LabelFields(
			this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY, null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt(), false));
		labelFieldsList.add(
			new LabelFields(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt(), false));

		// add trait fields
		labelFieldsList.addAll(this.settingsService.retrieveTraitsAsLabels(workbook));

		// add field map fields
		this.addAvailableFieldsForFieldMap(hasFieldMap, locale, labelFieldsList);

		// add inventory fields if any
		if (this.inventoryMiddlewareService.hasInventoryDetails(studyID)) {
			labelFieldsList.addAll(this.addInventoryRelatedLabelFields(locale));
		}

		return labelFieldsList;
	}

	private void addAvailableFieldsForFieldMap(final boolean hasFieldMap, final Locale locale, final List<LabelFields> labelFieldsList) {
		if (hasFieldMap) {
			labelFieldsList.add(new LabelFields(this.messageSource.getMessage(
				LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt(), false));
			labelFieldsList.add(new LabelFields(this.messageSource.getMessage(
				LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt(), false));
			labelFieldsList.add(new LabelFields(this.messageSource.getMessage(
				LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt(), false));
		}
	}

	protected List<LabelFields> addInventoryRelatedLabelFields(final Locale locale) {
		final List<LabelFields> labelFieldList = new ArrayList<>();

		labelFieldList.add(new LabelFields(this.messageSource.getMessage("label.printing.seed.inventory.amount", null, locale),
			AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_AMOUNT.getInt(), false));

		labelFieldList.add(new LabelFields(this.messageSource.getMessage("label.printing.seed.inventory.scale", null, locale),
			AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_SCALE.getInt(), false));

		labelFieldList.add(new LabelFields(this.messageSource.getMessage("label.printing.seed.inventory.lotid", null, locale),
			AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt(), false));

		return labelFieldList;
	}

	@Override
	public boolean checkAndSetFieldmapProperties(final UserLabelPrinting userLabelPrinting, final FieldMapInfo fieldMapInfoDetail) {
		// if there are datasets with fieldmap, check if all trial instances of the study have fieldmaps
		if (!fieldMapInfoDetail.getDatasetsWithFieldMap().isEmpty()) {
			for (final FieldMapDatasetInfo dataset : fieldMapInfoDetail.getDatasetsWithFieldMap()) {
				if (dataset.getTrialInstances().size() == dataset.getTrialInstancesWithFieldMap().size()) {
					userLabelPrinting.setFieldMapsExisting(true);
				} else {
					userLabelPrinting.setFieldMapsExisting(false);
				}
			}
			return true;
		} else {
			userLabelPrinting.setFieldMapsExisting(false);
			return false;
		}
	}

	@Override
	public LabelPrintingPresets getLabelPrintingPreset(final Integer presetId, final Integer presetType) {
		if (LabelPrintingPresets.STANDARD_PRESET == presetType) {
			final StandardPreset standardPreset = this.workbenchDataManager.getStandardPresetById(presetId);

			return new LabelPrintingPresets(presetId, standardPreset.getName(), LabelPrintingPresets.STANDARD_PRESET);

		} else {
			final ProgramPreset programPreset = this.presetService.getProgramPresetById(presetId);

			return new LabelPrintingPresets(presetId, programPreset.getName(), LabelPrintingPresets.PROGRAM_PRESET);
		}
	}

	@Override
	public ProgramPreset getLabelPrintingProgramPreset(final Integer programPresetId) {
		return this.presetService.getProgramPresetById(programPresetId);
	}

	@Override
	public List<LabelPrintingPresets> getAllLabelPrintingPresetsByName(final String presetName, final Integer programId) {
		final List<LabelPrintingPresets> out = new ArrayList<>();

		final String toolSectionName = ToolSection.PLANTING_LABEL_PRINTING_PRESET.name();

		final int fieldbookToolId = this.workbenchDataManager.getToolWithName(ToolName.FIELDBOOK_WEB.getName()).getToolId().intValue();
		final List<ProgramPreset> presets =
			this.presetService.getProgramPresetFromProgramAndToolByName(presetName, this.contextUtil.getCurrentProgramUUID(),
				fieldbookToolId, toolSectionName);

		for (final ProgramPreset preset : presets) {
			out.add(new LabelPrintingPresets(preset.getProgramPresetId(), preset.getName(), LabelPrintingPresets.PROGRAM_PRESET));
		}
		return out;
	}

	@Override
	public List<LabelPrintingPresets> getAllLabelPrintingPresets(final Integer programId) throws LabelPrintingException {
		try {
			final List<LabelPrintingPresets> allLabelPrintingPresets = new ArrayList<>();

			// 1. get the crop name of the particular programId,
			final int fieldbookToolId = this.workbenchDataManager.getToolWithName(ToolName.FIELDBOOK_WEB.getName()).getToolId().intValue();
			final String toolSectionName = ToolSection.PLANTING_LABEL_PRINTING_PRESET.name();

			// 2. add all program presets for fieldbook
			for (final ProgramPreset preset : this.presetService.getProgramPresetFromProgramAndTool(
				this.contextUtil.getCurrentProgramUUID(), fieldbookToolId, toolSectionName)) {
				allLabelPrintingPresets.add(new LabelPrintingPresets(preset.getProgramPresetId(), preset.getName(),
					LabelPrintingPresets.PROGRAM_PRESET));
			}

			return allLabelPrintingPresets;

		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
			throw new LabelPrintingException("label.printing.cannot.retrieve.presets", "database.connectivity.error", e.getMessage());

		}
	}

	@Override
	public String getLabelPrintingPresetConfig(final int presetId, final int presetType) throws LabelPrintingException {

		if (LabelPrintingPresets.STANDARD_PRESET == presetType) {
			if (this.workbenchDataManager.getStandardPresetById(presetId) == null) {
				throw new LabelPrintingException("label.printing.preset.does.not.exists");
			}
			return this.workbenchDataManager.getStandardPresetById(presetId).getConfiguration();
		} else {
			if (this.presetService.getProgramPresetById(presetId) == null) {
				throw new LabelPrintingException("label.printing.cannot.retrieve.presets", "label.printing.preset.does.not.exists", "");
			}
			return this.presetService.getProgramPresetById(presetId).getConfiguration();
		}
	}

	@Override
	public void saveOrUpdateLabelPrintingPresetConfig(final String settingsName, final String xmlConfig, final Integer programId) {
		// check if exists, override if true else add new
		final List<LabelPrintingPresets> searchPresetList =
			this.getAllLabelPrintingPresetsByName(settingsName, programId);
		final String toolSectionName = ToolSection.PLANTING_LABEL_PRINTING_PRESET.name();

		if (!searchPresetList.isEmpty()) {
			// update
			final ProgramPreset currentLabelPrintingPreset = this.getLabelPrintingProgramPreset(searchPresetList.get(0).getId());
			currentLabelPrintingPreset.setConfiguration(xmlConfig);

			this.presetService.saveOrUpdateProgramPreset(currentLabelPrintingPreset);
		} else {
			// add new
			final int fieldbookToolId = this.workbenchDataManager.getToolWithName(ToolName.FIELDBOOK_WEB.getName()).getToolId().intValue();
			final ProgramPreset preset = new ProgramPreset();
			preset.setName(settingsName);
			preset.setProgramUuid(this.contextUtil.getCurrentProgramUUID());
			preset.setToolId(fieldbookToolId);
			preset.setToolSection(toolSectionName);
			preset.setConfiguration(xmlConfig);

			this.presetService.saveOrUpdateProgramPreset(preset);
		}
	}

}
