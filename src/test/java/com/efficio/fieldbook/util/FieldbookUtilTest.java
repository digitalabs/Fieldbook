
package com.efficio.fieldbook.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.pojos.ListDataProject;
import org.junit.Test;

public class FieldbookUtilTest {

	@Test
	public void testGetColumnOrderListIfThereAreParameters() {
		String columnOrderDelimited = "[\"1100\", \"1900\"]";
		List<Integer> columnOrderList = FieldbookUtil.getColumnOrderList(columnOrderDelimited);
		Assert.assertEquals("Should have 2 integer list", 2, columnOrderList.size());
	}

	@Test
	public void testGetColumnOrderListIfThereAreNoParameters() {
		String columnOrderDelimited = "[ ]";
		List<Integer> columnOrderList = FieldbookUtil.getColumnOrderList(columnOrderDelimited);
		Assert.assertEquals("Should have 0 integer list", 0, columnOrderList.size());
	}

	@Test
	public void testSetColumnOrderingOnWorkbook() {
		Workbook workbook = new Workbook();
		String columnOrderDelimited = "[\"1100\", \"1900\"]";
		FieldbookUtil.setColumnOrderingOnWorkbook(workbook, columnOrderDelimited);
		List<Integer> orderedTermIds = workbook.getColumnOrderedLists();
		Assert.assertEquals("1st element should have term id 1100", 1100, orderedTermIds.get(0).intValue());
		Assert.assertEquals("2nd element should have term id 1900", 1900, orderedTermIds.get(1).intValue());
	}

	@Test
	public void testIsPlotDuplicateNonFirstInstanceIfNotFirstInstance() {
		ImportedCrosses crosses = new ImportedCrosses();
		crosses.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses.setEntryId(6);
		Set<Integer> dupeEntries = new TreeSet<Integer>();
		dupeEntries.add(5);
		dupeEntries.add(7);
		crosses.setDuplicateEntries(dupeEntries);
		Assert.assertTrue("Should return true since its not the first instance", FieldbookUtil.isPlotDuplicateNonFirstInstance(crosses));
	}

	@Test
	public void testisPlotDuplicateNonFirstInstanceIfFirstInstance() {
		ImportedCrosses crosses = new ImportedCrosses();
		crosses.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses.setEntryId(2);
		Set<Integer> dupeEntries = new TreeSet<Integer>();
		dupeEntries.add(5);
		dupeEntries.add(7);
		crosses.setDuplicateEntries(dupeEntries);
		Assert.assertFalse("Should return false since its not the first instance", FieldbookUtil.isPlotDuplicateNonFirstInstance(crosses));
	}

	@Test
	public void testisPlotDuplicateNonFirstInstanceIfPedigreeDupe() {
		ImportedCrosses crosses = new ImportedCrosses();
		crosses.setDuplicatePrefix(ImportedCrosses.PEDIGREE_DUPE_PREFIX);
		crosses.setEntryId(2);
		Set<Integer> dupeEntries = new TreeSet<Integer>();
		dupeEntries.add(5);
		dupeEntries.add(7);
		crosses.setDuplicateEntries(dupeEntries);
		Assert.assertFalse("Should return false since its a pedigree dupe", FieldbookUtil.isPlotDuplicateNonFirstInstance(crosses));
	}

	@Test
	public void testisPlotDuplicateNonFirstInstanceIfPlotDupeButNoLitOfDuplicate() {
		ImportedCrosses crosses = new ImportedCrosses();
		crosses.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses.setEntryId(2);
		crosses.setDuplicateEntries(null);
		Assert.assertFalse("Should return false since its it has no list of duplicate entries",
				FieldbookUtil.isPlotDuplicateNonFirstInstance(crosses));
	}

	@Test
	public void testMergeCrossesPlotDuplicateDataIfThereIsDuplicate() {
		ImportedCrosses crosses1 = new ImportedCrosses();
		crosses1.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses1.setEntryId(1);
		ImportedCrosses crosses2 = new ImportedCrosses();
		crosses2.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses2.setEntryId(2);
		crosses2.setGid("11223");
		crosses2.setCross("Cross 11223");
		ImportedCrosses crosses3 = new ImportedCrosses();
		crosses3.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses3.setEntryId(3);
		Set<Integer> dupeEntries = new TreeSet<Integer>();
		dupeEntries.add(2);
		dupeEntries.add(7);
		crosses3.setDuplicateEntries(dupeEntries);
		List<ImportedCrosses> importedCrossesList = new ArrayList<ImportedCrosses>();
		importedCrossesList.add(crosses1);
		importedCrossesList.add(crosses2);
		importedCrossesList.add(crosses3);
		FieldbookUtil.mergeCrossesPlotDuplicateData(crosses3, importedCrossesList);
		Assert.assertEquals("Gid should be the same as crosses 2 since its the duplicate", crosses2.getGid(), crosses3.getGid());
		Assert.assertEquals("Cross should be the same as crosses 2 since its the duplicate", crosses2.getCross(), crosses3.getCross());
	}

