
package com.efficio.fieldbook.service.internal;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

public class RESTDesignEngineTest extends AbstractBaseIntegrationTest {

	@Resource
	private DesignRunner designRunner;

	@Autowired
	private WorkbenchService workbenchService;

	private static FieldbookProperties fieldbookProperties;

	private static MainDesign mainDesign;

	@BeforeClass
	public static void setup() {
		// properties
		RESTDesignEngineTest.fieldbookProperties = new FieldbookProperties();

		RESTDesignEngineTest.mainDesign =
				ExpDesignUtil.createResolvableIncompleteBlockDesign("2", "18", "2", "ENTRY_NO", "REP_NO", "BLOCK_NO", "PLOT_NO", "0", "0.1", "1416451506872-rest-bv.csv",
						false);
	}

	@Test
	public void testRunner() {
		
		try {
			this.designRunner.runBVDesign(this.workbenchService, RESTDesignEngineTest.fieldbookProperties, RESTDesignEngineTest.mainDesign);
		} catch (IOException | FieldbookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

}
