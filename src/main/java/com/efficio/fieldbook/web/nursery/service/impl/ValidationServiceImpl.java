/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.nursery.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Resource;

import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ValidationServiceImpl implements ValidationService {

	private static final String DATA_TYPE_NUMERIC = "Numeric";
	private static final String ERROR_INVALID_CELL = "error.workbook.save.invalidCellValue";
	private static final String ERROR_NUMERIC_VARIABLE_VALUE = "error.workbook.save.invalidCellValueForNumericVariable";
	public static final String MISSING_VAL = "missing";

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Resource
	private WorkbenchDataManager workbenchDataManager;
	
	@Resource
	private ContextUtil contextUtil;

	@Override
	public boolean isValidValue(final MeasurementVariable var, final String value, final boolean validateDateForDB) {
		return this.isValidValue(var, value, null, validateDateForDB);
	}

	public boolean isValidValue(final MeasurementVariable var, final String value, final String cValueId, final boolean validateDateForDB) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		if (var.getMinRange() != null && var.getMaxRange() != null) {
			return this.validateIfValueIsMissingOrNumber(value.trim());
		} else if (validateDateForDB && var != null && var.getDataTypeId() != null && var.getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			return DateUtil.isValidDate(value);
		} else if (StringUtils.isNotBlank(var.getDataType()) && var.getDataType().equalsIgnoreCase(ValidationServiceImpl.DATA_TYPE_NUMERIC)) {
			return this.validateIfValueIsMissingOrNumber(value.trim());
		}
		return true;
	}

	private boolean validateIfValueIsMissingOrNumber(final String value) {
		if (ValidationServiceImpl.MISSING_VAL.equals(value.trim())) {
			return true;
		}
		return NumberUtils.isNumber(value);
	}
	
	@Override
	public void validateObservationValues(final Workbook workbook, final String instanceNumber) throws WorkbookParserException {
		final Locale locale = LocaleContextHolder.getLocale();
		if (workbook.getObservations() != null) {
			List<MeasurementRow> observations = new ArrayList<MeasurementRow>();
			if (instanceNumber != null && "".equalsIgnoreCase(instanceNumber)) {
				// meaning we want to validate all
				observations = workbook.getObservations();
			} else {
				observations = workbook.isNursery() ? workbook.getObservations()
						: WorkbookUtil.filterObservationsByTrialInstance(workbook.getObservations(), instanceNumber);
			}

			for (final MeasurementRow row : observations) {
				for (final MeasurementData data : row.getDataList()) {
					final MeasurementVariable variate = data.getMeasurementVariable();
					if (!this.isValidValue(variate, data.getValue(), data.getcValueId(), true)) {
						throw new WorkbookParserException(this.messageSource.getMessage(ValidationServiceImpl.ERROR_NUMERIC_VARIABLE_VALUE,
								new Object[] {variate.getName(), data.getValue()}, locale));

					}
				}
			}
		}
	}

	@Override
	public String validateConditionAndConstantValues(final Workbook workbook, final String instanceNumber) {
		String warningMessage = "";
		if (workbook.getConditions() != null) {
			for (final MeasurementVariable var : workbook.getConditions()) {
				if (WorkbookUtil.isConditionValidate(var.getTermId())) {
					if (var.getTermId() == TermId.BREEDING_METHOD_CODE.getId() && var.getValue() != null
							&& !"".equalsIgnoreCase(var.getValue())) {
						warningMessage = this.validateBreedingMethodCode(var);
					} else if (var.getTermId() == TermId.PI_ID.getId() && var.getValue() != null && !"".equalsIgnoreCase(var.getValue())) {
						warningMessage = this.validatePersonId(var);
					} else if (!this.isValidValue(var, var.getValue(), "", true)) {
						var.setOperation(null);
						var.setValue(null);
						warningMessage = this.setWarningMessage(var.getName());
					}
				}

			}
		}
		if (!workbook.getTrialObservations().isEmpty()) {
			List<MeasurementRow> observations = new ArrayList<MeasurementRow>();
			observations = WorkbookUtil.filterObservationsByTrialInstance(workbook.getTrialObservations(), instanceNumber);

			for (final MeasurementRow row : observations) {
				for (final MeasurementData data : row.getDataList()) {
					final MeasurementVariable variate = data.getMeasurementVariable();
					if (!this.isValidValue(variate, data.getValue(), data.getcValueId(), true)) {
						variate.setOperation(null);
						variate.setValue(null);
						data.setValue(null);
						warningMessage = this.setWarningMessage(variate.getName());
					}
				}
			}
		}
		return warningMessage;
	}

	String validateBreedingMethodCode(final MeasurementVariable var) {
		String warningMessage = "";
		final List<Method> methods = this.fieldbookMiddlewareService.getAllBreedingMethods(false);
		final Map<String, Method> methodMap = new HashMap<String, Method>();

		if (methods != null) {
			for (final Method method : methods) {
				methodMap.put(method.getMcode(), method);
			}
		}

		if (!methodMap.containsKey(var.getValue())) {
			// set operation and value to null since we don't want this value to be imported
			var.setOperation(null);
			var.setValue(null);
			// mark as error since there is no matching method code
			warningMessage = this.setWarningMessage(var.getName());
		} else {
			var.setOperation(Operation.UPDATE);
		}
		return warningMessage;
	}

	String validatePersonId(final MeasurementVariable var) {
		String warningMessage = "";
		if (NumberUtils.isNumber(var.getValue())) {
			final Integer workbenchUserId = this.workbenchDataManager.getWorkbenchUserIdByIBDBUserIdAndProjectId(Integer.parseInt(var.getValue()), this.contextUtil.getProjectInContext().getProjectId());
			if (workbenchUserId == null) {
				warningMessage = this.setWarningMessage(var.getName());
			}
		} else {
			warningMessage = this.setWarningMessage(var.getName());
		}
		return warningMessage;
	}

	@Override
	public void validateObservationValues(final Workbook workbook, final MeasurementRow row) throws MiddlewareQueryException {
		final Locale locale = LocaleContextHolder.getLocale();
		if (workbook.getObservations() != null) {
			for (final MeasurementData data : row.getDataList()) {
				final MeasurementVariable variate = data.getMeasurementVariable();
				if (!this.isValidValue(variate, data.getValue(), data.getcValueId(), false)) {
					throw new MiddlewareQueryException(this.messageSource.getMessage(ValidationServiceImpl.ERROR_INVALID_CELL,
							new Object[] {variate.getName(), data.getValue()}, locale));
				}
			}
		}
	}

	private String setWarningMessage(final String value) {
		return "The value for " + value + " in the import file is invalid and will not be imported. "
				+ "You can change this value by editing it manually, or by uploading a corrected import file.";
	}

}
