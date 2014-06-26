package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.junit.Test;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class TestBulkCountExpression extends TestExpression {

	@Test
	public void testNonBulkingSource() throws Exception {
		BulkCountExpression expression = new BulkCountExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				"[BCOUNT]", null, null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testBulkSourceAt1() throws Exception {
		BulkCountExpression expression = new BulkCountExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST-B", 
				"[BCOUNT]", null, null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testBulkSourceAt2() throws Exception {
		BulkCountExpression expression = new BulkCountExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST-2B", 
				"[BCOUNT]", null, null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testBulkSourceAtInvalidNumber() throws Exception {
		BulkCountExpression expression = new BulkCountExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST-11a22B", 
				"[BCOUNT]", null, null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testBulkSourceAtMultipleAdvanced() throws Exception {
		BulkCountExpression expression = new BulkCountExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST-B-4B-3B", 
				"[BCOUNT]", null, null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testCaseSensitive() throws Exception {
		BulkCountExpression expression = new BulkCountExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST-B-4B-3B", 
				"[bcount]", null, null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		System.out.println("process code in lower case");
		printResult(values, source);
	}
}
