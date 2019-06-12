package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.service.internal.DesignLicenseUtil;
import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
import com.efficio.fieldbook.service.internal.breedingview.BVLicenseParseException;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.AugmentedRandomizedBlockDesignService;
import com.efficio.fieldbook.web.common.service.EntryListOrderDesignService;
import com.efficio.fieldbook.web.common.service.ExperimentDesignService;
import com.efficio.fieldbook.web.common.service.PRepDesignService;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.TreatmentFactorData;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping(ExpDesignController.URL)
public class ExpDesignController extends BaseTrialController {

	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignController.class);
	public static final String URL = "/TrialManager/experimental/design";

	@Resource
	private RandomizeCompleteBlockDesignService randomizeCompleteBlockDesign;

	@Resource
	private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;

	@Resource
	private ResolvableRowColumnDesignService resolvableRowColumnDesign;

	@Resource
	private AugmentedRandomizedBlockDesignService augmentedRandomizedBlockDesignService;

	@Resource
	private EntryListOrderDesignService entryListOrderDesignService;

	@Resource
	private PRepDesignService pRepDesignService;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private DesignLicenseUtil designLicenseUtil;

	@Override
	public String getContentName() {
		return "TrialManager/openTrial";
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveDesignTypes", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public List<ExperimentDesignType> retrieveDesignTypes() {

		final List<ExperimentDesignType> designTypes = new ArrayList<>();

		designTypes.add(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK);
		designTypes.add(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK);
		designTypes.add(ExperimentDesignType.ROW_COL);
		designTypes.add(ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK);
		designTypes.add(ExperimentDesignType.CUSTOM_IMPORT);
		designTypes.add(ExperimentDesignType.ENTRY_LIST_ORDER);
		designTypes.add(ExperimentDesignType.P_REP);

		return designTypes;
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveInsertionManners", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public List<TermSummary> retrieveInsertionManners() {
		return InsertionMannerItem.getInsertionManners();
	}

	@ResponseBody
	@RequestMapping(value = "/delete/{measurementDatasetId}", method = RequestMethod.DELETE)
	public ExpDesignValidationOutput deleteGeneratedDesign(@PathVariable final Integer measurementDatasetId) {
		ExpDesignValidationOutput expParameterOutput = new ExpDesignValidationOutput(true, "The design was deleted successfully");
		final Locale locale = LocaleContextHolder.getLocale();

		try {

			this.userSelection.setMeasurementRowList(null);
			this.userSelection.getWorkbook().setOriginalObservations(null);
			this.userSelection.getWorkbook().setObservations(null);

			final VariableTypeList factors =
				this.studyDataManager.getAllStudyFactors(this.userSelection.getWorkbook().getStudyDetails().getId());

			for (final MeasurementVariable measurementVariable : this.userSelection.getWorkbook().getConditions()) {
				// update the operation for experiment design variables
				// EXP_DESIGN, EXP_DESIGN_SOURCE, NREP, PERCENTAGE_OF_REPLICATION
				// only if these variables already exists in the existing trial
				if (EXPERIMENT_DESIGN_FACTOR_IDS.contains(measurementVariable.getTermId()) && factors.findById(measurementVariable.getTermId()) != null) {
					measurementVariable.setOperation(Operation.DELETE);
				}
			}

			this.fieldbookMiddlewareService
				.deleteExperimentalDesignGenerated(this.userSelection.getWorkbook(), this.getCurrentProject().getUniqueID(),
					this.getCurrentProject().getCropType());
		} catch (final Exception e) {
			ExpDesignController.LOG.error(e.getMessage(), e);
			expParameterOutput = new ExpDesignValidationOutput(false,
				this.messageSource.getMessage("experiment.design.delete.generic.error", null, locale));
		}
		return expParameterOutput;
	}

	@ResponseBody
	@RequestMapping(value = "/generate", method = RequestMethod.POST)
	public ExpDesignValidationOutput showMeasurements(final Model model, @RequestBody final ExpDesignParameterUi expDesign) {
		/*
		 * 0 - Resolvable Complete Block Design 1 - Resolvable Incomplete Block Design 2 - Resolvable Row Col
		 */
		// we do the conversion
		final List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		final List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
		// transfer over data from user input into the list of setting details stored in the session
		final List<SettingDetail> combinedList = new ArrayList<>();
		combinedList.addAll(basicDetails);

		if (studyLevelConditions != null) {
			combinedList.addAll(studyLevelConditions);
		}

		final List<SettingDetail> variatesList = new ArrayList<>();

		// TODO: MARK FOR DELETE IBP-2689
		if (this.userSelection.getBaselineTraitsList() != null) {
			variatesList.addAll(this.userSelection.getBaselineTraitsList());
		}

		if (this.userSelection.getSelectionVariates() != null) {
			variatesList.addAll(this.userSelection.getSelectionVariates());
		}

		final String name = this.userSelection.getStudyName();
		final String description = this.userSelection.getStudyDescription();
		final String startDate = this.userSelection.getStudyStartDate();
		final String endDate = this.userSelection.getStudyEndDate();
		final String studyUpdate = this.userSelection.getStudyUpdate();

		final Dataset dataset = (Dataset) SettingsUtil.
				convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList, this.userSelection.getPlotsLevelList(),
						variatesList, this.userSelection, this.userSelection.getTrialLevelVariableList(),
						this.userSelection.getTreatmentFactors(), null, null, this.userSelection.getStudyConditions(),
						this.contextUtil.getCurrentProgramUUID(), description, startDate, endDate, studyUpdate);

		final Workbook temporaryWorkbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, this.contextUtil.getCurrentProgramUUID());
		this.userSelection.setTemporaryWorkbook(temporaryWorkbook);

		if (this.userSelection.getWorkbook() != null) {
			final int persistedNumberOfEnvironments = this.userSelection.getWorkbook().getTotalNumberOfInstances();
			if (persistedNumberOfEnvironments < Integer.parseInt(expDesign.getNoOfEnvironments())) {
				// This means we are adding new environments.
				// workbook.observations() collection is no longer pre-loaded into user session when trial is opened.
				// Load now as we need it to keep existing environments/observations data intact in workbook.
				// this is a compromise solution for now as saving trials still works through the workbook loaded in session.
				// If we dont load all observations at this stage, existing phenotypes will be wiped out on save.
				this.fieldbookMiddlewareService.loadAllObservations(this.userSelection.getWorkbook());
			}
		}

		final int designType = expDesign.getDesignType();
		final List<ImportedGermplasm> germplasmList =
				this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();

		ExpDesignValidationOutput expParameterOutput = new ExpDesignValidationOutput(true, "");
		final Locale locale = LocaleContextHolder.getLocale();
		try {

			// we validate here if there is germplasm
			if (germplasmList == null) {
				expParameterOutput = new ExpDesignValidationOutput(false,
						this.messageSource.getMessage("experiment.design.generate.no.germplasm", null, locale));
			} else {
				final ExperimentDesignService designService = this.getExpDesignService(designType);
				if (designService != null) {
					// we call the validation
					expParameterOutput = designService.validate(expDesign, germplasmList);
					// we call the actual process
					if (expParameterOutput.isValid()) {
						expDesign.setNoOfEnvironmentsToAdd(this.countNewEnvironments(expDesign.getNoOfEnvironments(), this.userSelection,
								expDesign.isHasMeasurementData()));

						// Setting starting plot number in user selection
						if (expDesign.getStartingPlotNo() != null && !expDesign.getStartingPlotNo().isEmpty()) {
							this.userSelection.setStartingPlotNo(Integer.parseInt(expDesign.getStartingPlotNo()));
						} else {
							// Default plot no will be 1 if not given
							expDesign.setStartingPlotNo("1");
							this.userSelection.setStartingPlotNo(1);
						}

						this.userSelection.setStartingEntryNo(StringUtil.parseInt(expDesign.getStartingEntryNo(), null));

						if (this.userSelection.getStartingEntryNo() != null) {
							Integer entryNo = this.userSelection.getStartingEntryNo();
							for (final ImportedGermplasm g : germplasmList) {
								g.setEntryId(entryNo++);
							}
						}

						BVDesignLicenseInfo bvDesignLicenseInfo = null;
						if (designService.requiresBreedingViewLicence()) {
							bvDesignLicenseInfo = this.designLicenseUtil.retrieveLicenseInfo();
							if(this.designLicenseUtil.isExpired(bvDesignLicenseInfo)) {
							  expParameterOutput = new ExpDesignValidationOutput(false,
									  this.messageSource.getMessage("experiment.design.license.expired", null, locale));
							  return expParameterOutput;
							}
						}

						final List<MeasurementRow> measurementRows =
							designService.generateDesign(germplasmList, expDesign, temporaryWorkbook.getConditions(), temporaryWorkbook.getFactors(),
									temporaryWorkbook.getGermplasmFactors(), temporaryWorkbook.getVariates(),temporaryWorkbook.getTreatmentFactors());

						this.userSelection.setExpDesignParams(expDesign);
						this.userSelection.setExpDesignVariables(designService.getExperimentalDesignVariables(expDesign));

						temporaryWorkbook.setObservations(this.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection,
								expDesign.isHasMeasurementData()));
						// should have at least 1 record
						final List<MeasurementVariable> currentNewFactors = new ArrayList<>();
						final List<MeasurementVariable> oldFactors = temporaryWorkbook.getFactors();
						final List<MeasurementVariable> deletedFactors = new ArrayList<>();
						if (measurementRows != null && !measurementRows.isEmpty()) {
							final List<MeasurementVariable> measurementDatasetVariables = new ArrayList<>();
							final MeasurementRow dataRow = measurementRows.get(0);
							for (final MeasurementData measurementData : dataRow.getDataList()) {
								measurementDatasetVariables.add(measurementData.getMeasurementVariable());
								if (measurementData.getMeasurementVariable() != null && measurementData.getMeasurementVariable()
									.isFactor()) {
									currentNewFactors.add(measurementData.getMeasurementVariable());
								}
							}
							temporaryWorkbook.setMeasurementDatasetVariables(measurementDatasetVariables);
						}
						for (final MeasurementVariable var : oldFactors) {
							// we do the cleanup of old variables
							if (WorkbookUtil.getMeasurementVariable(currentNewFactors, var.getTermId()) == null) {
								// we remove it
								deletedFactors.add(var);
							}
						}
						for (final MeasurementVariable var : deletedFactors) {
							oldFactors.remove(var);
						}

						temporaryWorkbook.setExpDesignVariables(designService.getRequiredDesignVariables());

						if (designService.requiresBreedingViewLicence() && this.designLicenseUtil.isExpiringWithinThirtyDays(bvDesignLicenseInfo)) {
							final int daysBeforeExpiration = Integer.parseInt(bvDesignLicenseInfo.getStatus().getLicense().getExpiryDays());
							expParameterOutput =
									new ExpDesignValidationOutput(true, this.messageSource.getMessage("experiment.design.license.expiring",
											new Integer[] {daysBeforeExpiration}, locale));
							expParameterOutput.setUserConfirmationRequired(true);
							return expParameterOutput;
						}

					}
				}
				this.saveDesignGenerated(expDesign);
			}
		} catch (final BVDesignException e) {
			// this should catch when the BV design is not successful
			expParameterOutput = new ExpDesignValidationOutput(false, this.messageSource.getMessage(e.getBvErrorCode(), null, locale));
		} catch (final BVLicenseParseException e) {
			expParameterOutput = new ExpDesignValidationOutput(false, e.getMessage());
		} catch (final Exception e) {
			ExpDesignController.LOG.error(e.getMessage(), e);
			expParameterOutput = new ExpDesignValidationOutput(false,
					this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}

		return expParameterOutput;
	}

	private void saveDesignGenerated(final ExpDesignParameterUi expDesign) {

		this.populateSettingData(this.userSelection.getStudyLevelConditions(), expDesign.getTrialSettings().getUserInput());

		this.initializeBasicUserSelectionLists();
		this.addDeletedSettingsList();
		final Map<String, TreatmentFactorData> treatmentFactorItems = convertTreatmentFactorMapToTreatmentFactorDataMap(expDesign.getTreatmentFactorsData());
		final Dataset newDataset = (Dataset) SettingsUtil.convertPojoToXmlDataSet(this.fieldbookMiddlewareService, this.userSelection.getStudyName(), this.userSelection,
			treatmentFactorItems, this.contextUtil.getCurrentProgramUUID());

		final Workbook workbookTemp = SettingsUtil.convertXmlDatasetToWorkbook(newDataset, this.userSelection.getExpDesignParams(),
			this.userSelection.getExpDesignVariables(),	this.fieldbookMiddlewareService, this.userSelection.getExperimentalDesignVariables(),
			this.contextUtil.getCurrentProgramUUID());

		workbookTemp.setStudyDetails(this.userSelection.getWorkbook().getStudyDetails());
		this.userSelection.setMeasurementRowList(null);
		this.userSelection.getWorkbook().setOriginalObservations(null);
		this.userSelection.getWorkbook().setObservations(null);

		this.addMeasurementVariablesToTrialObservationIfNecessary(expDesign.getEnvironments(), workbookTemp,
			this.userSelection.getTemporaryWorkbook().getTrialObservations());

		this.assignOperationOnExpDesignVariables(workbookTemp.getConditions());

		workbookTemp.setOriginalObservations(this.userSelection.getWorkbook().getOriginalObservations());
		workbookTemp.setTrialObservations(this.userSelection.getWorkbook().getTrialObservations());
		final int trialDatasetId = this.userSelection.getWorkbook().getTrialDatasetId();
		final int measurementDatasetId = this.userSelection.getWorkbook().getMeasurementDatesetId();
		workbookTemp.setTrialDatasetId(trialDatasetId);
		workbookTemp.setMeasurementDatesetId(measurementDatasetId);

		final List<MeasurementVariable> variablesForEnvironment = new ArrayList<>();
		variablesForEnvironment.addAll(workbookTemp.getTrialVariables());

		final List<MeasurementRow> trialEnvironmentValues = WorkbookUtil.createMeasurementRowsFromEnvironments(expDesign.getEnvironments(), variablesForEnvironment,
				this.userSelection.getExpDesignParams());
		workbookTemp.setTrialObservations(trialEnvironmentValues);

		this.userSelection.setWorkbook(workbookTemp);

		this.userSelection.setTrialEnvironmentValues(this.convertToValueReference(expDesign.getEnvironments()));
		WorkbookUtil.manageExpDesignVariablesAndObs(this.userSelection.getWorkbook(), this.userSelection.getTemporaryWorkbook());

		WorkbookUtil.addMeasurementDataToRowsExp(this.userSelection.getWorkbook().getFactors(), this.userSelection.getWorkbook().getObservations(), false, this.ontologyService,
			this.fieldbookService, this.contextUtil.getCurrentProgramUUID());
		WorkbookUtil.addMeasurementDataToRowsExp(this.userSelection.getWorkbook().getVariates(), this.userSelection.getWorkbook().getObservations(), true, this.ontologyService,
			this.fieldbookService, this.contextUtil.getCurrentProgramUUID());

		this.addVariablesFromTemporaryWorkbookToWorkbook(this.userSelection);
		this.updateObservationsFromTemporaryWorkbookToWorkbook(this.userSelection);

		this.userSelection.setTemporaryWorkbook(null);

		this.fieldbookMiddlewareService.saveExperimentalDesignGenerated(this.userSelection.getWorkbook(), this.getCurrentProject().getUniqueID(), this.getCurrentProject().getCropType());
	}

	private Map<String, TreatmentFactorData> convertTreatmentFactorMapToTreatmentFactorDataMap(final Map treatmentFactorsData) {
		final Map<String, TreatmentFactorData> treatmentFactorItems = new HashMap<>();
		if(treatmentFactorsData != null){
			final Iterator keySetIter = treatmentFactorsData.keySet().iterator();
			while (keySetIter.hasNext()) {
				final String key = (String) keySetIter.next();
				final Map treatmentDataMap = (Map) treatmentFactorsData.get(key);
				TreatmentFactorData treatmentFactorData = new TreatmentFactorData();
				final Object levelsObject =treatmentDataMap.get("levels");
				if (levelsObject instanceof String) {
					treatmentFactorData.setLevels(Integer.valueOf((String) levelsObject));
				} else if (levelsObject instanceof Integer) {
					treatmentFactorData.setLevels(Integer.valueOf((Integer) levelsObject));
				}
				treatmentFactorData.setLabels((List)treatmentDataMap.get("labels"));
				treatmentFactorData.setVariableId((Integer)treatmentDataMap.get("variableId"));
				treatmentFactorItems.put(key,treatmentFactorData);
			}
		}
		return treatmentFactorItems;
	}

	protected void assignOperationOnExpDesignVariables(final List<MeasurementVariable> conditions) {
		final VariableTypeList factors =
			this.studyDataManager.getAllStudyFactors(this.userSelection.getWorkbook().getStudyDetails().getId());

		for (final MeasurementVariable mvar : conditions) {
			// update the operation for experiment design variables
			// EXP_DESIGN, EXP_DESIGN_SOURCE, NREP, PERCENTAGE_OF_REPLICATION
			// only if these variables already exists in the existing trial
			if (EXPERIMENT_DESIGN_FACTOR_IDS.contains(mvar.getTermId()) && factors.findById(mvar.getTermId()) != null) {
				mvar.setOperation(Operation.UPDATE);
			}
		}
	}

	protected List<MeasurementRow> combineNewlyGeneratedMeasurementsWithExisting(final List<MeasurementRow> measurementRows,
		final UserSelection userSelection, final boolean hasMeasurementData) {
		final Workbook workbook;
		if (userSelection.getTemporaryWorkbook() != null && userSelection.getTemporaryWorkbook().getObservations() != null
			&& !userSelection.getTemporaryWorkbook().getObservations().isEmpty()) {
			workbook = userSelection.getTemporaryWorkbook();
		} else {
			workbook = userSelection.getWorkbook();
		}
		if (workbook != null && workbook.getObservations() != null && hasMeasurementData) {
			final List<MeasurementRow> observations = new ArrayList<>();
			observations.addAll(workbook.getObservations());
			observations.addAll(measurementRows);
			return observations;
		}
		return measurementRows;
	}

	protected String countNewEnvironments(final String noOfEnvironments, final UserSelection userSelection,
			final boolean hasMeasurementData) {
		final Workbook workbook;
		if (userSelection.getTemporaryWorkbook() != null && userSelection.getTemporaryWorkbook().getObservations() != null
				&& !userSelection.getTemporaryWorkbook().getObservations().isEmpty()) {
			workbook = userSelection.getTemporaryWorkbook();
		} else {
			workbook = userSelection.getWorkbook();
		}

		if (workbook != null && workbook.getObservations() != null && hasMeasurementData) {
			return String.valueOf(Integer.parseInt(noOfEnvironments) - this.getMaxInstanceNo(workbook.getObservations()));
		}
		return noOfEnvironments;
	}

	private int getMaxInstanceNo(final List<MeasurementRow> observations) {
		int maxTrialInstanceNo = 0;

		for (final MeasurementRow row : observations) {
			if (row.getDataList() != null) {
				final int trialNo = this.getTrialInstanceNo(row.getDataList());
				if (maxTrialInstanceNo < trialNo) {
					maxTrialInstanceNo = trialNo;
				}
			}
		}

		return maxTrialInstanceNo;
	}

	private int getTrialInstanceNo(final List<MeasurementData> dataList) {
		for (final MeasurementData data : dataList) {
			if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				return Integer.valueOf(data.getValue());
			}
		}
		return 0;
	}

	protected ExperimentDesignService getExpDesignService(final int designType) {
		if (designType == ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId()) {
			return this.randomizeCompleteBlockDesign;
		} else if (designType == ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId()) {
			return this.resolveIncompleteBlockDesign;
		} else if (designType == ExperimentDesignType.ROW_COL.getId()) {
			return this.resolvableRowColumnDesign;
		} else if (designType == ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getId()) {
			return this.augmentedRandomizedBlockDesignService;
		} else if (designType == ExperimentDesignType.ENTRY_LIST_ORDER.getId()) {
			return this.entryListOrderDesignService;
		} else if (designType == ExperimentDesignType.P_REP.getId()) {
			return this.pRepDesignService;
		}
		return null;
	}

	void setFieldbookProperties(final FieldbookProperties fieldbookProperties) {
		this.fieldbookProperties = fieldbookProperties;
	}

	void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
