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
package com.efficio.fieldbook.web.nursery.controller;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.TemplateSetting;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
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
    
    
    @Autowired
    WorkbenchDataManager workbenchDataManager;
    Dataset dataset;
    String datasetName;
    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
    	
		
		List<SettingDetail> nurseryLevelConditions = new ArrayList<SettingDetail>();
		nurseryLevelConditions.add(new SettingDetail(getTestSettingVariable("1"),
				new ArrayList(), "Test 1", true));
		nurseryLevelConditions.add(new SettingDetail(getTestSettingVariable("2"),
				new ArrayList(), "Test 2", false));
		List<SettingDetail> plotsLevelList = new ArrayList<SettingDetail>();
		plotsLevelList.add(new SettingDetail(getTestSettingVariable("3"),
				new ArrayList(), "Test 3", true));
		plotsLevelList.add(new SettingDetail(getTestSettingVariable("4"),
				new ArrayList(), "Test 4", false));
		
		List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
		baselineTraitsList.add(new SettingDetail(getTestSettingVariable("5"),
				new ArrayList(), "Test 5", true));
		baselineTraitsList.add(new SettingDetail(getTestSettingVariable("6"),
				new ArrayList(), "Test 6", false));
		
		datasetName = "test name";
		dataset = SettingsUtil.convertPojoToXmlDataset(datasetName, nurseryLevelConditions, plotsLevelList, baselineTraitsList);
		
    }
    
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
    	Tool tool = workbenchDataManager.getToolWithName(AppConstants.TOOL_NAME_NURSERY_MANAGER_WEB);
    	 TemplateSetting templateSetting = new TemplateSetting(null, 1, "Test Name"+System.currentTimeMillis(), tool, 
    	            SettingsUtil.generateSettingsXml(dataset), Boolean.TRUE);
    	
    	 workbenchDataManager.addTemplateSetting(templateSetting);
    	 
    	 TemplateSetting templateSettingFilter = new TemplateSetting();
    	 Integer id = templateSetting.getTemplateSettingId();
    	 templateSettingFilter.setTemplateSettingId(id);
    	 templateSettingFilter.setIsDefaultToNull();
    	 List<TemplateSetting> dbTemplateSettingList = workbenchDataManager.getTemplateSettings(templateSettingFilter);
    	 TemplateSetting dbTemplateSetting = dbTemplateSettingList.get(0);
    	 
    	 assertEquals(templateSetting.getTemplateSettingId(), dbTemplateSetting.getTemplateSettingId());
    	 assertEquals(templateSetting.getConfiguration(), dbTemplateSetting.getConfiguration());
    	  
    	
    	 workbenchDataManager.deleteTemplateSetting(id);
    	 dbTemplateSettingList = workbenchDataManager.getTemplateSettings(templateSettingFilter);
    	 
    	 assertEquals(0, dbTemplateSettingList.size());
    	 
    }
    
        
}
