package com.efficio.fieldbook.web.common.controller.derived_variables;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("DerivedVariableController")
public class DerivedVariableController {

	private static final Logger LOG = LoggerFactory.getLogger(DerivedVariableController.class);

	@Resource
	private UserSelection studySelection;

	@Resource
	private MessageSource messageSource;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private FormulaService formulaService;

	@Resource
	private StudyService studyService;

	@Resource
	private DerivedVariableProcessor processor;

	@ResponseBody
	@RequestMapping(value = "/derived-variable/execute", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> execute(@RequestBody final CalculateVariableRequest request, final BindingResult result) {

		// Process request

		final Workbook workbook = this.studySelection.getWorkbook();

		final Map<String, Object> results = new HashMap<>();

		if (request == null || request.getGeoLocationId() == null || request.getVariableId() == null) {
			results.put("errorMessage", this.getMessage("study.execute.calculation.invalid.request"));
			return new ResponseEntity<>(results, HttpStatus.BAD_REQUEST);
		}

		// FIXME BMS-4454
		this.fieldbookMiddlewareService.loadAllObservations(workbook);

		/**
		 * Should always revert the data first to the original data here we
		 * should move here that part the copies it to the original observation
		 */
		WorkbookUtil.resetWorkbookObservations(workbook);

		final Optional<FormulaDto> formulaOptional = this.formulaService.getByTargetId(request.getVariableId());
		if (!formulaOptional.isPresent()) {
			results.put("errorMessage", this.getMessage("study.execute.calculation.formula.not.found"));
			return new ResponseEntity<>(results, HttpStatus.BAD_REQUEST);
		}
		final FormulaDto formula = formulaOptional.get();

		workbook.setHasExistingDataOverwrite(false);
		final Map<String, Object> terms = DerivedVariableUtils.extractTerms(formula.getDefinition());

		// Verify that variables are present

		final Set<Integer> variableIdsOfTraitsInStudy = this.getVariableIdsOfTraitsInStudy();
		final Set<String> inputMissingVariables = new HashSet<>();
		for (final FormulaVariable formulaVariable : formula.getInputs()) {
			if (!variableIdsOfTraitsInStudy.contains(formulaVariable.getId())) {
				inputMissingVariables.add(formulaVariable.getName());
			}
		}
		if (!inputMissingVariables.isEmpty()) {
			results.put("errorMessage", this.getMessage("study.execute.calculation.missing.variables",
				new String[] {StringUtils.join(inputMissingVariables.toArray(), ", ")}));
			return new ResponseEntity<>(results, HttpStatus.BAD_REQUEST);
		}

		// Calculate

		final Set<String> inputMissingData = new HashSet<>();

		for (final MeasurementRow row : workbook.getObservations()) {
			if (!request.getGeoLocationId().equals((int)row.getLocationId())) {
				continue;
			}

			// Get input data

			final Set<String> rowInputMissingData = new HashSet<>();
			DerivedVariableUtils.extractValues(terms, row, rowInputMissingData);
			inputMissingData.addAll(rowInputMissingData);

			if (!rowInputMissingData.isEmpty() || terms.values().contains("")) {
				continue;
			}

			// Evaluate

			String value;
			try {
				final String executableFormula = DerivedVariableUtils.replaceDelimiters(formula.getDefinition());
				value = this.processor.evaluateFormula(executableFormula, terms);
			} catch (final Exception e) {
				LOG.error("Error evaluating formula " + formula + " with inputs " + terms, e);
				results.put("errorMessage", this.getMessage("study.execute.calculation.engine.exception"));
				return new ResponseEntity<>(results, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			if (StringUtils.isBlank(value)) {
				continue;
			}

			// Process calculation result

			final MeasurementData target = row.getMeasurementData(formula.getTargetTermId());
			target.setAccepted(false);

			// Process categorical data

			final String categoricalValue = ExportImportStudyUtil.getCategoricalIdCellValue(value,
				target.getMeasurementVariable().getPossibleValues(), true);

			if (target.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
				&& !categoricalValue.equals(value)) {
				value = categoricalValue;
				target.setcValueId(value);
			} else {
				target.setcValueId(null);
			}

			// Set overwrite info

			if (!target.getValue().equals(value)) {
				if (!target.getValue().isEmpty()) {
					workbook.setHasExistingDataOverwrite(true);
				}
				target.setValue(value);
				target.setOldValue(value);
				target.setValueStatus(null);
			}
		}

		// Process response

		results.put("hasDataOverwrite", workbook.hasExistingDataOverwrite());
		if (!inputMissingData.isEmpty()) {
			results.put( "inputMissingData",
				this.getMessage("study.execute.calculation.missing.data", new String[] {StringUtils.join(inputMissingData.toArray(), ", ")}));
		}

		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/derived-variable/dependencies", method = RequestMethod.GET)
	public ResponseEntity<Set<String>> dependencyVariables() {

		final Set<Integer> variableIdsOfTraitsInStudy = this.getVariableIdsOfTraitsInStudy();
		final Set<String> derivedVariablesDependencies = new HashSet<>();

		final Set<FormulaVariable> formulaVariables = this.formulaService.getAllFormulaVariables(variableIdsOfTraitsInStudy);
		for (final FormulaVariable formulaVariable : formulaVariables) {
			if (!variableIdsOfTraitsInStudy.contains(formulaVariable.getId())) {
				derivedVariablesDependencies.add(formulaVariable.getName());
			}
		}

		return new ResponseEntity<>(derivedVariablesDependencies, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/derived-variable/dependencyVariableHasMeasurementData", method = RequestMethod.POST)
	@Transactional
	public ResponseEntity<Boolean> dependencyVariableHasMeasurementData(@RequestBody final List<Integer> ids) {

		// if study is not yet saved, no measurement data yet
		final Workbook savedWorkbook = this.studySelection.getWorkbook();

		if (savedWorkbook == null) {
			return new ResponseEntity<Boolean>(false, HttpStatus.OK);
		}

		final boolean hasMeasurementData =
				this.checkDependencyVariableHasMeasurementDataEntered(ids, savedWorkbook.getStudyDetails().getId());

		return new ResponseEntity<Boolean>(hasMeasurementData, HttpStatus.OK);
	}

	protected boolean checkDependencyVariableHasMeasurementDataEntered(final List<Integer> ids, final Integer studyId) {

		final Set<Integer> variableIdsOfTraitsInStudy = this.getVariableIdsOfTraitsInStudy();
		final List<Integer> derivedVariablesDependencies = new ArrayList<>();

		final Set<FormulaVariable> formulaVariables = this.formulaService.getAllFormulaVariables(variableIdsOfTraitsInStudy);

		// Determine which of the ids are dependency (argument) variables. If a derived variable and its dependency variables
		// are removed in a study then there's no need to check if they have measurement data.
		for (final FormulaVariable formulaVariable : formulaVariables) {
			if (ids.contains(formulaVariable.getId()) && !ids.contains(formulaVariable.getTargetTermId())) {
				derivedVariablesDependencies.add(formulaVariable.getId());
			}
		}

		if (!derivedVariablesDependencies.isEmpty()) {
			// Then check if the dependency variables contain measurement data.
			return this.studyService.hasMeasurementDataEntered(derivedVariablesDependencies, studyId);
		}

		return false;
	}

	protected Set<Integer> getVariableIdsOfTraitsInStudy() {

		final Set<Integer> variableIdsOfTraitsInStudy = new HashSet<>();

		if (this.studySelection.getBaselineTraitsList() != null) {
			for (final SettingDetail settingDetail : this.studySelection.getBaselineTraitsList()) {
				variableIdsOfTraitsInStudy.add(settingDetail.getVariable().getCvTermId());
			}
		}

		return variableIdsOfTraitsInStudy;

	}

	private String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

	private String getMessage(final String code, final Object[] args) {
		return this.messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}
}
