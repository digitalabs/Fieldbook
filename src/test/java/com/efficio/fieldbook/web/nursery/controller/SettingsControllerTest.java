
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
import org.mockito.Matchers;
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

	private final String programUUID = UUID.randomUUID().toString();

	@Before
	public void setUp() {
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(this.programUUID);
		this.controller.setContextUtil(this.contextUtil);
		this.controller.setVariableDataManager(this.variableDataManager);
		this.controller.setFieldbookService(this.fieldbookService);

		this.createTestVariable();
		Mockito.when(this.variableDataManager.getVariable(Matchers.any(String.class), Matchers.any(Integer.class), Matchers.anyBoolean(),
				Matchers.anyBoolean())).thenReturn(this.testVariable);
		Mockito.when(this.fieldbookService.getAllPossibleValues(Matchers.anyInt())).thenReturn(Arrays.asList(this.testValueReference));
		Mockito.when(
				this.fieldbookService.getAllPossibleValuesFavorite(Matchers.anyInt(), Matchers.any(String.class), Matchers.anyBoolean()))
				.thenReturn(Arrays.asList(this.testValueReference));
	}

	@Test
	public void testGetCheckVariables() {
		final List<SettingDetail> nurseryLevelConditions = this.createSettingDetailVariables();
		final CreateNurseryForm form = new CreateNurseryForm();

		final List<SettingDetail> checkVariables = this.controller.getCheckVariables(nurseryLevelConditions, form);

		Assert.assertTrue("Expected only check variables but the list has non check variables as well.",
				WorkbookTestUtil.areDetailsFilteredVariables(checkVariables, AppConstants.CHECK_VARIABLES.getString()));
	}

	@Test
	public void testGetBasicDetails() {
		final List<SettingDetail> nurseryLevelConditions = this.createSettingDetailVariables();
		final CreateNurseryForm form = new CreateNurseryForm();

		final List<SettingDetail> basicDetails =
				this.controller.getSettingDetailsOfSection(nurseryLevelConditions, form, AppConstants.FIXED_NURSERY_VARIABLES.getString());

		Assert.assertTrue("Expected only basic detail variables but the list has non basic detail variables as well.",
				WorkbookTestUtil.areDetailsFilteredVariables(basicDetails, AppConstants.FIXED_NURSERY_VARIABLES.getString()));
	}

	@Test
	public void testHasMeasurementDataEnteredGivenAListOfMeasurementRowsWithData() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(5, StudyType.N);

		final List<MeasurementRow> measurementRowList = workbook.getObservations();

		Assert.assertTrue(SettingsController.hasMeasurementDataEntered(WorkbookDataUtil.CHALK_PCT_ID, measurementRowList));
	}

	@Test
	public void testHasMeasurementDataEnteredGivenAListOfMeasurementRowsWithoutData() {
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		final List<MeasurementData> dataList = new ArrayList<>();
		final MeasurementRow measurementRow = new MeasurementRow();
		dataList.add(this.getSampleMeasurementData(1, "Sample Data"));
		measurementRow.setDataList(dataList);
		measurementRowList.add(measurementRow);

		Assert.assertFalse("Expecting the measurement row list has no measurement data.",
				SettingsController.hasMeasurementDataEntered(2, measurementRowList));
	}

	@Test
	public void testHasMeasurementDataEnteredForVariablesWithAtLeast1WithData() {
		final EditNurseryController editNurseryController = new EditNurseryController();
		final List<Integer> variableIds = new ArrayList<>();
		variableIds.add(1);
		variableIds.add(2);
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		final List<MeasurementData> dataList = new ArrayList<>();
		final MeasurementRow measurementRow = new MeasurementRow();
		dataList.add(this.getSampleMeasurementData(1, "Sample Data"));
		dataList.add(this.getSampleMeasurementData(2, ""));
		measurementRow.setDataList(dataList);
		measurementRowList.add(measurementRow);
		userSelection.setMeasurementRowList(measurementRowList);
		final boolean hasMeasurementData = editNurseryController.hasMeasurementDataEnteredForVariables(variableIds, userSelection);
		Assert.assertTrue("Should return true since there is measuredData", hasMeasurementData);
	}

	@Test
	public void testHasMeasurementDataEnteredForVariablesWithNoData() {
		final EditNurseryController editNurseryController = new EditNurseryController();
		final List<Integer> variableIds = new ArrayList<>();
		variableIds.add(1);
		variableIds.add(2);
		final UserSelection userSelection = new UserSelection();
		final List<MeasurementRow> measurementRowList = new ArrayList<>();
		final List<MeasurementData> dataList = new ArrayList<>();
		final MeasurementRow measurementRow = new MeasurementRow();
		dataList.add(this.getSampleMeasurementData(1, ""));
		dataList.add(this.getSampleMeasurementData(2, ""));
		measurementRow.setDataList(dataList);
		measurementRowList.add(measurementRow);
		userSelection.setMeasurementRowList(measurementRowList);
		final boolean hasMeasurementData = editNurseryController.hasMeasurementDataEnteredForVariables(variableIds, userSelection);
		Assert.assertFalse("Should return false since there is measuredData", hasMeasurementData);
	}

	private MeasurementData getSampleMeasurementData(final Integer variableTermId, final String data) {
		final MeasurementData measurementData = new MeasurementData();
		measurementData.setLabel("LABEL_" + variableTermId);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(variableTermId);
		measurementData.setValue(data);
		measurementData.setMeasurementVariable(measurementVariable);
		return measurementData;
	}

	private List<SettingDetail> createSettingDetailVariables() {
		final List<SettingDetail> variables = new ArrayList<>();
		variables.add(this.createSettingDetail(TermId.TRIAL_INSTANCE_FACTOR.getId(), ""));
		variables.add(this.createSettingDetail(TermId.PI_NAME.getId(), ""));
		variables.add(this.createSettingDetail(TermId.PI_ID.getId(), ""));
		variables.add(this.createSettingDetail(TermId.CHECK_INTERVAL.getId(), ""));
		variables.add(this.createSettingDetail(TermId.CHECK_PLAN.getId(), ""));
		variables.add(this.createSettingDetail(TermId.CHECK_START.getId(), ""));
		return variables;
	}

	private SettingDetail createSettingDetail(final Integer cvTermId, final String value) {
		final SettingVariable variable = new SettingVariable();
		variable.setCvTermId(cvTermId);
		final SettingDetail settingDetail = new SettingDetail(variable, null, value, false);
		return settingDetail;
	}

	@Test
	public void testGetMethod_ById() throws MiddlewareQueryException {
		final CreateNurseryController createNurseryController = new CreateNurseryController();
		final FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		final int id = 70;
		final String name = "Accession into genebank";
		final String code = "AGB1";
		final String programUUID = null;
		final Method method = this.createMethod(id, name, code, programUUID);
		Mockito.doReturn(method).when(fieldbookMiddlewareService).getMethodById(id);

		final String idTermId = Integer.toString(id);
		final Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		studyConditionMap.put(idTermId, this.createMeasurementVariable(idTermId));

		final Method resultingMethod = createNurseryController.getMethod(studyConditionMap, idTermId, code, programUUID);
		Assert.assertEquals(method.getMid(), resultingMethod.getMid());
	}

	@Test
	public void testGetMethod_ById_EmptyValue() throws MiddlewareQueryException {
		final CreateNurseryController createNurseryController = new CreateNurseryController();
		final int id = 70;
		final String code = "AGB1";
		final String programUUID = null;

		final String idTermId = Integer.toString(id);
		final Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		studyConditionMap.put(idTermId, this.createMeasurementVariable(""));

		final Method resultingMethod = createNurseryController.getMethod(studyConditionMap, idTermId, code, programUUID);
		Assert.assertEquals(null, resultingMethod);
	}

	@Test
	public void testGetMethod_ByCode() throws MiddlewareQueryException {
		final CreateNurseryController createNurseryController = new CreateNurseryController();
		final FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		createNurseryController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		final int id = 70;
		final String name = "Accession into genebank";
		final String code = "AGB1";
		final String programUUID = null;
		final Method method = this.createMethod(id, name, code, programUUID);
		Mockito.doReturn(method).when(fieldbookMiddlewareService).getMethodByCode(code, programUUID);

		final String idTermId = Integer.toString(id);
		final Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		studyConditionMap.put(code, this.createMeasurementVariable(code));

		final Method resultingMethod = createNurseryController.getMethod(studyConditionMap, idTermId, code, programUUID);
		Assert.assertEquals(method.getMid(), resultingMethod.getMid());
	}

	@Test
	public void testGetMethod_ByCode_EmptyValue() throws MiddlewareQueryException {
		final CreateNurseryController createNurseryController = new CreateNurseryController();

		final int id = 70;
		final String code = "AGB1";
		final String programUUID = null;

		final String idTermId = Integer.toString(id);
		final Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		studyConditionMap.put(code, this.createMeasurementVariable(""));

		final Method resultingMethod = createNurseryController.getMethod(studyConditionMap, idTermId, code, programUUID);
		Assert.assertEquals(null, resultingMethod);
	}

	@Test
	public void testGetMethod_IdAndCodeNotFound() throws MiddlewareQueryException {
		final CreateNurseryController createNurseryController = new CreateNurseryController();
		final int id = 70;
		final String code = "AGB1";
		final String programUUID = null;
		final String idTermId = Integer.toString(id);
		final Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();

		final Method resultingMethod = createNurseryController.getMethod(studyConditionMap, idTermId, code, programUUID);
		Assert.assertEquals(null, resultingMethod);
	}

	private Method createMethod(final int id, final String name, final String code, final String uniqueID) {
		final Method method = new Method();
		method.setMid(id);
		method.setMname(name);
		method.setMcode(code);
		method.setUniqueID(uniqueID);
		return method;
	}

	private MeasurementVariable createMeasurementVariable(final String value) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("TEST");
		measurementVariable.setValue(value);
		return measurementVariable;
	}

	@Test
	public void testAddVariableSecionIdModelAttributes() {
		final ExtendedModelMap model = new ExtendedModelMap();
		this.controller.addVariableSectionIdentifiers(model);
		SettingsControllerTest.checkVariableSecionIdModelAttributes(model);
	}

	public static void checkVariableSecionIdModelAttributes(final ExtendedModelMap model) {
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
		final SettingDetail settingDetail =
				this.controller.createSettingDetailWithVariableType(this.testVariable.getId(), alias, VariableType.SELECTION_METHOD);
		Assert.assertEquals("Error in Role for settingDetail", VariableType.SELECTION_METHOD.getRole().name(),
				settingDetail.getRole().name());
		Assert.assertEquals("Error in Variable Type", VariableType.SELECTION_METHOD, settingDetail.getVariableType());
		Assert.assertEquals("Expecting variable alias to be used but was not.", alias, settingDetail.getVariable().getName());
		Assert.assertEquals("Expecting variable description to be used but was not.", SettingsControllerTest.TRAIT_DESCRIPTION,
				settingDetail.getVariable().getDescription());
		Assert.assertNull("Error in Value", settingDetail.getValue());

		Assert.assertTrue("Error in Name of PossibleValuesToJson",
				settingDetail.getPossibleValuesJson().contains(this.testValueReference.getName()));
		Assert.assertTrue("Error in Description of PossibleValuesToJson",
				settingDetail.getPossibleValuesJson().contains(this.testValueReference.getDescription()));
		Assert.assertTrue("Error in Key of PossibleValuesToJson",
				settingDetail.getPossibleValuesJson().contains(this.testValueReference.getKey()));
		Assert.assertTrue("Error in Name of PossibleValuesFavoriteToJson",
				settingDetail.getPossibleValuesFavoriteJson().contains(this.testValueReference.getName()));
		Assert.assertTrue("Error in Description of PossibleValuesFavoriteToJson",
				settingDetail.getPossibleValuesFavoriteJson().contains(this.testValueReference.getDescription()));
		Assert.assertTrue("Error in Key of PossibleValuesFavoriteToJson",
				settingDetail.getPossibleValuesFavoriteJson().contains(this.testValueReference.getKey()));

		Mockito.verify(this.variableDataManager, Mockito.times(1)).getVariable(this.contextUtil.getCurrentProgramUUID(),
				this.testVariable.getId(), false, false);
		Mockito.verify(this.fieldbookService, Mockito.times(1)).getAllPossibleValues(this.testVariable.getId());
		Mockito.verify(this.contextUtil, Mockito.times(1)).getProjectInContext();
		Mockito.verify(this.fieldbookService, Mockito.times(1)).getAllPossibleValuesFavorite(this.testVariable.getId(),
				this.controller.getCurrentProject().getUniqueID(), false);
	}

	@Test
	public void testCreateSettingDetailWithVariableTypeWhenAliasIsNull() {
		ContextHolder.setCurrentCrop("maize");
		this.createTestProject();

		final SettingDetail settingDetail =
				this.controller.createSettingDetailWithVariableType(this.testVariable.getId(), null, VariableType.SELECTION_METHOD);
		Assert.assertEquals("Error in Role for settingDetail", VariableType.SELECTION_METHOD.getRole().name(),
				settingDetail.getRole().name());
		Assert.assertEquals("Error in Variable Type", VariableType.SELECTION_METHOD, settingDetail.getVariableType());
		Assert.assertEquals("Expecting variable's standard name to be used since alias is null but was not.",
				SettingsControllerTest.TRAIT_NAME, settingDetail.getVariable().getName());
		Assert.assertEquals("Expecting variable description to be used but was not.", SettingsControllerTest.TRAIT_DESCRIPTION,
				settingDetail.getVariable().getDescription());
		Assert.assertNull("Error in Value", settingDetail.getValue());
	}

	@Test
	public void testCreateSettingDetailWithVariableTypeWhenAliasIsEmpty() {
		ContextHolder.setCurrentCrop("maize");
		this.createTestProject();

		final SettingDetail settingDetail =
				this.controller.createSettingDetailWithVariableType(this.testVariable.getId(), "", VariableType.SELECTION_METHOD);
		Assert.assertEquals("Error in Role for settingDetail", VariableType.SELECTION_METHOD.getRole().name(),
				settingDetail.getRole().name());
		Assert.assertEquals("Error in Variable Type", VariableType.SELECTION_METHOD, settingDetail.getVariableType());
		Assert.assertEquals("Expecting variable's standard name to be used since alias is empty but was not.",
				SettingsControllerTest.TRAIT_NAME, settingDetail.getVariable().getName());
		Assert.assertEquals("Expecting variable description to be used but was not.", SettingsControllerTest.TRAIT_DESCRIPTION,
				settingDetail.getVariable().getDescription());
		Assert.assertNull("Error in Value", settingDetail.getValue());
	}

	private void createTestProject() {
		final Project project = new Project();
		project.setUniqueID(this.programUUID);

		Mockito.when(this.controller.getCurrentProject()).thenReturn(project);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
	}

	private void createTestVariable() {
		final org.generationcp.middleware.domain.ontology.Method method = new org.generationcp.middleware.domain.ontology.Method();
		method.setId(UnitTestDaoIDGenerator.generateId(Method.class));
		method.setName("Method Name");

		final Property property = new Property();
		property.setName("Property Name");
		property.setCropOntologyId("CO:501");
		property.addClass("Class1");
		property.addClass("Class2");

		final Scale scale = new Scale();
		scale.setId(UnitTestDaoIDGenerator.generateId(Scale.class));
		scale.setName("Scale Name");
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		scale.setMinValue("5");
		scale.setMaxValue("500");

		this.testVariable = new Variable();
		this.testVariable.setId(UnitTestDaoIDGenerator.generateId(Variable.class));
		this.testVariable.setMinValue("10");
		this.testVariable.setMaxValue("100");
		this.testVariable.setName(SettingsControllerTest.TRAIT_NAME);
		this.testVariable.setDefinition(SettingsControllerTest.TRAIT_DESCRIPTION);
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
