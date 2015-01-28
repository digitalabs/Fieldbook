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
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.util.ContextUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.SaveListForm;
import com.efficio.fieldbook.web.nursery.bean.ImportedCrosses;
import com.efficio.fieldbook.web.nursery.bean.ImportedCrossesList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;
import com.efficio.fieldbook.web.nursery.service.CrossingService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TreeTableNode;

/**
 * The Class GermplasmTreeController.
 */
@Controller
@RequestMapping(value = "/ListTreeManager")
public class GermplasmTreeController  extends AbstractBaseFieldbookController{

	/** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmTreeController.class);
    
    private static final String GERMPLASM_LIST_TABLE_PAGE = "Common/includes/germplasmListTable";
    public static final String GERMPLASM_LIST_ROOT_NODES = "germplasmListRootNodes";
    private static final String GERMPLASM_LIST_TABLE_ROWS_PAGE = "Common/includes/germplasmListTableRows";
    public static final String GERMPLASM_LIST_CHILD_NODES = "germplasmListChildNodes";
    private static final String LOCAL = "LOCAL";
    private static final String CENTRAL = "CENTRAL";
	
    /** The Constant BATCH_SIZE. */
    public static final int BATCH_SIZE = 50;
	
    
    /** The germplasm list manager. */
    @Resource
    private GermplasmListManager germplasmListManager;
    @Resource
    private GermplasmListManager germplasmDataManager;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    @Resource
    private UserDataManager userDataManager;
    @Resource
    private CrossingService crossingServce;
    
    private static final String NAME_NOT_UNIQUE = "Name not unique";
    private static final String HAS_CHILDREN = "Folder has children";
	private static final String FOLDER = "FOLDER";

	private static final String IS_SUCCESS = "isSuccess";

	private static final String MESSAGE = "message";

	private static final String DATE_FORMAT = "yyyyMMdd";
	
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
                
