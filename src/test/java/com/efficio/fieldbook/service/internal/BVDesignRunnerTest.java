package com.efficio.fieldbook.service.internal;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.efficio.fieldbook.service.internal.impl.BVDesignRunner;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;

import static com.efficio.fieldbook.web.util.ExpDesignUtil.*;

public class BVDesignRunnerTest {
	
	@Test
	public void testGetXMLStringForRandomizedCompleteBlockDesign(){
		List<String> treatmentFactor = new ArrayList<String>();
		treatmentFactor.add("ENTRY_NO");
		treatmentFactor.add("FERTILIZER");
		
		List<String> levels = new ArrayList<String>();
		levels.add("24");
		levels.add("3");
		
		MainDesign mainDesign = createRandomizedCompleteBlockDesign("6", "Reps", "Plots",
				treatmentFactor, levels, "1", "");
		
		String expectedString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"RandomizedBlock\">"
				+ "<Parameter name=\"" + SEED_PARAM + "\" value=\":seedValue\"/>"
				+ "<Parameter name=\"" + NBLOCKS_PARAM + "\" value=\"6\"/>"
				+ "<Parameter name=\"" + BLOCKFACTOR_PARAM+ "\" value=\"Reps\"/>"
				+ "<Parameter name=\"" + PLOTFACTOR_PARAM + "\" value=\"Plots\"/>"
				+ "<Parameter name=\"" + TREATMENTFACTORS_PARAM + "\"><ListItem value=\"ENTRY_NO\"/><ListItem value=\"FERTILIZER\"/></Parameter>"
				+ "<Parameter name=\"levels\"><ListItem value=\"24\"/><ListItem value=\"3\"/></Parameter>"
				+ "<Parameter name=\"" + TIMELIMIT_PARAM + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString() + "\"/>"
				+ "<Parameter name=\"" + OUTPUTFILE_PARAM + "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);

		assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}
	
	
	@Test
	public void testGetXMLStringForResolvableIncompleteBlockDesign(){
		MainDesign mainDesign = ExpDesignUtil.createResolvableIncompleteBlockDesign("6", "24", 
				"2", "Treat", "Reps", "Subblocks", 
				"Plots", "0", "", "1", "", false);
		
		String expectedString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableIncompleteBlock\">"
				+ "<Parameter name=\"" + SEED_PARAM + "\" value=\":seedValue\"/><Parameter name=\"" + BLOCKSIZE_PARAM + "\" value=\"6\"/>"
				+ "<Parameter name=\"" + NTREATMENTS_PARAM+"\" value=\"24\"/><Parameter name=\"" + NREPLICATES_PARAM + "\" value=\"2\"/>"
				+ "<Parameter name=\"" + TREATMENTFACTOR_PARAM +"\" value=\"Treat\"/><Parameter name=\""+ REPLICATEFACTOR_PARAM + "\" value=\"Reps\"/>"
				+ "<Parameter name=\"" + BLOCKFACTOR_PARAM + "\" value=\"Subblocks\"/><Parameter name=\"" + PLOTFACTOR_PARAM + "\" value=\"Plots\"/>"
				+ "<Parameter name=\"" + NBLATIN_PARAM + "\" value=\"0\"/><Parameter name=\""+ TIMELIMIT_PARAM +"\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString() + "\"/>"
				+ "<Parameter name=\"" + OUTPUTFILE_PARAM + "\" value=\":outputFile\"/></Template></Templates>";

		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);

		assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}
	
	@Test
	public void testGetXMLStringForResolvableRowColExpDesign(){
		MainDesign mainDesign = ExpDesignUtil.createResolvableRowColDesign("50",
				"2", "5", "10", "Treat", "Reps", 
				"Rows", "Columns","Plots",
				"0", "0", "", "1", "", false);
		
		String expectedString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableRowColumn\">"
				+ "<Parameter name=\"" + SEED_PARAM + "\" value=\":seedValue\"/><Parameter name=\"" + NTREATMENTS_PARAM +"\" value=\"50\"/>"
				+ "<Parameter name=\"" + NREPLICATES_PARAM+ "\" value=\"2\"/><Parameter name=\"" + NROWS_PARAM + "\" value=\"5\"/>"
				+ "<Parameter name=\"" + NCOLUMNS_PARAM + "\" value=\"10\"/><Parameter name=\"" + TREATMENTFACTOR_PARAM + "\" value=\"Treat\"/>"
				+ "<Parameter name=\"" + REPLICATEFACTOR_PARAM + "\" value=\"Reps\"/><Parameter name=\"" + ROWFACTOR_PARAM + "\" value=\"Rows\"/>"
				+ "<Parameter name=\"" + COLUMNFACTOR_PARAM + "\" value=\"Columns\"/><Parameter name=\"" + PLOTFACTOR_PARAM + "\" value=\"Plots\"/>"
				+ "<Parameter name=\"" + NRLATIN_PARAM + "\" value=\"0\"/><Parameter name=\"" + NCLATIN_PARAM + "\" value=\"0\"/>"
				+ "<Parameter name=\""+ TIMELIMIT_PARAM +"\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString() + "\"/>"
				+ "<Parameter name=\"" + OUTPUTFILE_PARAM + "\" value=\":outputFile\"/></Template></Templates>";
		
		BVDesignRunner runner = new BVDesignRunner();
		String xmlString = runner.getXMLStringForDesign(mainDesign);
		
		assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}
	

	private void assertXMLStringEqualsExpected(MainDesign mainDesign, String expectedString,
			String xmlString) {
		String outputFile = mainDesign.getDesign().getParameterValue(OUTPUTFILE_PARAM);
		String outputFileMillisecs = outputFile.replace(BVDesignRunner.BV_PREFIX+BVDesignRunner.CSV_EXTENSION, "");
		String seedValue = new Integer(new Long(outputFileMillisecs).intValue()).toString();
		expectedString = expectedString.replace(":seedValue", seedValue);
		expectedString = expectedString.replace(":outputFile", outputFile);
		
		Assert.assertEquals(expectedString, xmlString);
	}

}
