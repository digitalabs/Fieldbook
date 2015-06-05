package com.efficio.fieldbook.web.nursery.controller;

/**
 * Created by cyrus on 5/8/15.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;


/**
 * The Class DesignImportController.
 */
@Controller
@RequestMapping(DesignImportController.URL)
public class DesignImportController extends SettingsController {

	private static final Logger LOG = LoggerFactory.getLogger(DesignImportController.class);
	
	public static final String URL = "/DesignImport";
	public static final String REVIEW_DETAILS_PAGINATION_TEMPLATE = "/DesignImport/reviewDetailsPagination";

	@Resource
	private DesignImportParser parser;

	@Resource
	private DesignImportService designImportService;
	
	@Resource
    private MessageSource messageSource;

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
			
			Set<MeasurementVariable> measurementVariables = designImportService.getDesignMeasurementVariables(workbook, designImportData, true);
	    	
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
				measurementRows = designImportService.generateDesign(workbook, designImportData, environmentData, true);
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

			boolean hasConflict = userSelection.getWorkbook() != null && hasConflict(
					designImportService.getDesignMeasurementVariables(userSelection.getTemporaryWorkbook(),
									userSelection.getDesignImportData()),
					new HashSet<>(userSelection.getWorkbook().getMeasurementDatasetVariables()));


