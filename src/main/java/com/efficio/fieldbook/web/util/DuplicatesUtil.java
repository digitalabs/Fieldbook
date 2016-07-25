
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;

public class DuplicatesUtil {

	public static final String SEPARATOR = "/";

	private DuplicatesUtil() {
		// private constructor for utility class
	}

	public static void processDuplicates(ImportedCrossesList parseResults) {
		if (parseResults != null) {
			DuplicatesUtil.detectDuplicationsAndReciprocalsFromImportedCrosses(parseResults);
		}
	}

	private static void setDuplicatePrefixAndEntriesForDuplicates(List<ImportedCrosses> importedCrossesList, String prefix) {
		List<Integer> entries = new ArrayList<>();
		DuplicatesUtil.getAllEntries(importedCrossesList, entries);
		for (ImportedCrosses importedCrosses : importedCrossesList) {
			if (DuplicatesUtil.canStillSetDuplicateNotes(importedCrosses, prefix)) {
				importedCrosses.setDuplicatePrefix(prefix);
				DuplicatesUtil.setDuplicateEntries(importedCrosses, entries);
			}
		}
	}

	private static boolean canStillSetDuplicateNotes(ImportedCrosses importedCrosses, String prefix) {
		boolean canStillSetDuplicateNotes = false;
		if (importedCrosses.getDuplicatePrefix() == null) {
			canStillSetDuplicateNotes = true;
		} else if (importedCrosses.isPlotDupe() && ImportedCrosses.PLOT_DUPE_PREFIX.equals(prefix)) {
			canStillSetDuplicateNotes = true;
		} else if (importedCrosses.isPedigreeDupe() && ImportedCrosses.PEDIGREE_DUPE_PREFIX.equals(prefix)) {
			canStillSetDuplicateNotes = true;
		} else if (importedCrosses.isPlotRecip() && ImportedCrosses.PLOT_RECIP_PREFIX.equals(prefix)) {
			canStillSetDuplicateNotes = true;
		} else if (importedCrosses.isPedigreeRecip() && ImportedCrosses.PEDIGREE_RECIP_PREFIX.equals(prefix)) {
			canStillSetDuplicateNotes = true;
		}
		return canStillSetDuplicateNotes;
	}

	private static void setDuplicateNotesBasedOnPrefixandEntries(ImportedCrosses importedCrosses) {
		if (importedCrosses.getDuplicatePrefix() != null) {
			importedCrosses.setDuplicate(importedCrosses.getDuplicatePrefix()
					+ DuplicatesUtil.getCommaSeparatedEntryIdsOfDuplicates(importedCrosses.getDuplicateEntries()));
		}
	}

	private static void setDuplicateEntries(ImportedCrosses importedCrosses, Collection<Integer> entries) {
		if (importedCrosses.getDuplicateEntries() == null) {
			importedCrosses.setDuplicateEntries(new TreeSet<Integer>());
		}
		for (Integer entryId : entries) {
			if (importedCrosses.getEntryId().equals(entryId)) {
				continue;
			}
			importedCrosses.getDuplicateEntries().add(entryId);
		}
	}

	private static String getCommaSeparatedEntryIdsOfDuplicates(Collection<Integer> entries) {
		String entryIDCSV = "";
		for (Integer entryId : entries) {
			entryIDCSV += entryId + ", ";
		}
		return entryIDCSV.substring(0, entryIDCSV.length() - 2);
	}

	private static void getAllEntries(Collection<ImportedCrosses> importedCrossesList, List<Integer> entries) {
		for (ImportedCrosses importedCrosses : importedCrossesList) {
			entries.add(importedCrosses.getEntryId());
		}
	}

