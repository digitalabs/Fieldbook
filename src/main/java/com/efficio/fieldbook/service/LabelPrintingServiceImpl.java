package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.labelprinting.LabelGeneratorFactory;
import com.efficio.fieldbook.util.labelprinting.SeedPreparationLabelGenerator;
import com.efficio.fieldbook.util.labelprinting.comparators.FieldMapLabelComparator;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.pojos.labelprinting.LabelPrintingProcessingParams;
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
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The Class LabelPrintingServiceImpl.
 */
@Service
@Transactional
public class LabelPrintingServiceImpl implements LabelPrintingService {

	private static final String ADVANCED = "ADVANCED";
	/** The Constant LOG. */
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

	/** The message source. */
	@Resource
	private MessageSource messageSource;

	@Resource
	private LabelGeneratorFactory labelGeneratorFactory;

	@Resource
	private WorkbenchService workbenchService;

	@Resource
	private PresetDataManager presetDataManager;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private SettingsService settingsService;

	@Resource
	protected FieldbookService fieldbookService;

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
	private UserLabelPrinting userLabelPrinting;

	public LabelPrintingServiceImpl() {
		super();
	}

	

	@Override
	public String generateLabelsForGermplasmList(final String labelType, final List<GermplasmListData> germplasmListDataList,
			final UserLabelPrinting userLabelPrinting) throws LabelPrintingException {
		final SeedPreparationLabelGenerator seedPreparationLabelGenerator =
				this.labelGeneratorFactory.retrieveSeedPreparationLabelGenerator(labelType);
		return seedPreparationLabelGenerator.generateLabels(germplasmListDataList, userLabelPrinting);
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
			final String selectedFields, final boolean isStockList, final UserLabelPrinting userLabelPrinting) {

		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setVariableMap(this.convertToMap(workbook.getConditions(), workbook.getFactors()));
		params.setSelectedFieldIDs(SettingsUtil.parseFieldListAndConvertToListOfIDs(selectedFields));

		if (isStockList) {
			params.setAllFieldIDs(this.convertToListInteger(this.getAvailableLabelFieldsForStockList(this
					.getStockListType(userLabelPrinting.getStockListTypeName()), Locale.ENGLISH, workbook.getStudyDetails()
					.getId())));
		} else {
			params.setAllFieldIDs(this.convertToListInteger(this.getAvailableLabelFieldsForStudy(true, Locale.ENGLISH, workbook
					.getStudyDetails().getId())));
		}

		final Map<String, List<MeasurementRow>> measurementData;
		final Map<String, MeasurementRow> environmentData;
		measurementData = this.extractMeasurementRowsPerTrialInstance(workbook.getObservations());
		environmentData = this.extractEnvironmentMeasurementDataPerTrialInstance(workbook);

		this.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, isStockList, params, measurementData, environmentData,
				userLabelPrinting);
	}

	void checkAndSetFieldMapInstanceInfo(final List<FieldMapTrialInstanceInfo> trialFieldMap, final Workbook workbook,
			final boolean isStockList, final LabelPrintingProcessingParams params,
			final Map<String, List<MeasurementRow>> measurementData, final Map<String, MeasurementRow> environmentData,
			final UserLabelPrinting userLabelPrinting) {

		final List<InventoryDetails> inventoryDetails;
		final Map<String, List<InventoryDetails>> trialInstanceInventoryDetailMap = new HashMap<>();

		if (isStockList) {
			inventoryDetails = this.getInventoryDetails(userLabelPrinting.getStockListId());

			for (final InventoryDetails inventoryDetail : inventoryDetails) {
				final String trialInstanceNumber;
				if (inventoryDetail.getInstanceNumber() == null) {
					/*
					 * InstanceNumber must be NULL in all inventoryDetails for existing Trial Stock stored Data because Instance No was
					 * not stored while Advancing.Considering by default instance number to "1"
					 */
					trialInstanceNumber = "1";
				} else {
					trialInstanceNumber = String.valueOf(inventoryDetail.getInstanceNumber());
				}

				if (!trialInstanceInventoryDetailMap.containsKey(trialInstanceNumber)) {
					trialInstanceInventoryDetailMap.put(trialInstanceNumber, new ArrayList<InventoryDetails>());
				}
				trialInstanceInventoryDetailMap.get(trialInstanceNumber).add(inventoryDetail);

			}

		}

		for (final FieldMapTrialInstanceInfo instanceInfo : trialFieldMap) {
			params.setInstanceInfo(instanceInfo);

			if (isStockList) {
				params.setIsStockList(true);
				final List<InventoryDetails> inventories;

				inventories = trialInstanceInventoryDetailMap.get(instanceInfo.getTrialInstanceNo());

				final Map<String, InventoryDetails> inventoriesMap = new HashMap<>();

				// This will prevent execption when no inventories found for Trial instance. This will not fill up Inventory map having
				// multiple locations in trial data as we have be default store instance no to "1" for existing trial data.
				if (inventories != null && !inventories.isEmpty()) {
					for (final InventoryDetails details : inventories) {
						inventoriesMap.put(details.getEntryId().toString(), details);
					}
				}
				params.setInventoryDetailsMap(inventoriesMap);
			}


			params.setInstanceMeasurements(measurementData.get(instanceInfo.getTrialInstanceNo()));
			params.setEnvironmentData(environmentData.get(instanceInfo.getTrialInstanceNo()));

			this.processUserSpecificLabelsForInstance(params, workbook);

			if (!isStockList) {
				this.processInventorySpecificLabelsForInstance(params, workbook);
			}
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
		final Map<Integer, InventoryDetails> inventoryDetailsMap = new HashMap<>();

		try {
			GermplasmList germplasmList = null;
			final GermplasmListType listType = GermplasmListType.STUDY;
			final List<GermplasmList> germplasmLists = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, listType);
			if (!germplasmLists.isEmpty()) {
				germplasmList = germplasmLists.get(0);
			}

			if (germplasmList != null) {
				final Integer listId = germplasmList.getId();
				final String germplasmListType = germplasmList.getType();
				final List<InventoryDetails> inventoryDetailList =
						this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId, germplasmListType);

				for (final InventoryDetails inventoryDetails : inventoryDetailList) {
					if (inventoryDetails.getLotId() != null) {
						inventoryDetailsMap.put(inventoryDetails.getGid(), inventoryDetails);
					}
				}
			}
		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}

		return inventoryDetailsMap;
	}

	@Override
	public void deleteProgramPreset(final Integer programPresetId) {

		this.presetDataManager.deleteProgramPreset(programPresetId);

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

		if (params.isStockList()) {
			final List<FieldMapLabel> fieldMapLabels = new ArrayList<>();
			for (final Map.Entry<String, InventoryDetails> entry : params.getInventoryDetailsMap().entrySet()) {
				final InventoryDetails inventoryDetail = entry.getValue();

				final FieldMapLabel label = new FieldMapLabel();

				final Map<Integer, String> userSpecifiedLabels =
						this.extractDataForUserSpecifiedLabels(params, null, inventoryDetail, firstEntry, workbook);

				params.setUserSpecifiedLabels(userSpecifiedLabels);

				label.setUserFields(userSpecifiedLabels);

				fieldMapLabels.add(label);

				if (firstEntry) {
					firstEntry = false;
				}

				params.getInstanceInfo().setLabelHeaders(params.getLabelHeaders());
			}

			// this overrides the existing fieldMapLabel objects so that it will retrieve details from stock list
			// and not from germplasm list of the nursery
			params.getInstanceInfo().setFieldMapLabels(fieldMapLabels);

		} else {
			for (final MeasurementRow measurement : params.getInstanceMeasurements()) {
				final FieldMapLabel label = params.getInstanceInfo().getFieldMapLabel(measurement.getExperimentId());

				final Map<Integer, String> userSpecifiedLabels =
						this.extractDataForUserSpecifiedLabels(params, measurement, null, firstEntry, workbook);

				params.setUserSpecifiedLabels(userSpecifiedLabels);

				label.setUserFields(userSpecifiedLabels);

				if (firstEntry) {
					firstEntry = false;
				}

				params.getInstanceInfo().setLabelHeaders(params.getLabelHeaders());
			}
		}
	}

	@Override
	public List<InventoryDetails> getInventoryDetails(final int stockListId) {

		List<InventoryDetails> listDataProjects = null;
		try {
			listDataProjects = this.inventoryMiddlewareService.getInventoryListByListDataProjectListId(stockListId);

			for (final InventoryDetails entry : listDataProjects) {
				this.setCross(entry);
			}
		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}

		return listDataProjects;
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
			final MeasurementRow measurementRow, final InventoryDetails inventoryDetail, final boolean populateHeaders,
			final Workbook workbook) {

		final Map<Integer, String> values = new HashMap<>();

		for (final Integer termID : params.getAllFieldIDs()) {

			if (params.isStockList()) {

				this.populateValuesForStockList(params, inventoryDetail, termID, values, populateHeaders);
				this.populateValuesForNurseryManagement(params, workbook, termID, values, populateHeaders);

			} else if (!this.populateValuesFromMeasurement(params, measurementRow, termID, values, populateHeaders)) {

				if (workbook.isNursery()) {

					this.populateValuesForNursery(params, workbook, termID, values, populateHeaders);

				} else {

					this.populateValuesForTrial(params, termID, values, populateHeaders);

				}

			}

		}

		return values;

	}

	private void populateValuesForNurseryManagement(final LabelPrintingProcessingParams params, final Workbook workbook,
			final Integer termID, final Map<Integer, String> values, final boolean populateHeaders) {
		final List<MeasurementVariable> variables = new ArrayList<>();
		variables.addAll(workbook.getConditions());

		final Integer newTermId = this.getCounterpartTermId(termID);

		final MeasurementVariable factorVariable = this.getMeasurementVariableByTermId(newTermId, variables);

		if (factorVariable != null && values.get(newTermId) == null) {
			values.put(newTermId, factorVariable.getValue());

			if (populateHeaders) {
				params.getLabelHeaders().put(newTermId, factorVariable.getName());
			}

		}
	}

	private void populateValuesForStockList(final LabelPrintingProcessingParams params, final InventoryDetails inventoryDetails,
			final Integer termID, final Map<Integer, String> values, final boolean populateHeaders) {

		String value;

		value = this.populateStockListFromGermplasmDescriptorVariables(termID, inventoryDetails);

		if (value == null) {
			value = this.populateStockListFromInventoryVariables(termID, inventoryDetails);
		}

		if (value == null) {
			value = this.populateStockListFromCrossingVariables(termID, inventoryDetails);
		}

		if (value != null) {

			values.put(termID, value);

			if (populateHeaders) {
				try {
					params.getLabelHeaders().put(termID, this.ontologyDataManager.getTermById(termID).getName());
				} catch (final MiddlewareQueryException e) {
					LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
				}
			}
		}

	}

	private String populateStockListFromGermplasmDescriptorVariables(final Integer termID, final InventoryDetails row) {
		String value = null;
		if (termID.equals(TermId.GID.getId())) {
			value = this.getValueForStockList(row.getGid());
		} else if (termID.equals(TermId.DESIG.getId())) {
			value = this.getValueForStockList(row.getGermplasmName());
		} else if (termID.equals(TermId.ENTRY_NO.getId())) {
			value = this.getValueForStockList(row.getEntryId());
		} else if (termID.equals(TermId.CROSS.getId())) {
			value = this.getValueForStockList(row.getCross());
		} else if (termID.equals(TermId.SEED_SOURCE.getId())) {
			value = this.getValueForStockList(row.getSource());
		} else if (termID.equals(TermId.GROUPGID.getId())) {
			value = this.getValueForStockList(row.getGroupId());
		} else if (termID.equals(TermId.REP_NO.getId())) {
			value = this.getValueForStockList(row.getReplicationNumber());
		} else if (termID.equals(TermId.PLOT_NO.getId())) {
			value = this.getValueForStockList(row.getPlotNumber());
		} else if (termID.equals(TermId.TRIAL_INSTANCE_FACTOR.getId())) {
			value = this.getValueForStockList(row.getInstanceNumber());
		}
		return value;
	}

	private String populateStockListFromInventoryVariables(final Integer termID, final InventoryDetails row) {
		String value = null;
		if (termID.equals(TermId.STOCKID.getId())) {
			value = this.getValueForStockList(row.getInventoryID());
		} else if (termID.equals(TermId.LOT_LOCATION_INVENTORY.getId())) {
			value = this.getValueForStockList(row.getLocationName());
		} else if (termID.equals(TermId.AMOUNT_INVENTORY.getId())) {
			value = this.getValueForStockList(row.getAmount());
		} else if (termID.equals(TermId.UNITS_INVENTORY.getId())) {
			value = this.getValueForStockList(row.getScaleName());
		} else if (termID.equals(TermId.COMMENT_INVENTORY.getId())) {
			value = this.getValueForStockList(row.getComment());
		}
		return value;
	}

	private String populateStockListFromCrossingVariables(final Integer termID, final InventoryDetails row) {
		String value = null;
		if (termID.equals(TermId.DUPLICATE.getId())) {
			value = this.getValueForStockList(row.getDuplicate());
		} else if (termID.equals(TermId.BULK_WITH.getId())) {
			value = this.getValueForStockList(row.getBulkWith());
		} else if (termID.equals(TermId.BULK_COMPL.getId())) {
			value = this.getValueForStockList(row.getBulkCompl());
		}
		return value;
	}

	private String getValueForStockList(final Object value) {
		if (value != null) {
			return value.toString();
		}
		return "";
	}

	@Override
	public GermplasmListType getStockListType(final String type) {
		return type.equalsIgnoreCase(LabelPrintingServiceImpl.ADVANCED) ? GermplasmListType.ADVANCED : GermplasmListType.CROSSES;
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

	protected void populateValuesForTrial(final LabelPrintingProcessingParams params, final Integer termID,
			final Map<Integer, String> values, final boolean populateHeaders) {

		final Integer newTermId = this.getCounterpartTermId(termID);

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

	protected void populateValuesForNursery(final LabelPrintingProcessingParams params, final Workbook workbook, final Integer termID,
			final Map<Integer, String> values, final boolean populateHeaders) {

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
	 * @param locale the locale
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

	/**
	 * Gets the available label fields for the inventory. The following options: {GID, Designation, Cross, Stock Id, Lot Id}
	 *
	 * @param locale the locale
	 * @return the list of available label fields
	 */
	@Override
	public List<LabelFields> getAvailableLabelFieldsForInventory(final Locale locale) {
		final List<LabelFields> labelFieldsList = new ArrayList<>();

		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.list.name", null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_LIST_NAME.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.entry.num", null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.gid", null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.designation", null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_DESIGNATION.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.cross", null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_CROSS.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.stockid", null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.seed.inventory.lotid", null, locale),
				AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.seed.inventory.source", null, locale),
				AppConstants.AVAILABLE_LABEL_SEED_SOURCE.getInt(), true));

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

		workbook = this.fieldbookMiddlewareService.getTrialDataSet(studyID);

		labelFieldsList.addAll(this.settingsService.retrieveTrialSettingsAsLabels(workbook));
		labelFieldsList.addAll(this.settingsService.retrieveTrialEnvironmentConditionsAsLabels(workbook));
		labelFieldsList.addAll(this.settingsService.retrieveExperimentalDesignFactorsAsLabels(workbook));
		labelFieldsList.addAll(this.settingsService.retrieveGermplasmDescriptorsAsLabels(workbook));

		try {
			workbook = this.fieldbookMiddlewareService.getNurseryDataSet(studyID);

			labelFieldsList.addAll(this.settingsService.retrieveNurseryManagementDetailsAsLabels(workbook));
			labelFieldsList.addAll(this.settingsService.retrieveGermplasmDescriptorsAsLabels(workbook));

		} catch (final MiddlewareException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}

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
		if (this.hasInventoryValues(studyID)) {
			labelFieldsList.addAll(this.addInventoryRelatedLabelFields(locale));
		}

		return labelFieldsList;
	}

	@Override
	public List<LabelFields> getAvailableLabelFieldsForStockList(final GermplasmListType listType, final Locale locale, final int studyID) {
		final List<LabelFields> labelFieldsList = new ArrayList<>();

		final Workbook workbook;

		labelFieldsList.add(new LabelFields(
			this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_STUDY_NAME_KEY, null, locale),
			AppConstants.AVAILABLE_LABEL_FIELDS_STUDY_NAME.getInt(), false));

		workbook = this.fieldbookMiddlewareService.getTrialDataSet(studyID);

		labelFieldsList.addAll(this.settingsService.retrieveTrialSettingsAsLabels(workbook));
		labelFieldsList.addAll(this.settingsService.retrieveTrialEnvironmentConditionsAsLabels(workbook));
		labelFieldsList.addAll(this.settingsService.retrieveGermplasmDescriptorsAsLabels(workbook));

		labelFieldsList
			.add(new LabelFields(ColumnLabels.REP_NO.getTermNameFromOntology(this.ontologyDataManager), TermId.REP_NO.getId(), true));

		// Stock List Specific Fields
		labelFieldsList.addAll(this.addStockListDetailsFields(listType));

		final Set<LabelFields> uniqueLabelFields = new HashSet<>(labelFieldsList);

		labelFieldsList.clear();
		labelFieldsList.addAll(uniqueLabelFields);

		return labelFieldsList;
	}

	private List<LabelFields> addStockListDetailsFields(final GermplasmListType listType) {
		final List<LabelFields> labelFieldList = new ArrayList<>();

		labelFieldList.add(new LabelFields(ColumnLabels.PLOT_NO.getTermNameFromOntology(this.ontologyDataManager), TermId.PLOT_NO.getId(),
				true));
		
		labelFieldList.add(new LabelFields(ColumnLabels.STOCKID.getTermNameFromOntology(this.ontologyDataManager), TermId.STOCKID.getId(),
				true));

		labelFieldList.add(new LabelFields(ColumnLabels.LOT_LOCATION.getTermNameFromOntology(this.ontologyDataManager),
				TermId.LOT_LOCATION_INVENTORY.getId(), true));

		labelFieldList.add(new LabelFields(ColumnLabels.AMOUNT.getTermNameFromOntology(this.ontologyDataManager), TermId.AMOUNT_INVENTORY
				.getId(), true));

		labelFieldList.add(new LabelFields(ColumnLabels.UNITS.getTermNameFromOntology(this.ontologyDataManager), TermId.UNITS_INVENTORY
				.getId(), true));

		labelFieldList.add(new LabelFields(ColumnLabels.COMMENT.getTermNameFromOntology(this.ontologyDataManager), TermId.COMMENT_INVENTORY
				.getId(), true));

		if (GermplasmListType.isCrosses(listType)) {

			labelFieldList.add(new LabelFields(ColumnLabels.DUPLICATE.getTermNameFromOntology(this.ontologyDataManager), TermId.DUPLICATE
					.getId(), true));

			labelFieldList.add(new LabelFields(ColumnLabels.BULK_WITH.getTermNameFromOntology(this.ontologyDataManager), TermId.BULK_WITH
					.getId(), true));

			labelFieldList.add(new LabelFields(ColumnLabels.BULK_COMPL.getTermNameFromOntology(this.ontologyDataManager), TermId.BULK_COMPL
					.getId(), true));

		}

		return labelFieldList;
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

	/***
	 * Returned true if the current study's germplasm list has inventory details
	 *
	 * @param studyID
	 * @return
	 */
	protected boolean hasInventoryValues(final int studyID) {
		try {
			GermplasmList germplasmList = null;
			final GermplasmListType listType = GermplasmListType.STUDY;
			final List<GermplasmList> germplasmLists = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyID, listType);
			if (!germplasmLists.isEmpty()) {
				germplasmList = germplasmLists.get(0);
			}

			if (germplasmList != null) {
				final Integer listId = germplasmList.getId();
				final String germplasmListType = germplasmList.getType();
				final List<InventoryDetails> inventoryDetailList =
						this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId, germplasmListType);

				for (final InventoryDetails inventoryDetails : inventoryDetailList) {
					if (inventoryDetails.getLotId() != null) {
						return true;
					}
				}
			}
		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}

		return false;
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
	public LabelPrintingPresets getLabelPrintingPreset(final Integer presetId, final Integer presetType)  {
		if (LabelPrintingPresets.STANDARD_PRESET == presetType) {
			final StandardPreset standardPreset = this.workbenchService.getStandardPresetById(presetId);

			return new LabelPrintingPresets(presetId, standardPreset.getName(), LabelPrintingPresets.STANDARD_PRESET);

		} else {
			final ProgramPreset programPreset = this.presetDataManager.getProgramPresetById(presetId);

			return new LabelPrintingPresets(presetId, programPreset.getName(), LabelPrintingPresets.PROGRAM_PRESET);
		}
	}

	@Override
	public ProgramPreset getLabelPrintingProgramPreset(final Integer programPresetId)  {
		return this.presetDataManager.getProgramPresetById(programPresetId);
	}

	@Override
	public List<LabelPrintingPresets> getAllLabelPrintingPresetsByName(final String presetName, final Integer programId)  {
		final List<LabelPrintingPresets> out = new ArrayList<>();
		
		final String toolSectionName = this.userLabelPrinting.isStockList() ? ToolSection.INVENTORY_LABEL_PRINTING_PRESET.name() : ToolSection.PLANTING_LABEL_PRINTING_PRESET.name();
		
		final List<ProgramPreset> presets =
					this.presetDataManager.getProgramPresetFromProgramAndToolByName(presetName, this.contextUtil.getCurrentProgramUUID(),
							this.workbenchService.getFieldbookWebTool().getToolId().intValue(), toolSectionName);

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
			final Integer fieldbookToolId = this.workbenchService.getFieldbookWebTool().getToolId().intValue();
			final String toolSectionName = this.userLabelPrinting.isStockList() ? ToolSection.INVENTORY_LABEL_PRINTING_PRESET.name() : ToolSection.PLANTING_LABEL_PRINTING_PRESET.name();

			// 2. add all program presets for fieldbook
			for (final ProgramPreset preset : this.presetDataManager.getProgramPresetFromProgramAndTool(
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
				if (this.workbenchService.getStandardPresetById(presetId) == null) {
					throw new LabelPrintingException("label.printing.preset.does.not.exists");
				}
				return this.workbenchService.getStandardPresetById(presetId).getConfiguration();
			} else {
				if (this.presetDataManager.getProgramPresetById(presetId) == null) {
					throw new LabelPrintingException("label.printing.cannot.retrieve.presets", "label.printing.preset.does.not.exists", "");
				}
				return this.presetDataManager.getProgramPresetById(presetId).getConfiguration();
			}
	}

	@Override
	public void saveOrUpdateLabelPrintingPresetConfig(final String settingsName, final String xmlConfig, final Integer programId) {
		// check if exists, override if true else add new
		final List<LabelPrintingPresets> searchPresetList =
				this.getAllLabelPrintingPresetsByName(settingsName, programId);
		final String toolSectionName = this.userLabelPrinting.isStockList() ? ToolSection.INVENTORY_LABEL_PRINTING_PRESET.name() : ToolSection.PLANTING_LABEL_PRINTING_PRESET.name();
		
		if (!searchPresetList.isEmpty()) {
			// update
			final ProgramPreset currentLabelPrintingPreset = this.getLabelPrintingProgramPreset(searchPresetList.get(0).getId());
			currentLabelPrintingPreset.setConfiguration(xmlConfig);

			this.presetDataManager.saveOrUpdateProgramPreset(currentLabelPrintingPreset);
		} else {
			// add new
			final ProgramPreset preset = new ProgramPreset();
			preset.setName(settingsName);
			preset.setProgramUuid(this.contextUtil.getCurrentProgramUUID());
			preset.setToolId(this.workbenchService.getFieldbookWebTool().getToolId().intValue());
			preset.setToolSection(toolSectionName);
			preset.setConfiguration(xmlConfig);

			this.presetDataManager.saveOrUpdateProgramPreset(preset);
		}
	}

	public void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setFieldbookMiddlewareService(final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	public void setInventoryMiddlewareService(final InventoryService inventoryMiddlewareService) {
		this.inventoryMiddlewareService = inventoryMiddlewareService;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}
}
