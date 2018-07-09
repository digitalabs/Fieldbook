package com.efficio.fieldbook.web.common.controller.derived_variables;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.data.initializer.SettingDetailTestDataInitializer;
import org.fest.util.Collections;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DerivedVariableControllerTest {

	public static final int STUDY_ID = 111;
	public static final int VARIABLE1_TERMID = 123;
	public static final int VARIABLE2_TERMID = 456;
	public static final int VARIABLE3_TERMID = 789;
	public static final int VARIABLE4_TERMID = 999;

	@Mock
	private UserSelection studySelection;

	@Mock
	private ResourceBundleMessageSource messageSource;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private FormulaService formulaService;

	@Mock
	private StudyService studyService;

	@InjectMocks
	private DerivedVariableController derivedVariableController;

	@Before
	public void init() {

		final Workbook workbook = new Workbook();
		workbook.setStudyDetails(new StudyDetails());
		workbook.getStudyDetails().setId(STUDY_ID);

		when(this.studySelection.getBaselineTraitsList()).thenReturn(this.createSettingDetails());
		when(this.studySelection.getWorkbook()).thenReturn(workbook);
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

		verify(this.studyService, Mockito.times(0)).hasMeasurementDataEntered(Matchers.anyList(), Matchers.anyInt());

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

}
