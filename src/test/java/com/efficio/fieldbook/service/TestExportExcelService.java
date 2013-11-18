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
package com.efficio.fieldbook.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.service.api.ExportExcelService;
import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class TestExportExcelService extends AbstractJUnit4SpringContextTests {
    
    
	@Autowired
	private ExportExcelService exportExcelService;
	
	@Autowired
	private FieldMapService fieldMapService;
	
	@Test
	public void testExportFieldMapToExcel() throws Exception{
		
		UserFieldmap userFieldMap = new UserFieldmap();
		userFieldMap.setSelectedName("Field Map Name");
		userFieldMap.setNumberOfEntries(25L);
		userFieldMap.setNumberOfReps(3L);
		userFieldMap.setTotalNumberOfPlots(75L);
		userFieldMap.setLocationName("IFPRI"); 
		userFieldMap.setFieldName("Field 1");
		userFieldMap.setBlockName("Block 1");
		userFieldMap.setNumberOfRowsInBlock(20);
		userFieldMap.setNumberOfRowsPerPlot(2);
		userFieldMap.setStartingColumn(2);
		userFieldMap.setStartingRange(2);
		userFieldMap.setPlantingOrder(userFieldMap.SERPENTINE);
		userFieldMap.setNumberOfRangesInBlock(10);

		Map<String, String> deletedPlot = new HashMap<String, String>(); // key = "<col>_<range>"
		deletedPlot.put("2_2", "X");
		deletedPlot.put("4_7", "X");
		deletedPlot.put("6_1", "X");
		deletedPlot.put("8_5", "X");

		Plot[][] plots = fieldMapService.createDummyData(userFieldMap.getNumberOfColumnsInBlock()
				, userFieldMap.getNumberOfRangesInBlock()
				, userFieldMap.getStartingRange()
				, userFieldMap.getStartingColumn()
				, userFieldMap.isSerpentine()
				, deletedPlot);
		userFieldMap.setFieldmap(plots);

	    String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = "FieldMap" +  "_" + currentDate + ".xls";
		exportExcelService.exportFieldMapToExcel(fileName, userFieldMap);
	}
	
}
