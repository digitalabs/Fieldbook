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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.PossibleValuesCache;
import com.efficio.fieldbook.web.nursery.service.NamingConventionService;
import com.efficio.fieldbook.web.nursery.service.impl.NamingConventionServiceFactory;
import com.efficio.fieldbook.web.util.AppConstants;

/**
 * The Class FieldbookServiceImpl.
 */
public class FieldbookServiceImpl implements FieldbookService{
	
	/** The file service. */
	@Resource
    private FileService fileService;
	
	@Autowired
	private NamingConventionServiceFactory namingConventionServiceFactory;
	
    @Autowired
    private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

    @Autowired
    private WorkbenchService workbenchService;
    
    @Autowired
    private OntologyService ontologyService;
	
	@Resource
	private PossibleValuesCache possibleValuesCache;
	

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

        NamingConventionService service = namingConventionServiceFactory.getNamingConventionService(advanceInfo);

	    return service.advanceNursery(advanceInfo, workbook);
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
                                             if(ref.getId().intValue() == TermId.STUDY_TYPE.getId()
                                                             || ref.getId().intValue() == TermId.PM_KEY.getId()
                                                             || ref.getId().intValue() == TermId.TRIAL_INSTANCE_FACTOR.getId()
                                                             || ref.getId().intValue() == TermId.DATASET_NAME.getId()
                                                             || ref.getId().intValue() == TermId.DATASET_TITLE.getId()
                                                             || ref.getId().intValue() == TermId.DATASET_TYPE.getId())
                                                     continue;
                                             
                                     } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
                                             if (inHideVariableFields(ref.getId(), AppConstants.HIDE_PLOT_FIELDS.getString())) {
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
			dbList = fieldbookMiddlewareService.getAllTreatmentLevels();
		}
		else {
			List<Integer> storedInIds = getStoredInIdsByMode(mode, false);
			dbList = fieldbookMiddlewareService.filterStandardVariablesByMode(storedInIds, new ArrayList<Integer>(), false);
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
								 || ref.getId().intValue() == TermId.DATASET_TYPE.getId())
							 continue;
						 
			         } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
		                 if (inHideVariableFields(ref.getId(), AppConstants.HIDE_PLOT_FIELDS.getString())) {
		                     continue;
		                 }
			         } else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
		                 if (inHideVariableFields(ref.getId(), AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString())) {
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
        	if(isNursery)
        		list.addAll(PhenotypicType.TRIAL_ENVIRONMENT.getTypeStorages());
            list.addAll(PhenotypicType.TRIAL_DESIGN.getTypeStorages());
            list.addAll(PhenotypicType.GERMPLASM.getTypeStorages());
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt() 
                || mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
            list.addAll(PhenotypicType.VARIATE.getTypeStorages());
        } else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
            list.addAll(PhenotypicType.TRIAL_ENVIRONMENT.getTypeStorages());
        } else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
        	list.addAll(PhenotypicType.TRIAL_DESIGN.getTypeStorages());
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

            if (TermId.BREEDING_METHOD_ID.getId() == id) {
                possibleValues = getAllBreedingMethods();
            } else if (TermId.LOCATION_ID.getId() == id) {
                possibleValues = convertLocationsToValueReferences(fieldbookMiddlewareService.getAllLocations());
            } else if (TermId.PI_ID.getId() == id || Integer.parseInt(AppConstants.COOPERATOR_ID.getString()) == id) {
                possibleValues = convertPersonsToValueReferences(fieldbookMiddlewareService.getAllPersons());
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
            if (TermId.BREEDING_METHOD_ID.getId() == id) {
                List<Integer> methodIds = workbenchService.getFavoriteProjectMethods(projectId);
                possibleValuesFavorite = getFavoriteBreedingMethods(methodIds);
            } else if (TermId.LOCATION_ID.getId() == id) {
                List<Long> locationIds = workbenchService.getFavoriteProjectLocationIds(projectId);
                possibleValuesFavorite = convertLocationsToValueReferences(fieldbookMiddlewareService
                        .getFavoriteLocationByProjectId(locationIds));
            }
        }
        return possibleValuesFavorite;
    }
	
    private List<ValueReference> getFavoriteBreedingMethods(List<Integer> projectIdList)
            throws MiddlewareQueryException {
        List<ValueReference> list = new ArrayList<ValueReference>();
        List<Method> methods = fieldbookMiddlewareService.getFavoriteBreedingMethods(projectIdList);
        if (methods != null && !methods.isEmpty()) {
            for (Method method : methods) {
                if (method != null) {
                    list.add(new ValueReference(method.getMid(), method.getMname(), method.getMname()));
                }
            }
        }
        return list;
    }
	
    private List<ValueReference> getAllBreedingMethods() throws MiddlewareQueryException {
        List<ValueReference> list = new ArrayList<ValueReference>();
        List<Method> methods = fieldbookMiddlewareService.getAllBreedingMethods();
        if (methods != null && !methods.isEmpty()) {
            for (Method method : methods) {
                if (method != null) {
                    list.add(new ValueReference(method.getMid(), method.getMname(), method.getMname()));
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
        if (!NumberUtils.isNumber(valueOrId)) {
        	return valueOrId;
        }
        if (possibleValues != null && !possibleValues.isEmpty()) {
        	for (ValueReference possibleValue : possibleValues) {
        		if (possibleValue.equals(valueOrId)) {
        			return possibleValue.getName();
        		}
        	}
        }

        Double valueId = Double.valueOf(valueOrId);
        if (TermId.BREEDING_METHOD_ID.getId() == id) {
        	return getBreedingMethodById(valueId.intValue());
        } else if (TermId.LOCATION_ID.getId() == id) {
            return getLocationById(valueId.intValue());
        } else if (TermId.PI_ID.getId() == id || Integer.parseInt(AppConstants.COOPERATOR_ID.getString()) == id) {
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
        	return method.getMname();
        }
        return null;
    }
    
    private String getLocationById(int id) throws MiddlewareQueryException {
    	Location location = fieldbookMiddlewareService.getLocationById(id);
    	if (location != null) {
    		return location.getLname();
    	}
    	return null;
    }
    
    private String getPersonById(int id) throws MiddlewareQueryException {
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
}
