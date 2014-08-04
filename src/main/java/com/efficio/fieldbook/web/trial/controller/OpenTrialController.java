package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.TrialData;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Controller
@RequestMapping(OpenTrialController.URL)
public class OpenTrialController extends
        BaseTrialController {

    private static final Logger LOG = LoggerFactory.getLogger(OpenTrialController.class);
    public static final String URL = "/TrialManager/openTrial";
    
    @Resource
    private OntologyService ontologyService;

    @Override
    public String getContentName() {
        return "TrialManager/createTrial";
    }

    @ModelAttribute("programLocationURL")
    public String getProgramLocation() {
        return fieldbookProperties.getProgramLocationsUrl();
    }

    @ModelAttribute("projectID")
    public String getProgramID() {
        return getCurrentProjectId();
    }

    @ModelAttribute("trialEnvironmentHiddenFields")
    public List<Integer> getTrialEnvironmentHiddenFields() {
        return buildVariableIDList(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString());
    }

    @ModelAttribute("operationMode")
    public String getOperationMode() {
        return "OPEN";
    }      

    @RequestMapping(value = "/trialSettings", method = RequestMethod.GET)
    public String showCreateTrial(Model model) {
        return showAjaxPage(model, URL_SETTINGS);
    }

    @RequestMapping(value = "/environment", method = RequestMethod.GET)
    public String showEnvironments(Model model) {
        return showAjaxPage(model, URL_ENVIRONMENTS);
    }


    @RequestMapping(value = "/germplasm", method = RequestMethod.GET)
    public String showGermplasm(Model model, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form) {
    	try {
    		List<GermplasmList> germplasmListTrial = fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(getCurrentProjectId()), GermplasmListType.TRIAL);
	        if(germplasmListTrial != null && !germplasmListTrial.isEmpty()){
	        	GermplasmList trialList = germplasmListTrial.get(0);        	
				fieldbookMiddlewareService.getSnapshot(trialList.getId());		
	        }
    	} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return showAjaxPage(model, URL_GERMPLASM);
    }

    @RequestMapping(value = "/treatment", method = RequestMethod.GET)
    public String showTreatmentFactors(Model model) {
        return showAjaxPage(model, URL_TREATMENT);
    }


    @RequestMapping(value = "/experimentalDesign", method = RequestMethod.GET)
    public String showExperimentalDesign(Model model) {
        return showAjaxPage(model, URL_EXPERIMENTAL_DESIGN);
    }

    @RequestMapping(value = "/measurements", method = RequestMethod.GET)
    public String showMeasurements(@ModelAttribute("createTrialForm") CreateTrialForm form, Model model) {
    	
    	// TODO : integrate loading of data for
	    Workbook workbook = userSelection.getWorkbook();
	    Integer measurementDatasetId = null;
        if (workbook != null) {
        	
        	if(workbook.getMeasurementDatesetId() != null){
        		measurementDatasetId = workbook.getMeasurementDatesetId(); 
        	}
        	
        	//this is so we can preview the exp design
        	if(userSelection.getTemporaryWorkbook() != null){
        		workbook = userSelection.getTemporaryWorkbook();
        		model.addAttribute("isExpDesignPreview", "1");
        	}
        	
            try {
				//SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), false, ontologyService);
				userSelection.setMeasurementRowList(workbook.getObservations());
				if(measurementDatasetId != null){
					form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(measurementDatasetId, SettingsUtil.buildVariates(workbook.getVariates())));
				}else{
					form.setMeasurementDataExisting(false);
				}
				
	            form.setMeasurementVariables(workbook.getMeasurementDatasetVariablesView());
	            
	            model.addAttribute("measurementRowCount", workbook.getObservations() != null ? workbook.getObservations().size() : 0);
			} catch (MiddlewareQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}           
        }
        
        return showAjaxPage(model, URL_MEASUREMENT);
    }

    @RequestMapping(value = "/{trialId}", method = RequestMethod.GET)
    public String openTrial(@ModelAttribute("createTrialForm") CreateTrialForm form, Model model, HttpSession session, @PathVariable Integer trialId) throws MiddlewareQueryException {
        SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});

        if (trialId != null && trialId != 0) {

            Workbook trialWorkbook = fieldbookMiddlewareService.getTrialDataSet(trialId);
            userSelection.setWorkbook(trialWorkbook);
            userSelection.setTemporaryWorkbook(null);
            model.addAttribute("basicDetailsData", prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false, trialId));
            model.addAttribute("germplasmData", prepareGermplasmTabInfo(trialWorkbook.getFactors(), false));
            model.addAttribute("environmentData", prepareEnvironmentsTabInfo(trialWorkbook, false));
            model.addAttribute("trialSettingsData", prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
            model.addAttribute("measurementsData", prepareMeasurementsTabInfo(trialWorkbook.getVariates(), false));
            model.addAttribute("measurementDataExisting", fieldbookMiddlewareService.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
                    SettingsUtil.buildVariates(trialWorkbook.getVariates())));
            model.addAttribute("measurementRowCount", trialWorkbook.getObservations().size());
            fieldbookMiddlewareService.setTreatmentFactorValues(trialWorkbook.getTreatmentFactors(), trialWorkbook.getMeasurementDatesetId());
            model.addAttribute("treatmentFactorsData", prepareTreatmentFactorsInfo(trialWorkbook.getTreatmentFactors(), false));
            userSelection.setMeasurementRowList(trialWorkbook.getObservations());
            form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(trialWorkbook.getVariates())));
            form.setStudyId(trialId);
            model.addAttribute("createNurseryForm", form); //so that we can reuse the same age being use for nursery
            model.addAttribute("experimentalDesignData", prepareExpDesignTabInfo());
            model.addAttribute("studyName", trialWorkbook.getStudyDetails().getLabel());
        }


        return showAngularPage(model);
    }

    /**
     *
     * @param data
     * @return
     * @throws MiddlewareQueryException
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public Map<String, Object> submit(@RequestBody TrialData data) throws MiddlewareQueryException {
        
        processEnvironmentData(data.getEnvironments());
        List<SettingDetail> studyLevelConditions = userSelection.getStudyLevelConditions();
        List<SettingDetail> basicDetails = userSelection.getBasicDetails();
        // transfer over data from user input into the list of setting details stored in the session
        populateSettingData(basicDetails, data.getBasicDetails().getBasicDetails());

        List<SettingDetail> combinedList = new ArrayList<SettingDetail>();
        combinedList.addAll(basicDetails);

        if (studyLevelConditions != null) {
            populateSettingData(studyLevelConditions, data.getTrialSettings().getUserInput());
            combinedList.addAll(studyLevelConditions);
        }
        
        if (userSelection.getPlotsLevelList() == null) {
            userSelection.setPlotsLevelList(new ArrayList<SettingDetail>());
        }
        if (userSelection.getBaselineTraitsList() == null) {
            userSelection.setBaselineTraitsList(new ArrayList<SettingDetail>());
        }
        if (userSelection.getNurseryConditions() == null) {
            userSelection.setNurseryConditions(new ArrayList<SettingDetail>());
        }
        if (userSelection.getTrialLevelVariableList() == null) {
            userSelection.setTrialLevelVariableList(new ArrayList<SettingDetail>());
        }
        if (userSelection.getTreatmentFactors() == null) {
            userSelection.setTreatmentFactors(new ArrayList<SettingDetail>());
        }
        
        //include deleted list if measurements are available
        SettingsUtil.addDeletedSettingsList(combinedList, userSelection.getDeletedStudyLevelConditions(), 
            userSelection.getStudyLevelConditions());
        SettingsUtil.addDeletedSettingsList(null, userSelection.getDeletedPlotLevelList(), 
            userSelection.getPlotsLevelList());
        SettingsUtil.addDeletedSettingsList(null, userSelection.getDeletedBaselineTraitsList(), 
            userSelection.getBaselineTraitsList());
        SettingsUtil.addDeletedSettingsList(null, userSelection.getDeletedNurseryConditions(), 
            userSelection.getNurseryConditions());
        SettingsUtil.addDeletedSettingsList(null, userSelection.getDeletedTrialLevelVariables(), 
            userSelection.getTrialLevelVariableList());
        SettingsUtil.addDeletedSettingsList(null, userSelection.getDeletedTreatmentFactors(), userSelection.getTreatmentFactors());

        String name = data.getBasicDetails().getBasicDetails().get(TermId.STUDY_NAME.getId());
        
        //retain measurement dataset id and trial dataset id
        int trialDatasetId = userSelection.getWorkbook().getTrialDatasetId();
        int measurementDatasetId = userSelection.getWorkbook().getMeasurementDatesetId();

        // TODO : integrate treatment factor detail once it's finalized

        Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, combinedList,
                userSelection.getPlotsLevelList(), userSelection.getBaselineTraitsList(), userSelection, userSelection.getTrialLevelVariableList(),
                userSelection.getTreatmentFactors(), data.getTreatmentFactors().getCurrentData(), null, userSelection.getNurseryConditions(), false);

        Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false);
        
        if (userSelection.getTemporaryWorkbook() != null) {
            userSelection.setMeasurementRowList(null);
            userSelection.getWorkbook().setOriginalObservations(null);
            userSelection.getWorkbook().setObservations(null);
        }
        
        workbook.setOriginalObservations(userSelection.getWorkbook().getOriginalObservations());
        workbook.setTrialObservations(userSelection.getWorkbook().getTrialObservations());
        workbook.setTrialDatasetId(trialDatasetId);
        workbook.setMeasurementDatesetId(measurementDatasetId);

        List<MeasurementVariable> variablesForEnvironment = new ArrayList<MeasurementVariable>();
        variablesForEnvironment.addAll(workbook.getTrialVariables());

        List<MeasurementRow> trialEnvironmentValues = WorkbookUtil.createMeasurementRowsFromEnvironments(data.getEnvironments().getEnvironments(), variablesForEnvironment) ;
        workbook.setTrialObservations(trialEnvironmentValues);

        createStudyDetails(workbook, data.getBasicDetails());

        userSelection.setWorkbook(workbook);

        // TODO : clarify if the environment values placed in session also need to be updated to include the values for the trial level conditions
        userSelection.setTrialEnvironmentValues(convertToValueReference(data.getEnvironments().getEnvironments()));


        Map<String, Object> returnVal = new HashMap<String, Object>();
        returnVal.put("environmentData", prepareEnvironmentsTabInfo(workbook, false));
        returnVal.put("measurementDataExisting", false);
        returnVal.put("measurementRowCount", 0);
        //saving of measurement rows
        if (userSelection.getMeasurementRowList() != null && userSelection.getMeasurementRowList().size() > 0) {
            try {                                
                WorkbookUtil.addMeasurementDataToRows(workbook.getFactors(), false, userSelection, ontologyService, fieldbookService);
                WorkbookUtil.addMeasurementDataToRows(workbook.getVariates(), true, userSelection, ontologyService, fieldbookService);
                
                workbook.setMeasurementDatasetVariables(null);
                workbook.setObservations(userSelection.getMeasurementRowList());
                
                userSelection.setWorkbook(workbook);
                
                fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), userSelection.getRemovedConditions(), AppConstants.ID_NAME_COMBINATION.getString(), true);
                fieldbookMiddlewareService.saveMeasurementRows(workbook);

                returnVal.put("measurementDataExisting", fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(),
                        SettingsUtil.buildVariates(workbook.getVariates())));
                returnVal.put("measurementRowCount", workbook.getObservations().size());

                return returnVal;
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage());
                return new HashMap<String, Object>();
            }
        } else {
            return returnVal;
        }
    }


    @ResponseBody
    @RequestMapping(value = "/updateSavedTrial", method = RequestMethod.GET)
    public Map<String, Object> updateSavedTrial(@RequestParam(value = "trialID") int id) throws MiddlewareQueryException {
        Map<String, Object> returnVal = new HashMap<String, Object>();
        Workbook trialWorkbook = fieldbookMiddlewareService.getTrialDataSet(id);
        userSelection.setWorkbook(trialWorkbook);
        returnVal.put("environmentData", prepareEnvironmentsTabInfo(trialWorkbook, false));
        returnVal.put("measurementDataExisting", fieldbookMiddlewareService.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
                            SettingsUtil.buildVariates(trialWorkbook.getVariates())));
        returnVal.put("measurementRowCount", trialWorkbook.getObservations().size());
        returnVal.put("measurementsData", prepareMeasurementsTabInfo(trialWorkbook.getVariates(), false));
        prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false, id);
        prepareGermplasmTabInfo(trialWorkbook.getFactors(), false);
        prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false);



        return returnVal;
    }

    @ResponseBody
    @RequestMapping(value = "/retrieveVariablePairs/{id}", method = RequestMethod.GET)
    public List<SettingDetail> retrieveVariablePairs(@PathVariable int id) {
        return super.retrieveVariablePairs(id);
    }

    @ModelAttribute("nameTypes")
    public List<UserDefinedField> getNameTypes(){
        try {
            return fieldbookMiddlewareService.getGermplasmNameTypes();
        }catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }
    
    /**
     * Reset session variables after save.
     *
     * @param form the form
     * @param model the model
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/recreate/session/variables", method = RequestMethod.GET)
    public String resetSessionVariablesAfterSave(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException{
        Workbook workbook = userSelection.getWorkbook();
        form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));
        
        resetSessionVariablesAfterSave(workbook, false);
        return loadMeasurementDataPage(false, form, workbook,workbook.getMeasurementDatasetVariablesView(), workbook.getObservations(), workbook.getMeasurementDatesetId(), workbook.getVariates(), model);
    }

    /**
     * Reset session variables after save.
     *
     * @param form the form
     * @param model the model
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value="/load/measurement", method = RequestMethod.GET)
    public String loadMeasurement(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException{
        Workbook workbook = userSelection.getWorkbook();
        List<MeasurementVariable> variates = workbook.getVariates();
        List<MeasurementVariable> measurementDatasetVariables = workbook.getMeasurementDatasetVariablesView();
        List<MeasurementRow> observations = workbook.getObservations();
        Integer measurementDatasetId = workbook.getMeasurementDatesetId();        
        form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(measurementDatasetId, SettingsUtil.buildVariates(variates)));        
        return loadMeasurementDataPage(false, form, workbook, measurementDatasetVariables, observations,measurementDatasetId, variates, model);
    }
    
    @RequestMapping(value="/load/preview/measurement", method = RequestMethod.GET)
    public String loadPreviewMeasurement(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException{
        Workbook workbook = userSelection.getTemporaryWorkbook();
        List<MeasurementVariable> variates = workbook.getVariates();
        List<MeasurementVariable> measurementDatasetVariables = workbook.getMeasurementDatasetVariables();        
        List<MeasurementRow> observations = workbook.getObservations();
        Integer measurementDatasetId = workbook.getMeasurementDatesetId();
        userSelection.setMeasurementRowList(workbook.getObservations());
        model.addAttribute("isExpDesignPreview", "1");
        return loadMeasurementDataPage(true, form, workbook, measurementDatasetVariables, observations,measurementDatasetId, variates, model);
    }
    @RequestMapping(value="/load/dynamic/change/measurement", method = RequestMethod.POST)
    public String loadDynamicChangeMeasurement(@ModelAttribute("createNurseryForm") CreateNurseryForm form, 
    		Model model, HttpServletRequest request) throws MiddlewareQueryException{
    	boolean isInPreviewMode = false;
        Workbook workbook = userSelection.getWorkbook();
        if(userSelection.getTemporaryWorkbook() != null){
        	isInPreviewMode = true;
        	workbook = userSelection.getTemporaryWorkbook();
        }
        List<MeasurementVariable> variates = workbook.getVariates();
        List<MeasurementVariable> measurementDatasetVariables = new ArrayList<MeasurementVariable>();
        measurementDatasetVariables.addAll(workbook.getMeasurementDatasetVariables());  
        //we show only traits that are being passed by the frontend
        String traitsListCsv = request.getParameter("traitsList");
    	List<MeasurementVariable> newMeasurementDatasetVariables = new ArrayList<MeasurementVariable>();
    	    		    		
    		if(!measurementDatasetVariables.isEmpty()){
				for(MeasurementVariable var : measurementDatasetVariables){
					if(var.isFactor()){
						newMeasurementDatasetVariables.add(var);
					}
				}
				if(traitsListCsv != null && !"".equalsIgnoreCase(traitsListCsv)){
					StringTokenizer token = new StringTokenizer(traitsListCsv, ",");        		
		    		while(token.hasMoreTokens()){
		    			int id = Integer.valueOf(token.nextToken());
		    			MeasurementVariable currentVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, id);
		    			if(currentVar == null){
			    			StandardVariable var = fieldbookMiddlewareService.getStandardVariable(id);
			    			MeasurementVariable newVar = ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD);
			    			newVar.setFactor(false);
			    			newMeasurementDatasetVariables.add(newVar);
		    			}else{
		    				newMeasurementDatasetVariables.add(currentVar);
		    			}
		    		}
				}
	    		measurementDatasetVariables = newMeasurementDatasetVariables;
    		}
    	
        	
        
        List<MeasurementRow> observations = workbook.getObservations();
        Integer measurementDatasetId = workbook.getMeasurementDatesetId();
        userSelection.setMeasurementRowList(workbook.getObservations());
        if(isInPreviewMode){
        	model.addAttribute("isExpDesignPreview", "1");
        }
        
        return loadMeasurementDataPage(true, form, workbook, measurementDatasetVariables, observations,measurementDatasetId, variates, model);
    }
    
    
    private String loadMeasurementDataPage(boolean isTemporary, CreateNurseryForm form, Workbook workbook, List<MeasurementVariable> measurementDatasetVariables, List<MeasurementRow> observations, Integer measurementDatasetId,  List<MeasurementVariable> variates, Model model) throws MiddlewareQueryException{
    	 //set measurements data
        userSelection.setMeasurementRowList(observations);
        if(!isTemporary){
        	userSelection.setWorkbook(workbook);
        }
        if(measurementDatasetId != null){
        	form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(measurementDatasetId, SettingsUtil.buildVariates(variates)));
        }else{
        	form.setMeasurementDataExisting(false);
        }
        //we do a matching of the name here so there won't be a problem in the data table
        if(observations != null && !observations.isEmpty()){
        	List<MeasurementData> dataList =  observations.get(0).getDataList();
        	for(MeasurementData data : dataList){
        		if(data.getMeasurementVariable() != null){
        			MeasurementVariable var = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, data.getMeasurementVariable().getTermId());
        			if(var != null && data.getMeasurementVariable().getName() != null){
        				var.setName(data.getMeasurementVariable().getName());
        			}
        		}
        	}
        }
        
        
        form.setMeasurementVariables(measurementDatasetVariables);     
        userSelection.setMeasurementDatasetVariable(measurementDatasetVariables);
        
        model.addAttribute("createNurseryForm", form);
        
        return super.showAjaxPage(model, URL_DATATABLE);
    }
}
