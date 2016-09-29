package com.efficio.fieldbook.web.experimentdesign;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesignParameter;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import com.google.common.base.Optional;
import junit.framework.Assert;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentDesignGeneratorTest {

	public static final String BLOCK_NO = "BLOCK_NO";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String ENTRY_NO = "ENTRY_NO";
	public static final String REP_NO = "REP_NO";
	public static final String NBLOCK = "2";
	public static final String OUTPUT_FILE = "outputfile.csv";

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

		final List<String> treatmentFactor = new ArrayList(Arrays.asList("FACTOR_1", "FACTOR_2"));
		final List<String> levels = new ArrayList(Arrays.asList("Level1", "Level2"));
		final Integer initialPlotNumber = 99;
		final Integer initialEntryNumber = 100;

		final MainDesign mainDesign = experimentDesignGenerator
				.createRandomizedCompleteBlockDesign(NBLOCK, BLOCK_NO, PLOT_NO, initialPlotNumber, initialEntryNumber, treatmentFactor,
						levels, OUTPUT_FILE);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN, expDesign.getName());
		Assert.assertEquals("", expDesign.getParameterValue(ExperimentDesignGenerator.SEED_PARAM));
		Assert.assertEquals(NBLOCK, expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));
		Assert.assertEquals(String.valueOf(initialPlotNumber),
				expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM));
		Assert.assertEquals(String.valueOf(initialEntryNumber),
				expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM));
		Assert.assertEquals(treatmentFactor.size(), expDesign.getParameterList(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM).size());
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
		final List<String> treatmentFactor = new ArrayList(Arrays.asList("FACTOR_1", "FACTOR_2"));
		final List<String> levels = new ArrayList(Arrays.asList("Level1", "Level2"));
		final Integer initialPlotNumber = 99;
		final Integer initialEntryNumber = 100;
		final String nBLatin = "";
		final String replatinGroups = "sample1,sample2";

		final MainDesign mainDesign = experimentDesignGenerator
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
		final List<String> treatmentFactor = new ArrayList(Arrays.asList("FACTOR_1", "FACTOR_2"));
		final List<String> levels = new ArrayList(Arrays.asList("Level1", "Level2"));
		final Integer initialPlotNumber = 99;
		final Integer initialEntryNumber = 100;
		final String nBLatin = "";
		final String replatinGroups = "sample1,sample2";

		final MainDesign mainDesign = experimentDesignGenerator
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
	public void testcCeateAugmentedRandomizedBlockDesign() {

		final String nblks = "2";
		final String numberOfTreatments = "22";
		final String nControls = "11";
		final String plotNumber = "1";

		final MainDesign mainDesign = experimentDesignGenerator
				.createAugmentedRandomizedBlockDesign(nblks, numberOfTreatments, nControls, ENTRY_NO, BLOCK_NO, PLOT_NO, plotNumber);

		final ExpDesign expDesign = mainDesign.getDesign();

		Assert.assertEquals(ExperimentDesignGenerator.AUGMENTED_RANDOMIZED_BLOCK_DESIGN, expDesign.getName());
		Assert.assertEquals(numberOfTreatments, expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
		Assert.assertEquals(nControls, expDesign.getParameterValue(ExperimentDesignGenerator.NCONTROLS_PARAM));
		Assert.assertEquals(nblks, expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		Assert.assertEquals(ENTRY_NO, expDesign.getParameterValue(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM));
		Assert.assertEquals(BLOCK_NO, expDesign.getParameterValue(ExperimentDesignGenerator.BLOCKFACTOR_PARAM));
		Assert.assertEquals(PLOT_NO, expDesign.getParameterValue(ExperimentDesignGenerator.PLOTFACTOR_PARAM));

	}

	@Test
	public void testAddInitialTreatmenNumberIfAvailableInitialEntryNumberIsNull() {

		final List<ExpDesignParameter> paramList = new ArrayList<>();

		final Integer initialEntryNumber = null;

		experimentDesignGenerator.addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		Assert.assertEquals("Initial Treatment Number param should not be added to the param list.", 0, paramList.size());

	}

	@Test
	public void testAddInitialTreatmenNumberIfAvailableInitialEntryNumberHasValue() {

		final List<ExpDesignParameter> paramList = new ArrayList<>();

		final Integer initialEntryNumber = 2;

		experimentDesignGenerator.addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

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

		final List<ListItem> listItems = experimentDesignGenerator.convertToListItemList(listOfString);

		Assert.assertEquals(2, listItems.size());
		Assert.assertEquals(sampleText1, listItems.get(0).getValue());
		Assert.assertEquals(sampleText2, listItems.get(1).getValue());

	}

	@Test
	public void testGetPlotNumberStringValue() {

		Assert.assertEquals("If the initialPlotNumber is null, it should return the default plot number which is '1'.", "1",
				experimentDesignGenerator.getPlotNumberStringValueOrDefault(null));
		Assert.assertEquals("99", experimentDesignGenerator.getPlotNumberStringValueOrDefault(99));
	}

	@Test
	public void testFindImportedGermplasmByEntryNumberAndChecksEntryNumberExistsInImportedGermplasmMap() {

		final Map<Integer, ImportedGermplasm> importedGermplasmMap = createImportedGermplasmMap();

		final Map<Integer, Integer> mapOfChecks = new HashMap<>();
		// Test Entry No 9 is mapped to Check Entry No 3
		mapOfChecks.put(9, 3);
		// Test Entry No 10 is mapped to Check Entry No 5
		mapOfChecks.put(10, 5);

		final Optional<ImportedGermplasm> optionalImportedGermplasm =
				experimentDesignGenerator.findImportedGermplasmByEntryNumberAndChecks(importedGermplasmMap, 1, mapOfChecks);

		Assert.assertTrue(optionalImportedGermplasm.isPresent());
		Assert.assertEquals(Integer.valueOf(1), optionalImportedGermplasm.get().getEntryId());

	}

	@Test
	public void testFindImportedGermplasmByEntryNumberAndChecksEntryNumberIsGeneratedFromDesignEngine() {

		final Map<Integer, ImportedGermplasm> importedGermplasmMap = createImportedGermplasmMap();

		final Map<Integer, Integer> mapOfChecks = new HashMap<>();
		// Test Entry No 9 is mapped to Check Entry No 3
		mapOfChecks.put(9, 3);
		// Test Entry No 10 is mapped to Check Entry No 5
		mapOfChecks.put(10, 5);

		final Optional<ImportedGermplasm> optionalImportedGermplasm =
				experimentDesignGenerator.findImportedGermplasmByEntryNumberAndChecks(importedGermplasmMap, 11, mapOfChecks);

		Assert.assertTrue(optionalImportedGermplasm.isPresent());
		Assert.assertEquals(Integer.valueOf(3), optionalImportedGermplasm.get().getEntryId());

	}

	@Test
	public void testFindImportedGermplasmByEntryNumberAndChecksEntryNumberIsOutsideTheRangeOfGermplasmList() {

		final Map<Integer, ImportedGermplasm> importedGermplasmMap = createImportedGermplasmMap();

		final Map<Integer, Integer> mapOfChecks = new HashMap<>();
		// Test Entry No 9 is mapped to Check Entry No 3
		mapOfChecks.put(9, 3);
		// Test Entry No 10 is mapped to Check Entry No 5
		mapOfChecks.put(10, 5);

		final Optional<ImportedGermplasm> optionalImportedGermplasm =
				experimentDesignGenerator.findImportedGermplasmByEntryNumberAndChecks(importedGermplasmMap, 999, mapOfChecks);

		Assert.assertFalse(optionalImportedGermplasm.isPresent());

	}

	@Test
	public void testFindImportedGermplasmByEntryNumberGeneratedByDesignEngine() {

		final Map<Integer, ImportedGermplasm> importedGermplasmMap = createImportedGermplasmMap();

		final Map<Integer, Integer> mapOfChecks = new HashMap<>();
		// Test Entry No 9 is mapped to Check Entry No 3
		mapOfChecks.put(9, 3);
		// Test Entry No 10 is mapped to Check Entry No 5
		mapOfChecks.put(10, 5);

		// This is mapped to Test Entry No 9
		final Integer entryNoGeneratedFromDesignEngine = 11;

		final Optional<ImportedGermplasm> optionalImportedGermplasm = experimentDesignGenerator
				.findImportedGermplasmByEntryNumberGeneratedByDesignEngine(importedGermplasmMap, entryNoGeneratedFromDesignEngine,
						mapOfChecks);

		Assert.assertTrue(optionalImportedGermplasm.isPresent());
		Assert.assertEquals(Integer.valueOf(3), optionalImportedGermplasm.get().getEntryId());

	}

	@Test
	public void testResolveMappedEntryNumber() {

		final Map<Integer, ImportedGermplasm> importedGermplasmMap = createImportedGermplasmMap();

		final Map<Integer, Integer> mapOfChecks = new HashMap<>();
		// Test Entry No 9 is mapped to Check Entry No 3
		mapOfChecks.put(9, 3);
		// Test Entry No 10 is mapped to Check Entry No 5
		mapOfChecks.put(10, 5);

		// This is mapped to Test Entry No 9
		final Integer entryNoGeneratedFromDesignEngine1 = 11;
		// This is mapped to Test Entry No 10
		final Integer entryNoGeneratedFromDesignEngine2 = 12;

		final Optional<Integer> result1 =
				experimentDesignGenerator.resolveMappedEntryNumber(importedGermplasmMap, entryNoGeneratedFromDesignEngine1, mapOfChecks);
		Assert.assertEquals(Integer.valueOf(3), result1.get());

		final Optional<Integer> result2 =
				experimentDesignGenerator.resolveMappedEntryNumber(importedGermplasmMap, entryNoGeneratedFromDesignEngine2, mapOfChecks);
		Assert.assertEquals(Integer.valueOf(5), result2.get());
	}

	private Map<Integer, ImportedGermplasm> createImportedGermplasmMap() {

		final Map<Integer, ImportedGermplasm> map = new HashMap<>();

		for (int i = 1; i <= 10; i++) {
			map.put(i, ImportedGermplasmMainInfoInitializer.createImportedGermplasm(i));
		}

		return map;

	}

}
