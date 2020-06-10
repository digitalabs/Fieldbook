
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
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

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;

public class ExportStudyGermplasmServiceTest {

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

	private String testFileName;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmExportService germplasmExportService;

	@Mock
	private Workbook workbook;

	@Mock
	private StudyDetails studyDetails;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private ImportedGermplasmMainInfo importedGermplasmMainInfo;

	@Mock
	private ImportedGermplasmList importedGermplasmList;

	@Mock
	ListDataProject listDataProject;

	@Mock
	GermplasmList germplasmList;

	@Mock
	InventoryDataManager inventoryDataManager;

	@InjectMocks
	private ExportStudyGermplasmServiceImpl exportStudyGermplasmListServiceImpl;

	@Before
	public void setUp() throws MiddlewareException {
		final List<ImportedGermplasm> importedGermplasms = this.setUpImportedGermplasm();

		MockitoAnnotations.initMocks(this);

		this.testFileName = "test.csv";

		Mockito.doReturn(ExportStudyGermplasmServiceTest.CURRENT_USER_ID).when(this.contextUtil).getCurrentWorkbenchUserId();

		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_NO.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.ENTRY_NO.getId(), ExportStudyGermplasmServiceTest.ENTRY_NO));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.DESIG.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.DESIG.getId(), ExportStudyGermplasmServiceTest.DESIGNATION));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.GID.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.GID.getId(), ExportStudyGermplasmServiceTest.GID));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.CROSS.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.CROSS.getId(), ExportStudyGermplasmServiceTest.PARENTAGE));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.SEED_SOURCE.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.SEED_SOURCE.getId(), ExportStudyGermplasmServiceTest.SEED_SOURCE));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_CODE.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.ENTRY_CODE.getId(), ExportStudyGermplasmServiceTest.ENTRY_CODE));
		Mockito.when(
				this.ontologyService.getStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(),
						ExportStudyGermplasmServiceTest.ENTRY_NUMBER_STORAGE));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.CHECK.getId(), ExportStudyGermplasmServiceTest.CHECK));
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(this.workbook);
		Mockito.when(this.workbook.getStudyDetails()).thenReturn(this.studyDetails);
		Mockito.when(this.userSelection.getImportedGermplasmMainInfo()).thenReturn(this.importedGermplasmMainInfo);
		Mockito.when(this.importedGermplasmMainInfo.getImportedGermplasmList()).thenReturn(this.importedGermplasmList);
		Mockito.when(this.importedGermplasmList.getImportedGermplasms()).thenReturn(importedGermplasms);
		Mockito.doReturn(this.getPlotLevelList()).when(this.userSelection).getPlotsLevelList();
		Mockito.doReturn(this.getGermplasmList()).when(this.fieldbookMiddlewareService)
				.getGermplasmListById(ExportStudyGermplasmServiceTest.LIST_ID);
		Mockito.doReturn(this.createListDataProject()).when(this.germplasmListManager)
				.retrieveSnapshotListData(ExportStudyGermplasmServiceTest.LIST_ID);
		Mockito.doReturn(ExportStudyGermplasmServiceTest.CURRENT_USER_NAME).when(this.fieldbookMiddlewareService)
				.getOwnerListName(ExportStudyGermplasmServiceTest.CURRENT_USER_ID);
		Mockito.doReturn("1010").when(this.fieldbookMiddlewareService).getOwnerListName(Matchers.anyInt());

		Mockito.when(ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), TermId.STOCKID.getId(), false)).thenReturn(this.createVariable(TermId.STOCKID.getId()));
		Mockito.when(ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), TermId.SEED_AMOUNT_G.getId(), false)).thenReturn(this.createVariable(TermId.SEED_AMOUNT_G.getId()));
	}

	@Test
	public void testExportGermplasmListCSVForStudyManager() {

		try {
			this.exportStudyGermplasmListServiceImpl.exportAsCSVFile(1, this.testFileName, this.getVisibleColumnMap());
			Mockito.verify(this.germplasmExportService, Mockito.times(1)).generateCSVFile(Matchers.any(List.class), Matchers.any(List.class),
					Matchers.anyString());

		} catch (final GermplasmListExporterException e) {
			Assert.fail();
		} catch (final IOException e) {
			Assert.fail();
		}

	}

	@Test
	public void testExportGermplasmListCSVForNurseryManager() {

		try {
			this.exportStudyGermplasmListServiceImpl.exportAsCSVFile(1, this.testFileName, this.getVisibleColumnMap());
			Mockito.verify(this.germplasmExportService, Mockito.times(1)).generateCSVFile(Matchers.any(List.class), Matchers.any(List.class),
					Matchers.anyString());
		} catch (final GermplasmListExporterException e) {
			Assert.fail();
		} catch (final IOException e) {
			Assert.fail();
		}

	}

	@After
	public void tearDown() {
		final File file = new File(this.testFileName);
		file.deleteOnExit();
	}

	@Test
	public void testGetExportColumnHeadersFromTableStudy() {

		final List<ExportColumnHeader> exportColumnHeaders =
				this.exportStudyGermplasmListServiceImpl.getExportColumnHeadersFromTable(this.getVisibleColumnMap());

		Assert.assertEquals(6, exportColumnHeaders.size());
		Assert.assertTrue(exportColumnHeaders.get(0).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(1).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(2).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(3).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(4).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(5).isDisplay());

		Assert.assertEquals(ExportStudyGermplasmServiceTest.GID, exportColumnHeaders.get(0).getName());
		Assert.assertEquals(ExportStudyGermplasmServiceTest.PARENTAGE, exportColumnHeaders.get(1).getName());
		Assert.assertEquals(ExportStudyGermplasmServiceTest.ENTRY_NO, exportColumnHeaders.get(2).getName());
		Assert.assertEquals(ExportStudyGermplasmServiceTest.DESIGNATION, exportColumnHeaders.get(3).getName());
		Assert.assertEquals(ExportStudyGermplasmServiceTest.SEED_SOURCE, exportColumnHeaders.get(4).getName());
		Assert.assertEquals(ExportStudyGermplasmServiceTest.ENTRY_CODE, exportColumnHeaders.get(5).getName());

	}

	@Test
	public void testGetExportColumnHeadersFromTableNursery() {

		final List<ExportColumnHeader> exportColumnHeaders =
				this.exportStudyGermplasmListServiceImpl.getExportColumnHeadersFromTable(this.getVisibleColumnMap());

		Assert.assertEquals(6, exportColumnHeaders.size());
		Assert.assertTrue(exportColumnHeaders.get(0).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(1).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(2).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(3).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(4).isDisplay());
		Assert.assertTrue(exportColumnHeaders.get(5).isDisplay());

		Assert.assertEquals(ExportStudyGermplasmServiceTest.GID, exportColumnHeaders.get(0).getName());
		Assert.assertEquals(ExportStudyGermplasmServiceTest.PARENTAGE, exportColumnHeaders.get(1).getName());
		Assert.assertEquals(ExportStudyGermplasmServiceTest.ENTRY_NO, exportColumnHeaders.get(2).getName());
		Assert.assertEquals(ExportStudyGermplasmServiceTest.DESIGNATION, exportColumnHeaders.get(3).getName());
		Assert.assertEquals(ExportStudyGermplasmServiceTest.SEED_SOURCE, exportColumnHeaders.get(4).getName());
		Assert.assertEquals(ExportStudyGermplasmServiceTest.ENTRY_CODE, exportColumnHeaders.get(5).getName());

	}

	@Test
	public void testGetExportColumnValuesFromTableStudy() {

		final List<ExportRow> exportColumnValues =
				this.exportStudyGermplasmListServiceImpl.getExportColumnValuesFromTable(1, this.getVisibleColumnMap());

		Assert.assertEquals(1, exportColumnValues.size());

		final ExportRow row = exportColumnValues.get(0);

		Assert.assertEquals(ExportStudyGermplasmServiceTest.DESIG_VALUE, row.getValueForColumn(TermId.DESIG.getId()));
		Assert.assertEquals(ExportStudyGermplasmServiceTest.SOURCE_VALUE, row.getValueForColumn(TermId.SEED_SOURCE.getId()));
		Assert.assertEquals(ExportStudyGermplasmServiceTest.ENTRY_CODE_VALUE, row.getValueForColumn(TermId.ENTRY_CODE.getId()));
		Assert.assertEquals(ExportStudyGermplasmServiceTest.CROSS_VALUE, row.getValueForColumn(TermId.CROSS.getId()));
		Assert.assertEquals(ExportStudyGermplasmServiceTest.ENTRY_NO_VALUE, row.getValueForColumn(TermId.ENTRY_NO.getId()));
		Assert.assertEquals(ExportStudyGermplasmServiceTest.GID_VALUE, row.getValueForColumn(TermId.GID.getId()));

	}

	@Test
	public void testGetExportColumnValuesFromTableNursery() {

		final List<ExportRow> exportRows =
				this.exportStudyGermplasmListServiceImpl.getExportColumnValuesFromTable(1, this.getVisibleColumnMap());

		Assert.assertEquals(1, exportRows.size());

		final ExportRow row = exportRows.get(0);

		Assert.assertEquals(ExportStudyGermplasmServiceTest.DESIG_VALUE, row.getValueForColumn(TermId.DESIG.getId()));
		Assert.assertEquals(ExportStudyGermplasmServiceTest.SOURCE_VALUE, row.getValueForColumn(TermId.SEED_SOURCE.getId()));
		Assert.assertEquals(ExportStudyGermplasmServiceTest.ENTRY_CODE_VALUE, row.getValueForColumn(TermId.ENTRY_CODE.getId()));
		Assert.assertEquals(ExportStudyGermplasmServiceTest.CROSS_VALUE, row.getValueForColumn(TermId.CROSS.getId()));
		Assert.assertEquals(ExportStudyGermplasmServiceTest.ENTRY_NO_VALUE, row.getValueForColumn(TermId.ENTRY_NO.getId()));
		Assert.assertEquals(ExportStudyGermplasmServiceTest.GID_VALUE, row.getValueForColumn(TermId.GID.getId()));

	}

	@Test
	public void testExtractInventoryVariableMapFromVisibleColumnsInventoryVariablesAreVisible() {

		final Map<String, Boolean> visibleColumnsMap = this.getVisibleColumnMap();
		visibleColumnsMap.put(String.valueOf(TermId.STOCKID.getId()), true);
		visibleColumnsMap.put(String.valueOf(TermId.SEED_AMOUNT_G.getId()), true);

		final Map<Integer,Variable> result = exportStudyGermplasmListServiceImpl.extractInventoryVariableMapFromVisibleColumns(visibleColumnsMap);

		Assert.assertEquals("There are 2 inventory variables in visibleColumnsMap so the size of InventoryVariableMap should be 2.", 2, result.size());
		Assert.assertTrue(result.containsKey(TermId.STOCKID.getId()));
		Assert.assertTrue(result.containsKey(TermId.SEED_AMOUNT_G.getId()));

	}

	@Test
	public void testExtractInventoryVariableMapFromVisibleColumnsInventoryVariablesAreNotVisible() {

		final Map<String, Boolean> visibleColumnsMap = this.getVisibleColumnMap();
		visibleColumnsMap.put(String.valueOf(TermId.STOCKID.getId()), false);
		visibleColumnsMap.put(String.valueOf(TermId.SEED_AMOUNT_G.getId()), false);

		final Map<Integer,Variable> result = exportStudyGermplasmListServiceImpl.extractInventoryVariableMapFromVisibleColumns(visibleColumnsMap);

		Assert.assertTrue("There are 2 inventory variables in visibleColumnsMap but they are not visible so the size of InventoryVariableMap should be empty", result.isEmpty());
		Assert.assertFalse(result.containsKey(TermId.STOCKID.getId()));
		Assert.assertFalse(result.containsKey(TermId.SEED_AMOUNT_G.getId()));

	}

	@Test
	public void testRemoveInventoryVariableMapFromVisibleColumns() {

		final Map<String, Boolean> visibleColumnsMap = this.getVisibleColumnMap();

		// Get the size of visibleColumnsMap before adding the inventory variables so that
		// we can compare the size of visibleColumnsMap after removing the inventory variables.
		final int visibleColumnsMapVariableCount = visibleColumnsMap.size();

		// Add inventory variables in visibleColumnsmap
		visibleColumnsMap.put(String.valueOf(TermId.STOCKID.getId()), true);
		visibleColumnsMap.put(String.valueOf(TermId.SEED_AMOUNT_G.getId()), true);

		exportStudyGermplasmListServiceImpl.removeInventoryVariableMapFromVisibleColumns(visibleColumnsMap);

		Assert.assertEquals("Expecting " + visibleColumnsMapVariableCount + " variables in visibleColumnsMap", visibleColumnsMapVariableCount, visibleColumnsMap.size());
		Assert.assertFalse(visibleColumnsMap.containsKey(TermId.STOCKID.getId()));
		Assert.assertFalse(visibleColumnsMap.containsKey(TermId.SEED_AMOUNT_G.getId()));

	}


	private Variable createVariable(final int termid) {

		final Variable variable = new Variable();
		variable.setId(termid);
		return variable;

	}

	private ImportedGermplasm generateImportedGermplasm() {

		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setEntryTypeValue(ExportStudyGermplasmServiceTest.CHECK_VALUE);
		importedGermplasm.setIndex(0);
		importedGermplasm.setGid(ExportStudyGermplasmServiceTest.GID_VALUE);
		importedGermplasm.setEntryCode(ExportStudyGermplasmServiceTest.ENTRY_CODE_VALUE);
		importedGermplasm.setEntryId(Integer.valueOf(ExportStudyGermplasmServiceTest.ENTRY_NO_VALUE));
		importedGermplasm.setSource(ExportStudyGermplasmServiceTest.SOURCE_VALUE);
		importedGermplasm.setCross(ExportStudyGermplasmServiceTest.CROSS_VALUE);
		importedGermplasm.setDesig(ExportStudyGermplasmServiceTest.DESIG_VALUE);

		return importedGermplasm;

	}

	private Map<String, Boolean> getVisibleColumnMap() {
		final Map<String, Boolean> visibleColumnMap = new LinkedHashMap<String, Boolean>();

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
		final List<SettingDetail> plotLevelList = new ArrayList<>();

		for (final Entry<String, Boolean> entry : this.getVisibleColumnMap().entrySet()) {
			plotLevelList.add(this.generateSettingDetail(Integer.valueOf(entry.getKey())));
		}

		return plotLevelList;

	}

	private SettingDetail generateSettingDetail(final Integer termId) {
		final SettingDetail settingDetail = new SettingDetail();
		settingDetail.setHidden(false);
		final SettingVariable var = new SettingVariable();
		var.setCvTermId(termId);
		settingDetail.setVariable(var);

		final StandardVariable stdVar;
		try {
			stdVar = this.ontologyService.getStandardVariable(termId, this.contextUtil.getCurrentProgramUUID());

			settingDetail.getVariable().setName(stdVar.getName());
			settingDetail.getVariable().setDescription(stdVar.getDescription());
			settingDetail.getVariable().setProperty(stdVar.getProperty().getName());
			settingDetail.getVariable().setScale(stdVar.getScale().getName());
			settingDetail.getVariable().setMethod(stdVar.getMethod().getName());
			settingDetail.getVariable().setDataType(stdVar.getDataType().getName());
			settingDetail.setPossibleValues(new ArrayList<ValueReference>());

		} catch (final MiddlewareException e) {
			// do nothing
		}

		return settingDetail;
	}

	private StandardVariable createStandardVariable(final int id, final String name) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(ExportStudyGermplasmServiceTest.TEST_DESCRIPTION);

		final Term prop = new Term();
		prop.setName(ExportStudyGermplasmServiceTest.TEST_PROPERTY);
		stdVar.setProperty(prop);

		final Term scale = new Term();
		scale.setName(ExportStudyGermplasmServiceTest.TEST_SCALE);
		stdVar.setScale(scale);

		final Term method = new Term();
		method.setName(ExportStudyGermplasmServiceTest.TEST_METHOD);
		stdVar.setMethod(method);

		final Term dataType = new Term();
		dataType.setName(ExportStudyGermplasmServiceTest.NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);

		return stdVar;
	}

	private GermplasmList getGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName(ExportStudyGermplasmServiceTest.SAMPLE_LIST);
		germplasmList.setUserId(ExportStudyGermplasmServiceTest.CURRENT_USER_ID);
		germplasmList.setDescription(ExportStudyGermplasmServiceTest.SAMPLE_DESCRIPTION);
		germplasmList.setType(ExportStudyGermplasmServiceTest.LST);
		germplasmList.setDate(ExportStudyGermplasmServiceTest.LIST_DATE);
		germplasmList.setNotes(ExportStudyGermplasmServiceTest.SAMPLE_NOTES);

		return germplasmList;
	}

	private List<ListDataProject> createListDataProject() {
		final List<ListDataProject> listData = new ArrayList<>();
		final ListDataProject data = new ListDataProject();
		data.setGermplasmId(Integer.valueOf(ExportStudyGermplasmServiceTest.GID_VALUE));
		data.setEntryCode(ExportStudyGermplasmServiceTest.ENTRY_CODE_VALUE);
		data.setEntryId(Integer.valueOf(ExportStudyGermplasmServiceTest.ENTRY_NO_VALUE));
		data.setSeedSource(ExportStudyGermplasmServiceTest.SOURCE_VALUE);
		data.setDesignation(ExportStudyGermplasmServiceTest.DESIG_VALUE);
		listData.add(data);
		return listData;
	}

	private List<ImportedGermplasm> setUpImportedGermplasm() {
		final List<ImportedGermplasm> importedGermplasms = new ArrayList<>();
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();

		importedGermplasm.setGid(ExportStudyGermplasmServiceTest.GID_VALUE);
		importedGermplasm.setEntryCode(ExportStudyGermplasmServiceTest.ENTRY_CODE_VALUE);
		importedGermplasm.setEntryId(1);
		importedGermplasm.setSource(ExportStudyGermplasmServiceTest.SOURCE_VALUE);
		importedGermplasm.setCross(ExportStudyGermplasmServiceTest.CROSS_VALUE);
		importedGermplasm.setDesig(ExportStudyGermplasmServiceTest.DESIG_VALUE);
		importedGermplasms.add(importedGermplasm);

		return importedGermplasms;
	}
}