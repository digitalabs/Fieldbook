package com.efficio.fieldbook.web.nursery.controller;

/**
 * Created by cyrus on 5/8/15.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
		return String.format("%s/designImportMain",URL);
	}


	@ResponseBody
	@RequestMapping(value = "/import", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public Map<String, Object> importFile(Model model,
			@ModelAttribute("importDesignForm") ImportDesignForm form) {

		Map<String, Object> resultsMap = new HashMap<>();
		
		try {
			
			initializeTemporaryWorkbook();
			
			Workbook workbook = userSelection.getTemporaryWorkbook();
			
			DesignImportData designImportData = parser.parseFile(form.getFile());
			
			createTestMapping(designImportData);
			
			userSelection.setDesignImportData(designImportData);
			
			designImportService.validateDesignData(designImportData);

			if (!designImportService.areTrialInstancesMatchTheSelectedEnvironments(workbook, designImportData)){
				resultsMap.put("warning", messageSource.getMessage("design.import.warning.trial.instances.donotmatch", null, Locale.ENGLISH));
			}
			
			resultsMap.put("isSuccess", 1);
			
		} catch (Exception e) {
			
			LOG.error(e.getMessage(), e);
			
			resultsMap.put("isSuccess", 0);
			// error messages is still in .prop format,
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		return resultsMap;
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
	@RequestMapping(value = "/showDetails/data", method = RequestMethod.GET)
	public List<Map<String, Object>> showDetailsData(Model model,
			@ModelAttribute("importDesignForm") ImportDesignForm form) {
			
		
		Workbook workbook = userSelection.getTemporaryWorkbook();
		DesignImportData designImportData = userSelection.getDesignImportData();
		
			List<MeasurementRow> measurementRows = new ArrayList<>();
			
			try {
				measurementRows = designImportService.generateDesign(workbook, designImportData);
			} catch (DesignValidationException e) {
				e.printStackTrace();
			}
	    	
	    	List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();
	    	
	    	for(MeasurementRow row : measurementRows){
	    		    		
	    		Map<String, Object> dataMap = generateDatatableDataMap(row, null);
	    		
	    		masterList.add(dataMap);
	    	}
	   	
	    	return masterList;
		
	}
	
	@ResponseBody
	@RequestMapping(value = "/generate", produces = "application/json; charset=utf-8")
	public Map<String, Object> showMeasurements() {

		Map<String, Object> resultsMap = new HashMap<>();
		
		try {
			
			Workbook workbook = userSelection.getTemporaryWorkbook();
			DesignImportData designImportData = userSelection.getDesignImportData();
			
			List<MeasurementRow> measurementRows;
			Set<MeasurementVariable> measurementVariables;
			
			measurementRows = designImportService.generateDesign(workbook, designImportData);
			measurementVariables = designImportService.getDesignMeasurementVariables(workbook, designImportData);
			
			workbook.setObservations(measurementRows);
			workbook.setMeasurementDatasetVariables(new ArrayList<MeasurementVariable>(measurementVariables));
			
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
	
	public void initializeTemporaryWorkbook(){
		
	      List<SettingDetail> studyLevelConditions = userSelection.getStudyLevelConditions();
	         List<SettingDetail> basicDetails = userSelection.getBasicDetails();
	         // transfer over data from user input into the list of setting details stored in the session
	    	 List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
	         combinedList.addAll(basicDetails);

	         if (studyLevelConditions != null) {             
	             combinedList.addAll(studyLevelConditions);
	         }

	         String name = "";

	         
	    	Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, combinedList,
	    			userSelection.getPlotsLevelList(), userSelection.getBaselineTraitsList(), userSelection, userSelection.getTrialLevelVariableList(),
	    			userSelection.getTreatmentFactors(), null, null, userSelection.getNurseryConditions(), false);

	        Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false);
	        StudyDetails details = new StudyDetails();
	        details.setStudyType(StudyType.T);
	        workbook.setStudyDetails(details);
	        
	        userSelection.setTemporaryWorkbook(workbook);
	        
	}
	
	protected void createTestMapping(DesignImportData designImportData) throws MiddlewareQueryException{
		
		List<DesignHeaderItem> trialEnv = new ArrayList<>();
		List<DesignHeaderItem> germplasm = new ArrayList<>();
		List<DesignHeaderItem> design = new ArrayList<>();
		List<DesignHeaderItem> variate = new ArrayList<>();
		
		for (DesignHeaderItem item : designImportData.getUnmappedHeaders()){
			StandardVariable stdVar = fieldbookMiddlewareService.getStandardVariableByName(item.getHeaderName());
			item.setVariable(stdVar);
			
			if (stdVar.getPhenotypicType() == PhenotypicType.TRIAL_ENVIRONMENT){
				trialEnv.add(item);
			}
			if (stdVar.getPhenotypicType() == PhenotypicType.GERMPLASM){
				germplasm.add(item);
			}
			if (stdVar.getPhenotypicType() == PhenotypicType.TRIAL_DESIGN){
				design.add(item);
			}
			if (stdVar.getPhenotypicType() == PhenotypicType.VARIATE){
				variate.add(item);
			}
		}
		
		designImportData.getMappedHeaders().put(PhenotypicType.TRIAL_ENVIRONMENT, trialEnv);
		designImportData.getMappedHeaders().put(PhenotypicType.GERMPLASM, germplasm);
		designImportData.getMappedHeaders().put(PhenotypicType.TRIAL_DESIGN, design);
		designImportData.getMappedHeaders().put(PhenotypicType.VARIATE, variate);
		
	}
}
