
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.efficio.fieldbook.web.trial.bean.AdvancingSource;

public class SequenceExpressionTest extends TestExpression {

	@Test
	public void testSequence() throws Exception {
		SequenceExpression expression = new SequenceExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[SEQUENCE]", null, true);
		source.setPlantsSelected(5);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, null);
		this.printResult(values, source);
	}

	@Test
	public void testNegativeNumber() throws Exception {
		SequenceExpression expression = new SequenceExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[SEQUENCE]", null, true);
		source.setPlantsSelected(-2);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, null);
		this.printResult(values, source);
	}

	@Test
	public void testCaseSensitive() throws Exception {
		SequenceExpression expression = new SequenceExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[sequence]", null, true);
		source.setPlantsSelected(5);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, null);
		System.out.println("process code is in lower case");
		this.printResult(values, source);
	}

	@Test
	public void testWithStartCount() throws Exception {
		SequenceExpression expression = new SequenceExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[sequence]", null, true);
		source.setPlantsSelected(5);
		source.setCurrentMaxSequence(5);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, null);
		System.out.println("process code is in lower case");
		this.printResult(values, source);
	}

	@Test
	public void testNonBulkingSequenceGeneration() {
		SequenceExpression expression = new SequenceExpression();
		// final false refers to nonBulking
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[SEQUENCE]", null, false);
		source.setPlantsSelected(5);
		source.setCurrentMaxSequence(5);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, null);
		Assert.assertEquals(5, values.size());
		this.printResult(values, source);
	}
}
