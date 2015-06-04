
package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class GroupCountExpressionTest extends TestExpression {

	GroupCountExpression dut = new GroupCountExpression();

	@Test
	public void testNoBulkingInName() {
		AdvancingSource source = this.createAdvancingSourceTestData("CML451 / ABC1234", "-", null, null, null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + "B*[COUNT]"));

		this.dut.apply(values, source);
		String value = values.get(0).toString();

		Assert.assertEquals("CML451 / ABC1234-B", value);
	}

	@Test
	public void testBulkingInName() {
		AdvancingSource source = this.createAdvancingSourceTestData("CML451 / ABC1234-B-B-B-B", "-", null, null, null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + "B*[COUNT]"));

		this.dut.apply(values, source);
		String value = values.get(0).toString();

		Assert.assertEquals("CML451 / ABC1234-B*4", value);
	}

	@Test
	public void testPoundCountNothingToCount() {
		AdvancingSource source = this.createAdvancingSourceTestData("CML451 / ABC1234", "-", null, null, null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + "#*[COUNT]"));

		this.dut.apply(values, source);
		String value = values.get(0).toString();

		Assert.assertEquals("CML451 / ABC1234-#", value);
	}

	@Test
	public void testPoundCountWithItems() {
		AdvancingSource source = this.createAdvancingSourceTestData("CML451 / ABC1234-#-#-#", "-", null, null, null, false);
		List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(source.getRootName() + "#*[COUNT]"));

		this.dut.apply(values, source);
		String value = values.get(0).toString();

		Assert.assertEquals("CML451 / ABC1234-#*3", value);
	}
}
