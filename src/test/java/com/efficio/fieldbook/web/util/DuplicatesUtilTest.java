
package com.efficio.fieldbook.web.util;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.middleware.util.Debug;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class DuplicatesUtilTest {

	/**
	 * Test to verify processDuplicates() method works as expected or not
	 * This will build required test data and assert each imported crosses.
	 * Each Imported crosses should have valid duplicate string.
	 * Ex. For EntryId=1, Plot Dupe: 15 | Pedigree Dupe: 12, 13 | Pedigree Recip: 10, 14
	 * 
	 * SEE src/test/resources/DowntonDupesRecipsResults.png for explanation of test case
	 */
	@Test
	public void testProcessDuplicates() {
		Map<String, String> plotNoToGidTestData = this.createPlotNoToGidTestData();
		ImportedCrossesList parseResults = this.createImportedCrossesListTestData(plotNoToGidTestData);

		Debug.println("BEFORE: ");
		this.debugTestData(parseResults);
		DuplicatesUtil.processDuplicatesAndReciprocals(parseResults);
		Debug.println("AFTER: ");
		this.debugTestData(parseResults);
		int entryId = 1;
		final String pipeCharacter = " | ";
		for (ImportedCrosses importedCrosses : parseResults.getImportedCrosses()) {
			switch (entryId) {
				case 1:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "2" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "5" + pipeCharacter +
							ImportedCrosses.PLOT_RECIP_PREFIX + "3", importedCrosses.getDuplicate());
					break;
				case 2:
					Assert.assertEquals(ImportedCrosses.PLOT_DUPE_PREFIX + "1" + pipeCharacter + ImportedCrosses.PEDIGREE_DUPE_PREFIX + "5" + pipeCharacter +
							ImportedCrosses.PLOT_RECIP_PREFIX + "3", importedCrosses.getDuplicate());
				break;
				case 3:
					Assert.assertEquals(ImportedCrosses.PLOT_RECIP_PREFIX + "1, 2"+ pipeCharacter + ImportedCrosses.PEDIGREE_RECIP_PREFIX + "5", importedCrosses.getDuplicate());
				break;
				case 4:
					Assert.assertEquals(ImportedCrosses.PEDIGREE_DUPE_PREFIX + "6" + pipeCharacter + ImportedCrosses.PEDIGREE_RECIP_PREFIX + "7", importedCrosses.getDuplicate());
					break;
				case 5:
					Assert.assertEquals(ImportedCrosses.PEDIGREE_DUPE_PREFIX + "1, 2" + pipeCharacter + ImportedCrosses.PEDIGREE_RECIP_PREFIX + "3", importedCrosses.getDuplicate());
					break;
				case 6:
					Assert.assertEquals(ImportedCrosses.PEDIGREE_DUPE_PREFIX + "4" + pipeCharacter + ImportedCrosses.PLOT_RECIP_PREFIX + "7", importedCrosses.getDuplicate());
					break;
				case 7:
					Assert.assertEquals(ImportedCrosses.PLOT_RECIP_PREFIX + "6" + pipeCharacter + ImportedCrosses.PEDIGREE_RECIP_PREFIX + "4", importedCrosses.getDuplicate());
					break;

				default:
					Assert.assertEquals(ImportedCrosses.PEDIGREE_DUPE_PREFIX + "12, 13", importedCrosses.getDuplicate());
			}
			entryId++;
		}
	}

	/**
	 * Create test data using Map which builds Plot Number and its Gid data
	 * @return plotNoToGidTestData which contains plot number and its gid data
	 */
	private Map<String, String> createPlotNoToGidTestData() {
		Map<String, String> plotNoToGidTestData = new HashMap<String, String>();
		plotNoToGidTestData.put("1", "Cora");
		plotNoToGidTestData.put("8", "MrCarson");
		plotNoToGidTestData.put("11", "LadyMary");
		plotNoToGidTestData.put("16", "MrCarson");
		return plotNoToGidTestData;
	}

	/**
	 * Make ImportedCrossesList which contains information about crosses
	 * This will build female and male gid, study name, female and male designation
	 * @param plotNoToGidTestData used to get gid from plot number
	 * @return importedCrossesList
	 */
	private ImportedCrossesList createImportedCrossesListTestData(Map<String, String> plotNoToGidTestData) {
		ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		for (int i = 1; i <= 7; i++) {
			String femalePlotNo = Integer.toString(i);
			String malePlotNo = Integer.toString(i);
			switch (i) {
				case 1:
					femalePlotNo = "16";
					malePlotNo = "1";
					break;
				case 2:
					femalePlotNo = "16";
					malePlotNo = "1";
					break;
				case 3:
					femalePlotNo = "1";
					malePlotNo = "16";
					break;
				case 4:
					femalePlotNo = "16";
					malePlotNo = "11";
					break;
				case 5:
					femalePlotNo = "8";
					malePlotNo = "1";
					break;
				case 6:
					femalePlotNo = "8";
					malePlotNo = "11";
					break;
				case 7:
					femalePlotNo = "11";
					malePlotNo = "8";
					break;

			}
			String femaleGid = plotNoToGidTestData.get(femalePlotNo);
			String maleGid = plotNoToGidTestData.get(malePlotNo);
			String femaleStudyName = "FNursery";
			String maleStudyName = "MNursery";
			String femaleDesig = femaleGid;
			String maleDesig = maleGid;
			String source = femaleStudyName + ":" + i + " " + DuplicatesUtil.SEPARATOR + " " + maleStudyName + ":" + (i + 10);
			importedCrossesList.addImportedCrosses(this.createImportedCrossesTestData(femaleDesig, maleDesig, femaleGid, maleGid, i,
					source, femalePlotNo, malePlotNo));
		}
		return importedCrossesList;
	}

	private void debugTestData(ImportedCrossesList importedCrossesList) {
		for (ImportedCrosses importedCrosses : importedCrossesList.getImportedCrosses()) {
			Debug.println(5, this.reviewImportedCrosses(importedCrosses));
		}
	}

	private String reviewImportedCrosses(ImportedCrosses importedCrosses) {
		return "ENTRY=" + importedCrosses.getEntryId() + "\t" + "FEMALE_PLOTNO=" + importedCrosses.getFemalePlotNo() + "\t"
				+ "MALE_PLOTNO=" + importedCrosses.getMalePlotNos().get(0) + "\t" + "FEMALE_GID=" + importedCrosses.getFemaleGid() + "\t"
				+ "MALE_GID=" + importedCrosses.getMaleGids().get(0) + "\t" + "PARENTAGE=" + importedCrosses.getCross() + "\t" + "DUPLICATE="
				+ importedCrosses.getDuplicate();
	}

	/**
	 * Method to set data in Imported Crosses
	 * @param femaleDesig Female Designation
	 * @param maleDesig Male Designation
	 * @param femaleGid Female Gid
	 * @param maleGid Male Gid
	 * @param entryId Entry Id
	 * @param source source
	 * @param femalePlotNo Female Plot Number
	 * @param malePlotNo Male Plot Number
	 * @return importedCrosses
	 */
	private ImportedCrosses createImportedCrossesTestData(String femaleDesig, String maleDesig, String femaleGid, String maleGid,
			Integer entryId, String source, String femalePlotNo, String malePlotNo) {
		ImportedCrosses importedCrosses = new ImportedCrosses();
		
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(Integer.valueOf(femaleGid), femaleDesig, "");
		femaleParent.setPlotNo(femalePlotNo);
		importedCrosses.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(Integer.valueOf(maleGid), maleDesig, "");
		maleParent.setPlotNo(malePlotNo);
		importedCrosses.setMaleParents(Lists.newArrayList(maleParent));
		importedCrosses.setEntryId(entryId);
		importedCrosses.setCross(importedCrosses.getFemaleDesignation() + DuplicatesUtil.SEPARATOR + importedCrosses.getMaleDesignationsAsString());
		importedCrosses.setSource(source);
		importedCrosses.setEntryCode(String.valueOf(entryId));
		return importedCrosses;
	}
}
