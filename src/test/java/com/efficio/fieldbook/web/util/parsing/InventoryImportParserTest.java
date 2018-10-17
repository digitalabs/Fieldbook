package com.efficio.fieldbook.web.util.parsing;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.commons.parsing.validation.BulkComplValidator;
import org.generationcp.commons.parsing.validation.CommaDelimitedValueValidator;
import org.generationcp.commons.parsing.validation.NonEmptyValidator;
import org.generationcp.commons.parsing.validation.ParseValidationMap;
import org.generationcp.commons.parsing.validation.ValueRangeValidator;
import org.generationcp.commons.parsing.validation.ValueTypeValidator;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.data.initializer.ScaleTestDataInitializer;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 2/26/2015 Time: 4:42 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class InventoryImportParserTest {

	private static final String SCALE_VALUE = "SEED_AMOUNT_kg";

	private static final String AMOUNT_HEADER = "SEED_AMOUNT_KG";
	public static final String PROGRAM_UUID = "ahsgfkasg-jas73324-ajksdhaskjf";

	@Mock
	private FileService fileService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	public OntologyDataManager ontologyDataManager;

	@InjectMocks
	private InventoryImportParser parser;

	public static final String TEST_FILE_NAME = "Inventory_Import_Template_v1.xlsx";
	public static final String TEST_FILE_NAME_WITH_BULKING = "Inventory_Import_Template_Bulking.xlsx";
	public static final int DUMMY_INDEX = 1;
	private static final int TEST_LIST_ID = 1;
	private static final GermplasmListType TEST_GERMPLASM_LIST_TYPE = GermplasmListType.CROSSES;

	private static final String STOCKID_PREFIX = "SID";

	private List<Location> testLocationList;
	private List<String> testStockIds;

	private String[] headers;
	private Map<InventoryHeaderLabels, Integer> inventoryHeaderLabelsMap;

	private ScaleTestDataInitializer scaleTDI;

	@Before
	public void setUp() throws Exception {
		this.scaleTDI = new ScaleTestDataInitializer();
		this.testLocationList = this.createDummyLocationList();
		this.testStockIds = this.createDummyStockIds();

		Mockito.when(contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.doReturn(this.testLocationList).when(this.fieldbookMiddlewareService).getLocationsByProgramUUID(PROGRAM_UUID);
		Mockito.doReturn(this.scaleTDI.createScale()).when(this.ontologyService)
				.getInventoryScaleByName(InventoryImportParserTest.AMOUNT_HEADER);
		Mockito.doReturn(this.testStockIds).when(this.inventoryDataManager)
				.getStockIdsByListDataProjectListId(InventoryImportParserTest.TEST_LIST_ID);

		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.ENTRY_ID.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.ENTRY.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.DESIGNATION.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.DESIGNATION.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.PARENTAGE.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.PARENTAGE.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.GID.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.GID.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.SEED_SOURCE.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.SOURCE.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.LOCATION.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.BULK_WITH.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.BULK_WITH.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.BULK_COMPL.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.BULK_COMPL.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.DUPLICATE.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.DUPLICATE.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.STOCKID_INVENTORY.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.STOCKID.getName(), null));
		Mockito.when(this.ontologyDataManager.getTermById(ColumnLabels.COMMENT.getTermId().getId())).thenReturn(new Term(1, InventoryHeaderLabels.COMMENT.getName(), null));
	}

	private Workbook createWorkbook(final GermplasmListType germplasmListType) throws Exception {
		String filename = InventoryImportParserTest.TEST_FILE_NAME;
		if (GermplasmListType.isCrosses(germplasmListType)) {
			filename = InventoryImportParserTest.TEST_FILE_NAME_WITH_BULKING;
		}
		final File workbookFile = new File(ClassLoader.getSystemClassLoader().getResource(filename).toURI());

		assert workbookFile.exists();

		return WorkbookFactory.create(workbookFile);
	}

	private List<String> createDummyStockIds() {
		final List<String> stockIds = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			stockIds.add(InventoryImportParserTest.STOCKID_PREFIX + "1-" + i);
		}
		return stockIds;
	}

	@Test
	public void testBuildAllowedLocationList() {
		this.parser.setLocations(this.testLocationList);
		final List<String> allowedLocationsList = this.parser.buildAllowedLocationsList();
		Assert.assertEquals(this.testLocationList.size(), allowedLocationsList.size());

	}

	@Test
	public void testConvertToLocationMap() {
		final Map<String, Location> locationMap = this.parser.convertToLocationMap(this.testLocationList);

		Assert.assertEquals(locationMap.size(), this.testLocationList.size());
		final Collection<Location> values = locationMap.values();
		for (final Location location : this.testLocationList) {
			Assert.assertTrue(values.contains(location));
		}
	}

	@Test
	public void testSetupParsingValidations() {
		this.generateHeaders(GermplasmListType.CROSSES);
		final ParseValidationMap map = this.parser.setupIndividualColumnValidation();
		Assert.assertTrue(!map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION_ABBR)).isEmpty());
		Assert.assertTrue(!map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT)).isEmpty());
		Assert.assertTrue(!map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY)).isEmpty());
		Assert.assertTrue(!map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID)).isEmpty());
		Assert.assertTrue(!map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_WITH)).isEmpty());
		Assert.assertTrue(!map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_COMPL)).isEmpty());

		Assert.assertTrue(map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION_ABBR))
				.get(0) instanceof ValueRangeValidator);
		Assert.assertTrue(
				map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT)).get(0) instanceof ValueTypeValidator);
		Assert.assertTrue(
				map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY)).get(0) instanceof ValueTypeValidator);
		Assert.assertTrue(
				map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID)).get(0) instanceof ValueTypeValidator);
		Assert.assertTrue(
				map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY)).get(1) instanceof NonEmptyValidator);
		Assert.assertTrue(
				map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID)).get(1) instanceof NonEmptyValidator);
		Assert.assertTrue(map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_WITH))
				.get(0) instanceof CommaDelimitedValueValidator);
		Assert.assertTrue(map.getValidations(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_COMPL))
				.get(0) instanceof BulkComplValidator);
	}

	private void generateHeaders(final GermplasmListType crosses) {
		this.inventoryHeaderLabelsMap = InventoryHeaderLabels.headers(GermplasmListType.CROSSES);
		this.headers = InventoryHeaderLabels.getHeaderNames(this.inventoryHeaderLabelsMap, this.ontologyDataManager);
		this.parser.setInventoryHeaderLabelsMap(this.inventoryHeaderLabelsMap);
		this.parser.setHeaders(this.headers);
	}

	@Test
	public void testObjectConversionAllInventoryItemsMissing() throws Exception {
		final GermplasmListType germplasmListType = GermplasmListType.ADVANCED;
		this.generateHeaders(germplasmListType);
		final InventoryImportParser.InventoryRowConverter rowConverter =
				this.createForTestingRowConverter(this.createWorkbook(germplasmListType));
		final Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY), "1");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID), "-10");

		final InventoryDetails details = rowConverter.convertToObject(testRowValue);

		Assert.assertEquals(new Integer(-10), details.getGid());
		Assert.assertNull(details.getAmount());
		Assert.assertNull(details.getLocationAbbr());
		Assert.assertNull(details.getScaleName());
	}

	@Test
	public void testObjectConversionWhereAmountIsNull() throws Exception {
		final GermplasmListType germplasmListType = GermplasmListType.ADVANCED;
		this.generateHeaders(germplasmListType);
		final InventoryImportParser.InventoryRowConverter rowConverter =
				this.createForTestingRowConverter(this.createWorkbook(germplasmListType));
		final Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY), "1");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID), "-10");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION_ABBR), "TEST1");

		final InventoryDetails details = rowConverter.convertToObject(testRowValue);

		Assert.assertEquals(new Integer(-10), details.getGid());
		Assert.assertEquals("The location abbr's value should be TEST1", "TEST1", details.getLocationAbbr());
		Assert.assertNull("The amount should be null", details.getAmount());
		Assert.assertNull("The scale name should be null", details.getScaleName());
	}

	@Test
	public void testObjectConversionWhereAmountIs0() throws Exception {
		final GermplasmListType germplasmListType = GermplasmListType.ADVANCED;
		this.generateHeaders(germplasmListType);
		final InventoryImportParser.InventoryRowConverter rowConverter =
				this.createForTestingRowConverter(this.createWorkbook(germplasmListType));
		final Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY), "1");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID), "-10");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION_ABBR), "TEST1");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT), "0");

		final InventoryDetails details = rowConverter.convertToObject(testRowValue);

		Assert.assertEquals(new Integer(-10), details.getGid());
		Assert.assertEquals("The location abbr's value should be TEST1", "TEST1", details.getLocationAbbr());
		Assert.assertEquals("The amount's value should be 0", "0.0", details.getAmount().toString());
		Assert.assertEquals("The scale name's value should be " + InventoryImportParserTest.SCALE_VALUE,
				InventoryImportParserTest.SCALE_VALUE, details.getScaleName());
	}

	@Test
	public void testObjectConversionNoInventoryItemsPresent() throws Exception {
		final GermplasmListType germplasmListType = GermplasmListType.ADVANCED;
		this.generateHeaders(germplasmListType);
		final InventoryImportParser.InventoryRowConverter rowConverter =
				this.createForTestingRowConverter(this.createWorkbook(germplasmListType));
		final Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY), "1");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID), "-10");
		final InventoryDetails details = rowConverter.convertToObject(testRowValue);
		Assert.assertNotNull("Inventory details could not be properly created when all inventory related columns are blank", details);
		Assert.assertNull(details.getAmount());
		Assert.assertNull(details.getComment());
	}

	@Test
	public void testObjectConversionAllInventoryItemsPresent() throws Exception {
		final GermplasmListType germplasmListType = GermplasmListType.CROSSES;
		this.generateHeaders(germplasmListType);
		final InventoryImportParser.InventoryRowConverter rowConverter =
				this.createForTestingRowConverter(this.createWorkbook(germplasmListType));
		final Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY), "1");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID), "-10");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION_ABBR), "TEST1");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT), "15");
		testRowValue.put(this.inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_COMPL), "y");

		final InventoryDetails details = rowConverter.convertToObject(testRowValue);
		Assert.assertNotNull("Inventory details could not be properly created when all inventory related columns are present", details);

		Assert.assertEquals(details.getAmount(), 15.0, 0);
		Assert.assertEquals("Inventory details not created with proper location abbr", "TEST1", details.getLocationAbbr());
		Assert.assertEquals("Inventory details not created with proper location id", new Integer(1), details.getLocationId());
		Assert.assertEquals("Inventory details not created with proper location name", "Test Location 1", details.getLocationName());
		Assert.assertEquals("Inventory details not created with proper scale id", new Integer(1), details.getScaleId());
		Assert.assertEquals("Inventory details not created with proper scale name", InventoryImportParserTest.SCALE_VALUE,
				details.getScaleName());
		Assert.assertEquals("Inventory details assumes upper case value for Bulk Compl in future comparisons", "Y", details.getBulkCompl());
	}

	protected InventoryImportParser.InventoryRowConverter createForTestingRowConverter(final Workbook workbook) {
		final Map<String, Location> locationMap = this.parser.convertToLocationMap(this.testLocationList);
		return new InventoryImportParser.InventoryRowConverter(workbook, InventoryImportParserTest.DUMMY_INDEX,
				InventoryImportParser.INVENTORY_SHEET, this.inventoryHeaderLabelsMap.size(), this.inventoryHeaderLabelsMap, locationMap,
				this.scaleTDI.createScale(), this.ontologyDataManager);
	}

	protected List<Location> createDummyLocationList() {
		final List<Location> locationList = new ArrayList<>();

		Location location = new Location();
		location.setLocid(1);
		location.setLabbr("TEST1");
		location.setLname("Test Location 1");
		locationList.add(location);

		location = new Location();
		location.setLocid(2);
		location.setLabbr("TEST2");
		location.setLname("Test Location 2");
		locationList.add(location);

		return locationList;
	}

	@Test
	public void testParseWorkbook_Crosses() throws Exception {
		final GermplasmListType germplasmListType = GermplasmListType.CROSSES;
		final Map<String, Object> additionalParams = new HashMap<>();
		additionalParams.put(InventoryImportParser.HEADERS_MAP_PARAM_KEY, InventoryHeaderLabels.headers(GermplasmListType.CROSSES));
		additionalParams.put(InventoryImportParser.LIST_ID_PARAM_KEY, InventoryImportParserTest.TEST_LIST_ID);
		additionalParams.put(InventoryImportParser.GERMPLASM_LIST_TYPE_PARAM_KEY, InventoryImportParserTest.TEST_GERMPLASM_LIST_TYPE);
		final ImportedInventoryList importedInventoryList =
				this.parser.parseWorkbook(this.createWorkbook(germplasmListType), additionalParams);
		Assert.assertNotNull(importedInventoryList);
	}

	@Test
	public void testIsAmountHeaderValidTrue() {
		final boolean isAmountHeaderValid = this.parser.isAmountHeaderValid(InventoryImportParserTest.AMOUNT_HEADER);
		Assert.assertTrue("The header should be valid", isAmountHeaderValid);
	}

	@Test
	public void testIsAmountHeaderValidFalse() {
		final boolean isAmountHeaderValid = this.parser.isAmountHeaderValid("SEED_AMOUNT_X");
		Assert.assertFalse("The header should be invalid", isAmountHeaderValid);
	}
}
