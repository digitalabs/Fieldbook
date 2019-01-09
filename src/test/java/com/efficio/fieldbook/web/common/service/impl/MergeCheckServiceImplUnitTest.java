package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MergeCheckServiceImplUnitTest {

	@InjectMocks
	private MergeCheckServiceImpl mergeCheckService;

	private static final int STARTING_GID_1 = 100;
	private static final int STARTING_GID_2 = 200;
	private static final String CHECK_ENTRY = "Check Entry";
	private static final String TEST_ENTRY = "Test Entry";

	private static final String PRIMARY_LIST_DESIG_PREFIX = "Primary";
	private static final int NUMBER_OF_PRIMARY_LIST = 9;
	private static final int NUMBER_OF_CHECKS = 3;

	/**
	 * This test will merge germplasm list and checks with valid data and will generate valid merge list.
	 */
	@Test
	public void testMergeGermplasmList(){

		List<ImportedGermplasm> primaryList = this.createGermplasmList(MergeCheckServiceImplUnitTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceImplUnitTest.NUMBER_OF_PRIMARY_LIST, MergeCheckServiceImplUnitTest.STARTING_GID_1, MergeCheckServiceImplUnitTest.TEST_ENTRY);
		List<ImportedGermplasm> checkList = this.createGermplasmList(MergeCheckServiceImplUnitTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceImplUnitTest.NUMBER_OF_CHECKS, MergeCheckServiceImplUnitTest.STARTING_GID_2, MergeCheckServiceImplUnitTest.CHECK_ENTRY);
		this.removeChecksFromPrimaryList(primaryList, checkList);

		int startIndex = 1;
		int interval = 3;
		int manner = 1;

		List<ImportedGermplasm> newList =
				this.mergeCheckService.mergeGermplasmList(primaryList, checkList, startIndex, interval, manner);
		Assert.assertEquals(12, newList.size());
		Assert.assertTrue(newList.get(0).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(1).getEntryId().equals(primaryList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(2).getEntryId().equals(primaryList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(3).getEntryId().equals(primaryList.get(2).getEntryId()));
		Assert.assertTrue(newList.get(4).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(5).getEntryId().equals(primaryList.get(3).getEntryId()));
		Assert.assertTrue(newList.get(6).getEntryId().equals(primaryList.get(4).getEntryId()));
		Assert.assertTrue(newList.get(7).getEntryId().equals(primaryList.get(5).getEntryId()));
		Assert.assertTrue(newList.get(8).getEntryId().equals(checkList.get(2).getEntryId()));
		Assert.assertTrue(newList.get(9).getEntryId().equals(primaryList.get(6).getEntryId()));
		Assert.assertTrue(newList.get(10).getEntryId().equals(primaryList.get(7).getEntryId()));
		Assert.assertTrue(newList.get(11).getEntryId().equals(primaryList.get(8).getEntryId()));
	}

	/**
	 * This test will pass empty check list. This will return primary list as it is as there is nothing to merge.
	 */
	@Test
	public void testMergeGermplasmListWithEmptyCheckList(){

		List<ImportedGermplasm> primaryList = this.createGermplasmList(MergeCheckServiceImplUnitTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceImplUnitTest.NUMBER_OF_PRIMARY_LIST, MergeCheckServiceImplUnitTest.STARTING_GID_1, MergeCheckServiceImplUnitTest.TEST_ENTRY);
		List<ImportedGermplasm> checkList = new ArrayList<>();

		int startIndex = 1;
		int interval = 3;
		int manner = 1;

		List<ImportedGermplasm> newList =
				this.mergeCheckService.mergeGermplasmList(primaryList, checkList, startIndex, interval, manner);

		Assert.assertEquals(primaryList.size(), newList.size());
		Assert.assertTrue(newList.get(0).getEntryId().equals(primaryList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(1).getEntryId().equals(primaryList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(2).getEntryId().equals(primaryList.get(2).getEntryId()));
		Assert.assertTrue(newList.get(3).getEntryId().equals(primaryList.get(3).getEntryId()));
		Assert.assertTrue(newList.get(4).getEntryId().equals(primaryList.get(4).getEntryId()));
		Assert.assertTrue(newList.get(5).getEntryId().equals(primaryList.get(5).getEntryId()));
		Assert.assertTrue(newList.get(6).getEntryId().equals(primaryList.get(6).getEntryId()));
		Assert.assertTrue(newList.get(7).getEntryId().equals(primaryList.get(7).getEntryId()));
		Assert.assertTrue(newList.get(8).getEntryId().equals(primaryList.get(8).getEntryId()));
	}

	/**
	 * This test will merge primary list and check list with extra added default check Id.
	 */
	@Test
	public void testMergeGermplasmListWithDefaultCheckId(){

		List<ImportedGermplasm> primaryList = this.createGermplasmList(MergeCheckServiceImplUnitTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceImplUnitTest.NUMBER_OF_PRIMARY_LIST, MergeCheckServiceImplUnitTest.STARTING_GID_1, MergeCheckServiceImplUnitTest.TEST_ENTRY);
		List<ImportedGermplasm> checkList = this.createGermplasmList(MergeCheckServiceImplUnitTest.PRIMARY_LIST_DESIG_PREFIX,
				MergeCheckServiceImplUnitTest.NUMBER_OF_CHECKS, MergeCheckServiceImplUnitTest.STARTING_GID_2, MergeCheckServiceImplUnitTest.CHECK_ENTRY);
		this.removeChecksFromPrimaryList(primaryList, checkList);

		int startIndex = 1;
		int interval = 3;
		int manner = 1;

		List<ImportedGermplasm> newList =
				this.mergeCheckService.mergeGermplasmList(primaryList, checkList, startIndex, interval, manner);
		Assert.assertEquals(12, newList.size());
		Assert.assertTrue(newList.get(0).getEntryId().equals(checkList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(1).getEntryId().equals(primaryList.get(0).getEntryId()));
		Assert.assertTrue(newList.get(2).getEntryId().equals(primaryList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(3).getEntryId().equals(primaryList.get(2).getEntryId()));
		Assert.assertTrue(newList.get(4).getEntryId().equals(checkList.get(1).getEntryId()));
		Assert.assertTrue(newList.get(5).getEntryId().equals(primaryList.get(3).getEntryId()));
		Assert.assertTrue(newList.get(6).getEntryId().equals(primaryList.get(4).getEntryId()));
		Assert.assertTrue(newList.get(7).getEntryId().equals(primaryList.get(5).getEntryId()));
		Assert.assertTrue(newList.get(8).getEntryId().equals(checkList.get(2).getEntryId()));
		Assert.assertTrue(newList.get(9).getEntryId().equals(primaryList.get(6).getEntryId()));
		Assert.assertTrue(newList.get(10).getEntryId().equals(primaryList.get(7).getEntryId()));
		Assert.assertTrue(newList.get(11).getEntryId().equals(primaryList.get(8).getEntryId()));
	}

	private List<ImportedGermplasm> createGermplasmList(String prefix, int size, int startingGid, String check) {
		List<ImportedGermplasm> list = new ArrayList<>();
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

}
