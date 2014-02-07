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
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

/**
 * The Class FieldbookServiceImpl.
 */
public class FieldbookServiceImpl implements FieldbookService{
	
	/** The file service. */
	@Resource
    private FileService fileService;
	
	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
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
	
	
	public List<ImportedGermplasm> advanceNursery(AdvancingNursery advanceInfo)
	        //int nurseryId, int namingConvention, String suffix, 
	        //Integer selectedMethod, String locationAbbreviation) 
	        throws MiddlewareQueryException {

	    return null;
	    /*
	    int nurseryId = advanceInfo.getStudy().getId();
	    String namingConvention = advanceInfo.getNamingConvention();
	    String suffix = advanceInfo.getSuffixConvention();
	    Integer selectedMethod = getIntegerValue(advanceInfo.getBreedingMethodId());
	    String locationAbbreviation = advanceInfo.getHarvestLocationAbbreviation();
	    
	    Workbook workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
	    AdvancingSourceList rows = new AdvancingSourceList(workbook);
	    Study nursery = fieldbookMiddlewareService.getStudy(nurseryId);
	    if (nursery.getConditions() != null && nursery.getConditions().size() > 0) {
	        Variable breedingMethod = nursery.getConditions().findById(TermId.BREEDING_METHOD_ID.getId());
	        if (breedingMethod != null && breedingMethod.getValue() != null && NumberUtils.isNumber(breedingMethod.getValue())) {
	            rows.setNurseryBreedingMethodId(Integer.valueOf(breedingMethod.getValue()));
	        }
	    }
	    rows.setSuffix(suffix);
	    rows.setSelectedMethodId(selectedMethod);
	    rows.setLocationAbbreviation(locationAbbreviation);
	    
	    NamingConventionService service = null;
	    //if (namingConvention.equals("CIMMYT-WHEAT")) {
	        service = new CimmytWheatConventionServiceImpl();
	    //}
	    return service.generateGermplasmList(rows);
	    */
	}
	
	private Integer getIntegerValue(String value) {
	    if (value != null && NumberUtils.isNumber(value)) {
	        return Integer.valueOf(value);
	    }
	    return null;
	}
}
