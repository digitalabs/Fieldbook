package com.efficio.fieldbook.web.stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.exceptions.StockException;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.commons.service.StockService;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.form.ImportStockForm;
import com.efficio.fieldbook.web.common.service.ImportInventoryService;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;
import com.efficio.fieldbook.web.util.parsing.InventoryHeaderLabels;
import com.efficio.fieldbook.web.util.parsing.InventoryImportParser;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 4/24/2015
 * Time: 4:38 PM
 */
@Controller
@RequestMapping(value = StockController.URL)
public class StockController extends AbstractBaseFieldbookController{
	private static final Logger LOG = LoggerFactory.getLogger(StockController.class);

	public static final String URL = "/stock";
	public static final String IS_SUCCESS = "isSuccess";
	public static final String FAILURE = "0";
	public static final String SUCCESS = "1";
	public static final String ERROR_MESSAGE = "errorMessage";
	public static final String HAS_ERROR = "hasError";

	@Resource
	private StockService stockService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private InventoryService inventoryService;

	@Resource
	private InventoryDataManager inventoryDataManager;

	@Resource
	private GermplasmListManager germplasmListManager;
	
	@Resource
	private ImportInventoryService importInventoryService;
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
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
            return fieldbookMiddlewareService.getAllSeedLocations();
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

	@ResponseBody
	@RequestMapping(value = "/retrieveNextStockPrefix", method = RequestMethod.POST)
	public Map<String, String> retrieveNextStockIDPrefix(@RequestBody StockListGenerationSettings generationSettings) {
		Map<String, String> resultMap = new HashMap<>();

		Integer validationResult = generationSettings.validateSettings();

		if (! validationResult.equals(StockListGenerationSettings.VALID_SETTINGS)) {
			return prepareValidationErrorMessages(validationResult);
		}

		try {
			String prefix = stockService.calculateNextStockIDPrefix(generationSettings.getBreederIdentifier(), generationSettings.getSeparator());
			// for UI purposes, we remove the separator from the generated prefix
			prefix = prefix.substring(0, prefix.length() -1);
			resultMap.put(IS_SUCCESS, SUCCESS);
			resultMap.put("prefix", prefix);

		} catch (MiddlewareException e) {
			LOG.error(e.getMessage(), e);
			resultMap.put(IS_SUCCESS, FAILURE);
		}

		return resultMap;
	}

	@RequestMapping(value ="/generateStockTabIfNecessary/{listId}", method = RequestMethod.GET)
	public String generateStockTabIfNecessary(@PathVariable Integer listId, Model model) {

		try {
			boolean transactionsExist = inventoryDataManager.transactionsExistForListProjectDataListID(listId);
			if (transactionsExist) {
				GermplasmList germplasmList = germplasmListManager.getGermplasmListById(listId);


				model.addAttribute("listName", germplasmList.getName());
				model.addAttribute("listId", listId);

				return "/NurseryManager/stockTab";
			}

		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}

		return "/NurseryManager/blank";
	}

