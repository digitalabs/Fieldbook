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

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.service.api.ExportExcelService;
import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class TestExportExcelService extends AbstractJUnit4SpringContextTests {
    
    
	@Autowired
	private ExportExcelService exportExcelService;
	
	//@Autowired
	private FieldMapService fieldMapService = new FieldMapServiceImpl();
	
	
	private List<FieldMapLabel> generateTestFieldMapLabels(int range, int col){
		List<FieldMapLabel> labels = new ArrayList<FieldMapLabel>();
        for (int i = 0; i < range*col; i++) {
            FieldMapLabel label = new FieldMapLabel(null, null, "DummyData-" + i, i, null);
            label.setStudyName("Dummy Trial");
            labels.add(label);
        }
        return labels;
	}
	@Test
	public void testExportFieldMapToExcelHorizontalSerpentine() throws Exception{
		
		UserFieldmap userFieldMap = new UserFieldmap();
		//userFieldMap.setSelectedName("Field Map Name");
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
		List<FieldMapLabel> fieldMapLabels = generateTestFieldMapLabels( userFieldMap.getNumberOfRangesInBlock(), userFieldMap.getNumberOfColumnsInBlock());
		List fieldmapInfoList = new ArrayList<FieldMapInfo>();		
		fieldmapInfoList.add(new FieldMapInfo(Integer.parseInt("1"), "1", fieldMapLabels));			
	    userFieldMap.setMachineRowCapacity(2);
		  	    
	    userFieldMap.setSelectedFieldmapList(new SelectedFieldmapList(fieldmapInfoList, false));
		Plot[][] plots = fieldMapService.createDummyData(userFieldMap.getNumberOfColumnsInBlock()
				, userFieldMap.getNumberOfRangesInBlock()
				, userFieldMap.getStartingRange()
				, userFieldMap.getStartingColumn()
				, userFieldMap.isSerpentine()
				, deletedPlot, fieldMapLabels, new HorizontalFieldMapLayoutIterator());
		userFieldMap.setFieldmap(plots);

	    String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = "FieldMapHorizontal" +  "_" + currentDate + ".xls";
		exportExcelService.exportFieldMapToExcel(fileName, userFieldMap);
		
		assertEquals("Dummy Trial<br/>Entry null<br/>Rep 76", plots[2][9].getDisplayString());
		assertEquals("Dummy Trial<br/>Entry null<br/>Rep 70", plots[8][9].getDisplayString());
		assertEquals(true, plots[8][5].isPlotDeleted());
	}
	
	@Test
	public void testExportFieldMapToExcelVerticalSerpentine() throws Exception{
		
		UserFieldmap userFieldMap = new UserFieldmap();
		//userFieldMap.setSelectedName("Field Map Name");
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
		List<FieldMapLabel> fieldMapLabels = generateTestFieldMapLabels( userFieldMap.getNumberOfRangesInBlock(), userFieldMap.getNumberOfColumnsInBlock());
		List fieldmapInfoList = new ArrayList<FieldMapInfo>();		
		fieldmapInfoList.add(new FieldMapInfo(Integer.parseInt("1"), "1", fieldMapLabels));			
	    userFieldMap.setMachineRowCapacity(2);
		  	    
	    userFieldMap.setSelectedFieldmapList(new SelectedFieldmapList(fieldmapInfoList, false));
		Plot[][] plots = fieldMapService.createDummyData(userFieldMap.getNumberOfColumnsInBlock()
				, userFieldMap.getNumberOfRangesInBlock()
				, userFieldMap.getStartingRange()
				, userFieldMap.getStartingColumn()
				, userFieldMap.isSerpentine()
				, deletedPlot, fieldMapLabels, new VerticalFieldMapLayoutIterator());
		userFieldMap.setFieldmap(plots);

	    String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = "FieldMapVertical" +  "_" + currentDate + ".xls";
		exportExcelService.exportFieldMapToExcel(fileName, userFieldMap);
		
		assertEquals("Dummy Trial<br/>Entry null<br/>Rep 10", plots[2][9].getDisplayString());
		assertEquals("Dummy Trial<br/>Entry null<br/>Rep 67", plots[8][9].getDisplayString());
		assertEquals(true, plots[8][5].isPlotDeleted());
	}
	
	@Test
	public void testExportFieldMapToExcelHorizontalRowColumn() throws Exception{
		
		UserFieldmap userFieldMap = new UserFieldmap();
		//userFieldMap.setSelectedName("Field Map Name");
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
		userFieldMap.setPlantingOrder(userFieldMap.ROW_COLUMN);
		userFieldMap.setNumberOfRangesInBlock(10);

		Map<String, String> deletedPlot = new HashMap<String, String>(); // key = "<col>_<range>"
		deletedPlot.put("2_2", "X");
		deletedPlot.put("4_7", "X");
		deletedPlot.put("6_1", "X");
		deletedPlot.put("8_5", "X");
		List<FieldMapLabel> fieldMapLabels = generateTestFieldMapLabels( userFieldMap.getNumberOfRangesInBlock(), userFieldMap.getNumberOfColumnsInBlock());
		List fieldmapInfoList = new ArrayList<FieldMapInfo>();		
		fieldmapInfoList.add(new FieldMapInfo(Integer.parseInt("1"), "1", fieldMapLabels));			
	    userFieldMap.setMachineRowCapacity(2);
		  	    
	    userFieldMap.setSelectedFieldmapList(new SelectedFieldmapList(fieldmapInfoList, false));
		Plot[][] plots = fieldMapService.createDummyData(userFieldMap.getNumberOfColumnsInBlock()
				, userFieldMap.getNumberOfRangesInBlock()
				, userFieldMap.getStartingRange()
				, userFieldMap.getStartingColumn()
				, userFieldMap.isSerpentine()
				, deletedPlot, fieldMapLabels, new HorizontalFieldMapLayoutIterator());
		userFieldMap.setFieldmap(plots);

	    String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = "FieldMapHorizontalRowColumn" +  "_" + currentDate + ".xls";
		exportExcelService.exportFieldMapToExcel(fileName, userFieldMap);
		assertEquals("Dummy Trial<br/>Entry null<br/>Rep 77", plots[2][9].getDisplayString());
		assertEquals("Dummy Trial<br/>Entry null<br/>Rep 83", plots[8][9].getDisplayString());
		assertEquals(true, plots[8][5].isPlotDeleted());
	}
	
	@Test
	public void testExportFieldMapToExcelVerticalRowColumn() throws Exception{
		
		UserFieldmap userFieldMap = new UserFieldmap();
		//userFieldMap.setSelectedName("Field Map Name");
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
		userFieldMap.setPlantingOrder(userFieldMap.ROW_COLUMN);
		userFieldMap.setNumberOfRangesInBlock(10);

		Map<String, String> deletedPlot = new HashMap<String, String>(); // key = "<col>_<range>"
		deletedPlot.put("2_2", "X");
		deletedPlot.put("4_7", "X");
		deletedPlot.put("6_1", "X");
		deletedPlot.put("8_5", "X");
		List<FieldMapLabel> fieldMapLabels = generateTestFieldMapLabels( userFieldMap.getNumberOfRangesInBlock(), userFieldMap.getNumberOfColumnsInBlock());
		List fieldmapInfoList = new ArrayList<FieldMapInfo>();		
		fieldmapInfoList.add(new FieldMapInfo(Integer.parseInt("1"), "1", fieldMapLabels));			
	    userFieldMap.setMachineRowCapacity(2);
		  	    
	    userFieldMap.setSelectedFieldmapList(new SelectedFieldmapList(fieldmapInfoList, false));
		Plot[][] plots = fieldMapService.createDummyData(userFieldMap.getNumberOfColumnsInBlock()
				, userFieldMap.getNumberOfRangesInBlock()
				, userFieldMap.getStartingRange()
				, userFieldMap.getStartingColumn()
				, userFieldMap.isSerpentine()
				, deletedPlot, fieldMapLabels, new VerticalFieldMapLayoutIterator());
		userFieldMap.setFieldmap(plots);

	    String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = "FieldMapVerticalRowColumn" +  "_" + currentDate + ".xls";
		exportExcelService.exportFieldMapToExcel(fileName, userFieldMap);
		assertEquals("Dummy Trial<br/>Entry null<br/>Rep 17", plots[2][9].getDisplayString());
		assertEquals("Dummy Trial<br/>Entry null<br/>Rep 74", plots[8][9].getDisplayString());
		assertEquals(true, plots[8][5].isPlotDeleted());
	}
	
}
