package com.efficio.fieldbook.web.common.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.efficio.fieldbook.web.common.service.ExportGermplasmListService;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import com.efficio.fieldbook.service.GermplasmExportService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;


public class ExportGermplasmListServiceTest {

	private static final long LIST_DATE = 20141112L;
	private static final String SAMPLE_NOTES = "Sample Notes";
	private static final String LST = "LST";
	private static final String SAMPLE_DESCRIPTION = "Sample description";
	private static final String SAMPLE_LIST = "Sample List";
	private static final String CATEG_CODE_VALUE = "CATEG CODE VAL";
	private static final String DESIG_VALUE = "DESIG VALUE";
	private static final String CROSS_VALUE = "Cross value";
	private static final String SOURCE_VALUE = "Source value";
	private static final String ENTRY_CODE_VALUE = "9742";
	private static final String ENTRY_NO_VALUE = "1";
	private static final String GID_VALUE = "12345";
	private static final String CHECK_VALUE = "1";
	private static final String NUMERIC_VARIABLE = "NUMERIC VARIABLE";
	private static final String TEST_METHOD = "TEST METHOD";
	private static final String TEST_SCALE = "TEST SCALE";
	private static final String TEST_PROPERTY = "TEST PROPERTY";
	private static final String TEST_DESCRIPTION = "TEST DESCRIPTION";

	private static final String CURRENT_USER_NAME = "User User";
	private static final int CURRENT_USER_ID = 1;
	private static final int LIST_ID = 1;
	//Columns
    private static final String ENTRY_NO = "entryNo";
    private static final String GID = "gid";
    private static final String ENTRY_CODE = "entryCode";
    private static final String DESIGNATION = "desig";
    private static final String PARENTAGE = "parentage";
    private static final String SEED_SOURCE = "seedSource";
    private static final String CHECK = "check";
    private static final String ENTRY_NUMBER_STORAGE = "entryNoStorage";
	
	private ExportGermplasmListServiceImpl exportGermplasmListService;
	private String testFileName;
	
	@Mock
	private OntologyService ontologyService;
	
	@Mock
	private UserSelection userSelection;

	@Mock
	private ContextUtil contextUtil;
	
	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@InjectMocks
	private ExportGermplasmListServiceImpl exportGermplasmListServiceOrigin;
	
	private GermplasmExportService exportServiceNursery;
	private GermplasmExportService exportServiceTrial;

