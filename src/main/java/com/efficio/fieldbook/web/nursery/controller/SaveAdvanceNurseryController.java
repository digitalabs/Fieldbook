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
package com.efficio.fieldbook.web.nursery.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

/**
 * The Class SaveAdvanceNurseryController.
 */
@Controller
@RequestMapping(SaveAdvanceNurseryController.URL)
public class SaveAdvanceNurseryController extends AbstractBaseFieldbookController{

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/saveAdvanceNursery";
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SaveAdvanceNurseryController.class);
    
    /** The user selection. */
    @Resource
    private AdvancingNursery advancingNursery;

    /** The fieldbook service. */
    @Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

    /** The fieldbook middleware service. */
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    /** The workbench data manager. */
    @Resource
    private WorkbenchService workbenchService;
    
    /** The message source. */
    @Resource
    private ResourceBundleMessageSource messageSource;

    /** The imported germplasm list. */
    private List<ImportedGermplasm> importedGermplasmList;
     
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/saveAdvanceNursery";
    }    
    
    /**
     * Shows the screen.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("advancingNurseryform") AdvancingNurseryForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{
        importedGermplasmList = fieldbookService.advanceNursery(advancingNursery);
        form.setGermplasmList(importedGermplasmList);
        form.setEntries(importedGermplasmList.size());
    	return super.show(model);
    }
       
    /**
     * Post advance nursery.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the map
     * @throws MiddlewareQueryException the middleware query exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public Map<String, String> postAdvanceNursery(@ModelAttribute("advancingNurseryform") AdvancingNurseryForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{
    	
        Map<String, String> resultMap = new HashMap<String, String>();

        try {
            String errorMessages = validate(form);
            if (errorMessages != null) {                
                resultMap.put("status", "-1");
                resultMap.put("errorMessage", errorMessages);
                return resultMap;
            }
            
            Map<Germplasm, List<Name>> germplasms = new HashMap<Germplasm, List<Name>>();
            Map<Germplasm, GermplasmListData> listDataItems = new HashMap<Germplasm, GermplasmListData>();
            GermplasmList germplasmList = createNurseryAdvanceGermplasmList(form, germplasms, listDataItems);
            fieldbookMiddlewareService.saveNurseryAdvanceGermplasmList(germplasms, listDataItems, germplasmList);
            resultMap.put("status", "1");

        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());
        }
        
        return resultMap;
    }
    
    /**
     * Validate.
     *
     * @param form the form
     * @return the string
     */
    private String validate(AdvancingNurseryForm form) {
        Locale locale = LocaleContextHolder.getLocale();
        StringBuilder errorMessages = null;
        
        //Required Field Checks
        StringBuilder requiredFields = null;
        
        // Advance list name and description are required
        if (StringUtils.isBlank(form.getNurseryAdvanceName())) {
            requiredFields = requiredFields == null ? new StringBuilder() : requiredFields.append(", ");
            requiredFields.append(messageSource.getMessage(
                    "nursery.save.advance.nursery.nursery.advance.name", null, locale));
        }
        if (StringUtils.isBlank(form.getNurseryAdvanceDescription())) {
            requiredFields = requiredFields == null ? new StringBuilder() : requiredFields.append(", ");
            requiredFields.append(messageSource.getMessage(
                    "nursery.save.advance.nursery.description", null, locale));
        }
        
        if (requiredFields != null) {
            errorMessages = errorMessages == null ? new StringBuilder() : errorMessages.append("<br />");
            errorMessages.append(messageSource.getMessage("error.mandatory.field"
                    , new String[] {requiredFields.toString()}, locale));
        }

        // Advance list name must not exist in listnms
        try {
            if (fieldbookMiddlewareService.getGermplasmListByName(form.getNurseryAdvanceName()) != null){
                if (errorMessages == null){
                    errorMessages = new StringBuilder();
                }
                if (!errorMessages.toString().contains(messageSource.getMessage(
                        "nursery.save.advance.nursery.nursery.advance.name", null, locale))) {
                    errorMessages.append("<br />");
                    errorMessages.append(messageSource.getMessage(
                            "nursery.save.advance.error.advance.nursery.exists" , null, locale));
                }
            }
        } catch (NoSuchMessageException e) {
            LOG.error(e.getMessage(), e);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        // Advance list table should not be empty
        if (importedGermplasmList == null || importedGermplasmList.isEmpty()){
            errorMessages = errorMessages == null ? new StringBuilder() : errorMessages.append("<br />");
            errorMessages.append(messageSource.getMessage("nursery.save.advance.error.row.list.empty" , null, locale));
        }

        return errorMessages != null ? errorMessages.toString() : null;
    }
    
    /**
     * Creates the nursery advance germplasm list.
     *
     * @param form the form
     * @param germplasms the germplasms
     * @param listDataItems the list data items
     * @return the germplasm list
     */
    private GermplasmList createNurseryAdvanceGermplasmList(AdvancingNurseryForm form
                                    , Map<Germplasm, List<Name>> germplasms
                                    , Map<Germplasm, GermplasmListData> listDataItems){
        
        // Create germplasm list
        String listName =  form.getNurseryAdvanceName();
        String harvestDate = advancingNursery.getHarvestDate(); 
        String listType = AppConstants.GERMPLASM_LIST_TYPE_HARVEST;
        Integer userId = 0;
        try {
            userId = workbenchService.getCurrentIbdbUserId(getCurrentProjectId());
            if (userId == null){
                userId = 0;
            }
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        String description = form.getNurseryAdvanceDescription();
        GermplasmList parent = null; //lhierarchy = 0
        Integer status = 1; 
        GermplasmList germplasmList = new GermplasmList(null, listName, Long.valueOf(harvestDate), listType, userId,
                description, parent, status);

        //Common germplasm fields
        Integer lgid = 0;
        Integer locationId = 0;
        String harvestLocationId = advancingNursery.getHarvestLocationId();
        if (harvestLocationId != null && !harvestLocationId.equals("")){
            locationId = Integer.valueOf(harvestLocationId); 
        }
        Integer gDate = Integer.valueOf(DateUtil.getCurrentDate()); 
        
        //Common germplasm list data fields
        Integer listDataId = null; 
        Integer listDataStatus = 0; //lrstatus = 0
        Integer localRecordId = 0; //llrecid = 0
        
        //Common name fields
        Integer nDate = gDate;
        Integer nRef = 0;

        // Create germplasms to save - Map<Germplasm, List<Name>> 
        for (ImportedGermplasm importedGermplasm : importedGermplasmList){
   
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
            
            Germplasm germplasm = new Germplasm(gid, methodId, gnpgs, gpid1, gpid2
                    , userId, lgid, locationId, gDate, preferredName);
            
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
    
}