
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;

@RunWith(MockitoJUnitRunner.class)
public class SettingsServiceImplTest {

	private static final int GERMPLASM_GROUP = VariableType.GERMPLASM_DESCRIPTOR.getId();

	@Mock
	private Workbook workbook;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private SettingsServiceImpl serviceDUT;

	@Test
	public void testIsGermplasmListField_ReturnsTrueForExistingVariableInNursery() throws MiddlewareQueryException {
		Mockito.doReturn(this.createStandardVariableTestData(VariableType.GERMPLASM_DESCRIPTOR)).when(this.fieldbookMiddlewareService)
				.getStandardVariable(Matchers.anyInt(), Matchers.anyString());

		Assert.assertTrue("Expecting to return true when the variable exists from germplasm descriptor.",
				this.serviceDUT.isGermplasmListField(1, true));
	}

	@Test
	public void testIsGermplasmListField_ReturnsFalseForNonExistingVariableInNursery() throws MiddlewareQueryException {
		Mockito.doReturn(this.createStandardVariableTestData(VariableType.NURSERY_CONDITION)).when(this.fieldbookMiddlewareService)
				.getStandardVariable(Matchers.anyInt(), Matchers.anyString());

		Assert.assertFalse("Expecting to return false when the variable does not exists from germplasm descriptor.",
				this.serviceDUT.isGermplasmListField(1, true));
	}

	@Test
	public void testRetrieveTraitsAsLabels() throws Exception {
		final List<MeasurementVariable> traits = this.initializeListOfVariates();

		Mockito.when(this.workbook.getVariates()).thenReturn(traits);
		Mockito.doReturn(this.createStandardVariableTestData(VariableType.GERMPLASM_DESCRIPTOR)).when(this.fieldbookMiddlewareService)
				.getStandardVariable(Matchers.anyInt(), Matchers.anyString());

		final List<LabelFields> result = this.serviceDUT.retrieveTraitsAsLabels(this.workbook);

		Mockito.verify(this.workbook, Mockito.times(1)).getVariates();

		Assert.assertEquals("equal results", this.initializeListOfVariates().size(), result.size());
	}

	private StandardVariable createStandardVariableTestData(final VariableType variableType) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setVariableTypes(new HashSet<VariableType>());
		stdVar.getVariableTypes().add(variableType);
		return stdVar;
	}

	private List<MeasurementVariable> initializeListOfVariates() {
		final List<MeasurementVariable> traits = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			final MeasurementVariable variate = new MeasurementVariable();
			variate.setName("variate_name" + i);
			variate.setTermId(i);

			traits.add(variate);
		}

		return traits;
	}

	@Test
	public void testRetrieveGermplasmDescriptorsAsLabels() {
		final List<MeasurementVariable> factors = this.createFactorsTestData();
		Mockito.doReturn(factors).when(this.workbook).getFactors();

		Mockito.doReturn(this.createStandardVariableTestData(VariableType.GERMPLASM_DESCRIPTOR)).when(this.fieldbookMiddlewareService)
				.getStandardVariable(Matchers.anyInt(), Matchers.anyString());

		final List<LabelFields> labelFields = this.serviceDUT.retrieveGermplasmDescriptorsAsLabels(this.workbook);
		Assert.assertNotNull("Label fields should not be null", labelFields);
		Assert.assertEquals("There should be 2 label fields returned", 2, labelFields.size());

		// verify expected label fields based on the test data
		boolean plotNoIsFound = false;
		for (final LabelFields label : labelFields) {
			if (label.getId() == TermId.PLOT_NO.getId()) {
				plotNoIsFound = true;
			}
		}
		Assert.assertTrue("The plot no should be found", plotNoIsFound);
	}

	private List<MeasurementVariable> createFactorsTestData() {
		final List<MeasurementVariable> factors = new ArrayList<>();
		// add variables that cannot be considered as germplasm descriptors
		factors.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId()));
		factors.add(this.createMeasurementVariable(TermId.BLOCK_NO.getId()));
		factors.add(this.createTreatmentFactorMeasurementVariable());
		factors.add(this.createMeasurementVariableWithRole(PhenotypicType.TRIAL_DESIGN));
		// add variables that can be considered as germplasm descriptors
		factors.add(this.createMeasurementVariable(TermId.PLOT_NO.getId()));
		factors.add(this.createMeasurementVariableWithRole(PhenotypicType.GERMPLASM));
		return factors;
	}

	private MeasurementVariable createMeasurementVariableWithRole(final PhenotypicType phenotypicType) {
		// use dummy id as we only want to set the role
		final MeasurementVariable mVarWithRole = this.createMeasurementVariable(new Double(Math.random()).intValue());
		mVarWithRole.setRole(phenotypicType);
		return mVarWithRole;
	}

	private MeasurementVariable createTreatmentFactorMeasurementVariable() {
		// use dummy id as checking for treatment factor is checked via the treatment factor
		final MeasurementVariable treatmentFactor = this.createMeasurementVariable(3455);
		treatmentFactor.setTreatmentLabel("DUMMY_TREATMENT_LABEL");
		return treatmentFactor;
	}

	private MeasurementVariable createMeasurementVariable(final int termId) {
		final MeasurementVariable mVar = new MeasurementVariable();
		mVar.setTermId(termId);
		return mVar;
	}
}
