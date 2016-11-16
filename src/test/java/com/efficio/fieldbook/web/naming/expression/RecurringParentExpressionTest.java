package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import junit.framework.Assert;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class RecurringParentExpressionTest extends TestExpression {

	@Mock
	private PedigreeDataManager pedigreeDataManager;

	@InjectMocks
	private RecurringParentExpression expression = new RecurringParentExpression();

	@Test
	public void testResolveRecurringParentFemale() {

		int maleParentGid = 1;
		int femaleParentGid = 2;

		Mockito.when(pedigreeDataManager.calculateRecurrentParent(maleParentGid, femaleParentGid))
				.thenReturn(PedigreeDataManager.FEMALE_RECURRENT);

		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[RCRPRNT]", null, true);
		List<StringBuilder> values = this.createInitialValues(source);
		source.setFemaleGid(femaleParentGid);
		source.setMaleGid(maleParentGid);

		expression.apply(values, source);

		Assert.assertEquals("GERMPLASM_TEST-F", values.get(0).toString());

	}

	@Test
	public void testResolveRecurringParentMale() {

		int maleParentGid = 1;
		int femaleParentGid = 2;

		Mockito.when(pedigreeDataManager.calculateRecurrentParent(maleParentGid, femaleParentGid))
				.thenReturn(PedigreeDataManager.MALE_RECURRENT);

		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[RCRPRNT]", null, true);
		List<StringBuilder> values = this.createInitialValues(source);
		source.setFemaleGid(femaleParentGid);
		source.setMaleGid(maleParentGid);

		expression.apply(values, source);

		Assert.assertEquals("GERMPLASM_TEST-M", values.get(0).toString());

	}

	@Test
	public void testResolveRecurringParentWithProcessCodePrefix() {

		int maleParentGid = 1;
		int femaleParentGid = 2;

		Mockito.when(pedigreeDataManager.calculateRecurrentParent(maleParentGid, femaleParentGid))
				.thenReturn(PedigreeDataManager.MALE_RECURRENT);

		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "B[RCRPRNT]", null, true);
		List<StringBuilder> values = this.createInitialValues(source);
		source.setFemaleGid(femaleParentGid);
		source.setMaleGid(maleParentGid);

		expression.apply(values, source);

		Assert.assertEquals("GERMPLASM_TEST-BM", values.get(0).toString());

	}

	@Test
	public void testResolveRecurringParentWithNoRecurringParent() {

		int maleParentGid = 1;
		int femaleParentGid = 2;

		Mockito.when(pedigreeDataManager.calculateRecurrentParent(maleParentGid, femaleParentGid)).thenReturn(PedigreeDataManager.NONE);

		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[RCRPRNT]", null, true);
		List<StringBuilder> values = this.createInitialValues(source);
		source.setFemaleGid(femaleParentGid);
		source.setMaleGid(maleParentGid);

		expression.apply(values, source);

		Assert.assertEquals("GERMPLASM_TEST-", values.get(0).toString());

	}

	@Test
	public void testResolveRecurringParentWithNoRecurringParentWithProcessCodePrefix() {

		int maleParentGid = 1;
		int femaleParentGid = 2;

		Mockito.when(pedigreeDataManager.calculateRecurrentParent(maleParentGid, femaleParentGid)).thenReturn(PedigreeDataManager.NONE);

		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "B[RCRPRNT]", null, true);
		List<StringBuilder> values = this.createInitialValues(source);
		source.setFemaleGid(femaleParentGid);
		source.setMaleGid(maleParentGid);

		expression.apply(values, source);

		Assert.assertEquals("GERMPLASM_TEST-B", values.get(0).toString());

	}

}
