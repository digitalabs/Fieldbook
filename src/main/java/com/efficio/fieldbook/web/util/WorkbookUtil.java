package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
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

	public static MeasurementVariable getMeasurementVariable(List<MeasurementVariable> variables, int id) {
    	if (variables != null && !variables.isEmpty()) {
			for (MeasurementVariable variable : variables) {
				if (variable != null && variable.getTermId() == id) {
					return variable;
				}
			}
    	}
		return null;
	}
	
	public static List<MeasurementRow> createMeasurementRows(List<List<ValueReference>> list, List<MeasurementVariable> variables) {
		List<MeasurementRow> observations = new ArrayList<MeasurementRow>();
		
		if (list != null && !list.isEmpty()) {
			for (List<ValueReference> row : list) {
				List<MeasurementData> dataList = new ArrayList<MeasurementData>();
				for (ValueReference ref : row) {
					MeasurementVariable var = getMeasurementVariable(variables, ref.getId());
					if (var != null) {
						boolean isEditable = !ref.getId().equals(TermId.TRIAL_INSTANCE_FACTOR.getId());
						MeasurementData data = new MeasurementData(var.getName(), ref.getName(), isEditable, var.getDataType(), var);
						dataList.add(data);
					}
				}
				observations.add(new MeasurementRow(dataList));
			}
		}
		
		return observations;
	}

}
