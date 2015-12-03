
package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.util.CrossExpansionProperties;
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

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.ExperimentDesignService;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Controller
@RequestMapping(ExpDesignController.URL)
public class ExpDesignController extends BaseTrialController {

	private static final String WHEAT = "wheat";
	private static final String CIMMYT = "cimmyt";
	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignController.class);
	public static final String URL = "/TrialManager/experimental/design";

	@Resource
	private RandomizeCompleteBlockDesignService randomizeCompleteBlockDesign;
	@Resource
	private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;
	@Resource
	private ResolvableRowColumnDesignService resolvableRowColumnDesign;
	@Resource
	private ResourceBundleMessageSource messageSource;
	@Resource
	private CrossExpansionProperties crossExpansionProperties;
	@Resource
	private ContextUtil contextUtil;

	@Override
	public String getContentName() {
		return "TrialManager/openTrial";
	}

	@ResponseBody
	@RequestMapping(value = "/isCimmytProfileWithWheatCrop", method = RequestMethod.GET)
	public Boolean isCimmytProfileWithWheatCrop() {
		final String profile = this.crossExpansionProperties.getProfile();
		final String cropName = this.contextUtil.getProjectInContext().getCropType().getCropName();
		if (profile != null && cropName != null) {
			return CIMMYT.equalsIgnoreCase(profile) && WHEAT.equalsIgnoreCase(cropName);
		}
		return false;
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
		final List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
		combinedList.addAll(basicDetails);

		if (studyLevelConditions != null) {
			combinedList.addAll(studyLevelConditions);
		}

		final String name = "";

		final Dataset dataset =
				(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList,
						this.userSelection.getPlotsLevelList(), this.userSelection.getBaselineTraitsList(), this.userSelection,
						this.userSelection.getTrialLevelVariableList(), this.userSelection.getTreatmentFactors(), null, null,
						this.userSelection.getNurseryConditions(), false, this.contextUtil.getCurrentProgramUUID());

		final Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false, this.contextUtil.getCurrentProgramUUID());
		final StudyDetails details = new StudyDetails();
		details.setStudyType(StudyType.T);
		workbook.setStudyDetails(details);
		this.userSelection.setTemporaryWorkbook(workbook);

		final int designType = expDesign.getDesignType();
		final List<ImportedGermplasm> germplasmList =
				this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();

		ExpDesignValidationOutput expParameterOutput = new ExpDesignValidationOutput(true, "");
		final Locale locale = LocaleContextHolder.getLocale();
		try {

			// we validate here if there is gerplasm
			if (germplasmList == null) {
				expParameterOutput =
						new ExpDesignValidationOutput(false, this.messageSource.getMessage("experiment.design.generate.no.germplasm", null,
								locale));
			} else {
				final ExperimentDesignService designService = this.getExpDesignService(designType);
				if (designService != null) {
					// we call the validation
					expParameterOutput = designService.validate(expDesign, germplasmList);
					// we call the actual process
					if (expParameterOutput.isValid()) {
						expDesign.setNoOfEnvironmentsToAdd(this.countNewEnvironments(expDesign.getNoOfEnvironments(), this.userSelection,
								expDesign.isHasMeasurementData()));
						final List<MeasurementRow> measurementRows =
								designService.generateDesign(germplasmList, expDesign, workbook.getConditions(), workbook.getFactors(),
										workbook.getGermplasmFactors(), workbook.getVariates(), workbook.getTreatmentFactors());

						this.userSelection.setExpDesignParams(expDesign);
						this.userSelection.setExpDesignVariables(designService.getExperimentalDesignVariables(expDesign));

						workbook.setObservations(this.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection,
								expDesign.isHasMeasurementData()));
						// should have at least 1 record
						final List<MeasurementVariable> currentNewFactors = new ArrayList<MeasurementVariable>();
						final List<MeasurementVariable> oldFactors = workbook.getFactors();
						final List<MeasurementVariable> deletedFactors = new ArrayList<MeasurementVariable>();
						if (measurementRows != null && !measurementRows.isEmpty()) {
							final List<MeasurementVariable> measurementDatasetVariables = new ArrayList<MeasurementVariable>();
							final MeasurementRow dataRow = measurementRows.get(0);
							for (final MeasurementData measurementData : dataRow.getDataList()) {
								measurementDatasetVariables.add(measurementData.getMeasurementVariable());
								if (measurementData.getMeasurementVariable() != null && measurementData.getMeasurementVariable().isFactor()) {
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
						workbook.setExpDesignVariables(designService.getRequiredVariable());
					}
				}
			}
		} catch (final BVDesignException e) {
			// this should catch when the BV design is not successful
			expParameterOutput = new ExpDesignValidationOutput(false, this.messageSource.getMessage(e.getBvErrorCode(), null, locale));
		} catch (final Exception e) {
			ExpDesignController.LOG.error(e.getMessage(), e);
			expParameterOutput =
					new ExpDesignValidationOutput(false, this.messageSource.getMessage("experiment.design.invalid.generic.error", null,
							locale));
		}

		return expParameterOutput;
	}

	protected List<MeasurementRow> combineNewlyGeneratedMeasurementsWithExisting(final List<MeasurementRow> measurementRows,
			final UserSelection userSelection, final boolean hasMeasurementData) {
		Workbook workbook = null;
		if (userSelection.getTemporaryWorkbook() != null && userSelection.getTemporaryWorkbook().getObservations() != null) {
			workbook = userSelection.getTemporaryWorkbook();
		} else {
			workbook = userSelection.getWorkbook();
		}
		if (workbook != null && workbook.getObservations() != null && hasMeasurementData) {
			final List<MeasurementRow> observations = new ArrayList<MeasurementRow>();
			observations.addAll(workbook.getObservations());
			observations.addAll(measurementRows);
			return observations;
		}
		return measurementRows;
	}

	protected String countNewEnvironments(final String noOfEnvironments, final UserSelection userSelection, final boolean hasMeasurementData) {
		Workbook workbook = null;
		if (userSelection.getTemporaryWorkbook() != null && userSelection.getTemporaryWorkbook().getObservations() != null) {
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
		if (designType == 0) {
			return this.randomizeCompleteBlockDesign;
		} else if (designType == 1) {
			return this.resolveIncompleteBlockDesign;
		} else if (designType == 2) {
			return this.resolvableRowColumnDesign;
		}
		return null;
	}

	public void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

	@Override
	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

}