	protected Map<String, String> prepareValidationErrorMessages(Integer validationResult) {
		Map<String, String> resultMap = new HashMap<>();
		resultMap.put(IS_SUCCESS, FAILURE);
		switch (validationResult){
			case StockListGenerationSettings.NUMBERS_FOUND :
				resultMap.put(ERROR_MESSAGE, messageSource.getMessage(
						"stock.generate.id.breeder.identifier.error.numbers.found", new Object[]{},
						Locale.getDefault()));
				break;
			case StockListGenerationSettings.SPACE_FOUND:
				resultMap.put(ERROR_MESSAGE, messageSource.getMessage(
						"stock.generate.id.breeder.identifier.error.space.found", new Object[]{},
						Locale.getDefault()));
				break;
			default : break;
		}

		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/generateStockList/{listId}", method = RequestMethod.POST)
	public Map<String, String> generateStockList(@RequestBody StockListGenerationSettings generationSettings,
												 @PathVariable("listId") Integer listDataProjectListId) {
		Map<String, String> resultMap = new HashMap<>();
		Integer validationResult = generationSettings.validateSettings();

		if (! validationResult.equals(StockListGenerationSettings.VALID_SETTINGS)) {
			return prepareValidationErrorMessages(validationResult);
		}

		try {
			Integer listDataID = germplasmListManager.retrieveDataListIDFromListDataProjectListID(listDataProjectListId);
			Map<ListDataProject, GermplasmListData> germplasmMap = generateGermplasmMap(listDataID, listDataProjectListId);

			String prefix = stockService.calculateNextStockIDPrefix(generationSettings.getBreederIdentifier(), generationSettings.getSeparator());
			Map<Integer, InventoryDetails> inventoryDetailMap = new HashMap<>();

			for (Map.Entry<ListDataProject, GermplasmListData> entry : germplasmMap.entrySet()) {
                InventoryDetails details = new InventoryDetails();
                details.setAmount(0d);
                details.setLocationId(null);
                details.setScaleId(null);
                details.setUserId(getCurrentIbdbUserId());
                details.setGid(entry.getKey().getGermplasmId());
                details.setSourceId(listDataID);
                details.setInventoryID(prefix + entry.getKey().getEntryId());

                inventoryDetailMap.put(entry.getKey().getEntryId(), details);
            }

			if (generationSettings.hasBulkInstructions()){
				stockService.processBulkSettings(germplasmMap.keySet(), inventoryDetailMap,
						generationSettings.isAddPedigreeDuplicate(), generationSettings.isAddPlotReciprocal(),
						generationSettings.isAddPedigreeReciprocal());
			}

			for (Map.Entry<ListDataProject, GermplasmListData> entry : germplasmMap.entrySet()) {
				ListDataProject project = entry.getKey();
				GermplasmListData data = entry.getValue();
				InventoryDetails details = inventoryDetailMap.get(project.getEntryId());
				inventoryService.addLotAndTransaction(details, data, project);

			}

			resultMap.put(IS_SUCCESS, SUCCESS);
		} catch (MiddlewareException e) {
			LOG.error(e.getMessage(), e);
			resultMap.put(IS_SUCCESS, FAILURE);
			resultMap.put(ERROR_MESSAGE, messageSource.getMessage(
					"common.default.error", new Object[]{},
					Locale.getDefault()));
		}

		return resultMap;
	}

	protected Map<ListDataProject, GermplasmListData> generateGermplasmMap(Integer listDataID, Integer listDataProjectListId) throws MiddlewareException {
		List<GermplasmListData> germplasmListDataList = germplasmListManager.getGermplasmListDataByListId(listDataID, 0, Integer.MAX_VALUE);
		List<ListDataProject> listDataProjectList = germplasmListManager.retrieveSnapshotListData(listDataProjectListId);

		Map<ListDataProject, GermplasmListData> germplasmMap = new HashMap<>();

		for (ListDataProject listDataProject : listDataProjectList) {
			boolean matchFound = false;
			for (GermplasmListData germplasmListData : germplasmListDataList) {
				if (germplasmListData.getEntryId().equals(listDataProject.getEntryId())) {
					germplasmMap.put(listDataProject, germplasmListData);
					matchFound = true;
					break;
				}
			}

			if (!matchFound) {
				germplasmMap.put(listDataProject, null);
			}

		}

		return germplasmMap;
	}
	
	@ResponseBody
	@RequestMapping(value ="/import", method = RequestMethod.POST)
	public Map<String,Object> importList(@ModelAttribute("importStockForm") ImportStockForm form) {
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			Integer listId = form.getStockListId();
			GermplasmList germplasmList = this.fieldbookMiddlewareService
					.getGermplasmListById(listId);
			GermplasmListType germplasmListType = GermplasmListType.valueOf(germplasmList.getType());
			Map<String,Object> additionalParams = new HashMap<String,Object>();
			additionalParams.put(InventoryImportParser.HEADERS_MAP_PARAM_KEY, 
					InventoryHeaderLabels.headers(germplasmListType));
			additionalParams.put(InventoryImportParser.LIST_ID_PARAM_KEY,listId);
			ImportedInventoryList importedInventoryList = 
					importInventoryService.parseFile(form.getFile(),additionalParams);
			List<InventoryDetails> inventoryDetailListFromDB = 
					inventoryService.getInventoryListByListDataProjectListId(listId, germplasmListType);
			importInventoryService.mergeInventoryDetails(
					inventoryDetailListFromDB, importedInventoryList, germplasmListType);
			updateInventory(listId,inventoryDetailListFromDB);
			result.put(HAS_ERROR,false);
			result.put("stockListId",listId);
		} catch (FileParsingException e) {
			LOG.error(e.getMessage(),e);
			result.put(HAS_ERROR,true);
			result.put(ERROR_MESSAGE,
					messageSource.getMessage(e.getMessage(),
							e.getMessageParameters(), Locale.getDefault()));
		} catch (FieldbookException e) {
			LOG.error(e.getMessage(),e);
			result.put(HAS_ERROR,true);
			result.put(ERROR_MESSAGE,e.getMessage());
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
			result.put(HAS_ERROR,true);
			result.put(ERROR_MESSAGE,messageSource.getMessage(
					"common.import.failed", new Object[]{},Locale.getDefault()));
		}
		return result;
	}

