
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			// FIXME - check back how to handle for polycross. For now pass the first male parent
			final String nMalePlotNo = importedCrossesMain.getMalePlotNos().get(0).toString();
			final String nFemaleGid = importedCrossesMain.getFemaleGid();
			// FIXME - check back how to handle for polycross. For now pass the first male parent
			final String nMaleGid = importedCrossesMain.getMaleGids().get(0).toString();

			String plotDupePrefix = ImportedCrosses.PLOT_DUPE_PREFIX;
			String pedigreeDupePrefix = ImportedCrosses.PEDIGREE_DUPE_PREFIX;
			String plotRecipPrefix = ImportedCrosses.PLOT_RECIP_PREFIX;
			String pedigreeRecipPrefix = ImportedCrosses.PEDIGREE_RECIP_PREFIX;

			for (ImportedCrosses possibleDuplicatesAndReciprocals : importedCrossesList.getImportedCrosses()) {
				if (!Objects.equals(importedCrossesMain.getEntryId(), possibleDuplicatesAndReciprocals.getEntryId())) {

					final String femaleGidExcludingMain = possibleDuplicatesAndReciprocals.getFemaleGid();
					// FIXME - check back how to handle for polycross. For now pass the first male parent
					final String maleGidExcludingMain = possibleDuplicatesAndReciprocals.getMaleGids().get(0).toString();
					final String femalePlotNoExcludingMain = possibleDuplicatesAndReciprocals.getFemalePlotNo().toString();
					// FIXME - check back how to handle for polycross. For now pass the first male parent
					final String malePlotNoExcludingMain = possibleDuplicatesAndReciprocals.getMalePlotNos().get(0).toString();

					// Duplicate scenario
					if (femaleGidExcludingMain.equals(nFemaleGid) && maleGidExcludingMain.equals(nMaleGid)) {
						if (Objects.equals(femalePlotNoExcludingMain, nFemalePlotNo) && Objects.equals(malePlotNoExcludingMain, nMalePlotNo)) {
							// Plot Dupe
							DuplicatesUtil.setDuplicatePrefixAndEntriesForDuplicates(Lists.newArrayList(possibleDuplicatesAndReciprocals),
									ImportedCrosses.PLOT_DUPE_PREFIX);
							plotDupePrefix = plotDupePrefix + possibleDuplicatesAndReciprocals.getEntryId() + ", ";
						} else {
							// Pedigree Dupe
							DuplicatesUtil.setDuplicatePrefixAndEntriesForDuplicates(Lists.newArrayList(possibleDuplicatesAndReciprocals),
									ImportedCrosses.PEDIGREE_DUPE_PREFIX);
							pedigreeDupePrefix = pedigreeDupePrefix + possibleDuplicatesAndReciprocals.getEntryId() + ", ";
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
							plotRecipPrefix = plotRecipPrefix + possibleDuplicatesAndReciprocals.getEntryId() + ", " ;
						} else {
							// Pedigree Reciprocal
							List<Integer> pedigreeReciprocalEntries = new ArrayList<>();
							DuplicatesUtil.getAllEntries(Lists.newArrayList(possibleDuplicatesAndReciprocals), pedigreeReciprocalEntries);
							importedCrossesMain.setDuplicatePrefix(ImportedCrosses.PEDIGREE_RECIP_PREFIX);
							DuplicatesUtil.setDuplicateEntries(possibleDuplicatesAndReciprocals, pedigreeReciprocalEntries);
							pedigreeRecipPrefix = pedigreeRecipPrefix + possibleDuplicatesAndReciprocals.getEntryId() + ", ";
						}
						if (importedCrossesMain.getDuplicateEntries() == null) {
							importedCrossesMain.setDuplicateEntries(new TreeSet<Integer>());
						}
						importedCrossesMain.getDuplicateEntries().add(possibleDuplicatesAndReciprocals.getEntryId());
						DuplicatesUtil.setDuplicateNotesBasedOnPrefixandEntries(importedCrossesMain);
					}
				}
			}

			plotDupePrefix = DuplicatesUtil.removeCommaAndPipeFromEnd(plotDupePrefix);
			pedigreeDupePrefix = DuplicatesUtil.removeCommaAndPipeFromEnd(pedigreeDupePrefix);
			plotRecipPrefix = DuplicatesUtil.removeCommaAndPipeFromEnd(plotRecipPrefix);
			pedigreeRecipPrefix = DuplicatesUtil.removeCommaAndPipeFromEnd(pedigreeRecipPrefix);

			String duplicateString = "";

			duplicateString = DuplicatesUtil.buildDuplicateString(plotDupePrefix ,ImportedCrosses.PLOT_DUPE_PREFIX);
			duplicateString = duplicateString + DuplicatesUtil.buildDuplicateString(pedigreeDupePrefix ,ImportedCrosses.PEDIGREE_DUPE_PREFIX);
			duplicateString = duplicateString + DuplicatesUtil.buildDuplicateString(plotRecipPrefix ,ImportedCrosses.PLOT_RECIP_PREFIX);
			duplicateString = duplicateString + DuplicatesUtil.buildDuplicateString(pedigreeRecipPrefix ,ImportedCrosses.PEDIGREE_RECIP_PREFIX);

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
	private static String removeCommaAndPipeFromEnd(String prefixString) {
		if(prefixString.endsWith(", ")) {
			int lastIndexOfComma = prefixString.lastIndexOf(", ");
			prefixString = prefixString.substring(0, lastIndexOfComma);
		} else if(prefixString.endsWith(" | ")) {
			int lastIndexOfPipe = prefixString.lastIndexOf(" | ");
			prefixString = prefixString.substring(0, lastIndexOfPipe);
		}

		return prefixString;
	}

	/**
	 * Function to build duplicate string which will be set in duplicate element of imported crosses object
	 * @param prefix contains information about duplication and reciprocals ex. Plot Dupe: 2, 3
	 * @param compareValue will decide if we need to append pipe character at the end or not
	 * If compareValue has no information then no need to append pipe character
	 * @return duplicateString which contains information about duplication & reciprocals ex. Plot Dupe: 2 | Pedigree Recip: 5, 6
	 */
	private static String buildDuplicateString(String prefix, String compareValue) {
		String duplicateString = "";
		if (!prefix.equals(compareValue)) {
			duplicateString = prefix + " | ";
		}

		return duplicateString;
	}
}
