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

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.efficio.fieldbook.web.label.printing.xml.LabelPrintingSetting;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
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
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.form.LabelPrintingForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import com.efficio.fieldbook.web.util.SessionUtility;
import org.springframework.web.util.WebUtils;

/**
 * The Class LabelPrintingController.
 * 
 * This class would handle the label printing for the pdf and excel generation.
 */
@Controller
@RequestMapping({LabelPrintingController.URL})
public class LabelPrintingController extends AbstractBaseFieldbookController{
 
     /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingController.class);
    
    /** The Constant URL. */
    public static final String URL = "/LabelPrinting/specifyLabelDetails";
    
    /** The user label printing. */
    @Resource
    private UserLabelPrinting userLabelPrinting;  
    
    /** The fieldbook middleware service. */
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    /** The label printing service. */
    @Resource
    private LabelPrintingService labelPrintingService;
    
    /** The user fieldmap. */
    @Resource
    private UserFieldmap userFieldmap;
    
    /** The Constant BUFFER_SIZE. */
    private static final int BUFFER_SIZE = 4096 * 4;
    
    /** The message source. */
    @Resource
    private ResourceBundleMessageSource messageSource;
    
   
    /**
     * Show trial label details.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @param id the id
     * @param locale the locale
     * @return the string
     */
    @RequestMapping(value="/trial/{id}", method = RequestMethod.GET)
    public String showTrialLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, 
            Model model, HttpServletRequest req, HttpSession session, @PathVariable int id , Locale locale) {
    	
    	SessionUtility.clearSessionData(session, new String[]{SessionUtility.LABEL_PRINTING_SESSION_NAME, SessionUtility.FIELDMAP_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
        Study study = null;
        List<FieldMapInfo> fieldMapInfoList = null;
        FieldMapInfo fieldMapInfo = null;
        boolean hasFieldMap = false;
        try {
            study = fieldbookMiddlewareService.getStudy(id);
            List<Integer> ids = new ArrayList<Integer>();
            ids.add(id);
            fieldMapInfoList = fieldbookMiddlewareService.getFieldMapInfoOfTrial(ids);
            
            for (FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
                fieldMapInfo = fieldMapInfoDetail;
                hasFieldMap = labelPrintingService.checkAndSetFieldmapProperties(
						getUserLabelPrinting(), fieldMapInfoDetail);
            }
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        getUserLabelPrinting().setStudy(study);
        getUserLabelPrinting().setFieldMapInfo(fieldMapInfo);
        getUserLabelPrinting().setBarcodeNeeded("1");
        getUserLabelPrinting().setNumberOfLabelPerRow("3");
        
        getUserLabelPrinting().setFilename(generateDefaultFilename(getUserLabelPrinting(), true));
        form.setUserLabelPrinting(getUserLabelPrinting());
        
        model.addAttribute("availableFields", labelPrintingService.getAvailableLabelFields(true, hasFieldMap, locale));
        
        form.setIsTrial(true);
        return super.show(model);
    }
    
    /**
     * Show nursery label details.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @param id the id
     * @param locale the locale
     * @return the string
     */
    @RequestMapping(value="/nursery/{id}", method = RequestMethod.GET)
    public String showNurseryLabelDetails(
            @ModelAttribute("labelPrintingForm") LabelPrintingForm form, Model model, 
            HttpServletRequest req, HttpSession session, @PathVariable int id, Locale locale) {
    	SessionUtility.clearSessionData(session, new String[]{SessionUtility.LABEL_PRINTING_SESSION_NAME, SessionUtility.FIELDMAP_SESSION_NAME, SessionUtility.PAGINATION_LIST_SELECTION_SESSION_NAME});
        Study study = null;
        List<FieldMapInfo> fieldMapInfoList = null;
        FieldMapInfo fieldMapInfo = null;
        boolean hasFieldMap = false;
        try {
            study = fieldbookMiddlewareService.getStudy(id);
            List<Integer> ids = new ArrayList<Integer>();
            ids.add(id);
            fieldMapInfoList = fieldbookMiddlewareService.getFieldMapInfoOfNursery(ids);
            for (FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
                fieldMapInfo = fieldMapInfoDetail;
                hasFieldMap = labelPrintingService.checkAndSetFieldmapProperties(getUserLabelPrinting(), fieldMapInfoDetail);
            }
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        getUserLabelPrinting().setStudy(study);
        getUserLabelPrinting().setFieldMapInfo(fieldMapInfo);
        getUserLabelPrinting().setBarcodeNeeded("1");
        getUserLabelPrinting().setNumberOfLabelPerRow("3");
        
        getUserLabelPrinting().setFilename(generateDefaultFilename(getUserLabelPrinting(), false));
        form.setUserLabelPrinting(getUserLabelPrinting());
        model.addAttribute("availableFields", labelPrintingService.getAvailableLabelFields(false, hasFieldMap, locale));
        form.setIsTrial(false);
        return super.show(model);
    }
    
    /**
     * Show fieldmap label details.
     *
     * @param form the form
     * @param model the model
     * @param session the session
     * @param locale the locale
     * @return the string
     */
    @RequestMapping(value="/fieldmap", method = RequestMethod.GET)
    public String showFieldmapLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, 
            Model model, HttpSession session, Locale locale) {
        List<FieldMapInfo> fieldMapInfoList = userFieldmap.getSelectedFieldMaps();
        FieldMapInfo fieldMapInfo = null;
        
        getUserLabelPrinting().setFieldMapInfo(fieldMapInfo);
        getUserLabelPrinting().setFieldMapInfoList(fieldMapInfoList);
        getUserLabelPrinting().setBarcodeNeeded("1");
        getUserLabelPrinting().setNumberOfLabelPerRow("3");
        
        getUserLabelPrinting().setFirstBarcodeField("");
        getUserLabelPrinting().setSecondBarcodeField("");
        getUserLabelPrinting().setThirdBarcodeField("");
        getUserLabelPrinting().setFieldMapsExisting(true);
        
        getUserLabelPrinting().setFilename(
                generateDefaultFilename(getUserLabelPrinting(), userFieldmap.isTrial()));
        form.setUserLabelPrinting(getUserLabelPrinting());
        
        model.addAttribute("availableFields"
                , labelPrintingService.getAvailableLabelFields(userFieldmap.isTrial(), true, locale));
        
        form.setIsTrial(userFieldmap.isTrial());
        
        return super.show(model);
    }
    
    /**
     * Generate default filename.
     *
     * @param userLabelPrinting the user label printing
     * @param isTrial the is trial
     * @return the string
     */
    private String generateDefaultFilename(UserLabelPrinting userLabelPrinting, boolean isTrial){
        String currentDate = DateUtil.getCurrentDate();
        String fileName = "Labels-for-" + userLabelPrinting.getName();
        
        if(isTrial) {
            if (getUserLabelPrinting().getFieldMapInfoList() != null) {
                fileName = "Trial-Field-Map-Labels-" + currentDate;
            } else {
            	//changed selected name to block name for now
                fileName += "-" + userLabelPrinting.getNumberOfInstances() 
                        + "-" + currentDate; 
            }
        } else {
            if (getUserLabelPrinting().getFieldMapInfoList() != null) {
                fileName = "Nursery-Field-Map-Labels-" + currentDate;
            } else {
            	//changed selected name to block name for now
                fileName += "-" + currentDate; 
            }
        }
        
        fileName = fileName.replaceAll("[^a-zA-Z0-9\\-_\\.=^&'@{}$!-#()%.+~_\\[\\]]", "_");
        fileName = fileName.replaceAll("\"", "_");
        
        return fileName;
    }
    
    /**
     * Export file.
     *
     * @param response the response
     * @return the string
     */
    @ResponseBody
    @RequestMapping(value="/download", method = RequestMethod.GET)
    public String exportFile(HttpServletResponse response) {
        
        String fileName = getUserLabelPrinting().getFilenameDL();

        if(fileName.indexOf(".pdf") != -1) {
        	response.setContentType("application/pdf");
        } else {
        	response.setContentType("application/vnd.ms-excel");
        }
        response.setHeader("Content-disposition", "attachment; filename=" + fileName);

        // the selected name + current date
        File xls = new File(getUserLabelPrinting().getFilenameDLLocation()); 
        FileInputStream in;
        
        try {
            in = new FileInputStream(xls);
            OutputStream out = response.getOutputStream();

            // use bigger if you want
            byte[] buffer= new byte[BUFFER_SIZE]; 
            int length = 0;

            while ((length = in.read(buffer)) > 0){
                 out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
        	LOG.error(e.getMessage(), e);
        } catch (IOException e) {
        	LOG.error(e.getMessage(), e);
        }
        
        return "";
    }
    
    /**
     * Submits the details.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @param response the response
     * @return the string
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public Map<String,Object> submitDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, 
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
        
        List<FieldMapInfo> fieldMapInfoList = getUserLabelPrinting().getFieldMapInfoList();
        List<StudyTrialInstanceInfo> trialInstances = null;
        if (fieldMapInfoList != null) {
            trialInstances = generateTrialInstancesFromSelectedFieldMaps(fieldMapInfoList, form);
        } else {
            trialInstances = generateTrialInstancesFromFieldMap();
            for(StudyTrialInstanceInfo trialInstance : trialInstances){
                FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();
                fieldMapTrialInstanceInfo.setLocationName(fieldMapTrialInstanceInfo.getSiteName());
            }
        }
        
        return generateLabels(trialInstances);
    } 
    
    protected Map<String,Object> generateLabels(List<StudyTrialInstanceInfo> trialInstances) {
    	Map<String,Object> results = new HashMap<String, Object>();
    	try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	String fileName = "";
	    	if(getUserLabelPrinting().getGenerateType().equalsIgnoreCase(AppConstants.LABEL_PRINTING_PDF.getString())){
	        	fileName = getFileNameAndSetFileLocations(".pdf"); 
	            fileName = labelPrintingService.generatePDFLabels(trialInstances, 
	                            getUserLabelPrinting(), baos);
	        } else if (getUserLabelPrinting().getGenerateType().equalsIgnoreCase(AppConstants.LABEL_PRINTING_EXCEL.getString())) {
	        	fileName  = getFileNameAndSetFileLocations(".xls");
	            fileName = labelPrintingService.generateXlSLabels(trialInstances, 
	                            getUserLabelPrinting(), baos);
	        } else {
	        	fileName = getFileNameAndSetFileLocations(".csv");
	        	fileName = labelPrintingService.generateCSVLabels(trialInstances, userLabelPrinting, baos);
	        }
	        results.put("isSuccess", 1);
	        results.put("fileName", fileName);
	    } catch (MiddlewareQueryException e) {
	        LOG.error(e.getMessage(), e);
	        results.put("isSuccess", 0);
	        results.put(AppConstants.MESSAGE.getString(), e.getMessage());
	    } catch (LabelPrintingException e) {
	        LOG.error(e.getMessage(), e);
	        results.put("isSuccess", 0);
	        Locale locale = LocaleContextHolder.getLocale();
	        results.put(AppConstants.MESSAGE.getString(), messageSource.getMessage(
	                e.getErrorCode(), new String[]{e.getLabelError()}, locale));
	    } catch (IOException e) {
	    	LOG.error(e.getMessage(), e);
	    	results.put("isSuccess", 0);
	        results.put(AppConstants.MESSAGE.getString(), e.getMessage());
	    }
    	return results;
	}

	@ResponseBody
	@RequestMapping(value = "/presets/list", method = RequestMethod.GET)
	public List<LabelPrintingPresets> getLabelPrintingPresets(HttpServletRequest request) {
		ContextInfo contextInfo = (ContextInfo) WebUtils
				.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

		try {
			return labelPrintingService
					.getAllLabelPrintingPresets(contextInfo.getSelectedProjectId().intValue());
		} catch (LabelPrintingException e) {
			LOG.error(e.getMessage(), e);
		}

		return new ArrayList<LabelPrintingPresets>();
	}

	@ResponseBody
	@RequestMapping(value = "/presets/{presetType}/{presetId}", method = RequestMethod.GET, produces = "application/json")
	public LabelPrintingSetting getLabelPrintingSetting(@PathVariable int presetType,
			@PathVariable int presetId) {
		try {
			final Unmarshaller parseXML = JAXBContext.newInstance(LabelPrintingSetting.class)
					.createUnmarshaller();

			// retrieve appropriate setting
			String xmlToRead = labelPrintingService.getLabelPrintingPresetConfig(presetType,presetId);

			return (LabelPrintingSetting) parseXML
					.unmarshal(new StringReader(xmlToRead));

		} catch (JAXBException e) {
			LOG.error(e.getMessage(), e);
		} catch (LabelPrintingException e) {
			LOG.error(e.getMessage(), e);
		}

		return null;
	}

    private String getFileNameAndSetFileLocations(String extension) {
    	String fileName = getUserLabelPrinting().getFilename().replaceAll(" ",  "-") + extension;
    	String fileNameLocation  = System.getProperty( "user.home" ) + "/"+fileName;
    	
    	getUserLabelPrinting().setFilenameDL(fileName);
    	getUserLabelPrinting().setFilenameDLLocation(fileNameLocation);
    	return fileName;
	}

	/**
     * Generate trial instances from field map.
     *
     * @return the list
     */
    private List<StudyTrialInstanceInfo> generateTrialInstancesFromFieldMap() {
        List<FieldMapTrialInstanceInfo> trialInstances = 
                getUserLabelPrinting().getFieldMapInfo().getDatasets().get(0).getTrialInstances();
        List<StudyTrialInstanceInfo> studyTrial = new ArrayList<StudyTrialInstanceInfo>();
        for (FieldMapTrialInstanceInfo trialInstance : trialInstances) {
            StudyTrialInstanceInfo studyTrialInstance = new StudyTrialInstanceInfo(
                        trialInstance, getUserLabelPrinting().getFieldMapInfo().getFieldbookName());
            studyTrial.add(studyTrialInstance);
        }
        return studyTrial;
    }
    
    /**
     * Generate trial instances from selected field maps.
     *
     * @param fieldMapInfoList the field map info list
     * @param form the form
     * @return the list
     */
    private List<StudyTrialInstanceInfo> generateTrialInstancesFromSelectedFieldMaps(
                List<FieldMapInfo> fieldMapInfoList, LabelPrintingForm form) {
        List<StudyTrialInstanceInfo> trialInstances = new ArrayList<StudyTrialInstanceInfo>();
        String[] fieldMapOrder = form.getUserLabelPrinting().getOrder().split(",");
        for (String fieldmap : fieldMapOrder) {
            String[] fieldMapGroup = fieldmap.split("\\|");
            int order = Integer.parseInt(fieldMapGroup[0]);
            int studyId = Integer.parseInt(fieldMapGroup[1]);
            int datasetId = Integer.parseInt(fieldMapGroup[2]);
            int geolocationId = Integer.parseInt(fieldMapGroup[3]);
            
            for (FieldMapInfo fieldMapInfo : fieldMapInfoList) {
                if (fieldMapInfo.getFieldbookId().equals(studyId)) {
                    fieldMapInfo.getDataSet(datasetId).getTrialInstance(geolocationId).setOrder(order);
                    StudyTrialInstanceInfo trialInstance = 
                                new StudyTrialInstanceInfo(fieldMapInfo.getDataSet(datasetId)
                                        .getTrialInstance(geolocationId), 
                                            fieldMapInfo.getFieldbookName());
                    if (userFieldmap.getBlockName() != null && userFieldmap.getLocationName() != null) {
                        trialInstance.getTrialInstance().setBlockName(userFieldmap.getBlockName());
                        trialInstance.getTrialInstance().setFieldName(userFieldmap.getFieldName());
                        trialInstance.getTrialInstance().setLocationName(userFieldmap.getLocationName());
                    }
                    trialInstances.add(trialInstance);
                    break;
                }
            }
        }
        
        Collections.sort(trialInstances, new  Comparator<StudyTrialInstanceInfo>() {
            @Override
            public int compare(StudyTrialInstanceInfo o1, StudyTrialInstanceInfo o2) {
                    return o1.getTrialInstance().getOrder().compareTo(o2.getTrialInstance().getOrder());
            }
        }
        );
        
        return trialInstances;
    }
    
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "LabelPrinting/specifyLabelDetails";
    }
    
    /**
     * Gets the user label printing.
     *
     * @return the user label printing
     */
    public UserLabelPrinting getUserLabelPrinting() {
        return userLabelPrinting;
    }
    
    /**
     * Sets the user label printing.
     *
     * @param userLabelPrinting the new user label printing
     */
    public void setUserLabelPrinting(UserLabelPrinting userLabelPrinting) {
        this.userLabelPrinting = userLabelPrinting;
    }
}