	private void updateInventory(Integer listId,
			List<InventoryDetails> inventoryDetailListFromDB) throws MiddlewareQueryException {
		inventoryDataManager.updateInventory(listId,inventoryDetailListFromDB);
	}
	
	@ResponseBody
	@RequestMapping(value ="/executeBulkingInstructions/{listId}", method = RequestMethod.POST)
	public Map<String,Object> executeBulkingInstructions(@PathVariable Integer listId) {
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			GermplasmList germplasmList = this.fieldbookMiddlewareService
					.getGermplasmListById(listId);
			GermplasmListType germplasmListType = GermplasmListType.valueOf(germplasmList.getType());
			List<InventoryDetails> inventoryDetailsList = 
					inventoryService.getInventoryListByListDataProjectListId(listId, germplasmListType);
			stockService.verifyIfBulkingForStockListCanProceed(listId, inventoryDetailsList);
			stockService.executeBulkingInstructions(inventoryDetailsList);
			result.put(HAS_ERROR,false);
			result.put("stockListId",listId);
		} catch (StockException e) {
			LOG.error(e.getMessage(),e);
			result.put(HAS_ERROR,true);
			result.put(ERROR_MESSAGE,
					messageSource.getMessage(e.getMessage(),
							e.getMessageParameters(), Locale.getDefault()));
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
			result.put(HAS_ERROR,true);
			result.put(ERROR_MESSAGE,messageSource.getMessage(
					"error.bulking.duplicates.and.reciprocals.failed", new Object[]{},Locale.getDefault()));
		}
		return result;
	}
	

    @RequestMapping(value="/ajax/{listId}/{entryIdList}", method = RequestMethod.GET)
    public String showAjax(@ModelAttribute("seedStoreForm") SeedStoreForm form,
    		@PathVariable Integer listId, @PathVariable String entryIdList, Model model, HttpSession session) {
    	form.setListId(listId);
    	form.setEntryIdList(entryIdList);
    	return super.showAjaxPage(model, "Inventory/addLotsModal");
    }
    
	@ResponseBody
    @RequestMapping(value="/update/lots", method = RequestMethod.POST)
    public Map<String, Object> updateLots(@ModelAttribute("seedStoreForm") SeedStoreForm form,
            Model model, Locale local) {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Integer> entryIdList = new ArrayList<Integer>();

        for (String gid : form.getEntryIdList().split(",")) {
            entryIdList.add(Integer.parseInt(gid));
        }
        
        //update of lots here
        Integer listId = form.getListId();
        
		try {
			GermplasmList germplasmList = this.fieldbookMiddlewareService
					.getGermplasmListById(listId);
			GermplasmListType germplasmListType = GermplasmListType.valueOf(germplasmList.getType());
			List<InventoryDetails> inventoryDetailListFromDB = 
					inventoryService.getInventoryListByListDataProjectListId(listId, germplasmListType);

			Double amount = form.getAmount();
			int inventoryLocationId = form.getInventoryLocationId();
			int inventoryScaleId = form.getInventoryScaleId();
			String inventoryComments = form.getInventoryComments();
			
			for(InventoryDetails inventoryDetail : inventoryDetailListFromDB){
				if(entryIdList.contains(inventoryDetail.getEntryId())){
					inventoryDetail.setAmount(amount);				
					inventoryDetail.setLocationId(inventoryLocationId);
					inventoryDetail.setScaleId(inventoryScaleId);
					inventoryDetail.setComment(inventoryComments);
				}
			}
			
			updateInventory(listId,inventoryDetailListFromDB);
			
			result.put("message", messageSource.getMessage("seed.inventory.update.inventory.success", null, local));
            result.put("success", 1);
            result.put("listId", listId);
			
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
			result.put("message", "error: " + e.getMessage());
            result.put("success", 0);
		}
		
        
        
        return result;
    }
    
	@Override
	public String getContentName() {
		return null;
	}
}