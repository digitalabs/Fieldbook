/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.label.printing.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.AbstractBaseControllerTest;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;

public class LabelPrintingControllerTest extends AbstractBaseControllerTest {
	
	@Resource
	private LabelPrintingController labelPrintingController;
	
	@Test
    public void testGenerationOfPDFLabels() {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_PDF.getString());
		labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		
		Map<String,Object> results = labelPrintingController.generateLabels(trialInstances);
		
		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertTrue("Expected pdf file generated but found " + results.get("fileName").toString(), 
				results.get("fileName").toString().contains("pdf"));
	}
	
	@Test
    public void testGenerationOfXLSLabels() {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_EXCEL.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_EXCEL.getString());
		labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		
		Map<String,Object> results = labelPrintingController.generateLabels(trialInstances);
		
		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertTrue("Expected xls file generated but found " + results.get("fileName").toString(), 
				results.get("fileName").toString().contains("xls"));
	}
	
	@Test
    public void testGenerationOfCSVLabels() {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_CSV.getString());
		labelPrintingController.setUserLabelPrinting(userLabelPrinting);
		
		Map<String,Object> results = labelPrintingController.generateLabels(trialInstances);
		
		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertTrue("Expected csv file generated but found " + results.get("fileName").toString(), 
				results.get("fileName").toString().contains("csv"));
	}
}
