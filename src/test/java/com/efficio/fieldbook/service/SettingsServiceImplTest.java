
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;

@RunWith(MockitoJUnitRunner.class)
public class SettingsServiceImplTest {

	public static final String PROGRAM_UUID = "63274-0324-9864asdf0-747";

	@Mock
	private Workbook workbook;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@InjectMocks
	private SettingsServiceImpl serviceDUT;

	@Before
	public void init() {

		Mockito.when(contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);

	}

	@Test
	public void testIsGermplasmListFieldReturnsTrueForExistingVariableInNursery() throws MiddlewareQueryException {

		final int standardVariableId = 1;
		final StandardVariable standardVariable = new StandardVariable();
		final Set<VariableType> variableTypes = new HashSet<>();
		variableTypes.add(VariableType.GERMPLASM_DESCRIPTOR);
		standardVariable.setVariableTypes(variableTypes);
		standardVariable.setId(standardVariableId);

		Mockito.when(fieldbookMiddlewareService.getStandardVariable(standardVariableId, PROGRAM_UUID)).thenReturn(standardVariable);

		Assert.assertTrue("Expecting to return true when the variable exists from germplasm descriptor OR experimental design.",
				this.serviceDUT.isGermplasmListField(standardVariableId));
	}

	@Test
	public void testIsGermplasmListFieldReturnsFalseForNonExistingVariableInNursery() throws MiddlewareQueryException {
		final int standardVariableId = 1;
		final StandardVariable standardVariable = new StandardVariable();
		final Set<VariableType> variableTypes = new HashSet<>();
		variableTypes.add(VariableType.ANALYSIS);
		standardVariable.setVariableTypes(variableTypes);
		standardVariable.setId(standardVariableId);

		Mockito.when(fieldbookMiddlewareService.getStandardVariable(standardVariableId, PROGRAM_UUID)).thenReturn(standardVariable);

		Assert.assertFalse("Expecting to return false when the variable do not exist from germplasm descriptor OR experimental design.",
				this.serviceDUT.isGermplasmListField(standardVariableId));
	}

	@Test
	public void testRetrieveTraitsAsLabels() throws Exception {

		final StandardVariable standardVariable = new StandardVariable();
		final Set<VariableType> variableTypes = new HashSet<>();
		variableTypes.add(VariableType.GERMPLASM_DESCRIPTOR);
		standardVariable.setVariableTypes(variableTypes);

		Mockito.when(fieldbookMiddlewareService.getStandardVariable(Mockito.anyInt(), Mockito.anyString())).thenReturn(standardVariable);

		final List<MeasurementVariable> traits = this.initializeListOfVariates();

		Mockito.when(this.workbook.getVariates()).thenReturn(traits);

		final List<LabelFields> result = this.serviceDUT.retrieveTraitsAsLabels(this.workbook);

		Assert.assertEquals("equal results", this.initializeListOfVariates().size(), result.size());
	}

	@Test
	public void testRetrieveExperimentalDesignAsLabelsBlockNoIsAvailable() {

		final StandardVariable standardVariable = new StandardVariable();
		final Set<VariableType> variableTypes = new HashSet<>();
		variableTypes.add(VariableType.GERMPLASM_DESCRIPTOR);
		standardVariable.setVariableTypes(variableTypes);

		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), PROGRAM_UUID)).thenReturn(standardVariable);

		final List<MeasurementVariable> factors = new ArrayList<>();
		final MeasurementVariable blockNo = new MeasurementVariable();
		blockNo.setTermId(TermId.BLOCK_NO.getId());
		factors.add(blockNo);

		Mockito.when(workbook.getFactors()).thenReturn(factors);

		final List<LabelFields> result = this.serviceDUT.retrieveExperimentalDesignFactorsAsLabels(workbook);

		Assert.assertEquals(1, result.size());
		Assert.assertEquals(TermId.BLOCK_NO.getId(), result.get(0).getId());
		Assert.assertTrue(result.get(0).isGermplasmListField());


	}

	@Test
	public void testRetrieveExperimentalDesignAsLabelsBlockNoIsNotAvailable() {

		final List<MeasurementVariable> factors = new ArrayList<>();
		final MeasurementVariable repNo = new MeasurementVariable();
		repNo.setTermId(TermId.REP_NO.getId());
		factors.add(repNo);

		Mockito.when(workbook.getFactors()).thenReturn(factors);

		final List<LabelFields> result = this.serviceDUT.retrieveExperimentalDesignFactorsAsLabels(workbook);

		Assert.assertTrue(result.isEmpty());


	}

	private List<MeasurementVariable> initializeListOfVariates() {
		final List<MeasurementVariable> traits = new ArrayList<>();

		for (int i = 99; i < 110; i++) {
			final MeasurementVariable variate = new MeasurementVariable();
			variate.setName("variate_name" + i);
			variate.setTermId(i);
			traits.add(variate);
		}

		return traits;
	}
}
