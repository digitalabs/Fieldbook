package com.efficio.fieldbook.web.trial.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.DesignTypeItem;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.util.ResourceFinder;
import org.generationcp.middleware.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.internal.DesignLicenseUtil;
import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
import com.efficio.fieldbook.service.internal.breedingview.BVLicenseParseException;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.AugmentedRandomizedBlockDesignService;
import com.efficio.fieldbook.web.common.service.ExperimentDesignService;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.importdesign.service.DesignImportService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

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
	private ResourceBundleMessageSource messageSource;
	@Resource
	private DesignLicenseUtil designLicenseUtil;

	@Override
	public String getContentName() {
		return "TrialManager/openTrial";
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveDesignTypes", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public List<DesignTypeItem> retrieveDesignTypes() {
		final List<DesignTypeItem> designTypes = new ArrayList<>();

		designTypes.add(DesignTypeItem.RANDOMIZED_COMPLETE_BLOCK);
		designTypes.add(DesignTypeItem.RESOLVABLE_INCOMPLETE_BLOCK);
		designTypes.add(DesignTypeItem.ROW_COL);
		designTypes.add(DesignTypeItem.AUGMENTED_RANDOMIZED_BLOCK);
		designTypes.add(DesignTypeItem.CUSTOM_IMPORT);

		return designTypes;
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

		if (this.userSelection.getBaselineTraitsList() != null) {
			variatesList.addAll(this.userSelection.getBaselineTraitsList());
		}

		if (this.userSelection.getSelectionVariates() != null) {
			variatesList.addAll(this.userSelection.getSelectionVariates());
		}

		final String name = "";

		final String description = "";
		final String startDate = "";
		final String endDate = "";
		final String studyUpdate = "";

		final Dataset dataset = (Dataset) SettingsUtil.
				convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList, this.userSelection.getPlotsLevelList(),
						variatesList, this.userSelection, this.userSelection.getTrialLevelVariableList(),
						this.userSelection.getTreatmentFactors(), null, null, this.userSelection.getNurseryConditions(), false,
						this.contextUtil.getCurrentProgramUUID(), description, startDate, endDate, studyUpdate);


		final Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false, this.contextUtil.getCurrentProgramUUID());
		final StudyDetails details = new StudyDetails();
		details.setStudyType(StudyType.T);
		workbook.setStudyDetails(details);
		this.userSelection.setTemporaryWorkbook(workbook);

		if (this.userSelection.getWorkbook() != null) {
			int persistedNumberOfEnvironments = this.userSelection.getWorkbook().getTotalNumberOfInstances();
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

			// we validate here if there is gerplasm
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

						BVDesignLicenseInfo bvDesignLicenseInfo = designLicenseUtil.retrieveLicenseInfo();

						if (this.designLicenseUtil.isExpired(bvDesignLicenseInfo)) {
							expParameterOutput =
									new ExpDesignValidationOutput(false, this.messageSource.getMessage("experiment.design.license.expired",
											null, locale));
							return expParameterOutput;
						}

						final List<MeasurementRow> measurementRows =
								designService.generateDesign(germplasmList, expDesign, workbook.getConditions(), workbook.getFactors(),
										workbook.getGermplasmFactors(), workbook.getVariates(), workbook.getTreatmentFactors());

						this.userSelection.setExpDesignParams(expDesign);
						this.userSelection.setExpDesignVariables(designService.getExperimentalDesignVariables(expDesign));

						workbook.setObservations(this.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection,
								expDesign.isHasMeasurementData()));
						// should have at least 1 record
						final List<MeasurementVariable> currentNewFactors = new ArrayList<>();
						final List<MeasurementVariable> oldFactors = workbook.getFactors();
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
							workbook.setMeasurementDatasetVariables(measurementDatasetVariables);
						}
						for (final MeasurementVariable var : oldFactors) {
							// we do the cleanup of old variables
							if (WorkbookUtil.getMeasurementVariable(currentNewFactors, var.getTermId()) == null) {
								// we remove it
								deletedFactors.add(var);
							}
						}
						if (oldFactors != null) {
							for (final MeasurementVariable var : deletedFactors) {
								oldFactors.remove(var);
							}
						}

						workbook.setExpDesignVariables(designService.getRequiredDesignVariables());

						if (this.designLicenseUtil.isExpiringWithinThirtyDays(bvDesignLicenseInfo)) {
							final int daysBeforeExpiration = Integer.parseInt(bvDesignLicenseInfo.getStatus().getLicense().getExpiryDays());
							expParameterOutput =
									new ExpDesignValidationOutput(true, this.messageSource.getMessage("experiment.design.license.expiring",
											new Integer[] {daysBeforeExpiration}, locale));
							expParameterOutput.setUserConfirmationRequired(true);
							return expParameterOutput;
						}

					}
				}
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

	protected List<MeasurementRow> combineNewlyGeneratedMeasurementsWithExisting(final List<MeasurementRow> measurementRows,
			final UserSelection userSelection, final boolean hasMeasurementData) {
		Workbook workbook = null;
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
		Workbook workbook = null;
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

	private ExperimentDesignService getExpDesignService(final int designType) {
		if (designType == DesignTypeItem.RANDOMIZED_COMPLETE_BLOCK.getId()) {
			return this.randomizeCompleteBlockDesign;
		} else if (designType == DesignTypeItem.RESOLVABLE_INCOMPLETE_BLOCK.getId()) {
			return this.resolveIncompleteBlockDesign;
		} else if (designType == DesignTypeItem.ROW_COL.getId()) {
			return this.resolvableRowColumnDesign;
		} else if (designType == DesignTypeItem.AUGMENTED_RANDOMIZED_BLOCK.getId()) {
			return this.augmentedRandomizedBlockDesignService;
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
