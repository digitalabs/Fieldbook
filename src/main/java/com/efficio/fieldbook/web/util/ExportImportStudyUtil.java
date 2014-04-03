package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;

public class ExportImportStudyUtil {

	public static String getCategoricalCellValue(String idValue, List<ValueReference> possibleValues) {
		if (idValue != null && NumberUtils.isNumber(idValue)) {
			for (ValueReference ref : possibleValues) {
				if (ref.getId().equals(Integer.valueOf(idValue))) {
					return ref.getDescription();
				}
			}
		}
		return "";
	}
	
    public static String getCategoricalIdCellValue(String description, List<ValueReference> possibleValues) {
    	for (ValueReference possibleValue : possibleValues) {
    		if (description.equalsIgnoreCase(possibleValue.getDescription())) {
    			return possibleValue.getId().toString();
    		}
    	}
    	return "";
    }

    public static List<Integer> getLocationIdsFromTrialInstances(Workbook workbook, int start, int end) {
		List<Integer> locationIds = new ArrayList<Integer>();
		
		List<MeasurementVariable> trialVariables = workbook.getTrialVariables();
		String label = null;
		List<MeasurementRow> trialObservations = workbook.getTrialObservations();
		if (trialVariables != null && start > 0 && end > 0) {
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
						if (trialInstanceNumber >= start && trialInstanceNumber <= end) {
							locationIds.add((int) trialObservation.getLocationId());
						}
					}
				}
			}
		}
		
		if (locationIds.isEmpty()) { //default is to show all
			if (trialObservations != null) {
				for (MeasurementRow trialObservation : trialObservations) {
					locationIds.add((int) trialObservation.getLocationId());
				}
			}
		}
		
		return locationIds;
	}
	
    public static List<MeasurementRow> getApplicableObservations(Workbook workbook, int start, int end) {
    	List<MeasurementRow> rows = null;
    	if (start > 0 && end > 0) {
	    	rows = new ArrayList<MeasurementRow>();
	    	List<Integer> locationIds = getLocationIdsFromTrialInstances(workbook, start, end);
	    	for (MeasurementRow row : workbook.getObservations()) {
	    		if (locationIds.contains((int) row.getLocationId())) {
	    			rows.add(row);
	    		}
	    	}
    	} 
    	else {
    		rows = workbook.getObservations();
    	}
    	return rows;
    }
}
