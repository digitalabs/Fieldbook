package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
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

}
