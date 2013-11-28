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
package com.efficio.fieldbook.web.label.printing.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.form.LabelPrintingForm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.util.AppConstants;


@Controller
@RequestMapping({LabelPrintingController.URL})
public class LabelPrintingController extends AbstractBaseFieldbookController{
 
     /** The Constant LOG. */
     private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingController.class);
    
    /** The Constant URL. */
    public static final String URL = "/LabelPrinting/specifyLabelDetails";
    
    @Resource
    private UserLabelPrinting userLabelPrinting;  
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @RequestMapping(value="/trial/{id}", method = RequestMethod.GET)
    public String showTrialLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, 
            Model model, HttpSession session, @PathVariable int id ) {
        session.invalidate();
        Study study = null;
        FieldMapInfo fieldMapInfo = null;
        try {
            study = fieldbookMiddlewareService.getStudy(id);
            fieldMapInfo = fieldbookMiddlewareService.getFieldMapInfoOfTrial(id);
        } catch (MiddlewareQueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        getUserLabelPrinting().setStudy(study);
        getUserLabelPrinting().setFieldMapInfo(fieldMapInfo);
        getUserLabelPrinting().setBarcodeNeeded("1");
        
        /*
        getUserLabelPrinting().setName(study.getName());
        getUserLabelPrinting().setTitle(study.getTitle());
        getUserLabelPrinting().setObjective(study.getObjective());
       
        getUserLabelPrinting().setNumberOfInstances("2");
        getUserLabelPrinting().setTotalNumberOfLabelToPrint("50");
         
        getUserLabelPrinting().setSizeOfLabelSheet("A4");
        getUserLabelPrinting().setNumberOfRowsPerPageOfLabel("10");
        getUserLabelPrinting().setNumberOfLabelPerRow("3");
        */
        form.setUserLabelPrinting(getUserLabelPrinting());
        
        model.addAttribute("availableFields",getAvailableLabelFields(true, false));
        
        form.setIsTrial(true);
        return super.show(model);
    }
    
    @RequestMapping(value="/nursery/{id}", method = RequestMethod.GET)
    public String showNurseryLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, Model model, 
            HttpSession session, @PathVariable int id) {
        session.invalidate();
        Study study = null;
        FieldMapInfo fieldMapInfo = null;
        try {
            study = fieldbookMiddlewareService.getStudy(id);
            fieldMapInfo = fieldbookMiddlewareService.getFieldMapInfoOfNursery(id);
        } catch (MiddlewareQueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        getUserLabelPrinting().setStudy(study);
        getUserLabelPrinting().setFieldMapInfo(fieldMapInfo);
        getUserLabelPrinting().setBarcodeNeeded("1");
        
        /*
        getUserLabelPrinting().setName(study.getName());
        getUserLabelPrinting().setTitle(study.getTitle());
        getUserLabelPrinting().setObjective(study.getObjective());
       
        getUserLabelPrinting().setNumberOfInstances("2");
        getUserLabelPrinting().setTotalNumberOfLabelToPrint("50");
         
        getUserLabelPrinting().setSizeOfLabelSheet("A4");
        getUserLabelPrinting().setNumberOfRowsPerPageOfLabel("10");
        getUserLabelPrinting().setNumberOfLabelPerRow("3");
        */
        form.setUserLabelPrinting(getUserLabelPrinting());
        model.addAttribute("availableFields", getAvailableLabelFields(false, false));
        form.setIsTrial(false);
        return super.show(model);
    }
    
    private List<LabelFields> getAvailableLabelFields(boolean isTrial, boolean isFromFieldMap){
        List<LabelFields> labelFieldsList = new ArrayList();
        
        
        labelFieldsList.add(new LabelFields("Entry #", AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM));
        labelFieldsList.add(new LabelFields("GID", AppConstants.AVAILABLE_LABEL_FIELDS_GID));
        labelFieldsList.add(new LabelFields("Germplasm Name", AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME));
        labelFieldsList.add(new LabelFields("Year", AppConstants.AVAILABLE_LABEL_FIELDS_YEAR));
        labelFieldsList.add(new LabelFields("Season", AppConstants.AVAILABLE_LABEL_FIELDS_SEASON));
        if(isTrial){
            labelFieldsList.add(new LabelFields("Trial Name", AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME));
            labelFieldsList.add(new LabelFields("Trial Instance #", AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM));
        }else{
            labelFieldsList.add(new LabelFields("Nursery Name", AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME));
        }
        if(isFromFieldMap){
            labelFieldsList.add(new LabelFields("Rep", AppConstants.AVAILABLE_LABEL_FIELDS_REP));
            labelFieldsList.add(new LabelFields("Location", AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION));
            labelFieldsList.add(new LabelFields("Block Name", AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME));
            labelFieldsList.add(new LabelFields("Plot (Range / Column)", AppConstants.AVAILABLE_LABEL_FIELDS_PLOT));
        }
        return labelFieldsList;
    }
    /**
     * Submits the details.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public String submitDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, BindingResult result, Model model) {
        return "redirect:" + GenerateLabelController.URL;
    } 
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "LabelPrinting/specifyLabelDetails";
    }

    
    public UserLabelPrinting getUserLabelPrinting() {
        return userLabelPrinting;
    }

    
    public void setUserLabelPrinting(UserLabelPrinting userLabelPrinting) {
        this.userLabelPrinting = userLabelPrinting;
    }
    
    
    
}
