package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import org.generationcp.middleware.domain.dms.StandardVariable;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SessionUtility;
import com.efficio.fieldbook.web.util.SettingsUtil;

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
    public String showMeasurements(@ModelAttribute("createTrialForm") CreateTrialForm form, Model model) {
    	
    	// TODO : integrate loading of data for
	    Workbook workbook = userSelection.getWorkbook();
        if (workbook != null) {
            try {
				SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), false, ontologyService);
				userSelection.setMeasurementRowList(workbook.getObservations());
				form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(workbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(workbook.getVariates())));
	            form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
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
            model.addAttribute("basicDetailsData", prepareBasicDetailsTabInfo(trialWorkbook.getStudyDetails(), false));
            model.addAttribute("germplasmData", prepareGermplasmTabInfo(trialWorkbook.getFactors(), false));
            model.addAttribute("environmentData", prepareEnvironmentsTabInfo(trialWorkbook, false));
            model.addAttribute("trialSettingsData", prepareTrialSettingsTabInfo(trialWorkbook.getStudyConditions(), false));
            model.addAttribute("measurementsData", prepareMeasurementsTabInfo(trialWorkbook.getVariates(), false));
            model.addAttribute("measurementDataExisting", fieldbookMiddlewareService.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(trialWorkbook.getVariates())));
            model.addAttribute("measurementRowCount", trialWorkbook.getObservations().size());
            form.setMeasurementDataExisting(fieldbookMiddlewareService.checkIfStudyHasMeasurementData(trialWorkbook.getMeasurementDatesetId(), SettingsUtil.buildVariates(trialWorkbook.getVariates())));
        }


        return showAngularPage(model);
    }

    @ResponseBody
    @RequestMapping(value = "/retrieveVariablePairs/{id}", method = RequestMethod.GET)
    public List<SettingDetail> retrieveVariablePairs(@PathVariable int id) {
        return super.retrieveVariablePairs(id);
    }

}
