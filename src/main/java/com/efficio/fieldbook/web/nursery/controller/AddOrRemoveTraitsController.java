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

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;

/**
 * The Class AddOrRemoveTraitsController.
 */
@Controller
@RequestMapping(AddOrRemoveTraitsController.URL)
public class AddOrRemoveTraitsController extends AbstractBaseFieldbookController{

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/addOrRemoveTraits";
    
    public static final String OBSERVATIONS_HTML = "NurseryManager/observations";
//    public static final String PAGINATION_TEMPLATE = "/NurseryManager/showAddOrRemoveTraitsPagination";
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AddOrRemoveTraitsController.class);
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    @Resource
    private MeasurementsGeneratorService measurementsGeneratorService;

    @Resource
    private ValidationService validationService;
    
    @Resource
    private PaginationListSelection paginationListSelection;
    
    /** The Constant BUFFER_SIZE. */
//    private static final int BUFFER_SIZE = 4096 * 4;
    
  

	/* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/addOrRemoveTraits";
    }
    
    /**
     * Shows the screen
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("createNurseryForm") CreateNurseryForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{
    	
    	//getUserSelection().getWorkbook().getMeasurementDatasetVariables();
    	getUserSelection().setMeasurementRowList(measurementsGeneratorService.generateRealMeasurementRows(getUserSelection()));
    	form.setMeasurementRowList(getUserSelection().getMeasurementRowList());
    	form.setMeasurementVariables(getUserSelection().getWorkbook().getMeasurementDatasetVariables());
    	form.changePage(1);
    	userSelection.setCurrentPage(form.getCurrentPage());
    	
    	return super.show(model);
    }
    
    @RequestMapping(value="/viewNursery/{nurseryId}", method = RequestMethod.GET)
    public String viewNursery(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, 
            @PathVariable int nurseryId) {
        Workbook workbook = null;
        
        try { 
            workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        if (workbook != null) {
        	getUserSelection().setMeasurementRowList(workbook.getObservations());
            form.setMeasurementRowList(getUserSelection().getMeasurementRowList());
            form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
            form.setStudyName(workbook.getStudyDetails().getStudyName());
            form.changePage(1);
            userSelection.setCurrentPage(form.getCurrentPage());
            userSelection.setWorkbook(workbook);
        }
        
        return super.show(model);
    }
    
    /**
     * Show details.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String showDetails(@ModelAttribute("createNurseryForm") CreateNurseryForm form,          
            BindingResult result, Model model) {
        // If operation = add new nursery
        Workbook workbook = userSelection.getWorkbook();
        if (workbook == null) {
            workbook = new Workbook();
        }
        int previewPageNum = userSelection.getCurrentPage();
        copyDataFromFormToUserSelection(form, previewPageNum);
        form.setMeasurementRowList(getUserSelection().getMeasurementRowList());
    	form.setMeasurementVariables(getUserSelection().getWorkbook().getMeasurementDatasetVariables());
      
        
        workbook.setObservations(form.getMeasurementRowList());
        userSelection.setWorkbook(workbook);
        
        return "redirect:" + SaveNurseryController.URL;
    }
    

//    @ResponseBody
//    @RequestMapping(value="/updateTraits", method = RequestMethod.POST)
//    public  Map<String, String> updateTraits(@ModelAttribute("createNurseryForm") CreateNurseryForm form,          
//            BindingResult result, Model model){
//        Map<String, String> resultMap = new HashMap<String, String>();
//        
//        Workbook workbook = userSelection.getWorkbook();
//        /*
//        int ctr = 0;
//        for (MeasurementRow observation : workbook.getObservations()) {
//            form.getMeasurementRowList().get(ctr).setExperimentId(observation.getExperimentId());
//            ctr++;
//        }
//		*/
//        int previewPageNum = userSelection.getCurrentPage();
//        
//        copyDataFromFormToUserSelection(form, previewPageNum);
//        
//        form.setMeasurementRowList(getUserSelection().getMeasurementRowList());
//    	form.setMeasurementVariables(getUserSelection().getWorkbook().getMeasurementDatasetVariables());
//    	form.setStudyName(workbook.getStudyDetails().getStudyName());
//    	
//        workbook.setObservations(form.getMeasurementRowList());
//
//        try { 
//        	validationService.validateObservationValues(workbook);
//            fieldbookMiddlewareService.saveMeasurementRows(workbook);
//            resultMap.put("status", "1");
//        } catch (MiddlewareQueryException e) {
//            LOG.error(e.getMessage(), e);
//            resultMap.put("status", "-1");
//            resultMap.put("errorMessage", e.getMessage());
//        }
//        
//        return resultMap;
//    }
//    /**
//     * Get for the pagination of the list
//     *
//     * @param form the form
//     * @param model the model
//     * @return the string
//     */
//    @RequestMapping(value="/page/{pageNum}/{previewPageNum}", method = RequestMethod.POST)
//    public String getPaginatedList(@PathVariable int pageNum, @PathVariable int previewPageNum
//            , @ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) {
//        //this set the necessary info from the session variable
//    	copyDataFromFormToUserSelection(form, previewPageNum);
//    	//we need to set the data in the measurementList
//    	
//    	
//    	
//    	form.setMeasurementRowList(getUserSelection().getMeasurementRowList());
//    	form.setMeasurementVariables(getUserSelection().getWorkbook().getMeasurementDatasetVariables());
//    	form.setStudyName(getUserSelection().getWorkbook().getStudyDetails().getStudyName());
//        form.changePage(pageNum);
//        userSelection.setCurrentPage(form.getCurrentPage());
//        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
//    }
    
    private void copyDataFromFormToUserSelection(CreateNurseryForm form, int previewPageNum){
    	for(int i = 0 ; i < form.getPaginatedMeasurementRowList().size() ; i++){
    		MeasurementRow measurementRow = form.getPaginatedMeasurementRowList().get(i);
    		int realIndex = ((previewPageNum - 1) * form.getResultPerPage()) + i;
    		for(int index = 0 ; index < measurementRow.getDataList().size() ; index++){
    			MeasurementData measurementData =  measurementRow.getDataList().get(index);
    			MeasurementData sessionMeasurementData = getUserSelection().getMeasurementRowList().get(realIndex).getDataList().get(index);
    			if(sessionMeasurementData.isEditable())
    				sessionMeasurementData.setValue(measurementData.getValue());    			
    		}
    		//getUserSelection().getMeasurementRowList().set(realIndex, measurementRow);
    	}
    }
