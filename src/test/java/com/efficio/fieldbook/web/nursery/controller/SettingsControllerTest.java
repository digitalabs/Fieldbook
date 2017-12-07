
package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.utils.test.UnitTestDaoIDGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.ExtendedModelMap;

import com.efficio.fieldbook.service.FieldbookServiceImpl;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class SettingsControllerTest {
	
	private static final String TRAIT_DESCRIPTION = "Ears Selected";

	private static final String TRAIT_NAME = "nEarsSel";

	private Variable testVariable;
	
	private ValueReference testValueReference;
	
	/**
	 * Class under test (SettingsController) is an abstract class so using a dummy impl to invoke methods for testing.
	 */
	class SettingsControllerUnitTestEnabler extends SettingsController {

		@Override
		public String getContentName() {
			return null;
		}
	}

	private final SettingsControllerUnitTestEnabler controller = new SettingsControllerUnitTestEnabler();

	@Mock
	private org.generationcp.commons.spring.util.ContextUtil contextUtil;

	@Mock
	private OntologyVariableDataManager variableDataManager;

	@Mock
	private FieldbookServiceImpl fieldbookService;

	private String programUUID = UUID.randomUUID().toString();

	@Before
	public void setUp() {
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(this.programUUID);
		controller.setContextUtil(this.contextUtil);
		controller.setVariableDataManager(this.variableDataManager);
		controller.setFieldbookService(this.fieldbookService);
		
		this.createTestVariable();
		Mockito.when(this.variableDataManager.getVariable(Mockito.any(String.class), Mockito.any(Integer.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(this.testVariable);
		Mockito.when(this.fieldbookService.getAllPossibleValues(Mockito.anyInt())).thenReturn(Arrays.asList(this.testValueReference));
		Mockito.when(this.fieldbookService.getAllPossibleValuesFavorite(Mockito.anyInt(), Mockito.any(String.class), Mockito.anyBoolean())).thenReturn(Arrays.asList(this.testValueReference));
	}

	@Test
	public void testGetCheckVariables() {
		List<SettingDetail> nurseryLevelConditions = this.createSettingDetailVariables();
		CreateNurseryForm form = new CreateNurseryForm();

		List<SettingDetail> checkVariables = this.controller.getCheckVariables(nurseryLevelConditions, form);

		Assert.assertTrue("Expected only check variables but the list has non check variables as well.",
				WorkbookTestUtil.areDetailsFilteredVariables(checkVariables, AppConstants.CHECK_VARIABLES.getString()));
	}

	@Test
	public void testGetBasicDetails() {
		List<SettingDetail> nurseryLevelConditions = this.createSettingDetailVariables();
		CreateNurseryForm form = new CreateNurseryForm();

		List<SettingDetail> basicDetails =
				this.controller.getSettingDetailsOfSection(nurseryLevelConditions, form, AppConstants.FIXED_NURSERY_VARIABLES.getString());

		Assert.assertTrue("Expected only basic detail variables but the list has non basic detail variables as well.",
				WorkbookTestUtil.areDetailsFilteredVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString()));
	}

	@Test
	public void testHasMeasurementDataEnteredGivenAListOfMeasurementRowsWithData() {
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);

		List<MeasurementRow> measurementRowList = workbook.getObservations();

		Assert.assertTrue(SettingsController.hasMeasurementDataEntered(WorkbookDataUtil.CHALK_PCT_ID, measurementRowList));
	}

	@Test
	public void testHasMeasurementDataEnteredGivenAListOfMeasurementRowsWithoutData() {
		List<MeasurementRow> measurementRowList = new ArrayList<>();
		List<MeasurementData> dataList = new ArrayList<>();
		MeasurementRow measurementRow = new MeasurementRow();
		dataList.add(this.getSampleMeasurementData(1, "Sample Data"));
		measurementRow.setDataList(dataList);
		measurementRowList.add(measurementRow);

		Assert.assertFalse("Expecting the measurement row list has no measurement data.",
				SettingsController.hasMeasurementDataEntered(2, measurementRowList));
	}

	@Test
	public void testHasMeasurementDataEnteredForVariablesWithAtLeast1WithData() {
		EditNurseryController editNurseryController = new EditNurseryController();
		List<Integer> variableIds = new ArrayList<>();
		variableIds.add(1);
		variableIds.add(2);
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<>();
		List<MeasurementData> dataList = new ArrayList<>();
		MeasurementRow measurementRow = new MeasurementRow();
		dataList.add(this.getSampleMeasurementData(1, "Sample Data"));
		dataList.add(this.getSampleMeasurementData(2, ""));
		measurementRow.setDataList(dataList);
		measurementRowList.add(measurementRow);
		userSelection.setMeasurementRowList(measurementRowList);
		boolean hasMeasurementData = editNurseryController.hasMeasurementDataEnteredForVariables(variableIds, userSelection);
		Assert.assertTrue("Should return true since there is measuredData", hasMeasurementData);
	}

	@Test
	public void testHasMeasurementDataEnteredForVariablesWithNoData() {
		EditNurseryController editNurseryController = new EditNurseryController();
		List<Integer> variableIds = new ArrayList<>();
		variableIds.add(1);
		variableIds.add(2);
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList<>();
		List<MeasurementData> dataList = new ArrayList<>();
		MeasurementRow measurementRow = new MeasurementRow();
		dataList.add(this.getSampleMeasurementData(1, ""));
		dataList.add(this.getSampleMeasurementData(2, ""));
		measurementRow.setDataList(dataList);
		measurementRowList.add(measurementRow);
		userSelection.setMeasurementRowList(measurementRowList);
		boolean hasMeasurementData = editNurseryController.hasMeasurementDataEnteredForVariables(variableIds, userSelection);
		Assert.assertFalse("Should return false since there is measuredData", hasMeasurementData);
	}

	private MeasurementData getSampleMeasurementData(Integer variableTermId, String data) {
		MeasurementData measurementData = new MeasurementData();
		measurementData.setLabel("LABEL_" + variableTermId);
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermId);
		measurementData.setValue(data);
		measurementData.setMeasurementVariable(measurementVariable);
		return measurementData;
	}

	private List<SettingDetail> createSettingDetailVariables() {
		List<SettingDetail> variables = new ArrayList<>();
		variables.add(this.createSettingDetail(TermId.STUDY_NAME.getId(), ""));
		variables.add(this.createSettingDetail(TermId.START_DATE.getId(), ""));
		variables.add(this.createSettingDetail(TermId.STUDY_OBJECTIVE.getId(), ""));
		variables.add(this.createSettingDetail(TermId.END_DATE.getId(), ""));
		variables.add(this.createSettingDetail(TermId.STUDY_UID.getId(), ""));
		variables.add(this.createSettingDetail(TermId.STUDY_UPDATE.getId(), ""));
		variables.add(this.createSettingDetail(TermId.TRIAL_INSTANCE_FACTOR.getId(), ""));
		variables.add(this.createSettingDetail(TermId.PI_NAME.getId(), ""));
		variables.add(this.createSettingDetail(TermId.PI_ID.getId(), ""));
		variables.add(this.createSettingDetail(TermId.CHECK_INTERVAL.getId(), ""));
		variables.add(this.createSettingDetail(TermId.CHECK_PLAN.getId(), ""));
		variables.add(this.createSettingDetail(TermId.CHECK_START.getId(), ""));
		return variables;
	}

	private SettingDetail createSettingDetail(Integer cvTermId, String value) {
		SettingVariable variable = new SettingVariable();
		variable.setCvTermId(cvTermId);
		SettingDetail settingDetail = new SettingDetail(variable, null, value, false);
		return settingDetail;
	}

	@Test
	public void testGetMethod_ById() throws MiddlewareQueryException {
		CreateNurseryController createNurseryController = new CreateNurseryController();
		FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		int id = 70;
		String name = "Accession into genebank";
		String code = "AGB1";
		String programUUID = null;
		Method method = this.createMethod(id, name, code, programUUID);
		Mockito.doReturn(method).when(fieldbookMiddlewareService).getMethodById(id);

		String idTermId = Integer.toString(id);
		Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		studyConditionMap.put(idTermId, this.createMeasurementVariable(idTermId));

		Method resultingMethod = createNurseryController.getMethod(studyConditionMap, idTermId, code, programUUID);
		Assert.assertEquals(method.getMid(), resultingMethod.getMid());
	}

	@Test
	public void testGetMethod_ById_EmptyValue() throws MiddlewareQueryException {
		CreateNurseryController createNurseryController = new CreateNurseryController();
		int id = 70;
		String code = "AGB1";
		String programUUID = null;

		String idTermId = Integer.toString(id);
		Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		studyConditionMap.put(idTermId, this.createMeasurementVariable(""));

		Method resultingMethod = createNurseryController.getMethod(studyConditionMap, idTermId, code, programUUID);
		Assert.assertEquals(null, resultingMethod);
	}

	@Test
	public void testGetMethod_ByCode() throws MiddlewareQueryException {
		CreateNurseryController createNurseryController = new CreateNurseryController();
		FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		int id = 70;
		String name = "Accession into genebank";
		String code = "AGB1";
		String programUUID = null;
		Method method = this.createMethod(id, name, code, programUUID);
		Mockito.doReturn(method).when(fieldbookMiddlewareService).getMethodByCode(code, programUUID);

		String idTermId = Integer.toString(id);
		Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		studyConditionMap.put(code, this.createMeasurementVariable(code));

		Method resultingMethod = createNurseryController.getMethod(studyConditionMap, idTermId, code, programUUID);
		Assert.assertEquals(method.getMid(), resultingMethod.getMid());
	}

	@Test
	public void testGetMethod_ByCode_EmptyValue() throws MiddlewareQueryException {
		CreateNurseryController createNurseryController = new CreateNurseryController();

		int id = 70;
		String code = "AGB1";
		String programUUID = null;

		String idTermId = Integer.toString(id);
		Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		studyConditionMap.put(code, this.createMeasurementVariable(""));

		Method resultingMethod = createNurseryController.getMethod(studyConditionMap, idTermId, code, programUUID);
		Assert.assertEquals(null, resultingMethod);
	}

	@Test
	public void testGetMethod_IdAndCodeNotFound() throws MiddlewareQueryException {
		CreateNurseryController createNurseryController = new CreateNurseryController();
		int id = 70;
		String code = "AGB1";
		String programUUID = null;
		String idTermId = Integer.toString(id);
		Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();

		Method resultingMethod = createNurseryController.getMethod(studyConditionMap, idTermId, code, programUUID);
		Assert.assertEquals(null, resultingMethod);
	}

	private Method createMethod(int id, String name, String code, String uniqueID) {
		Method method = new Method();
		method.setMid(id);
		method.setMname(name);
		method.setMcode(code);
		method.setUniqueID(uniqueID);
		return method;
	}

	private MeasurementVariable createMeasurementVariable(String value) {
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("TEST");
		measurementVariable.setValue(value);
		return measurementVariable;
	}

	@Test
	public void testAddVariableSecionIdModelAttributes() {
		ExtendedModelMap model = new ExtendedModelMap();
		this.controller.addVariableSectionIdentifiers(model);
		SettingsControllerTest.checkVariableSecionIdModelAttributes(model);
	}

	public static void checkVariableSecionIdModelAttributes(ExtendedModelMap model) {
		Assert.assertEquals(VariableType.TRAIT.getId(), model.get("baselineTraitsSegment"));
		Assert.assertEquals(VariableType.SELECTION_METHOD.getId(), model.get("selectionVariatesSegment"));
		Assert.assertEquals(VariableType.STUDY_DETAIL.getId(), model.get("studyLevelDetailType"));
		Assert.assertEquals(VariableType.GERMPLASM_DESCRIPTOR.getId(), model.get("plotLevelDetailType"));
		Assert.assertEquals(VariableType.NURSERY_CONDITION.getId(), model.get("nurseryConditionsType"));
	}

	@Test
	public void testCreateSettingDetailWithVariableType() {
		ContextHolder.setCurrentCrop("maize");
		this.createTestProject();

		final String alias = "nEarsSel_Local";
		SettingDetail settingDetail = this.controller.createSettingDetailWithVariableType(this.testVariable.getId(), alias, VariableType.SELECTION_METHOD);
		Assert.assertEquals("Error in Role for settingDetail", VariableType.SELECTION_METHOD.getRole().name(), settingDetail.getRole().name());
		Assert.assertEquals("Error in Variable Type", VariableType.SELECTION_METHOD, settingDetail.getVariableType());
		Assert.assertEquals("Expecting variable alias to be used but was not.", alias, settingDetail.getVariable().getName());
		Assert.assertEquals("Expecting variable description to be used but was not.", TRAIT_DESCRIPTION, settingDetail.getVariable().getDescription());
		Assert.assertNull("Error in Value", settingDetail.getValue());
		
		Assert.assertTrue("Error in Name of PossibleValuesToJson", settingDetail.getPossibleValuesJson().contains(this.testValueReference.getName()));
		Assert.assertTrue("Error in Description of PossibleValuesToJson", settingDetail.getPossibleValuesJson().contains(this.testValueReference.getDescription()));
		Assert.assertTrue("Error in Key of PossibleValuesToJson", settingDetail.getPossibleValuesJson().contains(this.testValueReference.getKey()));
		Assert.assertTrue("Error in Name of PossibleValuesFavoriteToJson", settingDetail.getPossibleValuesFavoriteJson().contains(this.testValueReference.getName()));
		Assert.assertTrue("Error in Description of PossibleValuesFavoriteToJson", settingDetail.getPossibleValuesFavoriteJson().contains(this.testValueReference.getDescription()));
		Assert.assertTrue("Error in Key of PossibleValuesFavoriteToJson", settingDetail.getPossibleValuesFavoriteJson().contains(this.testValueReference.getKey()));

		Mockito.verify(this.variableDataManager, Mockito.times(1)).getVariable(this.contextUtil.getCurrentProgramUUID(), this.testVariable.getId(), false, false);
		Mockito.verify(this.fieldbookService, Mockito.times(1)).getAllPossibleValues(this.testVariable.getId());
		Mockito.verify(this.contextUtil, Mockito.times(1)).getProjectInContext();
		Mockito.verify(this.fieldbookService, Mockito.times(1)).getAllPossibleValuesFavorite(this.testVariable.getId(), this.controller.getCurrentProject().getUniqueID(), false);
	}
	
	@Test
	public void testCreateSettingDetailWithVariableTypeWhenAliasIsNull() {
		ContextHolder.setCurrentCrop("maize");
		this.createTestProject();

		SettingDetail settingDetail = this.controller.createSettingDetailWithVariableType(this.testVariable.getId(), null, VariableType.SELECTION_METHOD);
		Assert.assertEquals("Error in Role for settingDetail", VariableType.SELECTION_METHOD.getRole().name(), settingDetail.getRole().name());
		Assert.assertEquals("Error in Variable Type", VariableType.SELECTION_METHOD, settingDetail.getVariableType());
		Assert.assertEquals("Expecting variable's standard name to be used since alias is null but was not.", TRAIT_NAME, settingDetail.getVariable().getName());
		Assert.assertEquals("Expecting variable description to be used but was not.", TRAIT_DESCRIPTION, settingDetail.getVariable().getDescription());
		Assert.assertNull("Error in Value", settingDetail.getValue());
	}
	
	@Test
	public void testCreateSettingDetailWithVariableTypeWhenAliasIsEmpty() {
		ContextHolder.setCurrentCrop("maize");
		this.createTestProject();

		SettingDetail settingDetail = this.controller.createSettingDetailWithVariableType(this.testVariable.getId(), "", VariableType.SELECTION_METHOD);
		Assert.assertEquals("Error in Role for settingDetail", VariableType.SELECTION_METHOD.getRole().name(), settingDetail.getRole().name());
		Assert.assertEquals("Error in Variable Type", VariableType.SELECTION_METHOD, settingDetail.getVariableType());
		Assert.assertEquals("Expecting variable's standard name to be used since alias is empty but was not.", TRAIT_NAME, settingDetail.getVariable().getName());
		Assert.assertEquals("Expecting variable description to be used but was not.", TRAIT_DESCRIPTION, settingDetail.getVariable().getDescription());
		Assert.assertNull("Error in Value", settingDetail.getValue());
	}

	private void createTestProject() {
		Project project = new Project();
		project.setUniqueID(programUUID);

		Mockito.when(this.controller.getCurrentProject()).thenReturn(project);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
	}

	private void createTestVariable(){
		org.generationcp.middleware.domain.ontology.Method method = new org.generationcp.middleware.domain.ontology.Method();
		method.setId(UnitTestDaoIDGenerator.generateId(Method.class));
		method.setName("Method Name");

		Property property = new Property();
		property.setName("Property Name");
		property.setCropOntologyId("CO:501");
		property.addClass("Class1");
		property.addClass("Class2");

		Scale scale = new Scale();
		scale.setId(UnitTestDaoIDGenerator.generateId(Scale.class));
		scale.setName("Scale Name");
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		scale.setMinValue("5");
		scale.setMaxValue("500");

		this.testVariable = new Variable();
		this.testVariable.setId(UnitTestDaoIDGenerator.generateId(Variable.class));
		this.testVariable.setMinValue("10");
		this.testVariable.setMaxValue("100");
		this.testVariable.setName(TRAIT_NAME);
		this.testVariable.setDefinition(TRAIT_DESCRIPTION);
		this.testVariable.setObsolete(false);
		this.testVariable.setObservations(-1);
		this.testVariable.setStudies(-1);
		this.testVariable.setIsFavorite(false);
		this.testVariable.setMethod(method);
		this.testVariable.setProperty(property);
		this.testVariable.setScale(scale);
		
		this.testValueReference = new ValueReference();
		this.testValueReference.setKey("1");
		this.testValueReference.setName("Value Reference Name");
		this.testValueReference.setDescription("Value Reference Description");
	}

}
