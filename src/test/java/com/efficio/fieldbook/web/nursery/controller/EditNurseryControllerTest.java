
package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class EditNurseryControllerTest {

	public static final int ROOT_FOLDER_ID = 1;
	public static final int PUBLIC_NURSERY_ID = 1;
	public static final int LOCAL_NURSERY_ID = -1;
	private static final int DEFAULT_TERM_ID = 1234;
	private static final int NOT_EXIST_TERM_ID = 2345;
	private static final int DEFAULT_TERM_ID_2 = 3456;
	private static final int NURSERY_ID = 1;
	public static final int CHILD_FOLDER_ID = 2;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpSession session;

	@Mock
	private CreateNurseryForm createNurseryForm;

	@Mock
	private ImportGermplasmListForm importGermplasmListForm;

	@Mock
	private Model model;

	@Mock
	private RedirectAttributes redirectAttributes;

	@Mock
	private UserSelection userSelection;

	@Mock
	org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private ErrorHandlerService errorHandlerService;

	@Mock
	private SettingVariable settingVar;

	@Mock
	private StudyDataManager studyDataManagerImpl;

	@Mock
	private WorkbenchDataManager workBenchDataManager;

	@Mock
	private StandardVariable standardVariable;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private AbstractBaseFieldbookController abstractBaseFieldbookController;

	@Mock
	private WorkbenchRuntimeData workbenchRD;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private SettingsController settingsController;

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private OntologyService ontologyService;

	@InjectMocks
	private EditNurseryController editNurseryController;

	@Test
	public void testUseExistingNurseryNoRedirect() throws Exception {
		final DmsProject dmsProject = Mockito.mock(DmsProject.class);
		final StudyDetails studyDetails = Mockito.mock(StudyDetails.class);
		final Workbook workbook = Mockito.mock(Workbook.class);
		final Project project = Mockito.mock(Project.class);

		Mockito.doReturn(dmsProject).when(this.studyDataManagerImpl).getProject(Matchers.anyInt());
		Mockito.when(dmsProject.getProgramUUID()).thenReturn("1002");
		Mockito.when(studyDetails.getParentFolderId()).thenReturn((long) 1);
		Mockito.when(workbook.getStudyDetails()).thenReturn(studyDetails);
		Mockito.doReturn(project).when(this.abstractBaseFieldbookController).getCurrentProject();
		Mockito.when(this.fieldbookMiddlewareService.getNurseryDataSet(Matchers.anyInt())).thenReturn(workbook);
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(Matchers.anyInt(), Matchers.anyString()))
						.thenReturn(this.standardVariable);
		Mockito.when(this.workBenchDataManager.getLastOpenedProjectAnyUser()).thenReturn(project);
		Mockito.when(this.workBenchDataManager.getWorkbenchRuntimeData()).thenReturn(this.workbenchRD);
		Mockito.when(this.fieldbookProperties.getProgramBreedingMethodsUrl()).thenReturn(Matchers.anyString());

		// test
		final String out = this.editNurseryController.useExistingNursery(this.createNurseryForm, this.importGermplasmListForm,
				EditNurseryControllerTest.NURSERY_ID, "context-info", this.model, this.request, this.redirectAttributes);

		Mockito.verify(this.fieldbookMiddlewareService).getNurseryDataSet(Matchers.anyInt());
		Assert.assertEquals("Should return the URL of the base_template", AbstractBaseFieldbookController.BASE_TEMPLATE_NAME, out);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUseExistingNurseryRedirectForIncompatibleStudy() throws Exception {
		final DmsProject dmsProject = Mockito.mock(DmsProject.class);

		// setup: we don't care actually what's happening inside controller.useExistingNursery, we just want it to return the URL
		Mockito.doReturn(dmsProject).when(this.studyDataManagerImpl).getProject(Matchers.anyInt());
		Mockito.when(dmsProject.getProgramUUID()).thenReturn("1002");
		Mockito.when(this.request.getCookies()).thenReturn(new Cookie[] {});
		Mockito.when(this.fieldbookMiddlewareService.getNurseryDataSet(EditNurseryControllerTest.NURSERY_ID))
				.thenThrow(MiddlewareQueryException.class);

		final String out = this.editNurseryController.useExistingNursery(this.createNurseryForm, this.importGermplasmListForm,
				EditNurseryControllerTest.NURSERY_ID, "context-info", this.model, this.request, this.redirectAttributes);
		Assert.assertEquals("should redirect to manage nurseries page", "redirect:" + ManageNurseriesController.URL, out);

		// assert that we should have produced a redirectErrorMessage
		final ArgumentCaptor<String> redirectArg1 = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> redirectArg2 = ArgumentCaptor.forClass(String.class);

		Mockito.verify(this.redirectAttributes).addFlashAttribute(redirectArg1.capture(), redirectArg2.capture());
		Assert.assertEquals("Value should be redirectErrorMessage", "redirectErrorMessage", redirectArg1.getValue());
	}

	@Test
	public void testCheckMeasurementData() throws Exception {
		this.initializeMeasurementRowList();

		final Map<String, String> result1 = this.editNurseryController.checkMeasurementData(this.createNurseryForm, this.model, 0,
				Integer.toString(EditNurseryControllerTest.DEFAULT_TERM_ID));

		Assert.assertTrue("the result of map with key HAS_MEASUREMENT_DATA_STR should be '1' ",
				result1.get(EditNurseryController.HAS_MEASUREMENT_DATA_STR).equals(EditNurseryController.SUCCESS));

		final Map<String, String> result2 = this.editNurseryController.checkMeasurementData(this.createNurseryForm, this.model, 0,
				Integer.toString(EditNurseryControllerTest.DEFAULT_TERM_ID_2));

		Assert.assertTrue("the result of map with key HAS_MEASUREMENT_DATA_STR should be '0' ",
				result2.get(EditNurseryController.HAS_MEASUREMENT_DATA_STR).equals(EditNurseryController.NO_MEASUREMENT));
	}

	@Test
	public void testHasMeasurementDataEntered() throws Exception {
		this.initializeMeasurementRowList();

		Assert.assertTrue("We should have a measurementVariable in the datalist with DEFAULT_TERM_ID",
				this.editNurseryController.hasMeasurementDataEntered(EditNurseryControllerTest.DEFAULT_TERM_ID));
		Assert.assertFalse("NOT_EXIST_TERM_ID should not exist in the measurement data list",
				this.editNurseryController.hasMeasurementDataEntered(EditNurseryControllerTest.NOT_EXIST_TERM_ID));
		Assert.assertFalse("DEFAULT_TERM_ID_2 has measurement data without value (null)",
				this.editNurseryController.hasMeasurementDataEntered(EditNurseryControllerTest.DEFAULT_TERM_ID_2));
	}

	@Test
	public void testSanitizeSettingDetailList() {
		final List<SettingDetail> settingDetails = new ArrayList<>();

		settingDetails.add(new SettingDetail());
		final SettingDetail settingDetail =
				new SettingDetail(Mockito.mock(SettingVariable.class), new ArrayList<ValueReference>(), "", false);
		settingDetails.add(settingDetail);

		this.editNurseryController.sanitizeSettingDetailList(settingDetails);

		Assert.assertEquals("Sanitize function does not properly remove setting variable objects that have empty state", 1,
				settingDetails.size());
		Assert.assertEquals("Expected remaining setting detail not found in sanitized list", settingDetail, settingDetails.get(0));
	}

	@Test
	public void testSettingOfCheckVariablesInEditNursery() {
		final CreateNurseryForm form = new CreateNurseryForm();
		final ImportGermplasmListForm form2 = new ImportGermplasmListForm();
		final List<SettingDetail> removedConditions = WorkbookTestUtil.createCheckVariables();
		this.editNurseryController.setCheckVariables(removedConditions, form2, form);

		Assert.assertNotNull(form2.getCheckVariables());
		Assert.assertTrue("Expected check variables but the list does not have all check variables.",
				WorkbookTestUtil.areDetailsFilteredVariables(form2.getCheckVariables(), AppConstants.CHECK_VARIABLES.getString()));
	}

	@Test
	public void testGetNurseryFolderName() throws Exception {
		// case folder id = root folder
		final String out = this.editNurseryController.getNurseryFolderName(EditNurseryControllerTest.ROOT_FOLDER_ID);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.never()).getFolderNameById(EditNurseryControllerTest.ROOT_FOLDER_ID);
		Assert.assertEquals("should be Nurseries", AppConstants.NURSERIES.getString(), out);

		// case not a root folder
		Mockito.when(this.fieldbookMiddlewareService.getFolderNameById(Matchers.anyInt())).thenReturn(Matchers.anyString());
		this.editNurseryController.getNurseryFolderName(EditNurseryControllerTest.CHILD_FOLDER_ID);
		Mockito.verify(this.fieldbookMiddlewareService).getFolderNameById(EditNurseryControllerTest.CHILD_FOLDER_ID);
	}

	@Test
	public void testAddNurseryTypeFromDesignImportWhenNurseryTypeValueIsNull() {
		final List<SettingDetail> studyLevelVariables = new ArrayList<SettingDetail>();
		Mockito.doReturn(null).when(this.userSelection).getNurseryTypeForDesign();
		this.editNurseryController.addNurseryTypeFromDesignImport(studyLevelVariables);

		Assert.assertTrue("studyLevelVariables should not be null", studyLevelVariables.isEmpty());
	}

	@Test
	public void testAddNurseryTypeFromDesignImportWhenNurseryTypeValueHasValue() {
		final List<SettingDetail> studyLevelVariables = new ArrayList<SettingDetail>();
		this.editNurseryController.addNurseryTypeFromDesignImport(studyLevelVariables);

		Assert.assertNotNull("studyLevelVariables should not be null", studyLevelVariables);
		final SettingDetail settingDetail = studyLevelVariables.get(0);

		Assert.assertEquals("Value should be zero but " + settingDetail.getValue(), "0", settingDetail.getValue());
		Assert.assertNotNull("settingDetail Variable should not be null ", settingDetail.getVariable());
	}

	@Test
	public void testAddNurseryFromDesignImportWhenDesignImportHasValue() {
		final List<SettingDetail> studyLevelVariables = Arrays.asList(this.initializeSettingDetails(true));
		final List<Integer> expDesignVariables = new ArrayList<Integer>();
		expDesignVariables.add(1);

		Mockito.when(this.userSelection.getExpDesignVariables()).thenReturn(expDesignVariables);

		this.editNurseryController.addNurseryTypeFromDesignImport(studyLevelVariables);

		Assert.assertEquals("studyLevelVariables' size should be 1", studyLevelVariables.size(), 1);
		final SettingDetail settingDetail = studyLevelVariables.get(0);

		Assert.assertNull("SettingDetail value should be null but " + settingDetail.getValue(), settingDetail.getValue());
		Assert.assertNotNull("settingDetail Variable should not be null ", settingDetail.getVariable());
	}

	@Test
	public void testAddExperimentalDesignTypeFromDesignImportTrue() {
		final List<SettingDetail> studyLevelVariables = new ArrayList<SettingDetail>();
		final List<Integer> expDesignVariables = new ArrayList<Integer>();
		expDesignVariables.add(1);
		Mockito.doReturn(expDesignVariables).when(this.userSelection).getExpDesignVariables();
		this.editNurseryController.addExperimentalDesignTypeFromDesignImport(studyLevelVariables);

		Assert.assertFalse("studyLevelVariables should not be empty", studyLevelVariables.isEmpty());
		final SettingDetail settingDetail = studyLevelVariables.get(0);

		Assert.assertEquals("Value should be " + TermId.OTHER_DESIGN.getId() + " but " + settingDetail.getValue(),
				String.valueOf(TermId.OTHER_DESIGN.getId()), settingDetail.getValue());
		Assert.assertNotNull("settingDetail Variable should not be null ", settingDetail.getVariable());
	}

	@Test
	public void testAddExperimentalDesignTypeFromDesignImportFalse() {
		final List<SettingDetail> studyLevelVariables = new ArrayList<SettingDetail>();
		this.editNurseryController.addExperimentalDesignTypeFromDesignImport(studyLevelVariables);

		Assert.assertTrue("studyLevelVariables should be empty", studyLevelVariables.isEmpty());
	}

	@Test
	public void testAddExperimentalDesignTypeFromDesignImportUpdate() {
		final List<SettingDetail> studyLevelVariables = Arrays.asList(this.initializeSettingDetails(false));
		final List<Integer> expDesignVariables = new ArrayList<Integer>();
		expDesignVariables.add(1);

		Mockito.when(this.userSelection.getExpDesignVariables()).thenReturn(expDesignVariables);

		this.editNurseryController.addExperimentalDesignTypeFromDesignImport(studyLevelVariables);

		Assert.assertEquals("studyLevelVariables' size should be 1", studyLevelVariables.size(), 1);
		final SettingDetail settingDetail = studyLevelVariables.get(0);

		Assert.assertNull("SettingDetail value should be null but " + settingDetail.getValue(), settingDetail.getValue());
		Assert.assertNotNull("settingDetail Variable should not be null ", settingDetail.getVariable());
	}

	@Test
	public void testDeleteMesurementRowsSuccess() {
		final Workbook workbook = Mockito.mock(Workbook.class);

		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);

		final Map<String, String> out = this.editNurseryController.deleteMeasurementRows();

		// test
		Assert.assertEquals("The status should be 1", "1", out.get("status"));
	}

	@Test
	public void testDeleteMesurementRowsHasError() {
		final Workbook workbook = Mockito.mock(Workbook.class);

		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		Mockito.doThrow(MiddlewareQueryException.class).when(this.fieldbookMiddlewareService).deleteObservationsOfStudy(Matchers.anyInt());
		
		final Map<String, String> out = this.editNurseryController.deleteMeasurementRows();

		// test
		Assert.assertEquals("The status should be -1", "-1", out.get("status"));
	}

	@Test
	public void testgetNameTypesSuccess() {
		final UserDefinedField nameType = Mockito.mock(UserDefinedField.class);
		final List<UserDefinedField> nameTypesReturn = Arrays.asList(nameType);

		Mockito.when(this.fieldbookMiddlewareService.getGermplasmNameTypes()).thenReturn(nameTypesReturn);

		final List<UserDefinedField> nameTypes = this.editNurseryController.getNameTypes();

		// test
		Assert.assertNotNull("The nametypes should not be null.", nameTypes);
	}

	@Test
	public void testSubmitWhereMeasurementsListHasNoValue() {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final SettingDetail settingDetail = Mockito.mock(SettingDetail.class);
		final SettingVariable variable = Mockito.mock(SettingVariable.class);
		final StandardVariable standardVariable = Mockito.mock(StandardVariable.class);
		final Term term = Mockito.mock(Term.class);

		Mockito.when(settingDetail.getVariable()).thenReturn(variable);
		Mockito.when(this.userSelection.getCacheStandardVariable(Matchers.anyInt())).thenReturn(standardVariable);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		Mockito.when(settingDetail.getRole()).thenReturn(PhenotypicType.STUDY);
		Mockito.when(standardVariable.getProperty()).thenReturn(term);
		Mockito.when(standardVariable.getScale()).thenReturn(term);
		Mockito.when(standardVariable.getMethod()).thenReturn(term);
		Mockito.when(standardVariable.getPhenotypicType()).thenReturn(PhenotypicType.STUDY);
		Mockito.when(standardVariable.getDataType()).thenReturn(term);
		final Map<String, String> out = this.editNurseryController.submit(this.createNurseryForm, this.model);

		Assert.assertEquals("The status should be 1", "1", out.get("status"));
	}

	@Test
	public void testSubmitWhereMeasurementsListHasValueSuccess() {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final SettingDetail settingDetail = Mockito.mock(SettingDetail.class);
		final SettingVariable variable = Mockito.mock(SettingVariable.class);
		final StandardVariable standardVariable = Mockito.mock(StandardVariable.class);
		final Term term = Mockito.mock(Term.class);
		final MeasurementRow measurementRow = Mockito.mock(MeasurementRow.class);

		final List<MeasurementRow> measurementsRows = Arrays.asList(measurementRow);

		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(measurementsRows);
		Mockito.when(settingDetail.getVariable()).thenReturn(variable);
		Mockito.when(this.userSelection.getCacheStandardVariable(Matchers.anyInt())).thenReturn(standardVariable);
		Mockito.when(this.userSelection.getWorkbook()).thenReturn(workbook);
		Mockito.when(settingDetail.getRole()).thenReturn(PhenotypicType.STUDY);
		Mockito.when(standardVariable.getProperty()).thenReturn(term);
		Mockito.when(standardVariable.getScale()).thenReturn(term);
		Mockito.when(standardVariable.getMethod()).thenReturn(term);
		Mockito.when(standardVariable.getPhenotypicType()).thenReturn(PhenotypicType.STUDY);
		Mockito.when(standardVariable.getDataType()).thenReturn(term);
		final Map<String, String> out = this.editNurseryController.submit(this.createNurseryForm, this.model);

		Assert.assertEquals("The status should be 1", "1", out.get("status"));
	}

	private SettingDetail initializeSettingDetails(final boolean isAddNursery) {
		final SettingDetail settingDetail = Mockito.mock(SettingDetail.class);

		final SettingVariable settingVariable = new SettingVariable();
		if (isAddNursery) {
			settingVariable.setCvTermId(TermId.NURSERY_TYPE.getId());
		} else {
			settingVariable.setCvTermId(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
		}

		Mockito.when(settingDetail.getVariable()).thenReturn(settingVariable);

		return settingDetail;
	}

	private void initializeMeasurementRowList() {
		final Random random = new Random(1000);
		// random numbers generated up-to 3 digits only so as not to conflict with test data
		final List<MeasurementData> measurementDataList =
				Arrays.asList(this.generateMockedMeasurementData(random.nextInt(100), Integer.toString(random.nextInt(100))),
						this.generateMockedMeasurementData(random.nextInt(100), Integer.toString(random.nextInt(100))),
						this.generateMockedMeasurementData(EditNurseryControllerTest.DEFAULT_TERM_ID,
								Integer.toString(EditNurseryControllerTest.DEFAULT_TERM_ID)),
				this.generateMockedMeasurementData(EditNurseryControllerTest.DEFAULT_TERM_ID_2, null));

		final MeasurementRow measurmentRow = Mockito.mock(MeasurementRow.class);
		final List<MeasurementRow> measurementRowList = Arrays.asList(measurmentRow);
		Mockito.when(measurmentRow.getDataList()).thenReturn(measurementDataList);
		Mockito.when(this.userSelection.getMeasurementRowList()).thenReturn(measurementRowList);
	}

	private MeasurementData generateMockedMeasurementData(final int termID, final String value) {
		final MeasurementData measurementData = Mockito.mock(MeasurementData.class);

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termID);

		Mockito.when(measurementData.getMeasurementVariable()).thenReturn(measurementVariable);
		Mockito.when(measurementData.getValue()).thenReturn(value);

		return measurementData;
	}
}
