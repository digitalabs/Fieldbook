package com.efficio.fieldbook.web.common.service.impl;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.util.ImportStudyUtil;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class CsvWorkbookParserTest {
	private static final String GW100_G_VALUE = "100";

		private static final String GW100_G = "GW100_g";

		private static final String PLOT_NO = "PLOT_NO";

		private static final String ENTRY_NO = "ENTRY_NO";

		private static final String DESIG = "DESIG";

		private static final String GID = "GID";

		private final String[] rowHeaders =
		{"ENTRY_TYPE", GID, "DESIGNATION", ENTRY_NO, "REP_NO", PLOT_NO};
	
	@Mock
	private Map<Integer, List<String>> csvMap;
	
	@Mock
	private ContextUtil contextUtil;
	
	@Mock
	private OntologyDataManager ontologyDataManager;
	
	@InjectMocks
	private CsvWorkbookParser csvWorkbookParser;
	
	private Workbook workbook;
	
	private Map<String, MeasurementRow> rowsMap;
	
	@Before
	public void setUp(){
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook(1, StudyType.N);
		this.rowsMap = ImportStudyUtil.createMeasurementRowsMap(workbook.getObservations(), "1", workbook.isNursery());
		this.csvWorkbookParser = new CsvWorkbookParser(workbook, "1", rowsMap);
		this.csvWorkbookParser.setContextUtil(this.contextUtil);
		this.csvWorkbookParser.setOntologyDataManager(this.ontologyDataManager);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn("1");
		Mockito.when(this.ontologyDataManager.findStandardVariablesByNameOrSynonym(Matchers.anyString(), Matchers.anyString())).thenReturn(new HashSet<StandardVariable>());
	}
	
	@Test
	public void testIsValidHeaderNamesTrue(){
		Assert.assertTrue("The headers should be valid.", this.csvWorkbookParser.isValidHeaderNames(rowHeaders));
	}
	
	@Test
	public void testIsValidHeaderNamesFalse(){
		final String[] headers =	{"ENTRY_TYPE", GID, "DESIGNATION", ENTRY_NO, "REP_NO", "plot"};
		Assert.assertFalse("The headers should be valid.", this.csvWorkbookParser.isValidHeaderNames(headers));
	}
	
	@Test
	public void testGetLabelFromKsuRequiredColumnTermIdPresentInEnum () {
		MeasurementTestDataInitializer  measurementTestDataInitializer = new MeasurementTestDataInitializer();
		MeasurementVariable mVar = measurementTestDataInitializer.createMeasurementVariable(TermId.ENTRY_NO.getId(), 1);
		String label = csvWorkbookParser.getLabelFromRequiredColumn(mVar);
		Assert.assertEquals("The label should be ENTRY_NO",ENTRY_NO, label);
		
	}
	
	@Test
	public void testGetLabelFromKsuRequiredColumnTermIdNotPresentInEnum () {
		MeasurementTestDataInitializer  measurementTestDataInitializer = new MeasurementTestDataInitializer();
		MeasurementVariable mVar = measurementTestDataInitializer.createMeasurementVariable(1, 1);
		String label = csvWorkbookParser.getLabelFromRequiredColumn(mVar);
		Assert.assertEquals("The label should be " + mVar.getName(), mVar.getName(), label);
	}
	
	@Test
	public void testRenameHeadersIfPossibleAndCheckForAddedTraitsHasAddedTraits(){
		final String[] headers =	{"ENTRY_TYPE", GID, "DESIGNATION", ENTRY_NO, "REP_NO", "plot", "NEWTRAIT"};
		when(csvMap.get(0)).thenReturn(Arrays.asList(headers));
		this.csvWorkbookParser.renameHeadersIfPossibleAndCheckForAddedTraits(this.csvMap, this.workbook);
		HashSet<ChangeType> modes = this.csvWorkbookParser.getModes();
		Assert.assertTrue("Modes should contain ADDED_TRAITS ChangeType", modes.contains(ChangeType.ADDED_TRAITS));
	}
	
	@Test
	public void testRenameHeadersIfPossibleAndCheckForAddedTraitsHasNoAddedTraits(){
		final String[] headers = {GID, DESIG, "ENTRY", "PLOT"};
		when(csvMap.get(0)).thenReturn(Arrays.asList(headers));
		this.csvWorkbookParser.renameHeadersIfPossibleAndCheckForAddedTraits(this.csvMap, this.workbook);
		HashSet<ChangeType> modes = this.csvWorkbookParser.getModes();
		Assert.assertFalse("Modes should notcontain ADDED_TRAITS ChangeType", modes.contains(ChangeType.ADDED_TRAITS));
	}
	
	@Test
	public void testGetKeyIdentifierFromRow() throws FileParsingException{
		List<String> row = Arrays.asList("1", "1", "1");
		List<Integer> indexes = Arrays.asList(1, 1, 2);
		String keyIdentifier  = this.csvWorkbookParser.getKeyIdentifierFromRow(row, indexes);
		Assert.assertEquals("KeyIdendtifier should be 1-1-1", "1-1-1", keyIdentifier);
	}
	
	@Test (expected = FileParsingException.class)
	public void testGetKeyIdentifierFromRowWithFileParsingException() throws FileParsingException{
		List<String> row = Arrays.asList("1", "", "1");
		List<Integer> indexes = Arrays.asList(1, 1, 2);
		this.csvWorkbookParser.getKeyIdentifierFromRow(row, indexes);
	}
	
	@Test
	public void testGetRealNumericValueOfIntWithTrailingZeroes(){
		String realNumericValue = this.csvWorkbookParser.getRealNumericValue("1.00");
		Assert.assertEquals("The value should be 1", "1", realNumericValue);
	}
	
	@Test
	public void testGetRealNumericValueOfIntOfDoubleValue(){
		String realNumericValue = this.csvWorkbookParser.getRealNumericValue("1.25");
		Assert.assertEquals("The value should be 1.25", "1.25", realNumericValue);
	}
	
	@Test
	public void testImportDataToWorkbook() throws FileParsingException{
		Map<Integer, List<String>> csvMap = createCsvMap();
		this.csvWorkbookParser.importDataToWorkbook(csvMap, this.workbook, "1", rowsMap);
		MeasurementRow mRow = this.workbook.getObservations().get(0);
		MeasurementData mData = mRow.getMeasurementData(GW100_G);
		Assert.assertEquals("The value should be 100", mData.getValue(), GW100_G_VALUE);		
	}

	private Map<Integer, List<String>> createCsvMap() {
		Map<Integer, List<String>> csvMap = new HashMap<>();
		final List<String> headers = Arrays.asList(GID, DESIG, ENTRY_NO, PLOT_NO, GW100_G);
		csvMap.put(0, headers);
		final List<String> row = Arrays.asList("999999", "TIANDOUGOU-9", "0", "0", GW100_G_VALUE);
		csvMap.put(1, row);
		return csvMap;
	}
}
