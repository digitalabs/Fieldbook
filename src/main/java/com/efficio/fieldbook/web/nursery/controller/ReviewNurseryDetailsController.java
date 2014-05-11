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
package com.efficio.fieldbook.web.nursery.controller;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.nursery.bean.NurseryDetails;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Controller
@RequestMapping(ReviewNurseryDetailsController.URL)
public class ReviewNurseryDetailsController extends AbstractBaseFieldbookController {

    public static final String URL = "/NurseryManager/reviewNurseryDetails";
    
    private static final Logger LOG = LoggerFactory.getLogger(ReviewNurseryDetailsController.class);

    @Resource
    private UserSelection userSelection;
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
    
    @Override
	public String getContentName() {
		return "NurseryManager/reviewNurseryDetails";
	}

    @RequestMapping(value = "/nursery/{id}", method = RequestMethod.GET)
    public String show(@PathVariable int id, Model model) throws MiddlewareQueryException {
    	
        if (id != 0) {     
            Workbook workbook = fieldbookMiddlewareService.getStudyVariableSettings(id, true);
            workbook.setStudyId(id);
            NurseryDetails details = SettingsUtil.convertWorkbookToNurseryDetails(workbook, fieldbookMiddlewareService, fieldbookService, userSelection);
            
            model.addAttribute("nurseryDetails", details);
        }    	
    	return show(model);
    }
    
    
    
}
