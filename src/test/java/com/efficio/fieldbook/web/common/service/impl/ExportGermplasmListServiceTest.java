
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.service.GermplasmExportService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;

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
	// Columns
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

		this.testFileName = "test.csv";

		this.exportGermplasmListService = Mockito.spy(this.exportGermplasmListServiceOrigin);

		this.exportServiceTrial = Mockito.spy(new GermplasmExportService(this.ontologyService, this.userSelection, false));
		Mockito.doReturn(this.generateImportedGermplasms()).when(this.exportServiceTrial).getImportedGermplasms();

		this.exportServiceNursery = Mockito.spy(new GermplasmExportService(this.ontologyService, this.userSelection, true));
		Mockito.doReturn(this.generateImportedGermplasms()).when(this.exportServiceNursery).getImportedGermplasms();

		Mockito.when(this.contextUtil.getCurrentUserLocalId()).thenReturn(ExportGermplasmListServiceTest.CURRENT_USER_ID);

		Mockito.doReturn(this.generateImportedGermplasms()).when(this.exportGermplasmListService).getImportedGermplasm();
		Mockito.doReturn(this.exportServiceTrial).when(this.exportGermplasmListService).getExportService(this.userSelection, false);
		Mockito.doReturn(this.exportServiceNursery).when(this.exportGermplasmListService).getExportService(this.userSelection, true);

		Mockito.doReturn(this.createStandardVariable(TermId.ENTRY_NO.getId(), ExportGermplasmListServiceTest.ENTRY_NO))
				.when(this.ontologyService).getStandardVariable(TermId.ENTRY_NO.getId());
		Mockito.doReturn(this.createStandardVariable(TermId.DESIG.getId(), ExportGermplasmListServiceTest.DESIGNATION))
				.when(this.ontologyService).getStandardVariable(TermId.DESIG.getId());
		Mockito.doReturn(this.createStandardVariable(TermId.GID.getId(), ExportGermplasmListServiceTest.GID)).when(this.ontologyService)
				.getStandardVariable(TermId.GID.getId());
		Mockito.doReturn(this.createStandardVariable(TermId.CROSS.getId(), ExportGermplasmListServiceTest.PARENTAGE))
				.when(this.ontologyService).getStandardVariable(TermId.CROSS.getId());
		Mockito.doReturn(this.createStandardVariable(TermId.SEED_SOURCE.getId(), ExportGermplasmListServiceTest.SEED_SOURCE))
				.when(this.ontologyService).getStandardVariable(TermId.SEED_SOURCE.getId());
		Mockito.doReturn(this.createStandardVariable(TermId.ENTRY_CODE.getId(), ExportGermplasmListServiceTest.ENTRY_CODE))
				.when(this.ontologyService).getStandardVariable(TermId.ENTRY_CODE.getId());
		Mockito.doReturn(
				this.createStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(), ExportGermplasmListServiceTest.ENTRY_NUMBER_STORAGE))
				.when(this.ontologyService).getStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId());
		Mockito.doReturn(this.createStandardVariable(TermId.CHECK.getId(), ExportGermplasmListServiceTest.CHECK))
				.when(this.ontologyService).getStandardVariable(TermId.CHECK.getId());
		Mockito.doReturn(this.getPlotLevelList()).when(this.userSelection).getPlotsLevelList();
		Mockito.doReturn(this.getGermplasmList()).when(this.fieldbookMiddlewareService)
				.getGermplasmListById(ExportGermplasmListServiceTest.LIST_ID);
		Mockito.doReturn(ExportGermplasmListServiceTest.CURRENT_USER_NAME).when(this.fieldbookMiddlewareService)
				.getOwnerListName(ExportGermplasmListServiceTest.CURRENT_USER_ID);

	}

	@Test
	public void test_exportGermplasmListXLS_ForTrialManager() {

		try {
			this.exportGermplasmListService.exportGermplasmListXLS(this.testFileName, 1, this.getVisibleColumnMap(), false);
			Mockito.verify(this.exportServiceTrial, Mockito.times(1)).generateGermplasmListExcelFile(
					Matchers.any(GermplasmListExportInputValues.class));
		} catch (GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void test_exportGermplasmListXLS_ForNurseryManager() {

		try {
			this.exportGermplasmListService.exportGermplasmListXLS(this.testFileName, 1, this.getVisibleColumnMap(), true);
			Mockito.verify(this.exportServiceNursery, Mockito.times(1)).generateGermplasmListExcelFile(
					Matchers.any(GermplasmListExportInputValues.class));
		} catch (GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void test_exportGermplasmListCSV_ForTrialManager() {

		try {

			this.exportGermplasmListService.exportGermplasmListCSV(this.testFileName, this.getVisibleColumnMap(), false);
			Mockito.verify(this.exportServiceTrial, Mockito.times(1)).generateCSVFile(Matchers.any(List.class), Matchers.any(List.class),
					Matchers.anyString());

		} catch (GermplasmListExporterException e) {
			Assert.fail();
		} catch (IOException e) {
			Assert.fail();
		}

	}

	@Test
	public void test_exportGermplasmListCSV_ForNurseryManager() {

		try {

			this.exportGermplasmListService.exportGermplasmListCSV(this.testFileName, this.getVisibleColumnMap(), true);
			Mockito.verify(this.exportServiceNursery, Mockito.times(1)).generateCSVFile(Matchers.any(List.class), Matchers.any(List.class),
					Matchers.anyString());
		} catch (GermplasmListExporterException e) {
			Assert.fail();
		} catch (IOException e) {
			Assert.fail();
		}

	}

	@After
	public void tearDown() {
		File file = new File(this.testFileName);
		file.deleteOnExit();
	}

	@Test
	public void testGetExportColumnHeadersFromTable_Trial() {

		List<ExportColumnHeader> exportColumnHeaders =
				this.exportGermplasmListService.getExportColumnHeadersFromTable(this.getVisibleColumnMap(), false);

		Assert.assertEquals(6, exportColumnHeaders.size());
		Assert.assertTrue(exportColumnHeaders.get(0).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(1).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(2).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(3).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(4).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(5).isDisplay());

		Assert.assertEquals(ExportGermplasmListServiceTest.DESIGNATION, exportColumnHeaders.get(0).getName());
		Assert.assertEquals(ExportGermplasmListServiceTest.SEED_SOURCE, exportColumnHeaders.get(1).getName());
		Assert.assertEquals(ExportGermplasmListServiceTest.ENTRY_CODE, exportColumnHeaders.get(2).getName());
		Assert.assertEquals(ExportGermplasmListServiceTest.PARENTAGE, exportColumnHeaders.get(3).getName());
		Assert.assertEquals(ExportGermplasmListServiceTest.ENTRY_NO, exportColumnHeaders.get(4).getName());
		Assert.assertEquals(ExportGermplasmListServiceTest.GID, exportColumnHeaders.get(5).getName());

	}

	@Test
	public void testGetExportColumnHeadersFromTable_Nursery() {

		List<ExportColumnHeader> exportColumnHeaders =
				this.exportGermplasmListService.getExportColumnHeadersFromTable(this.getVisibleColumnMap(), true);

		Assert.assertEquals(6, exportColumnHeaders.size());
		Assert.assertTrue(exportColumnHeaders.get(0).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(1).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(2).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(3).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(4).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(5).isDisplay());

		Assert.assertEquals(ExportGermplasmListServiceTest.GID, exportColumnHeaders.get(0).getName());
		Assert.assertEquals(ExportGermplasmListServiceTest.PARENTAGE, exportColumnHeaders.get(1).getName());
		Assert.assertEquals(ExportGermplasmListServiceTest.ENTRY_NO, exportColumnHeaders.get(2).getName());
		Assert.assertEquals(ExportGermplasmListServiceTest.DESIGNATION, exportColumnHeaders.get(3).getName());
		Assert.assertEquals(ExportGermplasmListServiceTest.SEED_SOURCE, exportColumnHeaders.get(4).getName());
		Assert.assertEquals(ExportGermplasmListServiceTest.ENTRY_CODE, exportColumnHeaders.get(5).getName());

	}

	@Test
	public void testGetExportColumnValuesFromTable_Trial() {

		List<Map<Integer, ExportColumnValue>> exportColumnValues =
				this.exportGermplasmListService.getExportColumnValuesFromTable(this.getVisibleColumnMap(), false);
		Assert.assertEquals(1, exportColumnValues.size());

		Map<Integer, ExportColumnValue> row = exportColumnValues.get(0);
		Assert.assertEquals(ExportGermplasmListServiceTest.DESIG_VALUE, row.get(TermId.DESIG.getId()).getValue());
		Assert.assertEquals(ExportGermplasmListServiceTest.SOURCE_VALUE, row.get(TermId.SEED_SOURCE.getId()).getValue());
		Assert.assertEquals(ExportGermplasmListServiceTest.ENTRY_CODE_VALUE, row.get(TermId.ENTRY_CODE.getId()).getValue());
		Assert.assertEquals(ExportGermplasmListServiceTest.CROSS_VALUE, row.get(TermId.CROSS.getId()).getValue());
		Assert.assertEquals(ExportGermplasmListServiceTest.ENTRY_NO_VALUE, row.get(TermId.ENTRY_NO.getId()).getValue());
		Assert.assertEquals(ExportGermplasmListServiceTest.GID_VALUE, row.get(TermId.GID.getId()).getValue());

	}

	@Test
	public void testGetExportColumnValuesFromTable_Nursery() {

		List<Map<Integer, ExportColumnValue>> exportColumnValues =
				this.exportGermplasmListService.getExportColumnValuesFromTable(this.getVisibleColumnMap(), false);
		Assert.assertEquals(1, exportColumnValues.size());

		Map<Integer, ExportColumnValue> row = exportColumnValues.get(0);
		Assert.assertEquals(ExportGermplasmListServiceTest.DESIG_VALUE, row.get(TermId.DESIG.getId()).getValue());
		Assert.assertEquals(ExportGermplasmListServiceTest.SOURCE_VALUE, row.get(TermId.SEED_SOURCE.getId()).getValue());
		Assert.assertEquals(ExportGermplasmListServiceTest.ENTRY_CODE_VALUE, row.get(TermId.ENTRY_CODE.getId()).getValue());
		Assert.assertEquals(ExportGermplasmListServiceTest.CROSS_VALUE, row.get(TermId.CROSS.getId()).getValue());
		Assert.assertEquals(ExportGermplasmListServiceTest.ENTRY_NO_VALUE, row.get(TermId.ENTRY_NO.getId()).getValue());
		Assert.assertEquals(ExportGermplasmListServiceTest.GID_VALUE, row.get(TermId.GID.getId()).getValue());

	}

	@Test
	public void test_getCategoricalCodeValue() {
		SettingDetail settingDetail = this.generateSettingDetail(TermId.CHECK.getId());

		List<ValueReference> possibleValues = new ArrayList<>();
		ValueReference valReference = new ValueReference();
		valReference.setId(Integer.valueOf(ExportGermplasmListServiceTest.CHECK_VALUE));
		valReference.setKey(ExportGermplasmListServiceTest.CHECK_VALUE);
		valReference.setName(ExportGermplasmListServiceTest.CATEG_CODE_VALUE);
		possibleValues.add(valReference);

		settingDetail.setPossibleValues(possibleValues);

		String categValue = this.exportGermplasmListService.getCategoricalCodeValue(this.generateImportedGermplasm(), settingDetail);

		Assert.assertEquals(ExportGermplasmListServiceTest.CATEG_CODE_VALUE, categValue);

	}

	@Test
	public void test_getCategoricalCodeValuePossibleValuesIsNull() {
		SettingDetail settingDetail = this.generateSettingDetail(TermId.CHECK.getId());
		settingDetail.setPossibleValues(null);

		String categValue = this.exportGermplasmListService.getCategoricalCodeValue(this.generateImportedGermplasm(), settingDetail);

		Assert.assertEquals(ExportGermplasmListServiceTest.CHECK_VALUE, categValue);
	}

	private List<ImportedGermplasm> generateImportedGermplasms() {

		List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
		importedGermplasms.add(this.generateImportedGermplasm());

		return importedGermplasms;
	}

	private ImportedGermplasm generateImportedGermplasm() {

		ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setCheck(ExportGermplasmListServiceTest.CHECK_VALUE);
		importedGermplasm.setIndex(0);
		importedGermplasm.setGid(ExportGermplasmListServiceTest.GID_VALUE);
		importedGermplasm.setEntryCode(ExportGermplasmListServiceTest.ENTRY_CODE_VALUE);
		importedGermplasm.setEntryId(Integer.valueOf(ExportGermplasmListServiceTest.ENTRY_NO_VALUE));
		importedGermplasm.setSource(ExportGermplasmListServiceTest.SOURCE_VALUE);
		importedGermplasm.setCross(ExportGermplasmListServiceTest.CROSS_VALUE);
		importedGermplasm.setDesig(ExportGermplasmListServiceTest.DESIG_VALUE);

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

		for (Entry<String, Boolean> entry : this.getVisibleColumnMap().entrySet()) {
			plotLevelList.add(this.generateSettingDetail(Integer.valueOf(entry.getKey())));
		}

		return plotLevelList;

	}

	private SettingDetail generateSettingDetail(Integer termId) {
		SettingDetail settingDetail = new SettingDetail();
		settingDetail.setHidden(false);
		SettingVariable var = new SettingVariable();
		var.setCvTermId(termId);
		settingDetail.setVariable(var);

		StandardVariable stdVar;
		try {
			stdVar = this.ontologyService.getStandardVariable(termId);

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

	private StandardVariable createStandardVariable(int id, String name) {
		StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(ExportGermplasmListServiceTest.TEST_DESCRIPTION);

		Term prop = new Term();
		prop.setName(ExportGermplasmListServiceTest.TEST_PROPERTY);
		stdVar.setProperty(prop);

		Term scale = new Term();
		scale.setName(ExportGermplasmListServiceTest.TEST_SCALE);
		stdVar.setScale(scale);

		Term method = new Term();
		method.setName(ExportGermplasmListServiceTest.TEST_METHOD);
		stdVar.setMethod(method);

		Term dataType = new Term();
		dataType.setName(ExportGermplasmListServiceTest.NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);

		return stdVar;
	}

	private GermplasmList getGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName(ExportGermplasmListServiceTest.SAMPLE_LIST);
		germplasmList.setUserId(ExportGermplasmListServiceTest.CURRENT_USER_ID);
		germplasmList.setDescription(ExportGermplasmListServiceTest.SAMPLE_DESCRIPTION);
		germplasmList.setType(ExportGermplasmListServiceTest.LST);
		germplasmList.setDate(ExportGermplasmListServiceTest.LIST_DATE);
		germplasmList.setNotes(ExportGermplasmListServiceTest.SAMPLE_NOTES);

		return germplasmList;
	}

}
