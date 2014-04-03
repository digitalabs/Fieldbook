package com.efficio.fieldbook.web.util;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementVariable;

public class WorkbookUtil {

	
	public static Integer getMeasurementVariableId(List<MeasurementVariable> variables, String name) {
		for (MeasurementVariable variable : variables) {
			if (variable.getName().equalsIgnoreCase(name)) {
				return variable.getTermId();
			}
		}
		return null;
	}

	public static String getMeasurementVariableName(List<MeasurementVariable> variables, int id) {
		for (MeasurementVariable variable : variables) {
			if (variable.getTermId() == id) {
				return variable.getName();
			}
		}
		return null;
	}
}
