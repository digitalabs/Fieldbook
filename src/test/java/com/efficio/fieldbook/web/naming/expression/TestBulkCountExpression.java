package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
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
				"GERMPLASM_TEST-B-2B-3B", 
				"[BCOUNT]", null, null, null, true);
		List<StringBuilder> values = createInitialValues(source);
		expression.apply(values, source);
		printResult(values, source);
	}

}
