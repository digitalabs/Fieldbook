package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class BracketsExpressionTest extends TestExpression {

	BracketsExpression dut = new BracketsExpression();

	@Test
	public void testBracketsNonCross() {
		String testRootName = "CMLI452";
		AdvancingSource source = createAdvancingSourceTestData(testRootName, "-", null, null, null, false);
		source.setRootName(testRootName);
		List<StringBuilder> builders = new ArrayList<>();
		builders.add(new StringBuilder(source.getRootName() + BracketsExpression.KEY));

		dut.apply(builders, source);

		assertEquals(testRootName, builders.get(0).toString());
	}

	@Test
	public void testBracketsCross() {
		String testRootName = "CMLI452 X POSI105";
		AdvancingSource source = createAdvancingSourceTestData(testRootName, "-", null, null, null,
				false);
		source.setRootName(testRootName);
		source.setRootNameType(GermplasmNameType.CROSS_NAME.getUserDefinedFieldID());

		List<StringBuilder> builders = new ArrayList<>();
		builders.add(new StringBuilder(source.getRootName() + BracketsExpression.KEY));

		dut.apply(builders, source);

		assertEquals("(" + testRootName + ")", builders.get(0).toString());
	}

}
