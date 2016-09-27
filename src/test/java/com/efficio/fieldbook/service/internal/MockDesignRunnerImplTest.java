
package com.efficio.fieldbook.service.internal;

import java.util.Arrays;

import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.impl.MockDesignRunnerImpl;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

@RunWith(MockitoJUnitRunner.class)
public class MockDesignRunnerImplTest {

	private MockDesignRunnerImpl mockDesignRunner = new MockDesignRunnerImpl();

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private FieldbookProperties fieldbookProperties;

	private ExperimentDesignGenerator experimentDesignGenerator = new ExperimentDesignGenerator();

	@Test
	public void testMockDesignRunnerRCBD() {

		MainDesign mainDesign =
				experimentDesignGenerator.createRandomizedCompleteBlockDesign("2", "REP_NO", "PLOT_NO", 200, 100, Arrays.asList("ENTRY_NO"),
						Arrays.asList("20"), "mock-bv-out.csv");
		try {
			final BVDesignOutput output = this.mockDesignRunner.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertTrue(output.isSuccess());
			// 20 entries, 2 reps, expecting 40 rows back.
			Assert.assertEquals(40, output.getBvResultList().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testMockDesignRunnerRIBD() {

		MainDesign mainDesign =
				experimentDesignGenerator.createResolvableIncompleteBlockDesign("2", "20", "2", "ENTRY_NO", "REP_NO", "BLOCK_NO", "PLOT_NO", 10, 5, "",
						"", "mock-bv-out.csv", false);
		try {
			final BVDesignOutput output = this.mockDesignRunner.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertTrue(output.isSuccess());
			// 20 entries, 2 reps, expecting 40 rows back.
			Assert.assertEquals(40, output.getBvResultList().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testMockDesignRunnerRRCD() {

		MainDesign mainDesign =
				experimentDesignGenerator.createResolvableRowColDesign("20", "2", "2", "10", "ENTRY_NO", "REP_NO", "ROW", "COL", "PLOT_NO", 10, 2, "",
						"", "", "mock-bv-out.csv", false);
		try {
			final BVDesignOutput output = this.mockDesignRunner.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertTrue(output.isSuccess());
			// 20 entries, 2 reps, expecting 40 rows back.
			Assert.assertEquals(40, output.getBvResultList().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}
