package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.trial.bean.*;
import com.efficio.fieldbook.web.util.AppConstants;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.*;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public abstract class BaseTrialController extends SettingsController {
    public static final String URL_SETTINGS = "TrialManager/templates/trialSettings";
    public static final String URL_GERMPLASM = "TrialManager/templates/germplasmDetails";
    public static final String URL_ENVIRONMENTS = "TrialManager/templates/environments";
    public static final String URL_TREATMENT = "TrialManager/templates/treatment";
    public static final String URL_EXPERIMENTAL_DESIGN = "TrialManager/templates/experimentalDesign";
    public static final String URL_MEASUREMENT = "TrialManager/templates/measurements";

    /** The ontology service. */
    @Resource
    protected OntologyService ontologyService;

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

    protected void processEnvironmentData(EnvironmentData data) {
        for (int i = 0; i < data.getEnvironments().size(); i++) {
            Map<Integer, String> values = data.getEnvironments().get(i).getManagementDetailValues();
            if (!values.containsKey(TermId.TRIAL_INSTANCE_FACTOR.getId())) {
                values.put(TermId.TRIAL_INSTANCE_FACTOR.getId(), Integer.toString(i + 1));
            } else if (values.get(TermId.TRIAL_INSTANCE_FACTOR.getId()) == null || values.get(TermId.TRIAL_INSTANCE_FACTOR).isEmpty()) {
                values.put(TermId.TRIAL_INSTANCE_FACTOR.getId(), Integer.toString(i + 1));
            }
        }
    }

    protected void populateSettingData(List<SettingDetail> details, Map<Integer, String> values) {
        if (details == null || details.isEmpty()) {
            return;
        }

        for (SettingDetail detail : details) {
            if (values.containsKey(detail.getVariable().getCvTermId())) {
                detail.setValue(values.get(detail.getVariable().getCvTermId()));
            }
        }
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

    protected TabInfo prepareGermplasmTabInfo(List<MeasurementVariable> measurementVariables, boolean isUsePrevious) throws MiddlewareQueryException {
        List<SettingDetail> detailList = new ArrayList<SettingDetail>();
        List<Integer> requiredIDList = buildVariableIDList(AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString());

        for (MeasurementVariable var : measurementVariables) {
            SettingDetail detail = createSettingDetail(var.getTermId(), var.getName());

            if (requiredIDList.contains(var.getTermId())) {
                detail.setDeletable(false);
            } else {
                detail.setDeletable(true);
            }

            if (!isUsePrevious) {
                detail.getVariable().setOperation(Operation.UPDATE);
            } else {
                detail.getVariable().setOperation(Operation.ADD);
            }

            detailList.add(detail);
        }

        TabInfo info = new TabInfo();
        info.setSettings(detailList);

        userSelection.setPlotsLevelList(detailList);

        return info;
    }

    protected TabInfo prepareMeasurementsTabInfo(List<MeasurementVariable> variatesList, boolean isUsePrevious) throws MiddlewareQueryException {

        List<SettingDetail> detailList = new ArrayList<SettingDetail>();

        for (MeasurementVariable var : variatesList) {
            SettingDetail detail = createSettingDetail(var.getTermId(), var.getName());

            if (!isUsePrevious) {
                detail.getVariable().setOperation(Operation.UPDATE);
            } else {
                detail.getVariable().setOperation(Operation.ADD);
            }

            detailList.add(detail);
        }

        TabInfo info = new TabInfo();
        info.setSettings(detailList);

        userSelection.setBaselineTraitsList(detailList);


        return info;
    }

    protected TabInfo prepareEnvironmentsTabInfo(Workbook workbook, boolean isUsePrevious) throws MiddlewareQueryException {
        TabInfo info = new TabInfo();
        Map<String, List<SettingDetail>> settingMap = new HashMap<String, List<SettingDetail>>();
        List<SettingDetail> managementDetailList = new ArrayList<SettingDetail>();
        List<SettingDetail> trialConditionsList = new ArrayList<SettingDetail>();
        List<Integer> hiddenFields = buildVariableIDList(AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString());

        for (MeasurementVariable var : workbook.getTrialConditions()) {
            SettingDetail detail = createSettingDetail(var.getTermId(), var.getName());

            if (hiddenFields.contains(var.getTermId())) {
                detail.setHidden(true);
            }

            managementDetailList.add(detail);

            if (!isUsePrevious) {
                detail.getVariable().setOperation(Operation.UPDATE);
            } else {
                detail.getVariable().setOperation(Operation.ADD);
            }
        }

        for (MeasurementVariable var : workbook.getTrialConstants()) {
            SettingDetail detail = createSettingDetail(var.getTermId(), var.getName());

            if (!isUsePrevious) {
                detail.getVariable().setOperation(Operation.UPDATE);
            } else {
                detail.getVariable().setOperation(Operation.ADD);
            }

            trialConditionsList.add(detail);
        }

        settingMap.put("managementDetails", managementDetailList);
        settingMap.put("trialConditionDetails", trialConditionsList);

        info.setSettingMap(settingMap);


        EnvironmentData data = new EnvironmentData();
        List<MeasurementRow> trialObservations = workbook.getTrialObservations();

        data.setNoOfEnvironments(trialObservations.size());

        List<Environment> environments = new ArrayList<Environment>();
        for (int i = 0; i < trialObservations.size(); i++) {
            Environment environment = new Environment();

            Map<Integer, String> managementDetailValues = new HashMap<Integer, String>();
            for (SettingDetail detail : managementDetailList) {

                MeasurementData mData = trialObservations.get(i).getMeasurementData(detail.getVariable().getCvTermId());
                if (mData != null) {
                    managementDetailValues.put(mData.getMeasurementVariable().getTermId(), mData.getValue());
                }
            }

            Map<Integer, String> trialConditionValues = new HashMap<Integer, String>();
            for (SettingDetail detail : trialConditionsList) {

                MeasurementData mData = trialObservations.get(i).getMeasurementData(detail.getVariable().getCvTermId());
                if (mData != null) {
                    trialConditionValues.put(mData.getMeasurementVariable().getTermId(), mData.getValue());
                }
            }

            environment.setManagementDetailValues(managementDetailValues);
            environment.setTrialDetailValues(trialConditionValues);
            environments.add(environment);
        }
        data.setEnvironments(environments);
        info.setData(data);


        userSelection.setTrialLevelVariableList(managementDetailList);

        userSelection.setNurseryConditions(trialConditionsList);


        return info;
    }

    public List<SettingDetail> retrieveVariablePairs(int cvTermId) {
        List<SettingDetail> output = new ArrayList<SettingDetail>();

        try {

            StandardVariable variable = ontologyService.getStandardVariable(cvTermId);

            List<StandardVariable> pairs = fieldbookMiddlewareService.getPossibleTreatmentPairs(variable.getId(),variable.getProperty().getId());

            for (StandardVariable item : pairs) {
                output.add(createSettingDetail(item.getId(),null));
            }

        } catch (MiddlewareQueryException e) {
            e.printStackTrace();

        }

        return output;
    }

    protected TabInfo prepareBasicDetailsTabInfo(StudyDetails studyDetails, boolean isUsePrevious) throws MiddlewareQueryException {
        Map<Integer, String> basicDetails = new HashMap<Integer, String>();
        List<SettingDetail> initialDetailList = new ArrayList<SettingDetail>();
        List<Integer> initialSettingIDs = buildVariableIDList(AppConstants.CREATE_TRIAL_REQUIRED_FIELDS.getString());

        for (Integer initialSettingID : initialSettingIDs) {
            try {
                basicDetails.put(initialSettingID, "");
                SettingDetail detail = createSettingDetail(initialSettingID, null);

                if (!isUsePrevious) {
                    detail.getVariable().setOperation(Operation.UPDATE);
                } else {
                    detail.getVariable().setOperation(Operation.ADD);
                }

                initialDetailList.add(detail);
            } catch (MiddlewareQueryException e) {
                e.printStackTrace();
            }
        }

        BasicDetails basic = new BasicDetails();

        basicDetails.put(TermId.STUDY_NAME.getId(), studyDetails.getStudyName());
        basicDetails.put(TermId.STUDY_TITLE.getId(), studyDetails.getTitle());
        basicDetails.put(TermId.STUDY_OBJECTIVE.getId(), studyDetails.getObjective());
        basicDetails.put(TermId.START_DATE.getId(), studyDetails.getStartDate());
        basicDetails.put(TermId.END_DATE.getId(), studyDetails.getEndDate());
        basic.setBasicDetails(basicDetails);

        int folderId = (int) studyDetails.getParentFolderId();
        String folderName;

        if (folderId == 1) {
            folderName = AppConstants.PROGRAM_TRIALS.getString();
        } else {
            folderName = fieldbookMiddlewareService.getFolderNameById(folderId);
        }

        basic.setFolderId(folderId);
        basic.setFolderName(folderName);
        basic.setFolderNameLabel(folderName);
        basic.setUserID(getCurrentIbdbUserId());
        basic.setUserName(fieldbookService.getPersonById(basic.getUserID()));


        TabInfo tab = new TabInfo();
        tab.setData(basic);


        userSelection.setBasicDetails(initialDetailList);


        return tab;
    }

    protected TabInfo prepareTrialSettingsTabInfo(List<MeasurementVariable> measurementVariables, boolean isUsePrevious) throws MiddlewareQueryException {
        TabInfo info = new TabInfo();
        Map<Integer, String> trialValues = new HashMap<Integer, String>();
        List<SettingDetail> details = new ArrayList<SettingDetail>();
        List<Integer> basicDetailIDList = buildVariableIDList(AppConstants.HIDE_TRIAL_FIELDS.getString());

        for (MeasurementVariable var : measurementVariables) {
            if (!basicDetailIDList.contains(var.getTermId())) {
                SettingDetail detail = createSettingDetail(var.getTermId(), var.getName());
                details.add(detail);

                if (!isUsePrevious) {
                    detail.getVariable().setOperation(Operation.UPDATE);
                } else {
                    detail.getVariable().setOperation(Operation.ADD);
                }

                trialValues.put(var.getTermId(), var.getValue());
            }
        }

        userSelection.setStudyLevelConditions(details);

        info.setSettings(details);
        TrialSettingsBean trialSettingsBean = new TrialSettingsBean();
        trialSettingsBean.setUserInput(trialValues);
        info.setData(trialSettingsBean);
        return info;
    }
}
