package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class TestTopLocationAbbreviationExpression extends TestExpression {

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

}
