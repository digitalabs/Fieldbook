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
package com.efficio.fieldbook.web.nursery.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.service.OtherCropsConventionService;
import com.efficio.fieldbook.web.util.AppConstants;

/**
 * 
 * Service implementation of advancing a nursery using the Other Crops Convention.
 *
 */
@Service
public class OtherCropsConventionServiceImpl 
extends AbstractNamingConventionServiceImpl 
implements OtherCropsConventionService {

	@Resource
	private FieldbookService fieldbookMiddlewareService;
	 
    @Override
    public List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList rows) throws MiddlewareQueryException {
        List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
        String newGermplasmName;
        Integer breedingMethodId = rows.getSelectedMethodId();
        int index = 1;
        Map<String, Method> breedingMethodMap = new HashMap();
        List<Method> methodList = fieldbookMiddlewareService.getAllBreedingMethods();
        for(Method method: methodList){
        	breedingMethodMap.put(method.getMid().toString(), method);
        }
        for (AdvancingSource row : rows.getRows()) {
            if (row.getGermplasm() != null && !row.isCheck() && row.getPlantsSelected() != null && row.getPlantsSelected() > 0) {
                if (rows.isBulk()) {
                    newGermplasmName = row.getGermplasm().getDesig() + "-" + rows.getSuffix();
                    addImportedGermplasmToList(list, row, newGermplasmName, breedingMethodId, index++, rows.getNurseryName(), breedingMethodMap);
                }
                else {
                    for (int i = 0; i < row.getPlantsSelected(); i++) {
                        newGermplasmName = row.getGermplasm().getDesig() + "-" + (i+1) + rows.getSuffix();
                        if (breedingMethodId == null) {
                            breedingMethodId = row.getBreedingMethodId();
                        }
                        addImportedGermplasmToList(list, row, newGermplasmName, breedingMethodId, index++, rows.getNurseryName(), breedingMethodMap);
                    }
                }
            }
        }
            
        return list;
    }

    @Override
    protected void assignNames(ImportedGermplasm germplasm, AdvancingSource source) {
        List<Name> names = new ArrayList<Name>();
        
        Name name = new Name();
        name.setGermplasmId(Integer.valueOf(source.getGermplasm().getGid()));
        if (source.getGermplasm().getBreedingMethodId().equals(
                AppConstants.METHOD_UNKNOWN_DERIVATIVE_METHOD_SF.getInt())) {
            name.setTypeId(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID());
        }
        else {
            if (source.getGermplasm().getBreedingMethodId().equals(
                    AppConstants.METHOD_UNKNOWN_GENERATIVE_METHOD_SF.getInt())
                && source.getGermplasm().getDesig().contains(AppConstants.NAME_SEPARATOR.getString())) {
                name.setTypeId(GermplasmNameType.CROSS_NAME.getUserDefinedFieldID());
            }
            else {
                name.setTypeId(GermplasmNameType.UNNAMED_CROSS.getUserDefinedFieldID());
            }
        }
        name.setNval(germplasm.getDesig());
        name.setNstat(1);
        names.add(name);
        
        germplasm.setNames(names);
    }
}
