package com.efficio.fieldbook.web.inventory.controller;

import com.efficio.fieldbook.web.inventory.bean.SeedSelection;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * The Class ManageNurseriesController.
 */
@Controller
@RequestMapping({SeedStoreManagerController.URL})
public class SeedStoreManagerController extends SeedInventoryTableDisplayingController {
    
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
    
    /** The ontology service. */
    @Resource
    private OntologyService ontologyService;

	/**
     * Gets the data types.
     *
     * @return the data types
     */
    @ModelAttribute("locationList")
    public List<Location> getLocationList() {
        try {
            List<Location> dataTypesOrig = fieldbookMiddlewareService.getAllSeedLocations();
            List<Location> dataTypes = dataTypesOrig;
            
            return dataTypes;
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return new ArrayList<>();
    }
    /**
     * Gets the favorite location list.
     *
     * @return the favorite location list
     */
    @ModelAttribute("favoriteLocationList")
    public List<Location> getFavoriteLocationList() {
        try {
            
            List<Long> locationsIds = fieldbookMiddlewareService.getFavoriteProjectLocationIds(this.getCurrentProject().getUniqueID());
            return fieldbookMiddlewareService
                                .getFavoriteLocationByProjectId(locationsIds);
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return new ArrayList<>();
    }
    
    @ModelAttribute("scaleList")
    public List<Scale> getScaleList() {
        try {
            return ontologyService.getAllInventoryScales();
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Shows the manage nurseries screen
     *
     * @param form the manage nurseries form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("seedStoreForm") SeedStoreForm form, Model model, HttpSession session) {   	 
    	return super.show(model);
    }    
    @RequestMapping(value="/ajax/{listId}", method = RequestMethod.GET)
    public String showAjax(@ModelAttribute("seedStoreForm") SeedStoreForm form,@PathVariable Integer listId, Model model, HttpSession session) {
    	form.setListId(listId);
    	return super.showAjaxPage(model, "Inventory/addLotsModal");
    }
        
    @RequestMapping(value="/advance/displayGermplasmDetails/{listId}", method = RequestMethod.GET)
    public String displayAdvanceGermplasmDetails(@PathVariable Integer listId,  
    		@ModelAttribute("seedStoreForm") SeedStoreForm form, HttpServletRequest req,
            Model model) {
        
    	return getInventoryGermplasmDetailsPage(form, listId, model, GermplasmListType.ADVANCED.name(), "/NurseryManager/savedFinalAdvanceList");
    }
    
    @RequestMapping(value="/crosses/displayGermplasmDetails/{listId}", method = RequestMethod.GET)
    public String displayCrossesGermplasmDetails(@PathVariable Integer listId,  
    		@ModelAttribute("seedStoreForm") SeedStoreForm form, HttpServletRequest req,
            Model model) {
        return getInventoryGermplasmDetailsPage(form, listId, model, GermplasmListType.CROSSES.name(), "/NurseryManager/savedFinalCrossesList");
        
    }
    
    protected String getInventoryGermplasmDetailsPage(SeedStoreForm form, Integer listId, Model model, String germplasmListType, String page){
    	try {          
            List<InventoryDetails> inventoryDetailList = inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId, germplasmListType);
            this.getPaginationListSelection().addFinalAdvancedList(listId.toString(), inventoryDetailList);
            
            getSeedSelection().setInventoryList(inventoryDetailList);
            form.setListId(listId);
            form.setInventoryList(inventoryDetailList);
            form.setCurrentPage(1);
            form.setGidList(Integer.toString(listId));                    
            
            model.addAttribute(TABLE_HEADER_LIST, getSeedInventoryTableHeader());
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, page);
    }

	@ResponseBody
    @RequestMapping(value="/save/lots", method = RequestMethod.POST)
    public Map<String, Object> saveLots(@ModelAttribute("seedStoreForm") SeedStoreForm form,
            Model model, Locale local) {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Integer> gidList = new ArrayList<Integer>();

        for (String gid : form.getGidList().split(",")) {
            gidList.add(Integer.parseInt(gid));
        }
        // TODO : for re implementation with inventory ID
        /*try {

            inventoryMiddlewareService.addLotsForList(gidList,
                    form.getInventoryLocationId(), form.getInventoryScaleId(),
                    form.getInventoryComments(), this.getCurrentIbdbUserId(),
                    form.getAmount(), form.getListId());
           
                result.put("message", messageSource
                        .getMessage("seed.inventory.add.lot.save.success", null, local));
                result.put("success", 1);

        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            result.put("message", "error: " + e.getMessage());
            result.put("success", 0);
        }*/
        
        return result;
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
	public void setInventoryMiddlewareService(InventoryService inventoryMiddlewareService) {
		this.inventoryMiddlewareService = inventoryMiddlewareService;
	}
	public void setSeedSelection(SeedSelection seedSelection) {
		this.seedSelection = seedSelection;
	}
	public void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}
	public void setOntologyService(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}
	public void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
