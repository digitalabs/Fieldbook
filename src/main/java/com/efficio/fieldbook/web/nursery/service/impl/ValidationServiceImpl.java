/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.nursery.service.impl;

import java.util.Locale;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.service.ValidationService;

@Service
public class ValidationServiceImpl implements ValidationService {

	private static final String DATA_TYPE_NUMERIC = "Numeric variable";
	
	@Resource
	private ResourceBundleMessageSource messageSource;
	
	private boolean isValidValue(MeasurementVariable var, String value) {
		if (var.getDataType() != null && value != null && !"".equals(value.trim()) && var.getDataType().equalsIgnoreCase(DATA_TYPE_NUMERIC)) {
			return NumberUtils.isNumber(value);
		}
		return true;
	}
	
	@Override
	public void validateObservationValues(Workbook workbook) throws MiddlewareQueryException {
		Locale locale = LocaleContextHolder.getLocale();
		if (workbook.getObservations() != null) {
			for (MeasurementRow row : workbook.getObservations()) {
				for (MeasurementVariable variate : workbook.getVariates()) {
					if (!isValidValue(variate, row.getMeasurementDataValue(variate.getName()))) {
						throw new MiddlewareQueryException(messageSource.getMessage("error.workbook.save.invalidCellValue", new Object[] {variate.getName()}, locale));
					}
				}
			}
		}
	}
}
