
package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
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

	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignController.class);
	public static final String URL = "/TrialManager/experimental/design";

	@Resource
	private OntologyService ontologyService;

	@Resource
	private RandomizeCompleteBlockDesignService randomizeCompleteBlockDesign;
	@Resource
	private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;
	@Resource
	private ResolvableRowColumnDesignService resolvableRowColumnDesign;
	@Resource
	private ResourceBundleMessageSource messageSource;

	@Override
	public String getContentName() {
		return "TrialManager/openTrial";
	}

	@ResponseBody
	@RequestMapping(value = "/generate", method = RequestMethod.POST)
	public ExpDesignValidationOutput showMeasurements(Model model, @RequestBody ExpDesignParameterUi expDesign) {
		/*
		 * 0 - Resolvable Complete Block Design 1 - Resolvable Incomplete Block Design 2 - Resolvable Row Col
		 */
		// we do the conversion
		List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
		// transfer over data from user input into the list of setting details stored in the session
		List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
		combinedList.addAll(basicDetails);

		if (studyLevelConditions != null) {
			combinedList.addAll(studyLevelConditions);
		}

		String name = "";

		Dataset dataset =
				(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList,
						this.userSelection.getPlotsLevelList(), this.userSelection.getBaselineTraitsList(), this.userSelection,
						this.userSelection.getTrialLevelVariableList(), this.userSelection.getTreatmentFactors(), null, null,
						this.userSelection.getNurseryConditions(), false);

		Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false);
		StudyDetails details = new StudyDetails();
		details.setStudyType(StudyType.T);
		workbook.setStudyDetails(details);
		this.userSelection.setTemporaryWorkbook(workbook);

		int designType = expDesign.getDesignType();
		List<ImportedGermplasm> germplasmList =
				this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();

		ExpDesignValidationOutput expParameterOutput = new ExpDesignValidationOutput(true, "");
		Locale locale = LocaleContextHolder.getLocale();
		try {

			// we validate here if there is gerplasm
			if (germplasmList == null) {
				expParameterOutput =
						new ExpDesignValidationOutput(false, this.messageSource.getMessage("experiment.design.generate.no.germplasm", null,
								locale));
			} else {
				ExperimentDesignService designService = this.getExpDesignService(designType);
				if (designService != null) {
					// we call the validation
					expParameterOutput = designService.validate(expDesign, germplasmList);
					// we call the actual process
					if (expParameterOutput.isValid()) {
						expDesign.setNoOfEnvironmentsToAdd(this.countNewEnvironments(expDesign.getNoOfEnvironments(), this.userSelection,
								expDesign.isHasMeasurementData()));
						List<MeasurementRow> measurementRows =
								designService.generateDesign(germplasmList, expDesign, workbook.getConditions(), workbook.getFactors(),
										workbook.getGermplasmFactors(), workbook.getVariates(), workbook.getTreatmentFactors());

						this.userSelection.setExpDesignParams(expDesign);
						this.userSelection.setExpDesignVariables(designService.getExperimentalDesignVariables(expDesign));

						workbook.setObservations(this.combineNewlyGeneratedMeasurementsWithExisting(measurementRows, this.userSelection,
								expDesign.isHasMeasurementData()));
						// should have at least 1 record
						List<MeasurementVariable> currentNewFactors = new ArrayList<MeasurementVariable>();
						List<MeasurementVariable> oldFactors = workbook.getFactors();
						List<MeasurementVariable> deletedFactors = new ArrayList<MeasurementVariable>();
						if (measurementRows != null && !measurementRows.isEmpty()) {
							List<MeasurementVariable> measurementDatasetVariables = new ArrayList<MeasurementVariable>();
							MeasurementRow dataRow = measurementRows.get(0);
							for (MeasurementData measurementData : dataRow.getDataList()) {
								measurementDatasetVariables.add(measurementData.getMeasurementVariable());
								if (measurementData.getMeasurementVariable() != null && measurementData.getMeasurementVariable().isFactor()) {
									currentNewFactors.add(measurementData.getMeasurementVariable());
								}
							}
							workbook.setMeasurementDatasetVariables(measurementDatasetVariables);
						}
						for (MeasurementVariable var : oldFactors) {
							// we do the cleanup of old variables
							if (WorkbookUtil.getMeasurementVariable(currentNewFactors, var.getTermId()) == null) {
								// we remove it
								deletedFactors.add(var);
							}
						}
						if (oldFactors != null) {
							for (MeasurementVariable var : deletedFactors) {
								oldFactors.remove(var);
							}
						}
						workbook.setExpDesignVariables(designService.getRequiredVariable());
					}
				}
			}
		} catch (BVDesignException e) {
			// this should catch when the BV design is not successful
			expParameterOutput = new ExpDesignValidationOutput(false, this.messageSource.getMessage(e.getBvErrorCode(), null, locale));
		} catch (Exception e) {
			ExpDesignController.LOG.error(e.getMessage(), e);
			expParameterOutput =
					new ExpDesignValidationOutput(false, this.messageSource.getMessage("experiment.design.invalid.generic.error", null,
							locale));
		}

		return expParameterOutput;
	}

	protected List<MeasurementRow> combineNewlyGeneratedMeasurementsWithExisting(List<MeasurementRow> measurementRows,
			UserSelection userSelection, boolean hasMeasurementData) {
		Workbook workbook = null;
		if (userSelection.getTemporaryWorkbook() != null && userSelection.getTemporaryWorkbook().getObservations() != null) {
			workbook = userSelection.getTemporaryWorkbook();
		} else {
			workbook = userSelection.getWorkbook();
		}
		if (workbook != null && workbook.getObservations() != null && hasMeasurementData) {
			List<MeasurementRow> observations = new ArrayList<MeasurementRow>();
			observations.addAll(workbook.getObservations());
			observations.addAll(measurementRows);
			return observations;
		}
		return measurementRows;
	}

	protected String countNewEnvironments(String noOfEnvironments, UserSelection userSelection, boolean hasMeasurementData) {
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

	private int getMaxInstanceNo(List<MeasurementRow> observations) {
		int maxTrialInstanceNo = 0;

		for (MeasurementRow row : observations) {
			if (row.getDataList() != null) {
				int trialNo = this.getTrialInstanceNo(row.getDataList());
				if (maxTrialInstanceNo < trialNo) {
					maxTrialInstanceNo = trialNo;
				}
			}
		}

		return maxTrialInstanceNo;
	}

	private int getTrialInstanceNo(List<MeasurementData> dataList) {
		for (MeasurementData data : dataList) {
			if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				return Integer.valueOf(data.getValue());
			}
		}
		return 0;
	}

	private ExperimentDesignService getExpDesignService(int designType) {
		if (designType == 0) {
			return this.randomizeCompleteBlockDesign;
		} else if (designType == 1) {
			return this.resolveIncompleteBlockDesign;
		} else if (designType == 2) {
			return this.resolvableRowColumnDesign;
		}
		return null;
	}
}
