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
package com.efficio.fieldbook.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.PossibleValuesCache;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * The Class FieldbookServiceImpl.
 */
public class FieldbookServiceImpl implements FieldbookService{
	
	/** The file service. */
	@Resource
    private FileService fileService;
	
    @Autowired
    private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

    @Autowired
    private WorkbenchService workbenchService;
    
    @Autowired
    private OntologyService ontologyService;
	
	@Resource
	private PossibleValuesCache possibleValuesCache;
	
	@Resource
	private NamingConventionService namingConventionService;
	

	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.service.api.FieldbookService#storeUserWorkbook(java.io.InputStream)
	 */
	@Override
    public String storeUserWorkbook(InputStream in) throws IOException {
        return getFileService().saveTemporaryFile(in);
    }
	
	/**
	 * Gets the file service.
	 *
	 * @return the file service
	 */
	public FileService getFileService() {
        return fileService;
    }
	
	/**
	 * Advance Nursery
	 */
	public List<ImportedGermplasm> advanceNursery(AdvancingNursery advanceInfo, Workbook workbook)
	        throws MiddlewareQueryException {

		return namingConventionService.advanceNursery(advanceInfo, workbook);
	}
	
	@Override
	public List<StandardVariableReference> filterStandardVariablesForSetting(int mode, Collection<SettingDetail> selectedList)
	throws MiddlewareQueryException {
		
		List<StandardVariableReference> result = new ArrayList<StandardVariableReference>();
		
		Set<Integer> selectedIds = new HashSet<Integer>();
		if (selectedList != null && !selectedList.isEmpty()) {
			for (SettingDetail settingDetail : selectedList) {
				selectedIds.add(settingDetail.getVariable().getCvTermId());
			}
		}

		List<Integer> storedInIds = getStoredInIdsByMode(mode, true);
		List<Integer> propertyIds = getPropertyIdsByMode(mode);

        List<StandardVariableReference> dbList = fieldbookMiddlewareService.filterStandardVariablesByMode(storedInIds, propertyIds,
                mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt() ? true : false);

        if (dbList != null && !dbList.isEmpty()) {

            for (StandardVariableReference ref : dbList) {
                if (!selectedIds.contains(ref.getId())) {

                    if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
                        if (inHideVariableFields(ref.getId(), AppConstants.FILTER_NURSERY_FIELDS.getString())
                                || ref.getId().intValue() == TermId.DATASET_NAME.getId()
                                || ref.getId().intValue() == TermId.DATASET_TITLE.getId()
                                || ref.getId().intValue() == TermId.DATASET_TYPE.getId()
                                || inHideVariableFields(ref.getId(), AppConstants.HIDE_ID_VARIABLES.getString()))
                            continue;

                    } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
                        if (inHideVariableFields(ref.getId(), AppConstants.HIDE_PLOT_FIELDS.getString())) {
                            continue;
                        }
                    } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
                        if (inHideVariableFields(ref.getId(), AppConstants.HIDE_ID_VARIABLES.getString())) {
                            continue;
                        }
                    }

                    result.add(ref);
                }
            }
        }

        Collections.sort(result);

        return result;
    }

    @Override
	public List<StandardVariableReference> filterStandardVariablesForTrialSetting(int mode, Collection<SettingDetail> selectedList)
	throws MiddlewareQueryException {
		
		List<StandardVariableReference> result = new ArrayList<StandardVariableReference>();		

		Set<Integer> selectedIds = new HashSet<Integer>();
		if (selectedList != null && !selectedList.isEmpty()) {
			for (SettingDetail settingDetail : selectedList) {
				selectedIds.add(settingDetail.getVariable().getCvTermId());
			}
		}

		List<StandardVariableReference> dbList = null;
		if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
			dbList = fieldbookMiddlewareService.getAllTreatmentLevels(AppConstants.CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS.getIntegerList());
		}
		else {
			List<Integer> storedInIds = getStoredInIdsByMode(mode, false);
			List<Integer> propertyIds = getPropertyIdsByMode(mode);
			dbList = fieldbookMiddlewareService.filterStandardVariablesByMode(storedInIds, propertyIds,
					 mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt() ? true : false);
		}
		
		if (dbList != null && !dbList.isEmpty()) {
			
			for (StandardVariableReference ref : dbList) {
				if (!selectedIds.contains(ref.getId())) {
					
					 if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
						 if(ref.getId().intValue() == TermId.STUDY_TYPE.getId()
								 || ref.getId().intValue() == TermId.PM_KEY.getId()
								 || ref.getId().intValue() == TermId.TRIAL_INSTANCE_FACTOR.getId()
								 || ref.getId().intValue() == TermId.DATASET_NAME.getId()
								 || ref.getId().intValue() == TermId.DATASET_TITLE.getId()
								 || ref.getId().intValue() == TermId.DATASET_TYPE.getId()){
							 continue;
						 }else if(inHideVariableFields(ref.getId(), AppConstants.HIDE_TRIAL_MANAGEMENT_SETTINGS_FIELDS.getString())){
							 continue;
						 }
						 
			         } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
		                 if (inHideVariableFields(ref.getId(), AppConstants.HIDE_PLOT_FIELDS.getString())) {
		                     continue;
		                 }
			         } else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
		                 if (inHideVariableFields(ref.getId(), AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString()) || inHideVariableFields(ref.getId(), AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS_FROM_POPUP.getString())) {
		                     continue;
		                 }
			         } else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
		                 if (inHideVariableFields(ref.getId(), AppConstants.CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS.getString())) {
		                     continue;
		                 }
			         }
					
					 result.add(ref);
				}
			}
		}
		
		
		 
		
		Collections.sort(result);

		return result;
	}
	
    private List<Integer> getStoredInIdsByMode(int mode, boolean isNursery) {
    	List<Integer> list = new ArrayList<Integer>();
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            list.addAll(PhenotypicType.STUDY.getTypeStorages());
            //list.addAll(PhenotypicType.DATASET.getTypeStorages());
            if(isNursery)
            	list.addAll(PhenotypicType.TRIAL_ENVIRONMENT.getTypeStorages());
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
//        	if(isNursery)
//        		list.addAll(PhenotypicType.TRIAL_ENVIRONMENT.getTypeStorages());
            list.addAll(PhenotypicType.TRIAL_DESIGN.getTypeStorages());
            list.addAll(PhenotypicType.GERMPLASM.getTypeStorages());
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt() 
                || mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
            list.addAll(PhenotypicType.VARIATE.getTypeStorages());
        } else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
            list.addAll(PhenotypicType.TRIAL_ENVIRONMENT.getTypeStorages());
        } else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
        	list.addAll(PhenotypicType.TRIAL_DESIGN.getTypeStorages());
        } else if (mode == AppConstants.SEGMENT_GERMPLASM.getInt()) {
        	list.addAll(PhenotypicType.GERMPLASM.getTypeStorages());
        }
        return list;
    }
    
    private List<Integer> getPropertyIdsByMode(int mode) {
        List<Integer> list = new ArrayList<Integer>();
        
        if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt() || mode == AppConstants.SEGMENT_TRAITS.getInt()
        		|| mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
        	
            StringTokenizer token = new StringTokenizer(AppConstants.SELECTION_VARIATES_PROPERTIES.getString(), ",");
            
            while(token.hasMoreTokens()){
                list.add(Integer.valueOf(token.nextToken()));
            }
        }
        return list;
    }
    
    private static boolean inHideVariableFields(Integer stdVarId, String variableList) {
        StringTokenizer token = new StringTokenizer(variableList, ",");
        boolean inList = false;
        while(token.hasMoreTokens()){
            if (stdVarId.equals(Integer.parseInt(token.nextToken()))) {
                inList = true;
                break;
            }
        }
        return inList;
    }
	
    @Override
    public List<ValueReference> getAllPossibleValues(int id) throws MiddlewareQueryException {
        List<ValueReference> possibleValues = possibleValuesCache.getPossibleValues(id);
        if (possibleValues == null) {

            if (TermId.BREEDING_METHOD_ID.getId() == id || TermId.BREEDING_METHOD_CODE.getId() == id) {
                List<ValueReference> list = new ArrayList<ValueReference>();
                list.add(new ValueReference(0, AppConstants.PLEASE_CHOOSE.getString(), AppConstants.PLEASE_CHOOSE.getString()));
                possibleValues = list;
                possibleValues.addAll(getAllBreedingMethods(true));
            } else if (TermId.LOCATION_ID.getId() == id) {
                possibleValues = convertLocationsToValueReferences(fieldbookMiddlewareService.getAllBreedingLocations());
            } else if (TermId.PI_ID.getId() == id || Integer.parseInt(AppConstants.COOPERATOR_ID.getString()) == id) {
                possibleValues = convertPersonsToValueReferences(fieldbookMiddlewareService.getAllPersonsOrderedByLocalCentral());
            } else if (TermId.NURSERY_TYPE.getId() == id) {
                possibleValues = fieldbookMiddlewareService.getAllNurseryTypes();
            } else if (TermId.EXPERIMENT_DESIGN_FACTOR.getId() == id) {
                possibleValues = filterValues(fieldbookMiddlewareService.getDistinctStandardVariableValues(id), AppConstants.EXPERIMENTAL_DESIGN_POSSIBLE_VALUES.getString());
            } else {
                possibleValues = fieldbookMiddlewareService.getDistinctStandardVariableValues(id);
            }
            possibleValuesCache.addPossibleValues(id, possibleValues);
        }
        return possibleValues;
	}
    
    private List<ValueReference> filterValues(List<ValueReference> possibleValues, String filter) {
        List<ValueReference> filteredValues = new ArrayList<ValueReference>();
        StringTokenizer token = new StringTokenizer(filter, ",");
        
        while (token.hasMoreTokens()) {
            Integer id = Integer.parseInt(token.nextToken());
            for (ValueReference value : possibleValues) {
                if (value.getId().equals(id)) {
                    filteredValues.add(value);
                }
            }
        }
        
        return filteredValues;
    }
	
    @Override
    public List<ValueReference> getAllPossibleValuesFavorite(int id, String projectId) throws MiddlewareQueryException {
        List<ValueReference> possibleValuesFavorite = null;
        if (possibleValuesFavorite == null) {
            if (TermId.BREEDING_METHOD_ID.getId() == id || TermId.BREEDING_METHOD_CODE.getId() == id) {
                List<Integer> methodIds = workbenchService.getFavoriteProjectMethods(projectId);
                List<ValueReference> list = new ArrayList<ValueReference>();
                list.add(new ValueReference(0, AppConstants.PLEASE_CHOOSE.getString(), AppConstants.PLEASE_CHOOSE.getString()));
                possibleValuesFavorite = list;
                possibleValuesFavorite.addAll(getFavoriteBreedingMethods(methodIds, false));
                
            } else if (TermId.LOCATION_ID.getId() == id) {
                List<Long> locationIds = workbenchService.getFavoriteProjectLocationIds(projectId);
                possibleValuesFavorite = convertLocationsToValueReferences(fieldbookMiddlewareService
                        .getFavoriteLocationByProjectId(locationIds));
            }
        }
        return possibleValuesFavorite;
    }
	
    private List<ValueReference> getFavoriteBreedingMethods(List<Integer> projectIdList, boolean isFilterOutGenerative)
            throws MiddlewareQueryException {
        List<ValueReference> list = new ArrayList<ValueReference>();
        List<Method> methods = fieldbookMiddlewareService.getFavoriteBreedingMethods(projectIdList, isFilterOutGenerative);
        if (methods != null && !methods.isEmpty()) {
            for (Method method : methods) {
                if (method != null) {
                    list.add(new ValueReference(method.getMid(), method.getMdesc(), method.getMname() + " - " + method.getMcode()));
                }
            }
        }
        return list;
    }
	
    @Override
    public List<ValueReference> getAllBreedingMethods(boolean isFilterOutGenerative) throws MiddlewareQueryException {
        List<ValueReference> list = new ArrayList<ValueReference>();
        List<Method> methods = fieldbookMiddlewareService.getAllBreedingMethods(isFilterOutGenerative);
        if (methods != null && !methods.isEmpty()) {
            for (Method method : methods) {
                if (method != null) {
                    list.add(new ValueReference(method.getMid(), method.getMdesc(), method.getMname() + " - " + method.getMcode()));
                }
            }
        }
        return list;
    }
	
    private List<ValueReference> convertLocationsToValueReferences(List<Location> locations) {
        List<ValueReference> list = new ArrayList<ValueReference>();
        if (locations != null && !locations.isEmpty()) {
            for (Location loc : locations) {
                if (loc != null) {
                    list.add(new ValueReference(loc.getLocid(), loc.getLname(), loc.getLname()));
                }
            }
        }
        return list;
    }
	
    @Override
    public List<ValueReference> getAllPossibleValuesByPSMR(String property, String scale, String method,
            PhenotypicType phenotypeType) throws MiddlewareQueryException {
        List<ValueReference> list = new ArrayList<ValueReference>();
        Integer standardVariableId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(
                property, scale, method, phenotypeType);
        if (standardVariableId != null)
            list = getAllPossibleValues(standardVariableId.intValue());
        return list;
    }

    private List<ValueReference> convertPersonsToValueReferences(List<Person> persons) {
        List<ValueReference> list = new ArrayList<ValueReference>();
        if (persons != null && !persons.isEmpty()) {
            for (Person person : persons) {
                if (person != null) {
                    list.add(new ValueReference(person.getId(), person.getDisplayName(), person.getDisplayName()));
                }
            }
        }
        return list;
    }

    @Override
    public String getValue(int id, String valueOrId, boolean isCategorical) throws MiddlewareQueryException {
        List<ValueReference> possibleValues = possibleValuesCache.getPossibleValues(id);
        if (!NumberUtils.isNumber(valueOrId) && TermId.BREEDING_METHOD_CODE.getId() != id && TermId.BREEDING_METHOD.getId() != id) {
        	return valueOrId;
        }
        if (possibleValues != null && !possibleValues.isEmpty()) {
        	for (ValueReference possibleValue : possibleValues) {
        		if (possibleValue.equals(valueOrId)) {
        			return possibleValue.getName();
        		}
        	}
        }

        Double valueId = null;
        if (NumberUtils.isNumber(valueOrId)) {
            valueId = Double.valueOf(valueOrId);
        }
        if (TermId.BREEDING_METHOD_ID.getId() == id) {
        	return getBreedingMethodById(valueId.intValue());
        } else if (TermId.BREEDING_METHOD_CODE.getId() == id) {
            return getBreedingMethodByCode(valueOrId);
        } else if (TermId.BREEDING_METHOD.getId() == id) {
            return getBreedingMethodByName(valueOrId);
        } else if (TermId.LOCATION_ID.getId() == id) {
            return getLocationById(valueId.intValue());
        } else if (TermId.PI_ID.getId() == id || Integer.parseInt(AppConstants.COOPERATOR_ID.getString()) == id || TermId.STUDY_UID.getId() == id) {
            return getPersonById(valueId.intValue());
        } else if (isCategorical) {
        	Term term = ontologyService.getTermById(valueId.intValue());
        	if (term != null) {
        		return term.getName();
        	}
        } else {
        	return valueOrId;
        }
        return null;
	}
    
    private String getBreedingMethodById(int id) throws MiddlewareQueryException {
        Method method = fieldbookMiddlewareService.getBreedingMethodById(id);
        if (method != null) {
        	return method.getMname() + " - " + method.getMcode();
        }
        return null;
    }
    
    private String getBreedingMethodByCode(String code) throws MiddlewareQueryException {
        Method method = fieldbookMiddlewareService.getMethodByCode(code);
        if (method != null) {
            return method.getMname() + " - " + method.getMcode();
        }
        return "";
    }
    
    private String getBreedingMethodByName(String name) throws MiddlewareQueryException {
        Method method = fieldbookMiddlewareService.getMethodByName(name);
        if (method != null) {
            return method.getMname() + " - " + method.getMcode();
        }
        return "";
    }
    
    private String getLocationById(int id) throws MiddlewareQueryException {
    	Location location = fieldbookMiddlewareService.getLocationById(id);
    	if (location != null) {
    		return location.getLname();
    	}
    	return null;
    }
    
    @Override
    public String getPersonById(int id) throws MiddlewareQueryException {
    	Person person = fieldbookMiddlewareService.getPersonById(id);
    	if (person != null) {
    		return person.getDisplayName();
    	}
    	return null;
    }
    
    @Override
    public Term getTermById(int termId) throws MiddlewareQueryException {
    	return ontologyService.getTermById(termId);
    }

    @Override
    public void setAllPossibleValuesInWorkbook(Workbook workbook) throws MiddlewareQueryException {
    	List<MeasurementVariable> allVariables = workbook.getAllVariables();
    	if (allVariables != null) {
    		for (MeasurementVariable variable : allVariables) {
    			if (variable.getPossibleValues() == null || variable.getPossibleValues().isEmpty()) {
					Property property = ontologyService.getProperty(variable.getProperty());
					if (property != null && property.getTerm().getId() == TermId.BREEDING_METHOD_PROP.getId()) {
						List<ValueReference> list = new ArrayList<ValueReference>();
						List<Method> methodList = fieldbookMiddlewareService.getAllBreedingMethods(true);
						//since we only need the name for the display
						//special handling for breeding methods
						if (methodList != null && !methodList.isEmpty()) {
			                for (Method method : methodList) {
			                    if (method != null) {
			                        list.add(new ValueReference(method.getMid(), method.getMname() + " - " + method.getMcode(), method.getMname() + " - " + method.getMcode()));
			                    }
			                }
			            }						
                        variable.setPossibleValues(list);
                    }
    			}
    		}
    	}
    }
    
    @Override
    public List<Enumeration> getCheckList() throws MiddlewareQueryException{
    	List<Enumeration> allEnumerations = ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
    	return allEnumerations;
    }
    
    @Override
    public Map<String,String> getIdNamePairForRetrieveAndSave(){
   	 String idNamePairs = AppConstants.ID_NAME_COMBINATION.getString();
   	 StringTokenizer tokenizer = new StringTokenizer(idNamePairs, ",");
   	 Map<String,String> idNameMap = new HashMap<String, String>();
			if(tokenizer.hasMoreTokens()){
				//we iterate it
				while(tokenizer.hasMoreTokens()){
					String pair = tokenizer.nextToken();
					StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
					String idTermId = tokenizerPair.nextToken();
 					String nameTermId = tokenizerPair.nextToken();
 					idNameMap.put(idTermId, nameTermId);
				}
			}
		return idNameMap;
   }
    
    private MeasurementVariable createMeasurementVariable(String idToCreate, String value, Operation operation) throws MiddlewareQueryException {
        StandardVariable stdvar = fieldbookMiddlewareService.getStandardVariable(Integer.valueOf(idToCreate));
        MeasurementVariable var = new MeasurementVariable(
                Integer.valueOf(idToCreate), stdvar.getName(), stdvar.getDescription(), stdvar.getScale().getName(), stdvar.getMethod().getName(),
                stdvar.getProperty().getName(), stdvar.getDataType().getName(), value, stdvar.getPhenotypicType().getLabelList().get(0));
        var.setStoredIn(stdvar.getStoredIn().getId());
        var.setDataTypeId(stdvar.getDataType().getId());
        var.setFactor(false);
        var.setOperation(operation);
        return var;
        
    }
    
    @Override
    public void createIdCodeNameVariablePairs(Workbook workbook, String idCodeNamePairs) throws MiddlewareQueryException{
        Map<String, MeasurementVariable> studyConditionMap = new HashMap<String, MeasurementVariable>();
        if (workbook != null && idCodeNamePairs != null && !idCodeNamePairs.equalsIgnoreCase("")) {
            //we get a map so we can check easily instead of traversing it again
            for(MeasurementVariable var : workbook.getConditions()){
                if(var != null){
                    studyConditionMap.put(Integer.toString(var.getTermId()), var);
                }
            }
            
            StringTokenizer tokenizer = new StringTokenizer(idCodeNamePairs, ",");
            if(tokenizer.hasMoreTokens()){
                //we iterate it
                while(tokenizer.hasMoreTokens()){
                    String pair = tokenizer.nextToken();
                    StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
                    String idTermId = tokenizerPair.nextToken();
                    String codeTermId = tokenizerPair.nextToken();
                    String nameTermId = tokenizerPair.nextToken();
                    
                    if (studyConditionMap.get(codeTermId) == null) {
                        //case when nursery comes from old fieldbook and has id variable saved
                        if (studyConditionMap.get(idTermId) != null) {
                            Method method = studyConditionMap.get(idTermId).getValue().isEmpty() ? null : fieldbookMiddlewareService.getMethodById(Double.valueOf(studyConditionMap.get(idTermId).getValue()).intValue());
                            
                            //add code if it is not yet in the list
                            workbook.getConditions().add(
                                    createMeasurementVariable(codeTermId, 
                                            method == null ? "" : method.getMcode(), 
                                            Operation.ADD));
                            
                            //add name if it is not yet in the list
                            if (studyConditionMap.get(nameTermId) == null) {
                                workbook.getConditions().add(
                                        createMeasurementVariable(nameTermId, 
                                                method == null ? "" : method.getMname(),
                                                Operation.ADD));
                            }
                            
                            //set the correct value of the name and id for update operation
                            for (MeasurementVariable var : workbook.getConditions()) {
                                if (var.getTermId() == Integer.parseInt(nameTermId)) {
                                    var.setValue(method == null ? "" : method.getMname());
                                }
                            }
                        }
                    } else {
                        Method method = null;
                        if (studyConditionMap.get(idTermId) != null) {
                            method = studyConditionMap.get(idTermId).getValue().isEmpty() ? null : fieldbookMiddlewareService.getMethodById(Double.valueOf(studyConditionMap.get(idTermId).getValue()).intValue());
                        } else {
                            method = studyConditionMap.get(codeTermId).getValue().isEmpty() ? null : fieldbookMiddlewareService.getMethodById(Integer.parseInt(studyConditionMap.get(codeTermId).getValue()));
                        }
                        
                        //add name variable if it is not yet in the list
                        if (studyConditionMap.get(nameTermId) == null && studyConditionMap.get(codeTermId).getOperation().equals(Operation.ADD)) {
                            workbook.getConditions().add(
                                    createMeasurementVariable(nameTermId, 
                                            method == null ? "" : method.getMname(), 
                                            Operation.ADD));
                        } 
                        
                        //set correct values of id, code and name before saving
                        if (studyConditionMap.get(nameTermId) != null || studyConditionMap.get(codeTermId) != null || studyConditionMap.get(idTermId) != null){
                            if (workbook.getConditions() != null) {                                
                                for (MeasurementVariable var : workbook.getConditions()) {
                                    if (var.getTermId() == Integer.parseInt(nameTermId)) {
                                        var.setValue(method == null ? "" : method.getMname());
                                    } else if (var.getTermId() == Integer.parseInt(codeTermId)) {
                                        var.setValue(method == null ? "" : method.getMcode());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            SettingsUtil.resetBreedingMethodValueToCode(fieldbookMiddlewareService, workbook.getObservations(), false, ontologyService);
        }
    }
    
    @Override
  	public void createIdNameVariablePairs(Workbook workbook, List<SettingDetail> settingDetails, String idNamePairs, boolean deleteNameWhenIdNotExist) throws MiddlewareQueryException{
  		
  		Map<String, MeasurementVariable> studyConditionMap = new HashMap<String, MeasurementVariable>();
  		Map<String, List<MeasurementVariable>> studyConditionMapList = new HashMap<String, List<MeasurementVariable>>();
  		if(workbook != null && idNamePairs != null && !idNamePairs.equalsIgnoreCase("")){
  			//we get a map so we can check easily instead of traversing it again
  			for(MeasurementVariable var : workbook.getConditions()){
  				if(var != null){
  					studyConditionMap.put(Integer.toString(var.getTermId()), var);
  					List<MeasurementVariable> varList = new ArrayList<MeasurementVariable>();
  					if(studyConditionMapList.get(Integer.toString(var.getTermId())) != null){
  						varList = studyConditionMapList.get(Integer.toString(var.getTermId()));
  					}
  					varList.add(var);
  					studyConditionMapList.put(Integer.toString(var.getTermId()), varList);
  				}
  			}
  		  		
  			StringTokenizer tokenizer = new StringTokenizer(idNamePairs, ",");
  			if(tokenizer.hasMoreTokens()){
  				//we iterate it
  				while(tokenizer.hasMoreTokens()){
  					String pair = tokenizer.nextToken();
  					StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
  					String idTermId = tokenizerPair.nextToken();
  					String nameTermId = tokenizerPair.nextToken();
  					if(studyConditionMap.get(idTermId) != null && studyConditionMap.get(nameTermId) != null){
  						/*means both are existing
  						 * we need to get the value from the id and save it in the name
  						 */
  						MeasurementVariable tempVarId = studyConditionMap.get(idTermId);
  						MeasurementVariable tempVarName = studyConditionMap.get(nameTermId);
  						String actualNameVal = "";
  						if(tempVarId.getValue() != null && !tempVarId.getValue().equalsIgnoreCase("")){
  							List<ValueReference> possibleValues = this.getAllPossibleValues(tempVarId.getTermId());
  							
  							for(ValueReference ref : possibleValues){
  								if(ref.getId() != null && ref.getId().toString().equalsIgnoreCase(tempVarId.getValue())){
  									actualNameVal = ref.getName();
  									break;
  								}
  							}
  						}
  						tempVarId.setName(tempVarName.getName() + AppConstants.ID_SUFFIX.getString());
  						tempVarName.setValue(actualNameVal);	
  						tempVarName.setOperation(tempVarId.getOperation());
  						if(tempVarId.getOperation() != null && Operation.DELETE == tempVarId.getOperation()){
  							if(studyConditionMapList.get(tempVarName.getTermId()) != null){
  								List<MeasurementVariable> varList  = studyConditionMapList.get(tempVarName.getTermId());
  								for(MeasurementVariable var : varList){
  									var.setOperation(Operation.DELETE);
  								}
  							}
  						}
  					}else if(studyConditionMap.get(idTermId) != null && studyConditionMap.get(nameTermId) == null){
  						/*means only id is existing
  						 * we need to create another variable of the name
  						 */
  						MeasurementVariable tempVarId = studyConditionMap.get(idTermId);
  						String actualNameVal = "";
  						if(tempVarId.getValue() != null && !tempVarId.getValue().equalsIgnoreCase("")){
  							List<ValueReference> possibleValues = this.getAllPossibleValues(tempVarId.getTermId());
  							
  							for(ValueReference ref : possibleValues){
  								if(ref.getId() != null && ref.getId().toString().equalsIgnoreCase(tempVarId.getValue())){
  									actualNameVal = ref.getName();
  									break;
  								}
  							}
  						}
  						
  						StandardVariable stdvar = fieldbookMiddlewareService.getStandardVariable(Integer.valueOf(nameTermId));
  						MeasurementVariable tempVarName = new MeasurementVariable(
  								Integer.valueOf(nameTermId), tempVarId.getName(), stdvar.getDescription(), stdvar.getScale().getName(), stdvar.getMethod().getName(),
  								stdvar.getProperty().getName(), stdvar.getDataType().getName(), actualNameVal, stdvar.getPhenotypicType().getLabelList().get(0));
  						tempVarName.setStoredIn(stdvar.getStoredIn().getId());
  						tempVarName.setDataTypeId(stdvar.getDataType().getId());
  						tempVarName.setFactor(false);
  						tempVarId.setName(tempVarId.getName() + AppConstants.ID_SUFFIX.getString());
  						if(tempVarId.getOperation() != Operation.DELETE){
	  						tempVarName.setOperation(Operation.ADD);
	  						workbook.getConditions().add(tempVarName);
	  						workbook.getTrialConditions().add(tempVarName);
	  						 SettingVariable svar = new SettingVariable(
	  								tempVarName.getName(), stdvar.getDescription(), stdvar.getProperty().getName(),
	  								stdvar.getScale().getName(), stdvar.getMethod().getName(), stdvar.getStoredIn().getName(), 
	  								stdvar.getDataType().getName(), stdvar.getDataType().getId(), 
	  								stdvar.getConstraints() != null && stdvar.getConstraints().getMinValue() != null ? stdvar.getConstraints().getMinValue() : null,
	  			            		stdvar.getConstraints() != null && stdvar.getConstraints().getMaxValue() != null ? stdvar.getConstraints().getMaxValue() : null);
	                        svar.setCvTermId(stdvar.getId());
	                        svar.setCropOntologyId(stdvar.getCropOntologyId() != null ? stdvar.getCropOntologyId() : "");
	                        svar.setTraitClass(stdvar.getIsA() != null ? stdvar.getIsA().getName() : "");
	                        svar.setOperation(Operation.UPDATE);
	  						SettingDetail settingDetail = new SettingDetail(svar, null, actualNameVal, true);
	  						settingDetails.add(settingDetail);
  						}
  						
  						//get value only gets the id, we need to get the value here
  						
  					}else if(studyConditionMap.get(idTermId) == null && studyConditionMap.get(nameTermId) != null){
  						/*means only name is existing
  						 * we need to create the variable of the id 
  						 */
  						MeasurementVariable tempVarName = studyConditionMap.get(nameTermId);
  						String actualIdVal = "";
  						if(tempVarName.getValue() != null && !tempVarName.getValue().equalsIgnoreCase("")){
  							List<ValueReference> possibleValues = this.getAllPossibleValues(Integer.valueOf(idTermId));
  							
  							for(ValueReference ref : possibleValues){
  								
  								if(ref.getId() != null && ref.getName().equalsIgnoreCase(tempVarName.getValue())){
  									actualIdVal = ref.getId().toString();
  									break;
  								}
  							}
  						}
  						
  						if(deleteNameWhenIdNotExist){
							//we need to delete the name
  							tempVarName.setOperation(Operation.DELETE);
  							//to be sure, we check all record and mark it as delete
  							if(studyConditionMapList.get(tempVarName.getTermId()) != null){
  								List<MeasurementVariable> varList  = studyConditionMapList.get(tempVarName.getTermId());
  								for(MeasurementVariable var : varList){
  									var.setOperation(Operation.DELETE);
  								}
  							}
  						}else{
  							StandardVariable stdvar = fieldbookMiddlewareService.getStandardVariable(Integer.valueOf(idTermId));
  	  						MeasurementVariable tempVarId = new MeasurementVariable(
  	  								Integer.valueOf(idTermId), tempVarName.getName() + AppConstants.ID_SUFFIX.getString(), stdvar.getDescription(), stdvar.getScale().getName(), stdvar.getMethod().getName(),
  	  								stdvar.getProperty().getName(), stdvar.getDataType().getName(), actualIdVal, stdvar.getPhenotypicType().getLabelList().get(0));
  	  						tempVarId.setStoredIn(stdvar.getStoredIn().getId());
  	  						tempVarId.setDataTypeId(stdvar.getDataType().getId());
  	  						tempVarId.setFactor(false);
  	  						tempVarId.setOperation(Operation.ADD);
  							workbook.getConditions().add(tempVarId);
  							workbook.getTrialConditions().add(tempVarId);
  						}
  					}
  					
  				}
  			}
  		}
  		if(workbook != null && !workbook.isNursery()){
  			//to be only done when it is a trial
  			addConditionsToTrialObservationsIfNecessary(workbook);
  		}
  	}
    
    private void addConditionsToTrialObservationsIfNecessary(Workbook workbook) throws MiddlewareQueryException {
    	if (workbook.getTrialObservations() != null && !workbook.getTrialObservations().isEmpty()
    			&& workbook.getTrialConditions() != null && !workbook.getTrialConditions().isEmpty()) {

			Map<String, String> idNameMap = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
			Set<String> keys = idNameMap.keySet();
			Map<String, String> nameIdMap = new HashMap<String, String>();
			for (String key : keys) {
				String entry = idNameMap.get(key);
				nameIdMap.put(entry, key);
			}
    		int index = 0;
    		for (MeasurementVariable variable : workbook.getTrialConditions()) {
    			for (MeasurementRow row : workbook.getTrialObservations()) {
    				MeasurementData data = row.getMeasurementData(variable.getTermId());
    				if (data == null) {
    					
    					String actualNameVal = "";
    					Integer idTerm = variable.getTermId();
						String pairId = idNameMap.get(String.valueOf(variable.getTermId()));
						if (pairId == null) {
							pairId = nameIdMap.get(String.valueOf(variable.getTermId()));
							idTerm = Integer.valueOf(pairId);
						}
						MeasurementData pairData = row.getMeasurementData(Integer.valueOf(pairId));
						
						MeasurementData idData = row.getMeasurementData(idTerm);
						if (idData != null) {
							List<ValueReference> possibleValues = idData.getMeasurementVariable().getPossibleValues();
						
							for(ValueReference ref : possibleValues){
								if(ref.getId() != null && ref.getId().toString().equalsIgnoreCase(pairData.getValue())){
									actualNameVal = ref.getName();
									break;
								}
							}
						}
  						
    					MeasurementData newData = new MeasurementData(variable.getName(), actualNameVal);
    					newData.setMeasurementVariable(variable);
    					row.getDataList().add(index, newData);
    				}
    			}
    			index++;
    		}
    	}
    }
}
