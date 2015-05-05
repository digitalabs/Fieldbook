package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;

public class DuplicatesUtil {
	public static final String SEPARATOR = "/";

	private DuplicatesUtil() {
		//private constructor for utility class
	}
	
	public static void processDuplicates(ImportedCrossesList parseResults) {
		if(parseResults!=null) {
			Map<Object,List<ImportedCrosses>> possibleDuplicates = 
					new LinkedHashMap<Object,List<ImportedCrosses>>();
			Map<Object,List<ImportedCrosses>> possibleReciprocals = 
					new LinkedHashMap<Object,List<ImportedCrosses>>();
			addToDuplicatesMap(parseResults, possibleDuplicates);
			setDuplicateNotesForDuplicates(possibleDuplicates);
			addToReciprocalsMap(parseResults, possibleReciprocals);
			setDuplicateNotesForReciprocals(possibleReciprocals);
		}
	}

	private static void setDuplicateNotesForReciprocals(
			Map<Object, List<ImportedCrosses>> possibleReciprocals) {
		for (Object key : possibleReciprocals.keySet()) {
			List<ImportedCrosses> importedCrossesList = possibleReciprocals.get(key);
			//check if it has reciprocals
			if(importedCrossesList==null || importedCrossesList.isEmpty()) {
				continue;
			}
			ImportedCrosses importedCrosses = (ImportedCrosses) key;
			Set<ImportedCrosses> plotReciprocals = getAllPlotReciprocals(importedCrossesList,
					importedCrosses.getFemalePlotNo(),importedCrosses.getMalePlotNo());
			Set<ImportedCrosses> pedigreeReciprocals = getAllPedigreeReciprocals(importedCrossesList,
					plotReciprocals);
			setDuplicatePrefixAndEntriesForReciprocals(importedCrosses,
					plotReciprocals,pedigreeReciprocals);
			setDuplicateNotesBasedOnPrefixandEntries(importedCrosses);
		}
	}
	
	private static Set<ImportedCrosses> getAllPedigreeReciprocals(
			List<ImportedCrosses> importedCrossesList,
			Set<ImportedCrosses> plotReciprocals) {
		Set<ImportedCrosses> pedigreeReciprocals = new LinkedHashSet<ImportedCrosses>();
		for (ImportedCrosses importedCrosses : importedCrossesList) {
			if(!plotReciprocals.contains(importedCrosses)) {
				pedigreeReciprocals.add(importedCrosses);
			}
		}
		return pedigreeReciprocals;
	}

	private static void setDuplicatePrefixAndEntriesForReciprocals(
			ImportedCrosses importedCrosses,
			Set<ImportedCrosses> plotReciprocals, 
			Set<ImportedCrosses> pedigreeReciprocals) {
		if(plotReciprocals!=null && !plotReciprocals.isEmpty()) {
			//process plot reciprocals
			List<Integer> plotReciprocalEntries = new ArrayList<Integer>();
			getAllEntries(plotReciprocals,plotReciprocalEntries);
			importedCrosses.setDuplicatePrefix(ImportedCrosses.PLOT_RECIP_PREFIX);
			setDuplicateEntries(importedCrosses,plotReciprocalEntries);
		} else {
			//process pedigree reciprocals
			List<Integer> pedigreeReciprocalEntries = new ArrayList<Integer>();
			getAllEntries(pedigreeReciprocals,pedigreeReciprocalEntries);
			importedCrosses.setDuplicatePrefix(ImportedCrosses.PEDIGREE_RECIP_PREFIX);
			setDuplicateEntries(importedCrosses,pedigreeReciprocalEntries);
		}
	}

