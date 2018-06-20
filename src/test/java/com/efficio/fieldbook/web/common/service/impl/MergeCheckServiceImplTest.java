package com.efficio.fieldbook.web.common.service.impl;

import java.util.List;

import junit.framework.Assert;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.google.common.collect.Lists;


public class MergeCheckServiceImplTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	/**
	 * This test makes sure that the checks are correctly numbered if their is no start entry numbers.
	 */
	@Test
	public void testCheckEntryNumberingWithoutAnyStartEntryNumber() throws Exception {
		List<ImportedGermplasm> importedCheckGermplasm = runUpdatePrimaryListAndChecksBeforeMerge(null);
		Assert.assertEquals(new Integer(3), importedCheckGermplasm.get(0).getEntryId());
		Assert.assertEquals(new Integer(4), importedCheckGermplasm.get(1).getEntryId());
	}

	/**
	 * This test makes sure that the checks are correctly numbered if their is no start entry numbers.
	 */
	@Test
	public void testCheckEntryNumberingWithStartEntryNumber() throws Exception {
		List<ImportedGermplasm> importedCheckGermplasm = runUpdatePrimaryListAndChecksBeforeMerge("8");
		Assert.assertEquals(new Integer(10), importedCheckGermplasm.get(0).getEntryId());
		Assert.assertEquals(new Integer(11), importedCheckGermplasm.get(1).getEntryId());
	}

	private List<ImportedGermplasm> runUpdatePrimaryListAndChecksBeforeMerge(final String startEntryNumber) {
		final MergeCheckServiceImpl mergeCheckServiceImpl = new MergeCheckServiceImpl();
		final ImportGermplasmListForm importGermplasmListForm = new ImportGermplasmListForm();
		importGermplasmListForm.setStartingEntryNo(startEntryNumber);
		importGermplasmListForm.setLastDraggedChecksList("10");
		importGermplasmListForm.setImportedGermplasm(Lists.newArrayList(new ImportedGermplasm(1, "desig", "check"), new ImportedGermplasm(2, "desig", "check")));
		importGermplasmListForm.setImportedCheckGermplasm(Lists.newArrayList(new ImportedGermplasm(1, "desig", "check"), new ImportedGermplasm(2, "desig", "check")));
		mergeCheckServiceImpl.updatePrimaryListAndChecksBeforeMerge(importGermplasmListForm);
		importGermplasmListForm.getImportedGermplasm();
		List<ImportedGermplasm> importedCheckGermplasm = importGermplasmListForm.getImportedCheckGermplasm();
		return importedCheckGermplasm;
	}




}
