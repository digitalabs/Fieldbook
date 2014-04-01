package com.efficio.fieldbook.web.trial.controller;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.trial.bean.TrialSelection;

@Controller
@RequestMapping(OpenTrialController.URL)
public class OpenTrialController extends
		AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(OpenTrialController.class);
    public static final String URL = "/TrialManager/addOrRemoveTraits";

    @Resource
	private TrialSelection trialSelection;
	
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	
	@Override
	public String getContentName() {
        return "TrialManager/openTrial";
	}

    @RequestMapping(value="/viewTrial/{trialId}", method = RequestMethod.GET)
    public String viewNursery(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, Model model, 
            @PathVariable int trialId) {
        Workbook workbook = null;
        
        try { 
            workbook = fieldbookMiddlewareService.getTrialDataSet(trialId);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        if (workbook != null) {
        	trialSelection.setMeasurementRowList(workbook.getObservations());
            form.setMeasurementRowList(trialSelection.getMeasurementRowList());
            form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
            form.setStudyName(workbook.getStudyDetails().getStudyName());
            form.changePage(1);
            trialSelection.setCurrentPage(form.getCurrentPage());
            trialSelection.setWorkbook(workbook);
        }
        
        return super.show(model);
    }
}
