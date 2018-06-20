
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.junit.Test;

import com.efficio.fieldbook.web.trial.bean.AdvancingSource;

public class NumberExpressionTest extends TestExpression {

	@Test
	public void testNumber() throws Exception {
		NumberExpression expression = new NumberExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[NUMBER]", null, true);
		source.setPlantsSelected(2);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, null);
		this.printResult(values, source);
	}

	@Test
	public void testNegativeNumber() throws Exception {
		NumberExpression expression = new NumberExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[NUMBER]", null, true);
		source.setPlantsSelected(-2);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, null);
		this.printResult(values, source);
	}

	@Test
	public void testCaseSensitive() throws Exception {
		NumberExpression expression = new NumberExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", "-", null, "[number]", null, true);
		source.setPlantsSelected(2);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, null);
		System.out.println("process code is in lower case");
		this.printResult(values, source);
	}

}
