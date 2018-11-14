package com.efficio.fieldbook.service.internal;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.impl.MockDesignRunnerImpl;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.bvdesign.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.bvdesign.BVDesignTrialInstance;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesignParameter;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import junit.framework.Assert;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class MockDesignRunnerImplTest {

	private final MockDesignRunnerImpl mockDesignRunner = new MockDesignRunnerImpl();

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private FieldbookProperties fieldbookProperties;

	private final ExperimentDesignGenerator experimentDesignGenerator = new ExperimentDesignGenerator();

	@Test
	public void testMockDesignRunnerRCBD() {

		final MainDesign mainDesign = this.experimentDesignGenerator
				.createRandomizedCompleteBlockDesign("2", "REP_NO", "PLOT_NO", 200, 100, TermId.ENTRY_NO.name(), Arrays.asList("ENTRY_NO"), Arrays.asList("20"),
						"mock-bv-out.csv");
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters().add(new ExpDesignParameter(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM, "2"));
		
		try {
			final BVDesignOutput output = this.mockDesignRunner.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertTrue(output.isSuccess());
			// Per instance: 20 entries, 2 reps, expecting 40 rows back.
			Assert.assertEquals(2, output.getTrialInstances().size());
			for (final BVDesignTrialInstance instance : output.getTrialInstances()) {				
				Assert.assertEquals(40, instance.getRows().size());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testMockDesignRunnerRIBD() {

		final MainDesign mainDesign = this.experimentDesignGenerator
				.createResolvableIncompleteBlockDesign("2", "20", "2", "ENTRY_NO", "REP_NO", "BLOCK_NO", "PLOT_NO", 10, 5, "", "",
						"mock-bv-out.csv", false);
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters().add(new ExpDesignParameter(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM, "3"));
				
		try {
			final BVDesignOutput output = this.mockDesignRunner.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertTrue(output.isSuccess());
			// Per instance: 20 entries, 2 reps, expecting 40 rows back.
			Assert.assertEquals(3, output.getTrialInstances().size());
			for (final BVDesignTrialInstance instance : output.getTrialInstances()) {				
				Assert.assertEquals(40, instance.getRows().size());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testMockDesignRunnerRRCD() {

		final MainDesign mainDesign = this.experimentDesignGenerator
				.createResolvableRowColDesign("20", "2", "2", "10", "ENTRY_NO", "REP_NO", "ROW", "COL", "PLOT_NO", 10, 2, "", "", "",
						"mock-bv-out.csv", false);
		// Configure number of instances to be generated
		mainDesign.getDesign().getParameters().add(new ExpDesignParameter(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM, "5"));
				
		try {
			final BVDesignOutput output = this.mockDesignRunner.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertTrue(output.isSuccess());
			// Per instance: 20 entries, 2 reps, expecting 40 rows back.
			Assert.assertEquals(5, output.getTrialInstances().size());
			for (final BVDesignTrialInstance instance : output.getTrialInstances()) {				
				Assert.assertEquals(40, instance.getRows().size());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}
