
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.util.AppConstants;

@RunWith(MockitoJUnitRunner.class)
public class SettingsServiceImplTest {

	private static final int GERMPLASM_GROUP = AppConstants.SEGMENT_GERMPLASM.getInt();;

	@Mock
	private Workbook workbook;

	@Mock
	private FieldbookService fieldbookService;

	@InjectMocks
	private SettingsServiceImpl serviceDUT;

	@Test
	public void testIsGermplasmListField_ReturnsTrueForExistingVariableInNursery() throws MiddlewareQueryException {
		List<StandardVariableReference> stdVars = new ArrayList<StandardVariableReference>();
		StandardVariableReference stdRef = new StandardVariableReference(1, "Sample Variable");
		stdVars.add(stdRef);
		Mockito.when(
				this.fieldbookService.filterStandardVariablesForSetting(SettingsServiceImplTest.GERMPLASM_GROUP,
						new ArrayList<SettingDetail>())).thenReturn(stdVars);

		Assert.assertTrue("Expecting to return true when the variable exists from germplasm descriptor.",
				this.serviceDUT.isGermplasmListField(1, true));
	}

	@Test
	public void testIsGermplasmListField_ReturnsFalseForNonExistingVariableInNursery() throws MiddlewareQueryException {
		List<StandardVariableReference> stdVars = new ArrayList<StandardVariableReference>();
		Mockito.when(
				this.fieldbookService.filterStandardVariablesForSetting(SettingsServiceImplTest.GERMPLASM_GROUP,
						new ArrayList<SettingDetail>())).thenReturn(stdVars);

		Assert.assertFalse("Expecting to return false when the variable does not exists from germplasm descriptor.",
				this.serviceDUT.isGermplasmListField(1, true));
	}

	@Test
	public void testIsGermplasmListField_ReturnsTrueForExistingVariableInTrial() throws MiddlewareQueryException {
		List<StandardVariableReference> stdVars = new ArrayList<StandardVariableReference>();
		StandardVariableReference stdRef = new StandardVariableReference(1, "Sample Variable");
		stdVars.add(stdRef);
		Mockito.when(
				this.fieldbookService.filterStandardVariablesForTrialSetting(SettingsServiceImplTest.GERMPLASM_GROUP,
						new ArrayList<SettingDetail>())).thenReturn(stdVars);

		Assert.assertTrue("Expecting to return true when the variable exists from germplasm descriptor.",
				this.serviceDUT.isGermplasmListField(1, false));
	}

	@Test
	public void testIsGermplasmListField_ReturnsFalseForNonExistingVariableInTrial() throws MiddlewareQueryException {
		List<StandardVariableReference> stdVars = new ArrayList<StandardVariableReference>();
		Mockito.when(
				this.fieldbookService.filterStandardVariablesForTrialSetting(SettingsServiceImplTest.GERMPLASM_GROUP,
						new ArrayList<SettingDetail>())).thenReturn(stdVars);

		Assert.assertFalse("Expecting to return false when the variable does not exists from germplasm descriptor.",
				this.serviceDUT.isGermplasmListField(1, true));
	}

	@Test
	public void testRetrieveTraitsAsLabels() throws Exception {
		List<MeasurementVariable> traits = this.initializeListOfVariates();

		Mockito.when(this.workbook.getVariates()).thenReturn(traits);

		List<LabelFields> result = this.serviceDUT.retrieveTraitsAsLabels(this.workbook);

		Mockito.verify(this.workbook, Mockito.times(1)).getVariates();

		Assert.assertEquals("equal results", this.initializeListOfVariates().size(), result.size());
	}

	private List<MeasurementVariable> initializeListOfVariates() {
		List<MeasurementVariable> traits = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			MeasurementVariable variate = new MeasurementVariable();
			variate.setName("variate_name" + i);
			variate.setTermId(i);

			traits.add(variate);
		}

		return traits;
	}
}
