
package com.efficio.fieldbook.web.trial.bean.bvdesign;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import junit.framework.Assert;

public class BVDesignOutputTest {

	private static final int NUM_OF_ENTRIES = 5;
	private static final int NUM_OF_REPS = 2;
	private static final int NUM_OF_TRIAL_INSTANCES = 3;
	private static final String TRIAL_NO = "TRIAL_NO";
	private static final String ENTRY_NO = "ENTRY_NO";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String REP_NO = "REP_NO";

	@Test
	public void testSetResults() {
		final BVDesignOutput bvOutput = new BVDesignOutput(0);
		bvOutput.setResults(BVDesignOutputTest.createEntries(BVDesignOutputTest.NUM_OF_TRIAL_INSTANCES, BVDesignOutputTest.NUM_OF_REPS,
				BVDesignOutputTest.NUM_OF_ENTRIES));
		Assert.assertTrue(bvOutput.isSuccess());
		Assert.assertEquals(BVDesignOutputTest.NUM_OF_TRIAL_INSTANCES, bvOutput.getTrialInstances().size());
		for (int i = 0; i < NUM_OF_TRIAL_INSTANCES; i++) {
			final BVDesignTrialInstance trialInstance = bvOutput.getTrialInstances().get(i);
			Assert.assertEquals(BVDesignOutputTest.NUM_OF_REPS * BVDesignOutputTest.NUM_OF_ENTRIES, trialInstance.getRows().size());
			Assert.assertEquals(i + 1, trialInstance.getInstanceNumber().intValue());
		}
	}

	public static List<String[]> createEntries(final Integer numOfInstances, final Integer numberofReps, final Integer numberOfEntries) {
		final List<String[]> entries = new ArrayList<String[]>();
		final String[] headers = new String[] {BVDesignOutputTest.TRIAL_NO, BVDesignOutputTest.PLOT_NO, BVDesignOutputTest.REP_NO,
				BVDesignOutputTest.ENTRY_NO};

		entries.add(headers);
		for (int i = 1; i <= numOfInstances; i++) {
			final String trial = String.valueOf(i);
			int plotNo = 100;
			for (int j = 1; j <= numberofReps; j++) {
				for (int k = 1; k <= numberOfEntries; k++) {
					final String plot = String.valueOf(plotNo++);
					final String[] data = new String[] {trial, plot, String.valueOf(j), String.valueOf(k)};
					entries.add(data);
				}
			}
		}
		return entries;
	}

}
