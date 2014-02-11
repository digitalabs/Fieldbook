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

import com.efficio.fieldbook.web.nursery.service.CimmytWheatConventionService;
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
    
    
    public NamingConventionService getNamingConventionService(String namingConvention) {
        if (namingConvention != null && NumberUtils.isNumber(namingConvention)) {
            int namingConventionValue = Integer.valueOf(namingConvention);
            switch (namingConventionValue) {
                case AppConstants.NAMING_CONVENTION_CIMMYT_WHEAT : return wheatService;
                case AppConstants.NAMING_CONVENTION_CIMMYT_MAIZE : return otherCropsService;
                case AppConstants.NAMING_CONVENTION_OTHER_CROPS  : return otherCropsService;
            }
        }
        
        return otherCropsService;
    }
    
}
