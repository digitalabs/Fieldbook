package com.efficio.fieldbook.web.stock;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ImportStockForm;
import com.efficio.fieldbook.web.common.service.ImportInventoryService;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;
import com.efficio.fieldbook.web.util.parsing.InventoryHeaderLabels;
import com.efficio.fieldbook.web.util.parsing.InventoryImportParser;
import com.google.common.base.Joiner;
import org.apache.commons.collections.ListUtils;
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
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.LocationType;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 4/24/2015 Time: 4:38 PM
 */
@Controller
@RequestMapping(value = StockController.URL)
public class StockController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(StockController.class);

	public static final String URL = "/stock";
	public static final String IS_SUCCESS = "isSuccess";
	public static final String FAILURE = "0";
	public static final String SUCCESS = "1";
	public static final String ERROR_MESSAGE = "errorMessage";
	public static final String HAS_ERROR = "hasError";
	public static final String STOCK_ID = "stockListId";
	@Resource
	private StockService stockService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private InventoryService inventoryService;

	@Resource
	private PlatformTransactionManager transactionManager;

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

	@Resource
	private UserSelection userSelection;

	@Resource
	private LocationDataManager locationDataManager;

	/**
	 * Gets the data types.
	 *
	 * @return the data types
	 */
	@ModelAttribute("favoriteSeedStorageLocationList")
	public List<Location> getFavoriteSeedStorageLocationList() {
		try {
			final List<Integer> locationsIds =
					this.fieldbookMiddlewareService.getFavoriteProjectLocationIds(this.contextUtil.getCurrentProgramUUID());
			final List<Location> faveLocations =
					this.fieldbookMiddlewareService.getFavoriteLocationByLocationIDs(locationsIds);

			final List<Location> allSeedStorageLocations = this.fieldbookMiddlewareService.getAllSeedLocations();
			return ListUtils.intersection(allSeedStorageLocations, faveLocations);

		} catch (final MiddlewareQueryException e) {
			StockController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
	}

	/**
	 * Gets the data types.
	 *
	 * @return the data types
	 */
	@ModelAttribute("allLocationList")
	public List<Location> getAllLocationList() {
		return this.fieldbookMiddlewareService.getLocationsByProgramUUID(this.contextUtil.getCurrentProgramUUID());
	}

	/**
	 * Gets the data types.
	 *
	 * @return the data types
	 */
	@ModelAttribute("locationList")
	public List<Location> getLocationList() {
		try {
			return this.fieldbookMiddlewareService.getAllSeedLocations();
		} catch (final MiddlewareQueryException e) {
			StockController.LOG.error(e.getMessage(), e);
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

			final List<Integer> locationsIds =
					this.fieldbookMiddlewareService.getFavoriteProjectLocationIds(this.getCurrentProject().getUniqueID());
			return this.fieldbookMiddlewareService.getFavoriteLocationByLocationIDs(locationsIds);
		} catch (final MiddlewareQueryException e) {
			StockController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
	}

	@ModelAttribute("scaleList")
	public List<Scale> getScaleList() {
		try {
			return this.ontologyService.getAllInventoryScales();
		} catch (final MiddlewareQueryException e) {
			StockController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<>();
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveNextStockPrefix", method = RequestMethod.POST)
	public Map<String, String> retrieveNextStockIDPrefix(@RequestBody final StockListGenerationSettings generationSettings) {
		final Map<String, String> resultMap = new HashMap<>();

		final Integer validationResult = generationSettings.validateSettings();

		if (!validationResult.equals(StockListGenerationSettings.VALID_SETTINGS)) {
			return this.prepareValidationErrorMessages(validationResult);
		}

		try {
			String prefix = this.stockService
					.calculateNextStockIDPrefix(generationSettings.getBreederIdentifier(), generationSettings.getSeparator());
			// for UI purposes, we remove the separator from the generated prefix
			prefix = prefix.substring(0, prefix.length() - 1);
			resultMap.put(StockController.IS_SUCCESS, StockController.SUCCESS);
			resultMap.put("prefix", prefix);

		} catch (final MiddlewareException e) {
			StockController.LOG.error(e.getMessage(), e);
			resultMap.put(StockController.IS_SUCCESS, StockController.FAILURE);
		}

		return resultMap;
	}

	@RequestMapping(value = "/generateStockTabIfNecessary/{listId}", method = RequestMethod.GET)
	public String generateStockTabIfNecessary(@PathVariable final Integer listId, final Model model) {

		try {
			final boolean transactionsExist = this.inventoryDataManager.transactionsExistForListProjectDataListID(listId);
			if (transactionsExist) {
				final GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(listId);

				model.addAttribute("listName", germplasmList.getName());
				model.addAttribute("listId", listId);

				return "/StudyManager/stockTab";
			}

		} catch (final MiddlewareQueryException e) {
			StockController.LOG.error(e.getMessage(), e);
		}

		return "StudyManager/blank";
	}

	protected Map<String, String> prepareValidationErrorMessages(final Integer validationResult) {
		final Map<String, String> resultMap = new HashMap<>();
		resultMap.put(StockController.IS_SUCCESS, StockController.FAILURE);
		switch (validationResult) {
			case StockListGenerationSettings.NUMBERS_FOUND:
				resultMap.put(StockController.ERROR_MESSAGE, this.messageSource
						.getMessage("stock.generate.id.breeder.identifier.error.numbers.found", new Object[] {}, Locale.getDefault()));
				break;
			case StockListGenerationSettings.SPACE_FOUND:
				resultMap.put(StockController.ERROR_MESSAGE, this.messageSource
						.getMessage("stock.generate.id.breeder.identifier.error.space.found", new Object[] {}, Locale.getDefault()));
				break;
			default:
				break;
		}

		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/generateStockList/{listId}", method = RequestMethod.POST)
	public Map<String, String> generateStockList(@RequestBody final StockListGenerationSettings generationSettings,
			@PathVariable("listId") final Integer listDataProjectListId) {
		final Map<String, String> resultMap = new HashMap<>();
		final Integer validationResult = generationSettings.validateSettings();

		if (!validationResult.equals(StockListGenerationSettings.VALID_SETTINGS)) {
			return this.prepareValidationErrorMessages(validationResult);
		}

		try {
			final Integer listDataID = this.germplasmListManager.retrieveDataListIDFromListDataProjectListID(listDataProjectListId);
			final Map<ListDataProject, GermplasmListData> germplasmMap = this.generateGermplasmMap(listDataID, listDataProjectListId);

			final String prefix = this.stockService
					.calculateNextStockIDPrefix(generationSettings.getBreederIdentifier(), generationSettings.getSeparator());
			final Map<Integer, InventoryDetails> inventoryDetailMap = new HashMap<>();

			final Integer currentUserId = this.getCurrentIbdbUserId();
			final Integer defaultSeedStorageLocationId = this.locationDataManager.getDefaultLocationByType(LocationType.SSTORE).getLocid();
			for (final Map.Entry<ListDataProject, GermplasmListData> entry : germplasmMap.entrySet()) {
				final InventoryDetails details = new InventoryDetails();
				details.setAmount(0d);
				details.setLocationId(defaultSeedStorageLocationId);
				details.setScaleId(null);
				details.setUserId(currentUserId);
				details.setGid(entry.getKey().getGermplasmId());
				details.setSourceId(listDataID);
				details.setInventoryID(prefix + entry.getKey().getEntryId());

				inventoryDetailMap.put(entry.getKey().getEntryId(), details);
			}

			if (generationSettings.hasBulkInstructions()) {
				this.stockService
						.processBulkSettings(germplasmMap.keySet(), inventoryDetailMap, generationSettings.isAddPedigreeDuplicate(),
								generationSettings.isAddPlotReciprocal(), generationSettings.isAddPedigreeReciprocal());
			}

			final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(final TransactionStatus arg0) {
					for (final Map.Entry<ListDataProject, GermplasmListData> entry : germplasmMap.entrySet()) {
						final ListDataProject project = entry.getKey();
						final GermplasmListData data = entry.getValue();
						final InventoryDetails details = inventoryDetailMap.get(project.getEntryId());
						StockController.this.inventoryService.addLotAndTransaction(details, data, project,
							StockController.this.contextUtil.getProjectInContext().getCropType());
					}
				}

			});

			resultMap.put(StockController.IS_SUCCESS, StockController.SUCCESS);
		} catch (final MiddlewareException e) {
			StockController.LOG.error(e.getMessage(), e);
			resultMap.put(StockController.IS_SUCCESS, StockController.FAILURE);
			resultMap.put(StockController.ERROR_MESSAGE,
					this.messageSource.getMessage("common.default.error", new Object[] {}, Locale.getDefault()));
		}

		return resultMap;
	}

	protected Map<ListDataProject, GermplasmListData> generateGermplasmMap(final Integer listDataID, final Integer listDataProjectListId) {
		final List<GermplasmListData> germplasmListDataList = this.germplasmListManager.getGermplasmListDataByListId(listDataID);
		final List<ListDataProject> listDataProjectList = this.germplasmListManager.retrieveSnapshotListData(listDataProjectListId);

		final Map<ListDataProject, GermplasmListData> germplasmMap = new HashMap<>();

		for (final ListDataProject listDataProject : listDataProjectList) {
			boolean matchFound = false;
			for (final GermplasmListData germplasmListData : germplasmListDataList) {
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
	@RequestMapping(value = "/import", method = RequestMethod.POST)
	public String importList(@ModelAttribute("importStockForm") final ImportStockForm form) {
		final Map<String, Object> result = new HashMap<>();
		try {
			final Integer listId = form.getStockListId();
			final GermplasmList germplasmList = this.fieldbookMiddlewareService.getGermplasmListById(listId);
			final GermplasmListType germplasmListType = GermplasmListType.valueOf(germplasmList.getType());
			final Map<String, Object> additionalParams = new HashMap<>();
			additionalParams.put(InventoryImportParser.HEADERS_MAP_PARAM_KEY, InventoryHeaderLabels.headers(germplasmListType));
			additionalParams.put(InventoryImportParser.LIST_ID_PARAM_KEY, listId);
			additionalParams.put(InventoryImportParser.GERMPLASM_LIST_TYPE_PARAM_KEY, germplasmListType);
			final ImportedInventoryList importedInventoryList = this.importInventoryService.parseFile(form.getFile(), additionalParams);
			final List<InventoryDetails> inventoryDetailListFromDB = this.inventoryService.getInventoryListByListDataProjectListId(listId);
			this.importInventoryService.validateInventoryDetails(inventoryDetailListFromDB, importedInventoryList, germplasmListType);
			// Setting List Id & Inventory Details in user selection that will be used if user wants to discard the imported stock list
			this.userSelection.setListId(listId);
			this.userSelection.setInventoryDetails(this.inventoryService.getInventoryListByListDataProjectListId(listId));

			if (this.importInventoryService.hasConflict(inventoryDetailListFromDB, importedInventoryList)) {
				result.put("hasConflict", true);
			}
			this.importInventoryService.mergeInventoryDetails(inventoryDetailListFromDB, importedInventoryList, germplasmListType);
			this.updateInventory(listId, inventoryDetailListFromDB);
			result.put(StockController.HAS_ERROR, false);
			result.put(StockController.STOCK_ID, listId);
			result.put("listType", germplasmListType);
		} catch (final FileParsingException e) {
			StockController.LOG.error(e.getMessage(), e);
			result.put(StockController.HAS_ERROR, true);
			result.put(StockController.ERROR_MESSAGE,
					this.messageSource.getMessage(e.getMessage(), e.getMessageParameters(), Locale.getDefault()));
		} catch (final FieldbookException e) {
			StockController.LOG.error(e.getMessage(), e);
			result.put(StockController.HAS_ERROR, true);
			result.put(StockController.ERROR_MESSAGE, e.getMessage());
		} catch (final Exception e) {
			StockController.LOG.error(e.getMessage(), e);
			result.put(StockController.HAS_ERROR, true);
			result.put(StockController.ERROR_MESSAGE,
					this.messageSource.getMessage("common.import.failed", new Object[] {}, Locale.getDefault()));
		}
		return this.convertObjectToJson(result);
	}

	@ResponseBody
	@RequestMapping(value = "/revertStockListData/data", method = RequestMethod.POST)
	public String revertStockListData(@ModelAttribute("importStockForm") final ImportStockForm form) {
		final Map<String, Object> result = new HashMap<>();
		final Integer listId = this.userSelection.getListId();
		final List<InventoryDetails> inventoryDetailListFromDB = this.userSelection.getInventoryDetails();
		this.updateInventory(listId, inventoryDetailListFromDB);
		result.put(StockController.STOCK_ID, listId);
		return this.convertObjectToJson(result);
	}

	private void updateInventory(final Integer listId, final List<InventoryDetails> inventoryDetailListFromDB) {
		this.inventoryDataManager.updateInventory(listId, inventoryDetailListFromDB);
	}

	@ResponseBody
	@RequestMapping(value = "/executeBulkingInstructions/{listId}", method = RequestMethod.POST)
	public Map<String, Object> executeBulkingInstructions(@PathVariable final Integer listId) {
		final Map<String, Object> result = new HashMap<>();
		try {
			final List<InventoryDetails> inventoryDetailsList = this.inventoryService.getInventoryListByListDataProjectListId(listId);
			this.stockService.verifyIfBulkingForStockListCanProceed(listId, inventoryDetailsList);
			this.stockService.executeBulkingInstructions(inventoryDetailsList);
			result.put(StockController.HAS_ERROR, false);
			result.put(StockController.STOCK_ID, listId);
		} catch (final StockException e) {
			StockController.LOG.error(e.getMessage(), e);
			result.put(StockController.HAS_ERROR, true);
			result.put(StockController.ERROR_MESSAGE,
					this.messageSource.getMessage(e.getMessage(), e.getMessageParameters(), Locale.getDefault()));
		} catch (final Exception e) {
			StockController.LOG.error(e.getMessage(), e);
			result.put(StockController.HAS_ERROR, true);
			result.put(StockController.ERROR_MESSAGE,
					this.messageSource.getMessage("error.bulking.duplicates.and.reciprocals.failed", new Object[] {}, Locale.getDefault()));
		}
		return result;
	}

	@RequestMapping(value = "/ajax/{listId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String showAjax(@ModelAttribute("seedStoreForm") final SeedStoreForm form, @PathVariable final Integer listId,
			@RequestBody final String[] stockIdsForUpdate, final Model model, final HttpSession session) {
		form.setListId(listId);
		form.setStockIdsForUpdate(Joiner.on(",").join(stockIdsForUpdate));
		return super.showAjaxPage(model, "Inventory/addLotsModal");
	}

	@ResponseBody
	@RequestMapping(value = "/update/lots", method = RequestMethod.POST)
	public Map<String, Object> updateLots(@ModelAttribute("seedStoreForm") final SeedStoreForm form, final Model model,
			final Locale local) {
		final Map<String, Object> result = new HashMap<>();
		final List<String> stockIdsForUpdate = new ArrayList<>(Arrays.asList(form.getStockIdsForUpdate().split(",")));

		final Integer listId = form.getListId();
		final Double amount = form.getAmount();
		final int inventoryLocationId = form.getInventoryLocationId();
		final int inventoryScaleId = form.getInventoryScaleId();
		final String inventoryComments = form.getInventoryComments();

		// Check that among lots to be updated, we will not be updating the amount of a lot that has multiple transactions already
		final Map<String, Double> stockIdsWithMultipleTransactions = this.inventoryDataManager.getStockIdsWithMultipleTransactions(listId);
		final Set<Double> amountsInDb =
			stockIdsWithMultipleTransactions.entrySet().stream().filter(e -> stockIdsForUpdate.contains(e.getKey()))
				.map(Map.Entry::getValue).collect(Collectors.toSet());
		for (final Double amountInDb : amountsInDb) {
			if (!amount.equals(amountInDb)) {
				result.put(StockController.HAS_ERROR, true);
				result.put(StockController.ERROR_MESSAGE,
					this.messageSource.getMessage("error.inventory.amount.cannot.be.updated", null, local));
				result.put(StockController.IS_SUCCESS, StockController.FAILURE);
				return result;
			}
		}

		try {
			final List<InventoryDetails> inventoryDetailListFromDB = this.inventoryService.getInventoryListByListDataProjectListId(listId);

			// update of lots here
			for (final InventoryDetails inventoryDetail : inventoryDetailListFromDB) {
				if (stockIdsForUpdate.contains(inventoryDetail.getInventoryID())) {
					inventoryDetail.setAmount(amount);
					inventoryDetail.setLocationId(inventoryLocationId);
					inventoryDetail.setScaleId(inventoryScaleId);
					inventoryDetail.setComment(inventoryComments);
				}
			}

			this.updateInventory(listId, inventoryDetailListFromDB);

			result.put("message", this.messageSource.getMessage("seed.inventory.update.inventory.success", null, local));
			result.put("success", 1);
			result.put("listId", listId);

		} catch (final MiddlewareQueryException e) {
			StockController.LOG.error(e.getMessage(), e);
			result.put(StockController.ERROR_MESSAGE, "error: " + e.getMessage());
			result.put("success", 0);
		}

		return result;
	}

	@Override
	public String getContentName() {
		return null;
	}
}
