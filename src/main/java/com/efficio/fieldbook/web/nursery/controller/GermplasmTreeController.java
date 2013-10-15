/* Copyright (c) 2013, All Rights Reserved.
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

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.util.TreeViewUtil;

@Controller
@RequestMapping(value = "/NurseryManager")
public class GermplasmTreeController{

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmTreeController.class);
    
    @Resource
    private GermplasmListManager germplasmListManager;
    
    @ResponseBody
    @RequestMapping(value = "/loadInitGermplasmTree/{dbInstance}", method = RequestMethod.GET)
    public String loadInitialGermplasmTree(@PathVariable String dbInstance) {

        try {
            int count = (int) germplasmListManager.countAllGermplasmLists();
            if (count > 0) {
                List<GermplasmList> germplasmLists = germplasmListManager.getAllGermplasmLists(0, count, Database.valueOf(dbInstance));
                String jsonResponse = TreeViewUtil.convertGermplasmListToJson(germplasmLists);
                return jsonResponse;
            }
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }
    
    @ResponseBody
    @RequestMapping(value = "/expandGermplasmTree/{dbInstance}/{parentId}", method = RequestMethod.GET)
    public String expandGermplasmTree(@PathVariable String dbInstance, @PathVariable int parentId) {
        //TODO: NOT IMPLEMENTED YET
        return "[]";
    }
    
}
