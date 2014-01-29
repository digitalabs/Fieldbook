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
package com.efficio.fieldbook.web.demo.controller;

import java.util.List;

import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.util.TreeViewUtil;

@Controller
public class TestTreeViewController extends AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(TestTreeViewController.class);
    
	@Autowired
	private transient StudyDataManager studyDataManager;
	
	@Override
	public String getContentName() {
		return "demo/treeview";
	}

	@RequestMapping(value = "/treeview", method = RequestMethod.GET)
	public String loadInitialTreeView2(Model model) {
		return super.show(model);
	}
	
	@ResponseBody
    @RequestMapping(value = "/loadInitTree", method = RequestMethod.GET)
    public String loadInitialTree() {
        try {
            List<FolderReference> rootFolders = studyDataManager.getRootFolders(Database.LOCAL);
            String jsonResponse = TreeViewUtil.convertFolderReferencesToJson(rootFolders, true);
            LOG.debug(jsonResponse);
            return jsonResponse;
        
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return "[]";
    }

    @ResponseBody
	@RequestMapping(value = "/expandTree/{parentId}", method = RequestMethod.GET)
	public String expandFolder(@PathVariable int parentId) {
		try {
			//TODO: use a generic MW method that returns a List<Reference> instead of 
			//several separate MW calls.
			List<Reference> childNodes = studyDataManager.getChildrenOfFolder(parentId);
//			if (childNodes == null || childNodes.size() == 0) {
//			    childNodes = studyDataManager.getStudiesByFolder(parentId);
//			}
            
			if (childNodes == null || childNodes.isEmpty()) {
                List<DatasetReference> datasets = studyDataManager.getDatasetReferences(parentId);
                return TreeViewUtil.convertDatasetReferencesToJson(datasets);

            } else {
    			return TreeViewUtil.convertReferencesToJson(childNodes);
            }
			
		} catch (Exception e) {
		    LOG.error(e.getMessage());
		}
		return "[]";
	}

}
