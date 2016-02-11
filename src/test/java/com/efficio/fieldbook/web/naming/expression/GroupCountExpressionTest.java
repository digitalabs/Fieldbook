
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
        testCountExpression("CML451 / ABC1234", "B*[COUNT]", "CML451 / ABC1234-B");
	}

	@Test
	public void testBulkingInName() {
        testCountExpression("CML451 / ABC1234-B-B-B-B", "B*[COUNT]", "CML451 / ABC1234-B*4");
	}

	@Test
	public void testPoundCountNothingToCount() {
        testCountExpression("CML451 / ABC1234", "#*[COUNT]", "CML451 / ABC1234-#");
	}

	@Test
	public void testPoundCountWithItems() {
        testCountExpression("CML451 / ABC1234-#-#-#", "#*[COUNT]", "CML451 / ABC1234-#*3");
	}

    @Test
    public void testCountTwoItemsGroup() {
        testCountExpression("CML451 / ABC1234-#-#", "#*[COUNT]", "CML451 / ABC1234-#*2");
    }

    protected void testCountExpression(String sourceName, String countExpression, String expectedValue) {
        AdvancingSource source = this.createAdvancingSourceTestData(sourceName, "-", null, null, null, false);
        List<StringBuilder> values = new ArrayList<>();
        values.add(new StringBuilder(source.getRootName() + countExpression));

        this.dut.apply(values, source);
        String value = values.get(0).toString();

        Assert.assertEquals(expectedValue, value);
    }
}
