/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.label.printing.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;

public class LabelPrintingControllerTest extends AbstractBaseControllerIntegrationTest {

	@Resource
	private LabelPrintingController labelPrintingController;

	@Test
	public void testGenerationOfPDFLabels() {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_PDF.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertTrue("Expected pdf file generated but found " + results.get("fileName").toString(), results.get("fileName").toString()
				.contains("pdf"));
	}

	@Test
	public void testGenerationOfXLSLabels() {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_EXCEL.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_EXCEL.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertTrue("Expected xls file generated but found " + results.get("fileName").toString(), results.get("fileName").toString()
				.contains("xls"));
	}

	@Test
	public void testGenerationOfCSVLabels() {
		List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
		userLabelPrinting.setGenerateType(AppConstants.LABEL_PRINTING_CSV.getString());
		this.labelPrintingController.setUserLabelPrinting(userLabelPrinting);

		Map<String, Object> results = this.labelPrintingController.generateLabels(trialInstances, false);

		Assert.assertNotNull("Expected results but found none", results);
		Assert.assertTrue("Expected csv file generated but found " + results.get("fileName").toString(), results.get("fileName").toString()
				.contains("csv"));
	}

	@Test
	public void testGetSelectedLabelFieldsForPDF() {
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
		String selectedLabelFields = this.labelPrintingController.getSelectedLabelFields(userLabelPrinting);
		String expected = userLabelPrinting.getLeftSelectedLabelFields() + "," + userLabelPrinting.getRightSelectedLabelFields();
		Assert.assertEquals("Expecting the return results is " + expected + " but returned " + selectedLabelFields, expected,
				selectedLabelFields);
	}

	@Test
	public void testGetSelectedLabelFieldsForCSV() {
		UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
		String selectedLabelFields = this.labelPrintingController.getSelectedLabelFields(userLabelPrinting);
		String expected = userLabelPrinting.getMainSelectedLabelFields();
		Assert.assertEquals("Expecting the return results is " + expected + " but returned " + selectedLabelFields, expected,
				selectedLabelFields);
	}

	@Test
	public void testGetLabelPrintingCustomReportsIfThereIsStudyId() throws MiddlewareQueryException {
		LabelPrintingController controller = new LabelPrintingController();
		WorkbenchService workbenchService = Mockito.mock(WorkbenchService.class);
		Integer studyId = new Integer(3);
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setStudyId(studyId);
		controller.setUserLabelPrinting(userLabelPrinting);
		controller.setWorkbenchService(workbenchService);
		CrossExpansionProperties crossExpansionProperties = new CrossExpansionProperties();
		crossExpansionProperties.setProfile("Cimmyt");
		controller.setCrossExpansionProperties(crossExpansionProperties);
		List<StandardPreset> standardPresets = new ArrayList<StandardPreset>();
		StandardPreset preset = new StandardPreset();
		preset.setConfiguration("<reports><profile>cimmyt</profile><report><code>WLBL05</code><name>labels without design, wheat</name></report><report><code>WLBL21</code><name>labels with design, wheat</name></report></reports>");
		standardPresets.add(preset);
		Mockito.when(workbenchService.getStandardPresetByCrop(Matchers.anyInt(), Matchers.anyString(), Matchers.anyString())).thenReturn(
				standardPresets);
		Tool fbTool = new Tool();
		fbTool.setToolId(new Long(1));
		Mockito.when(workbenchService.getFieldbookWebTool()).thenReturn(fbTool);
		ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		Project project = new Project();
		CropType cropType = new CropType();
		cropType.setCropName("Test");
		project.setCropType(cropType);
		Mockito.when(contextUtil.getProjectInContext()).thenReturn(project);
		controller.setContextUtil(contextUtil);
		List<CustomReportType> presets = controller.getLabelPrintingCustomReports();
		Assert.assertEquals("Should return 2 presets since there is a study", 2, presets.size());
	}

	@Test
	public void testGetLabelPrintingCustomReportsIfThereIsNoStudyId() throws MiddlewareQueryException {
		LabelPrintingController controller = new LabelPrintingController();
		WorkbenchService workbenchService = Mockito.mock(WorkbenchService.class);
		Integer studyId = null;
		UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
		userLabelPrinting.setStudyId(studyId);
		controller.setUserLabelPrinting(userLabelPrinting);
		controller.setWorkbenchService(workbenchService);
		List<CustomReportType> presets = controller.getLabelPrintingCustomReports();
		Assert.assertEquals("Should return no preset since there is not study", 0, presets.size());
	}
}
