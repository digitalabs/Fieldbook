package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        }
        return null;
    }

    @Override
    public String getContentName() {
        return null;
    }
}
