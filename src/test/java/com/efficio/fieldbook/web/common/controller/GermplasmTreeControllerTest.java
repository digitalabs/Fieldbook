
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.Model;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.SaveListForm;
import com.efficio.fieldbook.web.common.service.impl.CrossingServiceImpl;

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

	@InjectMocks
	@Spy
	private GermplasmTreeController controller;

	@Mock
	private ResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private final UserSelection userSelection = new UserSelection();

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	private SaveListForm form;

	@Spy
	private CrossingServiceImpl crossingService;

	@Before
	public void setUp() throws MiddlewareQueryException {

		Mockito.doReturn(this.createCrossSetting()).when(this.userSelection).getCrossSettings();
		Mockito.doReturn(this.createImportedCrossesList()).when(this.userSelection).getImportedCrossesList();
		Mockito.doReturn(this.createWorkBook()).when(this.userSelection).getWorkbook();

		Mockito.doReturn(null).when(this.fieldbookMiddlewareService).getGermplasmIdByName(Matchers.anyString());
		Mockito.doReturn(GermplasmTreeControllerTest.SAVED_GERMPLASM_ID).when(this.fieldbookMiddlewareService)
				.saveGermplasmList(Matchers.anyMap(), Matchers.any(GermplasmList.class));
		Mockito.doReturn(GermplasmTreeControllerTest.SAVED_LISTPROJECT_ID)
				.when(this.fieldbookMiddlewareService)
				.saveOrUpdateListDataProject(Matchers.anyInt(), Matchers.any(GermplasmListType.class), Matchers.anyInt(),
						Matchers.anyList(), Matchers.anyInt());

		Mockito.doReturn(GermplasmTreeControllerTest.PROJECT_ID).when(this.controller).getCurrentProjectId();
		Mockito.doReturn(1).when(this.controller).getCurrentIbdbUserId();

		Mockito.doReturn(1).when(this.crossingService).getIDForUserDefinedFieldCrossingName();

		Mockito.doReturn(new Method()).when(this.germplasmDataManager).getMethodByName(Matchers.anyString());
		Mockito.doReturn(this.createGermplasmIds()).when(this.germplasmDataManager).addGermplasm(Matchers.anyMap());
		Mockito.doReturn(this.createNameTypes()).when(this.germplasmListManager).getGermplasmNameTypes();
		Mockito.doReturn(this.createGermplasmListData()).when(this.germplasmListManager)
				.getGermplasmListDataByListId(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt());

		this.crossingService.setGermplasmDataManager(this.germplasmDataManager);
		this.crossingService.setGermplasmListManager(this.germplasmListManager);

		try {
			Mockito.doReturn(GermplasmTreeControllerTest.LIST_NAME_SHOULD_BE_UNIQUE).when(this.messageSource)
					.getMessage("germplasm.save.list.name.unique.error", null, LocaleContextHolder.getLocale());
		} catch (Exception e) {

		}

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

		Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class), Mockito.mock(HttpSession.class));

		Assert.assertEquals(1, result.get("isSuccess"));
		Assert.assertEquals(1, result.get("germplasmListId"));
		Assert.assertEquals(2, result.get("crossesListId"));
		Assert.assertEquals(this.form.getListIdentifier(), result.get("uniqueId"));
		Assert.assertEquals(this.form.getListName(), result.get("listName"));

	}

	@Test
	public void testSaveCrossesListPostWithExistingGermplasmList() throws MiddlewareQueryException {

		this.form = new SaveListForm();
		this.form.setListName(GermplasmTreeControllerTest.LIST_NAME);
		this.form.setListDate(GermplasmTreeControllerTest.LIST_DATE);
		this.form.setListDescription(GermplasmTreeControllerTest.LIST_DESCRIPTION);
		this.form.setListIdentifier(GermplasmTreeControllerTest.LIST_IDENTIFIER);
		this.form.setListNotes(GermplasmTreeControllerTest.LIST_NOTES);
		this.form.setListType(GermplasmTreeControllerTest.LIST_TYPE);
		this.form.setParentId(GermplasmTreeControllerTest.LIST_PARENT_ID);
		this.form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);

		Mockito.doReturn(this.createGermplasmList()).when(this.fieldbookMiddlewareService).getGermplasmListByName(Matchers.anyString());

		Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class), Mockito.mock(HttpSession.class));

		Assert.assertEquals(0, result.get("isSuccess"));
		Assert.assertEquals(GermplasmTreeControllerTest.LIST_NAME_SHOULD_BE_UNIQUE, result.get("message"));

	}

	@Test
	public void testSaveCrossesListPostWithError() throws MiddlewareQueryException {

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

		Map<String, Object> result = this.controller.savePost(this.form, Mockito.mock(Model.class), Mockito.mock(HttpSession.class));

		Assert.assertEquals(0, result.get("isSuccess"));
		Assert.assertEquals(GermplasmTreeControllerTest.ERROR_MESSAGE, result.get("message"));

	}

	private CrossSetting createCrossSetting() {
		CrossSetting crossSetting = new CrossSetting();

		CrossNameSetting crossNameSetting = new CrossNameSetting();
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

		ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		List<ImportedCrosses> importedCrosses = new ArrayList<>();
		ImportedCrosses cross = new ImportedCrosses();
		cross.setFemaleDesig("FEMALE-12345");
		cross.setFemaleGid("12345");
		cross.setMaleDesig("MALE-54321");
		cross.setMaleGid("54321");
		importedCrosses.add(cross);
		ImportedCrosses cross2 = new ImportedCrosses();
		cross2.setFemaleDesig("FEMALE-9999");
		cross2.setFemaleGid("9999");
		cross2.setMaleDesig("MALE-8888");
		cross2.setMaleGid("8888");
		importedCrosses.add(cross2);
		importedCrossesList.setImportedGermplasms(importedCrosses);

		return importedCrossesList;
	}

	private List<UserDefinedField> createNameTypes() {
		List<UserDefinedField> nameTypes = new ArrayList<>();
		UserDefinedField udf = new UserDefinedField();
		udf.setFcode(CrossingServiceImpl.USER_DEF_FIELD_CROSS_NAME[0]);
		nameTypes.add(udf);
		return nameTypes;
	}

	private List<Integer> createGermplasmIds() {
		List<Integer> ids = new ArrayList<>();
		ids.add(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID1));
		ids.add(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID2));
		return ids;
	}

	private List<GermplasmListData> createGermplasmListData() {
		List<GermplasmListData> listData = new ArrayList<>();

		GermplasmListData data1 = new GermplasmListData();
		data1.setGid(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID1));
		data1.setDesignation("DESIG 1");
		data1.setEntryId(1);
		data1.setGroupName("GROUP 1");
		data1.setSeedSource("SEED 1");
		listData.add(data1);
		GermplasmListData data2 = new GermplasmListData();
		data2.setGid(Integer.valueOf(GermplasmTreeControllerTest.SAVED_CROSSES_GID2));
		data2.setDesignation("DESIG 2");
		data2.setEntryId(2);
		data2.setGroupName("GROUP 2");
		data2.setSeedSource("SEED 2");
		listData.add(data2);

		return listData;

	}

	private Workbook createWorkBook() {
		Workbook wb = new Workbook();

		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(Integer.valueOf(GermplasmTreeControllerTest.PROJECT_ID));
		wb.setStudyDetails(studyDetails);
		return wb;

	}

	private GermplasmList createGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		return germplasmList;
	}
}
