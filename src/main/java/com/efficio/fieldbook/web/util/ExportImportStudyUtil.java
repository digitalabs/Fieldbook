package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportImportStudyUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExportImportStudyUtil.class);
	
	private static final Integer[] REQUIRED_COLUMNS = {TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId(), TermId.DESIG.getId()};
	
	private ExportImportStudyUtil(){
		// do nothing
	}

	public static String getCategoricalCellValue(String idValue, List<ValueReference> possibleValues) {
		//With the New Data Table, the idValue will contain the long text instead of the id.
		if (idValue != null && possibleValues != null && !possibleValues.isEmpty()) {
			for (ValueReference possibleValue : possibleValues) {
				if (idValue.equalsIgnoreCase(possibleValue.getDescription())) {
					return possibleValue.getName();
				}
			}
		}
		//just in case an id was passed, but this won't be the case most of the time
		if (idValue != null && NumberUtils.isNumber(idValue)) {
			for (ValueReference ref : possibleValues) {
				if (ref.getId().equals(Integer.valueOf(idValue))) {
					return ref.getName();
				}
			}
		}
		return idValue;
	}
	
    public static String getCategoricalIdCellValue(String description, List<ValueReference> possibleValues) {
    	return getCategoricalIdCellValue(description, possibleValues, false);
    }
    
    public static String getCategoricalIdCellValue(String description, List<ValueReference> possibleValues, boolean isReturnOriginalValue) {
    	if(description != null){
	    	for (ValueReference possibleValue : possibleValues) {
	    		if (description.equalsIgnoreCase(possibleValue.getName())) {
	    			return possibleValue.getId().toString();
	    		}
	    	}
    	}
    	return isReturnOriginalValue ? description : "";
    }

    public static List<Integer> getLocationIdsFromTrialInstances(Workbook workbook, List<Integer> instances) {
		List<Integer> locationIds = new ArrayList<Integer>();
		
		List<MeasurementVariable> trialVariables = workbook.getTrialVariables();
		String label = null;
		List<MeasurementRow> trialObservations = workbook.getTrialObservations();
		if (trialVariables != null && instances != null && !instances.isEmpty()) {
			for (MeasurementVariable trialVariable : trialVariables) {
				if (trialVariable.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					label = trialVariable.getName();
					break;
				}
			}
			if (trialObservations != null && label != null) {
				for (MeasurementRow trialObservation : trialObservations) {
					String trialInstanceString = trialObservation.getMeasurementDataValue(label);
					if (trialInstanceString != null && NumberUtils.isNumber(trialInstanceString)) {
						int trialInstanceNumber = Double.valueOf(trialInstanceString).intValue();						
						if(instances != null && instances.indexOf(Integer.valueOf(trialInstanceNumber)) != -1){
							locationIds.add((int) trialObservation.getLocationId());
						}
					}
				}
			}
		}
		
		if (locationIds.isEmpty() && trialObservations != null) {
			for (MeasurementRow trialObservation : trialObservations) {
				locationIds.add((int) trialObservation.getLocationId());
			}
		}
		
		return locationIds;
	}
	
    public static List<MeasurementRow> getApplicableObservations(Workbook workbook, List<MeasurementRow> observations,  List<Integer> instances) {
    	List<MeasurementRow> rows = null;
    	if (instances != null && !instances.isEmpty()) {
	    	rows = new ArrayList<MeasurementRow>();
	    	List<Integer> locationIds = getLocationIdsFromTrialInstances(workbook, instances);
	    	for (MeasurementRow row : observations) {
	    		if (locationIds.contains((int) row.getLocationId())) {
	    			rows.add(row);
	    		}
	    	}
    	} else {
    		rows = workbook.getObservations();
    	}
    	return rows;
    }
    
	public static String getSiteNameOfTrialInstance(MeasurementRow trialObservation, 
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService)
			throws MiddlewareQueryException {
		if (trialObservation != null && trialObservation.getMeasurementVariables() != null) {
			for (MeasurementData data : trialObservation.getDataList()) {
				if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_LOCATION.getId()) {
					return "_" + data.getValue();
				} else if (data.getMeasurementVariable().getTermId() == TermId.LOCATION_ID.getId()) {
					return getSiteNameOfTrialInstanceBasedOnLocationID(
							fieldbookMiddlewareService, data);
				}
			}
		}
		return "";
	}

	private static String getSiteNameOfTrialInstanceBasedOnLocationID(
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
			MeasurementData data) throws MiddlewareQueryException {
		if (data.getValue() != null && !data.getValue().isEmpty() && NumberUtils.isNumber(data.getValue())) {
			return "_" + fieldbookMiddlewareService.getLocationById(Integer.parseInt(data.getValue())).getLname();
		} else {
			return "";
		}
	}

	public static boolean partOfRequiredColumns(int termId) {
		for(int id : REQUIRED_COLUMNS){
			if(termId == id){
				return true;
			}
		}
		return false;
	}
	
	public static String getPropertyName(OntologyService ontologyService) {
		String propertyName = "";
		try {
		    propertyName = ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId()).getTerm().getName();
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		return propertyName;
	}
	
	public static boolean measurementVariableHasValue(MeasurementData dataCell) {
		return dataCell.getMeasurementVariable() != null
				&& dataCell.getMeasurementVariable().getPossibleValues() != null;
	}
	
	public static boolean isColumnVisible(int termId, List<Integer> visibleColumns){
		if(visibleColumns == null){
			return true;
		} else {
			return partOfRequiredColumns(termId) || visibleColumns.contains(termId);
		}
	}
}
