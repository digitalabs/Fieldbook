package com.efficio.fieldbook.web.experimentdesign;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.trial.bean.bvdesign.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.bvdesign.BVDesignOutputTest;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesignParameter;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.google.common.base.Optional;
import junit.framework.Assert;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentDesignGeneratorTest {

	public static final String BLOCK_NO = "BLOCK_NO";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String ENTRY_NO = "ENTRY_NO";
	public static final String REP_NO = "REP_NO";
	public static final String NBLOCK = "2";
	public static final String OUTPUT_FILE = "outputfile.csv";

	private static final int ENTRY_NO_9 = 9;
	private static final int ENTRY_NO_3= 3;
	private static final int ENTRY_NO_10 = 10;
	private static final int ENTRY_NO_5 = 5;

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private FieldbookService fieldbookService;

	@InjectMocks
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Test
	public void testCreateRandomizedCompleteBlockDesign() {

		final List<String> treatmentFactors = new ArrayList<>(Arrays.asList("FACTOR_1", "FACTOR_2"));
		final List<String> levels = new ArrayList<>(Arrays.asList("Level1", "Level2"));
		final Integer initialPlotNumber = 99;
		final Integer initialEntryNumber = 100;

		final MainDesign mainDesign = this.experimentDesignGenerator
				.createRandomizedCompleteBlockDesign(NBLOCK, BLOCK_NO, PLOT_NO, initialPlotNumber, initialEntryNumber, TermId.ENTRY_NO.name(),treatmentFactors,
						levels, OUTPUT_FILE);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN, expDesign.getName());
		Assert.assertEquals("", expDesign.getParameterValue(ExperimentDesignGenerator.SEED_PARAM));
		Assert.assertEquals(NBLOCK, expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(String.valueOf(initialPlotNumber),
				expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));
		Assert.assertEquals(treatmentFactors.size(), expDesign.getParameterList(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM).size());
		Assert.assertEquals(treatmentFactors.size(), expDesign.getParameterList(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM).size());
		Assert.assertEquals(levels.size(), expDesign.getParameterList(ExperimentDesignGenerator.LEVELS_PARAM).size());
		Assert.assertEquals(AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
				expDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
		Assert.assertEquals(OUTPUT_FILE, expDesign.getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM));

	}

	@Test
	public void testCreateResolvableIncompleteBlockDesign() {

		final String numberOfTreatments = "30";
		final String numberOfReplicates = "31";
		final String blockSize = "22";
		final Integer initialPlotNumber = 99;
		final Integer initialEntryNumber = 100;
		final String nBLatin = "";
		final String replatinGroups = "sample1,sample2";

		final MainDesign mainDesign = this.experimentDesignGenerator
				.createResolvableIncompleteBlockDesign(blockSize, numberOfTreatments, numberOfReplicates, ENTRY_NO, REP_NO, BLOCK_NO,
						PLOT_NO, initialPlotNumber, initialEntryNumber, nBLatin, replatinGroups, OUTPUT_FILE, false);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, expDesign.getName());
		Assert.assertEquals("", expDesign.getParameterValue(ExperimentDesignGenerator.SEED_PARAM));
		Assert.assertEquals(blockSize, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKSIZE_PARAM));
		Assert.assertEquals(numberOfTreatments, expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(numberOfReplicates, expDesign.getParameterValue(ExperimentDesignGenerator.NREPLICATES_PARAM));
		Assert.assertEquals(String.valueOf(initialEntryNumber),
				expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(REP_NO, expDesign.getParameterValue(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(String.valueOf(initialPlotNumber),
				expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));

		Assert.assertEquals("0", expDesign.getParameterValue(ExperimentDesignGenerator.NBLATIN_PARAM));

		Assert.assertEquals(AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
				expDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
		Assert.assertEquals(OUTPUT_FILE, expDesign.getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM));

	}

	@Test
	public void testCreateResolvableIncompleteBlockDesignLatinized() {

		final String numberOfTreatments = "30";
		final String numberOfReplicates = "31";
		final String blockSize = "22";
		final Integer initialPlotNumber = 99;
		final Integer initialEntryNumber = 100;
		final String nBLatin = "";
		final String replatinGroups = "sample1,sample2";

		final MainDesign mainDesign = this.experimentDesignGenerator
				.createResolvableIncompleteBlockDesign(blockSize, numberOfTreatments, numberOfReplicates, ENTRY_NO, REP_NO, BLOCK_NO,
						PLOT_NO, initialPlotNumber, initialEntryNumber, nBLatin, replatinGroups, OUTPUT_FILE, true);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, expDesign.getName());
		Assert.assertEquals("", expDesign.getParameterValue(ExperimentDesignGenerator.SEED_PARAM));
		Assert.assertEquals(blockSize, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKSIZE_PARAM));
		Assert.assertEquals(numberOfTreatments, expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(numberOfReplicates, expDesign.getParameterValue(ExperimentDesignGenerator.NREPLICATES_PARAM));
		Assert.assertEquals(String.valueOf(initialEntryNumber),
				expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(REP_NO, expDesign.getParameterValue(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(String.valueOf(initialPlotNumber),
				expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));

		// Latinized Parameters
		Assert.assertEquals(nBLatin, expDesign.getParameterValue(ExperimentDesignGenerator.NBLATIN_PARAM));
		Assert.assertEquals(2, expDesign.getParameterList(ExperimentDesignGenerator.REPLATINGROUPS_PARAM).size());

		Assert.assertEquals(AppConstants.EXP_DESIGN_TIME_LIMIT.getString(),
				expDesign.getParameterValue(ExperimentDesignGenerator.TIMELIMIT_PARAM));
		Assert.assertEquals(OUTPUT_FILE, expDesign.getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM));

	}

	@Test
	public void testCeateAugmentedRandomizedBlockDesign() {

		final Integer numberOfBlocks = 2;
		final Integer numberOfTreatments = 22;
		final Integer numberOfControls = 11;
		final Integer startingPlotNumber = 1;
		final Integer startingEntryNumber = 2;

		final MainDesign mainDesign = this.experimentDesignGenerator
				.createAugmentedRandomizedBlockDesign(numberOfBlocks, numberOfTreatments, numberOfControls, startingPlotNumber, startingEntryNumber, ENTRY_NO, BLOCK_NO, PLOT_NO);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.AUGMENTED_RANDOMIZED_BLOCK_DESIGN, expDesign.getName());
		Assert.assertEquals(String.valueOf(numberOfTreatments), expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfControls), expDesign.getParameterValue(ExperimentDesignGenerator.NCONTROLS_PARAM));
		Assert.assertEquals(String.valueOf(numberOfBlocks), expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		Assert.assertEquals(ENTRY_NO, expDesign.getParameterValue(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(String.valueOf(startingEntryNumber), expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(String.valueOf(startingPlotNumber), expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));
	}

	@Test
	public void testAddInitialTreatmenNumberIfAvailableInitialEntryNumberIsNull() {

		final List<ExpDesignParameter> paramList = new ArrayList<>();

		final Integer initialEntryNumber = null;

		this.experimentDesignGenerator.addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		Assert.assertEquals("Initial Treatment Number param should not be added to the param list.", 0, paramList.size());

	}

	@Test
	public void testAddInitialTreatmenNumberIfAvailableInitialEntryNumberHasValue() {

		final List<ExpDesignParameter> paramList = new ArrayList<>();

		final Integer initialEntryNumber = 2;

		this.experimentDesignGenerator.addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		Assert.assertEquals("Initial Treatment Number param should  be added to the param list.", 1, paramList.size());
		Assert.assertEquals(String.valueOf(initialEntryNumber), paramList.get(0).getValue());

	}

	@Test
	public void testConvertToListItemList() {

		final List<String> listOfString = new LinkedList<String>();

		final String sampleText1 = "sample text 1";
		final String sampleText2 = "sample text 2";

		listOfString.add(sampleText1);
		listOfString.add(sampleText2);

		final List<ListItem> listItems = this.experimentDesignGenerator.convertToListItemList(listOfString);

		Assert.assertEquals(2, listItems.size());
		Assert.assertEquals(sampleText1, listItems.get(0).getValue());
		Assert.assertEquals(sampleText2, listItems.get(1).getValue());

	}

	@Test
	public void testGetPlotNumberStringValue() {

		Assert.assertEquals("If the initialPlotNumber is null, it should return the default plot number which is '1'.", "1",
			this.experimentDesignGenerator.getPlotNumberStringValueOrDefault(null));
		Assert.assertEquals("99", this.experimentDesignGenerator.getPlotNumberStringValueOrDefault(99));
	}

	@Test
	public void testFindImportedGermplasmByEntryNumberAndChecksEntryNumberExistsInImportedGermplasmMap() {

		final Map<Integer, ImportedGermplasm> importedGermplasmMap = this.createImportedGermplasmMap();

		final Map<Integer, Integer> designExpectedEntriesMap = new HashMap<>();
		// Test Entry No 9 is mapped to Check Entry No 3
		designExpectedEntriesMap.put(ENTRY_NO_9, ENTRY_NO_3);
		// Test Entry No 10 is mapped to Check Entry No 5
		designExpectedEntriesMap.put(ENTRY_NO_10, ENTRY_NO_5);


		final Optional<ImportedGermplasm> optionalImportedGermplasm = this.experimentDesignGenerator
				.findImportedGermplasmByEntryNumberAndChecks(importedGermplasmMap, 1, designExpectedEntriesMap);

		Assert.assertTrue(optionalImportedGermplasm.isPresent());
		Assert.assertEquals(Integer.valueOf(1), optionalImportedGermplasm.get().getEntryId());

	}

	@Test
	public void testFindImportedGermplasmByEntryNumberAndChecksEntryNumberDoesNotExistInImportedGermplasmMap() {

		final Map<Integer, ImportedGermplasm> importedGermplasmMap = this.createImportedGermplasmMap();

		final Map<Integer, Integer> designExpectedEntriesMap = new HashMap<>();
		// Test Entry No 9 is mapped to Check Entry No 3
		designExpectedEntriesMap.put(ENTRY_NO_9, ENTRY_NO_3);
		// Test Entry No 10 is mapped to Check Entry No 5
		designExpectedEntriesMap.put(ENTRY_NO_10, ENTRY_NO_5);

		final Optional<ImportedGermplasm> optionalImportedGermplasm = this.experimentDesignGenerator
				.findImportedGermplasmByEntryNumberAndChecks(importedGermplasmMap, 9999, designExpectedEntriesMap);

		Assert.assertFalse(optionalImportedGermplasm.isPresent());

	}

	@Test
	public void testGetInitialTreatNumList() {
		final List<String> treatmentFactors = Arrays.asList(TermId.ENTRY_NO.name(), "NFERT_NO");
		final List<ListItem> listItems = this.experimentDesignGenerator.getInitialTreatNumList(treatmentFactors, 5, TermId.ENTRY_NO.name());
		Assert.assertEquals("5", listItems.get(0).getValue());
		Assert.assertEquals("1", listItems.get(1).getValue());
	}
	
	@Test
	public void testResolveMappedEntryNumber() {

		final Map<Integer, Integer> designExpectedEntriesMap = new HashMap<>();
		// Test Entry No 9 is mapped to Check Entry No 3
		designExpectedEntriesMap.put(ENTRY_NO_9, ENTRY_NO_3);
		// Test Entry No 10 is mapped to Check Entry No 5
		designExpectedEntriesMap.put(ENTRY_NO_10, ENTRY_NO_5);

		final Integer result1 =
			this.experimentDesignGenerator.resolveMappedEntryNumber(9, designExpectedEntriesMap);
		Assert.assertEquals("Lookup value 9 should return 3", Integer.valueOf(3), result1);

		final Integer result2 =
			this.experimentDesignGenerator.resolveMappedEntryNumber(10, designExpectedEntriesMap);
		Assert.assertEquals("Lookup value 10 should return 5", Integer.valueOf(5), result2);

		final Integer result5 =
			this.experimentDesignGenerator.resolveMappedEntryNumber(9999, designExpectedEntriesMap);
		Assert.assertEquals("9999 is not in map of checks, the return value should be the same number", Integer.valueOf(9999), result5);
	}
	
	@Test
	public void testGenerateExperimentDesignMeasurements() throws IOException, BVDesignException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyTypeDto.getTrialDto());
		final MainDesign mainDesign =
			this.experimentDesignGenerator.createRandomizedCompleteBlockDesign("2", ExperimentDesignGeneratorTest.REP_NO, ExperimentDesignGeneratorTest.PLOT_NO,
						301, 201, TermId.ENTRY_NO.name(), new ArrayList<String>(), new ArrayList<String>(), "");

		final int environments = 7;
		final int environmentsToAdd = 4;
		this.setMockValues(mainDesign, environmentsToAdd);

		final List<ImportedGermplasm> germplasmList = this.createImportedGermplasms(5);
		final List<StandardVariable> requiredExpDesignVariable = this.createRequiredVariables();
		final Map<String, List<String>> treatmentFactorValues = new HashMap<String, List<String>>();

		final List<MeasurementRow> measurementRowList =
			this.experimentDesignGenerator.generateExperimentDesignMeasurements(environments, environmentsToAdd, workbook.getTrialVariables(),
						workbook.getFactors(), workbook.getNonTrialFactors(), workbook.getVariates(), null, requiredExpDesignVariable,
						germplasmList, mainDesign, ExperimentDesignGeneratorTest.ENTRY_NO,
						treatmentFactorValues, new HashMap<Integer, Integer>());
		
		Assert.assertEquals(String.valueOf(environmentsToAdd),
				mainDesign.getDesign().getParameterValue(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM));
		Mockito.verify(this.fieldbookService, Mockito.times(1)).runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
		Assert.assertTrue("Expected study instances nos. from " + (environments - environmentsToAdd + 1) + " to " + environments
				+ " for all measurement rows but found a different trial no.",
				this.isStudyInstanceNumbersAssignedCorrectly(measurementRowList, environments, environmentsToAdd));
		
	}
	
	@Test
	public void testGenerateMeasurementsBVDesignError() throws IOException {
		final MainDesign mainDesign =
				this.experimentDesignGenerator.createRandomizedCompleteBlockDesign("2", ExperimentDesignGeneratorTest.REP_NO, ExperimentDesignGeneratorTest.PLOT_NO,
							301, 201, TermId.ENTRY_NO.name(), new ArrayList<String>(), new ArrayList<String>(), "");
		Mockito.doReturn(new BVDesignOutput(1)).when(this.fieldbookService).runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
		try {
			this.experimentDesignGenerator.generateExperimentDesignMeasurements(5, 3, null, null, null, null, null, null, null, mainDesign, null, null, null);
			Assert.fail("Expected to throw BVDesignException but didn't.");
		} catch (final BVDesignException e) {
			Assert.assertEquals("experiment.design.generate.generic.error", e.getBvErrorCode());
		}
	}
	
	@Test
	public void testGenerateMeasurementsBVDesignIOException() throws IOException {
		final MainDesign mainDesign =
				this.experimentDesignGenerator.createRandomizedCompleteBlockDesign("2", ExperimentDesignGeneratorTest.REP_NO, ExperimentDesignGeneratorTest.PLOT_NO,
							301, 201, TermId.ENTRY_NO.name(), new ArrayList<String>(), new ArrayList<String>(), "");
		Mockito.doThrow(new IOException()).when(this.fieldbookService).runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
		try {
			this.experimentDesignGenerator.generateExperimentDesignMeasurements(5, 3, null, null, null, null, null, null, null, mainDesign, null, null, null);
			Assert.fail("Expected to throw BVDesignException but didn't.");
		} catch (final BVDesignException e) {
			Assert.assertEquals("experiment.design.bv.exe.error.generate.generic.error", e.getBvErrorCode());
		}
	}
	
	@Test
	public void testGenerateExperimentDesignInvalidEntryNo() throws IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyTypeDto.getTrialDto());
		final MainDesign mainDesign =
			this.experimentDesignGenerator.createRandomizedCompleteBlockDesign("2", ExperimentDesignGeneratorTest.REP_NO, ExperimentDesignGeneratorTest.PLOT_NO,
						301, 201, TermId.ENTRY_NO.name(), new ArrayList<String>(), new ArrayList<String>(), "");

		final int environments = 7;
		final int environmentsToAdd = 4;
		this.setMockValues(mainDesign, environmentsToAdd);

		final List<ImportedGermplasm> germplasmList = this.createImportedGermplasms(5);
		final List<StandardVariable> requiredExpDesignVariable = this.createRequiredVariables();
		final Map<String, List<String>> treatmentFactorValues = new HashMap<String, List<String>>();

		// Make sure that specified entry number header does not exist in BVOutput headers
		final String entryNumberHeader = RandomStringUtils.randomAlphabetic(10);
		try {
			this.experimentDesignGenerator.generateExperimentDesignMeasurements(
					environments, environmentsToAdd, workbook.getTrialVariables(), workbook.getFactors(), workbook.getNonTrialFactors(),
					workbook.getVariates(), null, requiredExpDesignVariable, germplasmList, mainDesign, entryNumberHeader,
					treatmentFactorValues, new HashMap<Integer, Integer>());
			Assert.fail("Expected to throw BVDesignException but didn't.");
		} catch (BVDesignException e) {
			Assert.assertEquals("experiment.design.bv.exe.error.output.invalid.error", e.getBvErrorCode());
		}
	}
	
	@Test
	public void testGenerateExperimentDesignInvalidEntryID() throws IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyTypeDto.getTrialDto());
		final MainDesign mainDesign =
			this.experimentDesignGenerator.createRandomizedCompleteBlockDesign("2", ExperimentDesignGeneratorTest.REP_NO, ExperimentDesignGeneratorTest.PLOT_NO,
						301, 201, TermId.ENTRY_NO.name(), new ArrayList<String>(), new ArrayList<String>(), "");

		final int environments = 7;
		final int environmentsToAdd = 4;
		this.setMockValues(mainDesign, environmentsToAdd);

		// There are fewer entries in the germplasm list than the one returned by mock BVOutput, hence germplasm will not be found
		final List<ImportedGermplasm> germplasmList = this.createImportedGermplasms(3);
		final List<StandardVariable> requiredExpDesignVariable = this.createRequiredVariables();
		final Map<String, List<String>> treatmentFactorValues = new HashMap<String, List<String>>();

		final String entryNumberHeader = RandomStringUtils.randomAlphabetic(10);
		try {
			this.experimentDesignGenerator.generateExperimentDesignMeasurements(
					environments, environmentsToAdd, workbook.getTrialVariables(), workbook.getFactors(), workbook.getNonTrialFactors(),
					workbook.getVariates(), null, requiredExpDesignVariable, germplasmList, mainDesign, entryNumberHeader,
					treatmentFactorValues, new HashMap<Integer, Integer>());
			Assert.fail("Expected to throw BVDesignException but didn't.");
		} catch (BVDesignException e) {
			Assert.assertEquals("experiment.design.bv.exe.error.output.invalid.error", e.getBvErrorCode());
		}
	}
	
	private void setMockValues(final MainDesign design, final Integer numberOfInstances) throws IOException {
		Mockito.when(this.fieldbookService.runBVDesign(this.workbenchService, this.fieldbookProperties, design))
				.thenReturn(this.createBvOutput(numberOfInstances));
	}

	private BVDesignOutput createBvOutput(int numberOfInstances) {
		final BVDesignOutput bvOutput = new BVDesignOutput(0);
		bvOutput.setResults(BVDesignOutputTest.createEntries(numberOfInstances, 2, 5));
		return bvOutput;
	}
	
	private List<ImportedGermplasm> createImportedGermplasms(final Integer numberOfGermplasm) {
		final List<ImportedGermplasm> germplasms = new ArrayList<ImportedGermplasm>();

		for (int i=1; i <= numberOfGermplasm; i++) {			
			germplasms.add(new ImportedGermplasm(i, RandomStringUtils.randomAlphabetic(20), String.valueOf(new Random().nextInt()), "", "",
					"", ""));
		}

		return germplasms;
	}

	private List<StandardVariable> createRequiredVariables() {
		final List<StandardVariable> reqVariables = new ArrayList<StandardVariable>();

		reqVariables.add(this.createStandardVariable(TermId.REP_NO.getId(), ExperimentDesignGeneratorTest.REP_NO));
		reqVariables.add(this.createStandardVariable(TermId.PLOT_NO.getId(), ExperimentDesignGeneratorTest.PLOT_NO));

		return reqVariables;
	}
	
	private StandardVariable createStandardVariable(final int id, final String name) {
		final StandardVariable var = new StandardVariable();
		var.setId(id);
		var.setName(name);
		return var;
	}
	
	private boolean isStudyInstanceNumbersAssignedCorrectly(final List<MeasurementRow> measurementRowList, final int environments, final int environmentsToAdd) {
		for (final MeasurementRow row : measurementRowList) {
			for (final MeasurementData data : row.getDataList()) {
				if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()
						&& (Integer.parseInt(data.getValue()) > environments || Integer.parseInt(data.getValue()) < environments
								- environmentsToAdd + 1)) {
					return false;
				}
			}
		}
		return true;
	}

	private Map<Integer, ImportedGermplasm> createImportedGermplasmMap() {

		final Map<Integer, ImportedGermplasm> map = new HashMap<>();

		for (int i = 1; i <= 10; i++) {
			map.put(i, ImportedGermplasmMainInfoInitializer.createImportedGermplasm(i));
		}

		return map;

	}

}
