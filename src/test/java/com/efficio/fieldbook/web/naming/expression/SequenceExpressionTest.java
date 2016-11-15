package com.efficio.fieldbook.web.naming.expression;

import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Sets;
import junit.framework.Assert;

import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@RunWith(MockitoJUnitRunner.class)
public class SequenceExpressionTest extends TestExpression {

	private final static String KEY = "[SEQUENCE]";
	private final static String SEPARATOR = "-";
	private final static String GERMPLASM_NAME = "GERMPLASM_TEST";
	private final static String BREEDING_METHOD_PREFIX = "PREFIX";
	private final static String RESULT_DESIG = "GERMPLASM_TEST-5";

	private final static Integer NEXT_SEQ_NO_12 = 12;
	private final static Integer NEXT_SEQ_NO_13 = 13;
	private final static Integer NEXT_SEQ_NO_14 = 14;
	private final static Integer NEXT_SEQ_NO_15 = 15;
	private final static Integer NEXT_SEQ_NO_16 = 16;

	private SequenceExpression expression;

	@Mock
	KeySequenceRegisterService keySequenceRegisterService;

	@Before
	public void setUp() {
		expression = new SequenceExpression();
		expression.setKeySequenceRegisterService(keySequenceRegisterService);
	}

	@Test
	public void testSequence() throws Exception {
		AdvancingSource source =
				this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME, SequenceExpressionTest.SEPARATOR, null,
						SequenceExpressionTest.KEY, null, true);
		source.setPlantsSelected(5);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		expression.apply(values, source);

		Assert.assertEquals("The value after applying the process codes should be equal to " + RESULT_DESIG, RESULT_DESIG,
				values.get(0).toString());

	}

	@Test
	public void testNegativeNumber() throws Exception {
		AdvancingSource source =
				this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME, SequenceExpressionTest.SEPARATOR, null,
						SequenceExpressionTest.KEY, null, true);
		source.setPlantsSelected(-2);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		final String resultDesig = SequenceExpressionTest.GERMPLASM_NAME + SequenceExpressionTest.SEPARATOR;
		Assert.assertEquals("The value after applying the process codes should be equal to " + resultDesig, resultDesig,
				values.get(0).toString());
	}

	@Test
	public void testCaseSensitive() throws Exception {
		AdvancingSource source =
				this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME, SequenceExpressionTest.SEPARATOR, null,
						SequenceExpressionTest.KEY, null, true);
		source.setPlantsSelected(5);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		Assert.assertEquals("The value after applying the process codes should be equal to " + RESULT_DESIG, RESULT_DESIG,
				values.get(0).toString());
	}

	@Test
	public void testWithStartCount() throws Exception {
		AdvancingSource source =
				this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME, SequenceExpressionTest.SEPARATOR, null,
						SequenceExpressionTest.KEY, null, true);
		source.setPlantsSelected(5);
		source.setCurrentMaxSequence(5);
		List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		Assert.assertEquals("The value after applying the process codes should be equal to " + RESULT_DESIG, RESULT_DESIG,
				values.get(0).toString());
	}

	@Test
	public void testNonBulkingSequenceGenerationWithNegativePlantsSelectedValue() {
		// final false refers to nonBulking
		final AdvancingSource source =
				this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME, SequenceExpressionTest.SEPARATOR,
						BREEDING_METHOD_PREFIX, SequenceExpressionTest.KEY, null, false);
		source.setPlantsSelected(-1);
		source.setCurrentMaxSequence(5);
		final List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		final String resultDesig = GERMPLASM_NAME + SequenceExpressionTest.SEPARATOR + SequenceExpressionTest.BREEDING_METHOD_PREFIX;
		Assert.assertEquals("The value after applying the process codes should be equal to " + resultDesig, resultDesig,
				values.get(0).toString());
	}

	@Test
	public void testNonBulkingSequenceGenerationWithNegativeCurrentMaxSequencedValue() {
		Mockito.when(keySequenceRegisterService.incrementAndGetNextSequence(BREEDING_METHOD_PREFIX)).thenReturn(NEXT_SEQ_NO_12);
		// final false refers to nonBulking
		final AdvancingSource source =
				this.createAdvancingSourceTestData(SequenceExpressionTest.GERMPLASM_NAME, SequenceExpressionTest.SEPARATOR,
						BREEDING_METHOD_PREFIX, SequenceExpressionTest.KEY, null, false);
		source.setPlantsSelected(1);
		source.setCurrentMaxSequence(-5);
		final List<StringBuilder> values = this.createInitialValues(source);
		Assert.assertFalse("The value before applying the process codes should not be equal to " + SequenceExpressionTest.GERMPLASM_NAME,
				SequenceExpressionTest.GERMPLASM_NAME.equals(values.get(0).toString()));

		this.expression.apply(values, source);

		final String resultDesig =
				SequenceExpressionTest.GERMPLASM_NAME + SequenceExpressionTest.SEPARATOR + SequenceExpressionTest.BREEDING_METHOD_PREFIX
						+ SequenceExpressionTest.NEXT_SEQ_NO_12;
		Assert.assertEquals("The value after applying the process codes should be equal to " + resultDesig, resultDesig,
				values.get(0).toString());
	}

	@Test
	public void testNonBulkingSequenceGenerationWithPositivePlantSelectedAndCurrentMaxSequence() {
		Mockito.when(keySequenceRegisterService.incrementAndGetNextSequence(BREEDING_METHOD_PREFIX))
				.thenReturn(NEXT_SEQ_NO_12, NEXT_SEQ_NO_13, NEXT_SEQ_NO_14, NEXT_SEQ_NO_15, NEXT_SEQ_NO_16);

		// final false refers to nonBulking
		AdvancingSource source =
				this.createAdvancingSourceTestData(GERMPLASM_NAME, SequenceExpressionTest.SEPARATOR, BREEDING_METHOD_PREFIX,
						SequenceExpressionTest.KEY, null, false);
		source.setPlantsSelected(5);
		source.setCurrentMaxSequence(5);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source);

		HashSet<StringBuilder> uniqueNames = Sets.newHashSet(values);

		Assert.assertEquals(5, uniqueNames.size());

		final String resultDesig =
				SequenceExpressionTest.GERMPLASM_NAME + SequenceExpressionTest.SEPARATOR + SequenceExpressionTest.BREEDING_METHOD_PREFIX
						+ SequenceExpressionTest.NEXT_SEQ_NO_16;
		Assert.assertEquals("The value after applying the process codes should be equal to " + resultDesig, resultDesig,
				values.get(4).toString());

	}
}
