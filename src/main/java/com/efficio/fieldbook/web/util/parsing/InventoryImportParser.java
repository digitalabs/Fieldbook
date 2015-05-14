package com.efficio.fieldbook.web.util.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.AbstractExcelFileParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.WorkbookRowConverter;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.commons.parsing.validation.BulkComplValidator;
import org.generationcp.commons.parsing.validation.NonEmptyValidator;
import org.generationcp.commons.parsing.validation.ParseValidationMap;
import org.generationcp.commons.parsing.validation.CommaDelimitedValueValidator;
import org.generationcp.commons.parsing.validation.ValueRangeValidator;
import org.generationcp.commons.parsing.validation.ValueTypeValidator;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public class InventoryImportParser extends AbstractExcelFileParser<ImportedInventoryList> {

	private static final Logger LOG = LoggerFactory.getLogger(InventoryImportParser.class);

	public static final String ALL_INVENTORY_VALUES_REQUIRED = "inventory.import.parsing.validation.error.blank.inventory.value";
	public static final String INVALID_HEADERS = "common.parsing.invalid.headers";

	public static final int INVENTORY_SHEET = 0;
	
	public static final String HEADERS_MAP_PARAM_KEY = "HEADERS_MAP";

	public static final String LIST_ID_PARAM_KEY = "LIST_ID";

	private int currentParseIndex = 0;

	private ImportedInventoryList importedInventoryList;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;
	
	@Resource
	private InventoryDataManager inventoryDataManager;

	private List<Location> locations;

	private List<Scale> scales;
	
	private Map<InventoryHeaderLabels,Integer> inventoryHeaderLabelsMap;
	private String[] headers;
	
	private Integer listId;

	@Override
	public ImportedInventoryList parseWorkbook(Workbook workbook, Map<String,Object> additionalParams)
			throws FileParsingException {

		this.workbook = workbook;

		this.inventoryHeaderLabelsMap = (Map<InventoryHeaderLabels,Integer>)additionalParams.get(
				HEADERS_MAP_PARAM_KEY);
		
		this.headers = InventoryHeaderLabels.getHeaderNames(inventoryHeaderLabelsMap);
		
		this.listId = (Integer)additionalParams.get(LIST_ID_PARAM_KEY);
		
		validateFileHeader();

		parseInventoryDetails();

		return importedInventoryList;
	}

	protected void validateFileHeader() throws FileParsingException {
		if (isHeaderInvalid(currentParseIndex++, INVENTORY_SHEET, headers)) {
			throw new FileParsingException(INVALID_HEADERS);
		}
	}

	protected void parseInventoryDetails() throws FileParsingException {

		ParseValidationMap parseValidationMap = setupIndividualColumnValidation();
		InventoryRowConverter inventoryDetailsConverter = new InventoryRowConverter(
				workbook, currentParseIndex, INVENTORY_SHEET, 
				headers.length, inventoryHeaderLabelsMap,
				convertToLocationMap(locations), convertToScaleMap(scales));
		inventoryDetailsConverter.setValidationMap(parseValidationMap);

		List<InventoryDetails> detailList = inventoryDetailsConverter.convertWorkbookRowsToObject(
				new WorkbookRowConverter.ContinueTillBlank());

		importedInventoryList = new ImportedInventoryList(detailList, this.originalFilename);
	}

	public Map<String, Location> convertToLocationMap(List<Location> locationList) {
		Map<String, Location> locationMap = new HashMap<>();

		for (Location location : locationList) {
			locationMap.put(location.getLabbr(), location);
		}

		return locationMap;
	}

	public Map<String, Scale> convertToScaleMap(List<Scale> scaleList) {
		Map<String, Scale> scaleMap = new HashMap<>();

		for (Scale scale : scaleList) {
			scaleMap.put(scale.getName(), scale);
		}

		return scaleMap;
	}

	protected ParseValidationMap setupIndividualColumnValidation() {
		ParseValidationMap validationMap = new ParseValidationMap();
		for (int index = 0; index < headers.length; index++) {
			String header = headers[index];
			if(InventoryHeaderLabels.ENTRY.getName().equals(header) || 
					InventoryHeaderLabels.GID.getName().equals(header) ) {
				validationMap.addValidation(index, new ValueTypeValidator(Integer.class));
				validationMap.addValidation(index, new NonEmptyValidator());
			} else if(InventoryHeaderLabels.LOCATION.getName().equals(header)) {
				ValueRangeValidator locationValidator = new ValueRangeValidator(
						buildAllowedLocationsList());
				locationValidator.setValidationErrorMessage(
						"error.import.location.invalid.value");
				validationMap.addValidation(index,locationValidator);
			} else if(InventoryHeaderLabels.AMOUNT.getName().equals(header)) {
				ValueTypeValidator amountValidator = new ValueTypeValidator(Double.class);
				amountValidator.setValidationErrorMessage(
						"error.import.amount.must.be.numeric");
				validationMap.addValidation(index,amountValidator);
			} else if(InventoryHeaderLabels.SCALE.getName().equals(header)) {
				ValueRangeValidator scaleValidator = new ValueRangeValidator(buildAllowedScaleList());
				scaleValidator.setValidationErrorMessage(
						"error.import.scales.unaccepted.value");
				validationMap.addValidation(index,scaleValidator);
			} else if(InventoryHeaderLabels.BULK_WITH.getName().equals(header)) {
				CommaDelimitedValueValidator bulkWithValidator = 
						new CommaDelimitedValueValidator(buildAllowedStockList());
				bulkWithValidator.setValidationErrorMessage(
						"error.import.bulk.with.invalid.value");
				validationMap.addValidation(index,bulkWithValidator);
			} else if(InventoryHeaderLabels.BULK_COMPL.getName().equals(header)) {
				validationMap.addValidation(index,new BulkComplValidator(
						InventoryHeaderLabels.BULK_COMPL.ordinal(),
						InventoryHeaderLabels.BULK_WITH.ordinal()));
			}
		}
		return validationMap;
	}

	public List<String> buildAllowedLocationsList() {
		List<String> locationList = new ArrayList<>();

		try {
			locations = fieldbookMiddlewareService.getAllLocations();

			if (locations != null) {
				for (Location location : locations) {
					locationList.add(location.getLabbr());
				}
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}

		return locationList;
	}

	public List<String> buildAllowedScaleList() {
		List<String> allowedScales = new ArrayList<>();

		try {
			scales = ontologyService.getAllInventoryScales();

			if (scales != null) {
				for (Scale scale : scales) {
					allowedScales.add(scale.getName());
				}
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}

		return allowedScales;
	}
	
	public List<String> buildAllowedStockList() {
		List<String> stockIDList = new ArrayList<>();

		try {
			stockIDList = inventoryDataManager.getStockIdsByListDataProjectListId(listId);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}

		return stockIDList;
	}

	public static class InventoryRowConverter extends WorkbookRowConverter<InventoryDetails> {

		private Map<InventoryHeaderLabels,Integer> inventoryHeaderLabelsMap;
		private Map<String, Location> locationValidationMap;
		private Map<String, Scale> scaleValidationMap;

		public InventoryRowConverter(Workbook workbook, int startingIndex, int targetSheetIndex,
				int columnCount, Map<InventoryHeaderLabels,Integer> inventoryHeaderLabelsMap,
				Map<String, Location> locationValidationMap,
				Map<String, Scale> scaleValidationMap) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, 
					InventoryHeaderLabels.getHeaderNames(inventoryHeaderLabelsMap));
			this.inventoryHeaderLabelsMap = inventoryHeaderLabelsMap;
			this.locationValidationMap = locationValidationMap;
			this.scaleValidationMap = scaleValidationMap;
		}

		@Override public InventoryDetails convertToObject(Map<Integer, String> rowValues)
				throws FileParsingException {
			
			Integer gid = Integer.parseInt(rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID)));
			Integer entryId = Integer.parseInt(rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY)));
			String name = rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.DESIGNATION));
			String parentage = rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.PARENTAGE));
			String source = rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.SOURCE));
			String locationAbbr = rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION));
			String amountString = rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT));
			String scale = rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.SCALE));
			String comment = rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.COMMENT));
			String duplicate = rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.DUPLICATE));
			String bulkWith = rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_WITH));
			String bulkCompl = rowValues.get(
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_COMPL));

			// perform some row-based validation
			boolean inventorySpecificValuePresent = false;
			boolean inventorySpecificValueEmpty = false;

			for (int i : getInventorySpecificColumns()) {
				if (StringUtils.isEmpty(rowValues.get(i))) {
					inventorySpecificValueEmpty = true;
				} else {
					inventorySpecificValuePresent = true;
				}
			}

			if (inventorySpecificValueEmpty && inventorySpecificValuePresent) {
				throw new FileParsingException(ALL_INVENTORY_VALUES_REQUIRED, getCurrentIndex(),
						null, null);
			}

			InventoryDetails details = new InventoryDetails();
			details.setGid(gid);
			details.setEntryId(entryId);
			details.setGermplasmName(name);
			details.setParentage(parentage);
			details.setSource(source);
			details.setDuplicate(duplicate);
			details.setBulkWith(bulkWith);
			details.setBulkCompl(bulkCompl);
			

			if (!StringUtils.isEmpty(locationAbbr)) {
				Location location = locationValidationMap.get(locationAbbr);

				assert location != null;
				details.setLocationAbbr(locationAbbr);
				details.setLocationId(location.getLocid());
				details.setLocationName(location.getLname());
			}

			if (!StringUtils.isEmpty(scale)) {
				Scale scaleItem = scaleValidationMap.get(scale);
				assert scaleItem != null;
				details.setScaleName(scale);
				details.setScaleId(scaleItem.getId());

			}

			details.setComment(StringUtils.isEmpty(comment) ? null : comment);
			details.setAmount(
					StringUtils.isEmpty(amountString) ? null : Double.parseDouble(amountString));

			return details;
		}

		private int[] getInventorySpecificColumns() {
			return new int[] {
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION),
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT),
					inventoryHeaderLabelsMap.get(InventoryHeaderLabels.SCALE)};
		}
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public void setScales(List<Scale> scales) {
		this.scales = scales;
	}

	public void setHeaders(String[] headers) {
		this.headers = headers;
	}

	public void setInventoryHeaderLabelsMap(
			Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap) {
		this.inventoryHeaderLabelsMap = inventoryHeaderLabelsMap;
	}

	public void setInventoryDataManager(InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}
}