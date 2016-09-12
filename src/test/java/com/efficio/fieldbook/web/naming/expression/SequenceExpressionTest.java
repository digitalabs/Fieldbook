
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
		this.expression = new SequenceExpression();
		this.expression.setGermplasmDataManager(this.germplasmDataManager);
	}

	@Test
	public void testSequence() throws Exception {
		final AdvancingSource source = this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.SEPARATOR, null, SequenceExpressionTest.KEY, null, true);
		source.setPlantsSelected(5);
		final List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		Assert.assertEquals("The value after applying the process codes should be equal to " + SequenceExpressionTest.RESULT_DESIG,
				SequenceExpressionTest.RESULT_DESIG, values.get(0).toString());
	}

	@Test
	public void testNegativeNumber() throws Exception {
		final AdvancingSource source = this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.SEPARATOR, null, SequenceExpressionTest.KEY, null, true);
		source.setPlantsSelected(-2);
		final List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		final String resultDesig = SequenceExpressionTest.GERMPLASM_NAME + SequenceExpressionTest.SEPARATOR;
		Assert.assertEquals("The value after applying the process codes should be equal to " + resultDesig, resultDesig,
				values.get(0).toString());
	}

	@Test
	public void testCaseSensitive() throws Exception {
		final AdvancingSource source = this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.SEPARATOR, null, "[sequence]", null, true);
		source.setPlantsSelected(5);
		final List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		Assert.assertEquals("The value after applying the process codes should be equal to " + SequenceExpressionTest.RESULT_DESIG,
				SequenceExpressionTest.RESULT_DESIG, values.get(0).toString());
	}

	@Test
	public void testWithStartCount() throws Exception {
		final AdvancingSource source = this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.SEPARATOR, null, "[sequence]", null, true);
		source.setPlantsSelected(5);
		source.setCurrentMaxSequence(5);
		final List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		Assert.assertEquals("The value after applying the process codes should be equal to " + SequenceExpressionTest.RESULT_DESIG,
				SequenceExpressionTest.RESULT_DESIG, values.get(0).toString());
	}

	@Test
	public void testNonBulkingSequenceGenerationWithSequenceNumberEqualsTo1() {
		Mockito.when(this.germplasmDataManager.getNextSequenceNumberForCrossName(Matchers.anyString())).thenReturn("1");
		// final false refers to nonBulking
		final AdvancingSource source = this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.SEPARATOR, null, SequenceExpressionTest.KEY, null, false);
		source.setPlantsSelected(5);
		source.setCurrentMaxSequence(5);
		final List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		final String resultDesig = SequenceExpressionTest.GERMPLASM_NAME + SequenceExpressionTest.SEPARATOR + 6;
		Assert.assertEquals("The value after applying the process codes should be equal to " + resultDesig, resultDesig,
				values.get(0).toString());
	}

	@Test
	public void testNonBulkingSequenceGenerationWithSequenceNumberEqualsTo5() {
		Mockito.when(this.germplasmDataManager.getNextSequenceNumberForCrossName(Matchers.anyString())).thenReturn("5");
		// final false refers to nonBulking
		final AdvancingSource source = this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.SEPARATOR, null, SequenceExpressionTest.KEY, null, false);
		source.setPlantsSelected(5);
		source.setCurrentMaxSequence(5);
		final List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		final String resultDesig = SequenceExpressionTest.GERMPLASM_NAME + SequenceExpressionTest.SEPARATOR + 6;
		Assert.assertEquals("The value after applying the process codes should be equal to " + resultDesig, resultDesig,
				values.get(0).toString());
	}

	@Test
	public void testNonBulkingSequenceGenerationWithNegativePlantsSelectedValue() {
		Mockito.when(this.germplasmDataManager.getNextSequenceNumberForCrossName(Matchers.anyString())).thenReturn("5");
		// final false refers to nonBulking
		final AdvancingSource source = this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.SEPARATOR, null, SequenceExpressionTest.KEY, null, false);
		source.setPlantsSelected(-1);
		source.setCurrentMaxSequence(5);
		final List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		final String resultDesig = SequenceExpressionTest.GERMPLASM_NAME + SequenceExpressionTest.SEPARATOR;
		Assert.assertEquals("The value after applying the process codes should be equal to " + resultDesig, resultDesig,
				values.get(0).toString());
	}

	@Test
	public void testNonBulkingSequenceGenerationWithNegativeCurrentMaxSequencedValue() {
		// final false refers to nonBulking
		final AdvancingSource source = this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.SEPARATOR, null, SequenceExpressionTest.KEY, null, false);
		source.setPlantsSelected(1);
		source.setCurrentMaxSequence(-5);
		final List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		final String resultDesig = SequenceExpressionTest.GERMPLASM_NAME + SequenceExpressionTest.SEPARATOR + 1;
		Assert.assertEquals("The value after applying the process codes should be equal to " + resultDesig, resultDesig,
				values.get(0).toString());
	}
}
