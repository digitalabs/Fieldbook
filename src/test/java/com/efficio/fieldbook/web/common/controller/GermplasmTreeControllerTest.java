
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.service.UserTreeStateService;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.SaveListForm;
import com.efficio.fieldbook.web.common.service.impl.CrossingServiceImpl;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmTreeControllerTest {

	private static final String LIST_NAME_SHOULD_BE_UNIQUE = "List Name should be unique";
	private static final String PROJECT_ID = "1";
	private static final String LIST_PARENT_ID = GermplasmTreeControllerTest.PROJECT_ID;
	private static final String LIST_TYPE = "GERMPLASM LITS";
	private static final String LIST_NOTES = "LIST NOTES";
	private static final String LIST_IDENTIFIER = "LIST IDENTIFIER";
	private static final String LIST_DESCRIPTION = "LIST DESCRIPTION";
	private static final String LIST_DATE = "2015-01-30";
	private static final String SAVED_CROSSES_GID1 = "-9999";
	private static final String SAVED_CROSSES_GID2 = "-8888";
	private static final String LIST_NAME = "LIST 1";
	private static final Integer SAVED_GERMPLASM_ID = 1;
	private static final int SAVED_LISTPROJECT_ID = 2;
	private static final String ERROR_MESSAGE = "middeware exception message";
	private static final Integer TEST_USER_ID = 1;
	private static final String TEST_PROGRAM_UUID = "1234567890";

	@Mock
	private ResourceBundleMessageSource messageSource;

	@Mock
	private HttpServletRequest request;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private final UserSelection userSelection = new UserSelection();

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private ContextUtil contextUtil;

	private SaveListForm form;

	@Mock
	private CrossingServiceImpl crossingService;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private AbstractBaseFieldbookController abstractFieldbookController;

	@Mock
	private WorkbenchRuntimeData workbenchRuntimeData;

	@Mock
	private UserTreeStateService userTreeStateService;

	@InjectMocks
	private GermplasmTreeController controller;

	@Before
	public void setUp() throws MiddlewareQueryException {
		Mockito.doReturn(this.getProject()).when(this.workbenchDataManager).getLastOpenedProjectAnyUser();
		Mockito.doReturn(this.workbenchRuntimeData).when(this.workbenchDataManager).getWorkbenchRuntimeData();
		Mockito.doReturn(this.createCrossSetting()).when(this.userSelection).getCrossSettings();
		Mockito.doReturn(this.createImportedCrossesList()).when(this.userSelection).getImportedCrossesList();
		Mockito.doReturn(this.createWorkBook()).when(this.userSelection).getWorkbook();
		Mockito.doReturn(null).when(this.fieldbookMiddlewareService).getGermplasmIdByName(Matchers.anyString());
		Mockito.doReturn(GermplasmTreeControllerTest.SAVED_GERMPLASM_ID).when(this.fieldbookMiddlewareService)
				.saveGermplasmList(Matchers.anyList(), Matchers.any(GermplasmList.class));
		Mockito.doReturn(GermplasmTreeControllerTest.SAVED_LISTPROJECT_ID)
				.when(this.fieldbookMiddlewareService)
				.saveOrUpdateListDataProject(Matchers.anyInt(), Matchers.any(GermplasmListType.class), Matchers.anyInt(),
						Matchers.anyList(), Matchers.anyInt());

		Mockito.doReturn(1).when(this.crossingService).getIDForUserDefinedFieldCrossingName();

		Mockito.doReturn(new Method()).when(this.germplasmDataManager).getMethodByName(Matchers.anyString());
		Mockito.doReturn(this.createGermplasmIds()).when(this.germplasmDataManager).addGermplasm(Matchers.anyMap());
		Mockito.doReturn(this.createNameTypes()).when(this.germplasmListManager).getGermplasmNameTypes();
		Mockito.doReturn(this.createGermplasmListData()).when(this.germplasmListManager).getGermplasmListDataByListId(Matchers.anyInt());

		try {
			Mockito.doReturn(GermplasmTreeControllerTest.LIST_NAME_SHOULD_BE_UNIQUE).when(this.messageSource)
					.getMessage("germplasm.save.list.name.unique.error", null, LocaleContextHolder.getLocale());
		} catch (final Exception e) {

		}
		Mockito.when(this.germplasmDataManager.getPlotCodeField()).thenReturn(new UserDefinedField(1152));
	}

	private Project getProject() {
		final Project project = new Project();
		project.setProjectId((long) 1);
		return project;
	}

	@Test
	public void testSaveAdvanceListPostSuccessful() {
		final PaginationListSelection paginationListSelection = new PaginationListSelection();
		paginationListSelection.addAdvanceDetails(GermplasmTreeControllerTest.LIST_IDENTIFIER, this.createAdvancingNurseryForm());

		this.form = new SaveListForm();
		this.form.setListName(GermplasmTreeControllerTest.LIST_NAME);
		this.form.setListDate(GermplasmTreeControllerTest.LIST_DATE);
		this.form.setListDescription(GermplasmTreeControllerTest.LIST_DESCRIPTION);
		this.form.setListIdentifier(GermplasmTreeControllerTest.LIST_IDENTIFIER);
		this.form.setListNotes(GermplasmTreeControllerTest.LIST_NOTES);
		this.form.setListType(GermplasmTreeControllerTest.LIST_TYPE);
		this.form.setParentId(GermplasmTreeControllerTest.LIST_PARENT_ID);
		this.form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_ADVANCE);

		this.controller.setPaginationListSelection(paginationListSelection);

		final Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class));

		Assert.assertEquals("isSuccess Value should be 1", 1, result.get("isSuccess"));
		Assert.assertEquals("advancedGermplasmListId should be 2", 2, result.get("advancedGermplasmListId"));
		Assert.assertEquals("Unique ID should be LIST IDENTIFIER", this.form.getListIdentifier(), result.get("uniqueId"));
		Assert.assertEquals("List Name should be LIST 1", this.form.getListName(), result.get("listName"));
	}

	@Test
	public void testSaveCrossesListPostSuccessful() {
		this.form = new SaveListForm();
		this.form.setListName(GermplasmTreeControllerTest.LIST_NAME);
		this.form.setListDate(GermplasmTreeControllerTest.LIST_DATE);
		this.form.setListDescription(GermplasmTreeControllerTest.LIST_DESCRIPTION);
		this.form.setListIdentifier(GermplasmTreeControllerTest.LIST_IDENTIFIER);
		this.form.setListNotes(GermplasmTreeControllerTest.LIST_NOTES);
		this.form.setListType(GermplasmTreeControllerTest.LIST_TYPE);
		this.form.setParentId(GermplasmTreeControllerTest.LIST_PARENT_ID);
		this.form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		final Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class));

		Assert.assertEquals("isSuccess Value should be 1", 1, result.get("isSuccess"));
		Assert.assertEquals("germplasmListId should be 1", 1, result.get("germplasmListId"));
		Assert.assertEquals("crossesListId should be 2", 2, result.get("crossesListId"));
		Assert.assertEquals("Unique ID should be LIST IDENTIFIER", this.form.getListIdentifier(), result.get("uniqueId"));
		Assert.assertEquals("List Name should be LIST 1", this.form.getListName(), result.get("listName"));
	}

	@Test
	public void testSaveListPostWithExistingGermplasmList() throws MiddlewareQueryException {
		this.form = new SaveListForm();
		this.form.setListName(GermplasmTreeControllerTest.LIST_NAME);
		this.form.setListDate(GermplasmTreeControllerTest.LIST_DATE);
		this.form.setListDescription(GermplasmTreeControllerTest.LIST_DESCRIPTION);
		this.form.setListIdentifier(GermplasmTreeControllerTest.LIST_IDENTIFIER);
		this.form.setListNotes(GermplasmTreeControllerTest.LIST_NOTES);
		this.form.setListType(GermplasmTreeControllerTest.LIST_TYPE);
		this.form.setParentId(GermplasmTreeControllerTest.LIST_PARENT_ID);
		this.form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService)
				.getGermplasmListByName(Matchers.anyString(), Matchers.anyString());

		final Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class));

		Assert.assertEquals(0, result.get("isSuccess"));
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_NAME_SHOULD_BE_UNIQUE, result.get("message"));
	}

	@Test
	public void testSaveListPostWithError() throws MiddlewareQueryException {
		this.form = new SaveListForm();
		this.form.setListName(GermplasmTreeControllerTest.LIST_NAME);
		this.form.setListDate(GermplasmTreeControllerTest.LIST_DATE);
		this.form.setListDescription(GermplasmTreeControllerTest.LIST_DESCRIPTION);
		this.form.setListIdentifier(GermplasmTreeControllerTest.LIST_IDENTIFIER);
		this.form.setListNotes(GermplasmTreeControllerTest.LIST_NOTES);
		this.form.setListType(GermplasmTreeControllerTest.LIST_TYPE);
		this.form.setParentId(GermplasmTreeControllerTest.LIST_PARENT_ID);
		this.form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		Mockito.when(this.germplasmDataManager.getMethodByName(Matchers.anyString())).thenThrow(
				new MiddlewareQueryException(GermplasmTreeControllerTest.ERROR_MESSAGE));
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListByName(Matchers.anyString(), Matchers.anyString())).thenThrow(
				new MiddlewareQueryException(GermplasmTreeControllerTest.ERROR_MESSAGE));

		final Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class));

		Assert.assertEquals(0, result.get("isSuccess"));
		Assert.assertEquals(GermplasmTreeControllerTest.ERROR_MESSAGE, result.get("message"));
	}

	@Test
	public void testSaveTreeState() throws MiddlewareQueryException {
		final String[] expandedNodes = {"2", "5", "6"};

		Mockito.doReturn(TEST_USER_ID).when(this.contextUtil).getCurrentUserLocalId();
		Mockito.doReturn(TEST_PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		final String response = this.controller.saveTreeState(ListTreeState.GERMPLASM_LIST.toString(), expandedNodes);
		Assert.assertEquals("Should return ok", "OK", response);
	}

	@Test
	public void testLoadTreeStateNonSaveDialog() throws MiddlewareQueryException {
		Mockito.doReturn(TEST_USER_ID).when(this.contextUtil).getCurrentUserLocalId();
		Mockito.doReturn(TEST_PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		final List<String> response = new ArrayList<String>();
		response.add("1");
		response.add("2");
		Mockito.doReturn(response).when(this.userTreeStateService)
				.getUserProgramTreeStateByUserIdProgramUuidAndType(TEST_USER_ID, TEST_PROGRAM_UUID, ListTreeState.GERMPLASM_LIST.name());

		final String returnData = this.controller.retrieveTreeState(ListTreeState.GERMPLASM_LIST.name(), false);

		Assert.assertEquals("Should return [1, 2]", "[\"1\",\"2\"]", returnData);
	}

	@Test
	public void testLoadTreeStateSaveDialog() throws MiddlewareQueryException {
		Mockito.doReturn(TEST_USER_ID).when(this.contextUtil).getCurrentUserLocalId();
		Mockito.doReturn(TEST_PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		final List<String> response = new ArrayList<String>();
		response.add("1");
		response.add("2");
		Mockito.doReturn(response).when(this.userTreeStateService).getUserProgramTreeStateForSaveList(TEST_USER_ID, TEST_PROGRAM_UUID);

		final String returnData = this.controller.retrieveTreeState(ListTreeState.GERMPLASM_LIST.name(), true);

		Mockito.verify(this.userTreeStateService).getUserProgramTreeStateForSaveList(TEST_USER_ID, TEST_PROGRAM_UUID);

		Assert.assertEquals("Should return [1, 2]", "[\"1\",\"2\"]", returnData);
	}

	@Test
	public void testAddGermplasmFolder() {
		final HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		final String parentID = "1";
		final String folderName = "NewFolder";
		final int listId = 10;
		Mockito.doReturn(parentID).when(req).getParameter("parentFolderId");
		Mockito.doReturn(folderName).when(req).getParameter("folderName");
		Mockito.doReturn(listId).when(this.germplasmListManager).addGermplasmList(Mockito.any(GermplasmList.class));

		final Map<String, Object> resultsMap = this.controller.addGermplasmFolder(req);
		Assert.assertTrue("Expecting that Germplasm Folder is added successfully.", resultsMap.get(GermplasmTreeController.IS_SUCCESS)
				.equals("1"));
		Assert.assertTrue("Expecting that Germplasm Folder is added has id " + listId, resultsMap.get("id").equals(listId));
	}

	private CrossSetting createCrossSetting() {
		final CrossSetting crossSetting = new CrossSetting();

		final CrossNameSetting crossNameSetting = new CrossNameSetting();
		crossNameSetting.setPrefix("PREFIX");
		crossNameSetting.setSuffix("SUFFIX");
		crossNameSetting.setAddSpaceBetweenPrefixAndCode(true);
		crossNameSetting.setAddSpaceBetweenSuffixAndCode(true);
		crossNameSetting.setSeparator("|");
		crossNameSetting.setStartNumber(100);
		crossNameSetting.setNumOfDigits(7);

		crossSetting.setCrossNameSetting(crossNameSetting);
		crossSetting.setBreedingMethodSetting(new BreedingMethodSetting());
		crossSetting.setAdditionalDetailsSetting(new AdditionalDetailsSetting());

		return crossSetting;
	}

	private ImportedCrossesList createImportedCrossesList() {
		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final List<ImportedCrosses> importedCrosses = new ArrayList<>();
		final ImportedCrosses cross = new ImportedCrosses();
		cross.setFemaleDesig("FEMALE-12345");
		cross.setFemaleGid("12345");
		cross.setMaleDesig("MALE-54321");
		cross.setMaleGid("54321");
		cross.setGid("10021");
		importedCrosses.add(cross);
		final ImportedCrosses cross2 = new ImportedCrosses();
		cross2.setFemaleDesig("FEMALE-9999");
		cross2.setFemaleGid("9999");
		cross2.setMaleDesig("MALE-8888");
		cross2.setMaleGid("8888");
		cross2.setGid("10022");
		importedCrosses.add(cross2);
		importedCrossesList.setImportedGermplasms(importedCrosses);

		return importedCrossesList;
	}

	private List<UserDefinedField> createNameTypes() {
		final List<UserDefinedField> nameTypes = new ArrayList<>();
		final UserDefinedField udf = new UserDefinedField();
		udf.setFcode(CrossingServiceImpl.USER_DEF_FIELD_CROSS_NAME[0]);
		nameTypes.add(udf);
		return nameTypes;
	}

	private List<Integer> createGermplasmIds() {
		final List<Integer> ids = new ArrayList<>();
		ids.add(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID1));
		ids.add(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID2));
		return ids;
	}

	private List<GermplasmListData> createGermplasmListData() {
		final List<GermplasmListData> listData = new ArrayList<>();

		final GermplasmListData data1 = new GermplasmListData();
		data1.setGid(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID1));
		data1.setDesignation("DESIG 1");
		data1.setEntryId(1);
		data1.setGroupName("GROUP 1");
		data1.setSeedSource("SEED 1");
		listData.add(data1);
		final GermplasmListData data2 = new GermplasmListData();
		data2.setGid(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID2));
		data2.setDesignation("DESIG 2");
		data2.setEntryId(2);
		data2.setGroupName("GROUP 2");
		data2.setSeedSource("SEED 2");
		listData.add(data2);

		return listData;
	}

	private Workbook createWorkBook() {
		final Workbook wb = new Workbook();

		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(Integer.valueOf(GermplasmTreeControllerTest.PROJECT_ID));
		wb.setStudyDetails(studyDetails);
		return wb;
	}

	private GermplasmList createGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		return germplasmList;
	}

	private AdvancingNurseryForm createAdvancingNurseryForm() {
		final AdvancingNurseryForm advancingNurseryForm = new AdvancingNurseryForm();
		final ImportedGermplasm importedGermplasm = Mockito.mock(ImportedGermplasm.class);
		final Name name = Mockito.mock(Name.class);
		final List<Name> names = Arrays.asList(name);
		final List<ImportedGermplasm> importedGermplasmList = Arrays.asList(importedGermplasm);
		Mockito.doReturn(names).when(importedGermplasm).getNames();
		advancingNurseryForm.setHarvestYear("2015");
		advancingNurseryForm.setHarvestMonth("08");
		advancingNurseryForm.setGermplasmList(importedGermplasmList);
		return advancingNurseryForm;
	}
}
