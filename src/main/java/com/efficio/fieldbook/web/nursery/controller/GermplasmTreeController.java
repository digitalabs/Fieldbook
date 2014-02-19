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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
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

import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.pojos.treeview.TreeNode;

/**
 * The Class GermplasmTreeController.
 */
@Controller
@RequestMapping(value = "/NurseryManager")
public class GermplasmTreeController{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmTreeController.class);
    
    /** The Constant BATCH_SIZE. */
    private static final int BATCH_SIZE = 50;
    
    /** The germplasm list manager. */
    @Resource
    private GermplasmListManager germplasmListManager;
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/loadInitGermplasmTree", method = RequestMethod.GET)
    public String loadInitialGermplasmTree() {

        try {
            List<TreeNode> rootNodes = new ArrayList<TreeNode>();
            rootNodes.add(new TreeNode("LOCAL", AppConstants.GERMPLASM_LIST_LOCAL, true, "lead", Boolean.FALSE));
            rootNodes.add(new TreeNode("CENTRAL", AppConstants.GERMPLASM_LIST_CENTRAL, true, "lead", Boolean.FALSE));
            return TreeViewUtil.convertTreeViewToJson(rootNodes);
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }
    
    /**
     * Expand germplasm tree.
     *
     * @param parentKey the parent key
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/expandGermplasmTree/{parentKey}", method = RequestMethod.GET)
    public String expandGermplasmTree(@PathVariable String parentKey) {
        
        try {
            if (Database.LOCAL.toString().equals(parentKey) 
                    || Database.CENTRAL.toString().equals(parentKey)) {
                List<GermplasmList> rootLists = germplasmListManager
                            .getAllTopLevelListsBatched(BATCH_SIZE, Database.valueOf(parentKey));
                return TreeViewUtil.convertGermplasmListToJson(rootLists);
            } 
            else if (NumberUtils.isNumber(parentKey)) {
                int parentId = Integer.valueOf(parentKey);
                List<GermplasmList> childLists = germplasmListManager
                            .getGermplasmListByParentFolderIdBatched(parentId, BATCH_SIZE);
                return TreeViewUtil.convertGermplasmListToJson(childLists);
            }
            else {
                LOG.error("parentKey = " + parentKey + " is not a number");
            }
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }
    
}
