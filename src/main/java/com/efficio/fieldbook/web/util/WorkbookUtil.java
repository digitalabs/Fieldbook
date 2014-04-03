package com.efficio.fieldbook.web.util;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;

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
    
}
