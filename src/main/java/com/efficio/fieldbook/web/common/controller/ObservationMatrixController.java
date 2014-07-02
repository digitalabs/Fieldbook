package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.StudySelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.trial.bean.TrialSelection;
import com.efficio.fieldbook.web.util.DateUtil;

@Controller
@RequestMapping(ObservationMatrixController.URL)
public class ObservationMatrixController extends
		AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(ObservationMatrixController.class);
    public static final String URL = "/Common/addOrRemoveTraits";
    public static final String PAGINATION_TEMPLATE = "/Common/showAddOrRemoveTraitsPagination";
    public static final String PAGINATION_TEMPLATE_VIEW_ONLY = "/NurseryManager/showAddOrRemoveTraitsPagination";
    public static final String EDIT_EXPERIMENT_TEMPLATE = "/Common/updateExperimentModal";
    
	@Resource
	private UserSelection nurserySelection;
	
	@Resource
	private TrialSelection trialSelection;
	
	@Resource
	private ValidationService validationService;
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Resource
	private PaginationListSelection paginationListSelection;
	
	
	@Override
	public String getContentName() {
		return null;
	}
	
	/**
     * Get for the pagination of the list
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(value="/reload/{studyType}/{pageNum}/{previewPageNum}", method = RequestMethod.GET)
    public String getPaginatedListAfterImport(@PathVariable String studyType, @PathVariable int pageNum, @PathVariable int previewPageNum
            , @ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) {

    	boolean isTrial = studyType.equalsIgnoreCase("TRIAL");
    	StudySelection userSelection = getUserSelection(isTrial);    	
    	
    	form.setMeasurementRowList(userSelection.getWorkbook().getObservations());
    	form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
    	form.setStudyName(userSelection.getWorkbook().getStudyDetails().getStudyName());
        form.changePage(pageNum);
        userSelection.setCurrentPage(form.getCurrentPage());
        return super.showAjaxPage(model, PAGINATION_TEMPLATE);
    }


    @RequestMapping(value="/pageView/{studyType}/{pageNum}", method = RequestMethod.GET)
    public String getPaginatedListViewOnly(@PathVariable String studyType, @PathVariable int pageNum,
            @ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, @RequestParam("listIdentifier") String datasetId) {

    	boolean isTrial = studyType.equalsIgnoreCase("TRIAL");
    	StudySelection userSelection = getUserSelection(isTrial);
    	
    	List<MeasurementRow> rows = paginationListSelection.getReviewDetailsList(datasetId);
    	if (rows != null) {
    		form.setMeasurementRowList(rows);
    		form.changePage(pageNum);
    	}
    	List<MeasurementVariable> variables = paginationListSelection.getReviewVariableList(datasetId);
    	if (variables != null) {
    		form.setMeasurementVariables(variables);
    	}
        form.changePage(pageNum);
        userSelection.setCurrentPage(form.getCurrentPage());
        return super.showAjaxPage(model, PAGINATION_TEMPLATE_VIEW_ONLY);
    }

    @ResponseBody
    @RequestMapping(value="/{studyType}/updateTraits", method = RequestMethod.POST)
    public  Map<String, String> updateTraits(@ModelAttribute("createNurseryForm") CreateNurseryForm form,          
            @PathVariable String studyType, BindingResult result, Model model){

    	boolean isTrial = studyType.equalsIgnoreCase("TRIAL");
    	StudySelection userSelection = getUserSelection(isTrial);
    	
    	Map<String, String> resultMap = new HashMap<String, String>();
        
        Workbook workbook = userSelection.getWorkbook();
        
        int previewPageNum = userSelection.getCurrentPage();       
        
        form.setMeasurementRowList(userSelection.getMeasurementRowList());
    	form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
    	form.setStudyName(workbook.getStudyDetails().getStudyName());
    	
        workbook.setObservations(form.getMeasurementRowList());
        workbook.updateTrialObservationsWithReferenceList(form.getTrialEnvironmentValues());

        try { 
        	validationService.validateObservationValues(workbook);
            fieldbookMiddlewareService.saveMeasurementRows(workbook);
            resultMap.put("status", "1");
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            resultMap.put("status", "-1");
            resultMap.put("errorMessage", e.getMessage());
        }
        
        return resultMap;
    }

    private StudySelection getUserSelection(boolean isTrial) {
    	return isTrial ? this.trialSelection : this.nurserySelection;
    }
    
   
    @RequestMapping(value="/update/experiment/{index}", method = RequestMethod.GET)
    public String editExperimentModal(@PathVariable int index,
            @ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, 
            Model model) throws MiddlewareQueryException {

    	
    	StudySelection userSelection = getUserSelection(false);
    	List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
    	tempList.addAll(userSelection.getMeasurementRowList());

    	MeasurementRow row = tempList.get(index);    
    	MeasurementRow copyRow = row.copy();
    	copyMeasurementValue(copyRow, row);
    	if(copyRow != null && copyRow.getMeasurementVariables() != null){
    		for(MeasurementData var : copyRow.getDataList()){
    			if(var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null && var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()){
    				//we change the date to the UI format
    				var.setValue(DateUtil.convertToUIDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
    			}
    		}
    	}
    	form.setUpdateObservation(copyRow);
    	form.setExperimentIndex(index);
    	model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
    	model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
        return super.showAjaxPage(model, EDIT_EXPERIMENT_TEMPLATE);
    }
    @ResponseBody
    @RequestMapping(value="/data/table/ajax", method = RequestMethod.GET)
    public Map<String, Object> demoPageDataTablesAjax(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) {
    	
    	StudySelection userSelection = getUserSelection(false);
    	List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
    	//for(int i = 0 ; i < 20; i++)
    		tempList.addAll(userSelection.getMeasurementRowList());
    	form.setMeasurementRowList(tempList);
    	
    	List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();
    	
    	for(MeasurementRow row : tempList){
    		    		
    		Map<String, Object> dataMap = generateDatatableDataMap(row, null);
    		
    		masterList.add(dataMap);
    	}
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("data", masterList);
    	//map.put("columns", masterColumnList);    	
    	return map;
    }
    @ResponseBody
    @RequestMapping(value="/data/table/ajax/submit/{index}", method = RequestMethod.POST)
    public Map<String, Object> dataTablesAjaxSubmit(@PathVariable int index, @ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form) {
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	StudySelection userSelection = getUserSelection(false);
    	List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
    	tempList.addAll(userSelection.getMeasurementRowList());

    	MeasurementRow row = form.getUpdateObservation();
    	MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);
    	MeasurementRow copyRow = originalRow.copy();
    	copyMeasurementValue(copyRow, row);
    	
		try {
			validationService.validateObservationValues(userSelection.getWorkbook(), copyRow);
			//if there are no error, meaning everything is good, thats the time we copy it to the original
			copyMeasurementValue(originalRow, row);
			if(originalRow != null && originalRow.getMeasurementVariables() != null){
	    		for(MeasurementData var : originalRow.getDataList()){
	    			if(var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null && var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()){
	    				//we change the date to the UI format
	    				var.setValue(DateUtil.convertToDBDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
	    			}
	    		}
	    	}
			map.put("success", "1");
			Map<String, Object> dataMap = generateDatatableDataMap(originalRow, null);
	    	map.put("data", dataMap);
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			map.put("success", "0");
			map.put("errorMessage", e.getMessage());
		}
		    			    	    	    
    	return map;
    }
    
    private void copyMeasurementValue(MeasurementRow origRow, MeasurementRow valueRow){
    	for(int index = 0 ; index < origRow.getDataList().size() ; index++){
    		MeasurementData data =  origRow.getDataList().get(index);
    		MeasurementData valueRowData = valueRow.getDataList().get(index);
    		if(data.getMeasurementVariable().getPossibleValues() != null && !data.getMeasurementVariable().getPossibleValues().isEmpty()){
    			if(valueRowData.getcValueId() != null){
	    			data.setcValueId(valueRowData.getcValueId());
	    			data.setValue(valueRowData.getcValueId());
    			}else if(valueRowData.getValue() != null){
    				data.setcValueId(valueRowData.getValue());
	    			data.setValue(valueRowData.getValue());
    			}
    		}else {
    			data.setValue(valueRowData.getValue());
    		}
    	}
    }
    
    private Map<String, Object> generateDatatableDataMap(MeasurementRow row, String suffix){
    	Map<String, Object> dataMap = new HashMap<String, Object>();
    	//the 4 attributes are needed always
    	dataMap.put("Action", Integer.toString(row.getExperimentId()));
		dataMap.put("experimentId", Integer.toString(row.getExperimentId()));
		dataMap.put("GID", row.getMeasurementDataValue(TermId.GID.getId()));
		dataMap.put("DESIGNATION", row.getMeasurementDataValue(TermId.DESIG.getId()));
		for(MeasurementData data : row.getDataList()){
			String displayVal = data.getDisplayValue();
			if(suffix != null)
				displayVal += suffix;
			dataMap.put(data.getMeasurementVariable().getName(), displayVal);
		}
		return dataMap;
    }
}
