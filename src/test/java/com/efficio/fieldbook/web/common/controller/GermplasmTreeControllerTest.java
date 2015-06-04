package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.SaveListForm;
import com.efficio.fieldbook.web.common.service.impl.CrossingServiceImpl;

import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
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
import org.generationcp.middleware.manager.api.UserProgramStateDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmTreeControllerTest {
	
	private static final String LIST_NAME_SHOULD_BE_UNIQUE = "List Name should be unique";
	private static final String PROJECT_ID = "1";
	private static final String LIST_PARENT_ID = PROJECT_ID;
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
	private UserSelection userSelection = new UserSelection();
	
	@Mock
	private FieldbookService fieldbookMiddlewareService;

	private SaveListForm form;
	
	@Spy
	private CrossingServiceImpl crossingService;

	@Before
	public void setUp() throws MiddlewareQueryException {
		
		doReturn(createCrossSetting()).when(userSelection).getCrossSettings();
		doReturn(createImportedCrossesList()).when(userSelection).getImportedCrossesList();
		doReturn(createWorkBook()).when(userSelection).getWorkbook();
		
		doReturn(null).when(fieldbookMiddlewareService).getGermplasmIdByName(anyString());
		doReturn(SAVED_GERMPLASM_ID).when(fieldbookMiddlewareService).saveGermplasmList(anyMap(),
				any(GermplasmList.class));
		doReturn(SAVED_LISTPROJECT_ID).when(fieldbookMiddlewareService).saveOrUpdateListDataProject(anyInt(), any(GermplasmListType.class), anyInt(), anyList(), anyInt());
		
		doReturn(PROJECT_ID).when(controller).getCurrentProjectId();
		doReturn(1).when(controller).getCurrentIbdbUserId();
		
		doReturn(1).when(crossingService).getIDForUserDefinedFieldCrossingName();
		
		doReturn(new Method()).when(germplasmDataManager).getMethodByName(anyString());
		doReturn(createGermplasmIds()).when(germplasmDataManager).addGermplasm(anyMap());
		doReturn(createNameTypes()).when(germplasmListManager).getGermplasmNameTypes();
		doReturn(createGermplasmListData()).when(germplasmListManager).getGermplasmListDataByListId(anyInt(), anyInt(), anyInt());
		
		crossingService.setGermplasmDataManager(germplasmDataManager);
		crossingService.setGermplasmListManager(germplasmListManager);
		
		try{
			doReturn(LIST_NAME_SHOULD_BE_UNIQUE).when(messageSource).getMessage("germplasm.save.list.name.unique.error", null, LocaleContextHolder.getLocale());
		}catch(Exception e){
			
		}
		
	}
	
	@Test
	public void testSaveCrossesListPostSuccessful(){
		
		form = new SaveListForm();
		form.setListName(LIST_NAME);
		form.setListDate(LIST_DATE);
		form.setListDescription(LIST_DESCRIPTION);
		form.setListIdentifier(LIST_IDENTIFIER);
		form.setListNotes(LIST_NOTES);
		form.setListType(LIST_TYPE);
		form.setParentId(LIST_PARENT_ID);
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);
		
		Map<String, Object> result = controller.savePost(form, mock(Model.class),
				mock(HttpSession.class));
		
		assertEquals(1, result.get("isSuccess"));
		assertEquals(1, result.get("germplasmListId"));
		assertEquals(2, result.get("crossesListId"));
		assertEquals(form.getListIdentifier(), result.get("uniqueId"));
		assertEquals(form.getListName(), result.get("listName"));
		
	}
	
	@Test
	public void testSaveCrossesListPostWithExistingGermplasmList() throws MiddlewareQueryException{
		
		form = new SaveListForm();
		form.setListName(LIST_NAME);
		form.setListDate(LIST_DATE);
		form.setListDescription(LIST_DESCRIPTION);
		form.setListIdentifier(LIST_IDENTIFIER);
		form.setListNotes(LIST_NOTES);
		form.setListType(LIST_TYPE);
		form.setParentId(LIST_PARENT_ID);
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);
		
		doReturn(createGermplasmList()).when(fieldbookMiddlewareService).getGermplasmListByName(
				anyString());
		
		Map<String, Object> result = controller.savePost(form, mock(Model.class),
				mock(HttpSession.class));
		
		assertEquals(0, result.get("isSuccess"));
		assertEquals(LIST_NAME_SHOULD_BE_UNIQUE, result.get("message"));
		
	}
	
	@Test
	public void testSaveCrossesListPostWithError() throws MiddlewareQueryException{
		
		form = new SaveListForm();
		form.setListName(LIST_NAME);
		form.setListDate(LIST_DATE);
		form.setListDescription(LIST_DESCRIPTION);
		form.setListIdentifier(LIST_IDENTIFIER);
		form.setListNotes(LIST_NOTES);
		form.setListType(LIST_TYPE);
		form.setParentId(LIST_PARENT_ID);
		form.setGermplasmListType(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS);
		
		when(germplasmDataManager.getMethodByName(anyString())).thenThrow(
				new MiddlewareQueryException(ERROR_MESSAGE));
		
		Map<String, Object> result = controller.savePost(form, mock(Model.class), mock(HttpSession.class));
		
		assertEquals(0, result.get("isSuccess"));
		assertEquals(ERROR_MESSAGE, result.get("message"));
		
	}
	
	@Test
	public void testSaveTreeState() throws MiddlewareQueryException{
		String[] expandedNodes = {"2","5","6"};
		GermplasmTreeController germplasmTreeController = new GermplasmTreeController();
		ContextUtil contextUtil = Mockito.spy(new ContextUtil());
		Mockito.doReturn(new Integer(1)).when(contextUtil).getCurrentWorkbenchUserId();
		Project project = new Project();
		project.setUniqueID("1234567890");
		Mockito.doReturn(project).when(contextUtil).getProjectInContext();
		UserProgramStateDataManager manager = Mockito.mock(UserProgramStateDataManager.class);
		germplasmTreeController.setUserProgramStateDataManager(manager);
		germplasmTreeController.setContextUtil(contextUtil);
		String response = germplasmTreeController.saveTreeState(ListTreeState.GERMPLASM_LIST.toString(), expandedNodes);
		Assert.assertEquals("Should return ok", "OK", response);
	}
	
	@Test
	public void testLoadTreeState() throws MiddlewareQueryException{
		
		GermplasmTreeController germplasmTreeController = new GermplasmTreeController();
		ContextUtil contextUtil = Mockito.spy(new ContextUtil());
		Mockito.doReturn(new Integer(1)).when(contextUtil).getCurrentWorkbenchUserId();
		Project project = new Project();
		project.setUniqueID("1234567890");
		Mockito.doReturn(project).when(contextUtil).getProjectInContext();
		UserProgramStateDataManager manager = Mockito.mock(UserProgramStateDataManager.class);
		List<String> response = new ArrayList<String>();
		response.add("1");
		response.add("2");
		Mockito.doReturn(response).when(manager).getUserProgramTreeStateByUserIdProgramUuidAndType(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());				
		
		germplasmTreeController.setUserProgramStateDataManager(manager);
		germplasmTreeController.setContextUtil(contextUtil);
		String returnData = germplasmTreeController.retrieveTreeState(ListTreeState.GERMPLASM_LIST.toString());
		
		Assert.assertEquals("Should return [1, 2]", "[\"1\",\"2\"]", returnData);
	}
	
	private CrossSetting createCrossSetting(){
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
	
	private ImportedCrossesList createImportedCrossesList(){
		
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
	
	private List<UserDefinedField> createNameTypes(){
		List<UserDefinedField> nameTypes = new ArrayList<>();
		UserDefinedField udf = new UserDefinedField();
		udf.setFcode(CrossingServiceImpl.USER_DEF_FIELD_CROSS_NAME[0]);
		nameTypes.add(udf);
		return nameTypes;
	}
	
	private List<Integer> createGermplasmIds(){
		List<Integer> ids = new ArrayList<>();
		ids.add(Integer.valueOf(SAVED_CROSSES_GID1));
		ids.add(Integer.valueOf(SAVED_CROSSES_GID2));
		return ids;
	}
	
	private List<GermplasmListData> createGermplasmListData(){
		List<GermplasmListData> listData = new ArrayList<>();
		
		GermplasmListData data1 = new GermplasmListData();
		data1.setGid(Integer.valueOf(SAVED_CROSSES_GID1));
		data1.setDesignation("DESIG 1");
		data1.setEntryId(1);
		data1.setGroupName("GROUP 1");
		data1.setSeedSource("SEED 1");
		listData.add(data1);
		GermplasmListData data2 = new GermplasmListData();
		data2.setGid(Integer.valueOf(SAVED_CROSSES_GID2));
		data2.setDesignation("DESIG 2");
		data2.setEntryId(2);
		data2.setGroupName("GROUP 2");
		data2.setSeedSource("SEED 2");
		listData.add(data2);
		
		return listData;
		
	}
	
	private Workbook createWorkBook(){
		Workbook wb = new Workbook();
		
		StudyDetails studyDetails = new StudyDetails();
		studyDetails.setId(Integer.valueOf(PROJECT_ID));
		wb.setStudyDetails(studyDetails);
		return wb;
		
	}
	
	private GermplasmList createGermplasmList(){
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		return germplasmList;
	}
}
