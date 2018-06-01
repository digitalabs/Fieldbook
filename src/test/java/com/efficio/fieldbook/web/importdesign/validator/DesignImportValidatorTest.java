package com.efficio.fieldbook.web.importdesign.validator;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.importdesign.service.DesignImportService;
import junit.framework.Assert;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class DesignImportValidatorTest {

	@Mock
	private UserSelection userSelection;

	@Mock
	private MessageSource messageSource;

	@Mock
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Mock
	private DesignImportService designImportService;

	@InjectMocks
	private DesignImportValidator designImportValidator;

	private DesignImportData designImportData;

	@Before
	public void setUp() throws DesignValidationException {
		this.designImportData = DesignImportTestDataInitializer.createDesignImportData(1, 1);

		Mockito.doReturn("Error encountered.").when(this.messageSource)
				.getMessage("design.import.error.no.valid.values", null, Locale.ENGLISH);
		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final DesignHeaderItem trialInstanceHeaderItem = DesignImportTestDataInitializer
				.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		Mockito.doReturn(trialInstanceHeaderItem).when(this.designImportService).validateIfStandardVariableExists(
				this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_ENVIRONMENT),
				"design.import.error.study.is.required", TermId.TRIAL_INSTANCE_FACTOR);

		final DesignHeaderItem headerItem = DesignImportTestDataInitializer
				.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));
		Mockito.doReturn(headerItem).when(this.designImportService).validateIfStandardVariableExists(
				this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.GERMPLASM),
				"design.import.error.entry.no.is.required", TermId.ENTRY_NO);

		Mockito.doReturn("Error encountered entries {0} and listsize {1}").when(this.messageSource)
				.getMessage("design.import.error.mismatch.count.of.germplasm.entries", null, Locale.ENGLISH);
		Mockito.doReturn("entries do not match").when(this.messageSource)
				.getMessage("design.import.error.mismatch.germplasm.entries", null, Locale.ENGLISH);
	}

	@Test
	public void testValidateDesignData() {
		try {

			this.designImportValidator.validateDesignData(this.designImportData);

		} catch (final DesignValidationException e) {

			Assert.fail("The data should pass the validateDesignData test");
		}

	}

	@Test
	public void testValidateEntryNoMustBeUniquePerInstance() {

		try {

			final DesignHeaderItem trialInstanceHeaderItem = this.designImportService.validateIfStandardVariableExists(
					this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_ENVIRONMENT),
					"Error", TermId.TRIAL_INSTANCE_FACTOR);
			final DesignHeaderItem entryNoHeaderItem = this.designImportService.validateIfStandardVariableExists(
					this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.GERMPLASM), "Error",
					TermId.ENTRY_NO);

			final Map<String, Map<Integer, List<String>>> data =
					this.designImportService.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getRowDataMap());

			this.designImportValidator.validateEntryNoMustBeUniquePerInstance(entryNoHeaderItem, data);

		} catch (final DesignValidationException e) {

			Assert.fail("The list should pass the validateEntryNoMustBeUniquePerInstance test");
		}

	}

	@Test
	public void testValidateEntryNoMustBeUniquePerInstanceEntryNoIsNotUnique() {

		final DesignHeaderItem trialInstanceHeaderItem = DesignImportTestDataInitializer
				.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		final DesignHeaderItem entryNoHeaderItem = DesignImportTestDataInitializer
				.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

		final Map<Integer, List<String>> csvData = this.designImportData.getRowDataMap();
		csvData.get(1).set(entryNoHeaderItem.getColumnIndex(), "1");
		csvData.get(2).set(entryNoHeaderItem.getColumnIndex(), "1");
		csvData.get(3).set(entryNoHeaderItem.getColumnIndex(), "1");

		final Map<String, Map<Integer, List<String>>> data =
				DesignImportTestDataInitializer.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, csvData);

		try {

			this.designImportValidator.validateEntryNoMustBeUniquePerInstance(entryNoHeaderItem, data);

			Assert.fail("The list shouldn't pass the validateEntryNoMustBeUniquePerInstance test");

		} catch (final DesignValidationException e) {

		}
	}

	@Test
	public void testValidateGermplasmEntriesShouldMatchTheGermplasmList() {

		final Set<String> entryNumbers = new HashSet<>();
		final int startingEntryNo = 1;
		for (int x = startingEntryNo; x <= DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES; x++) {
			entryNumbers.add(String.valueOf(x));
		}

		try {

			this.designImportValidator.validateGermplasmEntriesShouldMatchTheGermplasmList(entryNumbers);

		} catch (final DesignValidationException e) {

			Assert.fail("The data should pass the validateGermplasmEntriesFromShouldMatchTheGermplasmList test");
		}

	}

	@Test
	public void testvValidateGermplasmEntriesShouldMatchTheGermplasmListListSizeDoNotMatch() {

		final int wrongNumberOfEntries = DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES + 5;
		final Set<String> entryNumbers = new HashSet<>();

		final int startingEntryNo = 1;
		for (int x = startingEntryNo; x <= wrongNumberOfEntries; x++) {
			entryNumbers.add(String.valueOf(x));
		}

		try {

			this.designImportValidator.validateGermplasmEntriesShouldMatchTheGermplasmList(entryNumbers);

		} catch (final DesignValidationException e) {

			Assert.assertEquals(e.getMessage(), "Error encountered entries 10 and listsize 5");
		}

	}

	@Test
	public void testvValidateGermplasmEntriesShouldMatchTheGermplasmListEntriesDoNotMatch() {

		final int wrongNumberOfEntries = DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES + 2;
		final Set<String> entryNumbers = new HashSet<>();

		final int startingEntryNo = 3;
		for (int x = startingEntryNo; x <= wrongNumberOfEntries; x++) {
			entryNumbers.add(String.valueOf(x));
		}

		try {

			this.designImportValidator.validateGermplasmEntriesShouldMatchTheGermplasmList(entryNumbers);

		} catch (final DesignValidationException e) {

			Assert.assertEquals(e.getMessage(), "entries do not match");
		}

	}

	@Test
	public void testValidateIfPlotNumberIsUnique() throws DesignValidationException {

		final DesignHeaderItem trialInstanceHeaderItem = this.designImportService.validateIfStandardVariableExists(
				this.designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_ENVIRONMENT),
				"Error", TermId.TRIAL_INSTANCE_FACTOR);
		final Map<String, Map<Integer, List<String>>> csvMap =
				this.designImportService.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getRowDataMap());

		try {

			this.designImportValidator
					.validateIfPlotNumberIsUniquePerInstance(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN),
							csvMap);

		} catch (final DesignValidationException e) {

			Assert.fail("The list should pass the validateIfPlotNumberIsUnique test");
		}

	}

	@Test
	public void testValidateIfPlotNumberIsUniquePerInstance() {

		final DesignHeaderItem trialInstanceHeaderItem = DesignImportTestDataInitializer
				.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		final DesignHeaderItem plotNoHeaderItem = DesignImportTestDataInitializer
				.filterDesignHeaderItemsByTermId(TermId.PLOT_NO, this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));

		final Map<Integer, List<String>> csvData = this.designImportData.getRowDataMap();
		csvData.get(0).set(plotNoHeaderItem.getColumnIndex(), "1");
		csvData.get(1).set(plotNoHeaderItem.getColumnIndex(), "1");
		csvData.get(2).set(plotNoHeaderItem.getColumnIndex(), "1");

		final Map<String, Map<Integer, List<String>>> csvMap =
				DesignImportTestDataInitializer.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, csvData);

		try {

			this.designImportValidator
					.validateIfPlotNumberIsUniquePerInstance(this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN),
							csvMap);

			Assert.fail("The list shouldn't pass the validateIfPlotNumberIsUniquePerInstance test");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testValidateColumnValuesForImportDesignWithOutInvalidValue() {
		final DesignImportData designImportData = DesignImportTestDataInitializer.createDesignImportData(1, 1);
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		try {
			this.designImportValidator.validateColumnValues(designImportData.getRowDataMap(), mappedHeaders);
		} catch (final DesignValidationException e) {
			Assert.fail(
					"Expecting that there is no exception thrown for the validation of test design import data with no invalid values.");
		}
	}

	@Test
	public void testRetrieveDesignHeaderItemsBasedOnDataType() {
		final DesignImportData designImportData = DesignImportTestDataInitializer.createDesignImportData(1, 1);
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();

		final List<DesignHeaderItem> numericDesignHeaderItems =
				this.designImportValidator.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.NUMERIC_VARIABLE.getId());
		Assert.assertEquals(DesignImportTestDataInitializer.NO_OF_NUMERIC_VARIABLES, numericDesignHeaderItems.size());

		final List<DesignHeaderItem> characterDesignHeaderItems =
				this.designImportValidator.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.CHARACTER_VARIABLE.getId());
		Assert.assertEquals(DesignImportTestDataInitializer.NO_OF_CHARACTER_VARIABLES, characterDesignHeaderItems.size());

		this.designImportValidator.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.CATEGORICAL_VARIABLE.getId());
		Assert.assertEquals(DesignImportTestDataInitializer.NO_OF_CATEGORICAL_VARIABLES, characterDesignHeaderItems.size());

	}
}
