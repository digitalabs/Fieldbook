package com.efficio.fieldbook.web.common.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.StudySelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.common.service.DataKaptureExportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.common.service.ExportDataCollectionOrderService;
import com.efficio.fieldbook.web.common.service.FieldroidExportStudyService;
import com.efficio.fieldbook.web.common.service.KsuCsvExportStudyService;
import com.efficio.fieldbook.web.common.service.KsuExceIExportStudyService;
import com.efficio.fieldbook.web.common.service.RExportStudyService;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingRowColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverRangeImpl;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.TrialSelection;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Controller
@RequestMapping(ExportStudyController.URL)
public class ExportStudyController extends AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(ExportStudyController.class);
    public static final String URL = "/ExportManager";
    private static final int BUFFER_SIZE = 4096 * 4;

    @Resource
    private UserSelection nurserySelection;
    
    @Resource
    private TrialSelection trialSelection;
    
    @Resource
    private FieldroidExportStudyService fielddroidExportStudyService;
    
    @Resource
    private RExportStudyService rExportStudyService;
    
    @Resource
    private ExcelExportStudyService excelExportStudyService;
    
    @Resource
    private DataKaptureExportStudyService dataKaptureExportStudyService;
    
    @Resource
    private KsuExceIExportStudyService ksuExcelExportStudyService;
    
    @Resource
    private KsuCsvExportStudyService ksuCsvExportStudyService;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private ExportOrderingRowColImpl exportOrderingRowColService;
    @Resource
    private ExportOrderingSerpentineOverRangeImpl exportOrderingSerpentineOverRangeService;
    @Resource
    private ExportOrderingSerpentineOverColImpl exportOrderingSerpentineOverColumnService;
    
    @Override
	public String getContentName() {
		return null;
	}

    @ResponseBody
    @RequestMapping(value="/export/{exportType}/{selectedTraitTermId}/{exportWayType}", method = RequestMethod.GET)
    public String exportRFileForNursery(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form,  
@PathVariable int exportType, @PathVariable int selectedTraitTermId,
@PathVariable int exportWayType,
HttpServletRequest req, HttpServletResponse response) {
    	boolean isTrial = false;
    	return doExport(exportType, selectedTraitTermId, response, isTrial,0,0,exportWayType,req);
    	
    }
    
    @ResponseBody
    @RequestMapping(value="/export/{exportType}/{exportWayType}", method = RequestMethod.GET)
    public String exportFile(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, 
    		@PathVariable int exportType, 
    		@PathVariable int exportWayType,
    		HttpServletRequest req, HttpServletResponse response) {
    	boolean isTrial = false;
        return doExport(exportType, 0, response, isTrial,0,0, exportWayType,req);
    	
    }
    
    @ResponseBody
    @RequestMapping(value="/exportTrial/{exportType}/{selectedTraitTermId}/{instanceNumberStart}/{instanceNumberEnd}/{exportWayType}", method = RequestMethod.GET)
    public String exportRFileForTrial(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, @PathVariable int exportType, 
    		@PathVariable int selectedTraitTermId, @PathVariable int instanceNumberStart, 
    		@PathVariable int instanceNumberEnd, 
    		@PathVariable int exportWayType,
    		HttpServletRequest req, HttpServletResponse response) {
    	boolean isTrial = true;
    	
    	return doExport(exportType, selectedTraitTermId, response, isTrial, instanceNumberStart, instanceNumberEnd, exportWayType, req);
    	
    }
    
    @ResponseBody
    @RequestMapping(value="/exportTrial/{exportType}/{instanceNumberStart}/{instanceNumberEnd}/{exportWayType}", method = RequestMethod.GET)
    public String exportFileTrial(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form,  
    		@PathVariable int exportType,  @PathVariable int instanceNumberStart,
    		@PathVariable int instanceNumberEnd,
    		@PathVariable int exportWayType, 
    		HttpServletRequest req, HttpServletResponse response) {
    	boolean isTrial = true;
        return doExport(exportType, 0, response, isTrial, instanceNumberStart, instanceNumberEnd, exportWayType, req);
    	
    }
   
    @ResponseBody
    @RequestMapping(value="/study/hasFieldMap", method = RequestMethod.GET)
    public String hasFieldMap(HttpServletRequest req, HttpServletResponse response) {
    	StudySelection userSelection = getUserSelection(false);    	
    	userSelection.getWorkbook().getTotalNumberOfInstances();     	    	
    	Integer datasetId = userSelection.getWorkbook().getMeasurementDatesetId();
    	boolean hasFieldMap = false;
		try {
			hasFieldMap = fieldbookMiddlewareService.hasFieldMap(datasetId);
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return hasFieldMap ? "1" : "0";
    }
    @ResponseBody
    @RequestMapping(value="/studyTrial/hasFieldMap", method = RequestMethod.GET)
    public String hasTrialFieldMap(HttpServletRequest req, HttpServletResponse response) {
    	StudySelection userSelection = getUserSelection(false);    	
    	userSelection.getWorkbook().getTotalNumberOfInstances();     	    	
    	Integer datasetId = userSelection.getWorkbook().getMeasurementDatesetId();
    	return datasetId.toString();
    }
    
    /**
     * Do export.
     *
     * @param exportType the export type
     * @param selectedTraitTermId the selected trait term id
     * @param response the response
     * @return the string
     */
    private String doExport(int exportType, int selectedTraitTermId, 
    		HttpServletResponse response, boolean isTrial, int start, int end, int exportWayType, HttpServletRequest req){
    	
    	/*
    	 * exportWayType
    	 * 1 - row column
    	 * 2 - serpentine (range)
    	 * 3 - serpentine (col)
    	 */
    	ExportDataCollectionOrderService exportDataCollectionService = getExportOrderService(exportWayType);
    	
    	
    	StudySelection userSelection = getUserSelection(isTrial);
    	if (start == 0 || end == 0) { //all
    		start = 1;
    		end = userSelection.getWorkbook().getTotalNumberOfInstances(); 
    	}
    	Workbook workbook = userSelection.getWorkbook();
    	
    	
    	exportDataCollectionService.reorderWorkbook(workbook);
    	
    	String filename = userSelection.getWorkbook().getStudyDetails().getStudyName();
    	String outputFilename = null;
    	if(AppConstants.EXPORT_NURSERY_FIELDLOG_FIELDROID.getInt() == exportType){
    		filename = filename  + AppConstants.EXPORT_FIELDLOG_SUFFIX.getString();
    		outputFilename = fielddroidExportStudyService.export(userSelection.getWorkbook(), filename, start, end);
    		response.setContentType("text/csv");
    	}else if(AppConstants.EXPORT_NURSERY_R.getInt() == exportType){
    		filename = filename  + AppConstants.EXPORT_R_SUFFIX.getString();
    		outputFilename = rExportStudyService.exportToR(userSelection.getWorkbook(), filename, selectedTraitTermId, start, end);    		
    		response.setContentType("text/csv");
    	}else if(AppConstants.EXPORT_NURSERY_EXCEL.getInt() == exportType){
    		filename = filename  + AppConstants.EXPORT_XLS_SUFFIX.getString();
    		outputFilename = excelExportStudyService.export(userSelection.getWorkbook(), filename, start, end);
    		if (end - start > 0) {
        		int extensionIndex = filename.lastIndexOf(".");
        		filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
        		response.setContentType("application/zip");
        	} else {
        		response.setContentType("application/vnd.ms-excel");
        	}
    	}else if(AppConstants.EXPORT_DATAKAPTURE.getInt() == exportType) {
    		outputFilename = dataKaptureExportStudyService.export(userSelection.getWorkbook(), filename, start, end);
    		response.setContentType("application/zip");
    		filename = filename + AppConstants.ZIP_FILE_SUFFIX.getString();
    	}else if (AppConstants.EXPORT_KSU_EXCEL.getInt() == exportType) {
    		filename = filename + AppConstants.EXPORT_XLS_SUFFIX.getString();
    		outputFilename = ksuExcelExportStudyService.export(userSelection.getWorkbook(), filename, start, end);
    		if (end - start > 0) {
        		int extensionIndex = filename.lastIndexOf(".");
        		filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
        		response.setContentType("application/zip");
    		}
    		else {
        		response.setContentType("application/vnd.ms-excel");
    		}
    	}else if (AppConstants.EXPORT_KSU_CSV.getInt() == exportType) {
    		filename = filename + AppConstants.EXPORT_CSV_SUFFIX.getString();
    		outputFilename = ksuCsvExportStudyService.export(userSelection.getWorkbook(), filename, start, end);
    		if (end - start > 0) {
        		int extensionIndex = filename.lastIndexOf(".");
        		filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
        		response.setContentType("application/zip");
    		}
    		else {
    			response.setContentType("text/csv");
    		}
    	}
    	
        File xls = new File(outputFilename); // the selected name + current date
        FileInputStream in;
        
        response.setHeader("Content-disposition","attachment; filename=" + SettingsUtil.cleanSheetAndFileName(filename));
        try {
            in = new FileInputStream(xls);
            OutputStream out = response.getOutputStream();

            byte[] buffer= new byte[BUFFER_SIZE]; // use bigger if you want
            int length = 0;

            while ((length = in.read(buffer)) > 0){
                 out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
        	LOG.error(e.getMessage(), e);
        } catch (IOException e) {
        	LOG.error(e.getMessage(), e);
        }
       
        return "";
    }
    
    private StudySelection getUserSelection(boolean isTrial) {
    	return isTrial ? this.trialSelection : this.nurserySelection;
    }
    
    private ExportDataCollectionOrderService getExportOrderService(int exportWayType){
    	if(exportWayType == 1){
    		return exportOrderingRowColService;
    	}else if(exportWayType == 2){
    		return exportOrderingSerpentineOverRangeService;
    	}else if(exportWayType == 3){
    		return exportOrderingSerpentineOverColumnService;
    	}
    	return exportOrderingRowColService;
    }
}
