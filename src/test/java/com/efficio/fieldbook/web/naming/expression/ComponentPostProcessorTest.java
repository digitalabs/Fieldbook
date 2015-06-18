package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.naming.impl.ProcessCodeFactory;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Daniel Villafuerte on 6/16/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class ComponentPostProcessorTest {

    @Mock
    private RuleFactory ruleFactory;

    @Mock
    private ProcessCodeFactory processCodeFactory;

    @InjectMocks
    private ComponentPostProcessor dut;

    @Test
    public void testProcessCodeAdd() {
        Expression testExpression = new Expression() {
            @Override
            public void apply(List<StringBuilder> values, AdvancingSource source) {
                // do nothing
            }

            @Override
            public String getExpressionKey() {
                return "TEST";
            }
        };

        dut.postProcessAfterInitialization(testExpression, testExpression.getExpressionKey());
        verify(processCodeFactory).addExpression(testExpression);
    }
}
