package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public class BulkGroupCountExpressionTest extends TestExpression {

	BulkGroupCountExpression dut = new BulkGroupCountExpression();

	@Test
	public void testNoBulkingInName() {
		AdvancingSource source = createAdvancingSourceTestData("CML451 / ABC1234", "-", null, null,
				null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + BulkGroupCountExpression.KEY));

		dut.apply(values, source);
		String value = values.get(0).toString();

		assertEquals("CML451 / ABC1234-B", value);
	}

	@Test
	public void testBulkingInName() {
		AdvancingSource source = createAdvancingSourceTestData("CML451 / ABC1234-B-B-B-B", "-", null, null,
				null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + BulkGroupCountExpression.KEY));

		dut.apply(values, source);
		String value = values.get(0).toString();

		assertEquals("CML451 / ABC1234-B*4", value);
	}
}
