package com.efficio.fieldbook.web.util;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class ImportStudyUtilTest {
	
	private Workbook workbook;
	
	@Before
	public void setUp(){
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook();
	}
	
	@Test
	public void testCreateMeasurementRowsMap(){
		List<MeasurementRow> observations = workbook.getObservations();
		
		Map<String, MeasurementRow> measurementRowsMap = ImportStudyUtil.createMeasurementRowsMap(observations, "1", true);
		Assert.assertEquals("The number of measurements in the measurementRowsMap should be equal to the number of the observationss", observations.size(), measurementRowsMap.size());
	}
	
	@Test
	public void testGetTrialInstanceNumberOfNursery() throws WorkbookParserException{
		String trialInstanceNumber = ImportStudyUtil.getTrialInstanceNo(workbook, "filename");
		Assert.assertEquals("The trial instance number should be 11", "1", trialInstanceNumber);
	}
	
	@Test(expected = WorkbookParserException.class)
	public void testGetTrialInstanceNumberOfNurseryWithError() throws WorkbookParserException{
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook(10, StudyType.T);
		ImportStudyUtil.getTrialInstanceNo(workbook, "filename");
	}
	
	@Test
	public void testGetTrialInstanceNumberOfNurseryOfTrial() throws WorkbookParserException{
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook(10, StudyType.T);
		String trialInstanceNumber = ImportStudyUtil.getTrialInstanceNo(workbook, "filename-11");
		Assert.assertEquals("The trial instance number should be 11", "11", trialInstanceNumber);
	}

}
