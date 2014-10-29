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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.DateUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Service
public class ValidationServiceImpl implements ValidationService {

	private static final String DATA_TYPE_NUMERIC = "Numeric variable";
	
	@Resource
	private ResourceBundleMessageSource messageSource;
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Override
	public boolean isValidValue(MeasurementVariable var, String value, boolean validateDateForDB) {
		return isValidValue(var, value, null, validateDateForDB);
	}
	
	public boolean isValidValue(MeasurementVariable var, String value, String cValueId, boolean validateDateForDB) {
		if (value == null || "".equals(value.trim())) {
			return true;
		}
		if (var.getMinRange() != null && var.getMaxRange() != null) {
			if (!NumberUtils.isNumber(value)) {
				return false;
			} else {
				Double numericValue = Double.valueOf(value);
				return numericValue <= var.getMaxRange() && numericValue >= var.getMinRange();
			}
			
		} else if(validateDateForDB && var != null && var.getDataTypeId() != null && var.getDataTypeId() == TermId.DATE_VARIABLE.getId() && value != null && !"".equals(value.trim())){
			return DateUtil.isValidDate(value);			
		}else if (var.getDataType() != null && value != null && !"".equals(value.trim()) && var.getDataType().equalsIgnoreCase(DATA_TYPE_NUMERIC)) {
			return NumberUtils.isNumber(value.trim());
			
		}  else if (var.getPossibleValues() != null && !var.getPossibleValues().isEmpty()) {
			Integer valueToCompare = null;
			try{
				valueToCompare = Integer.valueOf(value);
			}catch(NumberFormatException e){
				if (cValueId != null && NumberUtils.isNumber(cValueId) && isValidInteger(cValueId)) {
					valueToCompare = Integer.parseInt(cValueId);
				} else {
					return false;
				}
			}

			if (valueToCompare == null) {
				return false;
			}
			
			for (ValueReference ref : var.getPossibleValues()) {
				if(ref.getId().intValue()  == valueToCompare) {
					return true;
				}
			}
		} else {
			return true;
		}
		return false;
	}
	
	private boolean isValidInteger(String val){
		boolean valid = true;
		try {
		    Integer.parseInt(val);
		} catch (NumberFormatException e) {
			valid = false;
		}
		return valid;
	}
	
	@Override
	public void validateObservationValues(Workbook workbook, String instanceNumber) throws MiddlewareQueryException {
		Locale locale = LocaleContextHolder.getLocale();		
		if (workbook.getObservations() != null) {
			List<MeasurementRow> observations = new ArrayList();
			if(instanceNumber != null && "".equalsIgnoreCase(instanceNumber)){
				//meaning we want to validate all
				observations = workbook.getObservations();
			}else{
				observations = workbook.isNursery() ? workbook.getObservations() : WorkbookUtil.filterObservationsByTrialInstance(workbook.getObservations(), instanceNumber);
			}
			
			for (MeasurementRow row : observations) {
				for (MeasurementData data : row.getDataList()) {
					MeasurementVariable variate = data.getMeasurementVariable();
					if (!isValidValue(variate, data.getValue(), data.getcValueId(), true)) {
						throw new MiddlewareQueryException(messageSource.getMessage("error.workbook.save.invalidCellValue", new Object[] {variate.getName(), data.getValue()}, locale));
						
					}
				}
			}
		}
	}
	
	@Override
	public void validateConditionAndConstantValues(Workbook workbook, String instanceNumber) throws MiddlewareQueryException {
		Locale locale = LocaleContextHolder.getLocale();		
		if (workbook.getConditions() != null) {
			
			for (MeasurementVariable var : workbook.getConditions()) {
					
					if (WorkbookUtil.isConditionValidate(var.getTermId())){
						if(var.getTermId() == TermId.BREEDING_METHOD_CODE.getId() && var.getValue() != null && !var.getValue().equalsIgnoreCase("")){
							//we do the validation here
							 List<Method> methods = fieldbookMiddlewareService.getAllBreedingMethods(false);
				                HashMap<String, Method> methodMap = new HashMap<String, Method>();
				                //create a map to get method id based on given code
				                if (methods != null) {
				                    for (Method method : methods) {
				                        methodMap.put(method.getMcode(), method);
				                    }
				                }
				                
				                if(!methodMap.containsKey(var.getValue())){
				                	//this is an error since there is no matching method code
				                	var.setOperation(null);
				                	throw new MiddlewareQueryException(messageSource.getMessage("error.workbook.save.invalidCellValue", new Object[] {var.getName(), var.getValue()}, locale));
				                }else{
				                	var.setOperation(Operation.UPDATE);
				                }
						} else if(!isValidValue(var, var.getValue(), "", true)) {
							var.setOperation(null);
							throw new MiddlewareQueryException(messageSource.getMessage("error.workbook.save.invalidCellValue", new Object[] {var.getName(), var.getValue()}, locale));
						}
					}
				
			}
		}
		if(workbook.getTrialObservations() != null){
			List<MeasurementRow> observations = new ArrayList();
			observations = WorkbookUtil.filterObservationsByTrialInstance(workbook.getTrialObservations(), instanceNumber);
			
			
			for (MeasurementRow row : observations) {
				for (MeasurementData data : row.getDataList()) {
					MeasurementVariable variate = data.getMeasurementVariable();
					if (!isValidValue(variate, data.getValue(), data.getcValueId(), true)) {
						variate.setOperation(null);
						throw new MiddlewareQueryException(messageSource.getMessage("error.workbook.save.invalidCellValue", new Object[] {variate.getName(), data.getValue()}, locale));
						
					}
				}
			}
		}
	}
	@Override
	public void validateObservationValues(Workbook workbook, MeasurementRow row) throws MiddlewareQueryException {
		Locale locale = LocaleContextHolder.getLocale();
		if (workbook.getObservations() != null) {			
			for (MeasurementData data : row.getDataList()) {
				MeasurementVariable variate = data.getMeasurementVariable();
				if (!isValidValue(variate, data.getValue(), data.getcValueId(), false)) {
						throw new MiddlewareQueryException(messageSource.getMessage("error.workbook.save.invalidCellValue", new Object[] {variate.getName(), data.getValue()}, locale));
					}
				}			
		}
	}
		
}
