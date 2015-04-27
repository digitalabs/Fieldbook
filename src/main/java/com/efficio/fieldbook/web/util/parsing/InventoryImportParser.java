package com.efficio.fieldbook.web.util.parsing;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.AbstractExcelFileParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.WorkbookRowConverter;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.commons.parsing.validation.NonEmptyValidator;
import org.generationcp.commons.parsing.validation.ParseValidationMap;
import org.generationcp.commons.parsing.validation.ValueRangeValidator;
import org.generationcp.commons.parsing.validation.ValueTypeValidator;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public class InventoryImportParser extends AbstractExcelFileParser<ImportedInventoryList> {

	private static final Logger LOG = LoggerFactory.getLogger(InventoryImportParser.class);

	public static final String ALL_LOCATION_VALUES_REQUIRED = "inventory.import.parsing.validation.error.blank.location.value";
	public static final String INVALID_HEADERS = "common.parsing.invalid.headers";

	// aside from defining the expected value of the header labels, we are also defining here the order in which they appear
	enum InventoryHeaderLabels {
		ENTRY,
		DESIGNATION,
		PARENTAGE,
		GID,
		SOURCE,
		LOCATION,
		AMOUNT,
		SCALE,
		COMMENT;

		public static String[] names() {
			InventoryHeaderLabels[] values = values();
			String[] names = new String[values.length];

			for (int i = 0; i < values.length; i++) {
				names[i] = values[i].name();
			}

			return names;
		}
	}

	public static final int INVENTORY_SHEET = 0;
	public static final String[] HEADER_LABEL_ARRAY = InventoryHeaderLabels.names();

	public static final int[] INVENTORY_SPECIFIC_COLUMNS =
			new int[] {
					InventoryHeaderLabels.LOCATION.ordinal(),
					InventoryHeaderLabels.AMOUNT.ordinal(),
					InventoryHeaderLabels.SCALE.ordinal()};

	private int currentParseIndex = 0;

	private ImportedInventoryList importedInventoryList;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;

	private List<Location> locations;

	private List<Scale> scales;

	@Override
	public ImportedInventoryList parseWorkbook(Workbook workbook)
			throws FileParsingException {

		this.workbook = workbook;

		validateFileHeader();

		parseInventoryDetails();

		return importedInventoryList;
	}

	protected void validateFileHeader() throws FileParsingException {
		if (isHeaderInvalid(currentParseIndex++, INVENTORY_SHEET, HEADER_LABEL_ARRAY)) {
			throw new FileParsingException(INVALID_HEADERS);
		}
	}

	protected void parseInventoryDetails() throws FileParsingException {

		ParseValidationMap parseValidationMap = setupIndividualColumnValidation();
		InventoryRowConverter inventoryDetailsConverter = new InventoryRowConverter(workbook, currentParseIndex,
				INVENTORY_SHEET, HEADER_LABEL_ARRAY.length, HEADER_LABEL_ARRAY, convertToLocationMap(locations), convertToScaleMap(scales));
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

		// validation for ENTRY column
		validationMap.addValidation(InventoryHeaderLabels.ENTRY.ordinal(), new ValueTypeValidator(Integer.class));
		validationMap.addValidation(InventoryHeaderLabels.ENTRY.ordinal(), new NonEmptyValidator());

		validationMap.addValidation(InventoryHeaderLabels.GID.ordinal(),
				new ValueTypeValidator(Integer.class));
		validationMap.addValidation(InventoryHeaderLabels.GID.ordinal(), new NonEmptyValidator());

		validationMap.addValidation(InventoryHeaderLabels.LOCATION.ordinal(), new ValueRangeValidator(buildAllowedLocationsList()));
		validationMap.addValidation(InventoryHeaderLabels.AMOUNT.ordinal(), new ValueTypeValidator(Double.class));

		validationMap.addValidation(InventoryHeaderLabels.SCALE.ordinal(), new ValueRangeValidator(
				buildAllowedScaleList()));

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

	public static class InventoryRowConverter extends WorkbookRowConverter<InventoryDetails> {

		private Map<String, Location> locationValidationMap;
		private Map<String, Scale> scaleValidationMap;

		public InventoryRowConverter(Workbook workbook, int startingIndex, int targetSheetIndex,
				int columnCount, String[] columnLabels,
				Map<String, Location> locationValidationMap,
				Map<String, Scale> scaleValidationMap) {
			super(workbook, startingIndex, targetSheetIndex, columnCount, columnLabels);
			this.locationValidationMap = locationValidationMap;
			this.scaleValidationMap = scaleValidationMap;
		}

		@Override public InventoryDetails convertToObject(Map<Integer, String> rowValues)
				throws FileParsingException {
			// TODO: provide feature for mapping of columns to a different column order
			Integer gid = Integer.parseInt(rowValues.get(InventoryHeaderLabels.GID.ordinal()));
			Integer entryId = Integer
					.parseInt(rowValues.get(InventoryHeaderLabels.ENTRY.ordinal()));
			String name = rowValues.get(InventoryHeaderLabels.DESIGNATION.ordinal());
			String parentage = rowValues.get(InventoryHeaderLabels.PARENTAGE.ordinal());
			String source = rowValues.get(InventoryHeaderLabels.SOURCE.ordinal());
			String locationAbbr = rowValues.get(InventoryHeaderLabels.LOCATION.ordinal());
			String amountString = rowValues.get(InventoryHeaderLabels.AMOUNT.ordinal());
			String scale = rowValues.get(InventoryHeaderLabels.SCALE.ordinal());
			String comment = rowValues.get(InventoryHeaderLabels.COMMENT.ordinal());

			// perform some row-based validation
			// TODO: determine if row based validation is common occurrence, and if so model it as part of the hierarchy
			boolean inventorySpecificValuePresent = false;
			boolean inventorySpecificValueEmpty = false;

			for (int i : INVENTORY_SPECIFIC_COLUMNS) {
				if (StringUtils.isEmpty(rowValues.get(i))) {
					inventorySpecificValueEmpty = true;
				} else {
					inventorySpecificValuePresent = true;
				}
			}

			if (inventorySpecificValueEmpty && inventorySpecificValuePresent) {
				throw new FileParsingException(ALL_LOCATION_VALUES_REQUIRED, getCurrentIndex(),
						null, null);
			}

			InventoryDetails details = new InventoryDetails();
			details.setGid(gid);
			details.setEntryId(entryId);
			details.setGermplasmName(name);
			details.setParentage(parentage);
			details.setSource(source);

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
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public void setScales(List<Scale> scales) {
		this.scales = scales;
	}
}