
package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.junit.Test;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class SeasonExpressionTest extends TestExpression {

	@Test
	public void testSeasonAsPrefix() throws Exception {
		SeasonExpression expression = new SeasonExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", null, "[SEASON]", null, null, true);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source);
		this.printResult(values, source);
	}

	@Test
	public void testSeasonAsSuffix() throws Exception {
		SeasonExpression expression = new SeasonExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", ":", null, null, "[SEASON]", true);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source);
		this.printResult(values, source);
	}

	@Test
	public void testNoSeason() throws Exception {
		SeasonExpression expression = new SeasonExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", null, null, null, "[SEASON]", true);
		source.setSeason(null);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source);
		this.printResult(values, source);
	}

	@Test
	public void testCaseSensitive() throws Exception {
		SeasonExpression expression = new SeasonExpression();
		AdvancingSource source = this.createAdvancingSourceTestData("GERMPLASM_TEST", null, "[seasOn]", null, null, true);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source);
		System.out.println("process code is in lower case");
		this.printResult(values, source);
	}

}