	private static void setDuplicateNotesForDuplicates(
			Map<Object, List<ImportedCrosses>> possibleDuplicates) {
		for (Object parentage : possibleDuplicates.keySet()) {
			List<ImportedCrosses> importedCrossesList = possibleDuplicates.get(parentage);
			//check if it has duplicates based on parentage by checking the value of the map
			if(importedCrossesList==null || importedCrossesList.size() < 2) {
				continue;
			}
			Map<Object,List<ImportedCrosses>> femalePlotImportedCrosses = new HashMap
					<Object,List<ImportedCrosses>>();
			Map<Object,List<ImportedCrosses>> malePlotImportedCrosses = new HashMap
					<Object,List<ImportedCrosses>>();
			getAllPossiblePlotDuplicates(importedCrossesList,femalePlotImportedCrosses,malePlotImportedCrosses);
			setDuplicateNotesAsPlotOrPedigreeDuplicates(importedCrossesList,
					femalePlotImportedCrosses,malePlotImportedCrosses);
		}
		
	}

	private static void setDuplicatePrefixAndEntriesForDuplicates(
			List<ImportedCrosses> importedCrossesList, String prefix) {
		List<Integer> entries = new ArrayList<Integer>();
		getAllEntries(importedCrossesList,entries);
		for (ImportedCrosses importedCrosses : importedCrossesList) {
			if(canStillSetDuplicateNotes(importedCrosses,prefix)) {
				importedCrosses.setDuplicatePrefix(prefix);
				setDuplicateEntries(importedCrosses,entries);
			}
		}
	}

	private static boolean canStillSetDuplicateNotes(
			ImportedCrosses importedCrosses, String prefix) {
		if(importedCrosses.getDuplicatePrefix()==null) {
			return true;
		}
		if(importedCrosses.isPlotDupe() && ImportedCrosses.PLOT_DUPE_PREFIX.equals(prefix)) {
			return true;
		}
		if(importedCrosses.isPedigreeDupe() && ImportedCrosses.PEDIGREE_DUPE_PREFIX.equals(prefix)) {
			return true;
		}
		if(importedCrosses.isPlotRecip() && ImportedCrosses.PLOT_RECIP_PREFIX.equals(prefix)) {
			return true;
		}
		if(importedCrosses.isPedigreeRecip() && ImportedCrosses.PEDIGREE_RECIP_PREFIX.equals(prefix)) {
			return true;
		}
		return false;
	}

	private static void setDuplicateNotesAsPlotOrPedigreeDuplicates(
			List<ImportedCrosses> importedCrossesList,
			Map<Object, List<ImportedCrosses>> femalePlotImportedCrosses,
			Map<Object, List<ImportedCrosses>> malePlotImportedCrosses) {
		String plotPrefix = ImportedCrosses.PLOT_DUPE_PREFIX;
		String pedigreePrefix = ImportedCrosses.PEDIGREE_DUPE_PREFIX;
		setDuplicatePrefixAndEntriesForDuplicates(femalePlotImportedCrosses,plotPrefix);
		setDuplicatePrefixAndEntriesForDuplicates(malePlotImportedCrosses,plotPrefix);
		setDuplicatePrefixAndEntriesForDuplicates(importedCrossesList,pedigreePrefix);
		setDuplicateNotesBasedOnPrefixandEntries(importedCrossesList);
	}
	
	private static void setDuplicatePrefixAndEntriesForDuplicates(
			Map<Object, List<ImportedCrosses>> possiblePlotDuplicates,
			String plotPrefix) {
		for (Object plotNo : possiblePlotDuplicates.keySet()) {
			List<ImportedCrosses> plotBasedImportedCrosses = possiblePlotDuplicates.get(plotNo);
			if(plotBasedImportedCrosses==null || plotBasedImportedCrosses.size() < 2) {
				continue;
			}
			setDuplicatePrefixAndEntriesForDuplicates(plotBasedImportedCrosses,plotPrefix);
		}
	}

	private static void setDuplicateNotesBasedOnPrefixandEntries(
			List<ImportedCrosses> importedCrossesList) {
		for (ImportedCrosses importedCrosses : importedCrossesList) {
			setDuplicateNotesBasedOnPrefixandEntries(importedCrosses);
		}
	}
	
	private static void setDuplicateNotesBasedOnPrefixandEntries(
			ImportedCrosses importedCrosses) {
		if(importedCrosses.getDuplicatePrefix()!=null) {
			importedCrosses.setDuplicate(
					importedCrosses.getDuplicatePrefix() +
					getCommaSeparatedEntryIdsOfDuplicates(
							importedCrosses.getDuplicateEntries()));
		}
	}

