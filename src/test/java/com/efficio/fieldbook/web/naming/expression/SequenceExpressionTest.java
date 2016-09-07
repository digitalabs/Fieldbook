package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class SequenceExpressionTest extends TestExpression {

	private static final String KEY = "[SEQUENCE]";
	private static final String SEPARATOR = "-";
	private static final String GERMPLASM_NAME = "GERMPLASM_TEST";
	private static final String RESULT_DESIG = "GERMPLASM_TEST-5";
	
	private SequenceExpression expression;
	
	@Mock
	private GermplasmDataManager germplasmDataManager;
	
	@Before
	public void setUp() {
		expression = new SequenceExpression();
		expression.setGermplasmDataManager(germplasmDataManager);
	}
	
	@Test
	public void testSequence() throws Exception {
		AdvancingSource source = this.createAdvancingSourceTestData(GERMPLASM_NAME, SEPARATOR, null, KEY, null, true);
		source.setPlantsSelected(5);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the processcodes should not be equal to " + GERMPLASM_NAME, GERMPLASM_NAME.equals(values.get(0).toString()));
		expression.apply(values, source);
		Assert.assertEquals("The value after applying the processcodes should be equal to " + RESULT_DESIG, RESULT_DESIG, values.get(0).toString());
	}

	@Test
	public void testNegativeNumber() throws Exception {
		AdvancingSource source = this.createAdvancingSourceTestData(GERMPLASM_NAME, SEPARATOR, null, KEY, null, true);
		source.setPlantsSelected(-2);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the processcodes should not be equal to " + GERMPLASM_NAME, GERMPLASM_NAME.equals(values.get(0).toString()));
		expression.apply(values, source);
		Assert.assertEquals("The value after applying the processcodes should be equal to " + GERMPLASM_NAME + SEPARATOR, GERMPLASM_NAME + SEPARATOR, values.get(0).toString());
	}

	@Test
	public void testCaseSensitive() throws Exception {
		AdvancingSource source = this.createAdvancingSourceTestData(GERMPLASM_NAME, SEPARATOR, null, "[sequence]", null, true);
		source.setPlantsSelected(5);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the processcodes should not be equal to " + GERMPLASM_NAME, GERMPLASM_NAME.equals(values.get(0).toString()));
		expression.apply(values, source);
		Assert.assertEquals("The value after applying the processcodes should be equal to " + RESULT_DESIG, RESULT_DESIG, values.get(0).toString());
	}

	@Test
	public void testWithStartCount() throws Exception {
		AdvancingSource source = this.createAdvancingSourceTestData(GERMPLASM_NAME, SEPARATOR, null, "[sequence]", null, true);
		source.setPlantsSelected(5);
		source.setCurrentMaxSequence(5);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the processcodes should not be equal to " + GERMPLASM_NAME, GERMPLASM_NAME.equals(values.get(0).toString()));
		expression.apply(values, source);
		Assert.assertEquals("The value after applying the processcodes should be equal to " + RESULT_DESIG, RESULT_DESIG, values.get(0).toString());
	}

	@Test
	public void testNonBulkingSequenceGenerationWithSequenceNumberEqualsTo1() {
		Mockito.when(germplasmDataManager.getNextSequenceNumberForCrossName(Matchers.anyString())).thenReturn("1");
		// final false refers to nonBulking
		AdvancingSource source = this.createAdvancingSourceTestData(GERMPLASM_NAME, SEPARATOR, null, KEY, null, false);
		source.setPlantsSelected(5);
		source.setCurrentMaxSequence(5);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the processcodes should not be equal to " + GERMPLASM_NAME, GERMPLASM_NAME.equals(values.get(0).toString()));
		expression.apply(values, source);
		final String resultDesig = GERMPLASM_NAME + SEPARATOR + 6;
		Assert.assertEquals("The value after applying the processcodes should be equal to " + resultDesig, resultDesig, values.get(0).toString());
	}
	
	@Test
	public void testNonBulkingSequenceGenerationWithSequenceNumberEqualsTo5() {
		Mockito.when(germplasmDataManager.getNextSequenceNumberForCrossName(Matchers.anyString())).thenReturn("5");
		// final false refers to nonBulking
		AdvancingSource source = this.createAdvancingSourceTestData(GERMPLASM_NAME, SEPARATOR, null, KEY, null, false);
		source.setPlantsSelected(5);
		source.setCurrentMaxSequence(5);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the processcodes should not be equal to " + GERMPLASM_NAME, GERMPLASM_NAME.equals(values.get(0).toString()));
		expression.apply(values, source);
		final String resultDesig = GERMPLASM_NAME + SEPARATOR + 10;
		Assert.assertEquals("The value after applying the processcodes should be equal to " + resultDesig, resultDesig, values.get(0).toString());
	}
	
	@Test
	public void testNonBulkingSequenceGenerationWithNegativePlantsSelectedValue() {
		Mockito.when(germplasmDataManager.getNextSequenceNumberForCrossName(Matchers.anyString())).thenReturn("5");
		// final false refers to nonBulking
		AdvancingSource source = this.createAdvancingSourceTestData(GERMPLASM_NAME, SEPARATOR, null, KEY, null, false);
		source.setPlantsSelected(-1);
		source.setCurrentMaxSequence(5);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the processcodes should not be equal to " + GERMPLASM_NAME, GERMPLASM_NAME.equals(values.get(0).toString()));
		expression.apply(values, source);
		final String resultDesig = GERMPLASM_NAME + SEPARATOR;
		Assert.assertEquals("The value after applying the processcodes should be equal to " + resultDesig, resultDesig, values.get(0).toString());
	}
	
	@Test
	public void testNonBulkingSequenceGenerationWithNegativeCurrentMaxSequencedValue() {
		// final false refers to nonBulking
		AdvancingSource source = this.createAdvancingSourceTestData(GERMPLASM_NAME, SEPARATOR, null, KEY, null, false);
		source.setPlantsSelected(1);
		source.setCurrentMaxSequence(-5);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the processcodes should not be equal to " + GERMPLASM_NAME, GERMPLASM_NAME.equals(values.get(0).toString()));
		expression.apply(values, source);
		final String resultDesig = GERMPLASM_NAME + SEPARATOR + 1;
		Assert.assertEquals("The value after applying the processcodes should be equal to " + resultDesig, resultDesig, values.get(0).toString());
	}
}