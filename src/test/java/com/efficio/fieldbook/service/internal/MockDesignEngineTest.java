package com.efficio.fieldbook.service.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;


public class MockDesignEngineTest extends AbstractBaseIntegrationTest {
	
	@Resource
	private DesignRunner designRunner;
	
	@Autowired
	private WorkbenchService workbenchService;
	
	private static FieldbookProperties fieldbookProperties;
	
	private static MainDesign mainDesign;
	
	@BeforeClass
	public static void setup(){
		// properties
		fieldbookProperties = new FieldbookProperties();
		
		// experimental design parameters - taken from file we provide to BV
		String nBlock = "2";
		String blockFactor = "REP_NO";
		String plotFactor = "BLOCK_NO";
		List<String> treatmentFactor = Arrays.asList("ENTRY_NO");
		List<String> levels = Arrays.asList("20");
		String timeLimit = "0.1";
		String outputfile = "1416451506872-mock-bv.csv";
		mainDesign = ExpDesignUtil.createRandomizedCompleteBlockDesign(nBlock, blockFactor, plotFactor, treatmentFactor, levels, timeLimit, outputfile);
	}
	
	@Test
	public void testRunner(){
		
		try {
			designRunner.runBVDesign(workbenchService, fieldbookProperties, mainDesign);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
