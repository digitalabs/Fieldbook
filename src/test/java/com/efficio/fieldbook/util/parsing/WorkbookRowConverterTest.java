package com.efficio.fieldbook.util.parsing;

import com.efficio.fieldbook.web.util.parsing.InventoryHeaderLabels;
import com.efficio.fieldbook.web.util.parsing.InventoryImportParser;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.WorkbookRowConverter;
import org.generationcp.commons.parsing.validation.ParsingValidator;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.pojos.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 3/12/2015
 * Time: 5:13 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class WorkbookRowConverterTest {

	private WorkbookRowConverter<InventoryDetails> dut;

	public static final String TEST_VALUE = "TEST VALUE";
	public static final String TEST_COLUMN = "TEST_COLUMN";

	@Before
	public void setUp() throws Exception {
		Map<InventoryHeaderLabels,Integer> inventoryHeaderLabelsMap = 
				InventoryHeaderLabels.headers(GermplasmListType.CROSSES);
		dut = new InventoryImportParser.InventoryRowConverter(mock(Workbook.class), 0, 0,
				inventoryHeaderLabelsMap.size(), inventoryHeaderLabelsMap,
				new HashMap<String, Location>(), new HashMap<String, Scale>());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testApplyValidations() throws FileParsingException{
		List<ParsingValidator> validatorList = new ArrayList<>();
		ParsingValidator validator = mock(ParsingValidator.class);
		when(validator.isParsedValueValid(anyString(),anyMap())).thenReturn(true);

		validatorList.add(validator);

		dut.applyValidation(TEST_VALUE, null, TEST_COLUMN, validatorList);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = FileParsingException.class)
	public void testApplyValidationsParseErrorOccur() throws FileParsingException {
		List<ParsingValidator> validatorList = new ArrayList<>();
		ParsingValidator validator = mock(ParsingValidator.class);
		when(validator.isParsedValueValid(anyString(),anyMap())).thenReturn(false);

		validatorList.add(validator);

		dut.applyValidation(TEST_VALUE, null, TEST_COLUMN, validatorList);
	}

	@Test
	public void testContinueTillBlankWithNonBlankValue() {
		WorkbookRowConverter.ContinueExpression continueExpression = new WorkbookRowConverter.ContinueTillBlank();
		Map<Integer, String> currentValue = new HashMap<>();
		currentValue.put(1, "NON_EMPTY_VALUE");
		assertTrue(continueExpression.shouldContinue(currentValue));
	}

	@Test
	public void testContinueTillBlankWithBlankValue() {
		WorkbookRowConverter.ContinueExpression continueExpression = new WorkbookRowConverter.ContinueTillBlank();

		assertFalse(continueExpression.shouldContinue(null));
	}

	@Test
	public void testConvertWorkbookRowsToObjectBlank() throws FileParsingException{
		WorkbookRowConverter<InventoryDetails> mole = spy(dut);

		doReturn(true).when(mole).isRowEmpty(anyInt(), anyInt(), anyInt());
		List<InventoryDetails> inventoryDetailList = mole.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		assertNotNull(inventoryDetailList);
		assertTrue(inventoryDetailList.isEmpty());
	}

	@Test
	public void testConvertWorkbookRowsToObject() throws FileParsingException {
		WorkbookRowConverter<InventoryDetails> mole = spy(dut);

		doReturn(false).doReturn(true).when(mole).isRowEmpty(anyInt(), anyInt(), anyInt());
		doReturn("").when(mole).getCellStringValue(anyInt(), anyInt(), anyInt());
		InventoryDetails details = mock(InventoryDetails.class);
		doReturn(details).when(mole).convertToObject(anyMap());

		List<InventoryDetails> inventoryDetailList = mole
				.convertWorkbookRowsToObject(new WorkbookRowConverter.ContinueTillBlank());

		assertNotNull(inventoryDetailList);
		assertFalse(inventoryDetailList.isEmpty());
		assertEquals(details, inventoryDetailList.get(0));
	}
}
