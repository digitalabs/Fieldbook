package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.junit.Test;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class TestFirstExpression extends TestExpression {

	@Test
	public void testFirst() throws Exception {
		FirstExpression expression = new FirstExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST-12B:13C-14D:15E", 
				"[FIRST]:", "12C", null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testFirst2() throws Exception {
		FirstExpression expression = new FirstExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				"[FIRST]:", "12C", null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testCaseSensitive() throws Exception {
		FirstExpression expression = new FirstExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST-12B:13C-14D:15E", 
				"[first]:", "12C", null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		System.out.println("process is in lower case");
		printResult(values, source);
	}

}
