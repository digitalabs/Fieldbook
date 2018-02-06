package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


@RunWith(MockitoJUnitRunner.class)
public class AttributeSourceExpressionTest extends TestExpression {

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
	AttributeSourceExpression expression = new AttributeSourceExpression();

	@Test
	public void testAttributeAsPrefix() throws Exception {

		Mockito.when(germplasmDataManager.getAttributeValue(1000, "IBP_BMS")).thenReturn("AA");
		AdvancingSource source = this.createAdvancingSourceTestData("DER","GERMPLASM_TEST", null, "[ATTRSC.IBP_BMS]", "[SEQUENCE]", null, true);
		List<StringBuilder> values = this.createInitialValues(source);
		expression.apply(values, source, "[ATTRSC.IBP_BMS]");
		this.printResult(values, source);

		assertThat("GERMPLASM_TEST-AA",is(equalTo(values.get(0).toString())));
	}
}