	@Before
	public void setUp() throws MiddlewareQueryException {
		
		MockitoAnnotations.initMocks(this);
		
		testFileName = "test.csv";
		
		exportGermplasmListService = spy(exportGermplasmListServiceOrigin);

		exportServiceTrial = spy(new GermplasmExportService(ontologyService, userSelection, false));
		doReturn(generateImportedGermplasms()).when(exportServiceTrial).getImportedGermplasms();
		
		exportServiceNursery = spy(new GermplasmExportService(ontologyService, userSelection, true));
		doReturn(generateImportedGermplasms()).when(exportServiceNursery).getImportedGermplasms();

		when(contextUtil.getCurrentUserLocalId()).thenReturn(CURRENT_USER_ID);

		doReturn(generateImportedGermplasms()).when(exportGermplasmListService).getImportedGermplasm();
		doReturn(exportServiceTrial).when(exportGermplasmListService).getExportService(userSelection, false);
		doReturn(exportServiceNursery).when(exportGermplasmListService).getExportService(userSelection, true);

		doReturn(createStandardVariable(TermId.ENTRY_NO.getId(), ENTRY_NO)).when(ontologyService).getStandardVariable(TermId.ENTRY_NO.getId());
		doReturn(createStandardVariable(TermId.DESIG.getId(), DESIGNATION)).when(ontologyService).getStandardVariable(TermId.DESIG.getId());
		doReturn(createStandardVariable(TermId.GID.getId(), GID)).when(ontologyService).getStandardVariable(TermId.GID.getId());
		doReturn(createStandardVariable(TermId.CROSS.getId(), PARENTAGE)).when(ontologyService).getStandardVariable(TermId.CROSS.getId());
		doReturn(createStandardVariable(TermId.SEED_SOURCE.getId(), SEED_SOURCE)).when(ontologyService).getStandardVariable(TermId.SEED_SOURCE.getId());
		doReturn(createStandardVariable(TermId.ENTRY_CODE.getId(), ENTRY_CODE)).when(ontologyService).getStandardVariable(TermId.ENTRY_CODE.getId());
		doReturn(createStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(),ENTRY_NUMBER_STORAGE)).when(ontologyService).getStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId());
		doReturn(createStandardVariable(TermId.CHECK.getId(),CHECK)).when(ontologyService).getStandardVariable(TermId.CHECK.getId());
		doReturn(getPlotLevelList()).when(userSelection).getPlotsLevelList();
		doReturn(getGermplasmList()).when(fieldbookMiddlewareService).getGermplasmListById(LIST_ID);
		doReturn(CURRENT_USER_NAME).when(fieldbookMiddlewareService).getOwnerListName(CURRENT_USER_ID);

	}
	
	@Test 
	public void test_exportGermplasmListXLS_ForTrialManager(){
			
			try {
				exportGermplasmListService.exportGermplasmListXLS(testFileName, 1, getVisibleColumnMap(), false);
				verify(exportServiceTrial, times(1)).generateGermplasmListExcelFile(any(GermplasmListExportInputValues.class));
			} catch (GermplasmListExporterException e) {
				Assert.fail();
			}
		
	}
	
	@Test 
	public void test_exportGermplasmListXLS_ForNurseryManager(){
			
			try {
				exportGermplasmListService.exportGermplasmListXLS(testFileName, 1, getVisibleColumnMap(), true);
				verify(exportServiceNursery, times(1)).generateGermplasmListExcelFile(any(GermplasmListExportInputValues.class));
			} catch (GermplasmListExporterException e) {
				Assert.fail();
			}
			
	}
	
	@Test 
	public void test_exportGermplasmListCSV_ForTrialManager(){
		
		try {
			
			exportGermplasmListService.exportGermplasmListCSV(testFileName, getVisibleColumnMap(), false);
			verify(exportServiceTrial, times(1)).generateCSVFile(any(List.class), any(List.class), anyString());
			
		} catch (GermplasmListExporterException e) {
			Assert.fail();
		} catch (IOException e){
			Assert.fail();
		}
		
	}
	
	@Test 
	public void test_exportGermplasmListCSV_ForNurseryManager(){
		
		try {
			
			exportGermplasmListService.exportGermplasmListCSV(testFileName, getVisibleColumnMap(), true);
			verify(exportServiceNursery, times(1)).generateCSVFile(any(List.class), any(List.class), anyString());
		} catch (GermplasmListExporterException e) {
			Assert.fail();
		} catch (IOException e){
			Assert.fail();
		}
		
	}
		
	
	@After
	public void tearDown() {
		File file = new File(testFileName);
		file.deleteOnExit();
	}
	
	@Test
	public void testGetExportColumnHeadersFromTable_Trial(){
	
		List<ExportColumnHeader> exportColumnHeaders = exportGermplasmListService.getExportColumnHeadersFromTable(getVisibleColumnMap(), false);
		
		
		assertEquals(6, exportColumnHeaders.size());
		assertTrue(exportColumnHeaders.get(0).isDisplay());
		assertTrue(exportColumnHeaders.get(1).isDisplay());
		assertTrue(exportColumnHeaders.get(2).isDisplay());
		assertTrue(exportColumnHeaders.get(3).isDisplay());
		assertTrue(exportColumnHeaders.get(4).isDisplay());
		assertTrue(exportColumnHeaders.get(5).isDisplay());
		
		assertEquals(DESIGNATION, exportColumnHeaders.get(0).getName());
		assertEquals(SEED_SOURCE, exportColumnHeaders.get(1).getName());
		assertEquals(ENTRY_CODE, exportColumnHeaders.get(2).getName());
		assertEquals(PARENTAGE, exportColumnHeaders.get(3).getName());
		assertEquals(ENTRY_NO, exportColumnHeaders.get(4).getName());
		assertEquals(GID, exportColumnHeaders.get(5).getName());
			
	}
	
	@Test
	public void testGetExportColumnHeadersFromTable_Nursery(){
	
		List<ExportColumnHeader> exportColumnHeaders = exportGermplasmListService.getExportColumnHeadersFromTable(getVisibleColumnMap(), true);
		
	
		assertEquals(6, exportColumnHeaders.size());
		assertTrue(exportColumnHeaders.get(0).isDisplay());
		assertTrue(exportColumnHeaders.get(1).isDisplay());
		assertTrue(exportColumnHeaders.get(2).isDisplay());
		assertTrue(exportColumnHeaders.get(3).isDisplay());
		assertTrue(exportColumnHeaders.get(4).isDisplay());
		assertTrue(exportColumnHeaders.get(5).isDisplay());
		
		assertEquals(GID, exportColumnHeaders.get(0).getName());
		assertEquals(PARENTAGE, exportColumnHeaders.get(1).getName());
		assertEquals(ENTRY_NO, exportColumnHeaders.get(2).getName());
		assertEquals(DESIGNATION, exportColumnHeaders.get(3).getName());
		assertEquals(SEED_SOURCE, exportColumnHeaders.get(4).getName());
		assertEquals(ENTRY_CODE, exportColumnHeaders.get(5).getName());
			
	}
	
	@Test
	public void testGetExportColumnValuesFromTable_Trial(){
		
	
		List<Map<Integer, ExportColumnValue>> exportColumnValues = exportGermplasmListService.getExportColumnValuesFromTable(getVisibleColumnMap(), false);
		assertEquals(1, exportColumnValues.size());
		
		Map<Integer, ExportColumnValue> row = exportColumnValues.get(0);
		assertEquals(DESIG_VALUE, row.get(TermId.DESIG.getId()).getValue());
		assertEquals(SOURCE_VALUE, row.get(TermId.SEED_SOURCE.getId()).getValue());
		assertEquals(ENTRY_CODE_VALUE, row.get(TermId.ENTRY_CODE.getId()).getValue());
		assertEquals(CROSS_VALUE, row.get(TermId.CROSS.getId()).getValue());
		assertEquals(ENTRY_NO_VALUE, row.get(TermId.ENTRY_NO.getId()).getValue());
		assertEquals(GID_VALUE, row.get(TermId.GID.getId()).getValue());	
		
	}
	
	@Test
	public void testGetExportColumnValuesFromTable_Nursery(){
		
	
		List<Map<Integer, ExportColumnValue>> exportColumnValues = exportGermplasmListService.getExportColumnValuesFromTable(getVisibleColumnMap(), false);
		assertEquals(1, exportColumnValues.size());
		
		Map<Integer, ExportColumnValue> row = exportColumnValues.get(0);
		assertEquals(DESIG_VALUE, row.get(TermId.DESIG.getId()).getValue());
		assertEquals(SOURCE_VALUE, row.get(TermId.SEED_SOURCE.getId()).getValue());
		assertEquals(ENTRY_CODE_VALUE, row.get(TermId.ENTRY_CODE.getId()).getValue());
		assertEquals(CROSS_VALUE, row.get(TermId.CROSS.getId()).getValue());
		assertEquals(ENTRY_NO_VALUE, row.get(TermId.ENTRY_NO.getId()).getValue());
		assertEquals(GID_VALUE, row.get(TermId.GID.getId()).getValue());	
		
	}
	
	@Test
	public void test_getCategoricalCodeValue(){
		SettingDetail settingDetail = generateSettingDetail(TermId.CHECK.getId());
		
	    List<ValueReference> possibleValues = new ArrayList<>();
	    ValueReference valReference =  new ValueReference();
	    valReference.setId(Integer.valueOf(CHECK_VALUE));
	    valReference.setKey(CHECK_VALUE);
	    valReference.setName(CATEG_CODE_VALUE);
	    possibleValues.add(valReference);
		
	    settingDetail.setPossibleValues(possibleValues);
		
		String categValue = exportGermplasmListService.getCategoricalCodeValue(generateImportedGermplasm(), settingDetail);
		
		assertEquals(CATEG_CODE_VALUE, categValue);
		
	}
	
	@Test
	public void test_getCategoricalCodeValuePossibleValuesIsNull(){
		SettingDetail settingDetail = generateSettingDetail(TermId.CHECK.getId());
	    settingDetail.setPossibleValues(null);
		
	    String categValue = exportGermplasmListService.getCategoricalCodeValue(generateImportedGermplasm(), settingDetail);
		
		assertEquals(CHECK_VALUE, categValue);
	}
	
	private List<ImportedGermplasm> generateImportedGermplasms() {
		
		List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
		importedGermplasms.add(generateImportedGermplasm());
		
		return importedGermplasms;
	}
	
	private ImportedGermplasm generateImportedGermplasm(){
		
		ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setCheck(CHECK_VALUE);
		importedGermplasm.setIndex(0);
		importedGermplasm.setGid(GID_VALUE);
		importedGermplasm.setEntryCode(ENTRY_CODE_VALUE);
		importedGermplasm.setEntryId(Integer.valueOf(ENTRY_NO_VALUE));
		importedGermplasm.setSource(SOURCE_VALUE);
		importedGermplasm.setCross(CROSS_VALUE);
		importedGermplasm.setDesig(DESIG_VALUE);
		
		return importedGermplasm;
		
	}
	
	private Map<String, Boolean> getVisibleColumnMap() {
		Map<String, Boolean> visibleColumnMap = new HashMap<String, Boolean>();
		
		visibleColumnMap.put(String.valueOf(TermId.GID.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.CROSS.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_NO.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.DESIG.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.SEED_SOURCE.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_CODE.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId()), false);
		visibleColumnMap.put(String.valueOf(TermId.CHECK.getId()), false);
		
		return visibleColumnMap;

	}
	
	private List<SettingDetail> getPlotLevelList() {
		List<SettingDetail> plotLevelList = new ArrayList<>();
		
		for (Entry<String, Boolean> entry : getVisibleColumnMap().entrySet()){
			plotLevelList.add(generateSettingDetail(Integer.valueOf(entry.getKey())));
		}
		
		return plotLevelList;

	}
	
	private SettingDetail generateSettingDetail(Integer termId){
		SettingDetail settingDetail =  new SettingDetail();
		settingDetail.setHidden(false);
		SettingVariable var = new SettingVariable();
		var.setCvTermId(termId);
		settingDetail.setVariable(var);
		
		
		StandardVariable stdVar;
		try {
			stdVar = ontologyService.getStandardVariable(termId);
			
			settingDetail.getVariable().setName(stdVar.getName());
			settingDetail.getVariable().setDescription(stdVar.getDescription());
			settingDetail.getVariable().setProperty(stdVar.getProperty().getName());
			settingDetail.getVariable().setScale(stdVar.getScale().getName());
			settingDetail.getVariable().setMethod(stdVar.getMethod().getName());
			settingDetail.getVariable().setDataType(stdVar.getDataType().getName());
			
		} catch (MiddlewareQueryException e) {
			// do nothing
		}
		
		return settingDetail;
	}
	
	
	private StandardVariable createStandardVariable(int id, String name){
		StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(TEST_DESCRIPTION);
		
		Term prop = new Term();
		prop.setName(TEST_PROPERTY);
		stdVar.setProperty(prop);
		
		Term scale = new Term();
		scale.setName(TEST_SCALE);
		stdVar.setScale(scale);
		
		Term method = new Term();
		method.setName(TEST_METHOD);
		stdVar.setMethod(method);
		
		Term dataType = new Term();
		dataType.setName(NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);
		
		return stdVar;
	}
	
	private GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName(SAMPLE_LIST);
		germplasmList.setUserId(CURRENT_USER_ID);
		germplasmList.setDescription(SAMPLE_DESCRIPTION);
		germplasmList.setType(LST);
		germplasmList.setDate(LIST_DATE);
		germplasmList.setNotes(SAMPLE_NOTES);
		
		return germplasmList;
	}

}