	private static void setDuplicateEntries(
			ImportedCrosses importedCrosses,
			Collection<Integer> entries) {
		if(importedCrosses.getDuplicateEntries()==null) {
			importedCrosses.setDuplicateEntries(new TreeSet<Integer>());
		}
		for (Integer entryId : entries) {
			if(importedCrosses.getEntryId().equals(entryId)) {
				continue;
			}
			importedCrosses.getDuplicateEntries().add(entryId);
		}
	}

	private static String getCommaSeparatedEntryIdsOfDuplicates(Collection<Integer> entries) {
		String entryIDCSV = "";
		for (Integer entryId : entries) {
			entryIDCSV += entryId + ",";
		}
		return entryIDCSV.substring(0,entryIDCSV.length()-1);
	}

	private static void getAllEntries(Collection<ImportedCrosses> importedCrossesList,
			List<Integer> entries) {
		for (ImportedCrosses importedCrosses : importedCrossesList) {
			entries.add(importedCrosses.getEntryId());
		}
	}
	
	private static Set<ImportedCrosses> getAllPlotReciprocals(
			List<ImportedCrosses> importedCrossesList, 
			String femalePlotNo, String malePlotNo) {
		Set<ImportedCrosses> plotReciprocals = new LinkedHashSet<ImportedCrosses>();
		for (ImportedCrosses importedCrosses : importedCrossesList) {
			if(femalePlotNo.equals(importedCrosses.getMalePlotNo()) ||
				malePlotNo.equals(importedCrosses.getFemalePlotNo())) {
				plotReciprocals.add(importedCrosses);
			}
		}
		return plotReciprocals;
	}

	private static void getAllPossiblePlotDuplicates(
			List<ImportedCrosses> importedCrossesList,
			Map<Object, List<ImportedCrosses>> femalePlotImportedCrosses,
			Map<Object, List<ImportedCrosses>> malePlotImportedCrosses) {
		for (ImportedCrosses importedCrosses : importedCrossesList) {
			addToMap(femalePlotImportedCrosses,importedCrosses,importedCrosses.getFemalePlotNo());
			addToMap(malePlotImportedCrosses,importedCrosses,importedCrosses.getMalePlotNo());
		}
	}

	private static void addToMap(Map<Object, List<ImportedCrosses>> map,
			ImportedCrosses importedCrosses,
			Object key) {
		if(!map.containsKey(key)) {
			map.put(key, new ArrayList<ImportedCrosses>());
		}
		map.get(key).add(importedCrosses);
	}
	
	private static void addToReciprocalsMap(
			ImportedCrossesList importedCrossesList,
			Map<Object, List<ImportedCrosses>> reciprocalsMap) {
		for (ImportedCrosses importedCrosses : importedCrossesList.getImportedCrosses()) {
			if(importedCrosses.getDuplicate()!=null) {
				continue;
			}
			String maleAndFemaleGid = importedCrosses.getMaleGid()+SEPARATOR+importedCrosses.getFemaleGid();
			for (ImportedCrosses possibleReciprocal: importedCrossesList.getImportedCrosses()) {
				if(possibleReciprocal.getEntryId()!=importedCrosses.getEntryId()) {
					String femaleAndMaleGid = possibleReciprocal.getFemaleGid()+SEPARATOR+possibleReciprocal.getMaleGid(); 
					if(maleAndFemaleGid.equals(femaleAndMaleGid)) {
						addToMap(reciprocalsMap, possibleReciprocal, importedCrosses);
					}
				}
			}
		}
	}
	
	private static void addToDuplicatesMap(ImportedCrossesList importedCrossesList,
			Map<Object, List<ImportedCrosses>> possibleDuplicates) {
		for (ImportedCrosses importedCrosses : importedCrossesList.getImportedCrosses()) {
			addToMap(possibleDuplicates,importedCrosses,
					importedCrosses.getFemaleGid()+SEPARATOR+
					importedCrosses.getMaleGid());
		}
	}
}
