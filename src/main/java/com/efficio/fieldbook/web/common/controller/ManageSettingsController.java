package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 *
 * This controller class handles back end functionality that are common to the operations that require management of settings (Create/Edit Nursery/Trial)
 */

@Controller
@RequestMapping(value = ManageSettingsController.URL)
public class ManageSettingsController extends SettingsController{
    public static final String URL = "/manageSettings";

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ManageSettingsController.class);

    @Resource
    private OntologyService ontologyService;

    /**
     * Displays the Add Setting popup.
     *
     * @param mode the mode
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/displayAddSetting/{mode}", method = RequestMethod.GET)
    public Map<String, Object> showAddSettingPopup(@PathVariable int mode) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {

            List<StandardVariableReference> standardVariableList =
                    fieldbookService.filterStandardVariablesForSetting(mode, getSettingDetailList(mode));

            try {
                // TODO : question when the trait ref list is set to null
                if (userSelection.getTraitRefList() == null) {
                    List<TraitClassReference> traitRefList = ontologyService.getAllTraitGroupsHierarchy(true);
                    userSelection.setTraitRefList(traitRefList);
                }

                List<TraitClassReference> traitRefList = userSelection.getTraitRefList();

                //we convert it to map so that it would be easier to chekc if there is a record or not
                HashMap<String, StandardVariableReference> mapVariableRef = new HashMap<String, StandardVariableReference>();
                if (standardVariableList != null && !standardVariableList.isEmpty()) {
                    for (StandardVariableReference varRef : standardVariableList) {
                        mapVariableRef.put(varRef.getId().toString(), varRef);
                    }
                }

                // TODO : question purpose of mapVariableRef, as well as traitRefList
                String treeData = TreeViewUtil.convertOntologyTraitsToJson(traitRefList, mapVariableRef);
                String searchTreeData = TreeViewUtil.convertOntologyTraitsToSearchSingleLevelJson(traitRefList, mapVariableRef);
                result.put("treeData", treeData);
                result.put("searchTreeData", searchTreeData);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        //return "[]";
        return result;
    }

    /**
     * Adds the settings.
     *
     * @param form  the form
     * @param model the model
     * @param mode  the mode
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value = "/addSettings/{mode}", method = RequestMethod.POST)
    public List<SettingDetail> addSettings(@ModelAttribute("createNurseryForm") CreateNurseryForm form,
                              Model model, @PathVariable int mode) {
        List<SettingDetail> newSettings = new ArrayList<SettingDetail>();
        try {
            List<SettingVariable> selectedVariables = form.getSelectedVariables();
            if (selectedVariables != null && !selectedVariables.isEmpty()) {
                for (SettingVariable var : selectedVariables) {
                    Operation operation = removeVarFromDeletedList(var, mode);

                    var.setOperation(operation);
                    populateSettingVariable(var);
                    List<ValueReference> possibleValues =
                            fieldbookService.getAllPossibleValues(var.getCvTermId());
                    SettingDetail newSetting = new SettingDetail(var, possibleValues, null, true);
                    List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(var.getCvTermId(), this.getCurrentProjectId());
                    newSetting.setPossibleValuesFavorite(possibleValuesFavorite);
                    newSettings.add(newSetting);
                }
            }

            if (newSettings != null && !newSettings.isEmpty()) {
                addNewSettingDetails(mode, newSettings);
                return newSettings;
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return new ArrayList<SettingDetail>();
    }

    /**
     * Adds the new setting details.
     *
     * @param mode       the mode
     * @param newDetails the new details
     * @return the string
     * @throws Exception the exception
     */
    private void addNewSettingDetails(int mode
            , List<SettingDetail> newDetails) throws Exception {
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            if (userSelection.getStudyLevelConditions() == null) {
                userSelection.setStudyLevelConditions(newDetails);
            } else {
                userSelection.getStudyLevelConditions().addAll(newDetails);
            }

        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            if (userSelection.getPlotsLevelList() == null) {
                userSelection.setPlotsLevelList(newDetails);
            } else {
                userSelection.getPlotsLevelList().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt()) {
            if (userSelection.getBaselineTraitsList() == null) {
                userSelection.setBaselineTraitsList(newDetails);
            } else {
                userSelection.getBaselineTraitsList().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
            if (userSelection.getSelectionVariates() == null) {
                userSelection.setSelectionVariates(newDetails);
            } else {
                userSelection.getSelectionVariates().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
            if (userSelection.getTreatmentFactors() == null) {
                userSelection.setTreatmentFactors(newDetails);
            } else {
                userSelection.getTreatmentFactors().addAll(newDetails);
            }
        } else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
            if (userSelection.getTrialLevelVariableList() == null) {
                userSelection.setTrialLevelVariableList(newDetails);
            } else {
                userSelection.getTrialLevelVariableList().addAll(newDetails);
            }
        } else{
            if (userSelection.getNurseryConditions() == null) {
                userSelection.setNurseryConditions(newDetails);
            } else {
                userSelection.getNurseryConditions().addAll(newDetails);
            }
        }
    }

    private Operation removeVarFromDeletedList(SettingVariable var, int mode) {
            List<SettingDetail> settingsList = new ArrayList<SettingDetail>();
            if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
                settingsList = userSelection.getDeletedStudyLevelConditions();
            } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
                settingsList = userSelection.getDeletedPlotLevelList();
            } else if (mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()){
                settingsList = userSelection.getDeletedBaselineTraitsList();
            } else if (mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()){
                settingsList = userSelection.getDeletedNurseryConditions();
            }

            Operation operation = Operation.ADD;
            if (settingsList != null) {
                Iterator<SettingDetail> iter = settingsList.iterator();
                while (iter.hasNext()) {
                    SettingVariable deletedVariable = iter.next().getVariable();
                    if (deletedVariable.getCvTermId().equals(Integer.valueOf(var.getCvTermId()))) {
                        operation = deletedVariable.getOperation();
                        iter.remove();
                    }
                }
            }
            return operation;
        }

    /**
     * Gets the setting detail list.
     *
     * @param mode the mode
     * @return the setting detail list
     */
    private List<SettingDetail> getSettingDetailList(int mode) {
        if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
            return userSelection.getStudyLevelConditions();
        } else if (mode == AppConstants.SEGMENT_PLOT.getInt()) {
            return userSelection.getPlotsLevelList();
        } else if (mode == AppConstants.SEGMENT_TRAITS.getInt() || mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
            List<SettingDetail> newList = new ArrayList<SettingDetail>();

            for (SettingDetail setting : userSelection.getBaselineTraitsList()) {
                newList.add(setting);
            }

            for (SettingDetail setting : userSelection.getNurseryConditions()) {
                newList.add(setting);
            }

            return newList;
        } else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
            return userSelection.getSelectionVariates();
        } else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
            return userSelection.getTrialLevelVariableList();
        } else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
            return userSelection.getTreatmentFactors();
        }
        return null;
    }

    @Override
    public String getContentName() {
        return null;
    }
}
