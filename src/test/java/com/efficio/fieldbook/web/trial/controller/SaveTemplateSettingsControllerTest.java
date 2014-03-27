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
package com.efficio.fieldbook.web.trial.controller;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.TrialDataset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * The Class NurseryDetailsTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class SaveTemplateSettingsControllerTest extends AbstractJUnit4SpringContextTests {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SaveTemplateSettingsControllerTest.class);
    
    /** The workbench service. */
    @Autowired
    WorkbenchService workbenchService;
    
    /** The dataset. */
    TrialDataset dataset;
    
    /** The dataset name. */
    String datasetName;
    
    @Autowired
    org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
    	
		
		List<SettingDetail> nurseryLevelConditions = new ArrayList<SettingDetail>();
		nurseryLevelConditions.add(new SettingDetail(getTestSettingVariable("1"),
				new ArrayList<ValueReference>(), "Test 1", true));
		nurseryLevelConditions.add(new SettingDetail(getTestSettingVariable("2"),
				new ArrayList<ValueReference>(), "Test 2", false));
		List<SettingDetail> plotsLevelList = new ArrayList<SettingDetail>();
		plotsLevelList.add(new SettingDetail(getTestSettingVariable("3"),
				new ArrayList<ValueReference>(), "Test 3", true));
		plotsLevelList.add(new SettingDetail(getTestSettingVariable("4"),
				new ArrayList<ValueReference>(), "Test 4", false));
		
		List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
		baselineTraitsList.add(new SettingDetail(getTestSettingVariable("5"),
				new ArrayList<ValueReference>(), "Test 5", true));
		baselineTraitsList.add(new SettingDetail(getTestSettingVariable("6"),
				new ArrayList<ValueReference>(), "Test 6", false));

		List<SettingDetail> trialLevelConditions = new ArrayList<SettingDetail>();
		trialLevelConditions.add(new SettingDetail(getTestSettingVariable("7"),
				new ArrayList<ValueReference>(), "Test 7", true));
		trialLevelConditions.add(new SettingDetail(getTestSettingVariable("8"),
				new ArrayList<ValueReference>(), "Test 8", false));

		
		datasetName = "test name";
		dataset = (TrialDataset)SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, datasetName, nurseryLevelConditions, plotsLevelList, baselineTraitsList, null, 3, trialLevelConditions);
		
    }
    
    /**
     * Gets the test setting variable.
     *
     * @param prefix the prefix
     * @return the test setting variable
     */
    private SettingVariable getTestSettingVariable(String prefix){
		return new SettingVariable(prefix + " name", prefix + " description", prefix + "  property",
				prefix + "  scale", prefix + "  method", prefix + "  role", prefix + "  dataType");
	}
    
    /**
     * Test valid nursery workbook.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSaveRetrieveAndDeleteTemplateSettings() throws Exception {
    	int projectId = 1;
    	Tool tool = workbenchService.getToolWithName(AppConstants.TOOL_NAME_TRIAL_MANAGER_WEB.getString());
    	 TemplateSetting templateSetting = new TemplateSetting(null, 1, "Test Name"+System.currentTimeMillis(), tool, 
    	            SettingsUtil.generateSettingsXml(dataset), Boolean.TRUE);
    	
    	 workbenchService.addTemplateSetting(templateSetting);
    	 
    	 TemplateSetting templateSettingFilter = new TemplateSetting();
    	 Integer id = templateSetting.getTemplateSettingId();
    	 templateSettingFilter.setTemplateSettingId(id);
    	 templateSettingFilter.setIsDefaultToNull();
    	 List<TemplateSetting> dbTemplateSettingList = workbenchService.getTemplateSettings(templateSettingFilter);
    	 TemplateSetting dbTemplateSetting = dbTemplateSettingList.get(0);
    	 System.out.println(templateSetting.getConfiguration());
    	 assertEquals(templateSetting.getTemplateSettingId(), dbTemplateSetting.getTemplateSettingId());
    	 assertEquals(templateSetting.getConfiguration(), dbTemplateSetting.getConfiguration());
    	  
    	
    	 workbenchService.deleteTemplateSetting(id);
    	 dbTemplateSettingList = workbenchService.getTemplateSettings(templateSettingFilter);
    	 
    	 assertEquals(0, dbTemplateSettingList.size());
    	 
    }
    
        
}
