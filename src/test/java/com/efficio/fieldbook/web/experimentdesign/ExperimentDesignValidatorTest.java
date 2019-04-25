package com.efficio.fieldbook.web.experimentdesign;

import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.data.initializer.ImportedGermplasmMainInfoInitializer;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import junit.framework.Assert;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentDesignValidatorTest {

	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private ExperimentDesignValidator experimentDesignValidator;

	@Test
	public void testValidateAugmentedDesignSuccess() {

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setNumberOfBlocks("2");
		expDesignParameterUi.setStartingPlotNo("1");
		expDesignParameterUi.setStartingEntryNo("1");
		expDesignParameterUi.setTreatmentFactorsData(new HashMap());

		final List<ImportedGermplasm> importedGermplasmList = this.createImportedGermplasmList();
		// Make the first ImportedGermplasm a check entry type.
		importedGermplasmList.get(0).setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());

		try {
			this.experimentDesignValidator.validateAugmentedDesign(expDesignParameterUi, importedGermplasmList);
		} catch (final DesignValidationException e) {
			Assert.fail("validateAugmentedDesign() should not throw a DesignValidationException.");
		}

		Mockito.verifyZeroInteractions(this.messageSource);

	}

	@Test
	public void testValidateAugmentedDesignFail() {

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setNumberOfBlocks("1");
		expDesignParameterUi.setStartingPlotNo("1");
		expDesignParameterUi.setStartingEntryNo("1");

		// Create imported germplasm list without check
		final List<ImportedGermplasm> importedGermplasmList = this.createImportedGermplasmList();

		try {
			this.experimentDesignValidator.validateAugmentedDesign(expDesignParameterUi, importedGermplasmList);
			Assert.fail("validateAugmentedDesign() should throw a DesignValidationException.");
		} catch (final DesignValidationException e) {

		}

		Mockito.verify(this.messageSource).getMessage("germplasm.list.check.required.augmented.design", null, LocaleContextHolder.getLocale());

	}

	@Test
	public void testValidateIfCheckEntriesExistInGermplasmListCheckEntriesExist() {

		final List<ImportedGermplasm> importedGermplasmList = this.createImportedGermplasmList();
		// Make the first ImportedGermplasm a check entry type.
		importedGermplasmList.get(0).setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());

		try {
			this.experimentDesignValidator.validateIfCheckEntriesExistInGermplasmList(importedGermplasmList);
		} catch (final DesignValidationException e) {
			Assert.fail(
					"validateIfCheckEntriesExistInGermplasmList() should not throw a DesignValidationException because there are check entries in the imported germplasm list.");
		}

	}

	@Test
	public void testValidateIfCheckEntriesExistInGermplasmListCheckEntriesDoesNotExist() {

		final List<ImportedGermplasm> importedGermplasmList = this.createImportedGermplasmList();

		try {
			this.experimentDesignValidator.validateIfCheckEntriesExistInGermplasmList(importedGermplasmList);
			Assert.fail(
					"validateIfCheckEntriesExistInGermplasmList() should throw a DesignValidationException because there are no check entries in the imported germplasm list.");
		} catch (final DesignValidationException e) {

		}

		Mockito.verify(this.messageSource).getMessage("germplasm.list.check.required.augmented.design", null, LocaleContextHolder.getLocale());

	}

	@Test
	public void testValidateStartingPlotNoSuccess() {

		final int treatmentSize = 10;

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setStartingPlotNo("1");

		try {
			this.experimentDesignValidator.validateStartingPlotNo(expDesignParameterUi, treatmentSize);
		} catch (final DesignValidationException e) {
			Assert.fail("validateStartingPlotNo() should not fail.");
		}

		Mockito.verifyZeroInteractions(this.messageSource);
	}

	@Test
	public void testValidateStartingPlotNoEmptyNumber() {

		final int treatmentSize = 10;

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setStartingPlotNo("");

		try {
			this.experimentDesignValidator.validateStartingPlotNo(expDesignParameterUi, treatmentSize);
			Assert.fail("validateStartingPlotNo() should throw a DesignValidationException");
		} catch (final DesignValidationException e) {

		}
		Mockito.verify(this.messageSource).getMessage("plot.number.should.be.in.range", null, LocaleContextHolder.getLocale());
	}

	@Test
	public void testValidateStartingPlotNoNotNumeric() {

		final int treatmentSize = 10;

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setStartingPlotNo("abc");

		try {
			this.experimentDesignValidator.validateStartingPlotNo(expDesignParameterUi, treatmentSize);
			Assert.fail("validateStartingPlotNo() should throw a DesignValidationException");
		} catch (final DesignValidationException e) {

		}
		Mockito.verify(this.messageSource).getMessage("plot.number.should.be.in.range", null, LocaleContextHolder.getLocale());
	}

	@Test
	public void testValidateStartingPlotNoOutOfRange() {

		final int treatmentSize = 10;

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setStartingPlotNo("100000000");

		try {
			this.experimentDesignValidator.validateStartingPlotNo(expDesignParameterUi, treatmentSize);
			Assert.fail("validateStartingPlotNo() should throw a DesignValidationException");
		} catch (final DesignValidationException e) {

		}
		Mockito.verify(this.messageSource).getMessage("plot.number.should.be.in.range", null, LocaleContextHolder.getLocale());
	}

	@Test
	public void testValidateStartingEntryNoSuccess() {

		final int treatmentSize = 10;

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setStartingEntryNo("1");

		try {
			this.experimentDesignValidator.validateStartingEntryNo(expDesignParameterUi, treatmentSize);
		} catch (final DesignValidationException e) {
			Assert.fail("validateStartingEntryNo() should not fail.");
		}

		Mockito.verifyZeroInteractions(this.messageSource);
	}

	@Test
	public void testValidateStartingEntryNoEmptyNumber() {

		final int treatmentSize = 10;

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setStartingEntryNo("");

		try {
			this.experimentDesignValidator.validateStartingEntryNo(expDesignParameterUi, treatmentSize);
			Assert.fail("validateStartingEntryNo() should throw a DesignValidationException");
		} catch (final DesignValidationException e) {

		}
		Mockito.verify(this.messageSource).getMessage("entry.number.should.be.in.range", null, LocaleContextHolder.getLocale());
	}

	@Test
	public void testValidateStartingEntryNoNotNumeric() {

		final int treatmentSize = 10;

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setStartingEntryNo("abc");

		try {
			this.experimentDesignValidator.validateStartingEntryNo(expDesignParameterUi, treatmentSize);
			Assert.fail("validateStartingEntryNo() should throw a DesignValidationException");
		} catch (final DesignValidationException e) {

		}
		Mockito.verify(this.messageSource).getMessage("entry.number.should.be.in.range", null, LocaleContextHolder.getLocale());
	}

	@Test
	public void testValidateStartingEntryNoOutOfRange() {

		final int treatmentSize = 10;

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setStartingEntryNo("100000000");

		try {
			this.experimentDesignValidator.validateStartingEntryNo(expDesignParameterUi, treatmentSize);
			Assert.fail("validateStartingEntryNo() should throw a DesignValidationException");
		} catch (final DesignValidationException e) {

		}
		Mockito.verify(this.messageSource).getMessage("entry.number.should.be.in.range", null, LocaleContextHolder.getLocale());
	}

	@Test
	public void testValidateNumberOfBlocksSuccess() {

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setNumberOfBlocks("1");

		try {
			this.experimentDesignValidator.validateNumberOfBlocks(expDesignParameterUi);
		} catch (final DesignValidationException e) {
			Assert.fail("validateNumberOfBlocks() should not throw a DesignValidationException");
		}

	}

	@Test
	public void testValidateNumberOfBlocksNonNumeric() {

		final ExpDesignParameterUi expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setNumberOfBlocks("");

		try {
			this.experimentDesignValidator.validateNumberOfBlocks(expDesignParameterUi);
			Assert.fail("validateNumberOfBlocks() should throw a DesignValidationException");
		} catch (final DesignValidationException e) {

		}

		Mockito.verify(this.messageSource).getMessage("number.of.blocks.should.be.numeric", null, LocaleContextHolder.getLocale());

	}

	private List<ImportedGermplasm> createImportedGermplasmList() {

		final List<ImportedGermplasm> importedGermplasmList = new LinkedList<>();

		// Create 10 imported germpasm entries
		for (int i = 1; i <= 10; i++) {
			importedGermplasmList.add(ImportedGermplasmMainInfoInitializer.createImportedGermplasm(i));
		}

		return importedGermplasmList;

	}

}
