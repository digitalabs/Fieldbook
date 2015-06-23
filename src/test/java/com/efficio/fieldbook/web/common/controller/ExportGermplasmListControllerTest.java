
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ExportGermplasmListForm;
import com.efficio.fieldbook.web.common.service.ExportGermplasmListService;
import com.efficio.fieldbook.web.util.FieldbookProperties;

public class ExportGermplasmListControllerTest {

	private static final long LIST_DATE = 20141112L;
	private static final String SAMPLE_NOTES = "Sample Notes";
	private static final String LST = "LST";
	private static final String SAMPLE_DESCRIPTION = "Sample description";
	private static final String SAMPLE_LIST = "Sample List";
	private static final String LIST_NAME = "LIST NAME";
	private static final int CURRENT_USER_ID = 1;
	private static final int LIST_STATUS = 1;
	// Columns
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
	
	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private ExportGermplasmListController exportGermplasmListController;

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);

		this.exportGermplasmListController = Mockito.spy(new ExportGermplasmListController());
		this.exportGermplasmListController.setUserSelection(this.userSelection);
		this.exportGermplasmListController.setExportGermplasmListService(this.exportGermplasmListService);
		this.exportGermplasmListController.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);

		try {
			Mockito.doReturn(this.createStandardVariable(TermId.ENTRY_NO.getId(), ExportGermplasmListControllerTest.ENTRY_NO))
					.when(this.ontologyService).getStandardVariable(TermId.ENTRY_NO.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.DESIG.getId(), ExportGermplasmListControllerTest.DESIGNATION))
					.when(this.ontologyService).getStandardVariable(TermId.DESIG.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.GID.getId(), ExportGermplasmListControllerTest.GID))
					.when(this.ontologyService).getStandardVariable(TermId.GID.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.CROSS.getId(), ExportGermplasmListControllerTest.PARENTAGE))
					.when(this.ontologyService).getStandardVariable(TermId.CROSS.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.SEED_SOURCE.getId(), ExportGermplasmListControllerTest.SEED_SOURCE))
					.when(this.ontologyService).getStandardVariable(TermId.SEED_SOURCE.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.ENTRY_CODE.getId(), ExportGermplasmListControllerTest.ENTRY_CODE))
					.when(this.ontologyService).getStandardVariable(TermId.ENTRY_CODE.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(
					this.createStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(), ExportGermplasmListControllerTest.ENTRY_NUMBER_STORAGE))
					.when(this.ontologyService).getStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.createStandardVariable(TermId.CHECK.getId(), ExportGermplasmListControllerTest.CHECK))
					.when(this.ontologyService).getStandardVariable(TermId.CHECK.getId(),contextUtil.getCurrentProgramUUID());
			Mockito.doReturn(this.fieldbookProperties).when(this.exportGermplasmListController).getFieldbookProperties();
			Mockito.doReturn(this.getPlotLevelList()).when(this.userSelection).getPlotsLevelList();

			Mockito.doReturn(this.getGermplasmList()).when(this.fieldbookMiddlewareService).getGermplasmListById(Matchers.anyInt());

			Mockito.when(this.userSelection.getImportedGermplasmMainInfo()).thenReturn(Mockito.mock(ImportedGermplasmMainInfo.class));
			Mockito.when(this.userSelection.getImportedGermplasmMainInfo().getListId()).thenReturn(1);
			Mockito.when(this.userSelection.getImportedGermplasmMainInfo().getListName()).thenReturn(
					ExportGermplasmListControllerTest.LIST_NAME);
		} catch (MiddlewareException e) {
			// do nothing
		}

	}

	@Test
	public void test_exportGermplasmListExcelForTrial() {

		ExportGermplasmListForm form = new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");

		try {

			this.exportGermplasmListController.exportGermplasmList(form, ExportGermplasmListControllerTest.EXCEL_TYPE,
					ExportGermplasmListControllerTest.TRIAL_TYPE, this.req, this.response);
			Mockito.verify(this.exportGermplasmListService, Mockito.times(1)).exportGermplasmListXLS(Matchers.anyString(),
					Matchers.anyInt(), Matchers.any(Map.class), Matchers.anyBoolean());

		} catch (GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void test_exportGermplasmListExcelForNursery() {

		ExportGermplasmListForm form = new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");

		try {
			this.exportGermplasmListController.exportGermplasmList(form, ExportGermplasmListControllerTest.EXCEL_TYPE,
					ExportGermplasmListControllerTest.NURSERY_TYPE, this.req, this.response);
			Mockito.verify(this.exportGermplasmListService, Mockito.times(1)).exportGermplasmListXLS(Matchers.anyString(),
					Matchers.anyInt(), Matchers.any(Map.class), Matchers.anyBoolean());

		} catch (GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void test_exportGermplasmListCSVForTrial() {

		ExportGermplasmListForm form = new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");

		try {
			this.exportGermplasmListController.exportGermplasmList(form, ExportGermplasmListControllerTest.CSV_TYPE,
					ExportGermplasmListControllerTest.TRIAL_TYPE, this.req, this.response);
			Mockito.verify(this.exportGermplasmListService, Mockito.times(1)).exportGermplasmListCSV(Matchers.anyString(),
					Matchers.any(Map.class), Matchers.anyBoolean());
		} catch (GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void test_exportGermplasmListCSVForNursery() {

		ExportGermplasmListForm form = new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");

		try {
			this.exportGermplasmListController.exportGermplasmList(form, ExportGermplasmListControllerTest.CSV_TYPE,
					ExportGermplasmListControllerTest.NURSERY_TYPE, this.req, this.response);
			Mockito.verify(this.exportGermplasmListService, Mockito.times(1)).exportGermplasmListCSV(Matchers.anyString(),
					Matchers.any(Map.class), Matchers.anyBoolean());
		} catch (GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void test_exportGermplasmListCSVForNursery_NoSelectedGermplasmList() {

		try {
			Mockito.doReturn(null).when(this.fieldbookMiddlewareService).getGermplasmListById(Matchers.anyInt());
		} catch (MiddlewareQueryException e1) {
			Assert.fail();
		}
		Mockito.when(this.userSelection.getImportedGermplasmMainInfo()).thenReturn(null);

		ExportGermplasmListForm form = new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");

		try {
			this.exportGermplasmListController.exportGermplasmList(form, ExportGermplasmListControllerTest.CSV_TYPE,
					ExportGermplasmListControllerTest.NURSERY_TYPE, this.req, this.response);
			Mockito.verify(this.exportGermplasmListService, Mockito.times(0)).exportGermplasmListCSV(Matchers.anyString(),
					Matchers.any(Map.class), Matchers.anyBoolean());
		} catch (GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void test_getVisibleColumnsMapTrial() {

		String[] termIds = new String[] {String.valueOf(TermId.CHECK.getId())};
		Map<String, Boolean> result = this.exportGermplasmListController.getVisibleColumnsMap(termIds, false);

		Assert.assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));

		Assert.assertTrue(result.get(String.valueOf(TermId.CHECK.getId())));

		Assert.assertFalse(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		Assert.assertFalse(result.get(String.valueOf(TermId.CROSS.getId())));
		Assert.assertFalse(result.get(String.valueOf(TermId.SEED_SOURCE.getId())));
		Assert.assertFalse(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));

	}

	@Test
	public void test_getVisibleColumnsMapNursery() {

		String[] termIds = new String[] {"0"};
		Map<String, Boolean> result = this.exportGermplasmListController.getVisibleColumnsMap(termIds, true);

		Assert.assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.CROSS.getId())));

		Assert.assertNull(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		Assert.assertNull(result.get(String.valueOf(TermId.SEED_SOURCE.getId())));
		Assert.assertNull(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));
		Assert.assertNull(result.get(String.valueOf(TermId.CHECK.getId())));
	}

	@Test
	public void test_getVisibleColumnsMapWithNoVisibleColumns() {

		String[] termIds = new String[] {"0"};

		Map<String, Boolean> result = this.exportGermplasmListController.getVisibleColumnsMap(termIds, false);

		Assert.assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));

		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.CROSS.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.SEED_SOURCE.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.CHECK.getId())));
	}

	private List<SettingDetail> getPlotLevelList() {
		List<SettingDetail> plotLevelList = new ArrayList<>();

		for (Entry<String, Boolean> entry : this.getVisibleColumnMap().entrySet()) {
			plotLevelList.add(this.generateSettingDetail(Integer.valueOf(entry.getKey())));
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

	private SettingDetail generateSettingDetail(Integer termId) {
		SettingDetail settingDetail = new SettingDetail();
		settingDetail.setHidden(false);
		SettingVariable var = new SettingVariable();
		var.setCvTermId(termId);
		settingDetail.setVariable(var);

		StandardVariable stdVar;
		try {
			stdVar = this.ontologyService.getStandardVariable(termId,contextUtil.getCurrentProgramUUID());

			settingDetail.getVariable().setName(stdVar.getName());
			settingDetail.getVariable().setDescription(stdVar.getDescription());
			settingDetail.getVariable().setProperty(stdVar.getProperty().getName());
			settingDetail.getVariable().setScale(stdVar.getScale().getName());
			settingDetail.getVariable().setMethod(stdVar.getMethod().getName());
			settingDetail.getVariable().setDataType(stdVar.getDataType().getName());

		} catch (MiddlewareException e) {
			// do nothing
		}

		return settingDetail;
	}

	private StandardVariable createStandardVariable(int id, String name) {
		StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(ExportGermplasmListControllerTest.TEST_DESCRIPTION);

		Term prop = new Term();
		prop.setName(ExportGermplasmListControllerTest.TEST_PROPERTY);
		stdVar.setProperty(prop);

		Term scale = new Term();
		scale.setName(ExportGermplasmListControllerTest.TEST_SCALE);
		stdVar.setScale(scale);

		Term method = new Term();
		method.setName(ExportGermplasmListControllerTest.TEST_METHOD);
		stdVar.setMethod(method);

		Term dataType = new Term();
		dataType.setName(ExportGermplasmListControllerTest.NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);

		return stdVar;
	}

	@Test
	public void testSetExportListTypeFromOriginalGermplasmIfListRefIsNull() throws MiddlewareQueryException {
		ExportGermplasmListController controller = new ExportGermplasmListController();
		GermplasmList list = this.getGermplasmList();
		controller.setExportListTypeFromOriginalGermplasm(list);
		Assert.assertEquals("List type should not change since there is not list ref for the germplasm list",
				ExportGermplasmListControllerTest.LST, list.getType());
	}

	@Test
	public void testSetExportListTypeFromOriginalGermplasmIfListRefIsNotNull() throws MiddlewareQueryException {
		ExportGermplasmListController controller = new ExportGermplasmListController();
		GermplasmList list = this.getGermplasmList();
		list.setListRef(5);
		GermplasmList listRef = this.getGermplasmList();
		String harvest = "HARVEST";
		listRef.setType(harvest);
		Mockito.doReturn(listRef).when(this.fieldbookMiddlewareService).getGermplasmListById(Matchers.anyInt());
		controller.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);

		controller.setExportListTypeFromOriginalGermplasm(list);
		Assert.assertEquals("List type should  change since there is a list ref for the germplasm list", harvest, list.getType());
	}

	@Test
	public void testSetExportListTypeFromOriginalGermplasmIfListRefIsNotNullAndNotDeleted() throws MiddlewareQueryException {
		ExportGermplasmListController controller = new ExportGermplasmListController();
		GermplasmList list = this.getGermplasmList();
		list.setListRef(5);
		GermplasmList listRef = this.getGermplasmList();
		String harvest = "HARVEST";
		listRef.setType(harvest);
		Mockito.doReturn(listRef).when(this.fieldbookMiddlewareService).getGermplasmListById(Matchers.anyInt());
		controller.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);

		controller.setExportListTypeFromOriginalGermplasm(list);
		Assert.assertEquals("List type should  change since there is a list ref for the germplasm list", harvest, list.getType());
	}

	@Test
	public void testSetExportListTypeFromOriginalGermplasmIfListRefIsNotNullAndDeleted() throws MiddlewareQueryException {
		ExportGermplasmListController controller = new ExportGermplasmListController();
		GermplasmList list = this.getGermplasmList();
		list.setListRef(5);
		list.setType("SAMPLE");
		GermplasmList listRef = this.getGermplasmList();
		String harvest = "HARVEST";
		listRef.setType(harvest);

		listRef.setStatus(ExportGermplasmListControllerTest.STATUS_DELETED);
		Mockito.doReturn(listRef).when(this.fieldbookMiddlewareService).getGermplasmListById(Matchers.anyInt());
		controller.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);

		controller.setExportListTypeFromOriginalGermplasm(list);
		Assert.assertEquals("List type should be LST since the ref list id is deleted", ExportGermplasmListControllerTest.LST,
				list.getType());
	}

	@Test
	public void testSetExportListTypeFromOriginalGermplasmIfListRefIsNotNullButListIsNull() throws MiddlewareQueryException {
		ExportGermplasmListController controller = new ExportGermplasmListController();
		GermplasmList list = this.getGermplasmList();
		list.setListRef(5);
		GermplasmList listRef = this.getGermplasmList();
		String harvest = "HARVEST";
		listRef.setType(harvest);
		Mockito.doReturn(null).when(this.fieldbookMiddlewareService).getGermplasmListById(Matchers.anyInt());
		controller.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);

		controller.setExportListTypeFromOriginalGermplasm(list);
		Assert.assertEquals("List type should not change since there is not list ref for the germplasm list",
				ExportGermplasmListControllerTest.LST, list.getType());
	}

	private GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName(ExportGermplasmListControllerTest.SAMPLE_LIST);
		germplasmList.setUserId(ExportGermplasmListControllerTest.CURRENT_USER_ID);
		germplasmList.setDescription(ExportGermplasmListControllerTest.SAMPLE_DESCRIPTION);
		germplasmList.setType(ExportGermplasmListControllerTest.LST);
		germplasmList.setDate(ExportGermplasmListControllerTest.LIST_DATE);
		germplasmList.setNotes(ExportGermplasmListControllerTest.SAMPLE_NOTES);
		germplasmList.setStatus(ExportGermplasmListControllerTest.LIST_STATUS);

		return germplasmList;
	}

}
