package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignValidator;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import junit.framework.Assert;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class AugmentedRandomizedBlockDesignServiceImplTest {

	public static final String GENERIC_ERROR_MESSAGE = "generic error";
	private static final String PROGRAM_UUID = "2191a54c-7d98-40d0-ae6f-6a400e4546ce";
	public static final String DESIGN_ERROR_MESSAGE = "design error message";

	private static final int FIRST_CHECK_ENTRY = 0;
	private static final int THIRD_CHECK_ENTRY = 2;
	
	private StandardVariable entryNoVariable;
	private StandardVariable blockNoVariable;
	private StandardVariable plotNoVariable;

	@Mock
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Mock
	private ExperimentDesignValidator experimentDesignValidator;

	@Mock
	private MessageSource messageSource;

	@Mock
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	public ContextUtil contextUtil;

	@InjectMocks
	private AugmentedRandomizedBlockDesignServiceImpl augmentedRandomizedBlockDesignServiceImpl;

	@Before
	public void init() {
		this.entryNoVariable = StandardVariableTestDataInitializer.createStandardVariable(TermId.ENTRY_NO.getId(), "Entry No");
		this.blockNoVariable = StandardVariableTestDataInitializer.createStandardVariable(TermId.BLOCK_NO.getId(), "Block No");
		this.plotNoVariable = StandardVariableTestDataInitializer.createStandardVariable(TermId.PLOT_NO.getId(), "Plot No");
		
		Mockito.when(contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(), PROGRAM_UUID)).thenReturn(entryNoVariable);
		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), PROGRAM_UUID)).thenReturn(blockNoVariable);
		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), PROGRAM_UUID)).thenReturn(plotNoVariable);

		Mockito.when(messageSource.getMessage("experiment.design.invalid.generic.error", null, LocaleContextHolder.getLocale()))
				.thenReturn(GENERIC_ERROR_MESSAGE);

	}

	@Test
	public void testGenerateDesign() throws BVDesignException {

		final Integer startingPlotNo = 1;
		final Integer startingEntryNo = 1;
		final Integer numberOfBlocks = 2;
		final Integer noOfExistingEnvironments = 1;
		final Integer noOfEnvironmentsToBeAdded = 0;

		final ExpDesignParameterUi experimentDesignParameterFromUI = new ExpDesignParameterUi();

		experimentDesignParameterFromUI.setNoOfEnvironments(String.valueOf(noOfExistingEnvironments));
		experimentDesignParameterFromUI.setNoOfEnvironmentsToAdd(String.valueOf(noOfEnvironmentsToBeAdded));
		experimentDesignParameterFromUI.setNumberOfBlocks(String.valueOf(numberOfBlocks));
		experimentDesignParameterFromUI.setStartingPlotNo(String.valueOf(startingPlotNo));
		experimentDesignParameterFromUI.setStartingEntryNo(String.valueOf(startingEntryNo));

		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		final List<MeasurementVariable> variables = new ArrayList<>();
		final List<MeasurementVariable> factors = new ArrayList<>();
		final List<MeasurementVariable> nonTrialFactors = new ArrayList<>();
		final List<MeasurementVariable> variates = new ArrayList<>();
		final List<TreatmentVariable> treatmentVariables = new ArrayList<>();
		final List<StandardVariable> requiredVariables = augmentedRandomizedBlockDesignServiceImpl.getRequiredDesignVariables();

		final Set<Integer> entryIdsOfCheckEntries = augmentedRandomizedBlockDesignServiceImpl.getEntryIdsOfChecks(importedGermplasmList);
		final Set<Integer> entryIdsOfTestEntries = augmentedRandomizedBlockDesignServiceImpl.getEntryIdsOfTestEntries(importedGermplasmList);

		final Map<Integer, Integer> designExpectedEntriesMap = augmentedRandomizedBlockDesignServiceImpl.createMapOfDesignExpectedEntriesToGermplasmEntriesInTrial(importedGermplasmList, entryIdsOfCheckEntries, entryIdsOfTestEntries);

		final MainDesign mainDesign = new MainDesign();

		Mockito.when(this.experimentDesignGenerator
				.createAugmentedRandomizedBlockDesign(numberOfBlocks, importedGermplasmList.size(), importedGermplasmList.size(),
						startingPlotNo, startingEntryNo, entryNoVariable.getName(), blockNoVariable.getName(), plotNoVariable.getName()))
				.thenReturn(mainDesign);

		augmentedRandomizedBlockDesignServiceImpl
				.generateDesign(importedGermplasmList, experimentDesignParameterFromUI, variables, factors, nonTrialFactors, variates,
						treatmentVariables);

		Mockito.verify(this.experimentDesignGenerator)
				.createAugmentedRandomizedBlockDesign(numberOfBlocks, importedGermplasmList.size(), importedGermplasmList.size(),
						startingPlotNo, startingEntryNo, entryNoVariable.getName(), blockNoVariable.getName(), plotNoVariable.getName());

		Mockito.verify(this.experimentDesignGenerator)
				.generateExperimentDesignMeasurements(noOfExistingEnvironments, noOfEnvironmentsToBeAdded, variables, factors,
						nonTrialFactors, variates, treatmentVariables, requiredVariables, importedGermplasmList, mainDesign,
						entryNoVariable.getName(), null, designExpectedEntriesMap);

	}

	@Test
	public void testConvertStandardVariableListToMap() {

		final Map<Integer, StandardVariable> result = augmentedRandomizedBlockDesignServiceImpl
				.convertStandardVariableListToMap(augmentedRandomizedBlockDesignServiceImpl.getRequiredDesignVariables());

		Assert.assertTrue(result.containsKey(TermId.ENTRY_NO.getId()));
		Assert.assertTrue(result.containsKey(TermId.BLOCK_NO.getId()));
		Assert.assertTrue(result.containsKey(TermId.PLOT_NO.getId()));

	}

	@Test
	public void testGetRequiredVariable() {

		final List<StandardVariable> result = augmentedRandomizedBlockDesignServiceImpl.getRequiredDesignVariables();

		Assert.assertEquals("There should be 3 required variable for augmented design", 3, result.size());

		final Map<Integer, StandardVariable> standardVariableMap =
				augmentedRandomizedBlockDesignServiceImpl.convertStandardVariableListToMap(result);

		Assert.assertEquals(standardVariableMap.get(TermId.BLOCK_NO.getId()).getPhenotypicType(), PhenotypicType.TRIAL_DESIGN);
		Assert.assertEquals(standardVariableMap.get(TermId.PLOT_NO.getId()).getPhenotypicType(), PhenotypicType.TRIAL_DESIGN);
		Assert.assertEquals(standardVariableMap.get(TermId.ENTRY_NO.getId()).getPhenotypicType(), PhenotypicType.GERMPLASM);

	}

	@Test
	public void testCreateMapOfBreedingViewExpectedEntriesToGermplasmEntriesInStudy() {

		final List<ImportedGermplasm> importedGermplasmList = createImportedGermplasmList();

		// Set the Entry no 1 and 3 as check entries
		importedGermplasmList.get(0).setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		importedGermplasmList.get(2).setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());

		final Set<Integer> entryIdsOfCheckEntries = augmentedRandomizedBlockDesignServiceImpl.getEntryIdsOfChecks(importedGermplasmList);
		final Set<Integer> entryIdsOfTestEntries = augmentedRandomizedBlockDesignServiceImpl.getEntryIdsOfTestEntries(importedGermplasmList);

		final Map<Integer, Integer> result = augmentedRandomizedBlockDesignServiceImpl.createMapOfDesignExpectedEntriesToGermplasmEntriesInTrial(importedGermplasmList, entryIdsOfCheckEntries, entryIdsOfTestEntries);

		Assert.assertEquals("Entry Number 1 should be mapped to Test Entry 2", 2, result.get(1).intValue());
		Assert.assertEquals("Entry Number 2 should be mapped to Test Entry 3", 4, result.get(2).intValue());
		Assert.assertEquals("Entry Number 3 should be mapped to Test Entry 4", 5, result.get(3).intValue());
		Assert.assertEquals("Entry Number 4 should be mapped to Test Entry 5", 6, result.get(4).intValue());
		Assert.assertEquals("Entry Number 5 should be mapped to Test Entry 6", 7, result.get(5).intValue());
		Assert.assertEquals("Entry Number 6 should be mapped to Test Entry 7", 8, result.get(6).intValue());
		Assert.assertEquals("Entry Number 7 should be mapped to Test Entry 8", 9, result.get(7).intValue());
		Assert.assertEquals("Entry Number 8 should be mapped to Test Entry 10", 10, result.get(8).intValue());
		Assert.assertEquals("Entry Number 9 should be mapped to Check Entry 1", 1, result.get(9).intValue());
		Assert.assertEquals("Entry Number 10 should be mapped to Check Entry 3", 3, result.get(10).intValue());


	}

	@Test
	public void testGetEntryIdsOfCheckEntries() {

		final List<ImportedGermplasm> importedGermplasmList = createImportedGermplasmList();

		// Set the Entry no 1 and 3 as check entries
		importedGermplasmList.get(FIRST_CHECK_ENTRY).setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		importedGermplasmList.get(THIRD_CHECK_ENTRY).setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());

		final Set<Integer> result = augmentedRandomizedBlockDesignServiceImpl.getEntryIdsOfChecks(importedGermplasmList);

		Assert.assertEquals("There are only 2 checks in the germplasm list", 2, result.size());

		// Entry Id 1 and 3 should exist in the result set.
		Assert.assertTrue(result.contains(1));
		Assert.assertTrue(result.contains(3));

	}

	@Test
	public void testGetEntryIdsOfTestEntries() {

		final List<ImportedGermplasm> importedGermplasmList = createImportedGermplasmList();

		// Set the Entry no 1 and 3 as check entries
		importedGermplasmList.get(FIRST_CHECK_ENTRY).setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		importedGermplasmList.get(THIRD_CHECK_ENTRY).setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());

		final Set<Integer> result = augmentedRandomizedBlockDesignServiceImpl.getEntryIdsOfTestEntries(importedGermplasmList);

		Assert.assertEquals("There should be 8 test entries in the list", 8, result.size());

		// Entry Id 1 and 3 should not exist in the result set because they are check entries
		Assert.assertTrue(!result.contains(1));
		Assert.assertTrue(!result.contains(3));

	}

	@Test
	public void testGetExperimentalDesignVariables() {

		final List<Integer> result = augmentedRandomizedBlockDesignServiceImpl.getExperimentalDesignVariables(null);

		Assert.assertEquals("There are only two experiment design variables are assigned to augmented design.", 2, result.size());

		Assert.assertTrue(result.contains(TermId.EXPERIMENT_DESIGN_FACTOR.getId()));
		Assert.assertTrue(result.contains(TermId.NBLKS.getId()));

	}

	@Test
	public void testValidateDesignValidationSuccessful() throws DesignValidationException {

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		final List<ImportedGermplasm> germplasmList = new ArrayList<>();

		Mockito.doNothing().when(experimentDesignValidator).validateAugmentedDesign(expDesignParameterUi, germplasmList);

		final ExpDesignValidationOutput result = augmentedRandomizedBlockDesignServiceImpl.validate(expDesignParameterUi, germplasmList);

		Assert.assertTrue(result.isValid());

	}

	@Test
	public void testValidateDesignValidationExceptionError() throws DesignValidationException {

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		final List<ImportedGermplasm> germplasmList = new ArrayList<>();

		final DesignValidationException designValidationException = new DesignValidationException(DESIGN_ERROR_MESSAGE);

		Mockito.doThrow(designValidationException).when(experimentDesignValidator)
				.validateAugmentedDesign(expDesignParameterUi, germplasmList);

		final ExpDesignValidationOutput result = augmentedRandomizedBlockDesignServiceImpl.validate(expDesignParameterUi, germplasmList);

		Assert.assertEquals(DESIGN_ERROR_MESSAGE, result.getMessage());
		Assert.assertFalse(result.isValid());

	}

	@Test
	public void testValidateGenericError() throws DesignValidationException {

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		final List<ImportedGermplasm> germplasmList = new ArrayList<>();

		Mockito.doThrow(Exception.class).when(experimentDesignValidator).validateAugmentedDesign(expDesignParameterUi, germplasmList);

		final ExpDesignValidationOutput result = augmentedRandomizedBlockDesignServiceImpl.validate(expDesignParameterUi, germplasmList);

		Assert.assertEquals(GENERIC_ERROR_MESSAGE, result.getMessage());
		Assert.assertFalse(result.isValid());

	}

	private List<ImportedGermplasm> createImportedGermplasmList() {

		final List<ImportedGermplasm> importedGermplasmList = new LinkedList<>();

		// Create 10 imported germpasm entries
		for (int i = 1; i <= 10; i++) {
			final ImportedGermplasm importedGermplasm = ImportedGermplasmMainInfoInitializer.createImportedGermplasm(i);
			importedGermplasm.setEntryTypeCategoricalID(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
			importedGermplasmList.add(ImportedGermplasmMainInfoInitializer.createImportedGermplasm(i));
		}

		return importedGermplasmList;

	}

}
