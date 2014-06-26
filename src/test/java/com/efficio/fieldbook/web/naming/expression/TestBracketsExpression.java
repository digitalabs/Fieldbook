package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class TestBracketsExpression extends TestExpression {

	@Test
	public void testNameWithNoBrackets() throws Exception {
		BracketsExpression expression = new BracketsExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				"[BRACKETS]", null, null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testNameWithBrackets() throws Exception {
		BracketsExpression expression = new BracketsExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"(GERMPLASM_TEST)", 
				"[BRACKETS]", null, null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

	@Test
	public void testDoubleBrackets() throws Exception {
		BracketsExpression expression = new BracketsExpression();
		AdvancingSource source = createAdvancingSourceTestData(
				"GERMPLASM_TEST", 
				"[BRACKETS][BRACKETS]", null, null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		expression.apply(values, source);
		printResult(values, source);
	}

}
