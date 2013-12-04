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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.form.LabelPrintingForm;
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
    @Resource
    private LabelPrintingService labelPrintingService;
    @Resource
    private UserFieldmap userFieldmap;
    
    private static final int BUFFER_SIZE = 4096 * 4;
    
    @Resource
    private ResourceBundleMessageSource messageSource;
    
   
    @RequestMapping(value="/trial/{id}", method = RequestMethod.GET)
    public String showTrialLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, 
            Model model, HttpSession session, @PathVariable int id , Locale locale) {
        session.invalidate();
        Study study = null;
        List<FieldMapInfo> fieldMapInfoList = null;
        FieldMapInfo fieldMapInfo = null;
        try {
            study = fieldbookMiddlewareService.getStudy(id);
            List<Integer> ids = new ArrayList<Integer>();
            ids.add(id);
            fieldMapInfoList = fieldbookMiddlewareService.getFieldMapInfoOfTrial(ids);
            for (FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
                fieldMapInfo = fieldMapInfoDetail;
            }
        } catch (MiddlewareQueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        getUserLabelPrinting().setStudy(study);
        getUserLabelPrinting().setFieldMapInfo(fieldMapInfo);
        getUserLabelPrinting().setBarcodeNeeded("1");
        getUserLabelPrinting().setNumberOfLabelPerRow("3");
        
        getUserLabelPrinting().setFilename(generateDefaultFilename(getUserLabelPrinting(), true));
        form.setUserLabelPrinting(getUserLabelPrinting());
        
        model.addAttribute("availableFields",getAvailableLabelFields(true, false, locale));
        
        form.setIsTrial(true);
        return super.show(model);
    }
    
    @RequestMapping(value="/nursery/{id}", method = RequestMethod.GET)
    public String showNurseryLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, Model model, 
            HttpSession session, @PathVariable int id, Locale locale) {
        session.invalidate();
        Study study = null;
        List<FieldMapInfo> fieldMapInfoList = null;
        FieldMapInfo fieldMapInfo = null;
        try {
            study = fieldbookMiddlewareService.getStudy(id);
            List<Integer> ids = new ArrayList<Integer>();
            ids.add(id);
            fieldMapInfoList = fieldbookMiddlewareService.getFieldMapInfoOfNursery(ids);
            for (FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
                fieldMapInfo = fieldMapInfoDetail;
            }
        } catch (MiddlewareQueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        getUserLabelPrinting().setStudy(study);
        getUserLabelPrinting().setFieldMapInfo(fieldMapInfo);
        getUserLabelPrinting().setBarcodeNeeded("1");
        getUserLabelPrinting().setNumberOfLabelPerRow("3");
        
        
        
        getUserLabelPrinting().setFilename(generateDefaultFilename(getUserLabelPrinting(), false));
        form.setUserLabelPrinting(getUserLabelPrinting());
        model.addAttribute("availableFields", getAvailableLabelFields(false, false, locale));
        form.setIsTrial(false);
        return super.show(model);
    }
    
    @RequestMapping(value="/fieldmap", method = RequestMethod.GET)
    public String showFieldmapLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, 
            Model model, HttpSession session, Locale locale) {
        System.out.println(userFieldmap.getSelectedFieldMaps());
        List<FieldMapInfo> fieldMapInfoList = userFieldmap.getSelectedFieldMaps();
        FieldMapInfo fieldMapInfo = null;
        
        //getUserLabelPrinting().setStudy();
        getUserLabelPrinting().setFieldMapInfo(fieldMapInfo);
        getUserLabelPrinting().setFieldMapInfoList(fieldMapInfoList);
        getUserLabelPrinting().setBarcodeNeeded("1");
        getUserLabelPrinting().setNumberOfLabelPerRow("3");
        
        getUserLabelPrinting().setFilename(generateDefaultFilename(getUserLabelPrinting(), true));
        form.setUserLabelPrinting(getUserLabelPrinting());
        
        model.addAttribute("availableFields",getAvailableLabelFields(true, false, locale));
        
        form.setIsTrial(userFieldmap.isTrial());
        
        return super.show(model);
    }
    
    private String generateDefaultFilename(UserLabelPrinting userLabelPrinting, boolean isTrial){
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = "Labels_for_" + userLabelPrinting.getName();
        
        if(isTrial)
            fileName += "_" + userLabelPrinting.getNumberOfInstances() + "_" + currentDate; //changed selected name to block name for now        
        else
            fileName += "_" + currentDate; //changed selected name to block name for now
        
        return fileName;
    }
    
    private List<LabelFields> getAvailableLabelFields(boolean isTrial, boolean isFromFieldMap, Locale locale){
        List<LabelFields> labelFieldsList = new ArrayList();
        
        
        labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.entry.num", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM));
        labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.gid", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_GID));
        labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.germplasm.name", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME));
        labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.parentage", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE));
        labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.year", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_YEAR));
        labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.season", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_SEASON));
        if(isTrial){
            labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.trial.name", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME));
            labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.trial.instance.num", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM));
        }else{
            labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.nursery.name", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME));
        }
        if(isFromFieldMap){
            labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.rep", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_REP));
            labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.location", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION));
            labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.block.name", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME));
            labelFieldsList.add(new LabelFields(messageSource.getMessage("label.printing.available.fields.plot", null, locale), AppConstants.AVAILABLE_LABEL_FIELDS_PLOT));
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
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String submitDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, 
            BindingResult result, Model model, HttpServletResponse response) {
        
        getUserLabelPrinting().setBarcodeNeeded(form.getUserLabelPrinting().getBarcodeNeeded());
        getUserLabelPrinting().setSizeOfLabelSheet(form.getUserLabelPrinting().getSizeOfLabelSheet());
        getUserLabelPrinting().setNumberOfLabelPerRow(form.getUserLabelPrinting().getNumberOfLabelPerRow());
        getUserLabelPrinting().setNumberOfRowsPerPageOfLabel(form.getUserLabelPrinting().getNumberOfRowsPerPageOfLabel());
        getUserLabelPrinting().setLeftSelectedLabelFields(form.getUserLabelPrinting().getLeftSelectedLabelFields());
        getUserLabelPrinting().setRightSelectedLabelFields(form.getUserLabelPrinting().getRightSelectedLabelFields());
        getUserLabelPrinting().setFirstBarcodeField(form.getUserLabelPrinting().getFirstBarcodeField());        
        getUserLabelPrinting().setSecondBarcodeField(form.getUserLabelPrinting().getSecondBarcodeField());
        getUserLabelPrinting().setThirdBarcodeField(form.getUserLabelPrinting().getThirdBarcodeField());
        getUserLabelPrinting().setFilename(form.getUserLabelPrinting().getFilename());
        getUserLabelPrinting().setGenerateType(form.getUserLabelPrinting().getGenerateType());
        
         
        FieldMapDatasetInfo datasetInfo =  getUserLabelPrinting().getFieldMapInfo().getDatasets().get(0);
       
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            if(getUserLabelPrinting().getGenerateType().equalsIgnoreCase("1")){
                String fileName = labelPrintingService.generatePDFLabels(datasetInfo, getUserLabelPrinting(), baos);
                response.setHeader("Content-disposition","attachment; filename=" + getUserLabelPrinting().getFilename() + ".pdf");
            }else{
                String fileName = labelPrintingService.generateXlSLabels(datasetInfo, getUserLabelPrinting(), baos);
                response.setHeader("Content-disposition","attachment; filename=" + getUserLabelPrinting().getFilename() + ".xls");
            }
            //File xls = new File(fileName); // the selected name + current date
            //FileInputStream in;
            //in = new FileInputStream(xls);
            OutputStream out = response.getOutputStream();

            
            /*
            byte[] buffer= new byte[BUFFER_SIZE]; // use bigger if you want
            int length = 0;

            while ((length = in.read(buffer)) > 0){
                 out.write(buffer, 0, length);
            }
            */
            out.write(baos.toByteArray());
            
            //in.close();
            out.close();
            return "";
        } catch (MiddlewareQueryException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         
         
         
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
