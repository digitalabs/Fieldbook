package com.efficio.fieldbook.web.nursery.controller;

/**
 * Created by cyrus on 5/8/15.
 */

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;
import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.*;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;


/**
 * The Class DesignImportController.
 */
@Controller
@RequestMapping(DesignImportController.URL)
public class DesignImportController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(DesignImportController.class);
	
	public static final String URL = "/DesignImport";
	public static final String REVIEW_DETAILS_PAGINATION_TEMPLATE = "/DesignImport/reviewDetailsPagination";

	@Resource
	private DesignImportParser parser;
	
	@Resource
	private UserSelection userSelection;
	
	@Resource
	private DesignImportService designImportService;
	
	@Resource
    private MessageSource messageSource;
	
	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	
	/* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#show(org.springframework.ui.Model)
     */
	@RequestMapping(method = RequestMethod.GET)
	public String show(Model model) {
		return super.showAngularPage(model);
	}

	@Override public String getContentName() {
		return String.format("%s/designImportMain", URL);
	}


	@ResponseBody
	@RequestMapping(value = "/import/{studyType}", method = RequestMethod.POST, produces="text/plain")
	public String importFile(
			@ModelAttribute("importDesignForm") ImportDesignForm form, @PathVariable String studyType) {

		Map<String, Object> resultsMap = new HashMap<>();
		
		try {
			
			initializeTemporaryWorkbook(studyType);
			
			DesignImportData designImportData = parser.parseFile(form.getFile());
			
			performAutomap(designImportData);
			
			userSelection.setDesignImportData(designImportData);

			resultsMap.put("isSuccess", 1);
			
		} catch (MiddlewareQueryException | FileParsingException e) {
			
			LOG.error(e.getMessage(), e);
			
			resultsMap.put("isSuccess", 0);
			// error messages is still in .prop format,
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		// we return string instead of json to fix IE issue rel. DataTable
		return convertObjectToJson(resultsMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getMappingData", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public Map<String,List<DesignHeaderItem>> getMappingData() {
		Map<String,List<DesignHeaderItem>> mappingData = new HashMap<>();

		mappingData.put("unmappedHeaders", userSelection.getDesignImportData().getUnmappedHeaders());
		mappingData.put("mappedEnvironmentalFactors", userSelection.getDesignImportData().getMappedHeaders().get(
				PhenotypicType.TRIAL_ENVIRONMENT));
		mappingData.put("mappedDesignFactors", userSelection.getDesignImportData().getMappedHeaders().get(
				PhenotypicType.TRIAL_DESIGN));
		mappingData.put("mappedGermplasmFactors", userSelection.getDesignImportData().getMappedHeaders().get(
				PhenotypicType.GERMPLASM));
		mappingData.put("mappedTraits", userSelection.getDesignImportData().getMappedHeaders().get(
				PhenotypicType.VARIATE));



		return mappingData;
	}

	@RequestMapping(value = "/showDetails", method = RequestMethod.GET)
	public String showDetails(Model model) {
			
			Workbook workbook = userSelection.getTemporaryWorkbook();
			DesignImportData designImportData = userSelection.getDesignImportData();
			
			Set<MeasurementVariable> measurementVariables = designImportService.getDesignMeasurementVariables(workbook, designImportData);
	    	
			model.addAttribute("measurementVariables", measurementVariables);
			
	    	return super.showAjaxPage(model, REVIEW_DETAILS_PAGINATION_TEMPLATE);
		
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/showDetails/data", method = RequestMethod.POST, 
		produces = "application/json; charset=utf-8")
	public List<Map<String, Object>> showDetailsData(@RequestBody EnvironmentData environmentData, Model model,
			@ModelAttribute("importDesignForm") ImportDesignForm form) {
			
		processEnvironmentData(environmentData);
		
		Workbook workbook = userSelection.getTemporaryWorkbook();
		DesignImportData designImportData = userSelection.getDesignImportData();
		
			List<MeasurementRow> measurementRows = new ArrayList<>();
			
			try {
				measurementRows = designImportService.generateDesign(workbook, designImportData, environmentData);
			} catch (DesignValidationException e) {
				LOG.error(e.getMessage(), e);
			}
	    	
	    	List<Map<String, Object>> masterList = new ArrayList<>();
	    	
	    	for(MeasurementRow row : measurementRows){
	    		    		
	    		Map<String, Object> dataMap = generateDatatableDataMap(row, null);
	    		
	    		masterList.add(dataMap);
	    	}
	   	
	    	return masterList;
		
	}

	@ResponseBody
	@RequestMapping(value="/postSelectedNurseryType")
	public Boolean postSelectedNurseryType(@RequestBody String nurseryTypeId) {
		if (StringUtils.isNumeric(nurseryTypeId)) {
			Integer value = Integer.valueOf(nurseryTypeId);
			userSelection.setNurseryTypeForDesign(value);
		}

		return true;
	}

	@ResponseBody
	@RequestMapping(value = "/validateAndSaveNewMapping/{noOfEnvironments}", method = RequestMethod.POST)
	public Map<String,Object> validateAndSaveNewMapping(@RequestBody Map<String,List<DesignHeaderItem>> mappedHeaders,@PathVariable Integer noOfEnvironments) {

		Map<String,Object> resultsMap = new HashMap<>();
		try {
			updateDesignMapping(mappedHeaders);

			designImportService.validateDesignData(userSelection.getDesignImportData());

			if (!designImportService.areTrialInstancesMatchTheSelectedEnvironments(noOfEnvironments, userSelection.getDesignImportData())){
				resultsMap.put("warning", messageSource.getMessage("design.import.warning.trial.instances.donotmatch", null, Locale.ENGLISH));
			}

			resultsMap.put("success", Boolean.TRUE);

		} catch (MiddlewareQueryException | DesignValidationException e) {

			LOG.error(e.getMessage(), e);

			resultsMap.put("success", Boolean.FALSE);
			resultsMap.put("error",e.getMessage());
			resultsMap.put("message",e.getMessage());
		}

		return resultsMap;
	}

	protected void updateDesignMapping(Map<String, List<DesignHeaderItem>> mappedHeaders) throws MiddlewareQueryException {
		Map<PhenotypicType,List<DesignHeaderItem>> newMappingResults = new HashMap<>();

		for (Map.Entry<String,List<DesignHeaderItem>> item : mappedHeaders.entrySet()) {
			for (DesignHeaderItem mappedHeader : item.getValue()) {

				StandardVariable stdVar = fieldbookMiddlewareService.getStandardVariable(
						mappedHeader.getId());

				mappedHeader.setVariable(stdVar);
			}

			if ("mappedEnvironmentalFactors".equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.TRIAL_ENVIRONMENT, item.getValue());
			} else if ("mappedDesignFactors".equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.TRIAL_DESIGN, item.getValue());
			} else if ("mappedGermplasmFactors".equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.GERMPLASM, item.getValue());
			} else if ("mappedTraits".equals(item.getKey())) {
				newMappingResults.put(PhenotypicType.VARIATE, item.getValue());
			}
		}

		userSelection.getDesignImportData().setMappedHeaders(newMappingResults);
	}

	@ResponseBody
	@RequestMapping(value = "/generate",  method = RequestMethod.POST , 
		produces = "application/json; charset=utf-8")
	public Map<String, Object> showMeasurements(@RequestBody EnvironmentData environmentData) {

		Map<String, Object> resultsMap = new HashMap<>();
		
		processEnvironmentData(environmentData);
		
		try {
			
			Workbook workbook = userSelection.getTemporaryWorkbook();
			DesignImportData designImportData = userSelection.getDesignImportData();
			
			List<MeasurementRow> measurementRows;
			Set<MeasurementVariable> measurementVariables;
			Set<StandardVariable> expDesignVariables;
			Set<MeasurementVariable> experimentalDesignMeasurementVariables;
			
			measurementRows = designImportService.generateDesign(workbook, designImportData, environmentData);
			measurementVariables = designImportService.getDesignMeasurementVariables(workbook, designImportData);
			expDesignVariables = designImportService.getDesignRequiredStandardVariables(workbook, designImportData);
			experimentalDesignMeasurementVariables = designImportService.getDesignRequiredMeasurementVariable(workbook, designImportData);
			
			workbook.setObservations(measurementRows);
			workbook.setMeasurementDatasetVariables(new ArrayList<MeasurementVariable>(measurementVariables));
			workbook.setExpDesignVariables(new ArrayList<StandardVariable>(expDesignVariables));
			
			userSelection.setExperimentalDesignVariables(new ArrayList<MeasurementVariable>(experimentalDesignMeasurementVariables));
		
			ExpDesignParameterUi designParam = new ExpDesignParameterUi();
			designParam.setDesignType(3);
			userSelection.setExpDesignParams(designParam);
			
			List<Integer> expDesignTermIds = new ArrayList<>();
			expDesignTermIds.add(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
			userSelection.setExpDesignVariables(expDesignTermIds);

			
			resultsMap.put("isSuccess", 1);

		} catch (Exception e) {
			
			LOG.error(e.getMessage(), e);
			
			resultsMap.put("isSuccess", 0);
			// error messages is still in .prop format,
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		return resultsMap;
	}
	
	private Map<String, Object> generateDatatableDataMap(MeasurementRow row, String suffix){
    	Map<String, Object> dataMap = new HashMap<String, Object>();
    	//the 4 attributes are needed always
		for(MeasurementData data : row.getDataList()){
			String displayVal = data.getDisplayValue();
			if(suffix != null) {
				displayVal += suffix;
			}
			
			if (data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId()) ||
					data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())){
				Object[] categArray = new Object[] {displayVal, data.isAccepted()};
				dataMap.put(data.getMeasurementVariable().getName(), categArray);
			} else{
				dataMap.put(data.getMeasurementVariable().getName(), displayVal);
			}
		}
		return dataMap;
    }
	
	public void initializeTemporaryWorkbook(String studyType){
		
		
	      List<SettingDetail> studyLevelConditions = userSelection.getStudyLevelConditions();
	         List<SettingDetail> basicDetails = userSelection.getBasicDetails();
	         // transfer over data from user input into the list of setting details stored in the session
	    	 List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
	         combinedList.addAll(basicDetails);

	         if (studyLevelConditions != null) {             
	             combinedList.addAll(studyLevelConditions);
	         }

	         String name = "";

	         
	        Workbook workbook = null;
	        StudyDetails details = new StudyDetails();
	        
	        if ("T".equalsIgnoreCase(studyType)){
	        	
	        	Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, combinedList,
		    			userSelection.getPlotsLevelList(), userSelection.getBaselineTraitsList(), userSelection, userSelection.getTrialLevelVariableList(),
		    			userSelection.getTreatmentFactors(), null, null, userSelection.getNurseryConditions(), false);

	        	workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false);
	        	
	        	details.setStudyType(StudyType.T);
	        	
	        }else{
	        	
	        	List<SettingDetail> variatesList = new ArrayList<>();
	        	
	        	if (userSelection.getBaselineTraitsList() != null){
	        		variatesList.addAll(userSelection.getBaselineTraitsList());
	        	}
	        	
	        	if (userSelection.getSelectionVariates() != null){
	        		variatesList.addAll(userSelection.getSelectionVariates());
	        	}
	        	
	        	Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, combinedList,
		    			userSelection.getPlotsLevelList(), variatesList, userSelection, userSelection.getTrialLevelVariableList(),
		    			userSelection.getTreatmentFactors(), null, null, userSelection.getNurseryConditions(), true);

	        	workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true);
	        	
	        	details.setStudyType(StudyType.N);
	        	
	        }
	        
	        workbook.setStudyDetails(details);
	        
	        userSelection.setTemporaryWorkbook(workbook);
	        
	}
	
	protected void performAutomap(DesignImportData designImportData) throws MiddlewareQueryException{
		Map<PhenotypicType,List<DesignHeaderItem>> result = designImportService.categorizeHeadersByPhenotype(designImportData.getUnmappedHeaders());

		designImportData.setMappedHeaders(result);
		designImportData.setUnmappedHeaders(result.get(null));

	}
	
	protected void processEnvironmentData(EnvironmentData data) {
        for (int i = 0; i < data.getEnvironments().size(); i++) {
            Map<String, String> values = data.getEnvironments().get(i).getManagementDetailValues();
            if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))) {
                values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
            } else if (values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null || values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
                values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
            }
        }
    }
	
}