//    @ResponseBody
//    @RequestMapping(value="/export/{exportType}/{selectedTraitTermId}", method = RequestMethod.GET)
//    public String exportRFile(@PathVariable int exportType,@PathVariable int selectedTraitTermId, HttpServletResponse response) {
//    	return doExport(exportType, selectedTraitTermId, response);
//    	
//    }
//    
//    @ResponseBody
//    @RequestMapping(value="/export/{exportType}", method = RequestMethod.GET)
//    public String exportFile(@PathVariable int exportType, HttpServletResponse response) {
//        return doExport(exportType, 0, response);
//    	
//    }
//   
//    
//    /**
//     * Do export.
//     *
//     * @param exportType the export type
//     * @param selectedTraitTermId the selected trait term id
//     * @param response the response
//     * @return the string
//     */
//    private String doExport(int exportType, int selectedTraitTermId, HttpServletResponse response){
//    	String filename = getUserSelection().getWorkbook().getStudyDetails().getStudyName();
//    	if(AppConstants.EXPORT_NURSERY_FIELDLOG_FIELDROID.getInt() == exportType){
//    		filename = filename  + AppConstants.EXPORT_FIELDLOG_SUFFIX.getString();
//    		fielddroidExportStudyService.export(getUserSelection().getWorkbook(), filename);
//    		response.setContentType("text/csv");
//    	}else if(AppConstants.EXPORT_NURSERY_R.getInt() == exportType){
//    		filename = filename  + AppConstants.EXPORT_R_SUFFIX.getString();
//    		rExportStudyService.exportToR(getUserSelection().getWorkbook(), filename, selectedTraitTermId);    		
//    		response.setContentType("text/csv");
//    	}else if(AppConstants.EXPORT_NURSERY_EXCEL.getInt() == exportType){
//    		filename = filename  + AppConstants.EXPORT_XLS_SUFFIX.getString();
//    		excelExportStudyService.export(getUserSelection().getWorkbook(), filename);
//    		response.setContentType("application/vnd.ms-excel");
//    	}
//    	        
//        File xls = new File(filename); // the selected name + current date
//        FileInputStream in;
//        
//        response.setHeader("Content-disposition","attachment; filename=" + filename);
//        try {
//            in = new FileInputStream(xls);
//            OutputStream out = response.getOutputStream();
//
//            byte[] buffer= new byte[BUFFER_SIZE]; // use bigger if you want
//            int length = 0;
//
//            while ((length = in.read(buffer)) > 0){
//                 out.write(buffer, 0, length);
//            }
//            in.close();
//            out.close();
//        } catch (FileNotFoundException e) {
//        	LOG.error(e.getMessage(), e);
//        } catch (IOException e) {
//        	LOG.error(e.getMessage(), e);
//        }
//       
//        return "";
//    }
    
