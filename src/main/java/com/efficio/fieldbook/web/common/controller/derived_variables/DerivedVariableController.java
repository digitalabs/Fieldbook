package com.efficio.fieldbook.web.common.controller.derived_variables;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.google.common.base.Optional;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
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
import java.util.Map;

@Controller
@RequestMapping("DerivedVariableController")
public class DerivedVariableController {

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

	@ResponseBody
	@RequestMapping(value = "/derived-variable/execute", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> execute(@RequestBody final CalculateVariableRequest request, final BindingResult result) {

		// Process request
		final Workbook workbook = studySelection.getWorkbook();

		final Map<String, Object> results = new HashMap<>();
		results.put("hasDataOverwrite", workbook.hasExistingDataOverwrite() ? "1" : "0");

		if (request == null || request.getGeoLocationId() == null || request.getVariableId() == null) {
			results.put("errorMessage", getMessage("study.derived_variables.execute.invalid.request"));
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
			results.put("errorMessage", getMessage(""));
			return new ResponseEntity<>(results, HttpStatus.BAD_REQUEST);
		}

		// Calculate

		for (final MeasurementRow row : workbook.getObservations()) {
			if (!request.getGeoLocationId().equals((int)row.getLocationId())) {
				continue;
			}

			final DerivedVariableProcessor processor = new DerivedVariableProcessor();
			final Map<String, Object> terms = processor.extractTermsFromFormula(formula.get().getDefinition());;

			processor.fetchTermValuesFromMeasurement(terms, row);

			String value = processor.evaluateFormula(formula.get().getDefinition(), terms, null);

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

		return new ResponseEntity<>(results, HttpStatus.OK);
	}

}
