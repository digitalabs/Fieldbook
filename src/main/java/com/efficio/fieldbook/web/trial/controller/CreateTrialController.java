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
import com.efficio.fieldbook.web.common.bean.TreatmentFactorDetail;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.bean.*;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.ToolUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
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

import java.io.IOException;
import java.util.*;

/**
 * The Class CreateTrialController.
 */
@Controller
@RequestMapping(CreateTrialController.URL)
public class CreateTrialController extends SettingsController {
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
    public static final String URL_SETTINGS = "TrialManager/templates/trialSettings";
    public static final String URL_GERMPLASM = "TrialManager/templates/germplasmDetails";
    public static final String URL_ENVIRONMENTS = "TrialManager/templates/environments";
    public static final String URL_TREATMENT = "TrialManager/templates/treatment";
    public static final String URL_EXPERIMENTAL_DESIGN = "TrialManager/templates/experimentalDesign";
    public static final String URL_MEASUREMENT = "TrialManager/templates/measurements";

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "TrialManager/createTrial";
    }
    
    @RequestMapping(value="/open", method = RequestMethod.GET)
    public String show(Model model, HttpServletRequest req, HttpSession session) {
        SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME,SessionUtility.POSSIBLE_VALUES_SESSION_NAME});
        
        try {
            ToolUtil toolUtil = new ToolUtil();
            toolUtil.launchNativeTool(this.getOldFieldbookPath(), "--ibpApplication=IBFieldbookTools");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        
        return super.show(model);
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

    @ModelAttribute("programLocationURL")
    public String getProgramLocation() {
        return fieldbookProperties.getProgramLocationsUrl();
    }

    @ModelAttribute("projectID")
    public String getProgramID() {
        return getCurrentProjectId();
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
    public String showExperimentalDesign(Model model, HttpSession session, HttpServletRequest req) {
        return showAjaxPage(model, URL_EXPERIMENTAL_DESIGN);
    }

    @RequestMapping(value = "/measurements", method = RequestMethod.GET)
    public String showMeasurements(Model model, HttpSession session, HttpServletRequest req) {
        return showAjaxPage(model, URL_MEASUREMENT);
    }


    /**
     * Use existing Trial.
     *
     * @param form the form
     * @param trialId the Trial id
     * @param model the model
     * @param session the session
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
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
            List<SettingDetail> trialLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString()),
                    buildRequiredVariablesLabel(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString(), true),
                    buildRequiredVariablesFlag(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString()),
                    userSelection.getStudyLevelConditions(), true, "");

            //plot-level
            List<SettingDetail> plotLevelConditions = updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()),
                    buildRequiredVariablesLabel(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString(), false),
                    buildRequiredVariablesFlag(AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()),
                    userSelection.getPlotsLevelList(), false, "");

            //trial or study level variables
            List<SettingDetail> trialLevelVariableList = sortDefaultTrialVariables(updateRequiredFields(buildRequiredVariables(AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()),
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

    /**
     * Submit.
     *
     * @param form the form
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
    */

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String submit(@RequestBody TrialData data) throws MiddlewareQueryException {
        processEnvironmentData(data.getEnvironments());
        List<SettingDetail> studyLevelConditions = userSelection.getStudyLevelConditions();

        // transfer over data from user input into the list of setting details stored in the session
        populateSettingData(studyLevelConditions, data.getBasicDetails().getBasicDetails());
        populateSettingData(studyLevelConditions, data.getTrialSettings().getUserInput());

        String name = data.getBasicDetails().getBasicDetails().get(TermId.STUDY_NAME.getId());

        // TODO : integrate treatment factor detail once it's finalized

        Dataset dataset = (Dataset) SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, name, studyLevelConditions,
                userSelection.getPlotsLevelList(), userSelection.getBaselineTraitsList(), userSelection, userSelection.getTrialLevelVariableList(),
                userSelection.getTreatmentFactors(), null, null);

        // TODO : integrate trial level conditions in either dataset or workbook generation
        Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset);

        // TODO : optimize observation row generation by leveraging map instead of list of list array structure
        List<List<ValueReference>> trialEnvironmentValues = convertToValueReference(data.getEnvironments().getEnvironments());
        workbook.setTrialObservations(WorkbookUtil.createMeasurementRows(trialEnvironmentValues, workbook.getTrialVariables()));

        createStudyDetails(workbook, data.getBasicDetails());

        // TODO : integration with experimental design here

        userSelection.setWorkbook(workbook);
        userSelection.setTrialEnvironmentValues(trialEnvironmentValues);
        return "success";
    }

    protected List<List<ValueReference>> convertToValueReference(List<Environment> environments) {
        List<List<ValueReference>> returnVal = new ArrayList<List<ValueReference>>(environments.size());

        for (Environment environment : environments) {
            List<ValueReference> valueRefList = new ArrayList<ValueReference>();

            for (Map.Entry<Integer, String> entry : environment.getManagementDetailValues().entrySet()) {
                ValueReference valueRef = new ValueReference(entry.getKey(), entry.getValue());
                valueRefList.add(valueRef);
            }

            returnVal.add(valueRefList);
        }

        return returnVal;
    }

    protected void populateSettingData(List<SettingDetail> details, Map<Integer, String> values) {
        for (SettingDetail detail : details) {
            if (values.containsKey(detail.getVariable().getCvTermId())) {
                detail.setValue(values.get(detail.getVariable().getCvTermId()));
            }
        }
    }

    protected void processEnvironmentData(EnvironmentData data) {
        for (int i = 0; i < data.getEnvironments().size(); i++) {
            Map<Integer, String> values = data.getEnvironments().get(i).getManagementDetailValues();
            if (! values.containsKey(TermId.TRIAL_INSTANCE_FACTOR.getId())) {
                values.put(TermId.TRIAL_INSTANCE_FACTOR.getId(), Integer.toString(i + 1));
            }
        }
    }

    protected void createStudyDetails(Workbook workbook, BasicDetails detailBean) {
        if (workbook.getStudyDetails() == null) {
            workbook.setStudyDetails(new StudyDetails());
        }
        StudyDetails studyDetails = workbook.getStudyDetails();


        studyDetails.setTitle(detailBean.getBasicDetails().get(TermId.STUDY_TITLE.getId()));
        studyDetails.setObjective(detailBean.getBasicDetails().get(TermId.STUDY_OBJECTIVE.getId()));
        studyDetails.setStudyName(detailBean.getBasicDetails().get(TermId.STUDY_NAME.getId()));
        studyDetails.setStudyType(StudyType.T);

        if (detailBean.getFolderId() != null) {
            studyDetails.setParentFolderId(detailBean.getFolderId());
        }

        studyDetails.print(1);
    }

    /**
     * Creates the study details.
     *
     * @param workbook the workbook
     * @param conditions the conditions
     * @param folderId the folder id
     */
    private void createStudyDetails(Workbook workbook, List<SettingDetail> conditions, Integer folderId) {
        if (workbook.getStudyDetails() == null) {
            workbook.setStudyDetails(new StudyDetails());
        }
        StudyDetails studyDetails = workbook.getStudyDetails();

        if (conditions != null && !conditions.isEmpty()) {
	        studyDetails.setTitle(getSettingDetailValue(conditions, TermId.STUDY_TITLE.getId()));
	        studyDetails.setObjective(getSettingDetailValue(conditions, TermId.STUDY_OBJECTIVE.getId()));
	        studyDetails.setStudyName(getSettingDetailValue(conditions, TermId.STUDY_NAME.getId()));
	        studyDetails.setStudyType(StudyType.T);

	        if (folderId != null) {
	        	studyDetails.setParentFolderId(folderId);
	        }
    	}
        studyDetails.print(1);
    }

    /**
     * Gets the setting detail value.
     *
     * @param details the details
     * @param termId the term id
     * @return the setting detail value
     */
    private String getSettingDetailValue(List<SettingDetail> details, int termId) {
    	String value = null;

    	for (SettingDetail detail : details) {
    		if (detail.getVariable().getCvTermId().equals(termId)) {
    			value = detail.getValue();
    			break;
    		}
    	}

    	return value;
    }

    /*@ModelAttribute("experimentalDesignValues")*/
    public List<ValueReference> getExperimentalDesignValues() throws MiddlewareQueryException {
        return fieldbookService.getAllPossibleValues(TermId.EXPERIMENT_DESIGN_FACTOR.getId());
    }

    protected TabInfo prepareGermplasmTabInfo() {
        List<SettingDetail> initialDetailList = new ArrayList<SettingDetail>();
        List<Integer> initialSettingIDs = buildRequiredVariables(AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString());

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
        info.setData(new EnvironmentData());

        Map<String, List<SettingDetail>> settingMap = new HashMap<String, List<SettingDetail>>();
        settingMap.put("managementDetails", new ArrayList<SettingDetail>());
        settingMap.put("trialConditionDetails", new ArrayList<SettingDetail>());

        info.setSettingMap(settingMap);
        return info;
    }

    protected TabInfo prepareBasicDetailsTabInfo() {
        Map<Integer, String> basicDetails = new HashMap<Integer, String>();
        List<SettingDetail> initialDetailList = new ArrayList<SettingDetail>();
        List<Integer> initialSettingIDs = buildRequiredVariables(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());

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

        TabInfo tab = new TabInfo();
        tab.setData(basic);

        return tab;
    }

    protected TabInfo prepareTrialSettingsTabInfo() {
        TabInfo info = new TabInfo();
        info.setSettings(new ArrayList<SettingDetail>());

        return info;
    }
}
