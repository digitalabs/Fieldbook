package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ExportGermplasmListForm;
import com.efficio.fieldbook.web.common.service.ExportGermplasmListService;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import junit.framework.Assert;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class ExportGermplasmListControllerTest {
	
	private static final long LIST_DATE = 20141112L;
	private static final String SAMPLE_NOTES = "Sample Notes";
	private static final String LST = "LST";
	private static final String SAMPLE_DESCRIPTION = "Sample description";
	private static final String SAMPLE_LIST = "Sample List";
	private static final String LIST_NAME = "LIST NAME";
	private static final int CURRENT_USER_ID = 1;
	private static final int LIST_STATUS = 1;
	//Columns
    private static final String ENTRY_NO = "entryNo";
    private static final String GID = "gid";
    private static final String ENTRY_CODE = "entryCode";
    private static final String DESIGNATION = "desig";
    private static final String PARENTAGE = "parentage";
    private static final String SEED_SOURCE = "seedSource";
    private static final String CHECK = "check";
    private static final String ENTRY_NUMBER_STORAGE = "entryNoStorage";
    
	private static final String NUMERIC_VARIABLE = "NUMERIC VARIABLE";
	private static final String TEST_METHOD = "TEST METHOD";
	private static final String TEST_SCALE = "TEST SCALE";
	private static final String TEST_PROPERTY = "TEST PROPERTY";
	private static final String TEST_DESCRIPTION = "TEST DESCRIPTION";
	
	private static final int EXCEL_TYPE = 1;
	private static final int CSV_TYPE = 2;
	
	private static final String TRIAL_TYPE = "T";
	private static final String NURSERY_TYPE = "N";
	
	private static final Integer STATUS_DELETED = 9;

	@Mock
	private HttpServletResponse response;
	
	@Mock
	private HttpServletRequest req;
	
	@Mock
	private UserSelection userSelection;
	
	@Mock
	private OntologyService ontologyService;
	
	@Mock
	private FieldbookProperties fieldbookProperties;
	
	@Mock
	private FieldbookService fieldbookMiddlewareService;
	
	@Mock
	private ExportGermplasmListService exportGermplasmListService;
	

	private ExportGermplasmListController exportGermplasmListController;
	
	@Before
	public void setUp() {
		
		MockitoAnnotations.initMocks(this);
		
		exportGermplasmListController = spy(new ExportGermplasmListController());
		exportGermplasmListController.setUserSelection(userSelection);
		exportGermplasmListController.setExportGermplasmListService(exportGermplasmListService);
		exportGermplasmListController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		try {
			doReturn(createStandardVariable(TermId.ENTRY_NO.getId(), ENTRY_NO)).when(ontologyService).getStandardVariable(TermId.ENTRY_NO.getId());
			doReturn(createStandardVariable(TermId.DESIG.getId(), DESIGNATION)).when(ontologyService).getStandardVariable(TermId.DESIG.getId());
			doReturn(createStandardVariable(TermId.GID.getId(), GID)).when(ontologyService).getStandardVariable(TermId.GID.getId());
			doReturn(createStandardVariable(TermId.CROSS.getId(), PARENTAGE)).when(ontologyService).getStandardVariable(TermId.CROSS.getId());
			doReturn(createStandardVariable(TermId.SEED_SOURCE.getId(), SEED_SOURCE)).when(ontologyService).getStandardVariable(TermId.SEED_SOURCE.getId());
			doReturn(createStandardVariable(TermId.ENTRY_CODE.getId(), ENTRY_CODE)).when(ontologyService).getStandardVariable(TermId.ENTRY_CODE.getId());
			doReturn(createStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(),ENTRY_NUMBER_STORAGE)).when(ontologyService).getStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId());
			doReturn(createStandardVariable(TermId.CHECK.getId(),CHECK)).when(ontologyService).getStandardVariable(TermId.CHECK.getId());
			doReturn(fieldbookProperties).when(exportGermplasmListController).getFieldbookProperties();
			doReturn(getPlotLevelList()).when(userSelection).getPlotsLevelList();
			
			doReturn(getGermplasmList()).when(fieldbookMiddlewareService).getGermplasmListById(anyInt());
			
			when(userSelection.getImportedGermplasmMainInfo()).thenReturn(mock(ImportedGermplasmMainInfo.class));
			when(userSelection.getImportedGermplasmMainInfo().getListId()).thenReturn(1);
			when(userSelection.getImportedGermplasmMainInfo().getListName()).thenReturn(LIST_NAME);
		} catch (MiddlewareQueryException e) {
			//do nothing
		}
		
		
	}
	

	@Test
	public void test_exportGermplasmListExcelForTrial(){
		
		ExportGermplasmListForm form =  new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");
		
		try {
			
			exportGermplasmListController.exportGermplasmList(form, EXCEL_TYPE, TRIAL_TYPE, req, response);
			verify(exportGermplasmListService, times(1)).exportGermplasmListXLS(anyString(), anyInt(), any(Map.class), anyBoolean());
			
		} catch (GermplasmListExporterException e) {
			fail();
		}
		
	}
	
	@Test
	public void test_exportGermplasmListExcelForNursery(){
		
		ExportGermplasmListForm form =  new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");
		
		try {
			exportGermplasmListController.exportGermplasmList(form, EXCEL_TYPE, NURSERY_TYPE, req, response);
			verify(exportGermplasmListService, times(1)).exportGermplasmListXLS(anyString(), anyInt(), any(Map.class), anyBoolean());
			
		} catch (GermplasmListExporterException e) {
			fail();
		}
		
	}
	
	@Test
	public void test_exportGermplasmListCSVForTrial(){
		
		ExportGermplasmListForm form =  new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");
		
		try {
			exportGermplasmListController.exportGermplasmList(form, CSV_TYPE, TRIAL_TYPE, req, response);
			verify(exportGermplasmListService, times(1)).exportGermplasmListCSV(anyString(), any(Map.class), anyBoolean());
		} catch (GermplasmListExporterException e) {
			fail();
		}
		
	}
	
	@Test
	public void test_exportGermplasmListCSVForNursery(){
		
		ExportGermplasmListForm form =  new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");
		
		try {
			exportGermplasmListController.exportGermplasmList(form, CSV_TYPE, NURSERY_TYPE, req, response);
			verify(exportGermplasmListService, times(1)).exportGermplasmListCSV(anyString(), any(Map.class), anyBoolean());
		} catch (GermplasmListExporterException e) {
			fail();
		}
		
	}
	
	@Test
	public void test_exportGermplasmListCSVForNursery_NoSelectedGermplasmList(){
		
		try {
			doReturn(null).when(fieldbookMiddlewareService).getGermplasmListById(anyInt());
		} catch (MiddlewareQueryException e1) {
			fail();
		}
		when(userSelection.getImportedGermplasmMainInfo()).thenReturn(null);
		
		ExportGermplasmListForm form =  new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");
		
		try {
			exportGermplasmListController.exportGermplasmList(form, CSV_TYPE, NURSERY_TYPE, req, response);
			verify(exportGermplasmListService, times(0)).exportGermplasmListCSV(anyString(), any(Map.class), anyBoolean());
		} catch (GermplasmListExporterException e) {
			fail();
		}
		
	}
	
	
	
	@Test
	public void test_getVisibleColumnsMapTrial(){
		
		String[] termIds = new String[] {String.valueOf(TermId.CHECK.getId())};
		Map<String, Boolean> result = exportGermplasmListController.getVisibleColumnsMap(termIds, false);
		
		assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));
		
		assertTrue(result.get(String.valueOf(TermId.CHECK.getId())));
		
		assertFalse(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		assertFalse(result.get(String.valueOf(TermId.CROSS.getId())));
		assertFalse(result.get(String.valueOf(TermId.SEED_SOURCE.getId())));
		assertFalse(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));
		
		
	}
	
	@Test
	public void test_getVisibleColumnsMapNursery(){
		
		String[] termIds = new String[] {"0"};
		Map<String, Boolean> result = exportGermplasmListController.getVisibleColumnsMap(termIds, true);
		
		assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));
		assertTrue(result.get(String.valueOf(TermId.CROSS.getId())));
		
		assertNull(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		assertNull(result.get(String.valueOf(TermId.SEED_SOURCE.getId())));
		assertNull(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));
		assertNull(result.get(String.valueOf(TermId.CHECK.getId())));
	}
	
	@Test
	public void test_getVisibleColumnsMapWithNoVisibleColumns(){
		
		String[] termIds = new String[] {"0"};
		
		Map<String, Boolean> result = exportGermplasmListController.getVisibleColumnsMap(termIds, false);
		
		assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));
		
		assertTrue(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		assertTrue(result.get(String.valueOf(TermId.CROSS.getId())));
		assertTrue(result.get(String.valueOf(TermId.SEED_SOURCE.getId())));
		assertTrue(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));
		assertTrue(result.get(String.valueOf(TermId.CHECK.getId())));
	}
	
	private List<SettingDetail> getPlotLevelList() {
		List<SettingDetail> plotLevelList = new ArrayList<>();
		
		for (Entry<String, Boolean> entry : getVisibleColumnMap().entrySet()){
			plotLevelList.add(generateSettingDetail(Integer.valueOf(entry.getKey())));
		}
		
		return plotLevelList;

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
	
	@Test
	public void testSetExportListTypeFromOriginalGermplasmIfListRefIsNull() throws MiddlewareQueryException{
		ExportGermplasmListController controller = new ExportGermplasmListController();
		GermplasmList list = getGermplasmList();
		controller.setExportListTypeFromOriginalGermplasm(list);
		Assert.assertEquals("List type should not change since there is not list ref for the germplasm list", LST, list.getType());
	}
	
	@Test
	public void testSetExportListTypeFromOriginalGermplasmIfListRefIsNotNull() throws MiddlewareQueryException{
		ExportGermplasmListController controller = new ExportGermplasmListController();
		GermplasmList list = getGermplasmList();
		list.setListRef(5);
		GermplasmList listRef = getGermplasmList();
		String harvest = "HARVEST";
		listRef.setType(harvest);
		doReturn(listRef).when(fieldbookMiddlewareService).getGermplasmListById(anyInt());
		controller.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		controller.setExportListTypeFromOriginalGermplasm(list);
		Assert.assertEquals("List type should  change since there is a list ref for the germplasm list", harvest, list.getType());
	}
	
	@Test
	public void testSetExportListTypeFromOriginalGermplasmIfListRefIsNotNullAndNotDeleted() throws MiddlewareQueryException{
		ExportGermplasmListController controller = new ExportGermplasmListController();
		GermplasmList list = getGermplasmList();
		list.setListRef(5);
		GermplasmList listRef = getGermplasmList();
		String harvest = "HARVEST";
		listRef.setType(harvest);
		doReturn(listRef).when(fieldbookMiddlewareService).getGermplasmListById(anyInt());
		controller.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		controller.setExportListTypeFromOriginalGermplasm(list);
		Assert.assertEquals("List type should  change since there is a list ref for the germplasm list", harvest, list.getType());
	}
	
	@Test
	public void testSetExportListTypeFromOriginalGermplasmIfListRefIsNotNullAndDeleted() throws MiddlewareQueryException{
		ExportGermplasmListController controller = new ExportGermplasmListController();
		GermplasmList list = getGermplasmList();
		list.setListRef(5);
		list.setType("SAMPLE");
		GermplasmList listRef = getGermplasmList();
		String harvest = "HARVEST";
		listRef.setType(harvest);
		
		listRef.setStatus(STATUS_DELETED);
		doReturn(listRef).when(fieldbookMiddlewareService).getGermplasmListById(anyInt());
		controller.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		controller.setExportListTypeFromOriginalGermplasm(list);
		Assert.assertEquals("List type should be LST since the ref list id is deleted", LST, list.getType());
	}
	
	@Test
	public void testSetExportListTypeFromOriginalGermplasmIfListRefIsNotNullButListIsNull() throws MiddlewareQueryException{
		ExportGermplasmListController controller = new ExportGermplasmListController();
		GermplasmList list = getGermplasmList();
		list.setListRef(5);
		GermplasmList listRef = getGermplasmList();
		String harvest = "HARVEST";
		listRef.setType(harvest);
		doReturn(null).when(fieldbookMiddlewareService).getGermplasmListById(anyInt());
		controller.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		
		controller.setExportListTypeFromOriginalGermplasm(list);
		Assert.assertEquals("List type should not change since there is not list ref for the germplasm list", LST, list.getType());
	}
	
	private GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName(SAMPLE_LIST);
		germplasmList.setUserId(CURRENT_USER_ID);
		germplasmList.setDescription(SAMPLE_DESCRIPTION);
		germplasmList.setType(LST);
		germplasmList.setDate(LIST_DATE);
		germplasmList.setNotes(SAMPLE_NOTES);
		germplasmList.setStatus(LIST_STATUS);
		
		return germplasmList;
	}
	
	
	

}
