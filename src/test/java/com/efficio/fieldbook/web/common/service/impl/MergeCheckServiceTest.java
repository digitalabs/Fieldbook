
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;

@Ignore(value ="BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
public class MergeCheckServiceTest extends AbstractBaseIntegrationTest {

	@Resource
	private MergeCheckServiceImpl mergeCheckService;

	private static final int STARTING_GID_1 = 11;
	private static final int STARTING_GID_2 = 51;
	private static final String CHECK_ENTRY = "Check Entry";
	private static final String TEST_ENTRY = "Test Entry";

	private static final String PRIMARY_LIST_DESIG_PREFIX = "Primary";
	private static final String CHECK_DESIG_PREFIX = "C";
	private static final int NUMBER_OF_PRIMARY_LIST = 9;
	private static final int NUMBER_OF_CHECKS = 2;

	@Test
	public void testMergeGermplasmList_ChecksFromSameList() throws Exception {
		List<ImportedGermplasm> primaryList = this.createGermplasmList(MergeCheckServiceTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceTest.NUMBER_OF_PRIMARY_LIST, MergeCheckServiceTest.STARTING_GID_1, MergeCheckServiceTest.TEST_ENTRY);
		List<ImportedGermplasm> checkList = this.createGermplasmList(MergeCheckServiceTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceTest.NUMBER_OF_CHECKS, MergeCheckServiceTest.STARTING_GID_1, MergeCheckServiceTest.CHECK_ENTRY);
		this.removeChecksFromPrimaryList(primaryList, checkList);

		int startIndex = 1;
		int interval = 3;
		int manner = 1;
		List<ImportedGermplasm> newList =
				this.mergeCheckService.mergeGermplasmList(primaryList, checkList, startIndex, interval, manner);
		Assert.assertEquals(11, newList.size());
		Assert.assertTrue(newList.get(0).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(1).getEntryId().equals(primaryList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(2).getEntryId().equals(primaryList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(3).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(4).getEntryId().equals(primaryList.get(2).getEntryId()));
		Assert.assertTrue(newList.get(5).getEntryId().equals(primaryList.get(3).getEntryId()));
		Assert.assertTrue(newList.get(6).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(7).getEntryId().equals(primaryList.get(4).getEntryId()));
		Assert.assertTrue(newList.get(8).getEntryId().equals(primaryList.get(5).getEntryId()));
		Assert.assertTrue(newList.get(9).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(10).getEntryId().equals(primaryList.get(6).getEntryId()));
	}

	private void removeChecksFromPrimaryList(List<ImportedGermplasm> primaryList, List<ImportedGermplasm> checkList) {
		Iterator<ImportedGermplasm> importedGermplasmIterator = primaryList.iterator();
		while (importedGermplasmIterator.hasNext()) {
			ImportedGermplasm importedGermplasm = importedGermplasmIterator.next();
			for (ImportedGermplasm check : checkList) {
				if (importedGermplasm.getGid().equals(check.getGid()) && importedGermplasm.getEntryId().equals(check.getEntryId())) {
					importedGermplasmIterator.remove();
				}
			}
		}
	}

	@Test
	public void testMergeGermplasmList_ChecksFromDiffList() throws Exception {
		List<ImportedGermplasm> primaryList = this.createGermplasmList(MergeCheckServiceTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceTest.NUMBER_OF_PRIMARY_LIST, MergeCheckServiceTest.STARTING_GID_1, MergeCheckServiceTest.TEST_ENTRY);
		List<ImportedGermplasm> checkList = this.createGermplasmList(MergeCheckServiceTest.CHECK_DESIG_PREFIX,
				MergeCheckServiceTest.NUMBER_OF_CHECKS, MergeCheckServiceTest.STARTING_GID_2, MergeCheckServiceTest.CHECK_ENTRY);
		this.updateEntryIdOfChecks(checkList, primaryList.size() + 1);

		int startIndex = 1;
		int interval = 3;
		int manner = 1;
		List<ImportedGermplasm> newList =
				this.mergeCheckService.mergeGermplasmList(primaryList, checkList, startIndex, interval, manner);
		Assert.assertEquals(14, newList.size());
		Assert.assertTrue(newList.get(0).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(1).getEntryId().equals(primaryList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(2).getEntryId().equals(primaryList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(3).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(4).getEntryId().equals(primaryList.get(2).getEntryId()));
		Assert.assertTrue(newList.get(5).getEntryId().equals(primaryList.get(3).getEntryId()));
		Assert.assertTrue(newList.get(6).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(7).getEntryId().equals(primaryList.get(4).getEntryId()));
		Assert.assertTrue(newList.get(8).getEntryId().equals(primaryList.get(5).getEntryId()));
		Assert.assertTrue(newList.get(9).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(10).getEntryId().equals(primaryList.get(6).getEntryId()));
		Assert.assertTrue(newList.get(11).getEntryId().equals(primaryList.get(7).getEntryId()));
		Assert.assertTrue(newList.get(12).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(13).getEntryId().equals(primaryList.get(8).getEntryId()));
	}

	private void updateEntryIdOfChecks(List<ImportedGermplasm> checkList, int startingEntryId) {
		int entryId = startingEntryId;
		for (ImportedGermplasm check : checkList) {
			check.setEntryId(entryId);
			entryId++;
		}
	}

	@Test
	public void testMergeGermplasmListPerPosition_ChecksFromSameList() throws Exception {
		List<ImportedGermplasm> primaryList = this.createGermplasmList(MergeCheckServiceTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceTest.NUMBER_OF_PRIMARY_LIST, MergeCheckServiceTest.STARTING_GID_1, MergeCheckServiceTest.TEST_ENTRY);
		List<ImportedGermplasm> checkList = this.createGermplasmList(MergeCheckServiceTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceTest.NUMBER_OF_CHECKS, MergeCheckServiceTest.STARTING_GID_1, MergeCheckServiceTest.CHECK_ENTRY);
		this.removeChecksFromPrimaryList(primaryList, checkList);
		int startIndex = 1;
		int interval = 3;
		int manner = 2;
		List<ImportedGermplasm> newList =
				this.mergeCheckService.mergeGermplasmList(primaryList, checkList, startIndex, interval, manner);
		Assert.assertEquals(21, newList.size());
		Assert.assertTrue(newList.get(0).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(1).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(2).getEntryId().equals(primaryList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(3).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(4).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(5).getEntryId().equals(primaryList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(6).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(7).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(8).getEntryId().equals(primaryList.get(2).getEntryId()));
		Assert.assertTrue(newList.get(9).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(10).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(11).getEntryId().equals(primaryList.get(3).getEntryId()));
		Assert.assertTrue(newList.get(12).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(13).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(14).getEntryId().equals(primaryList.get(4).getEntryId()));
		Assert.assertTrue(newList.get(15).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(16).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(17).getEntryId().equals(primaryList.get(5).getEntryId()));
		Assert.assertTrue(newList.get(18).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(19).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(20).getEntryId().equals(primaryList.get(6).getEntryId()));
	}

	@Test
	public void testMergeGermplasmListPerPosition_ChecksFromDiffList() throws Exception {
		List<ImportedGermplasm> primaryList = this.createGermplasmList(MergeCheckServiceTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceTest.NUMBER_OF_PRIMARY_LIST, MergeCheckServiceTest.STARTING_GID_1, MergeCheckServiceTest.TEST_ENTRY);
		List<ImportedGermplasm> checkList = this.createGermplasmList(MergeCheckServiceTest.CHECK_DESIG_PREFIX,
				MergeCheckServiceTest.NUMBER_OF_CHECKS, MergeCheckServiceTest.STARTING_GID_2, MergeCheckServiceTest.CHECK_ENTRY);
		this.updateEntryIdOfChecks(checkList, primaryList.size() + 1);

		int startIndex = 1;
		int interval = 3;
		int manner = 2;
		List<ImportedGermplasm> newList =
				this.mergeCheckService.mergeGermplasmList(primaryList, checkList, startIndex, interval, manner);
		Assert.assertEquals(27, newList.size());
		Assert.assertTrue(newList.get(0).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(1).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(2).getEntryId().equals(primaryList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(3).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(4).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(5).getEntryId().equals(primaryList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(6).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(7).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(8).getEntryId().equals(primaryList.get(2).getEntryId()));
		Assert.assertTrue(newList.get(9).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(10).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(11).getEntryId().equals(primaryList.get(3).getEntryId()));
		Assert.assertTrue(newList.get(12).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(13).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(14).getEntryId().equals(primaryList.get(4).getEntryId()));
		Assert.assertTrue(newList.get(15).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(16).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(17).getEntryId().equals(primaryList.get(5).getEntryId()));
		Assert.assertTrue(newList.get(18).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(19).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(20).getEntryId().equals(primaryList.get(6).getEntryId()));
		Assert.assertTrue(newList.get(21).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(22).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(23).getEntryId().equals(primaryList.get(7).getEntryId()));
		Assert.assertTrue(newList.get(24).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(25).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(26).getEntryId().equals(primaryList.get(8).getEntryId()));
	}

	private List<ImportedGermplasm> createGermplasmList(String prefix, int size, int startingGid, String check) {
		List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
		int gid = startingGid;
		for (int i = 0; i < size; i++) {
			int entryId = i + 1;
			ImportedGermplasm germplasm = new ImportedGermplasm(entryId, prefix + entryId, null);
			germplasm.setEntryCode(Integer.toString(entryId));
			germplasm.setGid(Integer.toString(gid));
			germplasm.setEntryTypeValue(check);
			list.add(germplasm);
			gid++;
		}

		return list;
	}

	@Test
	public void testCleanGermplasmList_CheckFromSameList() {
		List<ImportedGermplasm> primaryList = this.createListOfGermplasmList(259, 249, 250, 260, 251, 261, 257, 262, 263, 264, 251, 263,
				262, 249, 250, 260, 259, 261, 264);
		List<ImportedGermplasm> checkList = new ArrayList<ImportedGermplasm>();
		checkList.add(primaryList.get(0));
		checkList.add(primaryList.get(3));
		List<ImportedGermplasm> primaryListWithoutCheckList = this.mergeCheckService.cleanGermplasmList(primaryList, checkList);
		Assert.assertNotNull(primaryListWithoutCheckList);
		Assert.assertEquals(primaryList.size() - checkList.size(), primaryListWithoutCheckList.size());
		Assert.assertFalse(primaryListWithoutCheckList.contains(primaryList.get(0)));
		Assert.assertFalse(primaryListWithoutCheckList.contains(primaryList.get(3)));
	}

	private List<ImportedGermplasm> createListOfGermplasmList(Integer... gids) {
		List<ImportedGermplasm> germplasmList = new ArrayList<ImportedGermplasm>();
		for (int index = 0; index < gids.length; index++) {
			ImportedGermplasm germplasm = new ImportedGermplasm();
			int entryId = index + 1;
			String entryCode = Integer.toString(index + 1);
			String gid = Integer.toString(gids[index]);
			germplasm.setEntryId(entryId);
			germplasm.setEntryCode("" + entryCode);
			germplasm.setGid(gid);
			germplasm.setIndex(index);
			germplasmList.add(germplasm);
		}
		return germplasmList;
	}

	@Test
	public void testUpdatePrimaryListAndChecksBeforeMerge_ChecksFromSameList() {
		boolean isChecksFromSameList = true;
		ImportGermplasmListForm form = this.createImportGermplasmListFormTestData(isChecksFromSameList);
		int expectedNumberOfPrimaryList = form.getImportedGermplasm().size() - form.getImportedCheckGermplasm().size();
		this.mergeCheckService.updatePrimaryListAndChecksBeforeMerge(form);
		Assert.assertEquals(expectedNumberOfPrimaryList, form.getImportedGermplasm().size());
		for (ImportedGermplasm check : form.getImportedCheckGermplasm()) {
			for (ImportedGermplasm germplasmList : form.getImportedGermplasm()) {
				// the entry id of the checks and primary list should not be equal
				Assert.assertFalse(check.getEntryId().equals(germplasmList.getEntryId()));
			}
		}
	}

	@Test
	public void testUpdatePrimaryListAndChecksBeforeMerge_ChecksNotFromSameList() {
		boolean isChecksFromSameList = false;
		ImportGermplasmListForm form = this.createImportGermplasmListFormTestData(isChecksFromSameList);
		int expectedNumberOfPrimaryList = form.getImportedGermplasm().size();
		this.mergeCheckService.updatePrimaryListAndChecksBeforeMerge(form);
		Assert.assertEquals(expectedNumberOfPrimaryList, form.getImportedGermplasm().size());
		int entryId = expectedNumberOfPrimaryList + 1;
		for (ImportedGermplasm check : form.getImportedCheckGermplasm()) {
			for (ImportedGermplasm germplasmList : form.getImportedGermplasm()) {
				// the entry id of the checks and primary list should not be equal
				Assert.assertFalse(check.getEntryId().equals(germplasmList.getEntryId()));
			}
			Assert.assertTrue(check.getEntryId().equals(entryId));
			entryId++;
		}
	}

	private ImportGermplasmListForm createImportGermplasmListFormTestData(boolean isChecksFromSameList) {
		ImportGermplasmListForm form = new ImportGermplasmListForm();
		form.setImportedGermplasm(this.createGermplasmList(MergeCheckServiceTest.PRIMARY_LIST_DESIG_PREFIX, 9,
				MergeCheckServiceTest.STARTING_GID_1, MergeCheckServiceTest.TEST_ENTRY));
		if (isChecksFromSameList) {
			form.setImportedCheckGermplasm(this.createGermplasmList(MergeCheckServiceTest.PRIMARY_LIST_DESIG_PREFIX, 3,
					MergeCheckServiceTest.STARTING_GID_1, MergeCheckServiceTest.CHECK_ENTRY));
			form.setLastDraggedChecksList("0");
		} else {
			form.setImportedCheckGermplasm(this.createGermplasmList(MergeCheckServiceTest.CHECK_DESIG_PREFIX, 3,
					MergeCheckServiceTest.STARTING_GID_2, MergeCheckServiceTest.CHECK_ENTRY));
			form.setLastDraggedChecksList(null);
		}

		return form;
	}
}