			resultsMap.put("success", Boolean.TRUE);
			resultsMap.put("hasConflict",hasConflict);
		} catch (MiddlewareQueryException | DesignValidationException e) {

			LOG.error(e.getMessage(), e);

			resultsMap.put("success", Boolean.FALSE);
			resultsMap.put("error",e.getMessage());
			resultsMap.put("message",e.getMessage());
		}

		return resultsMap;
	}

	protected boolean hasConflict(Set<MeasurementVariable> setA,Set<MeasurementVariable> setB) {
		Set<MeasurementVariable> a;
		Set<MeasurementVariable> b;

		if (setA.size() <= setB.size()) {
			a = setA;
			b = setB;
		} else {
			a = setB;
			b = setA;
		}

		for (MeasurementVariable e : a) {
			if (b.contains(e)) {
				return true;
			}
		}
		return false;
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
	public Map<String, Object> generateMeasurements(@RequestBody EnvironmentData environmentData) {

		Map<String, Object> resultsMap = new HashMap<>();

		processEnvironmentData(environmentData);
		
		try {
			
			Workbook workbook = userSelection.getTemporaryWorkbook();
			DesignImportData designImportData = userSelection.getDesignImportData();
			
			List<MeasurementRow> measurementRows;
			Set<MeasurementVariable> measurementVariables;
			Set<StandardVariable> expDesignVariables;
			Set<MeasurementVariable> experimentalDesignMeasurementVariables;

			measurementRows = designImportService.generateDesign(workbook, designImportData,
					environmentData, false);
			measurementVariables = designImportService.getDesignMeasurementVariables(workbook,
					designImportData, false);
			
			expDesignVariables = designImportService.getDesignRequiredStandardVariables(workbook,
					designImportData);
			
			experimentalDesignMeasurementVariables = designImportService.getDesignRequiredMeasurementVariable(workbook, designImportData);
			
			workbook.setObservations(measurementRows);

			workbook.setMeasurementDatasetVariables(new ArrayList<>(measurementVariables));
			workbook.setExpDesignVariables(new ArrayList<>(expDesignVariables));
			
			Set<MeasurementVariable> uniqueFactors = new HashSet<>(workbook.getFactors());
			uniqueFactors.addAll(designImportService
					.extractMeasurementVariable(PhenotypicType.TRIAL_ENVIRONMENT,
							designImportData.getMappedHeaders()));
			workbook.getFactors().clear();
			workbook.getFactors().addAll((new ArrayList<>(uniqueFactors)));

			Set<MeasurementVariable> uniqueVariates = new HashSet<>(workbook.getVariates());
			uniqueVariates.addAll(designImportService
					.extractMeasurementVariable(PhenotypicType.VARIATE,
							designImportData.getMappedHeaders()));
			workbook.getVariates().clear();
			workbook.getVariates().addAll((new ArrayList<>(uniqueVariates)));

			userSelection.setExperimentalDesignVariables(new ArrayList<>(experimentalDesignMeasurementVariables));

			ExpDesignParameterUi designParam = new ExpDesignParameterUi();
			designParam.setDesignType(3);
			userSelection.setExpDesignParams(designParam);
			
			List<Integer> expDesignTermIds = new ArrayList<>();
			expDesignTermIds.add(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
			userSelection.setExpDesignVariables(expDesignTermIds);

			// retrieve all trial level factors and convert them to setting details
			Set<MeasurementVariable> trialLevelFactors = new HashSet<>();
			for (MeasurementVariable factor : workbook.getFactors()) {
				if (PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().contains(factor.getLabel())) {
					trialLevelFactors.add(factor);
				}
			}

			List<SettingDetail> newDetails = SettingsUtil.convertWorkbookFactorsToSettingDetails(new ArrayList<MeasurementVariable>(trialLevelFactors), fieldbookMiddlewareService);
			populateThePossibleValues(newDetails);
			SettingsUtil.addNewSettingDetails(AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt(), newDetails, userSelection);
			
			//add experiment design factor
			TermId termId = TermId.getById(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
			SettingsUtil.addTrialCondition(termId, designParam, workbook, fieldbookMiddlewareService);
			
			//get the Experiment Design MeasurementVariable
			List<MeasurementVariable> trialVariables = new ArrayList<>(designImportService.extractMeasurementVariable(PhenotypicType.TRIAL_ENVIRONMENT,designImportData.getMappedHeaders()));
			for (MeasurementVariable trialCondition :workbook.getTrialConditions()){
				if (trialCondition.getTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId()){
					trialVariables.add(trialCondition);
				}
			}
		
		    resolveTheEnvironmentFactorsWithIDNamePairing(environmentData, workbook, designImportData, userSelection.getTrialLevelVariableList());
			
			List<MeasurementRow> trialEnvironmentValues = WorkbookUtil.createMeasurementRowsFromEnvironments(
					environmentData.getEnvironments(), 
					trialVariables,
					userSelection.getExpDesignParams());
			
		    workbook.setTrialObservations(trialEnvironmentValues);
		    


			resultsMap.put("isSuccess", 1);
			resultsMap.put("environmentData", environmentData);
			resultsMap.put("environmentSettings", userSelection.getTrialLevelVariableList());

		} catch (Exception e) {
			
			LOG.error(e.getMessage(), e);
			
			resultsMap.put("isSuccess", 0);
			// error messages is still in .prop format,
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		return resultsMap;
	}
	
	private void populateThePossibleValues(List<SettingDetail> newDetails) throws MiddlewareQueryException {
		for (SettingDetail settingDetail : newDetails){
			
			List<ValueReference> possibleValues =
					fieldbookService.getAllPossibleValues(settingDetail.getVariable().getCvTermId());
			settingDetail.setPossibleValues(possibleValues);
			settingDetail.setDeletable(true);
			
		}
		
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
	
	protected void resolveTheEnvironmentFactorsWithIDNamePairing(EnvironmentData environmentData,
			Workbook workbook, DesignImportData designImportData, List<SettingDetail> list) throws MiddlewareQueryException {
		
		Map<String, String> idNameMap = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
		Map<String, String> NameIdMap = switchKey(idNameMap);
		
		for (Environment environment : environmentData.getEnvironments()){
			
			Map<String, String> copyOfManagementDetailValues = new HashMap<>();
			copyOfManagementDetailValues.putAll(environment.getManagementDetailValues());
			
			for (Entry<String, String> managementDetail : environment.getManagementDetailValues().entrySet()){
				
				//For TRIAL_LOCATION (Location Name)
				if (Integer.valueOf(managementDetail.getKey()) == TermId.TRIAL_LOCATION.getId()){
					String termId = NameIdMap.get(managementDetail.getKey());
					if (termId != null){
						
						Location location = fieldbookMiddlewareService.getLocationByName(managementDetail.getValue(), Operation.EQUAL);
						copyOfManagementDetailValues.put(termId, String.valueOf(location.getLocid()));
						
						SettingDetail settingDetail = createSettingDetail(Integer.valueOf(termId), getHeaderName(Integer.valueOf(managementDetail.getKey()), designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT)));
						addSettingDetailToTrialLevelVariableListIfNecessary(settingDetail);
						
						copyOfManagementDetailValues.remove(managementDetail.getKey());
						SettingsUtil.deleteVariableInSession(list, Integer.valueOf(managementDetail.getKey()));
					}
				}
				
				//For COOPERATOR
				if (Integer.valueOf(managementDetail.getKey()) == 8373){
					String termId = NameIdMap.get(managementDetail.getKey());
					if (termId != null){
						
						copyOfManagementDetailValues.put(termId, String.valueOf(super.getCurrentIbdbUserId()));
						
						SettingDetail settingDetail = createSettingDetail(Integer.valueOf(termId), getHeaderName(Integer.valueOf(managementDetail.getKey()), designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT)));
						addSettingDetailToTrialLevelVariableListIfNecessary(settingDetail);
						
						copyOfManagementDetailValues.remove(managementDetail.getKey());
						SettingsUtil.deleteVariableInSession(list, Integer.valueOf(managementDetail.getKey()));
					}
					
				}
				
				//For Categorical Variables
				StandardVariable tempStandardVarable = fieldbookMiddlewareService.getStandardVariable(Integer.valueOf(managementDetail.getKey()));
				if (tempStandardVarable != null && tempStandardVarable.hasEnumerations()){
					
					Enumeration enumeration = findInEnumeration(managementDetail.getValue(), tempStandardVarable.getEnumerations());
					if (enumeration != null){
						copyOfManagementDetailValues.put(managementDetail.getKey(), String.valueOf(enumeration.getId()));
					}
				}
				
			}	
			
			environment.getManagementDetailValues().clear();
			environment.getManagementDetailValues().putAll(copyOfManagementDetailValues);
			
		}
		
	}

	private Enumeration findInEnumeration(String value, List<Enumeration> enumerations) {
		for (Enumeration enumeration : enumerations){
			if (enumeration.getName().equalsIgnoreCase(value)){
				return enumeration;
			}else if (enumeration.getDescription().equalsIgnoreCase(value)){
				return enumeration;
			}
		}
		return null;
	}

	protected void addSettingDetailToTrialLevelVariableListIfNecessary(SettingDetail settingDetail){
		
		for (SettingDetail sd : userSelection.getTrialLevelVariableList()){
			if (sd.getVariable().getCvTermId().intValue() == settingDetail.getVariable().getCvTermId().intValue()){
				return;
			}
		}
		userSelection.getTrialLevelVariableList().add(settingDetail);
	}
	
	
	private String getHeaderName(int termId, List<DesignHeaderItem> items){
		for (DesignHeaderItem item : items){
			if (item.getId() == termId){
				return item.getName();
			}
		}
		return "";
	}
	
	private Map<String, String> switchKey(Map<String, String> map){
		Map<String, String> newMap = new HashMap<>();
		for (Entry<String, String> entry : map.entrySet()){
			newMap.put(entry.getValue(), entry.getKey());
		}
		return newMap;
	}
	

   
    
    
	
}
