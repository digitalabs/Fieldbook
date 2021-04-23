package com.efficio.fieldbook.web.util;

import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class ExpDesignUtilTest {

    @Test
    public void testExperimentalDesignValueNull() {
        final ExperimentalDesignVariable experimentalDesignVariable = new ExperimentalDesignVariable(null);
        final MeasurementVariable measurementVariable = new MeasurementVariable();
        measurementVariable.setTermId(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
        measurementVariable.setValue(null);
        Assert.assertEquals("Null Parameter should return zero value",0, ExpDesignUtil.getExperimentalDesignValueFromExperimentalDesignVariable(null));
        Assert.assertEquals("Null Experiment Factor should return zero value", 0,  ExpDesignUtil.getExperimentalDesignValueFromExperimentalDesignVariable(experimentalDesignVariable));

        experimentalDesignVariable.setVariables(Collections.singletonList(measurementVariable));
        Assert.assertEquals("Null Experiment Factor value should return zero value", 0,  ExpDesignUtil.getExperimentalDesignValueFromExperimentalDesignVariable(experimentalDesignVariable));
    }

    @Test
    public void testExperimentalDesign() {
        final MeasurementVariable measurementVariable = new MeasurementVariable();
        measurementVariable.setTermId(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
        measurementVariable.setValue(String.valueOf(TermId.P_REP.getId()));
        final ExperimentalDesignVariable experimentalDesignVariable = new ExperimentalDesignVariable(Collections.singletonList(measurementVariable));
        Assert.assertEquals(TermId.P_REP.getId(), ExpDesignUtil.getExperimentalDesignValueFromExperimentalDesignVariable(experimentalDesignVariable));
    }
}
