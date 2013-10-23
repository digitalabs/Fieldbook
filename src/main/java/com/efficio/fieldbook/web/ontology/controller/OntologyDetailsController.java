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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OntologyDetailsController {
    
    private static final Logger LOG = LoggerFactory.getLogger(OntologyDetailsController.class);
    
    @Resource
    private OntologyService ontologyService;
    
    @ResponseBody
    @RequestMapping(value = "/OntologyBrowser/details/{variableId}", method = RequestMethod.GET)
    public Map<String, Object> getOntologyDetails(@PathVariable int variableId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            StandardVariable variable = ontologyService.getStandardVariable(variableId);
            if (variable != null && variable.getName() != null && !"".equals(variable.getName())) {
                resultMap.put("status", "success");
                resultMap.put("variable", variable);
                
                NumberFormat numberFormat = NumberFormat.getIntegerInstance();
                resultMap.put("projectCount", 
                        numberFormat.format(ontologyService.countProjectsByVariable(variableId)));
                resultMap.put("observationCount", 
                        numberFormat.format(ontologyService.countExperimentsByVariable(variableId, variable.getStoredIn().getId())));
                
            } else {
                resultMap.put("status", "notfound");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "fail");
        }
        
        return resultMap;
    }
    
}
