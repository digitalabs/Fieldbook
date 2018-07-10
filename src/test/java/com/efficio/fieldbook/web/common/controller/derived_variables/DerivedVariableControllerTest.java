package com.efficio.fieldbook.web.common.controller.derived_variables;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.data.initializer.SettingDetailTestDataInitializer;
import com.google.common.base.Optional;
import org.apache.commons.lang.math.RandomUtils;
import org.fest.util.Collections;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DerivedVariableControllerTest {

	public static final int STUDY_ID = 111;
	public static final int VARIABLE1_TERMID = 123;
	public static final int VARIABLE2_TERMID = 456;
	public static final int VARIABLE3_TERMID = 789;
	public static final int VARIABLE4_TERMID = 999;

	private static final String INVALID_REQUEST = "invalid request";
	private static final String NOT_FOUND = "not found";
	private static final String ENGINE_EXCEPTION = "engine exception";
	private static final String MISSING_DATA = "missing data";

	private static final String TERM_1 = "51496"; // GW_DW_g100grn - Grain weight BY GW DW - Measurement IN G/100grain
	private static final String TERM_2 = "50889"; // GMoi_NIRS_pct - Grain moisture BY NIRS Moi - Measurement IN %
	private static final String TERM_3 = "20358"; // PlotArea_m2 - Plot size
	private static final String TERM_4_EMPTY_VALUE = "20439"; // MRFVInc_Cmp_pct
	private static final String TERM_VALUE_1 = "1000";
	private static final String TERM_VALUE_2 = "12.5";
	private static final String TERM_VALUE_3 = "10";
	private static final String FORMULA = "({{" + TERM_1 + "}}/100)*((100-{{" + TERM_2 + "}})/(100-12.5))*(10/{{" + TERM_3 + "}})";
	private static final String FORMULA_RESULT = "10";

	private FormulaDto formulaDTO;

	@Mock
	private UserSelection studySelection;

	@Mock
	private MessageSource messageSource;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private FormulaService formulaService;

	@Mock
	private StudyService studyService;

	@Mock
	private DerivedVariableProcessor processor;

	@InjectMocks
	private DerivedVariableController derivedVariableController;

	@Before
	public void init() {

		final Workbook workbook = new Workbook();
		workbook.setStudyDetails(new StudyDetails());
		workbook.getStudyDetails().setId(STUDY_ID);

		final List<MeasurementRow> observations = new ArrayList<>();
		MeasurementRow measurementRow = createMeasurementRowTestData();
		observations.add(measurementRow);
		workbook.setObservations(observations);

		when(this.studySelection.getBaselineTraitsList()).thenReturn(this.createSettingDetails());
		when(this.studySelection.getWorkbook()).thenReturn(workbook);

		Locale locale = Locale.getDefault();
		when(this.messageSource.getMessage("study.execute.calculation.invalid.request", null, locale)).thenReturn(INVALID_REQUEST);
		when(this.messageSource.getMessage("study.execute.calculation.formula.not.found", null, locale)).thenReturn(NOT_FOUND);
		when(this.messageSource.getMessage("study.execute.calculation.engine.exception", null, locale)).thenReturn(ENGINE_EXCEPTION);
		when(this.messageSource.getMessage("study.execute.calculation.missing.data", null, locale)).thenReturn(MISSING_DATA);

		final Optional<FormulaDto> formulaOptional = Mockito.mock(Optional.class);
		Mockito.when(formulaOptional.isPresent()).thenReturn(true);
		when(this.formulaService.getByTargetId(anyInt())).thenReturn(formulaOptional);

		formulaDTO = new FormulaDto();
		formulaDTO.setDefinition(FORMULA);
		formulaDTO.setTargetTermId(Integer.valueOf(TERM_1));
		Mockito.when(formulaOptional.get()).thenReturn(formulaDTO);
	}

	@Test
	public void testExecute() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		request.setGeoLocationId(RandomUtils.nextInt());
		request.setVariableId(RandomUtils.nextInt());

		ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void testExecute_InvalidRequest() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();

		ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		Assert.assertEquals(INVALID_REQUEST, response.getBody().get("errorMessage"));
	}

	@Test
	public void testExecute_FormulaNotFound() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		request.setGeoLocationId(RandomUtils.nextInt());
		request.setVariableId(RandomUtils.nextInt());

		when(this.formulaService.getByTargetId(anyInt())).thenReturn(Optional.<FormulaDto>absent());

		ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		Assert.assertEquals(NOT_FOUND, response.getBody().get("errorMessage"));
	}

	@Test
	public void testExecute_Evaluate() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		int locationId = RandomUtils.nextInt();
		request.setGeoLocationId(locationId);
		request.setVariableId(RandomUtils.nextInt());
		for (MeasurementRow observation : this.studySelection.getWorkbook().getObservations()) {
			observation.setLocationId(locationId);
		}

		when(this.processor.evaluateFormula(anyString(), any(Map.class))).thenReturn(FORMULA_RESULT);

		ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
		Assert.assertFalse(response.getBody().containsKey("inputMissingData"));
	}

	@Test
	public void testExecute_EvaluateMissingData() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		int locationId = RandomUtils.nextInt();
		request.setGeoLocationId(locationId);
		request.setVariableId(RandomUtils.nextInt());
		List<MeasurementRow> observations = this.studySelection.getWorkbook().getObservations();
		MeasurementData measurementData = observations.get(0).getMeasurementData(Integer.valueOf(TERM_1));
		measurementData.setValue("");

		for (MeasurementRow observation : this.studySelection.getWorkbook().getObservations()) {
			observation.setLocationId(locationId);
		}

		when(this.processor.evaluateFormula(anyString(), any(Map.class))).thenReturn(FORMULA_RESULT);

		ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
		Assert.assertTrue(response.getBody().containsKey("inputMissingData"));
	}

	@Test
	public void testExecute_EvaluateException() {
		final BindingResult bindingResult = Mockito.mock(BindingResult.class);
		final CalculateVariableRequest request = new CalculateVariableRequest();
		int locationId = RandomUtils.nextInt();
		request.setGeoLocationId(locationId);
		request.setVariableId(RandomUtils.nextInt());
		for (MeasurementRow observation : this.studySelection.getWorkbook().getObservations()) {
			observation.setLocationId(locationId);
		}

		when(this.processor.evaluateFormula(anyString(), any(Map.class))).thenThrow(new RuntimeException());

		ResponseEntity<Map<String, Object>> response = this.derivedVariableController.execute(request, bindingResult);

		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		Assert.assertEquals(ENGINE_EXCEPTION, response.getBody().get("errorMessage"));
	}

	@Test
	public void testDependencyVariablesArgumentVariablesAreNotPresentInTheStudy() {

		final Set<Integer> variableIdsOfTraitsInStudy = Collections.set(VARIABLE1_TERMID, VARIABLE2_TERMID);
		final List<SettingDetail> settingDetails = new ArrayList<SettingDetail>();
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(VARIABLE1_TERMID, "VARIABLE1", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(VARIABLE2_TERMID, "VARIABLE2", "", "TRIAL"));

		final Set<FormulaVariable> formulaVariables = this.createFormulaVariables();
		when(this.studySelection.getBaselineTraitsList()).thenReturn(settingDetails);
		when(this.formulaService.getAllFormulaVariables(variableIdsOfTraitsInStudy)).thenReturn(this.createFormulaVariables());

		final ResponseEntity<Set<String>> result = this.derivedVariableController.dependencyVariables();

		assertEquals(formulaVariables.size(), result.getBody().size());
		for (FormulaVariable formulaVariable : formulaVariables) {
			result.getBody().contains(formulaVariable.getName());
		}

	}

	@Test
	public void testDependencyVariablesArgumentVariablesArePresentInTheStudy() {

		when(this.studySelection.getBaselineTraitsList()).thenReturn(this.createSettingDetails());
		when(this.formulaService.getAllFormulaVariables(Matchers.anySet())).thenReturn(this.createFormulaVariables());

		final ResponseEntity<Set<String>> result = this.derivedVariableController.dependencyVariables();

		assertTrue(result.getBody().isEmpty());

	}

	@Test
	public void testDependencyVariableHasMeasurementData() {

		final List<Integer> idsToBeRemoved = Collections.list(VARIABLE3_TERMID);
		final Set<Integer> variableIdsOfTraitsInStudy = derivedVariableController.getVariableIdsOfTraitsInStudy();

		when(this.formulaService.getAllFormulaVariables(variableIdsOfTraitsInStudy)).thenReturn(this.createFormulaVariables());
		when(this.studyService.hasMeasurementDataEntered(Matchers.anyList(), Matchers.eq(STUDY_ID))).thenReturn(true);

		final ResponseEntity<Boolean> result = derivedVariableController.dependencyVariableHasMeasurementData(idsToBeRemoved);

		final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		verify(this.studyService).hasMeasurementDataEntered(captor.capture(), Matchers.eq(STUDY_ID));

		final List<Integer> derivedVariablesDependencies = captor.getValue();

		assertTrue(derivedVariablesDependencies.contains(VARIABLE3_TERMID));
		assertTrue(result.getBody());

	}

	@Test
	public void testDependencyVariableHasMeasurementDataDerivedVariableAndItsDepencyVariablesAreRemoved() {

		final List<Integer> idsToBeRemoved = Collections.list(VARIABLE3_TERMID, VARIABLE1_TERMID);
		final Set<Integer> variableIdsOfTraitsInStudy = derivedVariableController.getVariableIdsOfTraitsInStudy();

		when(this.formulaService.getAllFormulaVariables(variableIdsOfTraitsInStudy)).thenReturn(this.createFormulaVariables());
		when(this.studyService.hasMeasurementDataEntered(Matchers.anyList(), Matchers.eq(STUDY_ID))).thenReturn(false);

		final ResponseEntity<Boolean> result = derivedVariableController.dependencyVariableHasMeasurementData(idsToBeRemoved);

		verify(this.studyService, Mockito.times(0)).hasMeasurementDataEntered(Matchers.anyList(), anyInt());

		assertFalse(result.getBody());

	}

	@Test
	public void testGetVariableIdsOfTraitsInStudy() {

		final Set<Integer> result = derivedVariableController.getVariableIdsOfTraitsInStudy();
		assertTrue(result.contains(VARIABLE1_TERMID));
		assertTrue(result.contains(VARIABLE2_TERMID));

	}

	private List<SettingDetail> createSettingDetails() {

		final List<SettingDetail> settingDetails = new ArrayList<SettingDetail>();

		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(VARIABLE1_TERMID, "VARIABLE1", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(VARIABLE2_TERMID, "VARIABLE2", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(VARIABLE3_TERMID, "VARIABLE3", "", "TRIAL"));
		settingDetails.add(SettingDetailTestDataInitializer.createSettingDetail(VARIABLE4_TERMID, "VARIABLE4", "", "TRIAL"));

		return settingDetails;

	}

	private Set<FormulaVariable> createFormulaVariables() {

		final Set<FormulaVariable> formulaVariables = new HashSet<>();

		final FormulaVariable formulaVariable1 = new FormulaVariable();
		formulaVariable1.setId(VARIABLE3_TERMID);
		formulaVariable1.setName("VARIABLE3");
		formulaVariable1.setTargetTermId(VARIABLE1_TERMID);

		final FormulaVariable formulaVariable2 = new FormulaVariable();
		formulaVariable2.setId(VARIABLE4_TERMID);
		formulaVariable2.setName("VARIABLE4");
		formulaVariable2.setTargetTermId(VARIABLE2_TERMID);

		formulaVariables.add(formulaVariable1);
		formulaVariables.add(formulaVariable2);

		return formulaVariables;

	}

	private MeasurementRow createMeasurementRowTestData() {
		MeasurementRow measurementRow = new MeasurementRow();
		measurementRow.setDataList(this.createMeasurementDataListTestData());
		return measurementRow;
	}

	private List<MeasurementData> createMeasurementDataListTestData() {
		List<MeasurementData> measurementDataList = new ArrayList<>();
		measurementDataList.add(this.createMeasurementDataTestData(TERM_1, TERM_VALUE_1, null));
		measurementDataList.add(this.createMeasurementDataTestData(TERM_2, TERM_VALUE_2, null));
		measurementDataList.add(this.createMeasurementDataTestData(TERM_3, TERM_VALUE_3, null));
		measurementDataList.add(this.createMeasurementDataTestData(TERM_4_EMPTY_VALUE, "", ""));
		return measurementDataList;
	}

	private MeasurementData createMeasurementDataTestData(String label, String value, String cValueId) {
		MeasurementData measurementData = new MeasurementData();
		measurementData.setLabel(label);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(Integer.valueOf(label));
		measurementVariable.setPossibleValues(new ArrayList<ValueReference>());
		measurementVariable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		measurementData.setMeasurementVariable(measurementVariable);
		measurementData.setValue(value);
		measurementData.setcValueId(cValueId);
		return measurementData;
	}

}
