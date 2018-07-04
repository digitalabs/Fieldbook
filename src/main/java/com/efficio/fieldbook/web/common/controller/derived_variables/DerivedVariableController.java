package com.efficio.fieldbook.web.common.controller.derived_variables;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
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
	private ResourceBundleMessageSource messageSource;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private FormulaService formulaService;

	private String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

	private String getMessage(final String code, final Object[] args) {
		return this.messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}

	@ResponseBody
	@RequestMapping(value = "/derived-variable/execute", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> execute(@RequestBody final CalculateVariableRequest request, final BindingResult result) {

		// Process request

		final Workbook workbook = studySelection.getWorkbook();

		final Map<String, Object> results = new HashMap<>();

		if (request == null || request.getGeoLocationId() == null || request.getVariableId() == null) {
			results.put("errorMessage", getMessage("study.execute.calculation.invalid.request"));
			return new ResponseEntity<>(results, HttpStatus.BAD_REQUEST);
		}

		// FIXME BMS-4454
		this.fieldbookMiddlewareService.loadAllObservations(workbook);

		/**
		 * Should always revert the data first to the original data here we
		 * should move here that part the copies it to the original observation
		 */
		WorkbookUtil.resetWorkbookObservations(workbook);

		final Optional<FormulaDto> formula = this.formulaService.getByTargetId(request.getVariableId());

		if (!formula.isPresent()) {
			// TODO
			results.put("errorMessage", getMessage("study.execute.calculation.formula.not.found"));
			return new ResponseEntity<>(results, HttpStatus.BAD_REQUEST);
		}

		// Calculate

		Set<String> inputMissingData = new HashSet<>();
		workbook.setHasExistingDataOverwrite(false);

		for (final MeasurementRow row : workbook.getObservations()) {
			if (!request.getGeoLocationId().equals((int)row.getLocationId())) {
				continue;
			}

			// Get input data

			final DerivedVariableProcessor processor = new DerivedVariableProcessor();
			final Map<String, Object> terms = processor.extractTerms(formula.get().getDefinition());;

			processor.extractValues(terms, row, inputMissingData);

			// Evaluate

			String value;
			try {
				value = processor.evaluateFormula(formula.get().getDefinition(), terms, null);
			} catch (Exception e) {
				LOG.error("Error evaluating formula " + formula.get() + " with inputs " + terms, e);
				results.put("errorMessage", getMessage("study.execute.calculation.engine.exception"));
				return new ResponseEntity<>(results, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			if (StringUtils.isBlank(value)) {
				continue;
			}

			// Process calculation result

			final MeasurementData target = row.getMeasurementData(formula.get().getTargetTermId());
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
			}
		}

		// Process response

		results.put("hasDataOverwrite", workbook.hasExistingDataOverwrite());
		if (!inputMissingData.isEmpty()) {
			results.put( "inputMissingData",
				getMessage("study.execute.calculation.missing.data", new String[] {StringUtils.join(inputMissingData.toArray(), ", ")}));
		}

		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/derived-variable/dependencies", method = RequestMethod.GET)
	public ResponseEntity<Set<String>> dependencyVariables() {

		final Set<Integer> variableIdsOfTraitsInStudy = new HashSet<>();
		final Set<String> derivedVariablesDependencies = new HashSet<>();

		if (studySelection.getBaselineTraitsList() != null) {
			for (final SettingDetail settingDetail : studySelection.getBaselineTraitsList()) {
				variableIdsOfTraitsInStudy.add(settingDetail.getVariable().getCvTermId());
			}
		}

		final List<FormulaVariable> formulaVariables = this.formulaService.getAllFormulaVariables(variableIdsOfTraitsInStudy);
		for (final FormulaVariable formulaVariable : formulaVariables) {
			if (!variableIdsOfTraitsInStudy.contains(formulaVariable.getId())) {
				derivedVariablesDependencies.add(formulaVariable.getName());
			}
		}

		return new ResponseEntity<>(derivedVariablesDependencies, HttpStatus.OK);
	}

}
