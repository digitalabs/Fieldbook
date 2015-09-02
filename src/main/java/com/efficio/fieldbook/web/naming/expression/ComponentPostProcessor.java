package com.efficio.fieldbook.web.naming.expression;

import org.generationcp.commons.ruleengine.RulesPostProcessor;

import com.efficio.fieldbook.web.naming.impl.ProcessCodeFactory;


/**
 * Created by Daniel Villafuerte on 6/15/2015.
 */
public class ComponentPostProcessor extends RulesPostProcessor{

    private ProcessCodeFactory processCodeFactory;

    @Override
    public Object postProcessAfterInitialization(Object o, String s) {
        super.postProcessAfterInitialization(o, s);
        if (o instanceof Expression) {
            processCodeFactory.addExpression((Expression) o);
        }

        return o;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) {
        return super.postProcessBeforeInitialization(o, s);
    }

    public void setProcessCodeFactory(ProcessCodeFactory processCodeFactory) {
        this.processCodeFactory = processCodeFactory;
    }
}