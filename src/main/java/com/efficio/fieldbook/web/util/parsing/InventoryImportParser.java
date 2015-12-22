
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
import org.generationcp.commons.parsing.validation.CommaDelimitedValueValidator;
import org.generationcp.commons.parsing.validation.NonEmptyValidator;
import org.generationcp.commons.parsing.validation.ParseValidationMap;
import org.generationcp.commons.parsing.validation.ValueRangeValidator;
import org.generationcp.commons.parsing.validation.ValueTypeValidator;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class InventoryImportParser extends AbstractExcelFileParser<ImportedInventoryList> {

	private static final Logger LOG = LoggerFactory.getLogger(InventoryImportParser.class);

	public static final String INVALID_HEADERS = "common.parsing.invalid.headers";

	public static final int INVENTORY_SHEET = 0;

	public static final String HEADERS_MAP_PARAM_KEY = "HEADERS_MAP";

	public static final String LIST_ID_PARAM_KEY = "LIST_ID";

	public static final String GERMPLASM_LIST_TYPE_PARAM_KEY = "GERMPLASM_LIST_TYPE";

	private int currentParseIndex = 0;

	private ImportedInventoryList importedInventoryList;

	private List<String> stockIDList = new ArrayList<>();
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private InventoryDataManager inventoryDataManager;

	private List<Location> locations;

	private List<Scale> scales;

	private Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap;
	private String[] headers;
	private String[] requiredHeaders;

	private Integer listId;

	private GermplasmListType listType;

	@Override
	public ImportedInventoryList parseWorkbook(Workbook workbook, Map<String, Object> additionalParams) throws FileParsingException {

		this.workbook = workbook;

		this.inventoryHeaderLabelsMap =
				(Map<InventoryHeaderLabels, Integer>) additionalParams.get(InventoryImportParser.HEADERS_MAP_PARAM_KEY);

		this.headers = InventoryHeaderLabels.getHeaderNames(this.inventoryHeaderLabelsMap);

		this.requiredHeaders = InventoryHeaderLabels.getRequiredHeaderNames(this.inventoryHeaderLabelsMap);
		this.listId = (Integer) additionalParams.get(InventoryImportParser.LIST_ID_PARAM_KEY);
		this.listType = (GermplasmListType) additionalParams.get(InventoryImportParser.GERMPLASM_LIST_TYPE_PARAM_KEY);
		this.stockIDList = this.buildAllowedStockList();
		this.validateFileHeader();

		this.parseInventoryDetails();

		return this.importedInventoryList;
	}

	void validateFileHeader() throws FileParsingException {
		int tempRowNo = this.currentParseIndex;
		if (this.isHeaderInvalid(tempRowNo, InventoryImportParser.INVENTORY_SHEET, this.requiredHeaders)
				&& this.isHeaderInvalid(tempRowNo, InventoryImportParser.INVENTORY_SHEET, this.headers)) {
			throw new FileParsingException(InventoryImportParser.INVALID_HEADERS);
		}

		if (!this.isHeaderInvalid(tempRowNo, InventoryImportParser.INVENTORY_SHEET, this.requiredHeaders)) {
			this.headers = this.requiredHeaders;
			this.inventoryHeaderLabelsMap = InventoryHeaderLabels.getRequiredHeadersMap(this.listType);
		}
		this.currentParseIndex++;
	}

	protected void parseInventoryDetails() throws FileParsingException {

		ParseValidationMap parseValidationMap = this.setupIndividualColumnValidation();
		InventoryRowConverter inventoryDetailsConverter =
				new InventoryRowConverter(this.workbook, this.currentParseIndex, InventoryImportParser.INVENTORY_SHEET, this.headers.length,
						this.inventoryHeaderLabelsMap, this.convertToLocationMap(this.locations), this.convertToScaleMap(this.scales));
		inventoryDetailsConverter.setValidationMap(parseValidationMap);
		inventoryDetailsConverter.setAllowedStockIdList(this.stockIDList);
		List<InventoryDetails> detailList =
				inventoryDetailsConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		this.importedInventoryList = new ImportedInventoryList(detailList, this.originalFilename);
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
		for (int index = 0; index < this.headers.length; index++) {
			String header = this.headers[index];
			if (InventoryHeaderLabels.ENTRY.getName().equals(header) || InventoryHeaderLabels.GID.getName().equals(header)) {
				validationMap.addValidation(index, new ValueTypeValidator(Integer.class));
				validationMap.addValidation(index, new NonEmptyValidator());
			} else if (InventoryHeaderLabels.LOCATION.getName().equals(header)) {
				ValueRangeValidator locationValidator = new ValueRangeValidator(this.buildAllowedLocationsList());
				locationValidator.setValidationErrorMessage("error.import.location.invalid.value");
				validationMap.addValidation(index, locationValidator);
			} else if (InventoryHeaderLabels.AMOUNT.getName().equals(header)) {
				ValueTypeValidator amountValidator = new ValueTypeValidator(Double.class);
				amountValidator.setValidationErrorMessage("error.import.amount.must.be.numeric");
				validationMap.addValidation(index, amountValidator);
			} else if (InventoryHeaderLabels.UNITS.getName().equals(header)) {
				ValueRangeValidator scaleValidator = new ValueRangeValidator(this.buildAllowedScaleList());
				scaleValidator.setValidationErrorMessage("error.import.scales.unaccepted.value");
				validationMap.addValidation(index, scaleValidator);
			} else if (InventoryHeaderLabels.BULK_WITH.getName().equals(header)) {
				CommaDelimitedValueValidator bulkWithValidator = new CommaDelimitedValueValidator(this.stockIDList);
				bulkWithValidator.setValidationErrorMessage("error.import.bulk.with.invalid.value");
				validationMap.addValidation(index, bulkWithValidator);
			} else if (InventoryHeaderLabels.BULK_COMPL.getName().equals(header)) {
				validationMap.addValidation(index, new BulkComplValidator(InventoryHeaderLabels.BULK_COMPL.ordinal(),
						InventoryHeaderLabels.BULK_WITH.ordinal()));
			}
		}
		return validationMap;
	}

	public List<String> buildAllowedLocationsList() {
		List<String> locationList = new ArrayList<>();

		try {
			this.locations = this.fieldbookMiddlewareService.getAllLocations();

			if (this.locations != null) {
				for (Location location : this.locations) {
					locationList.add(location.getLabbr());
				}
			}
		} catch (MiddlewareQueryException e) {
			InventoryImportParser.LOG.error(e.getMessage(), e);
		}

		return locationList;
	}

	public List<String> buildAllowedScaleList() {
		List<String> allowedScales = new ArrayList<>();

		try {
			this.scales = this.ontologyService.getAllInventoryScales();

			if (this.scales != null) {
				for (Scale scale : this.scales) {
					allowedScales.add(scale.getName());
				}
			}
		} catch (MiddlewareQueryException e) {
			InventoryImportParser.LOG.error(e.getMessage(), e);
		}

		return allowedScales;
	}

	public List<String> buildAllowedStockList() {
		try {
			this.stockIDList = this.inventoryDataManager.getStockIdsByListDataProjectListId(this.listId);
		} catch (MiddlewareQueryException e) {
			InventoryImportParser.LOG.error(e.getMessage(), e);
		}

		return this.stockIDList;
	}

	public static class InventoryRowConverter extends WorkbookRowConverter<InventoryDetails> {

		private final Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap;
		private final Map<String, Location> locationValidationMap;
		private final Map<String, Scale> scaleValidationMap;
		private List<String> allowedStockIdList;
		private Map<String, String> stockIDMap;
		
		public InventoryRowConverter(Workbook workbook, int startingIndex, int targetSheetIndex, int columnCount,
				Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap, Map<String, Location> locationValidationMap,
				Map<String, Scale> scaleValidationMap) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, InventoryHeaderLabels.getHeaderNames(inventoryHeaderLabelsMap));
			this.inventoryHeaderLabelsMap = inventoryHeaderLabelsMap;
			this.locationValidationMap = locationValidationMap;
			this.scaleValidationMap = scaleValidationMap;
			this.stockIDMap = new HashMap<String, String>();
		}

		@Override
		public InventoryDetails convertToObject(Map<Integer, String> rowValues) throws FileParsingException {

			Integer gid = Integer.parseInt(rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID)));
			Integer entryId = Integer.parseInt(rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY)));
			String name = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.DESIGNATION));
			String parentage = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.PARENTAGE));
			String source = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.SOURCE));
			String locationAbbr = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION));
			String amountString = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT));
			String scale = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.UNITS));
			String comment = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.COMMENT));
			String duplicate = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.DUPLICATE));
			String bulkWith = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_WITH));
			String bulkCompl = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_COMPL));

			InventoryDetails details = new InventoryDetails();
			details.setGid(gid);
			details.setEntryId(entryId);
			details.setGermplasmName(name);
			details.setParentage(parentage);
			details.setSource(source);
			details.setDuplicate(duplicate);
			details.setBulkWith(this.updateBulkWith(bulkWith));
			details.setBulkCompl(bulkCompl);

			if (!StringUtils.isEmpty(locationAbbr)) {
				Location location = this.locationValidationMap.get(locationAbbr);

				assert location != null;
				details.setLocationAbbr(locationAbbr);
				details.setLocationId(location.getLocid());
				details.setLocationName(location.getLname());
			}

			if (!StringUtils.isEmpty(scale)) {
				Scale scaleItem = this.scaleValidationMap.get(scale);
				assert scaleItem != null;
				details.setScaleName(scale);
				details.setScaleId(scaleItem.getId());

			}

			details.setComment(StringUtils.isEmpty(comment) ? null : comment);
			details.setAmount(StringUtils.isEmpty(amountString) ? null : Double.parseDouble(amountString));

			return details;
		}
		
		protected String updateBulkWith(String bulkWith) {
			if(bulkWith != null && bulkWith != "" && !this.allowedStockIdList.contains(bulkWith)){
				if(bulkWith.contains(",")){
					return this.matchStockIdArray(bulkWith);
				} else{
					return this.matchStockId(bulkWith);
				}
			}
			return bulkWith;
		}
		
		private String matchStockIdArray(String bulkWith){
			String newBulkWith = "";
			bulkWith = bulkWith.trim();
			String[] bulkWithArray = bulkWith.split(",");
			for (String parsedValue : bulkWithArray) {
				String trimmedValue = parsedValue.trim();
				if(parsedValue.equals(bulkWithArray[bulkWithArray.length-1])){
					newBulkWith += this.matchStockId(trimmedValue);
					break;
				}
				newBulkWith += this.matchStockId(trimmedValue) + ", ";
			}
			return newBulkWith;
		}
		
		private String matchStockId(String bulkWith){
			String value = this.stockIDMap.get(bulkWith.toUpperCase());
			if(value != null){
				return value;
			}
			return bulkWith;
		}
		
		private void setUpStockIDMap(){
			for(String stockID: this.allowedStockIdList){
				this.stockIDMap.put(stockID.toUpperCase(), stockID);
			}
		}
		protected void setAllowedStockIdList(List<String> allowedStockIdList){
			this.allowedStockIdList = allowedStockIdList;
			this.setUpStockIDMap();
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

	public void setInventoryHeaderLabelsMap(Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap) {
		this.inventoryHeaderLabelsMap = inventoryHeaderLabelsMap;
	}

	public void setInventoryDataManager(InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}
}