        		results.put(IS_SUCCESS, 1);
        		results.put("germplasmListId", germplasmListId);
        		results.put("advancedGermplasmListId", advancedId);
        		results.put("uniqueId", form.getListIdentifier());
        		results.put("listName", form.getListName());
        	}else{
        		results.put(IS_SUCCESS, 0);
        		String nameUniqueError = "germplasm.save.list.name.unique.error";
        		Locale locale = LocaleContextHolder.getLocale();
        		results.put(MESSAGE, messageSource.getMessage(
        				nameUniqueError, null, locale));
        	}
        	
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
            results.put(IS_SUCCESS, 0);
            results.put(MESSAGE, e.getMessage());
        }
        
        return results;
    }
    
    
    /**
     * Load initial germplasm tree for crosses.
     *
     * @return the string
     */
    @RequestMapping(value = "/saveCrossesList", method = RequestMethod.GET)
    public String saveList(@ModelAttribute("saveListForm") SaveListForm form,
    		Model model, HttpSession session) {

    	try {
            String listName = "";
            String listDescription = "";
            String listType = "LST"; 
            String listDate = DateUtil.getCurrentDateInUIFormat(); 
            
            if(userSelection.getImportedCrossesList() != null){
                listName = userSelection.getImportedCrossesList().getName();
                listDescription = userSelection.getImportedCrossesList().getTitle();
                listType = userSelection.getImportedCrossesList().getType();
                listDate = DateUtil.showUiDateFormat(userSelection.getImportedCrossesList().getDate());
            }
            
            form.setListName(listName);
            form.setListDescription(listDescription);
            form.setListType(listType);
            form.setListDate(listDate);
            
            form.setListDate(DateUtil.getCurrentDateInUIFormat());
            List<UserDefinedField> germplasmListTypes = germplasmListManager.getGermplasmListTypes();
            //form.setListType(AppConstants.GERMPLASM_LIST_TYPE_HARVEST.getString());
            model.addAttribute("germplasmListTypes", germplasmListTypes);
            
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
        return super.showAjaxPage(model, "Common/saveGermplasmList");
    }
    
    /**
     * Save the crosses list
     *
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/saveCrossesList", method = RequestMethod.POST)
    public Map<String, Object> saveCrossesListPost(@ModelAttribute("saveListForm") SaveListForm form,
    		Model model, HttpSession session) {
    	Map<String,Object> results = new HashMap<String, Object>();
        try {
        	
        	
        	CrossSetting crossSetting = userSelection.getCrossSettings();
        	ImportedCrossesList importedCrosssesList = userSelection.getImportedCrossesList();
        	
        	crossingServce.applyCrossSetting(crossSetting, importedCrosssesList, getCurrentIbdbUserId());
        	
        	GermplasmList germplasmListIsNew = fieldbookMiddlewareService.getGermplasmListByName(form.getListName());
        	if(germplasmListIsNew == null){
        		//we do the saving
        		Map<Germplasm, List<Name>> germplasms = new HashMap<Germplasm, List<Name>>();
                Map<Germplasm, GermplasmListData> listDataItems = new HashMap<Germplasm, GermplasmListData>();
                GermplasmList germplasmList = createCrossesGermplasmList(crossSetting, form, germplasms, listDataItems, importedCrosssesList);
                Integer germplasmListId = fieldbookMiddlewareService.saveNurseryAdvanceGermplasmList(germplasms, listDataItems, germplasmList);

                List<GermplasmListData> data = new ArrayList<GermplasmListData>();
                data.addAll(germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, Integer.MAX_VALUE));                
                List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProjectFromGermplasmListData(data);
                int crossesId = 0;
                if(userSelection.getWorkbook() != null && userSelection.getWorkbook().getStudyDetails() != null && userSelection.getWorkbook().getStudyDetails().getId() != null){
                	crossesId = fieldbookMiddlewareService.saveOrUpdateListDataProject(userSelection.getWorkbook().getStudyDetails().getId(), GermplasmListType.CROSSES, germplasmListId, listDataProject, getCurrentIbdbUserId());
                }
                
        		results.put(IS_SUCCESS, 1);
        		results.put("germplasmListId", germplasmListId);
        		results.put("crossesListId", crossesId);
        		results.put("uniqueId", form.getListIdentifier());
        		results.put("listName", form.getListName());
        	}else{
        		results.put(IS_SUCCESS, 0);
        		String nameUniqueError = "germplasm.save.list.name.unique.error";
        		Locale locale = LocaleContextHolder.getLocale();
        		results.put(MESSAGE, messageSource.getMessage(
        				nameUniqueError, null, locale));
        	}
        	
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
            results.put(IS_SUCCESS, 0);
            results.put(MESSAGE, e.getMessage());
        }
        
        return results;
    }
    
    private GermplasmList createCrossesGermplasmList(CrossSetting crossSetting,
			SaveListForm saveListForm, Map<Germplasm, List<Name>> germplasms,
			Map<Germplasm, GermplasmListData> listDataItems, ImportedCrossesList importedCrossesList) {
		
    	// Create germplasm list
    	AdditionalDetailsSetting additionalDetailsSetting = crossSetting.getAdditionalDetailsSetting();
        String listName =  saveListForm.getListName();
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
        if (saveListForm.getParentId() != null && !LOCAL.equals(saveListForm.getParentId())) {
        	parentId = Integer.valueOf(saveListForm.getParentId());
			try {
				gpList = germplasmListManager.getGermplasmListById(parentId);
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(),e);
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
        String harvestLocationId = additionalDetailsSetting.getHarvestLocationId().toString();
        if (harvestLocationId != null && !"".equals(harvestLocationId)){
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
        for (ImportedCrosses importedCrosses : importedCrossesList.getImportedCrosses()){
   
            Integer gid = null;
            if (importedCrosses.getGid() != null){
                gid = Integer.valueOf(importedCrosses.getGid());
            }
            Integer methodId = importedCrosses.getBreedingMethodId();
            Integer gnpgs = importedCrosses.getGnpgs();
            Integer gpid1 = importedCrosses.getGpid1();
            Integer gpid2 = importedCrosses.getGpid2();
            
            List<Name> names = importedCrosses.getNames();
            Name preferredName = names.get(0);

            for (Name name : names) {
                
                name.setLocationId(locationId);
                name.setNdate(nDate);
                name.setUserId(userId);
                name.setReferenceId(nRef);

                // If crop == CIMMYT WHEAT (crop with more than one name saved)
                // Germplasm name is the Names entry with NType = 1027, NVal = table.desig, NStat = 0
                if (!names.isEmpty() && name.getNstat() == 0
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
            
            Germplasm germplasm = new Germplasm(gid, methodId, gnpgs, gpid1, gpid2
                    , userId, lgid, locationId, 0, preferredName);
            
            germplasms.put(germplasm, names);
      
                    
            // Create list data items to save - Map<Germplasm, GermplasmListData> 
            Integer entryId = importedCrosses.getEntryId();  
            String entryCode = importedCrosses.getEntryCode(); 
            String seedSource = importedCrosses.getSource(); 
            String designation = importedCrosses.getDesig(); 
            String groupName = importedCrosses.getCross(); 
            
            if (groupName == null){
            	// Default value if null
            	groupName = "-"; 
            }
            
            GermplasmListData listData = new GermplasmListData(listDataId, germplasmList, gid, entryId, entryCode, seedSource,
                     designation, groupName, listDataStatus, localRecordId);
            
            listDataItems.put(germplasm, listData);
        }
        
        return germplasmList;

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
        if (saveListForm.getParentId() != null && !LOCAL.equals(saveListForm.getParentId())) {
        	parentId = Integer.valueOf(saveListForm.getParentId());
			try {
				gpList = germplasmListManager.getGermplasmListById(parentId);
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(),e);
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
        if (harvestLocationId != null && !"".equals(harvestLocationId)){
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
                if (!names.isEmpty() && name.getNstat() == 0
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
            
            Integer trueGdate = harvestDate != null && !"".equals(harvestDate.trim()) ? 
            		Integer.valueOf(harvestDate) : gDate;
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
            	// Default value if null
            	groupName = "-"; 
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
    	try {
            List<TreeNode> rootNodes = new ArrayList<TreeNode>();
            TreeNode localNode = new TreeNode(LOCAL, AppConstants.GERMPLASM_LIST_LOCAL.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString());
            TreeNode centralNode = new TreeNode(CENTRAL, AppConstants.GERMPLASM_LIST_CENTRAL.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString());
            rootNodes.add(localNode);
            rootNodes.add(centralNode);
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
    @RequestMapping(value = "/loadInitGermplasmTreeTable", method = RequestMethod.GET)
    public String loadInitialGermplasmTreeTable(Model model) {
    	try {
            List<TreeTableNode> rootNodes = new ArrayList<TreeTableNode>();
            TreeTableNode localNode = new TreeTableNode(
            		LOCAL, AppConstants.GERMPLASM_LIST_LOCAL.getString(), 
            		null, null, null, null, "1");
            TreeTableNode centralNode = new TreeTableNode(
            		CENTRAL, AppConstants.GERMPLASM_LIST_CENTRAL.getString(), 
            		null, null, null, null, "1");
            rootNodes.add(localNode);
            rootNodes.addAll(getGermplasmListFolderChildNodes(localNode));
            rootNodes.add(centralNode);
            rootNodes.addAll(getGermplasmListFolderChildNodes(centralNode));
            model.addAttribute(GERMPLASM_LIST_ROOT_NODES,rootNodes);
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
    	return super.showAjaxPage(model, GERMPLASM_LIST_TABLE_PAGE);
    }
    
    protected void markIfHasChildren(TreeTableNode node) throws MiddlewareQueryException {
    	List<GermplasmList> children = getGermplasmListChildren(node.getId());
    	node.setNumOfChildren(Integer.toString(children.size()));
	}

    protected List<GermplasmList> getGermplasmListChildren(String id) throws MiddlewareQueryException {
		List<GermplasmList> children = new ArrayList<GermplasmList>();
		if (Database.LOCAL.toString().equals(id) 
                || Database.CENTRAL.toString().equals(id)) {
    		children = germplasmListManager
                        .getAllTopLevelListsBatched(BATCH_SIZE, Database.valueOf(id));
        } else if (NumberUtils.isNumber(id)) {
        	int parentId = Integer.valueOf(id);
        	children = germplasmListManager
                    .getGermplasmListByParentFolderIdBatched(parentId, BATCH_SIZE);
        } else {
        	LOG.error("germplasm id = " + id + " is not a number");
        }
		return children;
	}
	
    protected List<TreeTableNode> getGermplasmListFolderChildNodes(TreeTableNode node) throws MiddlewareQueryException{
		List<TreeTableNode> childNodes = getGermplasmListFolderChildNodes(node.getId());
		if(childNodes!=null) {
			node.setNumOfChildren(Integer.toString(childNodes.size()));
		} else {
			node.setNumOfChildren("0");
		}
		return childNodes;
	}

    protected List<TreeTableNode> getGermplasmListFolderChildNodes(String id) throws MiddlewareQueryException{
		List<TreeTableNode> childNodes = new ArrayList<TreeTableNode>();
		if(id!=null && !"".equals(id)){
			childNodes = getGermplasmFolderChildrenNode(id);
			for(TreeTableNode newNode : childNodes){
				markIfHasChildren(newNode);
			}
		}
		return childNodes;
	}
    
    private List<TreeNode> getGermplasmChildNodes(String parentKey, boolean isFolderOnly) throws MiddlewareQueryException{
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		if(parentKey != null && !"".equals(parentKey)){
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
			if(childOfChildNode.isEmpty()) {
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
    
    private List<TreeTableNode> getGermplasmFolderChildrenNode(String id) throws MiddlewareQueryException{
    	return TreeViewUtil.convertGermplasmListToTreeTableNodes(
    			getGermplasmListChildren(id), userDataManager, germplasmListManager);
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
            TreeNode localNode = new TreeNode(LOCAL, AppConstants.GERMPLASM_LIST_LOCAL.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString());
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
    	Map<String, Object> dataResults = new HashMap<String, Object>();
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
     * Expand germplasm list folder.
     *
     * @param id the germplasm list ID
     * @return the response page
     */
    @RequestMapping(value = "/expandGermplasmListFolder/{id}", method = RequestMethod.GET)
    public String expandGermplasmListFolder(@PathVariable String id, Model model) {
    	try {
        	List<TreeTableNode> childNodes = getGermplasmListFolderChildNodes(id);
        	model.addAttribute(GERMPLASM_LIST_CHILD_NODES, childNodes);       
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        
    	return super.showAjaxPage(model, GERMPLASM_LIST_TABLE_ROWS_PAGE);
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

    private void checkIfUnique(String folderName) throws MiddlewareQueryException, MiddlewareException {
    	List<GermplasmList> centralDuplicate = germplasmListManager.
            	getGermplasmListByName(folderName, 0, 1, null, Database.CENTRAL);
        if(centralDuplicate!=null && !centralDuplicate.isEmpty()) {
        	throw new MiddlewareException(NAME_NOT_UNIQUE);
        }
        List<GermplasmList> localDuplicate = germplasmListManager.
            	getGermplasmListByName(folderName, 0, 1, null, Database.LOCAL);
        if(localDuplicate!=null && !localDuplicate.isEmpty()) {
        	throw new MiddlewareException(NAME_NOT_UNIQUE);
        }
        if(folderName.equalsIgnoreCase(AppConstants.GERMPLASM_LIST_LOCAL.getString()) ||
        		folderName.equalsIgnoreCase(AppConstants.GERMPLASM_LIST_CENTRAL.getString())){
        	throw new MiddlewareException(NAME_NOT_UNIQUE);
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
                newList = new GermplasmList(null,folderName,Long.valueOf((new SimpleDateFormat(DATE_FORMAT)).format(Calendar.getInstance().getTime())),FOLDER,userId,folderName,null,0);
            } else {
                gpList = germplasmListManager.getGermplasmListById(Integer.parseInt(id));

                if (gpList != null && !gpList.isFolder()) {
                    GermplasmList parent = null;

                    parent = gpList.getParent();

                    if (parent == null) {
                        newList = new GermplasmList(null,folderName,Long.valueOf((new SimpleDateFormat(DATE_FORMAT)).format(Calendar.getInstance().getTime())),FOLDER,userId,folderName,null,0);
                    } else {
                        newList = new GermplasmList(null,folderName,Long.valueOf((new SimpleDateFormat(DATE_FORMAT)).format(Calendar.getInstance().getTime())),FOLDER,userId,folderName,parent,0);
                    }
                } else {
                    newList = new GermplasmList(null,folderName,Long.valueOf((new SimpleDateFormat(DATE_FORMAT)).format(Calendar.getInstance().getTime())),FOLDER,userId,folderName,gpList,0);
                }

            }

            newList.setDescription("(NEW FOLDER) " + folderName);
            Integer germplasmListFolderId =  germplasmListManager.addGermplasmList(newList);
            resultsMap.put("id",germplasmListFolderId);
            resultsMap.put(IS_SUCCESS, "1");
        } catch (Exception e) {
        	LOG.error(e.getMessage(),e);
            resultsMap.put(IS_SUCCESS, "0");
        	resultsMap.put(MESSAGE, e.getMessage());
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

            resultsMap.put(IS_SUCCESS, "1");
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            resultsMap.put(IS_SUCCESS, "0");
        	resultsMap.put(MESSAGE, e.getMessage());
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
                throw new MiddlewareException(HAS_CHILDREN);
            }
       

       
            germplasmListManager.deleteGermplasmList(gpList);
            resultsMap.put(IS_SUCCESS, "1");
        } catch (Exception e) {
        	LOG.error(e.getMessage(),e);
            resultsMap.put(IS_SUCCESS, "0");
        	resultsMap.put(MESSAGE, e.getMessage());
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
        	LOG.error(e.getMessage(),e);
        }
        return resultsMap;
    }
    

	@Override
	public String getContentName() {
		return null;
	}
	
}
