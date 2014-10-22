package com.efficio.fieldbook.web.common.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.common.service.DataKaptureExportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelExportStudyService;
import com.efficio.fieldbook.web.common.service.ExportDataCollectionOrderService;
import com.efficio.fieldbook.web.common.service.FieldroidExportStudyService;
import com.efficio.fieldbook.web.common.service.KsuCsvExportStudyService;
import com.efficio.fieldbook.web.common.service.KsuExcelExportStudyService;
import com.efficio.fieldbook.web.common.service.RExportStudyService;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingRowColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverColImpl;
import com.efficio.fieldbook.web.common.service.impl.ExportOrderingSerpentineOverRangeImpl;
import com.efficio.fieldbook.web.trial.bean.ExportTrialInstanceBean;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Controller
@RequestMapping(ExportStudyController.URL)
public class ExportStudyController extends AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(ExportStudyController.class);
    public static final String URL = "/ExportManager";
    private static final int BUFFER_SIZE = 4096 * 4;
    private static String EXPORT_TRIAL_INSTANCE = "Common/includes/exportTrialInstance";

    @Resource
    private UserSelection studySelection;
    
    @Resource
    private FieldroidExportStudyService fielddroidExportStudyService;
    
    @Resource
    private RExportStudyService rExportStudyService;
    
    @Resource
    private ExcelExportStudyService excelExportStudyService;
    
    @Resource
    private DataKaptureExportStudyService dataKaptureExportStudyService;
    
    @Resource
    private KsuExcelExportStudyService ksuExcelExportStudyService;
    
    @Resource
    private KsuCsvExportStudyService ksuCsvExportStudyService;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private OntologyService ontologyService;
    
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
    @RequestMapping(value="/download/file", method = RequestMethod.GET)
    public String downloadFile(HttpServletRequest req, HttpServletResponse response) {
    	String outputFilename = req.getParameter("outputFilename");
    	String filename = req.getParameter("filename");
    	String contentType = req.getParameter("contentType");
    	
        File xls = new File(outputFilename); // the selected name + current date
        FileInputStream in;
        
        response.setHeader("Content-disposition","attachment; filename=" + SettingsUtil.cleanSheetAndFileName(filename));
        response.setContentType(contentType);
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
    
    @ResponseBody
    @RequestMapping(value="/export/{exportType}/{selectedTraitTermId}/{exportWayType}", method = RequestMethod.GET)
    public String exportRFileForNursery(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form,  
@PathVariable int exportType, @PathVariable int selectedTraitTermId,
@PathVariable int exportWayType,
HttpServletRequest req, HttpServletResponse response) throws MiddlewareQueryException {
    	boolean isTrial = false;
    	List<Integer> instancesList = new ArrayList();
    	instancesList.add(1);
    	return doExport(exportType, selectedTraitTermId, response, isTrial,instancesList,exportWayType,req);
    	
    }
    
    @ResponseBody
    @RequestMapping(value="/export/{exportType}/{exportWayType}", method = RequestMethod.GET)
    public String exportFile(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, 
    		@PathVariable int exportType, 
    		@PathVariable int exportWayType,
    		HttpServletRequest req, HttpServletResponse response) throws MiddlewareQueryException {
    	boolean isTrial = false;
    	List<Integer> instancesList = new ArrayList();
    	instancesList.add(1);
        return doExport(exportType, 0, response, isTrial,instancesList, exportWayType,req);
    	
    }
    
    @ResponseBody
    @RequestMapping(value="/exportTrial/{exportType}/{selectedTraitTermId}/{instances}/{exportWayType}", method = RequestMethod.GET)
    public String exportRFileForTrial(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, @PathVariable int exportType, 
    		@PathVariable int selectedTraitTermId, @PathVariable String instances, 
    		@PathVariable int exportWayType,
    		HttpServletRequest req, HttpServletResponse response) throws MiddlewareQueryException {
    	boolean isTrial = true;
    	List<Integer> instancesList = new ArrayList();
    	StringTokenizer tokenizer = new StringTokenizer(instances, "|");
    	while(tokenizer.hasMoreTokens()){
    		instancesList.add(Integer.valueOf(tokenizer.nextToken()));
    	}
    	return doExport(exportType, selectedTraitTermId, response, isTrial, instancesList, exportWayType, req);
    	
    }
    
    @ResponseBody
    @RequestMapping(value="/exportTrial/{exportType}/{instances}/{exportWayType}", method = RequestMethod.GET)
    public String exportFileTrial(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form,  
    		@PathVariable int exportType,  @PathVariable String instances,
    		@PathVariable int exportWayType, 
    		HttpServletRequest req, HttpServletResponse response) throws MiddlewareQueryException {
    	boolean isTrial = true;
    	List<Integer> instancesList = new ArrayList();
    	StringTokenizer tokenizer = new StringTokenizer(instances, "|");
    	while(tokenizer.hasMoreTokens()){
    		instancesList.add(Integer.valueOf(tokenizer.nextToken()));
    	}
        return doExport(exportType, 0, response, isTrial, instancesList, exportWayType, req);
    	
    }
   
    @ResponseBody
    @RequestMapping(value="/study/hasFieldMap", method = RequestMethod.GET)
    public String hasFieldMap(HttpServletRequest req, HttpServletResponse response) {
    	String studyId = req.getParameter("studyId");
    	UserSelection userSelection = getUserSelection();    	
    	boolean hasFieldMap = false;
		try {
			Workbook workbook = null;
			if("0".equalsIgnoreCase(studyId)){
				
				workbook = userSelection.getWorkbook();
				studyId = workbook.getStudyDetails().getId().toString();
			}else{
				//meaning for the session
				workbook = this.getPaginationListSelection().getReviewWorkbook(studyId);				
			}
			hasFieldMap = fieldbookMiddlewareService.checkIfStudyHasFieldmap(Integer.valueOf(studyId));
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return hasFieldMap ? "1" : "0";
    }
    @ResponseBody
    @RequestMapping(value="/studyTrial/hasFieldMap", method = RequestMethod.GET)
    public String hasTrialFieldMap(HttpServletRequest req, HttpServletResponse response) {
    	UserSelection userSelection = getUserSelection();    	
    	userSelection.getWorkbook().getTotalNumberOfInstances();     	    	
    	Integer datasetId = userSelection.getWorkbook().getMeasurementDatesetId();
    	return datasetId.toString();
    }
    
    @ResponseBody
    @RequestMapping(value="/study/traits", method = RequestMethod.GET)
    public String getStudyTraits(HttpServletRequest req, HttpServletResponse response) {
    	String studyId = req.getParameter("studyId");
    	
    	UserSelection userSelection = getUserSelection();    	
    	List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
		try {
			List<MeasurementVariable> tempVariates = new ArrayList<MeasurementVariable>();
			if("0".equalsIgnoreCase(studyId)){
			
				tempVariates = userSelection.getWorkbook().getMeasurementDatasetVariables();
				
			}else{
				//meaning for the session
				Workbook workbook = this.getPaginationListSelection().getReviewWorkbook(studyId);
				tempVariates = workbook.getVariates();
			}
			
			for(MeasurementVariable var : tempVariates){
				if(var.isFactor() == false){
					variates.add(var);
				}
			}
	    	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return super.convertObjectToJson(variates);
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
    		HttpServletResponse response, boolean isTrial, List<Integer> instances, int exportWayType, HttpServletRequest req) 
    		        throws MiddlewareQueryException {
    	
    	/*
    	 * exportWayType
    	 * 1 - row column
    	 * 2 - serpentine (range)
    	 * 3 - serpentine (col)
    	 */
    	ExportDataCollectionOrderService exportDataCollectionService = getExportOrderService(exportWayType);
    	
    	UserSelection userSelection = getUserSelection();
    	try {
	    	String studyId = req.getParameter("studyExportId");
	    	if(!"0".equalsIgnoreCase(studyId)){
	    		//we need to get the workbook and set it in the userSelectionObject
	    		Workbook workbookSession = null;
	    		
				if(getPaginationListSelection().getReviewFullWorkbook(studyId) == null){
					if(isTrial){
						workbookSession = fieldbookMiddlewareService.getTrialDataSet(Integer.valueOf(studyId));
					}else{
						workbookSession = fieldbookMiddlewareService.getNurseryDataSet(Integer.valueOf(studyId));
					}
					SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbookSession.getObservations(), false, ontologyService);
					
					getPaginationListSelection().addReviewFullWorkbook(studyId, workbookSession);
				}else{
					workbookSession = getPaginationListSelection().getReviewFullWorkbook(studyId);
				}
	    		
	    		userSelection.setWorkbook(workbookSession);
	    	}
    	} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	Workbook workbook = userSelection.getWorkbook();
    	
    	SettingsUtil.resetBreedingMethodValueToCode(fieldbookMiddlewareService, workbook.getObservations(), true, ontologyService);
    	
    	exportDataCollectionService.reorderWorkbook(workbook);
    	
    	String filename = HtmlUtils.htmlUnescape(userSelection.getWorkbook().getStudyDetails().getStudyName());
    	String outputFilename = null;
    	if(AppConstants.EXPORT_NURSERY_FIELDLOG_FIELDROID.getInt() == exportType){
    		filename = filename  + AppConstants.EXPORT_FIELDLOG_SUFFIX.getString();
    		outputFilename = fielddroidExportStudyService.export(userSelection.getWorkbook(), filename, instances);
    		response.setContentType("text/csv");
    	}else if(AppConstants.EXPORT_NURSERY_R.getInt() == exportType){
    		filename = filename  + AppConstants.EXPORT_R_SUFFIX.getString();
    		outputFilename = rExportStudyService.exportToR(userSelection.getWorkbook(), filename, selectedTraitTermId, instances);    		
    		response.setContentType("text/csv");
    	}else if(AppConstants.EXPORT_NURSERY_EXCEL.getInt() == exportType){
    		filename = filename  + AppConstants.EXPORT_XLS_SUFFIX.getString();
    		outputFilename = excelExportStudyService.export(userSelection.getWorkbook(), filename, instances);
    		if (instances != null && instances.size() > 1) {
        		int extensionIndex = filename.lastIndexOf(".");
        		filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
        		response.setContentType("application/zip");
        	} else {
        		filename = getOutputFileName(workbook.isNursery(), outputFilename, filename);
        		response.setContentType("application/vnd.ms-excel");
        	}
    	}else if(AppConstants.EXPORT_DATAKAPTURE.getInt() == exportType) {
    		outputFilename = dataKaptureExportStudyService.export(userSelection.getWorkbook(), filename, instances);
    		response.setContentType("application/zip");
    		filename = filename + AppConstants.ZIP_FILE_SUFFIX.getString();
    	}else if (AppConstants.EXPORT_KSU_EXCEL.getInt() == exportType) {
    		filename = filename + AppConstants.EXPORT_XLS_SUFFIX.getString();
    		outputFilename = ksuExcelExportStudyService.export(userSelection.getWorkbook(), filename, instances);
    		int extensionIndex = filename.lastIndexOf(".");
    		filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
    		response.setContentType("application/zip");
    	}else if (AppConstants.EXPORT_KSU_CSV.getInt() == exportType) {
    		filename = filename + AppConstants.EXPORT_CSV_SUFFIX.getString();
    		outputFilename = ksuCsvExportStudyService.export(userSelection.getWorkbook(), filename, instances);
    		int extensionIndex = filename.lastIndexOf(".");
    		filename = filename.substring(0, extensionIndex) + AppConstants.ZIP_FILE_SUFFIX.getString();
    		response.setContentType("application/zip");
    	}
    	Map<String, Object> results = new HashMap<String, Object>();
    	results.put("outputFilename", outputFilename);
    	results.put("filename", SettingsUtil.cleanSheetAndFileName(filename));
    	results.put("contentType", response.getContentType());
    	
    	SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), true, ontologyService);
    	
    	return super.convertObjectToJson(results);
    }
    
    protected String getOutputFileName(boolean isNursery, String outputFilename, String filename) {
    	if (!isNursery) {
			return outputFilename;
		}
		return filename;
	}

	private UserSelection getUserSelection() {
    	return this.studySelection;
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
    
    /**
     * Load initial germplasm tree.
     *
     * @return the string
     */
    @RequestMapping(value = "/trial/instances/{studyId}", method = RequestMethod.GET)
    public String saveList(
    		@PathVariable int studyId,
    		Model model, HttpSession session) {

    	List<ExportTrialInstanceBean> trialInstances = new ArrayList();

        List<Integer> trialIds = new ArrayList();
        trialIds.add(studyId);
        List<FieldMapInfo> fieldMapInfoList = new ArrayList();
        try{
        	fieldMapInfoList = fieldbookMiddlewareService.getFieldMapInfoOfTrial(trialIds);
        }catch(Exception e){
        	e.printStackTrace();
        }
        if(fieldMapInfoList != null && fieldMapInfoList.get(0).getDatasets() != null && fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances() != null){
        	for(int i = 0 ; i < fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances().size() ; i++){
        		FieldMapTrialInstanceInfo info = fieldMapInfoList.get(0).getDatasets().get(0).getTrialInstances().get(i);
        		trialInstances.add(new ExportTrialInstanceBean(info.getTrialInstanceNo(), info.getHasFieldMap()));
        	}
        }
        model.addAttribute("trialInstances", trialInstances);
        return super.showAjaxPage(model, EXPORT_TRIAL_INSTANCE);
    }
}
