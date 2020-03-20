package com.efficio.fieldbook.web.util.parsing;

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
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class InventoryImportParser extends AbstractExcelFileParser<ImportedInventoryList> {

	private static final Logger LOG = LoggerFactory.getLogger(InventoryImportParser.class);

	private static final String INVALID_HEADERS = "common.parsing.invalid.headers";

	static final int INVENTORY_SHEET = 0;

	public static final String HEADERS_MAP_PARAM_KEY = "HEADERS_MAP";

	public static final String LIST_ID_PARAM_KEY = "LIST_ID";

	public static final String GERMPLASM_LIST_TYPE_PARAM_KEY = "GERMPLASM_LIST_TYPE";

	private int currentParseIndex = 0;

	private ImportedInventoryList importedInventoryList;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private InventoryDataManager inventoryDataManager;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private OntologyDataManager ontologyDataManager;

	private List<Location> locations;

	private Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap;
	private String[] headers;
	private String[] requiredHeaders;

	private Integer listId;

	private GermplasmListType listType;

	private Scale scale;

	@Override
	public ImportedInventoryList parseWorkbook(final Workbook workbook, final Map<String, Object> additionalParams)
			throws FileParsingException {

		this.workbook = workbook;

		this.inventoryHeaderLabelsMap =
				(Map<InventoryHeaderLabels, Integer>) additionalParams.get(InventoryImportParser.HEADERS_MAP_PARAM_KEY);

		this.headers = InventoryHeaderLabels.getHeaderNames(this.inventoryHeaderLabelsMap, this.ontologyDataManager);

		this.requiredHeaders = InventoryHeaderLabels.getRequiredHeaderNames(this.inventoryHeaderLabelsMap, this.ontologyDataManager);
		this.listId = (Integer) additionalParams.get(InventoryImportParser.LIST_ID_PARAM_KEY);
		this.listType = (GermplasmListType) additionalParams.get(InventoryImportParser.GERMPLASM_LIST_TYPE_PARAM_KEY);
		this.validateFileHeader();

		this.parseInventoryDetails();

		return this.importedInventoryList;
	}

	private void validateFileHeader() throws FileParsingException {
		final int tempRowNo = this.currentParseIndex;
		if (this.isHeaderInvalid(tempRowNo, InventoryImportParser.INVENTORY_SHEET, this.requiredHeaders) && this
				.isHeaderInvalid(tempRowNo, InventoryImportParser.INVENTORY_SHEET, this.headers)) {
			throw new FileParsingException(InventoryImportParser.INVALID_HEADERS);
		}

		if (!this.isHeaderInvalid(tempRowNo, InventoryImportParser.INVENTORY_SHEET, this.requiredHeaders)) {
			this.headers = this.requiredHeaders;
			this.inventoryHeaderLabelsMap = InventoryHeaderLabels.getRequiredHeadersMap(this.listType);
		}
		this.currentParseIndex++;
	}

	private void parseInventoryDetails() throws FileParsingException {

		final ParseValidationMap parseValidationMap = this.setupIndividualColumnValidation();
		final InventoryRowConverter inventoryDetailsConverter =
				new InventoryRowConverter(this.workbook, this.currentParseIndex, InventoryImportParser.INVENTORY_SHEET, this.headers.length,
						this.inventoryHeaderLabelsMap, this.convertToLocationMap(this.locations), this.scale, this.ontologyDataManager);
		inventoryDetailsConverter.setValidationMap(parseValidationMap);

		final List<InventoryDetails> detailList =
				inventoryDetailsConverter.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		this.importedInventoryList = new ImportedInventoryList(detailList, this.originalFilename);
	}

	private Map<String, Location> convertToLocationMap(final List<Location> locationList) {
		final Map<String, Location> locationMap = new HashMap<>();

		for (final Location location : locationList) {
			locationMap.put(location.getLabbr(), location);
		}

		return locationMap;
	}

	ParseValidationMap setupIndividualColumnValidation() {
		final ParseValidationMap validationMap = new ParseValidationMap();
		for (int index = 0; index < this.headers.length; index++) {
			final String header = this.headers[index];
			if (InventoryHeaderLabels.ENTRY.getName().equals(header) || InventoryHeaderLabels.GID.getName().equals(header)) {
				validationMap.addValidation(index, new ValueTypeValidator(Integer.class));
				validationMap.addValidation(index, new NonEmptyValidator());
			} else if (InventoryHeaderLabels.LOCATION_ABBR.getName().equals(header)) {
				final ValueRangeValidator locationValidator = new ValueRangeValidator(this.buildAllowedLocationsList());
				locationValidator.setValidationErrorMessage("error.import.location.invalid.value");
				validationMap.addValidation(index, locationValidator);
			} else if (InventoryHeaderLabels.AMOUNT.getName().equals(header)) {
				final ValueTypeValidator amountValidator = new ValueTypeValidator(Double.class);
				amountValidator.setValidationErrorMessage("error.import.amount.must.be.numeric");
				validationMap.addValidation(index, amountValidator);
			} else if (InventoryHeaderLabels.BULK_WITH.getName().equals(header)) {
				final CommaDelimitedValueValidator bulkWithValidator = new CommaDelimitedValueValidator(this.buildAllowedStockList());
				bulkWithValidator.setValidationErrorMessage("error.import.bulk.with.invalid.value");
				validationMap.addValidation(index, bulkWithValidator);
			} else if (InventoryHeaderLabels.BULK_COMPL.getName().equals(header)) {
				validationMap.addValidation(index,
						new BulkComplValidator(InventoryHeaderLabels.BULK_COMPL.ordinal(), InventoryHeaderLabels.BULK_WITH.ordinal()));
			}
		}
		return validationMap;
	}

	List<String> buildAllowedLocationsList() {
		final List<String> locationList = new ArrayList<>();

		try {
			this.locations = this.fieldbookMiddlewareService.getLocationsByProgramUUID(contextUtil.getCurrentProgramUUID());

			if (this.locations != null) {
				for (final Location location : this.locations) {
					locationList.add(location.getLabbr());
				}
			}
		} catch (final MiddlewareQueryException e) {
			InventoryImportParser.LOG.error(e.getMessage(), e);
		}

		return locationList;
	}

	List<String> buildAllowedStockList() {
		List<String> stockIDList = new ArrayList<>();

		// TODO Remove exception swallowing
		try {
			stockIDList = this.inventoryDataManager.getStockIdsByListDataProjectListId(this.listId);
		} catch (final MiddlewareQueryException e) {
			InventoryImportParser.LOG.error(e.getMessage(), e);
		}

		return stockIDList;
	}

	public static class InventoryRowConverter extends WorkbookRowConverter<InventoryDetails> {

		private final Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap;
		private final Map<String, Location> locationValidationMap;
		private final Scale scale;

		public InventoryRowConverter(final Workbook workbook, final int startingIndex, final int targetSheetIndex, final int columnCount,
				final Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap, final Map<String, Location> locationValidationMap,
				final Scale scale, final OntologyDataManager ontologyDataManager) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, InventoryHeaderLabels.getHeaderNames(inventoryHeaderLabelsMap, ontologyDataManager));
			this.inventoryHeaderLabelsMap = inventoryHeaderLabelsMap;
			this.locationValidationMap = locationValidationMap;
			this.scale = scale;
		}

		@Override
		public InventoryDetails convertToObject(final Map<Integer, String> rowValues) throws FileParsingException {

			final Integer gid = Integer.parseInt(rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID)));
			final Integer entryId = Integer.parseInt(rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY)));
			final String name = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.DESIGNATION));
			final String parentage = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.PARENTAGE));
			final String source = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.SOURCE));
			final String locationAbbr = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION_ABBR));
			final String amountString = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT));
			final String comment = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.COMMENT));
			final String duplicate = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.DUPLICATE));
			final String bulkWith = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_WITH));

			String bulkCompl = rowValues.get(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_COMPL));
			if (!StringUtils.isEmpty(bulkCompl)) {
				bulkCompl = bulkCompl.toUpperCase();
			}

			final InventoryDetails details = new InventoryDetails();
			details.setGid(gid);
			details.setEntryId(entryId);
			details.setGermplasmName(name);
			details.setParentage(parentage);
			details.setSource(source);
			details.setDuplicate(duplicate);
			details.setBulkWith(bulkWith);
			details.setBulkCompl(bulkCompl);

			if (!StringUtils.isEmpty(locationAbbr) && this.locationValidationMap.get(locationAbbr) != null) {
				final Location location = this.locationValidationMap.get(locationAbbr);
				details.setLocationAbbr(locationAbbr);
				details.setLocationId(location.getLocid());
				details.setLocationName(location.getLname());
			}

			details.setAmount(StringUtils.isEmpty(amountString) ? null : Double.parseDouble(amountString));

			//Set the scale name if both the location and amount for the detail has value.
			if (details.getLocationAbbr() != null && details.getAmount() != null) {
				details.setScaleName(this.scale.getName());
				details.setScaleId(this.scale.getId());
			}

			details.setComment(StringUtils.isEmpty(comment) ? null : comment);
			return details;
		}
	}

	@Override
	protected boolean isHeaderInvalid(final int headerNo, final int sheetNumber, final String[] headers) {
		boolean isInvalid = false;

		for (int i = 0; i < headers.length; i++) {
			if (headers[i].equalsIgnoreCase(InventoryHeaderLabels.AMOUNT.getName())) {
				final String amountHeader = this.getCellStringValue(sheetNumber, headerNo, i);
				isInvalid = !this.isAmountHeaderValid(amountHeader);
			} else {
				isInvalid = isInvalid || !headers[i].equalsIgnoreCase(this.getCellStringValue(sheetNumber, headerNo, i));
			}
		}

		return isInvalid;
	}

	boolean isAmountHeaderValid(final String amountHeader) {
		this.scale = this.ontologyService.getInventoryScaleByName(amountHeader);
		return scale != null && scale.getTerm() != null;
	}

	public void setLocations(final List<Location> locations) {
		this.locations = locations;
	}

	public void setHeaders(final String[] headers) {
		this.headers = headers;
	}

	void setInventoryHeaderLabelsMap(final Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap) {
		this.inventoryHeaderLabelsMap = inventoryHeaderLabelsMap;
	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}
}
