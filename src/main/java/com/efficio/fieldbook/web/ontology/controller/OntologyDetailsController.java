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
package com.efficio.fieldbook.web.ontology.controller;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.ontology.form.OntologyDetailsForm;

@Controller
public class OntologyDetailsController extends AbstractBaseFieldbookController {
    
    private static final Logger LOG = LoggerFactory.getLogger(OntologyDetailsController.class);
    
    public static final String DETAILS_TEMPLATE = "/OntologyBrowser/detailTab";
    @Resource
    private OntologyService ontologyService;
    
    
    @RequestMapping(value = "/OntologyBrowser/details/{variableId}", method = RequestMethod.GET)
    public String getOntologyDetails(@PathVariable int variableId,  
            @ModelAttribute("ontologyDetailsForm") OntologyDetailsForm form, Model model) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            StandardVariable variable = ontologyService.getStandardVariable(variableId);
            if (variable != null && variable.getName() != null && !"".equals(variable.getName())) {
                resultMap.put("status", "success");
                resultMap.put("variable", variable);
                
                NumberFormat numberFormat = NumberFormat.getIntegerInstance();
                /*
                resultMap.put("projectCount", 
                        numberFormat.format(ontologyService.countProjectsByVariable(variableId)));
                resultMap.put("observationCount", 
                        numberFormat.format(ontologyService.countExperimentsByVariable(variableId, variable.getStoredIn().getId())));
                */
                form.setProjectCount(numberFormat.format(ontologyService.countProjectsByVariable(variableId)));
                form.setObservationCount(numberFormat.format(ontologyService.countProjectsByVariable(variableId)));
                form.setVariable(variable);
            } else {
                resultMap.put("status", "notfound");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "fail");
        }
        return super.showAjaxPage(model, DETAILS_TEMPLATE);
        //return resultMap;
    }

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
