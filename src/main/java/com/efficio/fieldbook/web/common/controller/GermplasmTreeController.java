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
import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
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
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.SaveListForm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
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
    @Resource
    private UserSelection userSelection;
    
    
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
        	form.setListDate(DateUtil.getCurrentDateInUIFormat());
        	form.setListIdentifier(listIdentifier);
        	List<UserDefinedField> germplasmListTypes = germplasmListManager.getGermplasmListTypes();
        	form.setListType(AppConstants.GERMPLASM_LIST_TYPE_HARVEST.getString());
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
        	AdvancingNurseryForm advancingNurseryForm = getPaginationListSelection().getAdvanceDetails(form.getListIdentifier());
        	
        	
        	GermplasmList germplasmListIsNew = fieldbookMiddlewareService.getGermplasmListByName(form.getListName());
        	if(germplasmListIsNew == null){
        		//we do the saving
        		Map<Germplasm, List<Name>> germplasms = new HashMap<Germplasm, List<Name>>();
                Map<Germplasm, GermplasmListData> listDataItems = new HashMap<Germplasm, GermplasmListData>();
                GermplasmList germplasmList = createNurseryAdvanceGermplasmList(advancingNurseryForm, form, germplasms, listDataItems);
                Integer germplasmListId = fieldbookMiddlewareService.saveNurseryAdvanceGermplasmList(germplasms, listDataItems, germplasmList);

                List<GermplasmListData> data = new ArrayList<GermplasmListData>();
                data.addAll(germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, Integer.MAX_VALUE));                
                List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProjectFromGermplasmListData(data);
                int advancedId = 0;
                if(userSelection.getWorkbook() != null && userSelection.getWorkbook().getStudyDetails() != null && userSelection.getWorkbook().getStudyDetails().getId() != null){
                	advancedId = fieldbookMiddlewareService.saveOrUpdateListDataProject(userSelection.getWorkbook().getStudyDetails().getId(), GermplasmListType.ADVANCED, germplasmListId, listDataProject, getCurrentIbdbUserId());
                }
                
        		results.put("isSuccess", 1);
        		results.put("germplasmListId", germplasmListId);
        		results.put("advancedGermplasmListId", advancedId);
        		results.put("uniqueId", form.getListIdentifier());
        		results.put("listName", form.getListName());
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
     * Creates the nursery advance germplasm list.
     *
     * @param form the form
     * @param germplasms the germplasms
     * @param listDataItems the list data items
     * @return the germplasm list
     */
    
    private GermplasmList createNurseryAdvanceGermplasmList(AdvancingNurseryForm form, SaveListForm saveListForm
                                    , Map<Germplasm, List<Name>> germplasms
                                    , Map<Germplasm, GermplasmListData> listDataItems){
        
        // Create germplasm list
        String listName =  saveListForm.getListName();
        String harvestDate = form.getHarvestYear() + form.getHarvestMonth() + "00"; 
        String listType = saveListForm.getListType(); 
        
        Integer userId = 0;
        try {
            userId = this.getCurrentIbdbUserId();
            if (userId == null){
                userId = 0;
            }
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        String description = saveListForm.getListDescription();
        GermplasmList parent = null;
        Integer parentId = null;
        GermplasmList gpList = null;
        if (saveListForm.getParentId() != null && !"LOCAL".equals(saveListForm.getParentId())) {
        	parentId = Integer.valueOf(saveListForm.getParentId());
			try {
				gpList = germplasmListManager.getGermplasmListById(parentId);
			} catch (MiddlewareQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        if (gpList != null && gpList.isFolder()) {           

            parent = gpList;

        }
        
        Integer status = 1; 
        Long dateLong = Long.valueOf(DateUtil.convertToDBDateFormat(TermId.DATE_VARIABLE.getId(), saveListForm.getListDate()));
        GermplasmList germplasmList = new GermplasmList(null, listName, dateLong, listType, userId,
                description, parent, status, saveListForm.getListNotes());

        //Common germplasm fields
        Integer lgid = 0;
        Integer locationId = 0;
        String harvestLocationId = form.getHarvestLocationId();
        if (harvestLocationId != null && !harvestLocationId.equals("")){
            locationId = Integer.valueOf(harvestLocationId); 
        }
        Integer gDate = Integer.valueOf(DateUtil.getCurrentDate()); 
        
        //Common germplasm list data fields
        Integer listDataId = null; 
        Integer listDataStatus = 0;
        Integer localRecordId = 0;
        
        //Common name fields
        Integer nDate = gDate;
        Integer nRef = 0;

        // Create germplasms to save - Map<Germplasm, List<Name>> 
        for (ImportedGermplasm importedGermplasm : form.getGermplasmList()){
   
            Integer gid = null;
            if (importedGermplasm.getGid() != null){
                gid = Integer.valueOf(importedGermplasm.getGid());
            }
            Integer methodId = importedGermplasm.getBreedingMethodId();
            Integer gnpgs = importedGermplasm.getGnpgs();
            Integer gpid1 = importedGermplasm.getGpid1();
            Integer gpid2 = importedGermplasm.getGpid2();
            
            List<Name> names = importedGermplasm.getNames();
            Name preferredName = names.get(0);

            for (Name name : names) {
                
                name.setLocationId(locationId);
                name.setNdate(nDate);
                name.setUserId(userId);
                name.setReferenceId(nRef);

                // If crop == CIMMYT WHEAT (crop with more than one name saved)
                // Germplasm name is the Names entry with NType = 1027, NVal = table.desig, NStat = 0
                if (names.size() > 0 && name.getNstat() == 0
                        && name.getTypeId() == GermplasmNameType.UNRESOLVED_NAME.getUserDefinedFieldID()) {
                    preferredName = name;
                }
            }
            
            if (names.size() > 1){
                for (Name name : names) {
                    if (name.getTypeId() == GermplasmNameType.UNRESOLVED_NAME.getUserDefinedFieldID()
                            && name.getNstat() == 0) {
                        preferredName = name;
                        break;
                    }
                }
            }
            
            Integer trueGdate = (harvestDate != null && !"".equals(harvestDate.trim()) ? Integer.valueOf(harvestDate) : gDate);
            Germplasm germplasm = new Germplasm(gid, methodId, gnpgs, gpid1, gpid2
                    , userId, lgid, locationId, trueGdate, preferredName);
            
            germplasms.put(germplasm, names);
                    
            // Create list data items to save - Map<Germplasm, GermplasmListData> 
            Integer entryId = importedGermplasm.getEntryId();  
            String entryCode = importedGermplasm.getEntryCode(); 
            String seedSource = importedGermplasm.getSource(); 
            String designation = importedGermplasm.getDesig(); 
            String groupName = importedGermplasm.getCross(); 
            if (groupName == null){
                groupName = "-"; // Default value if null
            }
            
            GermplasmListData listData = new GermplasmListData(listDataId, germplasmList, gid, entryId, entryCode, seedSource,
                     designation, groupName, listDataStatus, localRecordId);
            
            listDataItems.put(germplasm, listData);
        }
        
        return germplasmList;
    }
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/germplasm/detail/url", method = RequestMethod.GET)
    public String getGermplasmUrl() {

        return fieldbookProperties.getGermplasmDetailsUrl();
    }
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/germplasm/import/url", method = RequestMethod.GET)
    public String getImportGermplasmUrl(HttpServletRequest request) {
    	String contextParams = ContextUtil.getContextParameterString(request);
        return fieldbookProperties.getGermplasmImportUrl() + "?" + contextParams;
    }
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/loadInitGermplasmTree/{isFolderOnly}", method = RequestMethod.GET)
    public String loadInitialGermplasmTree(@PathVariable String isFolderOnly) {
    	boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
        try {
            List<TreeNode> rootNodes = new ArrayList<TreeNode>();
            TreeNode localNode = new TreeNode("LOCAL", AppConstants.GERMPLASM_LIST_LOCAL.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString());
            TreeNode centralNode = new TreeNode("CENTRAL", AppConstants.GERMPLASM_LIST_CENTRAL.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString());
            rootNodes.add(localNode);
            rootNodes.add(centralNode);
            return TreeViewUtil.convertTreeViewToJson(rootNodes);
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }
    
    private List<TreeNode> getGermplasmChildNodes(String parentKey, boolean isFolderOnly) throws MiddlewareQueryException{
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		if(parentKey != null && !parentKey.equalsIgnoreCase("")){
			
			try {
	        	
	            if (Database.LOCAL.toString().equals(parentKey) 
	                    || Database.CENTRAL.toString().equals(parentKey)) {
	                List<GermplasmList> rootLists = germplasmListManager
	                            .getAllTopLevelListsBatched(BATCH_SIZE, Database.valueOf(parentKey));
	                childNodes = TreeViewUtil.convertGermplasmListToTreeView(rootLists, isFolderOnly);
	            } else if (NumberUtils.isNumber(parentKey)) {
	                childNodes = getGermplasmChildrenNode(parentKey, isFolderOnly);	                
	            } else {
	                LOG.error("parentKey = " + parentKey + " is not a number");
	            }
	            
	        } catch(Exception e) {
	            LOG.error(e.getMessage(), e);
	        }		
		}

		for(TreeNode newNode : childNodes){
			List<TreeNode> childOfChildNode = getGermplasmChildrenNode(newNode.getKey(), isFolderOnly);
			if(childOfChildNode.size() == 0) {
				newNode.setIsLazy(false);
			}else{
				newNode.setIsLazy(true);
			}
		}
		return childNodes;
	}
    
    private List<TreeNode> getGermplasmChildrenNode(String parentKey, boolean isFolderOnly) throws MiddlewareQueryException{
    	List<TreeNode> childNodes = new ArrayList<TreeNode>();
    	int parentId = Integer.valueOf(parentKey);
    	List<GermplasmList> childLists = germplasmListManager
                .getGermplasmListByParentFolderIdBatched(parentId, BATCH_SIZE);
    	childNodes = TreeViewUtil.convertGermplasmListToTreeView(childLists, isFolderOnly);
    	return childNodes;
    }
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/loadInitGermplasmLocalTree/{isFolderOnly}", method = RequestMethod.GET)
    public String loadInitialGermplasmLocalTree(@PathVariable String isFolderOnly) {
    	boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
        try {
            List<TreeNode> rootNodes = new ArrayList<TreeNode>();
            TreeNode localNode = new TreeNode("LOCAL", AppConstants.GERMPLASM_LIST_LOCAL.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString());
            rootNodes.add(localNode);            
            localNode.setChildren(getGermplasmChildNodes(localNode.getKey(), isFolderOnlyBool));
            
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
        	dataResults.put("type", getTypeString(germplasmList.getType()));
        	
        	String statusValue = "Unlocked List";
    		if(germplasmList.getStatus() >= 100){
    			statusValue = "Locked List";
    		}
    		
        	dataResults.put("status", statusValue);
        	dataResults.put("date", germplasmList.getDate());
        	dataResults.put("owner", fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId()));
        	dataResults.put("notes", germplasmList.getNotes());
        	if(germplasmList.getType() != null && 
        			(germplasmList.getType().equalsIgnoreCase(GermplasmListType.NURSERY.toString()) || 
					germplasmList.getType().equalsIgnoreCase(GermplasmListType.TRIAL.toString())) ||
					germplasmList.getType().equalsIgnoreCase(GermplasmListType.CHECK.toString()) ||
					germplasmList.getType().equalsIgnoreCase(GermplasmListType.ADVANCED.toString())
					){
        		dataResults.put("totalEntries", fieldbookMiddlewareService.countListDataProjectGermplasmListDataByListId(listId));
        	}else{
        		dataResults.put("totalEntries", fieldbookMiddlewareService.countGermplasmListDataByListId(listId));
        	}
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return dataResults;
    }
    private String getTypeString(String typeCode) {
		try{
	        List<UserDefinedField> listTypes = germplasmListManager.getGermplasmListTypes();
	        
	        for (UserDefinedField listType : listTypes) {
	            if(typeCode.equals(listType.getFcode())){
	            	return listType.getFname();
	            }
	        }
		}catch(MiddlewareQueryException ex){
			LOG.error("Error in getting list types.", ex);
			return "Error in getting list types.";
		}
        
        return "Germplasm List";
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
    	boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;        
        try {
        	List<TreeNode> childNodes = getGermplasmChildNodes(parentKey, isFolderOnlyBool);
        	return TreeViewUtil.convertTreeViewToJson(childNodes);            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return "[]";
    }
   
    @ResponseBody
    @RequestMapping(value = "/expandGermplasmTree/{parentKey}", method = RequestMethod.GET)
    public String expandGermplasmAllTree(@PathVariable String parentKey) {
        return expandGermplasmTree(parentKey, "0");
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
        if(folderName.equalsIgnoreCase(AppConstants.GERMPLASM_LIST_LOCAL.getString()) ||
        		folderName.equalsIgnoreCase(AppConstants.GERMPLASM_LIST_CENTRAL.getString())){
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
        	Integer userId = this.getCurrentIbdbUserId();

            if (id == null) {
                newList = new GermplasmList(null,folderName,Long.valueOf((new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime())),"FOLDER",userId,folderName,null,0);
            } else {
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

            if (targetId != null) {
                GermplasmList parent = germplasmListManager.getGermplasmListById(Integer.parseInt(targetId));
                            gpList.setParent(parent);
            } else {
                gpList.setParent(null);
            }


            germplasmListManager.updateGermplasmList(gpList);

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return resultsMap;
    }
    

	@Override
	public String getContentName() {
		// TODO Auto-generated method stub
		return null;
	}
}
