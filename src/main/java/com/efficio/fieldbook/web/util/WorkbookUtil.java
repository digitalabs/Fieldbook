package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;

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

	public static void addVariateToObservations(MeasurementVariable mvar, List<MeasurementRow> observations) {
		if (observations != null) {
			for (MeasurementRow row : observations) {
				MeasurementData mData = new MeasurementData();
				mData.setMeasurementVariable(mvar);
				mData.setLabel(mvar.getName());
				mData.setDataType(mvar.getDataType());
				mData.setEditable(true);
				row.getDataList().add(mData);
			}
		}
	}
	
	public static List<String> getAddedTraits(List<MeasurementVariable> variables, List<MeasurementRow> observations) {
		List<String> newTraits = new ArrayList<String>();
		if (observations != null && !observations.isEmpty()) {
			List<MeasurementVariable> workbookVariables = observations.get(0).getMeasurementVariables();
			if (workbookVariables != null && !workbookVariables.isEmpty()) {
				for (MeasurementVariable wvar : workbookVariables) {
					if (!wvar.isFactor()) {
						boolean found = false;
						for (MeasurementVariable var : variables) {
							if (wvar.getTermId() == var.getTermId()) {
								found = true;
								break;
							}
						}
						if (!found) {
							newTraits.add(wvar.getName());
						}
					}
				}
			}
		}
		return newTraits;
	}

	public static List<MeasurementVariable> getAddedTraitVariables(List<MeasurementVariable> variables, List<MeasurementRow> observations) {
		List<MeasurementVariable> newTraits = new ArrayList<MeasurementVariable>();
		if (observations != null && !observations.isEmpty()) {
			List<MeasurementVariable> workbookVariables = observations.get(0).getMeasurementVariables();
			if (workbookVariables != null && !workbookVariables.isEmpty()) {
				for (MeasurementVariable wvar : workbookVariables) {
					if (!wvar.isFactor()) {
						boolean found = false;
						for (MeasurementVariable var : variables) {
							if (wvar.getTermId() == var.getTermId()) {
								found = true;
								break;
							}
						}
						if (!found) {
							wvar.setOperation(Operation.ADD);
							newTraits.add(wvar);
						}
					}
				}
			}
		}
		return newTraits;
	}
	
	public static void removeNewlyAddedTraits(List<MeasurementVariable> variables, List<MeasurementRow> observations) {
		List<MeasurementVariable> newTraits = getAddedTraitVariables(variables, observations);
		
	}
	
	public static void clearNewlyAddedImportTraits(List<MeasurementVariable> variables, List<MeasurementRow> observations) {
		List<MeasurementVariable> newTraits = getAddedTraitVariables(variables, observations);
		List<Integer> indexForRemoval = new ArrayList<Integer>();
		if (observations != null && !observations.isEmpty()) {
			List<MeasurementData> initialDataList = observations.get(0).getDataList();
			for(MeasurementData initialData : initialDataList){
				for(int index = 0 ; index < newTraits.size() ; index++){
					if(initialData.getMeasurementVariable().getTermId() ==  newTraits.get(index).getTermId()){
						//means this is a newly added trait, we should remove it
						indexForRemoval.add(Integer.valueOf(index));
					}
				}
			}
			if(indexForRemoval != null && !indexForRemoval.isEmpty()){
				for(MeasurementRow dataRow : observations){
					for(Integer removedMeasurementDataIndex : indexForRemoval){
						dataRow.getDataList().remove(removedMeasurementDataIndex);						
					}
				}
			}
			
		}
	}
	
	 public static void resetWorkbookObservations(Workbook workbook) {
    	if (workbook.getObservations() != null && !workbook.getObservations().isEmpty()) {
	    	if (workbook.getOriginalObservations() == null || workbook.getOriginalObservations().isEmpty()) {
	    		List<MeasurementRow> origObservations = new ArrayList<MeasurementRow>();
	    		for (MeasurementRow row : workbook.getObservations()) {
	    			origObservations.add(row.copy());
	    		}
	    		workbook.setOriginalObservations(origObservations);
	    	} else {
	    		List<MeasurementRow> observations = new ArrayList<MeasurementRow>();
	    		for (MeasurementRow row : workbook.getOriginalObservations()) {
	    			observations.add(row.copy());
	    		}
	    		workbook.setObservations(observations);
	    	}
    	}
    }
    
}
