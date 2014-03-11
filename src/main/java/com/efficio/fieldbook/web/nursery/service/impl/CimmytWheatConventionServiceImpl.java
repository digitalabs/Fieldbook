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

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.service.CimmytWheatConventionService;
import com.efficio.fieldbook.web.util.AppConstants;

/**
 * 
 * Service implementation for advancing a nursery using the CIMMYT-Wheat convention.
 *
 */
@Service
public class CimmytWheatConventionServiceImpl 
extends AbstractNamingConventionServiceImpl 
implements CimmytWheatConventionService {
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;

    @Override
    protected List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList rows) throws MiddlewareQueryException {
        List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
        String newGermplasmName;
        String suffix = rows.getLocationAbbreviation();
        long start = System.currentTimeMillis();
        int index = 1;
        Map<String, Method> breedingMethodMap = new HashMap();
        List<Method> methodList = fieldbookMiddlewareService.getAllBreedingMethods();
        for(Method method: methodList){
        	breedingMethodMap.put(method.getMid().toString(), method);
        }
       // System.out.println("end breedingMethodMap : " + (System.currentTimeMillis()-start));
        for (AdvancingSource row : rows.getRows()) {
            if (row.getPlantsSelected() != null && row.getGermplasm() != null && !row.isCheck()) {
                String origGermplasmName = getOrigGermplasmName(row);
                if (row.getPlantsSelected().equals(0)) {
                    newGermplasmName = origGermplasmName + "-0" + suffix;
                    addImportedGermplasmToList(list, row, newGermplasmName, 
                            AppConstants.RANDOM_BULK_SF.getInt(), index++, rows.getNurseryName(), breedingMethodMap);
                }
                else if (row.getPlantsSelected().intValue() < 0) {
                    if (row.getGermplasm().getDesig().endsWith("T")) {
                        newGermplasmName = origGermplasmName + "-0" + Math.abs(row.getPlantsSelected()) + "TOP" + suffix;
                    }
                    else {
                        newGermplasmName = origGermplasmName + "-0" + Math.abs(row.getPlantsSelected()) + suffix;
                    }
                    addImportedGermplasmToList(list, row, newGermplasmName, 
                            AppConstants.SELECTED_BULK_SF.getInt(), index++, rows.getNurseryName(), breedingMethodMap);
                }
                else {
                    if (rows.isBulk()) {
                        newGermplasmName = origGermplasmName + "-" + suffix;
                        addImportedGermplasmToList(list, row, newGermplasmName, 
                                rows.getSelectedMethodId(), index++, rows.getNurseryName(), breedingMethodMap);
                    }
                    else {
                        for (int i = 0; i < row.getPlantsSelected(); i++) {
                            newGermplasmName = origGermplasmName + "-" + (i+1) + suffix;
                            addImportedGermplasmToList(list, row, newGermplasmName, 
                                    AppConstants.SINGLE_PLANT_SELECTION_SF.getInt(), 
                                    index++, rows.getNurseryName(), breedingMethodMap);
                        }
                    }
                }
            }
        }
        //System.out.println("end generateGermplasmList : " + (System.currentTimeMillis()-start));
        return list;
    }
    
    private String getOrigGermplasmName(AdvancingSource source) throws MiddlewareQueryException {
        String name = null;
        if (source.getGermplasm() != null && source.getGermplasm().getGid() != null && NumberUtils.isNumber(source.getGermplasm().getGid())) {
            name = fieldbookMiddlewareService.getCimmytWheatGermplasmNameByGid(Integer.valueOf(source.getGermplasm().getGid()));
        }
        if (name == null || "".equals(name)) {
            name = source.getGermplasm().getDesig();
        }
        return name;
    }
    
    @Override
    protected void assignNames(ImportedGermplasm germplasm, AdvancingSource row) {
        List<Name> names = new ArrayList<Name>();
        
        Name name = new Name();
        name.setGermplasmId(Integer.valueOf(row.getGermplasm().getGid()));
        name.setTypeId(GermplasmNameType.CIMMYT_SELECTION_HISTORY.getUserDefinedFieldID());
        name.setNval(germplasm.getDesig());
        name.setNstat(0);
        names.add(name);
        
        name = new Name();
        name.setGermplasmId(Integer.valueOf(row.getGermplasm().getGid()));
        name.setTypeId(GermplasmNameType.UNRESOLVED_NAME.getUserDefinedFieldID());
        name.setNval(germplasm.getDesig());
        name.setNstat(1);
        names.add(name);
        
        name = new Name();
        name.setGermplasmId(Integer.valueOf(row.getGermplasm().getGid()));
        name.setTypeId(GermplasmNameType.CIMMYT_WHEAT_PEDIGREE.getUserDefinedFieldID());
        name.setNval(germplasm.getCross());
        if (germplasm.getCross() == null) {
            name.setNval("-");
        }
        name.setNstat(0);
        names.add(name);
        
        germplasm.setNames(names);
    }
    
}
