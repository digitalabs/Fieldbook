package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 4/15/2015
 * Time: 1:34 PM
 */
public class PoundGroupCountExpressionTest extends TestExpression {
	PoundGroupCountExpression dut = new PoundGroupCountExpression();

	@Test
	public void testNoPoundInName() {
		AdvancingSource source = createAdvancingSourceTestData("CML451 / ABC1234", "-", null, null,
				null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + PoundGroupCountExpression.KEY));

		dut.apply(values, source);
		String value = values.get(0).toString();

		assertEquals("CML451 / ABC1234-#", value);
	}

	@Test
	public void testPoundInName() {
		AdvancingSource source = createAdvancingSourceTestData("CML451 / ABC1234-#-#-#-#", "-",
				null, null,
				null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + PoundGroupCountExpression.KEY));

		dut.apply(values, source);
		String value = values.get(0).toString();

		assertEquals("CML451 / ABC1234-#*4", value);
	}
}