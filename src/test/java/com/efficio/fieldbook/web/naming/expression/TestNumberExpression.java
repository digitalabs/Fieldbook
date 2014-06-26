package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.junit.Test;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class TestNumberExpression extends TestExpression {

	@Test
	public void testNumber() throws Exception {
		NumberExpression expression = new NumberExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				"-", null, "[NUMBER]", null, true);
		source.setPlantsSelected(2);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testNegativeNumber() throws Exception {
		NumberExpression expression = new NumberExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				"-", null, "[NUMBER]", null, true);
		source.setPlantsSelected(-2);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testCaseSensitive() throws Exception {
		NumberExpression expression = new NumberExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				"-", null, "[number]", null, true);
		source.setPlantsSelected(2);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		System.out.println("process code is in lower case");
		printResult(values, source);
	}

}
