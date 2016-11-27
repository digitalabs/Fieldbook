package com.efficio.fieldbook.service.internal;

import com.efficio.fieldbook.service.internal.impl.BVDesignRunner;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BVDesignRunnerTest {

	private final ExperimentDesignGenerator experimentDesignGenerator = new ExperimentDesignGenerator();

	@Test
	public void testGetXMLStringForRandomizedCompleteBlockDesign() {
		List<String> treatmentFactor = new ArrayList<>();
		treatmentFactor.add("ENTRY_NO");
		treatmentFactor.add("FERTILIZER");

		List<String> levels = new ArrayList<>();
		levels.add("24");
		levels.add("3");

		MainDesign mainDesign =
				experimentDesignGenerator.createRandomizedCompleteBlockDesign("6", "Reps", "Plots", 301, 201, treatmentFactor, levels, "");

		String expectedString =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"RandomizedBlock\">"
						+ "<Parameter name=\"" + ExperimentDesignGenerator.SEED_PARAM + "\" value=\":seedValue\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NBLOCKS_PARAM + "\" value=\"6\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.BLOCKFACTOR_PARAM + "\" value=\"Reps\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.PLOTFACTOR_PARAM + "\" value=\"Plots\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM + "\" value=\"301\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM + "\" value=\"201\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.TREATMENTFACTORS_PARAM
						+ "\"><ListItem value=\"ENTRY_NO\"/><ListItem value=\"FERTILIZER\"/></Parameter>"
						+ "<Parameter name=\"levels\"><ListItem value=\"24\"/><ListItem value=\"3\"/></Parameter>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.TIMELIMIT_PARAM + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
						+ "\"/>" + "<Parameter name=\"" + ExperimentDesignGenerator.OUTPUTFILE_PARAM
						+ "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableIncompleteBlockDesign() {
		MainDesign mainDesign = experimentDesignGenerator
				.createResolvableIncompleteBlockDesign("6", "24", "2", "Treat", "Reps", "Subblocks", "Plots", 301, null, "0", "", "",
						false);

		String expectedString =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableIncompleteBlock\">"
						+ "<Parameter name=\"" + ExperimentDesignGenerator.SEED_PARAM + "\" value=\":seedValue\"/><Parameter name=\""
						+ ExperimentDesignGenerator.BLOCKSIZE_PARAM + "\" value=\"6\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NTREATMENTS_PARAM + "\" value=\"24\"/><Parameter name=\""
						+ ExperimentDesignGenerator.NREPLICATES_PARAM + "\" value=\"2\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.TREATMENTFACTOR_PARAM + "\" value=\"Treat\"/><Parameter name=\""
						+ ExperimentDesignGenerator.REPLICATEFACTOR_PARAM + "\" value=\"Reps\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.BLOCKFACTOR_PARAM + "\" value=\"Subblocks\"/><Parameter name=\""
						+ ExperimentDesignGenerator.PLOTFACTOR_PARAM + "\" value=\"Plots\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM + "\" value=\"301\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NBLATIN_PARAM + "\" value=\"0\"/><Parameter name=\""
						+ ExperimentDesignGenerator.TIMELIMIT_PARAM + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
						+ "\"/>" + "<Parameter name=\"" + ExperimentDesignGenerator.OUTPUTFILE_PARAM
						+ "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableRowColExpDesign() {
		MainDesign mainDesign = experimentDesignGenerator
				.createResolvableRowColDesign("50", "2", "5", "10", "Treat", "Reps", "Rows", "Columns", "Plots", 301, null, "0", "0", "",
						"", false);

		String expectedString =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableRowColumn\">"
						+ "<Parameter name=\"" + ExperimentDesignGenerator.SEED_PARAM + "\" value=\":seedValue\"/><Parameter name=\""
						+ ExperimentDesignGenerator.NTREATMENTS_PARAM + "\" value=\"50\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NREPLICATES_PARAM + "\" value=\"2\"/><Parameter name=\""
						+ ExperimentDesignGenerator.NROWS_PARAM + "\" value=\"5\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NCOLUMNS_PARAM + "\" value=\"10\"/><Parameter name=\""
						+ ExperimentDesignGenerator.TREATMENTFACTOR_PARAM + "\" value=\"Treat\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.REPLICATEFACTOR_PARAM + "\" value=\"Reps\"/><Parameter name=\""
						+ ExperimentDesignGenerator.ROWFACTOR_PARAM + "\" value=\"Rows\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.COLUMNFACTOR_PARAM + "\" value=\"Columns\"/><Parameter name=\""
						+ ExperimentDesignGenerator.PLOTFACTOR_PARAM + "\" value=\"Plots\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM + "\" value=\"301\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NRLATIN_PARAM + "\" value=\"0\"/><Parameter name=\""
						+ ExperimentDesignGenerator.NCLATIN_PARAM + "\" value=\"0\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.TIMELIMIT_PARAM + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
						+ "\"/>" + "<Parameter name=\"" + ExperimentDesignGenerator.OUTPUTFILE_PARAM
						+ "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableIncompleteBlockDesignWithEntryNumber() {
		MainDesign mainDesign = experimentDesignGenerator
				.createResolvableIncompleteBlockDesign("6", "24", "2", "ENTRY_NO", "Reps", "Subblocks", "Plots", 301, 245, "0", "", "",
						false);

		String expectedString =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableIncompleteBlock\">"
						+ "<Parameter name=\"" + ExperimentDesignGenerator.SEED_PARAM + "\" value=\":seedValue\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.BLOCKSIZE_PARAM + "\" value=\"6\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NTREATMENTS_PARAM + "\" value=\"24\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NREPLICATES_PARAM + "\" value=\"2\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.TREATMENTFACTOR_PARAM + "\" value=\"ENTRY_NO\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM + "\" value=\"245\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.REPLICATEFACTOR_PARAM + "\" value=\"Reps\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.BLOCKFACTOR_PARAM + "\" value=\"Subblocks\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.PLOTFACTOR_PARAM + "\" value=\"Plots\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM + "\" value=\"301\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NBLATIN_PARAM + "\" value=\"0\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.TIMELIMIT_PARAM + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
						+ "\"/>" + "<Parameter name=\"" + ExperimentDesignGenerator.OUTPUTFILE_PARAM
						+ "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableRowColumnDesignWithEntryNumber() {
		MainDesign mainDesign = experimentDesignGenerator
				.createResolvableRowColDesign("24", "2", "5", "10", "ENTRY_NO", "Reps", "Rows", "Columns", "Plots", 301, 245, "0", "0", "",
						"", false);

		String expectedString =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableRowColumn\">"
						+ "<Parameter name=\"" + ExperimentDesignGenerator.SEED_PARAM + "\" value=\":seedValue\"/><Parameter name=\""
						+ ExperimentDesignGenerator.NTREATMENTS_PARAM + "\" value=\"24\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NREPLICATES_PARAM + "\" value=\"2\"/><Parameter name=\""
						+ ExperimentDesignGenerator.NROWS_PARAM + "\" value=\"5\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NCOLUMNS_PARAM + "\" value=\"10\"/><Parameter name=\""
						+ ExperimentDesignGenerator.TREATMENTFACTOR_PARAM + "\" value=\"ENTRY_NO\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM + "\" value=\"245\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.REPLICATEFACTOR_PARAM + "\" value=\"Reps\"/><Parameter name=\""
						+ ExperimentDesignGenerator.ROWFACTOR_PARAM + "\" value=\"Rows\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.COLUMNFACTOR_PARAM + "\" value=\"Columns\"/><Parameter name=\""
						+ ExperimentDesignGenerator.PLOTFACTOR_PARAM + "\" value=\"Plots\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM + "\" value=\"301\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NRLATIN_PARAM + "\" value=\"0\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.NCLATIN_PARAM + "\" value=\"0\"/>" + "<Parameter name=\""
						+ ExperimentDesignGenerator.TIMELIMIT_PARAM + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
						+ "\"/>" + "<Parameter name=\"" + ExperimentDesignGenerator.OUTPUTFILE_PARAM
						+ "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);
		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	private void assertXMLStringEqualsExpected(MainDesign mainDesign, String expectedString, String xmlString) {
		String outputFile = mainDesign.getDesign().getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM);
		String outputFileMillisecs = outputFile.replace(BVDesignRunner.BV_PREFIX + BVDesignRunner.CSV_EXTENSION, "");
		String seedValue = this.getSeedValue(outputFileMillisecs);
		expectedString = expectedString.replace(":seedValue", seedValue);
		expectedString = expectedString.replace(":outputFile", outputFile);

		Assert.assertEquals(expectedString, xmlString);
	}

	private String getSeedValue(String currentTimeMillis) {
		String seedValue = currentTimeMillis;
		if (Long.parseLong(currentTimeMillis) > Integer.MAX_VALUE) {
			seedValue = seedValue.substring(seedValue.length() - 9);
		}
		return seedValue;
	}
}
