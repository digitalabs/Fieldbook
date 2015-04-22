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
public class GroupCountExpressionTest extends TestExpression {

	GroupCountExpression dut = new GroupCountExpression();

	@Test
	public void testNoBulkingInName() {
		AdvancingSource source = createAdvancingSourceTestData("CML451 / ABC1234", "-", null, null,
				null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + "B*[COUNT]"));

		dut.apply(values, source);
		String value = values.get(0).toString();

		assertEquals("CML451 / ABC1234-B", value);
	}

	@Test
	public void testBulkingInName() {
		AdvancingSource source = createAdvancingSourceTestData("CML451 / ABC1234-B-B-B-B", "-", null, null,
				null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + "B*[COUNT]"));

		dut.apply(values, source);
		String value = values.get(0).toString();

		assertEquals("CML451 / ABC1234-B*4", value);
	}

	@Test
	public void testPoundCountNothingToCount() {
		AdvancingSource source = createAdvancingSourceTestData("CML451 / ABC1234", "-", null, null,
				null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + "#*[COUNT]"));

		dut.apply(values, source);
		String value = values.get(0).toString();

		assertEquals("CML451 / ABC1234-#", value);
	}

	@Test
	public void testPoundCountWithItems() {
		AdvancingSource source = createAdvancingSourceTestData("CML451 / ABC1234-#-#-#", "-", null, null,
				null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + "#*[COUNT]"));

		dut.apply(values, source);
		String value = values.get(0).toString();

		assertEquals("CML451 / ABC1234-#*3", value);
	}
}
