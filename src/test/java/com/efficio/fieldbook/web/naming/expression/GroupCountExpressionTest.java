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
        testCountExpression("CML451 / ABC1234-B-B-B-B", "B*[COUNT]", "CML451 / ABC1234-B*5");
	}

	@Test
	public void testPoundCountNothingToCount() {
        testCountExpression("CML451 / ABC1234", "#*[COUNT]", "CML451 / ABC1234-#");
	}

	@Test
	public void testPoundCountWithItems() {
        testCountExpression("CML451 / ABC1234-#-#-#", "#*[COUNT]", "CML451 / ABC1234-#*4");
	}

    @Test
    public void testCountTwoItemsGroup() {
        testCountExpression("CML451 / ABC1234-#-#", "#*[COUNT]", "CML451 / ABC1234-#*3");
    }

    @Test
    public void testGroupCounting() {
        GroupCountExpression.CountResultBean bean = this.dut.countContinuousExpressionOccurrence("-B","WM14AST0001L-B-3-1-1-2-B*3");
        Assert.assertEquals(3, bean.getCount());
    }

    @Test
    public void testAggregateGroupCountingFirst() {
        GroupCountExpression.CountResultBean bean = this.dut.countContinuousExpressionOccurrence("-B","WM14AST0001L-B-3-1-1-2-B-B*3");
        Assert.assertEquals(4, bean.getCount());
    }

    @Test
    public void testAggregateGroupCountingLast() {
        GroupCountExpression.CountResultBean bean = this.dut.countContinuousExpressionOccurrence("-B","WM14AST0001L-B-3-1-1-2-B*3-B");
        Assert.assertEquals(4, bean.getCount());
    }

    protected void testCountExpression(String sourceName, String countExpression, String expectedValue) {
        AdvancingSource source = this.createAdvancingSourceTestData(sourceName, "-", null, null, null, true);
        List<StringBuilder> values = new ArrayList<>();
        values.add(new StringBuilder(source.getRootName() + countExpression));

        this.dut.apply(values, source);
        String value = values.get(0).toString();

        Assert.assertEquals(expectedValue, value);
    }
}