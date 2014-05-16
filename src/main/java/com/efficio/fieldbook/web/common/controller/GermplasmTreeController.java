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
package com.efficio.fieldbook.web.common.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.form.SaveListForm;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.pojos.treeview.TreeNode;

/**
 * The Class GermplasmTreeController.
 */
@Controller
@RequestMapping(value = "/ListTreeManager")
public class GermplasmTreeController  extends AbstractBaseFieldbookController{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmTreeController.class);
    
    /** The Constant BATCH_SIZE. */
    private static final int BATCH_SIZE = 50;
    
    /** The germplasm list manager. */
    @Resource
    private GermplasmListManager germplasmListManager;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    @Autowired
    private WorkbenchDataManager manager;
    @Resource
    private WorkbenchService workbenchService;
    
    private String NAME_NOT_UNIQUE = "Name not unique";
    private String HAS_CHILDREN = "Folder has children";
    @Resource
    private ResourceBundleMessageSource messageSource;
    
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @RequestMapping(value = "/saveList/{listIdentifier}", method = RequestMethod.GET)
    public String saveList(@ModelAttribute("saveListForm") SaveListForm form,
    		@PathVariable String listIdentifier,
    		Model model, HttpSession session) {

        try {
        	form.setListIdentifier(listIdentifier);
        	List<UserDefinedField> germplasmListTypes = germplasmListManager.getGermplasmListTypes();
        	
        	model.addAttribute("germplasmListTypes", germplasmListTypes);
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return super.showAjaxPage(model, "Common/saveGermplasmList");
    }
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/saveList", method = RequestMethod.POST)
    public Map<String, Object> saveListPost(@ModelAttribute("saveListForm") SaveListForm form,
    		Model model, HttpSession session) {
    	Map<String,Object> results = new HashMap<String, Object>();
        try {
        	GermplasmList germplasmList = fieldbookMiddlewareService.getGermplasmListByName(form.getListName());
        	if(germplasmList == null){
        		//we do the saving
        		results.put("isSuccess", 1);
        	}else{
        		results.put("isSuccess", 0);
        		String nameUniqueError = "germplasm.save.list.name.unique.error";
        		Locale locale = LocaleContextHolder.getLocale();
        		results.put("message", messageSource.getMessage(
        				nameUniqueError, null, locale));
        	}
        	
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
            results.put("isSuccess", 0);
            results.put("message", e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/germplasm/detail/url", method = RequestMethod.GET)
    public String getGermplasmUrl() {

        return AppConstants.GERMPLASM_DETAILS_URL.getString();
    }
    
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
            rootNodes.add(new TreeNode("LOCAL", AppConstants.GERMPLASM_LIST_LOCAL.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString()));
            rootNodes.add(new TreeNode("CENTRAL", AppConstants.GERMPLASM_LIST_CENTRAL.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString()));
            return TreeViewUtil.convertTreeViewToJson(rootNodes);
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/loadInitGermplasmLocalTree", method = RequestMethod.GET)
    public String loadInitialGermplasmLocalTree() {

        try {
            List<TreeNode> rootNodes = new ArrayList<TreeNode>();
            rootNodes.add(new TreeNode("LOCAL", AppConstants.GERMPLASM_LIST_LOCAL.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString()));
            return TreeViewUtil.convertTreeViewToJson(rootNodes);
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/germplasm/list/header/details/{listId}", method = RequestMethod.GET)
    public Map<String, Object> getGermplasmListHeaderDetails(@PathVariable int listId) {
    	HashMap<String, Object> dataResults = new HashMap<String, Object>();
        try {
        	GermplasmList germplasmList = fieldbookMiddlewareService.getGermplasmListById(listId);
        	dataResults.put("name", germplasmList.getName());
        	dataResults.put("description", germplasmList.getDescription());
        	dataResults.put("type", germplasmList.getType());
        	dataResults.put("status", germplasmList.getStatusString());
        	dataResults.put("date", germplasmList.getDate());
        	dataResults.put("owner", fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId()));
        	dataResults.put("notes", germplasmList.getNotes());
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return dataResults;
    }
   
    
    /**
     * Expand germplasm tree.
     *
     * @param parentKey the parent key
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/expandGermplasmTree/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
    public String expandGermplasmTree(@PathVariable String parentKey, @PathVariable String isFolderOnly) {
        
        try {
        	boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
            if (Database.LOCAL.toString().equals(parentKey) 
                    || Database.CENTRAL.toString().equals(parentKey)) {
                List<GermplasmList> rootLists = germplasmListManager
                            .getAllTopLevelListsBatched(BATCH_SIZE, Database.valueOf(parentKey));
                return TreeViewUtil.convertGermplasmListToJson(rootLists, isFolderOnlyBool);
            } 
            else if (NumberUtils.isNumber(parentKey)) {
                int parentId = Integer.valueOf(parentKey);
                List<GermplasmList> childLists = germplasmListManager
                            .getGermplasmListByParentFolderIdBatched(parentId, BATCH_SIZE);
                return TreeViewUtil.convertGermplasmListToJson(childLists, isFolderOnlyBool);
            }
            else {
                LOG.error("parentKey = " + parentKey + " is not a number");
            }
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }

    private void checkIfUnique(String folderName) throws MiddlewareQueryException, Exception {
    	List<GermplasmList> centralDuplicate = germplasmListManager.
            	getGermplasmListByName(folderName, 0, 1, null, Database.CENTRAL);
        if(centralDuplicate!=null && !centralDuplicate.isEmpty()) {
        	throw new Exception(NAME_NOT_UNIQUE);
        }
        List<GermplasmList> localDuplicate = germplasmListManager.
            	getGermplasmListByName(folderName, 0, 1, null, Database.LOCAL);
        if(localDuplicate!=null && !localDuplicate.isEmpty()) {
        	throw new Exception(NAME_NOT_UNIQUE);
        }
	}
    
    @ResponseBody
    @RequestMapping(value = "/addGermplasmFolder", method = RequestMethod.POST)
    public Map<String, Object> addGermplasmFolder(HttpServletRequest req) {
        String id = req.getParameter("parentFolderId");
        String folderName = req.getParameter("folderName");
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		
        
        GermplasmList gpList = null;
        GermplasmList newList = null;
        try {
        	
        	checkIfUnique(folderName);
        	Integer userId =workbenchService.getCurrentIbdbUserId(this.getCurrentProjectId());

            if (id == null) {
                newList = new GermplasmList(null,folderName,Long.valueOf((new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime())),"FOLDER",userId,folderName,null,0);
            }
            else {
                gpList = germplasmListManager.getGermplasmListById(Integer.parseInt(id));

                if (gpList != null && !gpList.isFolder()) {
                    GermplasmList parent = null;

                    parent = gpList.getParent();

                    if (parent == null) {
                        newList = new GermplasmList(null,folderName,Long.valueOf((new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime())),"FOLDER",userId,folderName,null,0);
                    } else {
                        newList = new GermplasmList(null,folderName,Long.valueOf((new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime())),"FOLDER",userId,folderName,parent,0);
                    }
                } else {
                    newList = new GermplasmList(null,folderName,Long.valueOf((new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime())),"FOLDER",userId,folderName,gpList,0);
                }

            }

            newList.setDescription("(NEW FOLDER) " + folderName);
            Integer germplasmListFolderId =  germplasmListManager.addGermplasmList(newList);
            resultsMap.put("isSuccess", "1");
        } catch (Exception e) {
            e.printStackTrace();
            resultsMap.put("isSuccess", "0");
        	resultsMap.put("message", e.getMessage());
        }
        return resultsMap;
    }
    
    @ResponseBody
    @RequestMapping(value = "/renameGermplasmFolder", method = RequestMethod.POST)
    public Map<String, Object> renameStudyFolder(HttpServletRequest req) {
    	Map<String, Object> resultsMap = new HashMap<String, Object>();
    	String newName = req.getParameter("newFolderName");
        String folderId = req.getParameter("folderId");        
        
        try {

            GermplasmList gpList = germplasmListManager.getGermplasmListById(Integer.parseInt(folderId));

            checkIfUnique(newName);
            gpList.setName(newName);

            germplasmListManager.updateGermplasmList(gpList);

            resultsMap.put("isSuccess", "1");
        } catch (Exception e) {
            e.printStackTrace();
            resultsMap.put("isSuccess", "0");
        	resultsMap.put("message", e.getMessage());
        }
        return resultsMap;
    }
    
    public boolean hasChildren(Integer id) throws MiddlewareQueryException {
        return !germplasmListManager.getGermplasmListByParentFolderId(id,0,Integer.MAX_VALUE).isEmpty();
    }
    @ResponseBody
    @RequestMapping(value = "/deleteGermplasmFolder", method = RequestMethod.POST)
    public Map<String, Object> deleteGermplasmFolder(HttpServletRequest req) {
    	Map<String, Object> resultsMap = new HashMap<String, Object>();
       
        GermplasmList gpList = null;
        String folderId = req.getParameter("folderId");
        try {
            gpList = germplasmListManager.getGermplasmListById(Integer.parseInt(folderId));

       
        
            if (hasChildren(gpList.getId())) {
                throw new Exception(HAS_CHILDREN);
            }
       

       
            germplasmListManager.deleteGermplasmList(gpList);
            resultsMap.put("isSuccess", "1");
        } catch (Exception e) {
            e.printStackTrace();
            resultsMap.put("isSuccess", "0");
        	resultsMap.put("message", e.getMessage());
        }
        return resultsMap;
    }
    
    @ResponseBody
    @RequestMapping(value = "/moveGermplasmFolder", method = RequestMethod.POST)
    public Map<String, Object> moveStudyFolder(HttpServletRequest req) {
		 String sourceId =  req.getParameter("sourceId");
		 String targetId =  req.getParameter("targetId");
		 
		 
		 Map<String, Object> resultsMap = new HashMap<String, Object>();     
        
        try {
            GermplasmList gpList = germplasmListManager.getGermplasmListById(Integer.parseInt(sourceId));

            /*if (!gpList.isFolder())
                 throw new Error(NOT_FOLDER);*/

            if (targetId != null) {
                GermplasmList parent = germplasmListManager.getGermplasmListById(Integer.parseInt(targetId));
                            gpList.setParent(parent);
            } else {
                gpList.setParent(null);
            }


            germplasmListManager.updateGermplasmList(gpList);

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            //throw new Error(messageSource.getMessage(Message.ERROR_DATABASE));
        }
        return resultsMap;
    }
    

	@Override
	public String getContentName() {
		// TODO Auto-generated method stub
		return null;
	}
}
