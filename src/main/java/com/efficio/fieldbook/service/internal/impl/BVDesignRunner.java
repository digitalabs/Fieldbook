package com.efficio.fieldbook.service.internal.impl;

import au.com.bytecode.opencsv.CSVReader;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.DesignRunner;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.commons.pojo.ProcessTimeoutThread;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class BVDesignRunner implements DesignRunner {

	public static final String BV_PREFIX = "-bv";
	public static final String CSV_EXTENSION = ".csv";

	private static final Logger LOG = LoggerFactory.getLogger(BVDesignRunner.class);
	// set 3 minutes for the design runner process to timeout
	private static final long DESIGN_RUNNER_TIMEOUT_MILLIS = 3 * 60 * 1000;
	private static String XML_EXTENSION = ".xml";
	private static String BREEDING_VIEW_EXE = "BreedingView.exe";
	private static String BVDESIGN_EXE = "BVDesign.exe";

	@Override
	public BVDesignOutput runBVDesign(final WorkbenchService workbenchService, final FieldbookProperties fieldbookProperties,
			final MainDesign design) throws IOException {

		final String bvDesignLocation = BVDesignRunner.getBreedingViewExeLocation(workbenchService);
		int returnCode = -1;
		if (bvDesignLocation != null && design != null && design.getDesign() != null) {
			final String xml = this.getXMLStringForDesign(design);

			final String filepath = BVDesignRunner.writeToFile(xml, fieldbookProperties);

			final ProcessBuilder pb = new ProcessBuilder(bvDesignLocation, "-i" + filepath);
			final Process p = pb.start();
			// add a timeout for the design runner
			final ProcessTimeoutThread processTimeoutThread = new ProcessTimeoutThread(p, BVDesignRunner.DESIGN_RUNNER_TIMEOUT_MILLIS);
			processTimeoutThread.start();
			try {
				final InputStreamReader isr = new InputStreamReader(p.getInputStream());
				final BufferedReader br = new BufferedReader(isr);

				String lineRead;
				while ((lineRead = br.readLine()) != null) {
					BVDesignRunner.LOG.debug(lineRead);
				}

				returnCode = p.waitFor();
				// add here the code to parse the csv file
			} catch (final InterruptedException e) {
				BVDesignRunner.LOG.error(e.getMessage(), e);
			} finally {
				if (processTimeoutThread != null) {
					// Stop the thread if it's still running
					processTimeoutThread.interrupt();
				}
				if (p != null) {
					// missing these was causing the mass amounts of open 'files'
					p.getInputStream().close();
					p.getOutputStream().close();
					p.getErrorStream().close();
				}
			}
		}
		final BVDesignOutput output = new BVDesignOutput(returnCode);
		if (returnCode == 0) {

			final File outputFile = new File(design.getDesign().getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM));
			final FileReader fileReader = new FileReader(outputFile);
			final CSVReader reader = new CSVReader(fileReader);
			final List<String[]> myEntries = reader.readAll();

			output.setResults(myEntries);
			fileReader.close();
			reader.close();
			outputFile.delete();

		}
		return output;
	}

	public String getXMLStringForDesign(final MainDesign design) {
		String xml = "";
		final Long currentTimeMillis = System.currentTimeMillis();
		final String outputFilePath = currentTimeMillis + BVDesignRunner.BV_PREFIX + BVDesignRunner.CSV_EXTENSION;

		design.getDesign().setParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputFilePath);
		design.getDesign().setParameterValue(ExperimentDesignGenerator.SEED_PARAM, this.getSeedValue(currentTimeMillis));

		try {
			xml = ExpDesignUtil.getXmlStringForSetting(design);
		} catch (final JAXBException e) {
			BVDesignRunner.LOG.error(e.getMessage(), e);
		}
		return xml;
	}

	private String getSeedValue(final Long currentTimeMillis) {
		String seedValue = Long.toString(currentTimeMillis);
		if (currentTimeMillis > Integer.MAX_VALUE) {
			seedValue = seedValue.substring(seedValue.length() - 9);
		}
		return seedValue;
	}

	private static String getBreedingViewExeLocation(final WorkbenchService workbenchService) {
		String bvDesignLocation = null;
		Tool bvTool = null;
		try {
			bvTool = workbenchService.getToolWithName(AppConstants.TOOL_NAME_BREEDING_VIEW.getString());
		} catch (final MiddlewareQueryException e) {
			BVDesignRunner.LOG.error(e.getMessage(), e);
		}
		if (bvTool != null) {
			// write xml to temp file
			final File absoluteToolFile = new File(bvTool.getPath()).getAbsoluteFile();
			bvDesignLocation = absoluteToolFile.getAbsolutePath().replaceAll(BVDesignRunner.BREEDING_VIEW_EXE, BVDesignRunner.BVDESIGN_EXE);
		}
		return bvDesignLocation;
	}

	private static String writeToFile(final String xml, final FieldbookProperties fieldbookProperties) {
		String filenamePath = BVDesignRunner.generateBVFilePath(BVDesignRunner.XML_EXTENSION, fieldbookProperties);
		try {

			final File file = new File(filenamePath);
			final BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(xml);
			output.close();
			filenamePath = file.getAbsolutePath();
		} catch (final IOException e) {
			BVDesignRunner.LOG.error(e.getMessage(), e);
		}
		return filenamePath;
	}

	private static String generateBVFilePath(final String extensionFilename, final FieldbookProperties fieldbookProperties) {
		final String filename = BVDesignRunner.generateBVFileName(extensionFilename);
		final String filenamePath = fieldbookProperties.getUploadDirectory() + File.separator + filename;
		final File f = new File(filenamePath);
		return f.getAbsolutePath();
	}

	private static String generateBVFileName(final String extensionFileName) {
		return System.currentTimeMillis() + BVDesignRunner.BV_PREFIX + extensionFileName;
	}

}
