package com.efficio.fieldbook.web.nursery.service.impl;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.service.CimmytWheatConventionService;
import com.efficio.fieldbook.web.nursery.service.NamingConventionService;
import com.efficio.fieldbook.web.nursery.service.OtherCropsConventionService;
import com.efficio.fieldbook.web.util.AppConstants;

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
