package com.efficio.fieldbook.web.common.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.StudySelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.common.service.DataKaptureImportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelImportStudyService;
import com.efficio.fieldbook.web.common.service.FieldroidImportStudyService;
import com.efficio.fieldbook.web.common.service.ImportStudyService;
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
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;

    @Resource
    private WorkbenchService workbenchService;
    
    /** The message source. */
    @Resource
    private ResourceBundleMessageSource messageSource;
    
    @Override
	public String getContentName() {
		return null;
	}
    @ResponseBody
    @RequestMapping(value="/import/{studyType}/{importType}", method = RequestMethod.POST)
    public String importFile(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form
            ,@PathVariable String studyType
    		,@PathVariable int importType, BindingResult result, Model model) {

    	boolean isTrial = studyType.equalsIgnoreCase("TRIAL");
    	ImportResult importResult = null;
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
	    			
	    			importResult = fieldroidImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename));
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
	    			importResult = excelImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename));
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
	    			
	    			importResult = dataKaptureImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename));
					
				} catch (WorkbookParserException e) {
					LOG.error(e.getMessage(), e);
					result.rejectValue("file", e.getMessage());
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
            }
    	}

    	Locale locale = LocaleContextHolder.getLocale();
    	Map<String, Object> resultsMap = new HashMap<String,Object>();
    	if(!result.hasErrors()){
    		userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
	    	//form.setMeasurementRowList(userSelection.getMeasurementRowList());
	    	form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
	    	form.changePage(userSelection.getCurrentPage());
	    	userSelection.setCurrentPage(form.getCurrentPage());
	    	form.setImportVal(1);
	    	form.setNumberOfInstances(userSelection.getWorkbook().getTotalNumberOfInstances());
	    	form.setTrialEnvironmentValues(transformTrialObservations(userSelection.getWorkbook().getTrialObservations(), nurserySelection.getTrialLevelVariableList()));
	    	form.setTrialLevelVariables(nurserySelection.getTrialLevelVariableList());
	    	
	    	if(importResult.getErrorMessage() != null && !importResult.getErrorMessage().equalsIgnoreCase("")){
	    		resultsMap.put("isSuccess", 0);	    		
	    		resultsMap.put("error", importResult.getErrorMessage());
	    	}else{
	    		resultsMap.put("isSuccess", 1);
		    	resultsMap.put("mode", importResult.getMode());
		    	populateConfirmationMessages(importResult.getChangeDetails());
		    	resultsMap.put("changeDetails", importResult.getChangeDetails());
		    	resultsMap.put("errorMessage", importResult.getErrorMessage());
		    	String reminderConfirmation = "";
		    	if(importResult.getMode() != ImportStudyService.EDIT_ONLY){
		    		reminderConfirmation = messageSource.getMessage("confirmation.import." + importResult.getMode(), null, locale);
		    	}
		    	resultsMap.put("message", reminderConfirmation);
	    	}
	    	
	    	
    	}else{
    		resultsMap.put("isSuccess", 0);
    		String errorCode = result.getFieldError("file").getCode();
    		try{
    			resultsMap.put("error", messageSource.getMessage(errorCode, null, locale));
    		}catch(NoSuchMessageException e){    			
    			resultsMap.put("error",messageSource.getMessage("nursery.import.incorrect.input.file", null, locale));
    		}
    	}
	    	
    	//return show(model, isTrial);
    	//return resultsMap;
    	return super.convertObjectToJson(resultsMap);
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
    
    @ResponseBody
    @RequestMapping(value="/revert/data", method=RequestMethod.GET)
    public String revertData(Model model) {
    	UserSelection userSelection = (UserSelection) getUserSelection(false);
    	
    	List<MeasurementRow> list = new ArrayList<MeasurementRow>();
    	for (MeasurementRow row : userSelection.getWorkbook().getOriginalObservations()) {
    		list.add(row.copy());
    	}
    	userSelection.getWorkbook().setObservations(list);
    	userSelection.setMeasurementRowList(list);
    	
    	return "success";
    }
    
    @ResponseBody
    @RequestMapping(value="/apply/change/details", method=RequestMethod.POST)
    public String applyChangeDetails(@RequestParam(value="data") String userResponses) throws Exception {
    	UserSelection userSelection = (UserSelection) getUserSelection(false);
    	ObjectMapper objectMapper = new ObjectMapper();
    	GermplasmChangeDetail[] responseDetails = objectMapper.readValue(userResponses, GermplasmChangeDetail[].class);
    	List<MeasurementRow> observations = userSelection.getWorkbook().getObservations();
    	for (GermplasmChangeDetail responseDetail : responseDetails) {
    		if (responseDetail.getIndex() < observations.size()) {
    			MeasurementRow row = observations.get(responseDetail.getIndex());
				int userId = workbenchService.getCurrentIbdbUserId(getCurrentProjectId());
				MeasurementData desigData = row.getMeasurementData(TermId.DESIG.getId());
				MeasurementData gidData = row.getMeasurementData(TermId.GID.getId());
    			if (responseDetail.getStatus() == 1) { // add germplasm name to gid
    				fieldbookMiddlewareService.addGermplasmName(responseDetail.getNewDesig(), Integer.valueOf(responseDetail.getOriginalGid()), userId);
    				desigData.setValue(responseDetail.getNewDesig());
    				gidData.setValue(responseDetail.getOriginalGid());
    			}
    			else if (responseDetail.getStatus() == 2) { //create new germlasm 
    				int newGid = fieldbookMiddlewareService.addGermplasm(responseDetail.getNewDesig(), userId);
    				desigData.setValue(responseDetail.getNewDesig());
    				gidData.setValue(String.valueOf(newGid));
    			}
    			else if (responseDetail.getStatus() == 3) { //choose gids
    				desigData.setValue(responseDetail.getNewDesig());
    				gidData.setValue(String.valueOf(responseDetail.getSelectedGid()));
    				
    			}
    		}
    	}
    	
    	return "success";
    }
    
    private void populateConfirmationMessages(List<GermplasmChangeDetail> details) {
    	if (details != null && !details.isEmpty()) {
    		for (int index = 0 ; index < details.size() ; index++) {
    			String[] args = new String[] {String.valueOf(index+1), String.valueOf(details.size()), 
    					details.get(index).getOriginalDesig(), details.get(index).getNewDesig()};
    			String message = messageSource.getMessage("import.change.desig.confirmation", args, LocaleContextHolder.getLocale());
    			details.get(index).setMessage(message);
    		}
    	}
    }
}
