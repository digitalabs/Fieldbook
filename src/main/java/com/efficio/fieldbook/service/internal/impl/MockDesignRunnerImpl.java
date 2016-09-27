
package com.efficio.fieldbook.service.internal.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.DesignRunner;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
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

		@SuppressWarnings("unused")
		String xml = "";
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
			lines = Integer.parseInt(levelList.get(0).getValue());
			replications = Integer.parseInt(expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		} else if (expDesign.getName().equals(ExperimentDesignGenerator.AUGMENTED_RANDOMIZED_BLOCK_DESIGN)) {
			lines = Integer.valueOf(expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
			replications = Integer.parseInt(expDesign.getParameterValue(ExperimentDesignGenerator.NBLOCKS_PARAM));
		} else {
			lines = Integer.valueOf(expDesign.getParameterValue(ExperimentDesignGenerator.NTREATMENTS_PARAM));
			replications = Integer.parseInt(expDesign.getParameterValue(ExperimentDesignGenerator.NREPLICATES_PARAM));
		}

		final String initPlotNoParam = expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM);
		int startingPlotNo = StringUtils.isNumeric(initPlotNoParam) ? Integer.valueOf(initPlotNoParam) : 1;
		
		final String initEntryNoParam = expDesign.getParameterValue(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM);
		int startingEntryNo = StringUtils.isNumeric(initEntryNoParam) ? Integer.valueOf(initEntryNoParam) : 1;

		List<String[]> csvLines = new ArrayList<>();
		csvLines.add(new String[] {"PLOT_NO", "REP_NO", "ENTRY_NO"});

		List<Integer> entryNumbers = new ArrayList<>();
		for (int i = 1; i <= lines; i++) {
			entryNumbers.add(startingEntryNo++);
		}

		for (int rep = 1; rep <= replications; rep++) {
			// Randomize entry number arrangements per replication
			Collections.shuffle(entryNumbers);
			for (int j = 0; j < entryNumbers.size(); j++) {
				csvLines.add(new String[] {String.valueOf(startingPlotNo++), String.valueOf(rep), entryNumbers.get(j).toString()});
			}
		}
		BVDesignOutput output = new BVDesignOutput(0);
		output.setResults(csvLines);
		return output;
	}
}
