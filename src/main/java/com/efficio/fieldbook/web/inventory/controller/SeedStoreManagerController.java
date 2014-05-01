package com.efficio.fieldbook.web.inventory.controller;



import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.inventory.bean.SeedSelection;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.form.ManageNurseriesForm;
import com.efficio.fieldbook.web.nursery.form.NurseryDetailsForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;

/**
 * The Class ManageNurseriesController.
 */
@Controller
@RequestMapping({SeedStoreManagerController.URL})
public class SeedStoreManagerController extends AbstractBaseFieldbookController{
    
    private static final Logger LOG = LoggerFactory.getLogger(SeedStoreManagerController.class);
    
    /** The Constant URL. */
    public static final String URL = "/SeedStoreManager";
    public static final String PAGINATION_TEMPLATE = "/Inventory/seedInventoryPagination";

    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private InventoryService inventoryMiddlewareService;
    
    /** The user selection. */
    @Resource
    private SeedSelection seedSelection;
    
    /**
     * Shows the manage nurseries screen
     *
     * @param manageNurseriesForm the manage nurseries form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("seedStoreForm") SeedStoreForm form, Model model, HttpSession session) {
    	 session.invalidate();
    	 /*
        try {
           
        }
        catch (MiddlewareQueryException e){
            LOG.error(e.getMessage(), e);
        }
        */
    	return super.show(model);
    }
    @RequestMapping(value="/displayGermplasmDetails/{listId}", method = RequestMethod.GET)
    public String displayGermplasmDetails(@PathVariable Integer listId,  @ModelAttribute("seedStoreForm") SeedStoreForm form,
            Model model) {
        
        try {
                        
            List<InventoryDetails> inventoryDetailList = inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId);
            
            //for testing
            inventoryDetailList = new ArrayList();
            for(int i = 0 ; i < 300 ; i++){
            	inventoryDetailList.add(new InventoryDetails(i, "Germplasm " + i, null,
            			null, "Location " + i, null,
            			new Integer(0), null, "Source " + i,
            			null, "Scale "+ i));
            }
            
            int index = 0;
            for(InventoryDetails inventoryDetail : inventoryDetailList){
            	inventoryDetail.setIndex(index++);
            }
            
            getSeedSelection().setInventoryList(inventoryDetailList);
            form.setInventoryList(inventoryDetailList);
            //form.changePage(1);
            form.setCurrentPage(1);
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    /**
     * Get for the pagination of the list
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/page/{pageNum}", method = RequestMethod.GET)
    public String getPaginatedList(@PathVariable int pageNum
            , @ModelAttribute("seedStoreForm") SeedStoreForm form, Model model) {
        List<InventoryDetails> inventoryDetailsList = getSeedSelection().getInventoryList();
        if(inventoryDetailsList != null){
            form.setInventoryList(inventoryDetailsList);
            form.setCurrentPage(pageNum);
        }
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "Inventory/main";
    }
    
    /**
     * Gets the form.
     *
     * @return the form
     */
    @ModelAttribute("form")
    public SeedStoreForm getForm() {
        return new SeedStoreForm();
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getUserSelection()
     */
    public SeedSelection getSeedSelection() {
        return this.seedSelection;
    }
    
   
}