	@Test
	public void testMergeCrossesPlotDuplicateDataIfThereIsNoDuplicate() {
		ImportedCrosses crosses1 = new ImportedCrosses();
		crosses1.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses1.setEntryId(1);
		ImportedCrosses crosses2 = new ImportedCrosses();
		crosses2.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses2.setEntryId(2);
		crosses2.setGid("11223");
		crosses2.setCross("Cross 11223");
		ImportedCrosses crosses3 = new ImportedCrosses();
		crosses3.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses3.setEntryId(3);
		crosses3.setGid("555");
		crosses3.setDuplicateEntries(null);
		List<ImportedCrosses> importedCrossesList = new ArrayList<ImportedCrosses>();
		importedCrossesList.add(crosses1);
		importedCrossesList.add(crosses2);
		importedCrossesList.add(crosses3);
		FieldbookUtil.mergeCrossesPlotDuplicateData(crosses3, importedCrossesList);
		Assert.assertEquals("Gid should still be the same since there is no duplicate", "555", crosses3.getGid());
	}

	@Test
	public void testIsContinueCrossingMergeWhenMergeConditionIsFalse() {
		ImportedCrosses crosses3 = new ImportedCrosses();
		crosses3.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses3.setEntryId(3);
		Set<Integer> dupeEntries = new TreeSet<Integer>();
		dupeEntries.add(4);
		dupeEntries.add(7);
		crosses3.setDuplicateEntries(dupeEntries);
		Assert.assertFalse("Should return false since there is no plot duplicate",
				FieldbookUtil.isContinueCrossingMerge(false, false, crosses3));
		Assert.assertFalse("Should return false since there is preserve plot duplicate true",
				FieldbookUtil.isContinueCrossingMerge(true, true, crosses3));
		Assert.assertFalse("Should return false since duplicate entries is non first instance",
				FieldbookUtil.isContinueCrossingMerge(true, true, crosses3));
	}

	@Test
	public void testIsContinueCrossingMergeWhenMergeConditionIsTrue() {
		ImportedCrosses crosses3 = new ImportedCrosses();
		crosses3.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		crosses3.setEntryId(3);
		Set<Integer> dupeEntries = new TreeSet<Integer>();
		dupeEntries.add(2);
		dupeEntries.add(7);
		crosses3.setDuplicateEntries(dupeEntries);
		Assert.assertTrue("Should return true since there is plot duplicate, preserve plot duplicate is false and non first instance",
				FieldbookUtil.isContinueCrossingMerge(true, false, crosses3));
	}

	@Test
	public void testCopyDupeNotesToListDataProjectIfSameSize() {
		ImportedCrosses crosses1 = new ImportedCrosses();
		crosses1.setDuplicate("Dupe 1");
		ImportedCrosses crosses2 = new ImportedCrosses();
		crosses2.setDuplicate("Dupe 2");
		ImportedCrosses crosses3 = new ImportedCrosses();
		crosses3.setDuplicate("Dupe 3");
		List<ImportedCrosses> importedCrossesList = new ArrayList<ImportedCrosses>();
		importedCrossesList.add(crosses1);
		importedCrossesList.add(crosses2);
		importedCrossesList.add(crosses3);
		List<ListDataProject> listDataProject = new ArrayList<ListDataProject>();
		listDataProject.add(new ListDataProject());
		listDataProject.add(new ListDataProject());
		listDataProject.add(new ListDataProject());
		FieldbookUtil.copyDupeNotesToListDataProject(listDataProject, importedCrossesList);
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals("Should have copied the duplicate string from the original pojo",
					importedCrossesList.get(i).getDuplicate(), listDataProject.get(i).getDuplicate());
		}
	}

	@Test
	public void testCopyDupeNotesToListDataProjectIfNotSameSize() {
		ImportedCrosses crosses1 = new ImportedCrosses();
		crosses1.setDuplicate("Dupe 1");
		ImportedCrosses crosses2 = new ImportedCrosses();
		crosses2.setDuplicate("Dupe 2");
		ImportedCrosses crosses3 = new ImportedCrosses();
		crosses3.setDuplicate("Dupe 3");
		List<ImportedCrosses> importedCrossesList = new ArrayList<ImportedCrosses>();
		importedCrossesList.add(crosses1);
		importedCrossesList.add(crosses2);
		importedCrossesList.add(crosses3);
		List<ListDataProject> listDataProject = new ArrayList<ListDataProject>();
		listDataProject.add(new ListDataProject());
		listDataProject.add(new ListDataProject());

		FieldbookUtil.copyDupeNotesToListDataProject(listDataProject, importedCrossesList);
		for (int i = 0; i < listDataProject.size(); i++) {
			Assert.assertNull("Should have null for the duplicate since it was not copied", listDataProject.get(i).getDuplicate());
		}
	}

	@Test
	public void testCopyDupeNotesToListDataProjectIfCrossesListIsNull() {
		List<ListDataProject> listDataProject = new ArrayList<ListDataProject>();
		listDataProject.add(new ListDataProject());
		listDataProject.add(new ListDataProject());

		FieldbookUtil.copyDupeNotesToListDataProject(listDataProject, null);
		for (int i = 0; i < listDataProject.size(); i++) {
			Assert.assertNull("Should have null for the duplicate since it was not copied", listDataProject.get(i).getDuplicate());
		}
	}
}
