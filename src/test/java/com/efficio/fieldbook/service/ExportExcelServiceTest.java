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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.ExportExcelService;
import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapList;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.service.impl.HorizontalFieldMapLayoutIterator;
import com.efficio.fieldbook.web.label.printing.service.impl.VerticalFieldMapLayoutIterator;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;

public class ExportExcelServiceTest extends AbstractBaseIntegrationTest {
       
	@Autowired
	private ExportExcelService exportExcelService;
	
	private FieldMapService fieldMapService = new FieldMapServiceImpl();
	
	
	/**
	 * Generate test field map labels.
	 *
	 * @param range the range
	 * @param col the col
	 * @return the list
	 */
	private List<FieldMapLabel> generateTestFieldMapLabels(int range, int col){
		List<FieldMapLabel> labels = new ArrayList<FieldMapLabel>();
        for (int i = 0; i < range*col; i++) {
            FieldMapLabel label = new FieldMapLabel(null, null, "DummyData-" + i, i, i);
            label.setStudyName("Dummy Trial");
            labels.add(label);
        }
        return labels;
	}
	
	/**
	 * Test export field map to excel horizontal serpentine.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testExportFieldMapToExcelHorizontalSerpentine() throws Exception{
		
	    UserFieldmap userFieldMap = createUserFieldMap();
		Map<String, String> deletedPlot = createDeletedPlot();
		
		List<FieldMapLabel> fieldMapLabels = generateTestFieldMapLabels( 
		        userFieldMap.getNumberOfRangesInBlock(), userFieldMap.getNumberOfColumnsInBlock());
		List<FieldMapInfo> fieldmapInfoList = new ArrayList<FieldMapInfo>();		
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

        String fileName = "FieldMapHorizontal" +  "_" + DateUtil.getCurrentDate() + ".xls";
		exportExcelService.exportFieldMapToExcel(fileName, userFieldMap);
		
		assertEquals("Dummy Trial-76<br/>Entry null<br/>Rep 76", plots[2][9].getDisplayString());
		assertEquals("Dummy Trial-70<br/>Entry null<br/>Rep 70", plots[8][9].getDisplayString());
		assertTrue(plots[8][5].isPlotDeleted());
	}
	
	/**
	 * Test export field map to excel vertical serpentine.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testExportFieldMapToExcelVerticalSerpentine() throws Exception{
		
        UserFieldmap userFieldMap = createUserFieldMap();
        Map<String, String> deletedPlot = createDeletedPlot();

		List<FieldMapLabel> fieldMapLabels = generateTestFieldMapLabels( 
		        userFieldMap.getNumberOfRangesInBlock(), userFieldMap.getNumberOfColumnsInBlock());
		List<FieldMapInfo> fieldmapInfoList = new ArrayList<FieldMapInfo>();		
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

        String fileName = "FieldMapVertical" +  "_" + DateUtil.getCurrentDate() + ".xls";
		exportExcelService.exportFieldMapToExcel(fileName, userFieldMap);
		
		assertEquals("Dummy Trial-10<br/>Entry null<br/>Rep 10", plots[2][9].getDisplayString());
		assertEquals("Dummy Trial-67<br/>Entry null<br/>Rep 67", plots[8][9].getDisplayString());
		assertTrue(plots[8][5].isPlotDeleted());
	}
	
	/**
	 * Test export field map to excel horizontal row column.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testExportFieldMapToExcelHorizontalRowColumn() throws Exception{
		
	    UserFieldmap userFieldMap = createUserFieldMap();
        userFieldMap.setPlantingOrder(AppConstants.ROW_COLUMN.getInt());

        Map<String, String> deletedPlot = createDeletedPlot();

		List<FieldMapLabel> fieldMapLabels = generateTestFieldMapLabels( 
		        userFieldMap.getNumberOfRangesInBlock(), userFieldMap.getNumberOfColumnsInBlock());
		List<FieldMapInfo> fieldmapInfoList = new ArrayList<FieldMapInfo>();		
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

        String fileName = "FieldMapHorizontalRowColumn" +  "_" + DateUtil.getCurrentDate() + ".xls";
		exportExcelService.exportFieldMapToExcel(fileName, userFieldMap);
		assertEquals("Dummy Trial-77<br/>Entry null<br/>Rep 77", plots[2][9].getDisplayString());
		assertEquals("Dummy Trial-83<br/>Entry null<br/>Rep 83", plots[8][9].getDisplayString());
		assertTrue(plots[8][5].isPlotDeleted());
	}
	
	/**
	 * Test export field map to excel vertical row column.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testExportFieldMapToExcelVerticalRowColumn() throws Exception{

		UserFieldmap userFieldMap = createUserFieldMap();
        userFieldMap.setPlantingOrder(AppConstants.ROW_COLUMN.getInt());

        Map<String, String> deletedPlot = createDeletedPlot();

		List<FieldMapLabel> fieldMapLabels = generateTestFieldMapLabels( 
		        userFieldMap.getNumberOfRangesInBlock(), userFieldMap.getNumberOfColumnsInBlock());
		List<FieldMapInfo> fieldmapInfoList = new ArrayList<FieldMapInfo>();		
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

        String fileName = "FieldMapVerticalRowColumn" +  "_" + DateUtil.getCurrentDate() + ".xls";
		exportExcelService.exportFieldMapToExcel(fileName, userFieldMap);
		assertEquals("Dummy Trial-17<br/>Entry null<br/>Rep 17", plots[2][9].getDisplayString());
		assertEquals("Dummy Trial-74<br/>Entry null<br/>Rep 74", plots[8][9].getDisplayString());
		assertTrue(plots[8][5].isPlotDeleted());
	}
	
	private UserFieldmap createUserFieldMap(){
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
	        userFieldMap.setPlantingOrder(AppConstants.SERPENTINE.getInt());
	        userFieldMap.setNumberOfRangesInBlock(10);
	        
	        return userFieldMap;
	}
	
	private Map<String, String> createDeletedPlot(){
	    Map<String, String> deletedPlot = new HashMap<String, String>(); // key = "<col>_<range>"
        deletedPlot.put("2_2", "X");
        deletedPlot.put("4_7", "X");
        deletedPlot.put("6_1", "X");
        deletedPlot.put("8_5", "X");
        return deletedPlot;
	}
		
}