//    @RequestMapping(value="/import/{importType}", method = RequestMethod.POST)
//    public String importFile(@ModelAttribute("createNurseryForm") CreateNurseryForm form
//            ,@PathVariable int importType, BindingResult result, Model model) {
//    	    	    	    	
//    	if(AppConstants.EXPORT_NURSERY_FIELDLOG_FIELDROID.getInt() == importType){
//    		MultipartFile file = form.getFile();
//            if (file == null) {
//           	 result.rejectValue("file", AppConstants.FILE_NOT_FOUND_ERROR.getString());
//            } else {
//                boolean isCSVFile = file.getOriginalFilename().contains(".csv");
//                if (!isCSVFile) {
//               	 	result.rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
//                }
//            }
//            if(!result.hasErrors()){
//	    		try {
//	    			String filename = fileService.saveTemporaryFile(file.getInputStream());
//	    			
//					fieldroidImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename));
//				} catch (WorkbookParserException e) {
//					LOG.error(e.getMessage(), e);
//					result.rejectValue("file", e.getMessage());
//				} catch (IOException e) {
//					LOG.error(e.getMessage(), e);
//				}
//            }
//    	}else if(AppConstants.EXPORT_NURSERY_EXCEL.getInt() == importType){
//    		
//    		
//        	
//        	 MultipartFile file = form.getFile();
//             if (file == null) {
//            	 result.rejectValue("file", AppConstants.FILE_NOT_FOUND_ERROR.getString());
//             } else {
//                 boolean isExcelFile = file.getOriginalFilename().contains(".xls") 
//                         || file.getOriginalFilename().contains(".xlsx");
//                 if (!isExcelFile) {
//                	 result.rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
//                 }
//             }
//             if(!result.hasErrors()){
//	    		try {
//	    			String filename = fileService.saveTemporaryFile(file.getInputStream());
//	    			
//					excelImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename));
//				} catch (WorkbookParserException e) {
//					LOG.error(e.getMessage(), e);
//					result.rejectValue("file", e.getMessage());
//				} catch (IOException e) {
//					LOG.error(e.getMessage(), e);
//				}
//             }
//    	}
//    	
//    	
//	    	form.setMeasurementRowList(getUserSelection().getMeasurementRowList());
//	    	form.setMeasurementVariables(getUserSelection().getWorkbook().getMeasurementDatasetVariables());
//	    	form.changePage(userSelection.getCurrentPage());
//	    	userSelection.setCurrentPage(form.getCurrentPage());
//	    	form.setImportVal(1);
//    	    	    	
//    	return super.show(model);
//    }
//    
    /**
     * Gets the user selection.
     *
     * @return the user selection
     */
    public UserSelection getUserSelection() {
        return this.userSelection;
    }

    @RequestMapping(value="/viewNurseryAjax/{datasetId}", method = RequestMethod.GET)
    public String viewNurseryAjax(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, 
            @PathVariable int datasetId) {
        Workbook workbook = null;
        
        try { 
            workbook = fieldbookMiddlewareService.getNurseryDataSet(datasetId);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        if (workbook != null) {
        	getUserSelection().setMeasurementRowList(workbook.getObservations());
            form.setMeasurementRowList(getUserSelection().getMeasurementRowList());
            form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
            form.setStudyName(workbook.getStudyDetails().getStudyName());
            paginationListSelection.addReviewDetailsList(String.valueOf(workbook.getMeasurementDatesetId()), form.getMeasurementRowList());
            paginationListSelection.addReviewVariableList(String.valueOf(workbook.getMeasurementDatesetId()), form.getMeasurementVariables());
            form.changePage(1);
            userSelection.setCurrentPage(form.getCurrentPage());
            userSelection.setWorkbook(workbook);
        }
        
        return super.showAjaxPage(model, OBSERVATIONS_HTML);
    }
}