	private static void detectDuplicationsAndReciprocalsFromImportedCrosses(ImportedCrossesList importedCrossesList) {
		for (ImportedCrosses importedCrossesMain : importedCrossesList.getImportedCrosses()) {
			if (importedCrossesMain.getDuplicate() != null) {
				continue;
			}

			final String nFemalePlotNo = importedCrossesMain.getFemalePlotNo();
			final String nMalePlotNo = importedCrossesMain.getMalePlotNo();
			final String nFemaleGid = importedCrossesMain.getFemaleGid();
			final String nMaleGid = importedCrossesMain.getMaleGid();

			for (ImportedCrosses possibleDuplicatesAndReciprocals : importedCrossesList.getImportedCrosses()) {
				if (!Objects.equals(importedCrossesMain.getEntryId(), possibleDuplicatesAndReciprocals.getEntryId())) {

					final String femaleGidExcludingMain = possibleDuplicatesAndReciprocals.getFemaleGid();
					final String maleGidExcludingMain = possibleDuplicatesAndReciprocals.getMaleGid();
					final String femalePlotNoExcludingMain = possibleDuplicatesAndReciprocals.getFemalePlotNo();
					final String malePlotNoExcludingMain = possibleDuplicatesAndReciprocals.getFemalePlotNo();

					// Duplicate scenario
					if (femaleGidExcludingMain.equals(maleGidExcludingMain)) {
						if (Objects.equals(femalePlotNoExcludingMain, malePlotNoExcludingMain)) {
							// Plot Dupe
							DuplicatesUtil.setDuplicatePrefixAndEntriesForDuplicates(Lists.newArrayList(possibleDuplicatesAndReciprocals),
									ImportedCrosses.PLOT_DUPE_PREFIX);
						} else {
							// Pedigree Dupe
							DuplicatesUtil.setDuplicatePrefixAndEntriesForDuplicates(Lists.newArrayList(possibleDuplicatesAndReciprocals),
									ImportedCrosses.PEDIGREE_DUPE_PREFIX);
						}
						if (importedCrossesMain.getDuplicateEntries() == null) {
							importedCrossesMain.setDuplicateEntries(new TreeSet<Integer>());
						}
						importedCrossesMain.getDuplicateEntries().add(possibleDuplicatesAndReciprocals.getEntryId());
						DuplicatesUtil.setDuplicateNotesBasedOnPrefixandEntries(importedCrossesMain);
					}
					// Reciprocal scenario
					if (Objects.equals(femaleGidExcludingMain, nMaleGid) && Objects.equals(maleGidExcludingMain, nFemaleGid)) {
						if (femalePlotNoExcludingMain.equals(nMalePlotNo) && malePlotNoExcludingMain.equals(nFemalePlotNo)) {
							// Plot Reciprocal
							List<Integer> plotReciprocalEntries = new ArrayList<>();
							DuplicatesUtil.getAllEntries(Lists.newArrayList(possibleDuplicatesAndReciprocals), plotReciprocalEntries);
							importedCrossesMain.setDuplicatePrefix(ImportedCrosses.PLOT_RECIP_PREFIX);
							DuplicatesUtil.setDuplicateEntries(possibleDuplicatesAndReciprocals, plotReciprocalEntries);
						} else {
							// Pedigree Reciprocal
							List<Integer> pedigreeReciprocalEntries = new ArrayList<>();
							DuplicatesUtil.getAllEntries(Lists.newArrayList(possibleDuplicatesAndReciprocals), pedigreeReciprocalEntries);
							importedCrossesMain.setDuplicatePrefix(ImportedCrosses.PEDIGREE_RECIP_PREFIX);
							DuplicatesUtil.setDuplicateEntries(possibleDuplicatesAndReciprocals, pedigreeReciprocalEntries);
						}
						if (importedCrossesMain.getDuplicateEntries() == null) {
							importedCrossesMain.setDuplicateEntries(new TreeSet<Integer>());
						}
						importedCrossesMain.getDuplicateEntries().add(possibleDuplicatesAndReciprocals.getEntryId());
						DuplicatesUtil.setDuplicateNotesBasedOnPrefixandEntries(importedCrossesMain);
					}
				}
			}
		}
	}
}
