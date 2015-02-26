package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.ValueReference;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.DateUtil;

@Controller
@RequestMapping(ObservationMatrixController.URL)
public class ObservationMatrixController extends
		AbstractBaseFieldbookController {

    public static final String MISSING_VALUE = "missing";
	private static final String TRIAL = "TRIAL";
	private static final Logger LOG = LoggerFactory.getLogger(ObservationMatrixController.class);
    public static final String URL = "/Common/addOrRemoveTraits";
    public static final String PAGINATION_TEMPLATE = "/Common/showAddOrRemoveTraitsPagination";
    public static final String PAGINATION_TEMPLATE_VIEW_ONLY = "/NurseryManager/showAddOrRemoveTraitsPagination";
    public static final String EDIT_EXPERIMENT_TEMPLATE = "/Common/updateExperimentModal";
    public static final String EDIT_EXPERIMENT_CELL_TEMPLATE = "/Common/updateExperimentCell";
	private static final String STATUS = "status";
	private static final String ERROR_MESSAGE = "errorMessage";
	private static final String INDEX = "index";
	private static final String SUCCESS = "success";
	private static final String TERM_ID = "termId";
	private static final String DATA = "data";
    
	@Resource
	private UserSelection studySelection;
	
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

    	boolean isTrial = studyType.equalsIgnoreCase(TRIAL);
    	UserSelection userSelection = getUserSelection(isTrial);    	
    	userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
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

    	boolean isTrial = studyType.equalsIgnoreCase(TRIAL);
    	UserSelection userSelection = getUserSelection(isTrial);
    	
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

    	boolean isTrial = studyType.equalsIgnoreCase(TRIAL);
    	UserSelection userSelection = getUserSelection(isTrial);
    	
    	Map<String, String> resultMap = new HashMap<String, String>();
        
        Workbook workbook = userSelection.getWorkbook();
        
        form.setMeasurementRowList(userSelection.getMeasurementRowList());
    	form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
    	form.setStudyName(workbook.getStudyDetails().getStudyName());
    	
        workbook.setObservations(form.getMeasurementRowList());
        workbook.updateTrialObservationsWithReferenceList(form.getTrialEnvironmentValues());

        try { 
        	validationService.validateObservationValues(workbook, "");
            fieldbookMiddlewareService.saveMeasurementRows(workbook);
            resultMap.put(STATUS, "1");
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
            resultMap.put(STATUS, "-1");
            resultMap.put(ERROR_MESSAGE, e.getMessage());
        }
        
        return resultMap;
    }

    private UserSelection getUserSelection(boolean isTrial) {
    	return this.studySelection;
    }
    protected void setStudySelection(UserSelection userSelection) {
    	this.studySelection = userSelection;
    }
   
    @RequestMapping(value="/update/experiment/{index}", method = RequestMethod.GET)
    public String editExperimentModal(@PathVariable int index,
            @ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, 
            Model model) throws MiddlewareQueryException {

    	
    	UserSelection userSelection = getUserSelection(false);
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
    	model.addAttribute("isNursery", userSelection.getWorkbook().isNursery());
        return super.showAjaxPage(model, EDIT_EXPERIMENT_TEMPLATE);
    }

    @ResponseBody
    @RequestMapping(value="/update/experiment/cell/data", method = RequestMethod.POST)
    public Map<String, Object> updateExperimentCellData( @RequestBody Map<String,String> data, HttpServletRequest req) {

    	Map<String, Object> map = new HashMap<String, Object>();
    	
    	int index = Integer.valueOf(data.get(INDEX));
    	int termId = Integer.valueOf(data.get(TERM_ID));
    	String value = data.get("value");
    	//for categorical
    	int isNew = Integer.valueOf(data.get("isNew"));
    	boolean isDiscard = "1".equalsIgnoreCase(req.getParameter("isDiscard")) ? true : false;
    	
    	map.put(INDEX, index);
    	
    	UserSelection userSelection = getUserSelection(false);
    	List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
    	tempList.addAll(userSelection.getMeasurementRowList());
    
    	MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);
    	
		try {
			if(!isDiscard){				
				MeasurementRow copyRow = originalRow.copy();
		    	copyMeasurementValue(copyRow, originalRow, isNew == 1 ? true : false);
				//we set the data to the copy row
		    	if(copyRow != null && copyRow.getMeasurementVariables() != null){
		    		updatePhenotypeValues(copyRow.getDataList(),value,termId,isNew);
		    	}
				validationService.validateObservationValues(userSelection.getWorkbook(), copyRow);
				//if there are no error, meaning everything is good, thats the time we copy it to the original
				copyMeasurementValue(originalRow, copyRow, isNew == 1 ? true : false);
				updateDates(originalRow);
			}
			map.put(SUCCESS, "1");
			Map<String, Object> dataMap = generateDatatableDataMap(originalRow, null);
	    	map.put(DATA, dataMap);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
			map.put(SUCCESS, "0");
			map.put(ERROR_MESSAGE, e.getMessage());
		}
		    			    	    	    
    	return map;
    }
    
    private void updateDates(MeasurementRow originalRow) {
    	if(originalRow != null && originalRow.getMeasurementVariables() != null){
    		for(MeasurementData var : originalRow.getDataList()){
    			convertToDBDateIfDate(var);
    		}
    	}
	}
    
    private void convertToUIDateIfDate(MeasurementData var) {
    	if(var != null && var.getMeasurementVariable() != null && 
    			var.getMeasurementVariable().getDataTypeId() != null && 
    			var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()){
			var.setValue(DateUtil.convertToUIDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
		}
	}
    
    private void convertToDBDateIfDate(MeasurementData var) {
    	if(var != null && var.getMeasurementVariable() != null && 
    			var.getMeasurementVariable().getDataTypeId() != null && 
    			var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()){
			var.setValue(DateUtil.convertToDBDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
		}
	}

	private void updatePhenotypeValues(List<MeasurementData> measurementDataList, String value, int termId, int isNew) {
		for(MeasurementData var : measurementDataList){
			if(var != null && var.getMeasurementVariable().getTermId() == termId) {
				if(var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || 
					!var.getMeasurementVariable().getPossibleValues().isEmpty()){
					if(isNew == 1){
						var.setcValueId(null);
						var.setCustomCategoricalValue(true);
					} else{
						var.setcValueId(value);
						var.setCustomCategoricalValue(false);
					}
					var.setValue(value);
					var.setAccepted(true);
				}else{
					var.setValue(value);
				}
				break;
			}
		}
	}

	@ResponseBody
    @RequestMapping(value="/update/experiment/cell/accepted", method = RequestMethod.POST)
    public Map<String, Object> markExperimentCellDataAsAccepted( @RequestBody Map<String,String> data, HttpServletRequest req) {

    	Map<String, Object> map = new HashMap<String, Object>();
    	
    	int index = Integer.valueOf(data.get(INDEX));
    	int termId = Integer.valueOf(data.get(TERM_ID));
    	
    	map.put(INDEX, index);
    	
    	UserSelection userSelection = getUserSelection(false);
    	MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);

		if(originalRow != null && originalRow.getMeasurementVariables() != null){
    		for(MeasurementData var : originalRow.getDataList()){	    			
    			if(var != null && var.getMeasurementVariable().getTermId() == termId && 
    				(var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || 
    				!var.getMeasurementVariable().getPossibleValues().isEmpty())){
    				var.setAccepted(true);
    				if (isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var.getMeasurementVariable().getPossibleValues())){
    					var.setCustomCategoricalValue(true);
    				}else{
    					var.setCustomCategoricalValue(false);
    				}
    				break;
    			}
    		}
    	}
		
		map.put(SUCCESS, "1");
		Map<String, Object> dataMap = generateDatatableDataMap(originalRow, null);
    	map.put(DATA, dataMap);
		    			    	    	    
    	return map;
    }
    
    @ResponseBody
    @RequestMapping(value="/update/experiment/cell/accepted/all", method = RequestMethod.GET)
    public Map<String, Object> markAllExperimentDataAsAccepted() {

    	Map<String, Object> map = new HashMap<String, Object>();

    	UserSelection userSelection = getUserSelection(false);
    	for (MeasurementRow row : userSelection.getMeasurementRowList())  {
    		if(row != null && row.getMeasurementVariables() != null){
        		markNonEmptyCategoricalValuesAsAccepted(row.getDataList());
        	}
    	}
    	
		map.put(SUCCESS, "1");
		    			    	    	    
    	return map;
    }
    
    private void markNonEmptyCategoricalValuesAsAccepted(List<MeasurementData> measurementDataList) {
    	for(MeasurementData var : measurementDataList){	    			
			if(var != null && !StringUtils.isEmpty(var.getValue()) && 
				(var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || 
				!var.getMeasurementVariable().getPossibleValues().isEmpty())){
				var.setAccepted(true);
				if (isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var.getMeasurementVariable().getPossibleValues())){
					var.setCustomCategoricalValue(true);
				}else{
					var.setCustomCategoricalValue(false);
				}
				
			}
		}
	}
    private void markNonEmptyCategoricalValuesAsMissing(List<MeasurementData> measurementDataList) {
    	for(MeasurementData var : measurementDataList){	    			
			if(var != null && !StringUtils.isEmpty(var.getValue()) && 
				(var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || 
				!var.getMeasurementVariable().getPossibleValues().isEmpty())){
				var.setAccepted(true);				
				if (isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var.getMeasurementVariable().getPossibleValues())){
					var.setValue(MISSING_VALUE);
					var.setCustomCategoricalValue(true);
				}else{
					var.setCustomCategoricalValue(false);
				}
			}
		}
	}

	@ResponseBody
    @RequestMapping(value="/update/experiment/cell/missing/all", method = RequestMethod.GET)
    public Map<String, Object> markAllExperimentDataAsMissing() {
    	Map<String, Object> map = new HashMap<String, Object>();
    	UserSelection userSelection = getUserSelection(false);
    	for (MeasurementRow row : userSelection.getMeasurementRowList())  {
    		if(row != null && row.getMeasurementVariables() != null){
    			markNonEmptyCategoricalValuesAsMissing(row.getDataList());
        	}
    	}
		map.put(SUCCESS, "1");    	    
    	return map;
    }
        
   
    @RequestMapping(value="/update/experiment/cell/{index}/{termId}", method = RequestMethod.GET)
    public String editExperimentCells(@PathVariable int index, @PathVariable int termId,              
            Model model) throws MiddlewareQueryException {
    	
    	UserSelection userSelection = getUserSelection(false);
    	List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
    	tempList.addAll(userSelection.getMeasurementRowList());

    	MeasurementRow row = tempList.get(index);    
    	MeasurementRow copyRow = row.copy();
    	copyMeasurementValue(copyRow, row);
    	MeasurementData editData = null;
    	List<ValueReference> possibleValues = new ArrayList<ValueReference>();
    	if(copyRow != null && copyRow.getMeasurementVariables() != null){
    		for(MeasurementData var : copyRow.getDataList()){
    			convertToUIDateIfDate(var);
    			if(var != null && (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var.getMeasurementVariable().getPossibleValues().isEmpty())){
    				possibleValues = var.getMeasurementVariable().getPossibleValues();
    			}
    			if(var != null && var.getMeasurementVariable().getTermId() == termId){
    				editData = var;
    				break;
    			}
    		}
    	}
    	updateModel(model,userSelection.getWorkbook().isNursery(),editData, index, termId, possibleValues);
        return super.showAjaxPage(model, EDIT_EXPERIMENT_CELL_TEMPLATE);
    }

	private void updateModel(Model model, boolean isNursery, MeasurementData measurementData,
    		int index, int termId, List<ValueReference> possibleValues) {
    	model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
    	model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
    	model.addAttribute("isNursery", isNursery);
    	model.addAttribute("measurementData", measurementData);
    	model.addAttribute(INDEX, index);
    	model.addAttribute(TERM_ID, termId);
    	model.addAttribute("possibleValues", possibleValues);
	}

	@ResponseBody
    @RequestMapping(value="/data/table/ajax", method = RequestMethod.GET)
    public List<Map<String, Object>> getPageDataTablesAjax(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) {
    	
    	UserSelection userSelection = getUserSelection(false);
    	List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
    	tempList.addAll(userSelection.getMeasurementRowList());
    	form.setMeasurementRowList(tempList);
    	
    	List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();
    	
    	for(MeasurementRow row : tempList){
    		    		
    		Map<String, Object> dataMap = generateDatatableDataMap(row, null);
    		
    		masterList.add(dataMap);
    	}
   	
    	return masterList;
    }
    @ResponseBody
    @RequestMapping(value="/data/table/ajax/submit/{index}", method = RequestMethod.POST)
    public Map<String, Object> dataTablesAjaxSubmit(@PathVariable int index, @ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	UserSelection userSelection = getUserSelection(false);
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
			updateDates(originalRow);
			map.put(SUCCESS, "1");
			Map<String, Object> dataMap = generateDatatableDataMap(originalRow, null);
	    	map.put(DATA, dataMap);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
			map.put(SUCCESS, "0");
			map.put(ERROR_MESSAGE, e.getMessage());
		}
		    			    	    	    
    	return map;
    }

    protected boolean isCategoricalValueOutOfBounds(String cValueId, String value, List<ValueReference> possibleValues){
    	
    	String val = cValueId;
    	if (val == null ){
    		val = value;
    	}
    	
    	for (ValueReference ref : possibleValues){
    		if (ref.getKey().equals(val)){
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    protected void copyMeasurementValue(MeasurementRow origRow, MeasurementRow valueRow){
    	this.copyMeasurementValue(origRow, valueRow, false);
    }
    protected void copyMeasurementValue(MeasurementRow origRow, MeasurementRow valueRow, boolean isNew){

    	for(int index = 0 ; index < origRow.getDataList().size() ; index++){
    		MeasurementData data =  origRow.getDataList().get(index);
    		MeasurementData valueRowData = valueRow.getDataList().get(index);
    		copyMeasurementDataValue(data,valueRowData,isNew);
    	}
    }
    
    private void copyMeasurementDataValue(MeasurementData oldData,
			MeasurementData newData, boolean isNew) {
    	if(oldData.getMeasurementVariable().getPossibleValues() != null && !oldData.getMeasurementVariable().getPossibleValues().isEmpty()){		
    		oldData.setAccepted(newData.isAccepted());
    		if (!StringUtils.isEmpty(oldData.getValue()) 
					&& oldData.isAccepted() 
					&& isCategoricalValueOutOfBounds(oldData.getcValueId(), oldData.getValue(), 
							oldData.getMeasurementVariable().getPossibleValues())){
				oldData.setCustomCategoricalValue(true);
			}else{
				oldData.setCustomCategoricalValue(false);
			}
			if(newData.getcValueId() != null){
				if(isNew){
					oldData.setCustomCategoricalValue(true);
					oldData.setcValueId(null);
				}else{
					oldData.setcValueId(newData.getcValueId());
					oldData.setCustomCategoricalValue(false);
				}
				oldData.setValue(newData.getcValueId());
			}else if(newData.getValue() != null){
				if(isNew){
					oldData.setCustomCategoricalValue(true);
					oldData.setcValueId(null);
				}else{
					oldData.setcValueId(newData.getValue());
					oldData.setCustomCategoricalValue(false);
				}
				oldData.setValue(newData.getValue());
			}
		}else {
			oldData.setValue(newData.getValue());
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
			if(suffix != null) {
				displayVal += suffix;
			}
			
			if (data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())){
				Object[] categArray = new Object[] {displayVal, data.isAccepted()};
				dataMap.put(data.getMeasurementVariable().getName(), categArray);
			}else{
				dataMap.put(data.getMeasurementVariable().getName(), displayVal);
			}
		}
		UserSelection userSelection = getUserSelection(false);
		if(userSelection != null && userSelection.getMeasurementDatasetVariable() != null && !userSelection.getMeasurementDatasetVariable().isEmpty()){
			for(MeasurementVariable var : userSelection.getMeasurementDatasetVariable()){
				if(!dataMap.containsKey(var.getName())){
					dataMap.put(var.getName(), "");
				}
			}
		}
		return dataMap;
    }

	public void setValidationService(ValidationService validationService) {
		this.validationService = validationService;
	}
    
}
