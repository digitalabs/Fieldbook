package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping(OpenTrialController.URL)
public class OpenTrialController extends
        BaseTrialController {

    private static final Logger LOG = LoggerFactory.getLogger(OpenTrialController.class);
    public static final String URL = "/TrialManager/openTrial";

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

    @RequestMapping(value = "/{trialId}", method = RequestMethod.GET)
    public String openTrial(Model model, HttpSession session, @PathVariable Integer trialId) throws MiddlewareQueryException {
        SessionUtility.clearSessionData(session, new String[]{SessionUtility.USER_SELECTION_SESSION_NAME, SessionUtility.POSSIBLE_VALUES_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});

        if (trialId != null && trialId != 0) {

            Workbook trialWorkbook = fieldbookMiddlewareService.getTrialDataSet(trialId);

            model.addAttribute("basicDetailsData", prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false));
            model.addAttribute("germplasmData", prepareGermplasmTabInfo(trialWorkbook.getFactors(), false));
            model.addAttribute("environmentData", prepareEnvironmentsTabInfo(trialWorkbook, false));
            model.addAttribute("trialSettingsData", prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
            model.addAttribute("measurementsData", prepareMeasurementsTabInfo(trialWorkbook.getVariates(), false));

            // TODO : integrate loading of data for
        }


        return showAngularPage(model);
    }



}
