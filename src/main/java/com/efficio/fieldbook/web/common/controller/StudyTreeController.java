package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.pojos.treeview.TreeNode;

@Controller
@RequestMapping(StudyTreeController.URL)
public class StudyTreeController extends AbstractBaseFieldbookController {
	private static final Logger LOG = LoggerFactory.getLogger(StudyTreeController.class);
    public static final String URL = "/StudyTreeManager";
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    @Resource
    private StudyDataManager studyDataManager;
    


	/**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/loadInitialNurseryTree", method = RequestMethod.GET)
    public String loadInitialNurseryTree() {

        try {
            List<TreeNode> rootNodes = new ArrayList<TreeNode>();
            rootNodes.add(new TreeNode("LOCAL", AppConstants.PROGRAM_NURSERIES.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString()));
            rootNodes.add(new TreeNode("CENTRAL", AppConstants.PUBLIC_NURSERIES.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString()));
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
    @RequestMapping(value = "/expandNurseryTree/{parentKey}", method = RequestMethod.GET)
    public String expandGermplasmTree(@PathVariable String parentKey) {
       
        try {
            if (Database.LOCAL.toString().equals(parentKey) 
                    || Database.CENTRAL.toString().equals(parentKey)) {
            	/*
                List<GermplasmList> rootLists = germplasmListManager
                            .getAllTopLevelListsBatched(BATCH_SIZE, Database.valueOf(parentKey));
                return TreeViewUtil.convertGermplasmListToJson(rootLists);
                */
            	 try {
            		 Database instance = Database.LOCAL;
            		 if(Database.CENTRAL.toString().equals(parentKey))
            			 instance = Database.CENTRAL;
            		 
                     List<FolderReference> rootFolders = fieldbookMiddlewareService.getRootFolders(instance);
                     String jsonResponse = TreeViewUtil.convertStudyFolderReferencesToJson(rootFolders, true, false, true, fieldbookMiddlewareService);
                     LOG.debug(jsonResponse);
                     return jsonResponse;
                 
                 } catch (Exception e) {
                     LOG.error(e.getMessage());
                 }
                 return "[]";
            } 
            else if (NumberUtils.isNumber(parentKey)) {
            	
                int parentId = Integer.valueOf(parentKey);
                List<Reference> folders = fieldbookMiddlewareService
                            .getChildrenOfFolder(parentId);
                //convert reference to folder refence
                List<FolderReference> folRefs = TreeViewUtil.convertReferenceToFolderReference(folders);
                
                
                return TreeViewUtil.convertStudyFolderReferencesToJson(folRefs, true, false, true, fieldbookMiddlewareService);
                
            }
            else {
                LOG.error("parentKey = " + parentKey + " is not a number");
            }
            
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
    @RequestMapping(value = "/retrieveChildren/{parentKey}", method = RequestMethod.GET)
    public String retrieveChildren(@PathVariable String parentKey) {
       
        try {
            if (Database.LOCAL.toString().equals(parentKey) 
                    || Database.CENTRAL.toString().equals(parentKey)) {
            	/*
                List<GermplasmList> rootLists = germplasmListManager
                            .getAllTopLevelListsBatched(BATCH_SIZE, Database.valueOf(parentKey));
                return TreeViewUtil.convertGermplasmListToJson(rootLists);
                */
            	 try {
            		 Database instance = Database.LOCAL;
            		 if(Database.CENTRAL.toString().equals(parentKey))
            			 instance = Database.CENTRAL;
            		 
                     List<FolderReference> rootFolders = fieldbookMiddlewareService.getRootFolders(instance);
                     String jsonResponse = TreeViewUtil.convertStudyFolderReferencesToJson(rootFolders, false, true, true, fieldbookMiddlewareService);
                     LOG.debug(jsonResponse);
                     return jsonResponse;
                 
                 } catch (Exception e) {
                     LOG.error(e.getMessage());
                 }
                 return "[]";
            } 
            else if (NumberUtils.isNumber(parentKey)) {
            	
                int parentId = Integer.valueOf(parentKey);
                List<Reference> folders = fieldbookMiddlewareService
                            .getChildrenOfFolder(parentId);
                //convert reference to folder refence
                List<FolderReference> folRefs = TreeViewUtil.convertReferenceToFolderReference(folders);
                
                
                return TreeViewUtil.convertStudyFolderReferencesToJson(folRefs, false, true, true, fieldbookMiddlewareService);
                
            }
            else {
                LOG.error("parentKey = " + parentKey + " is not a number");
            }
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }
    
    @ResponseBody
    @RequestMapping(value = "/addStudyFolder", method = RequestMethod.POST)
    public Map<String, Object> addStudyFolder(HttpServletRequest req) {
        String parentKey = req.getParameter("parentFolderId");
        String folderName = req.getParameter("folderName");
		Map<String, Object> resultsMap = new HashMap<String, Object>();
        try {
        Integer parentFolderId = Integer.parseInt(parentKey);
        if (!TreeViewUtil.isFolder(parentFolderId, fieldbookMiddlewareService)) {
            //get parent
            DmsProject project = studyDataManager.getParentFolder(parentFolderId);
            if (project == null) {
                throw new Error("Parent folder cannot be null");
            }
            parentFolderId = project.getProjectId();
        }
            studyDataManager.addSubFolder(parentFolderId, folderName, folderName);
            resultsMap.put("isSuccess", "1");
        }catch(Exception e){
        	e.printStackTrace();
        	resultsMap.put("isSuccess", "0");
        	resultsMap.put("message", e.getMessage());
        }        
        return resultsMap;
        
    }
    @ResponseBody
    @RequestMapping(value = "/renameStudyFolder", method = RequestMethod.POST)
    public Map<String, Object> renameStudyFolder(HttpServletRequest req) {
    	Map<String, Object> resultsMap = new HashMap<String, Object>();
        try {
            String newFolderName = req.getParameter("newFolderName");
            String folderId = req.getParameter("folderId");

            this.studyDataManager.renameSubFolder(newFolderName, Integer.parseInt(folderId));
            resultsMap.put("isSuccess", "1");
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            resultsMap.put("isSuccess", "0");
        	resultsMap.put("message", e.getMessage());
        }
        return resultsMap;
    }
    
	
    @ResponseBody
    @RequestMapping(value = "/deleteStudyFolder", method = RequestMethod.POST)
    public Map<String, Object> deleteStudyListFolder(HttpServletRequest req) {
    	Map<String, Object> resultsMap = new HashMap<String, Object>();
        try {
        	String folderId = req.getParameter("folderId");
            studyDataManager.deleteEmptyFolder(Integer.parseInt(folderId));
            resultsMap.put("isSuccess", "1");
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            resultsMap.put("isSuccess", "0");
        	resultsMap.put("message", e.getMessage());
        }
        return resultsMap;
    }
    @ResponseBody
    @RequestMapping(value = "/moveStudyFolder", method = RequestMethod.POST)
    public Map<String, Object> moveStudyFolder(HttpServletRequest req) {
		 String sourceId =  req.getParameter("sourceId");
		 String targetId =  req.getParameter("targetId");
		 String isStudy =  req.getParameter("isStudy");
		 boolean isAStudy = "1".equalsIgnoreCase(isStudy) ? true : false;
		 Map<String, Object> resultsMap = new HashMap<String, Object>();
        try {
            studyDataManager.moveDmsProject(Integer.parseInt(sourceId), Integer.parseInt(targetId), isAStudy);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            throw new Error(e.getMessage());
        }
        return resultsMap;
    }

	 
	@Override
	public String getContentName() {
		// TODO Auto-generated method stub
		return null;
	}
}
