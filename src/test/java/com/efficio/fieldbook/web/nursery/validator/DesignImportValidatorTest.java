
package com.efficio.fieldbook.web.nursery.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.data.initializer.DesignImportDataInitializer;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;

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
	public void setUp() {
		this.designImportData = DesignImportDataInitializer.createDesignImportData();

		Mockito.doReturn("Error encountered.").when(this.messageSource)
				.getMessage("design.import.error.no.valid.values", null, Locale.ENGLISH);
		Mockito.doReturn(ImportedGermplasmMainInfoInitializer.createImportedGermplasmMainInfo()).when(this.userSelection)
				.getImportedGermplasmMainInfo();

		final DesignHeaderItem trialInstanceHeaderItem =
				DesignImportDataInitializer.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, this.designImportData
						.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

		Mockito.doReturn(trialInstanceHeaderItem)
				.when(this.designImportService)
				.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

		final DesignHeaderItem headerItem =
				DesignImportDataInitializer.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO,
						this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));
		Mockito.doReturn(headerItem).when(this.designImportService)
				.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));
	}

	@Test
	public void testValidateDesignData() throws MiddlewareException {
		try {

			this.designImportValidator.validateDesignData(this.designImportData);

		} catch (final DesignValidationException e) {

			Assert.fail("The data should pass the validateDesignData test");
		}

	}

	@Test
	public void testValidateEntryNoMustBeUniquePerInstance() throws MiddlewareException {

		try {

			final DesignHeaderItem trialInstanceHeaderItem =
					this.designImportService.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, this.designImportData
							.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
			final DesignHeaderItem entryNoHeaderItem =
					this.designImportService.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO,
							this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

			final Map<String, Map<Integer, List<String>>> data =
					this.designImportService.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getCsvData());

			this.designImportValidator.validateEntryNoMustBeUniquePerInstance(entryNoHeaderItem, data);

		} catch (final DesignValidationException e) {

			Assert.fail("The list should pass the validateEntryNoMustBeUniquePerInstance test");
		}

	}

	@Test
	public void testValidateEntryNoMustBeUniquePerInstanceEntryNoIsNotUnique() throws MiddlewareException {

		final DesignHeaderItem trialInstanceHeaderItem =
				DesignImportDataInitializer.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, this.designImportData
						.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		final DesignHeaderItem entryNoHeaderItem =
				DesignImportDataInitializer.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO,
						this.designImportData.getMappedHeaders().get(PhenotypicType.GERMPLASM));

		final Map<Integer, List<String>> csvData = this.designImportData.getCsvData();
		csvData.get(1).set(entryNoHeaderItem.getColumnIndex(), "1");
		csvData.get(2).set(entryNoHeaderItem.getColumnIndex(), "1");
		csvData.get(3).set(entryNoHeaderItem.getColumnIndex(), "1");

		final Map<String, Map<Integer, List<String>>> data =
				DesignImportDataInitializer.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, csvData);

		try {

			this.designImportValidator.validateEntryNoMustBeUniquePerInstance(entryNoHeaderItem, data);

			Assert.fail("The list shouldn't pass the validateEntryNoMustBeUniquePerInstance test");

		} catch (final DesignValidationException e) {

		}
	}

	@Test
	public void testValidateGermplasmEntriesFromShouldMatchTheGermplasmList() {

		final Set<String> entryNumbers = new HashSet<>();
		for (int x = 1; x <= DesignImportDataInitializer.NO_OF_TEST_ENTRIES; x++) {
			entryNumbers.add(String.valueOf(x));
		}

		try {

			this.designImportValidator.validateGermplasmEntriesFromShouldMatchTheGermplasmList(entryNumbers);

		} catch (final DesignValidationException e) {

			Assert.fail("The data should pass the validateGermplasmEntriesFromShouldMatchTheGermplasmList test");
		}

	}

	@Test
	public void testValidateIfEntryNumberExistsNoEntryNumber() throws MiddlewareException {

		try {

			this.designImportValidator.validateIfStandardVariableExists(this.designImportData
					.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_ENVIRONMENT),
					"design.import.error.entry.no.is.required", TermId.ENTRY_NO);

			Assert.fail("The logic did not detect that the entry number doesn't exist");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testValidateIfEntryNumberExistsWithEntryNumber() throws MiddlewareException {

		try {

			this.designImportValidator.validateIfStandardVariableExists(this.designImportData
					.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.GERMPLASM),
					"design.import.error.entry.no.is.required", TermId.ENTRY_NO);

		} catch (final DesignValidationException e) {

			Assert.fail("The logic did not detect that the entry number doesn't exist");

		}

	}

	@Test
	public void testValidateIfPlotNumberExistsNoPlotNumber() throws MiddlewareException {

		try {

			this.designImportValidator.validateIfStandardVariableExists(this.designImportData
					.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.GERMPLASM),
					"design.import.error.plot.no.is.required", TermId.PLOT_NO);
			Assert.fail("The logic did not detect that the plot number do not exist");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testValidateIfPlotNumberExistsWithPlotNumber() throws MiddlewareException {

		try {

			this.designImportValidator.validateIfStandardVariableExists(this.designImportData
					.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_DESIGN),
					"design.import.error.plot.no.is.required", TermId.PLOT_NO);

		} catch (final DesignValidationException e) {

			Assert.fail("The logic did not detect that the plot number exist");

		}

	}

	@Test
	public void testValidateIfPlotNumberIsUnique() throws MiddlewareException {

		final DesignHeaderItem trialInstanceHeaderItem =
				this.designImportService.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, this.designImportData
						.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		final Map<String, Map<Integer, List<String>>> csvMap =
				this.designImportService.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, this.designImportData.getCsvData());

		try {

			this.designImportValidator.validateIfPlotNumberIsUniquePerInstance(
					this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN), csvMap);

		} catch (final DesignValidationException e) {

			Assert.fail("The list should pass the validateIfPlotNumberIsUnique test");
		}

	}

	@Test
	public void testValidateIfPlotNumberIsUniquePerInstance() throws MiddlewareException {

		final DesignHeaderItem trialInstanceHeaderItem =
				DesignImportDataInitializer.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, this.designImportData
						.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		final DesignHeaderItem plotNoHeaderItem =
				DesignImportDataInitializer.filterDesignHeaderItemsByTermId(TermId.PLOT_NO,
						this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));

		final Map<Integer, List<String>> csvData = this.designImportData.getCsvData();
		csvData.get(0).set(plotNoHeaderItem.getColumnIndex(), "1");
		csvData.get(1).set(plotNoHeaderItem.getColumnIndex(), "1");
		csvData.get(2).set(plotNoHeaderItem.getColumnIndex(), "1");

		final Map<String, Map<Integer, List<String>>> csvMap =
				DesignImportDataInitializer.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, csvData);

		try {

			this.designImportValidator.validateIfPlotNumberIsUniquePerInstance(
					this.designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN), csvMap);

			Assert.fail("The list shouldn't pass the validateIfPlotNumberIsUniquePerInstance test");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testValidateIfStandardVariableExists() throws MiddlewareException {
		try {

			this.designImportValidator.validateIfStandardVariableExists(this.designImportData
					.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_ENVIRONMENT),
					"design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);

		} catch (final DesignValidationException e) {

			Assert.fail("The logic did not detect that the trial number exist");

		}

	}

	@Test
	public void testValidateIfStandardVariableExistsTrialInstanceDoNotExist() throws MiddlewareException {

		try {

			this.designImportValidator.validateIfStandardVariableExists(this.designImportData
					.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.GERMPLASM),
					"design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);

			Assert.fail("The logic should detect that the trial number exist");

		} catch (final DesignValidationException e) {

		}

	}

	@Test
	public void testValidateColumnValuesForImportDesignWithOutInvalidValue() {
		final DesignImportData designImportData = DesignImportDataInitializer.createDesignImportData();
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();
		try {
			this.designImportValidator.validateColumnValues(designImportData.getCsvData(), mappedHeaders);
		} catch (final DesignValidationException e) {
			Assert.fail("Expecting that there is no exception thrown for the validation of test design import data with no invalid values.");
		}
	}

	@Test
	public void testRetrieveDesignHeaderItemsBasedOnDataType() {
		final DesignImportData designImportData = DesignImportDataInitializer.createDesignImportData();
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();

		final List<DesignHeaderItem> numericDesignHeaderItems =
				this.designImportValidator.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.NUMERIC_VARIABLE.getId());
		Assert.assertEquals(DesignImportDataInitializer.NO_OF_NUMERIC_VARIABLES, numericDesignHeaderItems.size());

		final List<DesignHeaderItem> characterDesignHeaderItems =
				this.designImportValidator.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.CHARACTER_VARIABLE.getId());
		Assert.assertEquals(DesignImportDataInitializer.NO_OF_CHARACTER_VARIABLES, characterDesignHeaderItems.size());

		this.designImportValidator.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.CATEGORICAL_VARIABLE.getId());
		Assert.assertEquals(DesignImportDataInitializer.NO_OF_CATEGORICAL_VARIABLES, characterDesignHeaderItems.size());

	}
}
