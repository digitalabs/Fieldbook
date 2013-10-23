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

import javax.annotation.Resource;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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
    private OntologyDataManager ontologyDataManager;
    
    @ResponseBody
    @RequestMapping(value = "/OntologyBrowser/details/{variableId}", method = RequestMethod.GET)
    public String getOntologyDetails(@PathVariable int variableId) {
        try {
            StandardVariable variable = ontologyDataManager.getStandardVariable(variableId);
            if (variable != null && variable.getName() != null && !"".equals(variable.getName())) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(variable);
            }
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }
    
}
