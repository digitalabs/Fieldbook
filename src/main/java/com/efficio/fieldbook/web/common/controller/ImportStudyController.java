package com.efficio.fieldbook.web.common.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
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
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.common.service.DataKaptureImportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelImportStudyService;
import com.efficio.fieldbook.web.common.service.FieldroidImportStudyService;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Controller
@RequestMapping(ImportStudyController.URL)
public class ImportStudyController extends AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(ImportStudyController.class);
    public static final String URL = "/ImportManager";
    private String ADD_OR_REMOVE_TRAITS_HTML =  "/NurseryManager/addOrRemoveTraits";

    @Resource
    private UserSelection studySelection;
    
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
    private OntologyService ontologyService;
    
    /** The message source. */
    @Resource
    private ResourceBundleMessageSource messageSource;
    
    @Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
    
    @Override
	public String getContentName() {
		return null;
	}
    @ResponseBody
    @RequestMapping(value="/import/{studyType}/{importType}", method = RequestMethod.POST)
    public String importFile(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form
            ,@PathVariable String studyType
    		,@PathVariable int importType, BindingResult result, Model model) {

    	boolean isTrial = ("TRIAL").equalsIgnoreCase(studyType);
    	ImportResult importResult = null;
    	UserSelection userSelection = getUserSelection(isTrial);
    	/**
    	 * Should always revert the data first to the original data here
    	 * we should move here that part the copies it to the original observation
    	 */
    	
        WorkbookUtil.resetWorkbookObservations(userSelection.getWorkbook());
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
	    			
	    			importResult = fieldroidImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename), ontologyService, fieldbookMiddlewareService);
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
	    			importResult = excelImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename), ontologyService, fieldbookMiddlewareService);
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
	    			
	    			importResult = dataKaptureImportStudyService.importWorkbook(userSelection.getWorkbook(), fileService.getFilePath(filename), ontologyService, fieldbookMiddlewareService);
					
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
    	resultsMap.put("hasDataOverwrite", userSelection.getWorkbook().hasExistingDataOverwrite() ? "1" : "0");
    	if(!result.hasErrors()){
    		userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
	    	form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
	    	form.changePage(userSelection.getCurrentPage());
	    	userSelection.setCurrentPage(form.getCurrentPage());
	    	form.setImportVal(1);
	    	form.setNumberOfInstances(userSelection.getWorkbook().getTotalNumberOfInstances());
	    	form.setTrialEnvironmentValues(transformTrialObservations(userSelection.getWorkbook().getTrialObservations(), userSelection.getTrialLevelVariableList()));
	    	form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());
	    	
	    	if(importResult.getErrorMessage() != null && !("").equalsIgnoreCase(importResult.getErrorMessage())){
	    		resultsMap.put("isSuccess", 0);	    		
	    		resultsMap.put("error", importResult.getErrorMessage());
	    	}else{
	    		resultsMap.put("isSuccess", 1);
		    	resultsMap.put("modes", importResult.getModes());
		    	populateConfirmationMessages(importResult.getChangeDetails());
		    	resultsMap.put("changeDetails", importResult.getChangeDetails());
		    	resultsMap.put("errorMessage", importResult.getErrorMessage());
		    	List<String> detailErrorMessage = new ArrayList<String>();
		    	String reminderConfirmation = "";
		    	if(importResult.getModes() != null && !importResult.getModes().isEmpty()){
		    		for (ChangeType mode : importResult.getModes()) {
		    			String message = messageSource.getMessage(mode.getMessageCode(), null, locale);
		    			if (mode == ChangeType.ADDED_TRAITS) {
		    				message += StringUtils.join(
		    						WorkbookUtil.getAddedTraits(
		    								userSelection.getWorkbook().getVariates(), userSelection.getWorkbook().getObservations()), ", ");
		    			}
		    			detailErrorMessage.add(message);
		    		}
		    		reminderConfirmation = messageSource.getMessage("confirmation.import.text", null, locale);
		    	}
		    	resultsMap.put("message", reminderConfirmation);
		    	resultsMap.put("confirmMessage", messageSource.getMessage("confirmation.import.text.to.proceed", null, locale));
		    	resultsMap.put("detailErrorMessage", detailErrorMessage);
		    	resultsMap.put("conditionConstantsImportErrorMessage", importResult.getConditionsAndConstantsErrorMessage());
	    	}
	    	
	    	
    	}else{
    		resultsMap.put("isSuccess", 0);
    		String errorCode = result.getFieldError("file").getCode();
    		try{
    			resultsMap.put("error", messageSource.getMessage(errorCode, null, locale));
    		}catch(NoSuchMessageException e){    			
    			resultsMap.put("error", errorCode);
    			LOG.error(e.getMessage(), e);
    		}
    	}
	    	
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
    
    private UserSelection getUserSelection(boolean isTrial) {
    	return this.studySelection;
    }
    
    public String show(Model model, boolean isTrial) {
        setupModelInfo(model);
        model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName(isTrial));        
        return BASE_TEMPLATE_NAME;
    }
    
    private String getContentName(boolean isTrial) {
    	return isTrial ? "TrialManager/openTrial" : "NurseryManager/addOrRemoveTraits";
    }
    
    @RequestMapping(value="/revert/data", method=RequestMethod.GET)
    public String revertData(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) {
    	
    	doRevertData(form);
    	
        return super.showAjaxPage(model, ADD_OR_REMOVE_TRAITS_HTML);
    }
    
    private void doRevertData(CreateNurseryForm form){
    	UserSelection userSelection = (UserSelection) getUserSelection(false);
    	//we should remove here the newly added traits
    	List<MeasurementVariable> newVariableList = new ArrayList<MeasurementVariable>();    	    	
    	
    	newVariableList.addAll(userSelection.getWorkbook().isNursery() ? userSelection.getWorkbook().getMeasurementDatasetVariables() : userSelection.getWorkbook().getMeasurementDatasetVariablesView());
    	form.setMeasurementVariables(newVariableList);
    	List<MeasurementRow> list = new ArrayList<MeasurementRow>();
    	if (userSelection.getWorkbook().getOriginalObservations() != null) {
        	for (MeasurementRow row : userSelection.getWorkbook().getOriginalObservations()) {
        		list.add(row.copy());
        	}
    	}
    	userSelection.getWorkbook().setObservations(list);
    	userSelection.setMeasurementRowList(list);

    	WorkbookUtil.revertImportedConditionAndConstantsData(userSelection.getWorkbook());
    }
    
    @ResponseBody
    @RequestMapping(value="/apply/change/details", method=RequestMethod.POST)
    public String applyChangeDetails(@RequestParam(value="data") String userResponses) throws Exception {
    	UserSelection userSelection = (UserSelection) getUserSelection(false);
    	ObjectMapper objectMapper = new ObjectMapper();
    	GermplasmChangeDetail[] responseDetails = objectMapper.readValue(userResponses, GermplasmChangeDetail[].class);
    	List<MeasurementRow> observations = userSelection.getWorkbook().getObservations();
    	Map<String, Map<String, String>> changeMap = new HashMap<String, Map<String,String>>();
    	for (GermplasmChangeDetail responseDetail : responseDetails) {
    		if (responseDetail.getIndex() < observations.size()) {
    			MeasurementRow row = observations.get(responseDetail.getIndex());
				int userId = this.getCurrentIbdbUserId();
				MeasurementData desigData = row.getMeasurementData(TermId.DESIG.getId());
				MeasurementData gidData = row.getMeasurementData(TermId.GID.getId());
				MeasurementData entryNumData = row.getMeasurementData(TermId.ENTRY_NO.getId());
    			if (responseDetail.getStatus() == 1) { 
    				// add germplasm name to gid
    				String gDate = DateUtil.convertToDBDateFormat(TermId.DATE_VARIABLE.getId(), responseDetail.getImportDate());
					Integer dateInteger = Integer.valueOf(gDate); 
    				fieldbookMiddlewareService.addGermplasmName(responseDetail.getNewDesig(), 
    						Integer.valueOf(responseDetail.getOriginalGid()), userId, responseDetail.getNameType(), responseDetail.getImportLocationId(), dateInteger);
    				desigData.setValue(responseDetail.getNewDesig());
    				gidData.setValue(responseDetail.getOriginalGid());    				
    			} else if (responseDetail.getStatus() == 2) { 
    				//create new germlasm
					String gDate = DateUtil.convertToDBDateFormat(TermId.DATE_VARIABLE.getId(), responseDetail.getImportDate());
					Integer dateInteger = Integer.valueOf(gDate); 
    			    Name name = new Name(null, null, responseDetail.getNameType(), 1, userId, responseDetail.getNewDesig(), responseDetail.getImportLocationId(), dateInteger, 0);
    			    Germplasm germplasm = new Germplasm(null, responseDetail.getImportMethodId(), 0, 0, 0, userId, 
    			            0, responseDetail.getImportLocationId(), dateInteger, name);
    				int newGid = fieldbookMiddlewareService.addGermplasm(germplasm, name);
    				desigData.setValue(responseDetail.getNewDesig());
    				gidData.setValue(String.valueOf(newGid));
    			} else if (responseDetail.getStatus() == 3) { 
    				//choose gids
    				desigData.setValue(responseDetail.getNewDesig());
    				gidData.setValue(String.valueOf(responseDetail.getSelectedGid()));
    				
    			}
    			
    			if (responseDetail.getStatus() > 0 && entryNumData != null && entryNumData.getValue() != null) {	    			
    				Map<String, String> tempMap = new HashMap<String,String>();
    				tempMap.put(Integer.toString(TermId.GID.getId()), gidData.getValue());
    				tempMap.put(Integer.toString(TermId.DESIG.getId()), desigData.getValue());
					changeMap.put(entryNumData.getValue(), tempMap);					
    			}
    		}
    	}
    	
    	//we need to set the gid and desig for the trial with the same entry number
    	if(!userSelection.getWorkbook().isNursery()){
    		for(MeasurementRow row : observations){
    			MeasurementData entryNumData = row.getMeasurementData(TermId.ENTRY_NO.getId());
    			if(entryNumData != null && entryNumData.getValue() != null && changeMap.containsKey(entryNumData.getValue())){
    				Map<String, String> tempMap = changeMap.get(entryNumData.getValue());
    				MeasurementData desigData = row.getMeasurementData(TermId.DESIG.getId());
    				MeasurementData gidData = row.getMeasurementData(TermId.GID.getId());
    				desigData.setValue(tempMap.get(Integer.toString(TermId.DESIG.getId())));
    				gidData.setValue(tempMap.get(Integer.toString(TermId.GID.getId())));
    			}
    		}
    	}
    	
    	return "success";
    }
    //germplasm trial entry plot - 345
    
    private void populateConfirmationMessages(List<GermplasmChangeDetail> details) {
    	if (details != null && !details.isEmpty()) {
    		for (int index = 0 ; index < details.size() ; index++) {
    			String[] args = new String[] {String.valueOf(index+1), String.valueOf(details.size()), 
    					details.get(index).getOriginalDesig(), details.get(index).getTrialInstanceNumber(), 
    					details.get(index).getEntryNumber(), details.get(index).getPlotNumber()};
    			String message = messageSource.getMessage("import.change.desig.confirmation", args, LocaleContextHolder.getLocale());
    			details.get(index).setMessage(message);
    		}
    	}
    }

    @RequestMapping(value="/import/save", method=RequestMethod.POST)
    public String saveImportedFiles(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws Exception {
    	UserSelection userSelection = (UserSelection) getUserSelection(false);
    	List<MeasurementVariable> traits = WorkbookUtil.getAddedTraitVariables(
								    			userSelection.getWorkbook().getVariates(), 
								    			userSelection.getWorkbook().getObservations());
    	Workbook workbook = userSelection.getWorkbook();
    	userSelection.getWorkbook().getVariates().addAll(traits);
    	
        fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<SettingDetail>(), AppConstants.ID_NAME_COMBINATION.getString(), true);
        
        //will do the cleanup for BM_CODE_VTE here
        SettingsUtil.resetBreedingMethodValueToCode(fieldbookMiddlewareService, workbook.getObservations(), false, ontologyService);
    	fieldbookMiddlewareService.saveMeasurementRows(userSelection.getWorkbook());
    	SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), false, ontologyService);
    	userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
    	    
    	userSelection.getWorkbook().setOriginalObservations(userSelection.getWorkbook().getObservations());
    	List<SettingDetail> newTraits = new ArrayList<SettingDetail>();
    	List<SettingDetail> selectedVariates = new ArrayList<SettingDetail>();
    	SettingsUtil.convertWorkbookVariatesToSettingDetails(traits, 
    			fieldbookMiddlewareService, fieldbookService, newTraits, selectedVariates);
    	
    	if(workbook.isNursery()){
    		userSelection.getSelectionVariates().addAll(selectedVariates);
    		userSelection.setNewSelectionVariates(selectedVariates);
    		form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
    	}else{
    		form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
    	}
    	userSelection.getBaselineTraitsList().addAll(newTraits);
    	userSelection.setNewTraits(newTraits);
    	
    	for (SettingDetail detail : newTraits) {
    		detail.getVariable().setOperation(Operation.UPDATE);
    	}
    	for (SettingDetail detail : selectedVariates) {
    		detail.getVariable().setOperation(Operation.UPDATE);
    	}    	    	
    	form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(userSelection.getWorkbook().getMeasurementDatesetId(), SettingsUtil.buildVariates(userSelection.getWorkbook().getVariates())));
    	
    	fieldbookService.saveStudyColumnOrdering(userSelection.getWorkbook().getStudyDetails().getId(), userSelection.getWorkbook().getStudyDetails().getStudyName(), form.getColumnOrders(), userSelection.getWorkbook());
    	
        return super.showAjaxPage(model,ADD_OR_REMOVE_TRAITS_HTML);
    }
    
    
    
    @RequestMapping(value="/import/preview", method=RequestMethod.POST)
    public String previewImportedFiles(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws Exception {
    	UserSelection userSelection = (UserSelection) getUserSelection(false);
    	List<MeasurementVariable> traits = WorkbookUtil.getAddedTraitVariables(
								    			userSelection.getWorkbook().getVariates(), 
								    			userSelection.getWorkbook().getObservations());
    	
    	userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
    	List<MeasurementVariable> newVariableList = new ArrayList<MeasurementVariable>();
    	
    	form.setMeasurementVariables(newVariableList);
    	
    	newVariableList.addAll(userSelection.getWorkbook().isNursery() ? userSelection.getWorkbook().getMeasurementDatasetVariables() :  userSelection.getWorkbook().getMeasurementDatasetVariablesView());
    	newVariableList.addAll(traits); 	
        return super.showAjaxPage(model, ADD_OR_REMOVE_TRAITS_HTML);
    }
    
    @ResponseBody
    @RequestMapping(value="/retrieve/new/import/variables", method=RequestMethod.GET)
    public Map<String, String> getNewlyImportedTraits() throws Exception {
    	UserSelection userSelection = (UserSelection) getUserSelection(false);
    	ObjectMapper objectMapper = new ObjectMapper();
    	Map<String, String> map = new HashMap<String, String>();
    	List<SettingDetail> newTraits = userSelection.getNewTraits();
    	List<SettingDetail> selectedVariates = userSelection.getNewSelectionVariates();
    	map.put("newTraits", objectMapper.writeValueAsString(newTraits));
    	map.put("newSelectionVariates", objectMapper.writeValueAsString(selectedVariates));
    	return map;
    }
    
    
}
