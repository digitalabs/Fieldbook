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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.PossibleValuesCache;
import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
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
	
	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
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
	public List<ImportedGermplasm> advanceNursery(AdvancingNursery advanceInfo)
	        throws MiddlewareQueryException {

        String namingConvention = advanceInfo.getNamingConvention();

        NamingConventionService service = namingConventionServiceFactory.getNamingConventionService(namingConvention);

	    return service.advanceNursery(advanceInfo);
	}
	
	@Override
	public List<StandardVariableReference> filterStandardVariablesForSetting(Collection<StandardVariable> sourceList, int mode, Collection<SettingDetail> selectedList) {
		List<StandardVariableReference> result = new ArrayList<StandardVariableReference>();
		if (sourceList != null && !sourceList.isEmpty()) {
			Set<Integer> selectedIds = new HashSet<Integer>();
			if (selectedList != null && !selectedList.isEmpty()) {
				for (SettingDetail settingDetail : selectedList) {
					selectedIds.add(settingDetail.getVariable().getCvTermId());
				}
			}
			
			for (StandardVariable var : sourceList) {
				if (isApplicableInCurrentMode(var, mode) && !selectedIds.contains(var.getId())) {
					result.add(new StandardVariableReference(var.getId(), var.getName(), var.getDescription()));
				}
			}
		}
		return result;
	}
	
    private boolean isApplicableInCurrentMode(StandardVariable var, int mode) {
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            return var.getPhenotypicType() == PhenotypicType.STUDY || var.getPhenotypicType() == PhenotypicType.DATASET
                    || var.getPhenotypicType() == PhenotypicType.TRIAL_ENVIRONMENT;
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            return var.getPhenotypicType() == PhenotypicType.TRIAL_ENVIRONMENT
                    || var.getPhenotypicType() == PhenotypicType.TRIAL_DESIGN
                    || var.getPhenotypicType() == PhenotypicType.GERMPLASM;
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt()) {
            return var.getPhenotypicType() == PhenotypicType.VARIATE;
        }
        return false;
    }
	
	@Override
	public List<ValueReference> getAllPossibleValues(int id) throws MiddlewareQueryException {
		List<ValueReference> possibleValues = possibleValuesCache.getPossibleValues(id);
		if (possibleValues == null) {
			
			if (TermId.BREEDING_METHOD.getId() == id) {
				possibleValues = getAllBreedingMethods();
			}
			else if (TermId.TRIAL_LOCATION.getId() == id) {
				possibleValues = convertLocationsToValueReferences(fieldbookMiddlewareService.getAllLocations());
			}
			else if (TermId.PI_NAME.getId() == id) {
				possibleValues = convertPersonsToValueReferences(fieldbookMiddlewareService.getAllPersons());
			}
			else if (TermId.NURSERY_TYPE.getId() == id) {
				possibleValues = fieldbookMiddlewareService.getAllNurseryTypes();
			}
			else {
				possibleValues = fieldbookMiddlewareService.getDistinctStandardVariableValues(id);
			}
			possibleValuesCache.addPossibleValues(id, possibleValues);
		}
		return possibleValues;
	}
	
	private List<ValueReference> getAllBreedingMethods() throws MiddlewareQueryException {
		List<ValueReference> list = new ArrayList<ValueReference>();
		List<Method> methods = fieldbookMiddlewareService.getAllBreedingMethods();
		if (methods != null && !methods.isEmpty()) {
			for (Method method : methods) {
				list.add(new ValueReference(method.getMid(), method.getMname(), method.getMname()));
			}
		}
		return list;
	}
	
	private List<ValueReference> convertLocationsToValueReferences(List<Location> locations) {
		List<ValueReference> list = new ArrayList<ValueReference>();
		if (locations != null && !locations.isEmpty()) {
			for (Location loc : locations) {
				list.add(new ValueReference(loc.getLocid(), loc.getLname(), loc.getLname()));
			}
		}
		return list;
	}
	
	@Override
	public List<ValueReference> getAllPossibleValuesByPSMR(String property,
			String scale, String method, PhenotypicType phenotypeType)
			throws MiddlewareQueryException {
		List<ValueReference> list = new ArrayList<ValueReference>();
		Integer standardVariableId = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(property, scale, method, phenotypeType);
		if(standardVariableId != null)
			list = getAllPossibleValues(standardVariableId.intValue());
		return list;
	}

	private List<ValueReference> convertPersonsToValueReferences(List<Person> persons) {
		List<ValueReference> list = new ArrayList<ValueReference>();
		if (persons != null && !persons.isEmpty()) {
			for (Person person : persons) {
				list.add(new ValueReference(person.getId(), person.getDisplayName()));
			}
		}
		return list;
	}
}
