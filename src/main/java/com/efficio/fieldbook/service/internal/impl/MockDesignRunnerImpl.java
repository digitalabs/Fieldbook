package com.efficio.fieldbook.service.internal.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.DesignRunner;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.bvdesign.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

@Component("MockDesignRunner")
public class MockDesignRunnerImpl implements DesignRunner {

	private static final Logger LOG = LoggerFactory.getLogger(MockDesignRunnerImpl.class);

	private static String CSV_EXTENSION = ".csv";
	private static String BV_PREFIX = "-bv";
	private static String OUTPUT_FILE_PARAMETER_NAME = "outputfile";

	@Override
	public BVDesignOutput runBVDesign(WorkbenchService workbenchService, FieldbookProperties fieldbookProperties, MainDesign design)
			throws IOException {

		String outputFilePath = System.currentTimeMillis() + MockDesignRunnerImpl.BV_PREFIX + MockDesignRunnerImpl.CSV_EXTENSION;

		design.getDesign().setParameterValue(MockDesignRunnerImpl.OUTPUT_FILE_PARAMETER_NAME, outputFilePath);

		@SuppressWarnings("unused") String xml = "";
		try {
			xml = ExpDesignUtil.getXmlStringForSetting(design);
		} catch (JAXBException e) {
			MockDesignRunnerImpl.LOG.error(e.getMessage(), e);
		}

		// params should be in the MainDesign instance

		ExpDesign expDesign = design.getDesign();
		MockDesignRunnerImpl.LOG.info("Mocking Design for " + expDesign.getName());

		Integer lines = new Integer(0);
		Integer replications = new Integer(1);

		if (expDesign.getName().equals(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN)) {
			List<ListItem> levelList = expDesign.getParameterList(ExperimentDesignGenerator.LEVELS_PARAM);
			lines = Integer.parseInt(levelList.get(levelList.size()-1).getValue());
			replications = Integer.parseInt(expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		} else if (expDesign.getName().equals(ExperimentDesignGenerator.AUGMENTED_RANDOMIZED_BLOCK_DESIGN)) {
			lines = Integer.valueOf(expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
			replications = Integer.parseInt(expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		} else {
			lines = Integer.valueOf(expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
			replications = Integer.parseInt(expDesign.getParameterValue(ExperimentDesignGenerator.NREPLICATES_PARAM));
		}

		final String numberTrialsParam = expDesign.getParameterValue(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM);
		final int numberTrials = StringUtils.isNumeric(numberTrialsParam) ? Integer.valueOf(numberTrialsParam) : 1;
				
		final String initEntryNoParam = expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM);
		int startingEntryNo = StringUtils.isNumeric(initEntryNoParam) ? Integer.valueOf(initEntryNoParam) : 1;

		List<String[]> csvLines = new ArrayList<>();

		List<Pair<Integer, Integer>> rowColTuples = new ArrayList<>();
		if (expDesign.getName().equals(ExperimentDesignGenerator.RESOLVABLE_ROW_COL_DESIGN)) {
			int rows = Integer.valueOf(expDesign.getParameterValue(ExperimentDesignGenerator.NROWS_PARAM));
			int cols = Integer.valueOf(expDesign.getParameterValue(ExperimentDesignGenerator.NCOLUMNS_PARAM));

			for (int r = 1; r <= rows; r++) {
				for (int c = 1; c <= cols; c++) {
					rowColTuples.add(new ImmutablePair<Integer, Integer>(r, c));
				}
			}
			csvLines.add(new String[] {"TRIAL", "PLOT_NO", "REP_NO", "ENTRY_NO", "ROW", "COL"});
		} else if(expDesign.getName().equals(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN) && expDesign.getParameterList(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM).size() != 1) {
			final List<String> constants = Arrays.asList("TRIAL", "PLOT_NO", "REP_NO");
			final List<String> headers = new ArrayList<>(constants);
			List<ListItem> treatmentFactorsList = expDesign.getParameterList(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM);
			for(ListItem item: treatmentFactorsList) {
				headers.add(item.getValue());
			}
			String[] headersAsArray = new String[headers.size()];
			headers.toArray(headersAsArray);
			csvLines.add(headersAsArray);
		} else {
			csvLines.add(new String[] {"TRIAL", "PLOT_NO", "REP_NO", "ENTRY_NO"});
		}

		List<Integer> entryNumbers = new ArrayList<>();
		for (int i = 1; i <= lines; i++) {
			entryNumbers.add(startingEntryNo++);
		}
		List<ListItem> treatmentFactorsList = expDesign.getParameterList(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM);
		List<List<String>> tfValuesListForCSV = this.getTreatmentFactorValuesCombinations(expDesign);

		for (int instance = 1; instance <= numberTrials; instance++) {
			final String initPlotNoParam = expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM);
			int startingPlotNo = StringUtils.isNumeric(initPlotNoParam) ? Integer.valueOf(initPlotNoParam) : 1;
			for (int rep = 1; rep <= replications; rep++) {
				if(!expDesign.getName().equals(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN) || treatmentFactorsList.size()==1) {
					startingPlotNo = populateCSVLines(expDesign, startingPlotNo, csvLines, rowColTuples, entryNumbers, instance, rep);
				} else {
					startingPlotNo = populateCSVLines(startingPlotNo, csvLines, entryNumbers, instance, rep, tfValuesListForCSV);
				}
			}
		}
		BVDesignOutput output = new BVDesignOutput(0);
		output.setResults(csvLines);
		return output;
	}

	private int populateCSVLines(
		final ExpDesign expDesign, int startingPlotNo, final List<String[]> csvLines, final List<Pair<Integer, Integer>> rowColTuples,
		final List<Integer> entryNumbers, final int instance, final int rep) {
		int rowColTuplesCounter = 0;
		// Randomize entry number arrangements per replication
		Collections.shuffle(entryNumbers);
		for (int j = 0; j < entryNumbers.size(); j++) {
			final List<String> csvLine = new ArrayList<>();
			csvLine.add(String.valueOf(instance));
			csvLine.add(String.valueOf(startingPlotNo++));
			csvLine.add(String.valueOf(rep));
			csvLine.add(entryNumbers.get(j).toString());
			if (expDesign.getName().equals(ExperimentDesignGenerator.RESOLVABLE_ROW_COL_DESIGN)) {
				Pair<Integer, Integer> rowColTuple = rowColTuples.get(rowColTuplesCounter);
				csvLine.add(String.valueOf(rowColTuple.getLeft()));
				csvLine.add(String.valueOf(rowColTuple.getRight()));
				rowColTuplesCounter++;
			}
			String[] csvLineAsArray = new String[csvLine.size()];
			csvLine.toArray(csvLineAsArray);
			csvLines.add(csvLineAsArray);
		}
		return startingPlotNo;
	}

	private int populateCSVLines(int startingPlotNo, final List<String[]> csvLines,
		final List<Integer> entryNumbers, final int instance, final int rep, List<List<String>> tfValuesList) {
		// Randomize entry number arrangements per replication
		Collections.shuffle(entryNumbers);
		for (int j = 0; j < entryNumbers.size(); j++) {
			final List<String> csvLine = new ArrayList<>();
			csvLine.add(String.valueOf(instance));
			//Randomize the treatment factor values combinations
			Collections.shuffle(tfValuesList);
			for(List<String> tfValues: tfValuesList) {
				csvLine.add(String.valueOf(startingPlotNo++));
				csvLine.add(String.valueOf(rep));

				final List<String> tfCsvLine = new ArrayList<>(csvLine);
				tfCsvLine.addAll(tfValues);
				tfCsvLine.add(entryNumbers.get(j).toString());
				String[] csvLineAsArray = new String[tfCsvLine.size()];
				tfCsvLine.toArray(csvLineAsArray);
				csvLines.add(csvLineAsArray);
			}
		}
		return startingPlotNo;
	}


	public List<List<String>> getTreatmentFactorValuesCombinations(final ExpDesign expDesign) {
		List<ListItem> treatmentFactorsList = expDesign.getParameterList(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM);
		List<ListItem> levelList = expDesign.getParameterList(ExperimentDesignGenerator.LEVELS_PARAM);
		List<List<String>> tfValuesListForCSV = new ArrayList<>();
		if((expDesign.getName().equals(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN) && treatmentFactorsList.size()!=1)) {
			//Create the lists of treatment factor values
			List<List<String>> tfValuesList = new ArrayList<>();
			for(int tfIndex = 0; tfIndex<treatmentFactorsList.size()-1; tfIndex++) {
				final List<String> tfValues =  new ArrayList<>();
				for(int levelIndex = 1; levelIndex<=Integer.valueOf(levelList.get(tfIndex).getValue()); levelIndex++) {
					tfValues.add(String.valueOf(levelIndex));
				}
				tfValuesList.add(tfValues);
			}

			if (CollectionUtils.isEmpty(tfValuesList)) {
				return tfValuesListForCSV;
			} else {
				this.getCombinations(tfValuesList, tfValuesListForCSV, 0, new ArrayList<String>());
				return tfValuesListForCSV;
			}
		}
		return tfValuesListForCSV;
	}

	private void getCombinations(List<List<String>> tfValuesList, List<List<String>> tfValuesListForCsv, int tfValuesSize, List<String> tfValues) {
		// if number of elements in tfValues, final reached, add and return
		if (tfValuesSize == tfValuesList.size()) {
			tfValuesListForCsv.add(tfValues);
			return;
		}

		// iterate from current list and copy current element N times, one for each element
		List<String> currentCollection = tfValuesList.get(tfValuesSize);
		for (String element : currentCollection) {
			List<String> copy = new ArrayList<>(tfValues);
			copy.add(element);
			getCombinations(tfValuesList, tfValuesListForCsv, tfValuesSize + 1, copy);
		}
	}

}
