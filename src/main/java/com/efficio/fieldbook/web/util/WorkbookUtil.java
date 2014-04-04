package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;

public class WorkbookUtil {

	
	public static Integer getMeasurementVariableId(List<MeasurementVariable> variables, String name) {
    	if (variables != null && !variables.isEmpty()) {
			for (MeasurementVariable variable : variables) {
				if (variable.getName().equalsIgnoreCase(name)) {
					return variable.getTermId();
				}
			}
    	}
		return null;
	}

	public static String getMeasurementVariableName(List<MeasurementVariable> variables, int id) {
    	if (variables != null && !variables.isEmpty()) {
			for (MeasurementVariable variable : variables) {
				if (variable != null && variable.getTermId() == id) {
					return variable.getName();
				}
			}
    	}
		return null;
	}

    public static String getValueByIdInRow(List<MeasurementVariable> variables, int termId, MeasurementRow row) {
    	String label = getMeasurementVariableName(variables, termId);
    	if (label != null) {
   			return row.getMeasurementDataValue(label);
    	}
    	return null;
    }

    public static List<MeasurementRow> filterObservationsByTrialInstance(List<MeasurementRow> observations, String trialInstance) {
    	List<MeasurementRow> list = new ArrayList<MeasurementRow>();
    	if (observations != null && !observations.isEmpty()) {
    		List<MeasurementVariable> variables = observations.get(0).getMeasurementVariables();
    		for (MeasurementRow row : observations) {
    			String value = getValueByIdInRow(variables, TermId.TRIAL_INSTANCE_FACTOR.getId(), row);
    			if (value == null || value != null && value.equals(trialInstance)) {
    				list.add(row);
    			}
    		}
    	}
    	return list;
    }
}
