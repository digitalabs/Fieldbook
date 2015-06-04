
package com.efficio.fieldbook.service.internal.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

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

		List<String[]> csvLines = new ArrayList<>();
		String[] csv = {"PLOT_NO", "REP_NO", "ENTRY_NO"};
		csvLines.add(csv);
		List<ListItem> levelList = expDesign.getParameterList("levels");
		Integer lines = Integer.parseInt(levelList.get(0).getValue());
		List<Integer> items = new ArrayList<Integer>(lines);
		Integer blocks = Integer.parseInt(expDesign.getParameterValue("nblocks"));
		for (int i = 1; i <= blocks; i++) {
			int count = 0;
			items.clear();
			while (count < lines) {
				Integer item = Math.round((float) Math.random() * lines) + 1;
				if (!items.contains(item) && item <= lines) {
					count++;
					items.add(item);
					csv = new String[] {String.valueOf(count), String.valueOf(i), item.toString()};
					csvLines.add(csv);
				}
			}
		}

		BVDesignOutput output = new BVDesignOutput(0);
		output.setResults(csvLines);
		return output;

	}

}
