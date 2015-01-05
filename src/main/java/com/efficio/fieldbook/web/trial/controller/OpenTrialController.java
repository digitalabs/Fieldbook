package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.bean.TrialData;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Controller
@RequestMapping(OpenTrialController.URL)
public class OpenTrialController extends
        BaseTrialController {

    private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
    private static final String TRIAL = "TRIAL";
	public static final String URL = "/TrialManager/openTrial";
    public static final String IS_EXP_DESIGN_PREVIEW = "isExpDesignPreview";
    public static final String MEASUREMENT_ROW_COUNT = "measurementRowCount";
    public static final String ENVIRONMENT_DATA_TAB = "environmentData";
    public static final String MEASUREMENT_DATA_EXISTING = "measurementDataExisting";
    private static final Logger LOG = LoggerFactory.getLogger(OpenTrialController.class);
    
    @Resource
    private OntologyService ontologyService;

    @Resource
    private ErrorHandlerService errorHandlerService;

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

    @ModelAttribute("programMethodURL")
    public String getProgramMethod() {
        return fieldbookProperties.getProgramBreedingMethodsUrl();
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

        Workbook workbook = userSelection.getWorkbook();
        Integer measurementDatasetId = null;
        if (workbook != null) {

            if (workbook.getMeasurementDatesetId() != null) {
                measurementDatasetId = workbook.getMeasurementDatesetId();
            }

            //this is so we can preview the exp design
            if (userSelection.getTemporaryWorkbook() != null) {
                workbook = userSelection.getTemporaryWorkbook();
                model.addAttribute(IS_EXP_DESIGN_PREVIEW, "0");
            }

            try {
                userSelection.setMeasurementRowList(workbook.getObservations());
                if (measurementDatasetId != null) {
                    form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(measurementDatasetId, SettingsUtil.buildVariates(workbook.getVariates())));
                } else {
                    form.setMeasurementDataExisting(false);
                }

                form.setMeasurementVariables(workbook.getMeasurementDatasetVariablesView());

                model.addAttribute(MEASUREMENT_ROW_COUNT, workbook.getObservations() != null ? workbook.getObservations().size() : 0);
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        return showAjaxPage(model, URL_MEASUREMENT);
    }

    @RequestMapping(value = "/{trialId}", method = RequestMethod.GET)
    public String openTrial(@ModelAttribute("createTrialForm") CreateTrialForm form, @PathVariable Integer trialId, Model model, HttpSession session, RedirectAttributes redirectAttributes) throws MiddlewareQueryException {
        clearSessionData(session);

        try {
            if (trialId != null && trialId != 0) {
                final Workbook trialWorkbook = fieldbookMiddlewareService.getTrialDataSet(trialId);

                userSelection.setConstantsWithLabels(trialWorkbook.getConstants());
                userSelection.setWorkbook(trialWorkbook);
                userSelection.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(trialWorkbook.getConditions()));
                userSelection.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(userSelection.getExperimentalDesignVariables()));
                userSelection.setTemporaryWorkbook(null);
                userSelection.setMeasurementRowList(trialWorkbook.getObservations());

                fieldbookMiddlewareService.setTreatmentFactorValues(trialWorkbook.getTreatmentFactors(), trialWorkbook.getMeasurementDatesetId());

                form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(trialWorkbook.getVariates())));
                form.setStudyId(trialId);

                this.setModelAttributes(form, trialId, model, trialWorkbook);
                this.setUserSelectionImportedGermplasmMainInfo(trialId, model);
            }
            return showAngularPage(model);

        } catch (MiddlewareQueryException e) {
            LOG.debug(e.getMessage(), e);

            redirectAttributes.addFlashAttribute("redirectErrorMessage", errorHandlerService.getErrorMessagesAsString(e.getCode(), new String[]{AppConstants.TRIAL.getString(), StringUtils.capitalize(AppConstants.TRIAL.getString()), AppConstants.TRIAL.getString()}, "\n"));
            return "redirect:" + ManageTrialController.URL;
        }
    }

    protected void setUserSelectionImportedGermplasmMainInfo(Integer trialId, Model model) throws MiddlewareQueryException {
        List<GermplasmList> germplasmLists = fieldbookMiddlewareService.getGermplasmListsByProjectId(Integer.valueOf(trialId), GermplasmListType.TRIAL);
        if (germplasmLists != null && !germplasmLists.isEmpty()) {
            GermplasmList germplasmList = germplasmLists.get(0);
            List<ListDataProject> data = fieldbookMiddlewareService.getListDataProject(germplasmList.getId());
            if (data != null && !data.isEmpty()) {
                model.addAttribute("germplasmListSize", data.size());
                List<ImportedGermplasm> list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(data);
                ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
                importedGermplasmList.setImportedGermplasms(list);
                ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
                mainInfo.setListId(germplasmList.getId());
                mainInfo.setAdvanceImportType(true);
                mainInfo.setImportedGermplasmList(importedGermplasmList);
                userSelection.setImportedGermplasmMainInfo(mainInfo);
                userSelection.setImportValid(true);
            }
        }
    }

    protected void setModelAttributes(CreateTrialForm form, Integer trialId, Model model, Workbook trialWorkbook) throws MiddlewareQueryException {
        model.addAttribute("basicDetailsData", prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false, trialId));
        model.addAttribute("germplasmData", prepareGermplasmTabInfo(trialWorkbook.getFactors(), false));
        model.addAttribute(ENVIRONMENT_DATA_TAB, prepareEnvironmentsTabInfo(trialWorkbook, false));
        model.addAttribute("trialSettingsData", prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
        model.addAttribute("measurementsData", prepareMeasurementsTabInfo(trialWorkbook.getVariates(), false));
        model.addAttribute("experimentalDesignData", prepareExperimentalDesignTabInfo(trialWorkbook.getExperimentalDesignVariables(), false));
        model.addAttribute(MEASUREMENT_DATA_EXISTING, fieldbookMiddlewareService.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
                SettingsUtil.buildVariates(trialWorkbook.getVariates())));
        model.addAttribute(MEASUREMENT_ROW_COUNT, trialWorkbook.getObservations().size());
        model.addAttribute("treatmentFactorsData", prepareTreatmentFactorsInfo(trialWorkbook.getTreatmentFactors(), false));

        //so that we can reuse the same age being use for nursery
        model.addAttribute("createNurseryForm", form);
        model.addAttribute("experimentalDesignSpecialData", prepareExperimentalDesignSpecialData());
        model.addAttribute("studyName", trialWorkbook.getStudyDetails().getLabel());

        model.addAttribute("germplasmListSize", 0);
    }

    protected void clearSessionData(HttpSession session) {
        SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
    }

    /**
     * @param data
     * @return
     * @throws MiddlewareQueryException
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public Map<String, Object> submit(@RequestParam("replace") int replace, @RequestBody TrialData data) throws MiddlewareQueryException {

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

        Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, combinedList,
                userSelection.getPlotsLevelList(), userSelection.getBaselineTraitsList(), userSelection, userSelection.getTrialLevelVariableList(),
                userSelection.getTreatmentFactors(), data.getTreatmentFactors().getCurrentData(), null, userSelection.getNurseryConditions(), false);

        SettingsUtil.setConstantLabels(dataset, userSelection.getConstantsWithLabels());

        Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false, userSelection.getExpDesignParams(), userSelection.getExpDesignVariables(), fieldbookMiddlewareService, userSelection.getExperimentalDesignVariables());

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

        List<MeasurementRow> trialEnvironmentValues = WorkbookUtil.createMeasurementRowsFromEnvironments(data.getEnvironments().getEnvironments(), variablesForEnvironment, userSelection.getExpDesignParams());
        workbook.setTrialObservations(trialEnvironmentValues);

        createStudyDetails(workbook, data.getBasicDetails());

        userSelection.setWorkbook(workbook);

        userSelection.setTrialEnvironmentValues(convertToValueReference(data.getEnvironments().getEnvironments()));


        Map<String, Object> returnVal = new HashMap<String, Object>();
        returnVal.put(ENVIRONMENT_DATA_TAB, prepareEnvironmentsTabInfo(workbook, false));
        returnVal.put(MEASUREMENT_DATA_EXISTING, false);
        returnVal.put(MEASUREMENT_ROW_COUNT, 0);

        //saving of measurement rows
        if (userSelection.getMeasurementRowList() != null && (!userSelection.getMeasurementRowList().isEmpty()) && replace == 0) {
            try {
                WorkbookUtil.addMeasurementDataToRows(workbook.getFactors(), false, userSelection, ontologyService, fieldbookService);
                WorkbookUtil.addMeasurementDataToRows(workbook.getVariates(), true, userSelection, ontologyService, fieldbookService);

                workbook.setMeasurementDatasetVariables(null);
                workbook.setObservations(userSelection.getMeasurementRowList());

                userSelection.setWorkbook(workbook);

                fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<SettingDetail>(), AppConstants.ID_NAME_COMBINATION.getString(), true);
                
                fieldbookMiddlewareService.saveMeasurementRows(workbook);

                returnVal.put(MEASUREMENT_DATA_EXISTING, fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(),
                        SettingsUtil.buildVariates(workbook.getVariates())));
                returnVal.put(MEASUREMENT_ROW_COUNT, workbook.getObservations().size());

                return returnVal;
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
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
        userSelection.setExperimentalDesignVariables(WorkbookUtil.getExperimentalDesignVariables(trialWorkbook.getConditions()));
        userSelection.setExpDesignParams(SettingsUtil.convertToExpDesignParamsUi(userSelection.getExperimentalDesignVariables()));
        returnVal.put(ENVIRONMENT_DATA_TAB, prepareEnvironmentsTabInfo(trialWorkbook, false));
        returnVal.put(MEASUREMENT_DATA_EXISTING, fieldbookMiddlewareService.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(),
                SettingsUtil.buildVariates(trialWorkbook.getVariates())));
        returnVal.put(MEASUREMENT_ROW_COUNT, trialWorkbook.getObservations().size());
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
    public List<UserDefinedField> getNameTypes() {
        try {
            return fieldbookMiddlewareService.getGermplasmNameTypes();
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        return new ArrayList<UserDefinedField>();
    }

    /**
     * Reset session variables after save.
     *
     * @param form  the form
     * @param model the model
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value = "/recreate/session/variables", method = RequestMethod.GET)
    public String resetSessionVariablesAfterSave(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
        Workbook workbook = userSelection.getWorkbook();
        form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));

        resetSessionVariablesAfterSave(workbook, false);
        return loadMeasurementDataPage(false, form, workbook, workbook.getMeasurementDatasetVariablesView(), model,"");
    }

    /**
     * Reset session variables after save.
     *
     * @param form  the form
     * @param model the model
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(value = "/load/measurement", method = RequestMethod.GET)
    public String loadMeasurement(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
        Workbook workbook = userSelection.getWorkbook();
        List<MeasurementVariable> measurementDatasetVariables = workbook.getMeasurementDatasetVariablesView();
        form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));
        return loadMeasurementDataPage(false, form, workbook, measurementDatasetVariables, model,"");
    }

    @RequestMapping(value = "/load/preview/measurement", method = RequestMethod.GET)
    public String loadPreviewMeasurement(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) throws MiddlewareQueryException {
        Workbook workbook = userSelection.getTemporaryWorkbook();
        Workbook originalWorkbook = userSelection.getWorkbook();
        userSelection.setMeasurementRowList(workbook.getObservations());  
        model.addAttribute(IS_EXP_DESIGN_PREVIEW, isPreviewEditable(originalWorkbook));
        return loadMeasurementDataPage(true, form, workbook, workbook.getMeasurementDatasetVariables(), model, "");
    }
    
    protected String isPreviewEditable(Workbook originalWorkbook){
    	String isPreviewEditable = "0";
        if(originalWorkbook == null || originalWorkbook.getStudyDetails() == null || originalWorkbook.getStudyDetails().getId() == null){
        	isPreviewEditable = "1";
        }
        return isPreviewEditable;
    }

    @RequestMapping(value = "/load/dynamic/change/measurement", method = RequestMethod.POST)
    public String loadDynamicChangeMeasurement(@ModelAttribute("createNurseryForm") CreateNurseryForm form,
                                               Model model, HttpServletRequest request) throws MiddlewareQueryException {
        boolean isInPreviewMode = false;
        Workbook workbook = userSelection.getWorkbook();
        if (userSelection.getTemporaryWorkbook() != null) {
            isInPreviewMode = true;
            workbook = userSelection.getTemporaryWorkbook();
        }
        
        List<MeasurementVariable> measurementDatasetVariables = new ArrayList<MeasurementVariable>();
        measurementDatasetVariables.addAll(workbook.getMeasurementDatasetVariablesView());
        //we show only traits that are being passed by the frontend
        String traitsListCsv = request.getParameter("traitsList");
        
        List<MeasurementVariable> newMeasurementDatasetVariables = new ArrayList<MeasurementVariable>();

        List<SettingDetail> traitList = userSelection.getBaselineTraitsList();

        if (!measurementDatasetVariables.isEmpty()) {
            for (MeasurementVariable var : measurementDatasetVariables) {
                if (var.isFactor()) {
                    newMeasurementDatasetVariables.add(var);
                }
            }
            if (traitsListCsv != null && !"".equalsIgnoreCase(traitsListCsv)) {
                StringTokenizer token = new StringTokenizer(traitsListCsv, ",");
                while (token.hasMoreTokens()) {
                    int id = Integer.valueOf(token.nextToken());
                    MeasurementVariable currentVar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, id);
                    if (currentVar == null) {
                        StandardVariable var = fieldbookMiddlewareService.getStandardVariable(id);
                        MeasurementVariable newVar = ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, fieldbookService);
                        newVar.setFactor(false);
                        newMeasurementDatasetVariables.add(newVar);
                        SettingsUtil.findAndUpdateVariableName(traitList, newVar);
                    } else {
                        newMeasurementDatasetVariables.add(currentVar);
                        SettingsUtil.findAndUpdateVariableName(traitList, currentVar);
                    }
                }
            }
            measurementDatasetVariables = newMeasurementDatasetVariables;
        }

        //we do a cleanup here
        if (isInPreviewMode) {
            model.addAttribute(IS_EXP_DESIGN_PREVIEW, "0");
        }

        return loadMeasurementDataPage(true, form, workbook, measurementDatasetVariables, model, request.getParameter("deletedEnvironment"));
    }

	private String loadMeasurementDataPage(boolean isTemporary, CreateNurseryForm form, Workbook workbook, 
    								List<MeasurementVariable> measurementDatasetVariables, Model model, 
    								String deletedEnvironments) throws MiddlewareQueryException {
    	
    	List<MeasurementRow> observations = workbook.getObservations();
    	Integer measurementDatasetId = workbook.getMeasurementDatesetId();
    	List<MeasurementVariable> variates = workbook.getVariates();
    	
        //set measurements data
        userSelection.setMeasurementRowList(observations);
        if (!isTemporary) {
            userSelection.setWorkbook(workbook);
        }
        if (measurementDatasetId != null) {
            form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(measurementDatasetId, SettingsUtil.buildVariates(variates)));
        } else {
            form.setMeasurementDataExisting(false);
        }
        //we do a matching of the name here so there won't be a problem in the data table
        if (observations != null && !observations.isEmpty()) {
            List<MeasurementData> dataList = observations.get(0).getDataList();
            for (MeasurementData data : dataList) {
                processMeasurementVariable(measurementDatasetVariables, data);
            }
            userSelection.setMeasurementRowList(observations);
        }
        //remove deleted environment from existing observation
        if(deletedEnvironments.length() > 0  && !"0".equals(deletedEnvironments)){
        	Workbook tempWorkbook = processDeletedEnvironments(deletedEnvironments, measurementDatasetVariables, workbook);
        	form.setMeasurementRowList(tempWorkbook.getObservations());
        	model.addAttribute(MEASUREMENT_ROW_COUNT, tempWorkbook.getObservations() != null ? tempWorkbook.getObservations().size() : 0);
        }
        
        form.setMeasurementVariables(measurementDatasetVariables); 
        userSelection.setMeasurementDatasetVariable(measurementDatasetVariables);
        model.addAttribute("createNurseryForm", form);
        return super.showAjaxPage(model, URL_DATATABLE);
    }

	private void processMeasurementVariable(
			List<MeasurementVariable> measurementDatasetVariables,
			MeasurementData data) {
		if (data.getMeasurementVariable() != null) {
		    MeasurementVariable var = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables, data.getMeasurementVariable().getTermId());
		    if (var != null && data.getMeasurementVariable().getName() != null) {
		        var.setName(data.getMeasurementVariable().getName());
		    }
		}
	}
	
	private Workbook processDeletedEnvironments(String deletedEnvironment, List<MeasurementVariable> measurementDatasetVariables, Workbook workbook) {
    	
		Workbook tempWorkbook = userSelection.getTemporaryWorkbook();
    	if(tempWorkbook == null){
    		tempWorkbook = generateTemporaryWorkbook();
    	}
		
    	List<MeasurementRow> filteredObservations = getFilteredObservations(userSelection.getMeasurementRowList(),deletedEnvironment);
    	List<MeasurementRow> filteredTrialObservations = getFilteredTrialObservations(workbook.getTrialObservations(),deletedEnvironment);
    	
    	tempWorkbook.setTrialObservations(filteredTrialObservations);
    	tempWorkbook.setObservations(filteredObservations);
    	tempWorkbook.setMeasurementDatasetVariables(measurementDatasetVariables);
    	
        userSelection.setTemporaryWorkbook(tempWorkbook);
    	userSelection.setMeasurementRowList(filteredObservations);
        userSelection.getWorkbook().setTrialObservations(filteredTrialObservations);
        userSelection.getWorkbook().setObservations(filteredObservations);
        userSelection.getWorkbook().setMeasurementDatasetVariables(measurementDatasetVariables);

		return tempWorkbook;
	}

	private Workbook generateTemporaryWorkbook() {
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

        Workbook tempWorkbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false);
        StudyDetails details = new StudyDetails();
        details.setStudyType(StudyType.T);
        tempWorkbook.setStudyDetails(details);
        
		return tempWorkbook;
	}

	protected List<MeasurementRow> getFilteredTrialObservations(
			List<MeasurementRow> trialObservations, String deletedEnvironment) {
		
		if("0".equalsIgnoreCase(deletedEnvironment) || "".equalsIgnoreCase(deletedEnvironment) || trialObservations == null){
			return trialObservations;
		}
		
		List<MeasurementRow> filteredTrialObservations = new ArrayList<MeasurementRow>();
		filteredTrialObservations.addAll(trialObservations);
		
		//remove the deleted trial instance
		for(MeasurementRow row : trialObservations){	
        	List<MeasurementData> dataList = row.getDataList();
        	for(MeasurementData data : dataList){
        		if(isATrialInstanceMeasurementVariable(data) && deletedEnvironment.equalsIgnoreCase(data.getValue())){
        			filteredTrialObservations.remove(row);
        			break;
        		}
        	}
        }
		
		filteredTrialObservations = updateTrialInstanceNoAfterDelete(deletedEnvironment,filteredTrialObservations);
		
		return filteredTrialObservations;
	}

	private boolean isATrialInstanceMeasurementVariable(MeasurementData data) {
		if (data.getMeasurementVariable() != null) {
            MeasurementVariable var = data.getMeasurementVariable();
            if (var != null && data.getMeasurementVariable().getName() != null
            		&& (TRIAL_INSTANCE.equalsIgnoreCase(var.getName()) || TRIAL.equalsIgnoreCase(var.getName()))){
            	return true;
            }
		}
		return false;
	}

	protected List<MeasurementRow> updateTrialInstanceNoAfterDelete(String deletedEnvironment,
			List<MeasurementRow> filteredMeasurementRowList) {
		
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		measurementRowList.addAll(filteredMeasurementRowList);
		
		for(MeasurementRow row : measurementRowList){	
        	List<MeasurementData> dataList = row.getDataList();
        	for(MeasurementData data : dataList){
        		if(isATrialInstanceMeasurementVariable(data)){
            		updateEnvironmentThatIsGreaterThanDeletedEnvironment(
							deletedEnvironment, data);
                	break;
        		}
        	}
        }
		
		return measurementRowList;
	}

	private void updateEnvironmentThatIsGreaterThanDeletedEnvironment(
			String deletedEnvironment, MeasurementData data) {
		Integer deletedInstanceNo = Integer.valueOf(deletedEnvironment);
		Integer currentInstanceNo = Integer.valueOf(data.getValue());
		
		if(deletedInstanceNo < currentInstanceNo){
			data.setValue(String.valueOf(--currentInstanceNo));
		}
	}

	protected List<MeasurementRow> getFilteredObservations(List<MeasurementRow> observations, String deletedEnvironment) {
		
		if("0".equalsIgnoreCase(deletedEnvironment) || "".equalsIgnoreCase(deletedEnvironment)){
			return observations;
		}
		
        List<MeasurementRow> filteredObservations = new ArrayList<MeasurementRow>();
        for(MeasurementRow row : observations){	
        	List<MeasurementData> dataList = row.getDataList();
        	for(MeasurementData data : dataList){
        		if(isATrialInstanceMeasurementVariable(data) && 
        				!deletedEnvironment.equalsIgnoreCase(data.getValue()) 
        				&& !"0".equalsIgnoreCase(data.getValue()) ){
        			filteredObservations.add(row);
                	break;
        		}
        	}
        }
        
        filteredObservations = updateTrialInstanceNoAfterDelete(deletedEnvironment, filteredObservations);

		return filteredObservations;
	}
}
