/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.bean.*;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CreateTrialController.
 */
@Controller
@RequestMapping(CreateTrialController.URL)
public class CreateTrialController extends BaseTrialController {
    // TODO : rename and repurpose class to handle not just initial creation, but also editing

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CreateTrialController.class);

    /**
     * The Constant URL.
     */
    public static final String URL = "/TrialManager/createTrial";

    /**
     * The Constant URL_SETTINGS.
     */


    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "TrialManager/createTrial";
    }

    @ModelAttribute("operationMode")
    public String getOperationMode() {
        return "CREATE";
    }

    /**
     * Show.
     *
     * @param model   the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(Model model, HttpSession session) throws MiddlewareQueryException {


        SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});

        model.addAttribute("basicDetailsData", prepareBasicDetailsTabInfo());
        model.addAttribute("germplasmData", prepareGermplasmTabInfo());
        model.addAttribute("environmentData", prepareEnvironmentsTabInfo());
        model.addAttribute("trialSettingsData", prepareTrialSettingsTabInfo());

        return showAngularPage(model);
    }

    @ResponseBody
    @RequestMapping(value = "/useExistingTrial", method = RequestMethod.GET)
    public Map<String, TabInfo> getExistingTrialDetails(Model model, @RequestParam(value = "trialID") Integer trialID) throws MiddlewareQueryException{
        Map<String, TabInfo> tabDetails = new HashMap<String, TabInfo>();
        if (trialID != null && trialID != 0) {
            Workbook trialWorkbook = fieldbookMiddlewareService.getTrialDataSet(trialID);

            tabDetails.put("basicDetailsData", prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), true));
            tabDetails.put("germplasmData", prepareGermplasmTabInfo(trialWorkbook.getFactors(), true));
            tabDetails.put("environmentData", prepareEnvironmentsTabInfo(trialWorkbook, true));
            tabDetails.put("trialSettingsData", prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), true));
            tabDetails.put("measurementsData", prepareMeasurementsTabInfo(trialWorkbook.getVariates(), true));
        }

        return tabDetails;
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
    public String showTreatmentFactors(Model model, HttpSession session, HttpServletRequest req) {
        return showAjaxPage(model, URL_TREATMENT);
    }


    @RequestMapping(value = "/experimentalDesign", method = RequestMethod.GET)
    public String showExperimentalDesign(Model model) {
        return showAjaxPage(model, URL_EXPERIMENTAL_DESIGN);
    }

    @RequestMapping(value = "/measurements", method = RequestMethod.GET)
    public String showMeasurements(Model model) {
        return showAjaxPage(model, URL_MEASUREMENT);
    }


    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String submit(@RequestBody TrialData data) throws MiddlewareQueryException {
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

        String name = data.getBasicDetails().getBasicDetails().get(TermId.STUDY_NAME.getId());

        // TODO : integrate treatment factor detail once it's finalized

        Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, combinedList,
                userSelection.getPlotsLevelList(), userSelection.getBaselineTraitsList(), userSelection, userSelection.getTrialLevelVariableList(),
                userSelection.getTreatmentFactors(), null, null, userSelection.getNurseryConditions(), false);

        Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset, false);

        List<MeasurementVariable> variablesForEnvironment = new ArrayList<MeasurementVariable>();
        variablesForEnvironment.addAll(workbook.getTrialVariables());

        List<MeasurementRow> trialEnvironmentValues = WorkbookUtil.createMeasurementRowsFromEnvironments(data.getEnvironments().getEnvironments(), variablesForEnvironment) ;
        workbook.setTrialObservations(trialEnvironmentValues);

        createStudyDetails(workbook, data.getBasicDetails());

        // TODO : integration with experimental design here

        userSelection.setWorkbook(workbook);

        // TODO : clarify if the environment values placed in session also need to be updated to include the values for the trial level conditions
        userSelection.setTrialEnvironmentValues(convertToValueReference(data.getEnvironments().getEnvironments()));
        return "success";
    }

    protected void extractDataFromMetadata(List<SettingDetail> details, Map<Integer, String> values) {
        if (details == null || details.isEmpty()) {
            return;
        }

        for (SettingDetail detail : details) {
            if (! values.containsKey(detail.getVariable().getCvTermId()) && (detail.getValue() != null || detail.getValue().isEmpty())) {
                values.put(detail.getVariable().getCvTermId(), detail.getValue());
            }
        }
    }

    /*@ModelAttribute("experimentalDesignValues")*/
    public List<ValueReference> getExperimentalDesignValues() throws MiddlewareQueryException {
        return fieldbookService.getAllPossibleValues(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
    }

    protected TabInfo prepareGermplasmTabInfo() {
        List<SettingDetail> initialDetailList = new ArrayList<SettingDetail>();
        List<Integer> initialSettingIDs = buildVariableIDList(AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString());

        for (Integer initialSettingID : initialSettingIDs) {
            try {
                SettingDetail detail = createSettingDetail(initialSettingID, null);
                initialDetailList.add(detail);
            } catch (MiddlewareQueryException e) {
                e.printStackTrace();
            }

        }

        TabInfo info = new TabInfo();
        info.setSettings(initialDetailList);

        if (userSelection.getPlotsLevelList() == null) {
            userSelection.setPlotsLevelList(initialDetailList);
        }

        return info;
    }

    protected TabInfo prepareEnvironmentsTabInfo() throws MiddlewareQueryException{
        TabInfo info = new TabInfo();
        EnvironmentData data = new EnvironmentData();
        int noOfEnvironments = Integer.parseInt(AppConstants.DEFAULT_NO_OF_ENVIRONMENT_COUNT.getString());
        data.setNoOfEnvironments(noOfEnvironments);
        info.setData(data);

        for (int i = 0; i < noOfEnvironments; i++) {
            data.getEnvironments().add(new Environment());
        }

        Map<String, List<SettingDetail>> settingMap = new HashMap<String, List<SettingDetail>>();
        List<SettingDetail> managementDetailList = new ArrayList<SettingDetail>();
        List<Integer> hiddenFields = buildVariableIDList(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString());

        for (Integer id : buildVariableIDList(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString())) {
            SettingDetail detail = createSettingDetail(id, null);
            for (Integer hiddenField : hiddenFields) {
                if (id.equals(hiddenField)) {
                    detail.setHidden(true);
                }
            }

            managementDetailList.add(detail);
        }

        settingMap.put("managementDetails", managementDetailList);
        settingMap.put("trialConditionDetails", new ArrayList<SettingDetail>());

        if (userSelection.getTrialLevelVariableList() == null || userSelection.getBasicDetails().isEmpty()) {
            userSelection.setTrialLevelVariableList(managementDetailList);
        }

        info.setSettingMap(settingMap);
        return info;
    }

    protected TabInfo prepareBasicDetailsTabInfo() throws MiddlewareQueryException{
        Map<Integer, String> basicDetails = new HashMap<Integer, String>();
        List<SettingDetail> initialDetailList = new ArrayList<SettingDetail>();
        List<Integer> initialSettingIDs = buildVariableIDList(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());

        for (Integer initialSettingID : initialSettingIDs) {
            try {
                basicDetails.put(initialSettingID, "");
                SettingDetail detail = createSettingDetail(initialSettingID, null);
                initialDetailList.add(detail);
            } catch (MiddlewareQueryException e) {
                e.printStackTrace();
            }
        }

        BasicDetails basic = new BasicDetails();
        basic.setBasicDetails(basicDetails);

        basic.setFolderId(1);
        basic.setFolderName(AppConstants.PROGRAM_TRIALS.getString());
        basic.setFolderNameLabel(AppConstants.PROGRAM_TRIALS.getString());
        basic.setUserID(getCurrentIbdbUserId());
        basic.setUserName(fieldbookService.getPersonById(basic.getUserID()));

        TabInfo tab = new TabInfo();
        tab.setData(basic);

        if (userSelection.getBasicDetails() == null || userSelection.getBasicDetails().isEmpty()) {
            userSelection.setBasicDetails(initialDetailList);
        }

        return tab;
    }

    protected TabInfo prepareTrialSettingsTabInfo() {
        TabInfo info = new TabInfo();
        info.setSettings(new ArrayList<SettingDetail>());
        info.setData(new TrialSettingsBean());
        return info;
    }

    /**
         * Submit.
         *
         * @return the string
         * @throws MiddlewareQueryException the middleware query exception
         */
        /*
        @ResponseBody
        @RequestMapping(method = RequestMethod.POST)
        public String submit(@ModelAttribute("createTrialForm") CreateTrialForm form, Model model) throws MiddlewareQueryException {

        	String name = null;
        	for (SettingDetail nvar : form.getStudyLevelVariables()) {
        		if (nvar.getVariable() != null && nvar.getVariable().getCvTermId() != null && nvar.getVariable().getCvTermId().equals(TermId.STUDY_NAME.getId())) {
        			name = nvar.getValue();
        			break;
        		}
        	}

        	form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());
        	Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, form.getStudyLevelVariables(),
        			form.getPlotLevelVariables(), form.getBaselineTraitVariables(), userSelection, form.getTrialLevelVariables(), null, form.getTreatmentFactors());
        	Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset);
        	userSelection.setWorkbook(workbook);

        	if (form.getDesignLayout() != null && form.getDesignLayout().equals(AppConstants.DESIGN_LAYOUT_SAME_FOR_ALL.getString())
        			&& form.getExperimentalDesignForAll() != null) {

        		for (List<ValueReference> rowValues : form.getTrialEnvironmentValues()) {
        			for (ValueReference cellValue : rowValues) {
        				if (cellValue.getId().equals(TermId.EXPERIMENT_DESIGN_FACTOR.getId())) {
        					cellValue.setName(form.getExperimentalDesignForAll());
        				}
        			}
        		}
        	}

        	if (form.getTrialEnvironmentValues() != null && !form.getTrialEnvironmentValues().isEmpty()) {
        		userSelection.getWorkbook().setTrialObservations(WorkbookUtil.createMeasurementRows(form.getTrialEnvironmentValues(), workbook.getTrialVariables()));
        	}

         	userSelection.setTrialEnvironmentValues(form.getTrialEnvironmentValues());

        	createStudyDetails(workbook, form.getStudyLevelVariables(), form.getFolderId());

        	return "success";
        }



            @RequestMapping(value="/trial/{trialId}", method = RequestMethod.GET)
            public String useExistingTrial(@ModelAttribute("manageSettingsForm") CreateTrialForm form, @PathVariable int trialId
                    , Model model, HttpSession session) throws MiddlewareQueryException{
                if(trialId != 0){
                    Workbook workbook = null;

                    try {
                        workbook = fieldbookMiddlewareService.getTrialDataSet(trialId);
                    } catch (MiddlewareQueryException e) {
                        LOG.error(e.getMessage(), e);
                    }

                    userSelection.setWorkbook(workbook);
                    Dataset dataset = (Dataset)SettingsUtil.convertWorkbookToXmlDataset(workbook, false);
                    SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId(), true, true);

                    //study-level
                    List<SettingDetail> trialLevelConditions = updateRequiredFields(buildVariableIDList(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString()),
                            buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString(), true),
                            buildRequiredVariablesFlag(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString()),
                            userSelection.getStudyLevelConditions(), true, "");

                    //plot-level
                    List<SettingDetail> plotLevelConditions = updateRequiredFields(buildVariableIDList(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()),
                            buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false),
                            buildRequiredVariablesFlag(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()),
                            userSelection.getPlotsLevelList(), false, "");

                    //trial or study level variables
                    List<SettingDetail> trialLevelVariableList = sortDefaultTrialVariables(updateRequiredFields(buildVariableIDList(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()),
                            buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString(), true),
                            buildRequiredVariablesFlag(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()),
                            userSelection.getTrialLevelVariableList(), true, ""));

                    userSelection.setStudyLevelConditions(trialLevelConditions);
                    userSelection.setPlotsLevelList(plotLevelConditions);
                    userSelection.setTrialLevelVariableList(trialLevelVariableList);
                    form.setStudyLevelVariables(userSelection.getStudyLevelConditions());
                    form.setBaselineTraitVariables(userSelection.getBaselineTraitsList());
                    form.setPlotLevelVariables(userSelection.getPlotsLevelList());
                    form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());
                    form.setTreatmentFactors(convertSettingDetailToTreatment(userSelection.getTreatmentFactors()));

                    //build trial environment details
                    List<List<ValueReference>> trialEnvList = createTrialEnvValueList(userSelection.getTrialLevelVariableList(), 1, true);
                    form.setTrialEnvironmentValues(trialEnvList);
                    form.setTrialInstances(1);
                    form.setRequiredFields(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());
                }

                model.addAttribute("createTrialForm", form);
                model.addAttribute("settingsTrialList", getTrialSettingsList());
                model.addAttribute("trialList", getTrialList());
                model.addAttribute("experimentalDesignValues", getExperimentalDesignValues());
                return super.showAjaxPage(model, URL_SETTINGS);
            }

            private List<TreatmentFactorDetail> convertSettingDetailToTreatment(List<SettingDetail> treatmentFactors) {
                    List<TreatmentFactorDetail> newTreatmentFactors = new ArrayList<TreatmentFactorDetail>();
                    int index = 0;

                    for (SettingDetail settingDetail : treatmentFactors) {
                        if (index % 2 == 0) {
                            newTreatmentFactors.add(new TreatmentFactorDetail(settingDetail.getVariable().getCvTermId(),
                                    treatmentFactors.get(index + 1).getVariable().getCvTermId(), "1",
                                    treatmentFactors.get(index + 1).getValue(), settingDetail.getVariable().getName(),
                                    treatmentFactors.get(index + 1).getVariable().getName(),
                                    treatmentFactors.get(index + 1).getVariable().getDataTypeId(),
                                    treatmentFactors.get(index + 1).getPossibleValuesJson(),
                                    treatmentFactors.get(index + 1).getVariable().getMinRange(),
                                    treatmentFactors.get(index + 1).getVariable().getMaxRange()));
                            index++;
                        } else {
                            index++;
                            continue;
                        }
                    }

                    return newTreatmentFactors;
                }

                private List<List<ValueReference>> createTrialEnvValueList(List<SettingDetail> trialLevelVariableList, int trialInstances, boolean addDefault) {
                    List<List<ValueReference>> trialEnvValueList = new ArrayList<List<ValueReference>>();
                    for (int i=0; i<trialInstances; i++) {
                        List<ValueReference> trialInstanceVariables = new ArrayList<ValueReference>();
                        for (SettingDetail detail : trialLevelVariableList) {
                            if (detail.getVariable().getCvTermId() != null) {
                                //set value to empty except for trial instance no.
                                if (detail.getVariable().getCvTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
                                    trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), String.valueOf(i+1)));
                                } else {
                                    trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), ""));
                                }
                            } else {
                                trialInstanceVariables.add(new ValueReference(0, ""));
                            }
                        }
                        trialEnvValueList.add(trialInstanceVariables);
                    }
                    userSelection.setTrialEnvironmentValues(trialEnvValueList);
                    return trialEnvValueList;
                }

                private List<List<ValueReference>> createTrialEnvValueList(List<SettingDetail> trialLevelVariableList) {
                    List<List<ValueReference>> trialEnvValueList = new ArrayList<List<ValueReference>>();
                    List<MeasurementRow> trialObservations = userSelection.getWorkbook().getTrialObservations();

                    for (MeasurementRow trialObservation : trialObservations) {
                        List<ValueReference> trialInstanceVariables = new ArrayList<ValueReference>();
                        for (SettingDetail detail : trialLevelVariableList) {
                            String headerName = WorkbookUtil.getMeasurementVariableName(userSelection.getWorkbook().getTrialVariables(), detail.getVariable().getCvTermId());
                            String value = trialObservation.getMeasurementDataValue(headerName);
                            trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), value));
                        }
                        trialEnvValueList.add(trialInstanceVariables);
                    }
                    userSelection.setTrialEnvironmentValues(trialEnvValueList);
                    return trialEnvValueList;
                }

                private List<SettingDetail> sortDefaultTrialVariables(List<SettingDetail> trialLevelVariableList) {
                    //set orderBy
                    StringTokenizer tokenOrder = new StringTokenizer(AppConstants.TRIAL_ENVIRONMENT_ORDER.getString(), ",");
                    int i=0;
                    int tokenSize = tokenOrder.countTokens();
                    while (tokenOrder.hasMoreTokens()) {
                        String variableId = tokenOrder.nextToken();
                        for (SettingDetail settingDetail : trialLevelVariableList) {
                            if (settingDetail.getVariable().getCvTermId().equals(Integer.parseInt(variableId))) {
                                settingDetail.setOrder((tokenSize-i)*-1);
                            }
                        }
                        i++;
                    }

                    Collections.sort(trialLevelVariableList, new  Comparator<SettingDetail>() {
                        @Override
                        public int compare(SettingDetail o1, SettingDetail o2) {
                                return o1.getOrder() - o2.getOrder();
                        }
                    });

                    return trialLevelVariableList;
                }

                private List<ValueReference> getPossibleValuesOfDefaultVariable(String variableName) {
                    List<ValueReference> values = new ArrayList<ValueReference>();
                    variableName = variableName.toUpperCase().replace(" ", "_");

                    StringTokenizer token = new StringTokenizer(AppConstants.getString(variableName + AppConstants.VALUES.getString()), ",");

                    int i = 0;
                    while (token.hasMoreTokens()) {
                        values.add(new ValueReference(i, token.nextToken()));
                        i++;
                    }

                    return values;
                }
        */
}
