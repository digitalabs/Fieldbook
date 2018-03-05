/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package com.efficio.fieldbook.web.common.service;

import java.io.IOException;
import java.util.List;

import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.middleware.domain.etl.Workbook;


public interface ExportStudyService {

	FileExportInfo export(Workbook workbook, String filename, List<Integer> instances) throws IOException;
	
	FileExportInfo export(Workbook workbook, String filename, List<Integer> instances, List<Integer> visibleColumns) throws IOException;

}
