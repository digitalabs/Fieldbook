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
package com.efficio.fieldbook.web.nursery.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * The Class AddOrRemoveTraitsController.
 */
@Controller
@RequestMapping(AddOrRemoveTraitsController.URL)
public class AddOrRemoveTraitsController extends AbstractBaseFieldbookController{

    /** The Constant URL. */
    public static final String URL = "/NurseryManager/addOrRemoveTraits";
    
    public static final String OBSERVATIONS_HTML = "NurseryManager/observations";
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AddOrRemoveTraitsController.class);
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private OntologyService ontologyService;
    
    @Resource
    private MeasurementsGeneratorService measurementsGeneratorService;

    @Resource
    private ValidationService validationService;
    
    @Resource
    private PaginationListSelection paginationListSelection;

	@Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
  

	/* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "NurseryManager/addOrRemoveTraits";
    }
    
    /**
     * Shows the screen
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("createNurseryForm") CreateNurseryForm form
            , Model model, HttpSession session) throws MiddlewareQueryException{
    	
    	//getUserSelection().getWorkbook().getMeasurementDatasetVariables();
    	getUserSelection().setMeasurementRowList(measurementsGeneratorService.generateRealMeasurementRows(getUserSelection()));
    	form.setMeasurementRowList(getUserSelection().getMeasurementRowList());
    	form.setMeasurementVariables(getUserSelection().getWorkbook().getMeasurementDatasetVariables());
    	form.changePage(1);
    	userSelection.setCurrentPage(form.getCurrentPage());
    	
    	return super.show(model);
    }
    
    @RequestMapping(value="/viewNursery/{nurseryId}", method = RequestMethod.GET)
    public String viewNursery(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, 
            @PathVariable int nurseryId) {
        Workbook workbook = null;
        
        try { 
            workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        if (workbook != null) {
        	getUserSelection().setMeasurementRowList(workbook.getObservations());
            form.setMeasurementRowList(getUserSelection().getMeasurementRowList());
            form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
            form.setStudyName(workbook.getStudyDetails().getStudyName());
            form.changePage(1);
            userSelection.setCurrentPage(form.getCurrentPage());
            userSelection.setWorkbook(workbook);
        }
        
        return super.show(model);
    }
      
    /**
     * Gets the user selection.
     *
     * @return the user selection
     */
    public UserSelection getUserSelection() {
        return this.userSelection;
    }

    @RequestMapping(value="/viewNurseryAjax/{datasetId}", method = RequestMethod.GET)
    public String viewNurseryAjax(@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, 
            @PathVariable int datasetId) {

    	Workbook workbook = null;
        try { 
        	workbook = fieldbookMiddlewareService.getCompleteDataset(datasetId, false);
            fieldbookService.setAllPossibleValuesInWorkbook(workbook);
            SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), false, ontologyService);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        getUserSelection().setMeasurementRowList(workbook.getObservations());
        form.setMeasurementRowList(getUserSelection().getMeasurementRowList());
      	form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
	    paginationListSelection.addReviewDetailsList(String.valueOf(datasetId), form.getMeasurementRowList());
	    paginationListSelection.addReviewVariableList(String.valueOf(datasetId), form.getMeasurementVariables());
	    form.changePage(1);
	    userSelection.setCurrentPage(form.getCurrentPage());
    	
        return super.showAjaxPage(model, OBSERVATIONS_HTML);
    }
}