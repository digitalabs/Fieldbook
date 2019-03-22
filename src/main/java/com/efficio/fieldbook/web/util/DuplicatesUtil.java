
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DuplicatesUtil {

	public static final String SEPARATOR = "/";
	
	public static final Logger LOG = LoggerFactory.getLogger(DuplicatesUtil.class);

	private DuplicatesUtil() {
		// private constructor for utility class
	}

	public static void processDuplicatesAndReciprocals(ImportedCrossesList parseResults) {
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
		return importedCrosses.getDuplicatePrefix() == null
				|| (importedCrosses.isPlotDupe() && ImportedCrosses.PLOT_DUPE_PREFIX.equals(prefix))
				|| (importedCrosses.isPedigreeDupe() && ImportedCrosses.PEDIGREE_DUPE_PREFIX.equals(prefix))
				|| (importedCrosses.isPlotRecip() && ImportedCrosses.PLOT_RECIP_PREFIX.equals(prefix))
				|| (importedCrosses.isPedigreeRecip() && ImportedCrosses.PEDIGREE_RECIP_PREFIX.equals(prefix));
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

	/**
	 * Function to detect Plot Dupe, Pedigree Dupe, Plot Recip and Pedigree Recip
	 * It will set duplications & reciprocals information in duplicate element of imported crosses object
	 * @param importedCrossesList list of imported crosses
	 */
	private static void detectDuplicationsAndReciprocalsFromImportedCrosses(ImportedCrossesList importedCrossesList) {
		for (ImportedCrosses importedCrossesMain : importedCrossesList.getImportedCrosses()) {
			if (importedCrossesMain.getDuplicate() != null) {
				continue;
			}

			final String nFemalePlotNo = importedCrossesMain.getFemalePlotNo().toString();
			final String nFemaleGid = importedCrossesMain.getFemaleGid();
			// FIXME - check back how to handle for polycross. For now pass the first male parent
			final String nMalePlotNo = importedCrossesMain.getMalePlotNos().get(0).toString();
			final String nMaleGid = importedCrossesMain.getMaleGids().get(0).toString();

			final StringBuilder plotDupePrefix = new StringBuilder(ImportedCrosses.PLOT_DUPE_PREFIX);
			final StringBuilder pedigreeDupePrefix = new StringBuilder(ImportedCrosses.PEDIGREE_DUPE_PREFIX);
			final StringBuilder plotRecipPrefix = new StringBuilder(ImportedCrosses.PLOT_RECIP_PREFIX);
			final StringBuilder pedigreeRecipPrefix = new StringBuilder(ImportedCrosses.PEDIGREE_RECIP_PREFIX);

			for (ImportedCrosses possibleDuplicatesAndReciprocals : importedCrossesList.getImportedCrosses()) {
				if (!Objects.equals(importedCrossesMain.getEntryId(), possibleDuplicatesAndReciprocals.getEntryId())) {

					final String femaleGidExcludingMain = possibleDuplicatesAndReciprocals.getFemaleGid();
					final String femalePlotNoExcludingMain = possibleDuplicatesAndReciprocals.getFemalePlotNo().toString();
					// FIXME - check back how to handle for polycross. For now pass the first male parent
					final String maleGidExcludingMain = possibleDuplicatesAndReciprocals.getMaleGids().get(0).toString();
					final String malePlotNoExcludingMain = possibleDuplicatesAndReciprocals.getMalePlotNos().get(0).toString();

					// Duplicate scenario
					if (femaleGidExcludingMain.equals(nFemaleGid) && maleGidExcludingMain.equals(nMaleGid)) {
						if (Objects.equals(femalePlotNoExcludingMain, nFemalePlotNo) && Objects.equals(malePlotNoExcludingMain, nMalePlotNo)) {
							// Plot Dupe
							DuplicatesUtil.setDuplicatePrefixAndEntriesForDuplicates(Lists.newArrayList(possibleDuplicatesAndReciprocals),
									ImportedCrosses.PLOT_DUPE_PREFIX);
							plotDupePrefix.append(possibleDuplicatesAndReciprocals.getEntryId() + ", ");
						} else {
							// Pedigree Dupe
							DuplicatesUtil.setDuplicatePrefixAndEntriesForDuplicates(Lists.newArrayList(possibleDuplicatesAndReciprocals),
									ImportedCrosses.PEDIGREE_DUPE_PREFIX);
							pedigreeDupePrefix.append(possibleDuplicatesAndReciprocals.getEntryId() + ", ");
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
							plotRecipPrefix.append(possibleDuplicatesAndReciprocals.getEntryId() + ", ");
						} else {
							// Pedigree Reciprocal
							List<Integer> pedigreeReciprocalEntries = new ArrayList<>();
							DuplicatesUtil.getAllEntries(Lists.newArrayList(possibleDuplicatesAndReciprocals), pedigreeReciprocalEntries);
							importedCrossesMain.setDuplicatePrefix(ImportedCrosses.PEDIGREE_RECIP_PREFIX);
							DuplicatesUtil.setDuplicateEntries(possibleDuplicatesAndReciprocals, pedigreeReciprocalEntries);
							pedigreeRecipPrefix.append(possibleDuplicatesAndReciprocals.getEntryId() + ", ");
						}
						if (importedCrossesMain.getDuplicateEntries() == null) {
							importedCrossesMain.setDuplicateEntries(new TreeSet<Integer>());
						}
						importedCrossesMain.getDuplicateEntries().add(possibleDuplicatesAndReciprocals.getEntryId());
						DuplicatesUtil.setDuplicateNotesBasedOnPrefixandEntries(importedCrossesMain);
					}
				}
			}

			final String plotDupePrefixFinal = DuplicatesUtil.removeCommaAndPipeFromEnd(plotDupePrefix.toString());
			final String pedigreeDupePrefixFinal = DuplicatesUtil.removeCommaAndPipeFromEnd(pedigreeDupePrefix.toString());
			final String plotRecipPrefixFinal = DuplicatesUtil.removeCommaAndPipeFromEnd(plotRecipPrefix.toString());
			final String pedigreeRecipPrefixFinal = DuplicatesUtil.removeCommaAndPipeFromEnd(pedigreeRecipPrefix.toString());

			String duplicateString = "";

			duplicateString = DuplicatesUtil.buildDuplicateString(plotDupePrefixFinal, ImportedCrosses.PLOT_DUPE_PREFIX);
			duplicateString =
					duplicateString + DuplicatesUtil.buildDuplicateString(pedigreeDupePrefixFinal, ImportedCrosses.PEDIGREE_DUPE_PREFIX);
			duplicateString =
					duplicateString + DuplicatesUtil.buildDuplicateString(plotRecipPrefixFinal, ImportedCrosses.PLOT_RECIP_PREFIX);
			duplicateString =
					duplicateString + DuplicatesUtil.buildDuplicateString(pedigreeRecipPrefixFinal, ImportedCrosses.PEDIGREE_RECIP_PREFIX);

			duplicateString = DuplicatesUtil.removeCommaAndPipeFromEnd(duplicateString);
			
			LOG.info("EntryID : " + importedCrossesMain.getEntryId() + " : " + duplicateString);

			importedCrossesMain.setDuplicate(duplicateString);
		}
	}

	/**
	 * Function to remove comma ',' or pipe '|' character at the end of the string
	 * Ex. last recip info will contain pipe like this Pedigree Recip: 5, 6 |
	 * @param prefixString string which contains comma or pipe character at the end
	 * @return string from which we have removed comma or pipe from end
	 */
	private static String removeCommaAndPipeFromEnd(final String prefixString) {
		String prefixFinal = prefixString;
		if(prefixString.endsWith(", ")) {
			int lastIndexOfComma = prefixString.lastIndexOf(", ");
			prefixFinal = prefixString.substring(0, lastIndexOfComma);
		} else if(prefixString.endsWith(" | ")) {
			int lastIndexOfPipe = prefixString.lastIndexOf(" | ");
			prefixFinal = prefixString.substring(0, lastIndexOfPipe);
		}

		return prefixFinal;
	}

	/**
	 * Function to build duplicate string which will be set in duplicate element of imported crosses object
	 * @param prefix contains information about duplication and reciprocals ex. Plot Dupe: 2, 3
	 * @param compareValue will decide if we need to append pipe character at the end or not
	 * If compareValue has no information then no need to append pipe character
	 * @return duplicateString which contains information about duplication & reciprocals ex. Plot Dupe: 2 | Pedigree Recip: 5, 6
	 */
	private static String buildDuplicateString(final String prefix, final String compareValue) {
		String duplicateString = "";
		if (!prefix.equals(compareValue)) {
			duplicateString = prefix + " | ";
		}

		return duplicateString;
	}
}
