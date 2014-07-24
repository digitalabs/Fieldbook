package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.junit.Test;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class TopLocationAbbreviationExpressionTest extends TestExpression {

	@Test
	public void testLabbrAsPrefix() throws Exception {
		TopLocationAbbreviationExpression expression = new TopLocationAbbreviationExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				null, "[TLABBR]", null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testLabbrAsSuffix() throws Exception {
		TopLocationAbbreviationExpression expression = new TopLocationAbbreviationExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				":", null, null, "[TLABBR]", true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testNoLabbr() throws Exception {
		TopLocationAbbreviationExpression expression = new TopLocationAbbreviationExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				null, null, null, "[TLABBR]", true);
		source.setLocationAbbreviation(null);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testCaseSensitive() throws Exception {
		TopLocationAbbreviationExpression expression = new TopLocationAbbreviationExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				null, "[tLabbr]", null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		System.out.println("process code is in lower case");
		printResult(values, source);
	}

}
