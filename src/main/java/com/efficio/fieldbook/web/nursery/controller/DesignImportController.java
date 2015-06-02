
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
import com.efficio.fieldbook.web.util.AppConstants;

import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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

import javax.annotation.Resource;

import java.util.*;


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
	
	/*
	 * (non-Javadoc)
	 * 
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#show(org.springframework.ui.Model)
     */
	@Override
	@RequestMapping(method = RequestMethod.GET)
	public String show(Model model) {
		return super.showAngularPage(model);
	}

	@Override
	public String getContentName() {
		return String.format("%s/designImportMain", DesignImportController.URL);
	}

	@ResponseBody
	@RequestMapping(value = "/import/{studyType}", method = RequestMethod.POST, produces = "text/plain")
	public String importFile(@ModelAttribute("importDesignForm") ImportDesignForm form, @PathVariable String studyType) {

		Map<String, Object> resultsMap = new HashMap<>();
		
		try {
			
			this.initializeTemporaryWorkbook(studyType);
			
			DesignImportData designImportData = this.parser.parseFile(form.getFile());
			
			this.performAutomap(designImportData);
			
			this.userSelection.setDesignImportData(designImportData);

			resultsMap.put("isSuccess", 1);
			
		} catch (MiddlewareQueryException | FileParsingException e) {
			
			DesignImportController.LOG.error(e.getMessage(), e);
			
			resultsMap.put("isSuccess", 0);
			// error messages is still in .prop format,
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		// we return string instead of json to fix IE issue rel. DataTable
		return this.convertObjectToJson(resultsMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getMappingData", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public Map<String, List<DesignHeaderItem>> getMappingData() {
		Map<String, List<DesignHeaderItem>> mappingData = new HashMap<>();

		mappingData.put("unmappedHeaders", this.userSelection.getDesignImportData().getUnmappedHeaders());
		mappingData.put("mappedEnvironmentalFactors",
				this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));
		mappingData
				.put("mappedDesignFactors", this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.TRIAL_DESIGN));
		mappingData
				.put("mappedGermplasmFactors", this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.GERMPLASM));
		mappingData.put("mappedTraits", this.userSelection.getDesignImportData().getMappedHeaders().get(PhenotypicType.VARIATE));

		return mappingData;
	}

	@RequestMapping(value = "/showDetails", method = RequestMethod.GET)
	public String showDetails(Model model) {
			
		Workbook workbook = this.userSelection.getTemporaryWorkbook();
		DesignImportData designImportData = this.userSelection.getDesignImportData();
			
		Set<MeasurementVariable> measurementVariables = this.designImportService.getDesignMeasurementVariables(workbook, designImportData);
	    	
			model.addAttribute("measurementVariables", measurementVariables);
			
		return super.showAjaxPage(model, DesignImportController.REVIEW_DETAILS_PAGINATION_TEMPLATE);
		
	}
	
	@ResponseBody
	@RequestMapping(value = "/showDetails/data", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public List<Map<String, Object>> showDetailsData(@RequestBody EnvironmentData environmentData, Model model,
			@ModelAttribute("importDesignForm") ImportDesignForm form) {
			
		this.processEnvironmentData(environmentData);
		
		Workbook workbook = this.userSelection.getTemporaryWorkbook();
		DesignImportData designImportData = this.userSelection.getDesignImportData();
		
			List<MeasurementRow> measurementRows = new ArrayList<>();
			
			try {
			measurementRows = this.designImportService.generateDesign(workbook, designImportData, environmentData);
			} catch (DesignValidationException e) {
			DesignImportController.LOG.error(e.getMessage(), e);
			}
	    	
	    	List<Map<String, Object>> masterList = new ArrayList<>();
	    	
		for (MeasurementRow row : measurementRows) {
	    		    		
			Map<String, Object> dataMap = this.generateDatatableDataMap(row, null);
	    		
	    		masterList.add(dataMap);
	    	}
	   	
	    	return masterList;
		
	}

	@ResponseBody
	@RequestMapping(value = "/postSelectedNurseryType")
	public Boolean postSelectedNurseryType(@RequestBody String nurseryTypeId) {
		if (StringUtils.isNumeric(nurseryTypeId)) {
			Integer value = Integer.valueOf(nurseryTypeId);
			this.userSelection.setNurseryTypeForDesign(value);
		}

		return true;
	}

	@ResponseBody
	@RequestMapping(value = "/validateAndSaveNewMapping/{noOfEnvironments}", method = RequestMethod.POST)
	public Map<String, Object> validateAndSaveNewMapping(@RequestBody Map<String, List<DesignHeaderItem>> mappedHeaders,
			@PathVariable Integer noOfEnvironments) {

		Map<String, Object> resultsMap = new HashMap<>();
		try {
			this.updateDesignMapping(mappedHeaders);

			this.designImportService.validateDesignData(this.userSelection.getDesignImportData());

			if (!this.designImportService.areTrialInstancesMatchTheSelectedEnvironments(noOfEnvironments,
					this.userSelection.getDesignImportData())) {
				resultsMap.put("warning",
						this.messageSource.getMessage("design.import.warning.trial.instances.donotmatch", null, Locale.ENGLISH));
			}

			resultsMap.put("success", Boolean.TRUE);

		} catch (MiddlewareQueryException | DesignValidationException e) {

			DesignImportController.LOG.error(e.getMessage(), e);

			resultsMap.put("success", Boolean.FALSE);
			resultsMap.put("error", e.getMessage());
			resultsMap.put("message", e.getMessage());
		}

		return resultsMap;
	}

	protected void updateDesignMapping(Map<String, List<DesignHeaderItem>> mappedHeaders) throws MiddlewareQueryException {
		Map<PhenotypicType, List<DesignHeaderItem>> newMappingResults = new HashMap<>();

		for (Map.Entry<String, List<DesignHeaderItem>> item : mappedHeaders.entrySet()) {
			for (DesignHeaderItem mappedHeader : item.getValue()) {

				StandardVariable stdVar = this.fieldbookMiddlewareService.getStandardVariable(mappedHeader.getId());

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

		this.userSelection.getDesignImportData().setMappedHeaders(newMappingResults);
	}

	@ResponseBody
	@RequestMapping(value = "/generate", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public Map<String, Object> showMeasurements(@RequestBody EnvironmentData environmentData) {

		Map<String, Object> resultsMap = new HashMap<>();
		
		this.processEnvironmentData(environmentData);
		
		try {
			
			Workbook workbook = this.userSelection.getTemporaryWorkbook();
			DesignImportData designImportData = this.userSelection.getDesignImportData();
			
			List<MeasurementRow> measurementRows;
			Set<MeasurementVariable> measurementVariables;
			Set<StandardVariable> expDesignVariables;
			Set<MeasurementVariable> experimentalDesignMeasurementVariables;
			
			measurementRows = designImportService.generateDesign(workbook, designImportData,
					environmentData);
			measurementVariables = designImportService.getDesignMeasurementVariables(workbook,
					designImportData);
			expDesignVariables = designImportService.getDesignRequiredStandardVariables(workbook,
					designImportData);
			experimentalDesignMeasurementVariables = designImportService.getDesignRequiredMeasurementVariable(
					workbook, designImportData);
			
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
			workbook.setVariates(new ArrayList<>(uniqueVariates));

			userSelection.setExperimentalDesignVariables(new ArrayList<>(experimentalDesignMeasurementVariables));

			this.userSelection.setExperimentalDesignVariables(new ArrayList<>(experimentalDesignMeasurementVariables));
		
			ExpDesignParameterUi designParam = new ExpDesignParameterUi();
			designParam.setDesignType(3);
			this.userSelection.setExpDesignParams(designParam);
			
			List<Integer> expDesignTermIds = new ArrayList<>();
			expDesignTermIds.add(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
			this.userSelection.setExpDesignVariables(expDesignTermIds);
			
			// retrieve all trial level factors and convert them to setting details
			Set<MeasurementVariable> trialLevelFactors = new HashSet<>();
			for (MeasurementVariable factor : workbook.getFactors()) {
				if (PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().contains(factor.getLabel())) {

					// remove this variable in the selection
					addVariableInDeletedList(userSelection.getTrialLevelVariableList(), AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt(), factor.getTermId(),false);
					SettingsUtil.deleteVariableInSession(userSelection.getTrialLevelVariableList(),factor.getTermId());

					trialLevelFactors.add(factor);
				}
			}

			List<SettingDetail> newDetails = SettingsUtil.convertWorkbookFactorsToSettingDetails(new ArrayList<MeasurementVariable>(trialLevelFactors), fieldbookMiddlewareService);
			SettingsUtil.addNewSettingDetails(AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt(), newDetails, userSelection);

			resultsMap.put("isSuccess", 1);
			resultsMap.put("environmentData", userSelection.getTrialLevelVariableList());

		} catch (Exception e) {
			
			DesignImportController.LOG.error(e.getMessage(), e);
			
			resultsMap.put("isSuccess", 0);
			// error messages is still in .prop format,
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		return resultsMap;
	}
	
	private Map<String, Object> generateDatatableDataMap(MeasurementRow row, String suffix) {
    	Map<String, Object> dataMap = new HashMap<String, Object>();
		// the 4 attributes are needed always
		for (MeasurementData data : row.getDataList()) {
			String displayVal = data.getDisplayValue();
			if (suffix != null) {
				displayVal += suffix;
			}
			
			if (data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())
					|| data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())) {
				Object[] categArray = new Object[] {displayVal, data.isAccepted()};
				dataMap.put(data.getMeasurementVariable().getName(), categArray);
			} else {
				dataMap.put(data.getMeasurementVariable().getName(), displayVal);
			}
		}
		return dataMap;
    }
	
	public void initializeTemporaryWorkbook(String studyType) {
		
		List<SettingDetail> studyLevelConditions = this.userSelection.getStudyLevelConditions();
		List<SettingDetail> basicDetails = this.userSelection.getBasicDetails();
	         // transfer over data from user input into the list of setting details stored in the session
	    	 List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
	         combinedList.addAll(basicDetails);

	         if (studyLevelConditions != null) {             
	             combinedList.addAll(studyLevelConditions);
	         }

	         String name = "";

	        Workbook workbook = null;
	        StudyDetails details = new StudyDetails();
	        
		if ("T".equalsIgnoreCase(studyType)) {
	        	
			Dataset dataset =
					(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList,
							this.userSelection.getPlotsLevelList(), this.userSelection.getBaselineTraitsList(), this.userSelection,
							this.userSelection.getTrialLevelVariableList(), this.userSelection.getTreatmentFactors(), null, null,
							this.userSelection.getNurseryConditions(), false);

	        	workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false);
	        	
	        	details.setStudyType(StudyType.T);
	        	
		} else {
	        	
	        	List<SettingDetail> variatesList = new ArrayList<>();
	        	
			if (this.userSelection.getBaselineTraitsList() != null) {
				variatesList.addAll(this.userSelection.getBaselineTraitsList());
	        	}
	        
			if (this.userSelection.getSelectionVariates() != null) {
				variatesList.addAll(this.userSelection.getSelectionVariates());
	        	}
	
			Dataset dataset =
					(Dataset) SettingsUtil.convertPojoToXmlDataset(this.fieldbookMiddlewareService, name, combinedList,
							this.userSelection.getPlotsLevelList(), variatesList, this.userSelection,
							this.userSelection.getTrialLevelVariableList(), this.userSelection.getTreatmentFactors(), null, null,
							this.userSelection.getNurseryConditions(), true);

	        	workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, true);
	        	
	        	details.setStudyType(StudyType.N);
	        	
	        }
	        
	        workbook.setStudyDetails(details);
	        
		this.userSelection.setTemporaryWorkbook(workbook);
	        
	}
	
	protected void performAutomap(DesignImportData designImportData) throws MiddlewareQueryException {
		Map<PhenotypicType, List<DesignHeaderItem>> result =
				this.designImportService.categorizeHeadersByPhenotype(designImportData.getUnmappedHeaders());

		designImportData.setMappedHeaders(result);
		designImportData.setUnmappedHeaders(result.get(null));

	}
	
	protected void processEnvironmentData(EnvironmentData data) {
        for (int i = 0; i < data.getEnvironments().size(); i++) {
            Map<String, String> values = data.getEnvironments().get(i).getManagementDetailValues();
            if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))) {
                values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			} else if (values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null
					|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
                values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
            }
        }
    }
	
}
