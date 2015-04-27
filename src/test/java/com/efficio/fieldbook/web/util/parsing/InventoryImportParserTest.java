package com.efficio.fieldbook.web.util.parsing;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.validation.ParseValidationMap;
import org.generationcp.commons.parsing.validation.ValueRangeValidator;
import org.generationcp.commons.parsing.validation.ValueTypeValidator;
import org.generationcp.commons.service.FileService;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/26/2015
 * Time: 4:42 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class InventoryImportParserTest {

	@Mock
	private FileService fileService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyService ontologyService;

	@InjectMocks
	private InventoryImportParser parser;

	private InventoryImportParser moled;

	private Workbook workbook;

	public static final String TEST_FILE_NAME = "Inventory_Import_Template_v1.xlsx";
	public static final int DUMMY_INDEX = 1;

	private List<Location> testLocationList;
	private List<Scale> testScaleList;

	@Before
	public void setUp() throws Exception {
		moled = spy(parser);
		File workbookFile = new File(ClassLoader.getSystemClassLoader().getResource(TEST_FILE_NAME).toURI());

		assert workbookFile.exists();

		workbook = WorkbookFactory.create(workbookFile);

		testLocationList = createDummyLocationList();
		testScaleList = createDummyScaleList();
		doReturn(testLocationList).when(fieldbookMiddlewareService).getAllLocations();
		doReturn(testScaleList).when(ontologyService).getAllInventoryScales();

	}

	@Test
	public void testBuildAllowedLocationList() {
		moled.setLocations(testLocationList);
		List<String> allowedLocationsList = moled.buildAllowedLocationsList();

		assertEquals(testLocationList.size(), allowedLocationsList.size());

	}

	@Test
	public void testBuildAllowedScaleList() {
		moled.setScales(testScaleList);
		List<String> allowedScaleList = moled.buildAllowedScaleList();

		assertEquals(testScaleList.size(), allowedScaleList.size());
	}

	@Test
	public void testConvertToLocationMap() {
		Map<String, Location> locationMap = moled.convertToLocationMap(testLocationList);

		assertEquals(locationMap.size(), testLocationList.size());
		Collection<Location> values = locationMap.values();
		for (Location location : testLocationList) {
			assertTrue(values.contains(location));
		}
	}

	@Test
	public void testConvertToScaleMap() {
		Map<String, Scale> scaleMap = moled.convertToScaleMap(testScaleList);

		assertEquals(scaleMap.size(), testScaleList.size());
		Collection<Scale> values = scaleMap.values();
		for (Scale scale : testScaleList) {
			assertTrue(values.contains(scale));
		}
	}

	@Test
	public void testSetupParsingValidations() {
		ParseValidationMap map = moled.setupIndividualColumnValidation();

		assertTrue(!map.getValidations(InventoryImportParser.InventoryHeaderLabels.LOCATION.ordinal()).isEmpty());
		assertTrue(!map.getValidations(InventoryImportParser.InventoryHeaderLabels.SCALE.ordinal()).isEmpty());
		assertTrue(!map.getValidations(InventoryImportParser.InventoryHeaderLabels.AMOUNT.ordinal()).isEmpty());
		assertTrue(!map.getValidations(InventoryImportParser.InventoryHeaderLabels.ENTRY.ordinal()).isEmpty());
		assertTrue(!map.getValidations(InventoryImportParser.InventoryHeaderLabels.GID.ordinal()).isEmpty());

		assertTrue(map.getValidations(InventoryImportParser.InventoryHeaderLabels.LOCATION.ordinal()).get(0) instanceof ValueRangeValidator);
		assertTrue(map.getValidations(InventoryImportParser.InventoryHeaderLabels.SCALE.ordinal()).get(0) instanceof ValueRangeValidator);
		assertTrue(map.getValidations(InventoryImportParser.InventoryHeaderLabels.AMOUNT.ordinal()).get(0) instanceof ValueTypeValidator);
	}

	@Test(expected = FileParsingException.class)
	public void testObjectConversionNotAllInventoryItemsPresent() throws FileParsingException{
		InventoryImportParser.InventoryRowConverter rowConverter = createForTestingRowConverter();
		Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(InventoryImportParser.InventoryHeaderLabels.ENTRY.ordinal(), "1");
		testRowValue.put(InventoryImportParser.InventoryHeaderLabels.GID.ordinal(), "-10");
		testRowValue.put(InventoryImportParser.InventoryHeaderLabels.LOCATION.ordinal(), "DUMMY LOCATION");

		// only location has a value out of the three inventory related columns, an exception needs to be thrown
		rowConverter.convertToObject(testRowValue);
	}

	@Test
	public void testObjectConversionNoInventoryItemsPresent() throws FileParsingException {
		InventoryImportParser.InventoryRowConverter rowConverter = createForTestingRowConverter();
		Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(InventoryImportParser.InventoryHeaderLabels.ENTRY.ordinal(), "1");
		testRowValue.put(InventoryImportParser.InventoryHeaderLabels.GID.ordinal(), "-10");

		InventoryDetails details = rowConverter.convertToObject(testRowValue);
		assertNotNull(
				"Inventory details could not be properly created when all inventory related columns are blank",
				details);
		assertNull(details.getAmount());
		assertNull(details.getComment());
	}

	@Test
	public void testObjectConversionAllInventoryItemsPresent() throws FileParsingException {
		InventoryImportParser.InventoryRowConverter rowConverter = createForTestingRowConverter();
		Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(InventoryImportParser.InventoryHeaderLabels.ENTRY.ordinal(), "1");
		testRowValue.put(InventoryImportParser.InventoryHeaderLabels.GID.ordinal(), "-10");
		testRowValue.put(InventoryImportParser.InventoryHeaderLabels.LOCATION.ordinal(), "TEST1");
		testRowValue.put(InventoryImportParser.InventoryHeaderLabels.SCALE.ordinal(), "SCALE2");
		testRowValue.put(InventoryImportParser.InventoryHeaderLabels.AMOUNT.ordinal(), "15");

		InventoryDetails details = rowConverter.convertToObject(testRowValue);
		assertNotNull(
				"Inventory details could not be properly created when all inventory related columns are present",
				details);

		assertEquals(details.getAmount(), 15.0, 0);
		assertEquals("Inventory details not created with proper location abbr", "TEST1", details.getLocationAbbr());
		assertEquals("Inventory details not created with proper location id", new Integer(1), details.getLocationId());
		assertEquals("Inventory details not created with proper location name", "Test Location 1", details.getLocationName());
		assertEquals("Inventory details not created with proper scale id", new Integer(2), details.getScaleId());
		assertEquals("Inventory details not created with proper scale name", "SCALE2", details.getScaleName());
	}

	protected InventoryImportParser.InventoryRowConverter createForTestingRowConverter() {
		Map<String, Location> locationMap = moled.convertToLocationMap(testLocationList);
		Map<String, Scale> scaleMap = moled.convertToScaleMap(testScaleList);

		return new InventoryImportParser.InventoryRowConverter(workbook, DUMMY_INDEX,
				InventoryImportParser.INVENTORY_SHEET,
				InventoryImportParser.HEADER_LABEL_ARRAY.length,
				InventoryImportParser.HEADER_LABEL_ARRAY, locationMap, scaleMap);
	}

	protected List<Location> createDummyLocationList() {
		List<Location> locationList = new ArrayList<>();

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

	protected List<Scale> createDummyScaleList() {
		List<Scale> scaleList = new ArrayList<>();

		Scale scale = new Scale(new Term(1, "SCALE1", "Test"));
		scale.setDisplayName("Test Scale 1");
		scaleList.add(scale);

		scale = new Scale(new Term(2, "SCALE2", "Test"));
		scale.setDisplayName("Test Display 2");
		scaleList.add(scale);

		return scaleList;
	}
}

