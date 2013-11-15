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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.service.api.ExportExcelService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;

/**
 * The Class ExcelExportServiceImpl.
 */
public class ExportExcelServiceImpl implements ExportExcelService{
    
    private static final Logger LOG = LoggerFactory.getLogger(ExportExcelServiceImpl.class);
    
	public FileOutputStream exportFieldMapToExcel(String fileName, UserFieldmap userFieldMap) throws FieldbookException{
		
		Locale locale = LocaleContextHolder.getLocale();
		
        File xls = new File(fileName); // the selected name + current date

		String blockCapacity = userFieldMap.getBlockCapacityString(); 	//"10 Columns, 10 Ranges"
		Integer columns = userFieldMap.getNumberOfColumnsInBlock(); 	// 10
		String startingCoordinates = userFieldMap.getStartingCoordinateString(); // Column 1, Range 1
		String plantingOrder = userFieldMap.getPlantingOrderString();  //plantingOrder: 1 = RowColumn, 2 = Serpentine

//		String columns = messageSource.getMessage("fieldmap.label.columns", null, locale);

		//TODO
		
		return null;
		
	}


}
