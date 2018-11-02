package com.efficio.fieldbook.service.internal;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.impl.BVDesignRunner;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import junit.framework.Assert;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BVDesignRunnerTest {

	public static final String BV_DESIGN_EXECUTABLE_PATH = "bvDesignExecutablePath";

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private BVDesignRunner.BVDesignOutputReader outputReader;

	@Mock
	private BVDesignRunner.BVDesignProcessRunner processRunner;

	@Mock
	private BVDesignRunner.BVDesignXmlInputWriter inputWriter;

	private final ExperimentDesignGenerator experimentDesignGenerator = new ExperimentDesignGenerator();

	private BVDesignRunner bvDesignRunner;

	@Before
	public void init() {

		this.bvDesignRunner = new BVDesignRunner();
		this.bvDesignRunner.setOutputReader(this.outputReader);
		this.bvDesignRunner.setProcessRunner(this.processRunner);
		this.bvDesignRunner.setInputWriter(this.inputWriter);

		when(fieldbookProperties.getBvDesignPath()).thenReturn(BV_DESIGN_EXECUTABLE_PATH);
	}

	@Test
	public void testRunBVDesignSuccess() throws IOException {

		final String xmlInputFilePath = "xmlInputFilePath";
		final Integer successfulReturnCode = 0;

		final MainDesign mainDesign = createRandomizedCompleteBlockDesign();

		when(this.inputWriter.write(anyString(), eq(this.fieldbookProperties))).thenReturn(xmlInputFilePath);
		when(this.processRunner.run(BV_DESIGN_EXECUTABLE_PATH, "-i" + xmlInputFilePath)).thenReturn(successfulReturnCode);
		when(this.outputReader.read(anyString())).thenReturn(new ArrayList<String[]>());

		final BVDesignOutput bvDesignOutput = this.bvDesignRunner.runBVDesign(workbenchService, fieldbookProperties, mainDesign);

		verify(this.processRunner).run(BV_DESIGN_EXECUTABLE_PATH, "-i" + xmlInputFilePath);
		verify(this.outputReader).read(mainDesign.getDesign().getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM));
		assertTrue(bvDesignOutput.isSuccess());

	}

	@Test
	public void testRunBVDesignFail() throws IOException {

		final String xmlInputFilePath = "xmlInputFilePath";
		final Integer failureReturnCode = -1;

		final MainDesign mainDesign = createRandomizedCompleteBlockDesign();

		when(this.inputWriter.write(anyString(), eq(this.fieldbookProperties))).thenReturn(xmlInputFilePath);
		when(this.processRunner.run(BV_DESIGN_EXECUTABLE_PATH, "-i" + xmlInputFilePath)).thenReturn(failureReturnCode);

		final BVDesignOutput bvDesignOutput = this.bvDesignRunner.runBVDesign(workbenchService, fieldbookProperties, mainDesign);

		verify(this.processRunner).run(BV_DESIGN_EXECUTABLE_PATH, "-i" + xmlInputFilePath);
		verify(this.outputReader, never()).read(mainDesign.getDesign().getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM));
		assertFalse(bvDesignOutput.isSuccess());

	}

	@Test
	public void testGetXMLStringForRandomizedCompleteBlockDesign() {

		final MainDesign mainDesign = createRandomizedCompleteBlockDesign();

		final String expectedString =
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

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableIncompleteBlockDesign() {
		final MainDesign mainDesign = experimentDesignGenerator
				.createResolvableIncompleteBlockDesign("6", "24", "2", "Treat", "Reps", "Subblocks", "Plots", 301, null, "0", "", "",
						false);

		final String expectedString =
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

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableRowColExpDesign() {
		final MainDesign mainDesign = experimentDesignGenerator
				.createResolvableRowColDesign("50", "2", "5", "10", "Treat", "Reps", "Rows", "Columns", "Plots", 301, null, "0", "0", "",
						"", false);

		final String expectedString =
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

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableIncompleteBlockDesignWithEntryNumber() {
		final MainDesign mainDesign = experimentDesignGenerator
				.createResolvableIncompleteBlockDesign("6", "24", "2", "ENTRY_NO", "Reps", "Subblocks", "Plots", 301, 245, "0", "", "",
						false);

		final String expectedString =
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

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableRowColumnDesignWithEntryNumber() {
		final MainDesign mainDesign = experimentDesignGenerator
				.createResolvableRowColDesign("24", "2", "5", "10", "ENTRY_NO", "Reps", "Rows", "Columns", "Plots", 301, 245, "0", "0", "",
						"", false);

		final String expectedString =
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

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);
		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	private void assertXMLStringEqualsExpected(final MainDesign mainDesign, String expectedString, final String xmlString) {
		final String outputFile = mainDesign.getDesign().getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM);
		final String outputFileMillisecs = outputFile.replace(BVDesignRunner.BV_PREFIX + BVDesignRunner.CSV_EXTENSION, "");
		final String seedValue = this.getSeedValue(outputFileMillisecs);
		expectedString = expectedString.replace(":seedValue", seedValue);
		expectedString = expectedString.replace(":outputFile", outputFile);

		Assert.assertEquals(expectedString, xmlString);
	}

	private String getSeedValue(final String currentTimeMillis) {
		String seedValue = currentTimeMillis;
		if (Long.parseLong(currentTimeMillis) > Integer.MAX_VALUE) {
			seedValue = seedValue.substring(seedValue.length() - 9);
		}
		return seedValue;
	}

	private MainDesign createRandomizedCompleteBlockDesign() {

		final List<String> treatmentFactors = new ArrayList<>();
		treatmentFactors.add("ENTRY_NO");
		treatmentFactors.add("FERTILIZER");

		final List<String> levels = new ArrayList<>();
		levels.add("24");
		levels.add("3");

		return experimentDesignGenerator.createRandomizedCompleteBlockDesign("6", "Reps", "Plots", 301, 201, TermId.ENTRY_NO.name(), treatmentFactors, levels, "");
	}
}
