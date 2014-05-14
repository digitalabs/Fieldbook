package com.efficio.fieldbook.web.common.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.StudySelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.common.service.DataKaptureImportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelImportStudyService;
import com.efficio.fieldbook.web.common.service.FieldroidImportStudyService;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.TrialSelection;
import com.efficio.fieldbook.web.util.AppConstants;

@Controller
@RequestMapping(ImportStudyController.URL)
public class ImportStudyController extends AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(ImportStudyController.class);
    public static final String URL = "/ImportManager";

    @Resource
    private UserSelection nurserySelection;
    
    @Resource
    private TrialSelection trialSelection;
    
    @Resource
    private FileService fileService;
    
    @Resource
    private FieldroidImportStudyService fieldroidImportStudyService;
    
    @Resource
    private ExcelImportStudyService excelImportStudyService;
    
    @Resource
    private DataKaptureImportStudyService dataKaptureImportStudyService;

    @Override
	public String getContentName() {
		return null;
	}

    @RequestMapping(value="/import/{studyType}/{importType}", method = RequestMethod.POST)
    public String importFile(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form
            ,@PathVariable String studyType
    		,@PathVariable int importType, BindingResult result, Model model) {

    	boolean isTrial = studyType.equalsIgnoreCase("TRIAL");
    	StudySelection userSelection = getUserSelection(isTrial);
    	if(AppConstants.EXPORT_NURSERY_FIELDLOG_FIELDROID.getInt() == importType){
    		MultipartFile file = form.getFile();
            if (file == null) {
           	 result.rejectValue("file", AppConstants.FILE_NOT_FOUND_ERROR.getString());
            } else {
                boolean isCSVFile = file.getOriginalFilename().contains(".csv");
                if (!isCSVFile) {
               	 	result.rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
                }
            }
            if(!result.hasErrors()){
	    		try {
	    			String filename = fileService.saveTemporaryFile(file.getInputStream());
	    			
					fieldroidImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename));
				} catch (WorkbookParserException e) {
					LOG.error(e.getMessage(), e);
					result.rejectValue("file", e.getMessage());
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
            }
    	}else if(AppConstants.EXPORT_NURSERY_EXCEL.getInt() == importType){
    		
    		
        	
        	 MultipartFile file = form.getFile();
             if (file == null) {
            	 result.rejectValue("file", AppConstants.FILE_NOT_FOUND_ERROR.getString());
             } else {
                 boolean isExcelFile = file.getOriginalFilename().contains(".xls") 
                         || file.getOriginalFilename().contains(".xlsx");
                 if (!isExcelFile) {
                	 result.rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
                 }
             }
             if(!result.hasErrors()){
	    		try {
	    			String filename = fileService.saveTemporaryFile(file.getInputStream());
	    			
					excelImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename));
				} catch (WorkbookParserException e) {
					LOG.error(e.getMessage(), e);
					result.rejectValue("file", e.getMessage());
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
             }

    	}else if(AppConstants.IMPORT_DATAKAPTURE.getInt() == importType){
    		
       	 	MultipartFile file = form.getFile();
            if (file == null) {
           	 result.rejectValue("file", AppConstants.FILE_NOT_FOUND_ERROR.getString());
            } else {
                boolean isCSVFile = file.getOriginalFilename().contains(".csv");
                if (!isCSVFile) {
               	 result.rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
                }
            }
            if(!result.hasErrors()){
	    		try {
	    			String filename = fileService.saveTemporaryFile(file.getInputStream());
	    			
					dataKaptureImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename));
					
				} catch (WorkbookParserException e) {
					LOG.error(e.getMessage(), e);
					result.rejectValue("file", e.getMessage());
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
            }
    	}
    	
    	
	    	form.setMeasurementRowList(userSelection.getMeasurementRowList());
	    	form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
	    	form.changePage(userSelection.getCurrentPage());
	    	userSelection.setCurrentPage(form.getCurrentPage());
	    	form.setImportVal(1);
	    	form.setNumberOfInstances(userSelection.getWorkbook().getTotalNumberOfInstances());
	    	form.setTrialEnvironmentValues(transformTrialObservations(userSelection.getWorkbook().getTrialObservations(), nurserySelection.getTrialLevelVariableList()));
	    	form.setTrialLevelVariables(nurserySelection.getTrialLevelVariableList());
	    	
	    	
    	return show(model, isTrial);
    }
    
    private List<List<ValueReference>> transformTrialObservations(List<MeasurementRow> trialObservations, List<SettingDetail> trialHeaders) {
    	List<List<ValueReference>> list = new ArrayList<List<ValueReference>>();
    	if (trialHeaders != null && !trialHeaders.isEmpty()) {
	    	if (trialObservations != null && !trialObservations.isEmpty()) {
	    		for (MeasurementRow row : trialObservations) {
	        		List<ValueReference> refList = new ArrayList<ValueReference>();
	        		for (SettingDetail header : trialHeaders) {
	        			for (MeasurementData data : row.getDataList()) {
	        				if (data.getMeasurementVariable() != null
	        					&& data.getMeasurementVariable().getTermId() == header.getVariable().getCvTermId()) {
	        					
	        					refList.add(new ValueReference(data.getMeasurementVariable().getTermId(), data.getValue()));
	        				}
	        			}
	        		}
	        		list.add(refList);
	    		}
	    	}
    	}
    	return list;
    }
    
    private StudySelection getUserSelection(boolean isTrial) {
    	return isTrial ? this.trialSelection : this.nurserySelection;
    }
    
    public String show(Model model, boolean isTrial) {
        setupModelInfo(model);
        model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName(isTrial));        
        return BASE_TEMPLATE_NAME;
    }
    
    private String getContentName(boolean isTrial) {
    	return isTrial ? "TrialManager/openTrial" : "NurseryManager/addOrRemoveTraits";
    }
}
