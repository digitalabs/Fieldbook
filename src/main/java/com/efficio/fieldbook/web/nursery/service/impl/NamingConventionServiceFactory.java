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

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;
import com.efficio.fieldbook.web.nursery.service.CimmytWheatConventionService;
import com.efficio.fieldbook.web.nursery.service.MaizeColchicinizeService;
import com.efficio.fieldbook.web.nursery.service.MaizeSelfBulkedService;
import com.efficio.fieldbook.web.nursery.service.MaizeSelfShelledService;
import com.efficio.fieldbook.web.nursery.service.MaizeSibIncreaseService;
import com.efficio.fieldbook.web.nursery.service.NamingConventionService;
import com.efficio.fieldbook.web.nursery.service.OtherCropsConventionService;
import com.efficio.fieldbook.web.util.AppConstants;

/**
 * 
 * Factory for creating Naming Convention Services.
 *
 */
@Service
public class NamingConventionServiceFactory {

    @Resource
    private CimmytWheatConventionService wheatService;
    
    @Resource
    private OtherCropsConventionService otherCropsService;
    
    @Resource
    private MaizeSelfBulkedService maizeSelfBulkedService;
    
    @Resource
    private MaizeSelfShelledService maizeSelfShelledService;
    
    @Resource
    private MaizeSibIncreaseService maizeSibIncreaseService;
    
    @Resource
    private MaizeColchicinizeService maizeColchicinizeService;
    
    
    public NamingConventionService getNamingConventionService(AdvancingNursery advanceInfo) {
    	String namingConvention = advanceInfo.getNamingConvention();
        if (namingConvention != null && NumberUtils.isNumber(namingConvention)) {
            int namingConventionValue = Integer.valueOf(namingConvention);
            if (namingConventionValue == AppConstants.NAMING_CONVENTION_CIMMYT_WHEAT.getInt()) {
                return wheatService;
            } else if (namingConventionValue == AppConstants.NAMING_CONVENTION_CIMMYT_MAIZE.getInt()) {
            	if(AdvancingNurseryForm.maizeBulkMethodId == Integer.valueOf(advanceInfo.getBreedingMethodId())) {
            		return maizeSelfBulkedService;
            	}else if(AdvancingNurseryForm.maizeSelfMethodId == Integer.valueOf(advanceInfo.getBreedingMethodId())) {
            		return maizeSelfShelledService;
            	}else if(AdvancingNurseryForm.maizeSibMethodId == Integer.valueOf(advanceInfo.getBreedingMethodId())) {
            		return maizeSibIncreaseService;
            	}else if(AdvancingNurseryForm.maizeColchMethodId == Integer.valueOf(advanceInfo.getBreedingMethodId())) {
            		return maizeColchicinizeService;
            	}
            } else if (namingConventionValue == AppConstants.NAMING_CONVENTION_OTHER_CROPS.getInt()) {
                return otherCropsService;
            }
        }
        
        return otherCropsService;
    }
    
}
