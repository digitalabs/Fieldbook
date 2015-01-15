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
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.efficio.fieldbook.web.label.printing.xml.BarcodeLabelPrintingSetting;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.label.printing.xml.CSVExcelLabelPrintingSetting;
import com.efficio.fieldbook.web.label.printing.xml.LabelPrintingSetting;
import com.efficio.fieldbook.web.label.printing.xml.PDFLabelPrintingSetting;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.Workbook;
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
import org.springframework.web.bind.annotation.*;

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
public class LabelPrintingController extends AbstractBaseFieldbookController {

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

    @Resource
    private UserSelection userSelection;
    
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
                        this.userLabelPrinting, fieldMapInfoDetail);
            }
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }

        this.userLabelPrinting.setStudy(study);
        this.userLabelPrinting.setFieldMapInfo(fieldMapInfo);
        this.userLabelPrinting.setBarcodeNeeded("0");
        this.userLabelPrinting.setIncludeColumnHeadinginNonPdf("1");
        this.userLabelPrinting.setNumberOfLabelPerRow("3");

        this.userLabelPrinting.setFilename(generateDefaultFilename(this.userLabelPrinting, true));
        form.setUserLabelPrinting(this.userLabelPrinting);

        model.addAttribute("availableFields", labelPrintingService.getAvailableLabelFields(true, hasFieldMap, locale, id));

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
                hasFieldMap = labelPrintingService.checkAndSetFieldmapProperties(this.userLabelPrinting, fieldMapInfoDetail);
            }
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        this.userLabelPrinting.setStudy(study);
        this.userLabelPrinting.setFieldMapInfo(fieldMapInfo);
        this.userLabelPrinting.setBarcodeNeeded("0");
        this.userLabelPrinting.setIncludeColumnHeadinginNonPdf("1");
        this.userLabelPrinting.setNumberOfLabelPerRow("3");

        this.userLabelPrinting.setFilename(generateDefaultFilename(this.userLabelPrinting, false));
        form.setUserLabelPrinting(this.userLabelPrinting);
        model.addAttribute("availableFields", labelPrintingService.getAvailableLabelFields(false, hasFieldMap, locale, id));
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

        this.userLabelPrinting.setFieldMapInfo(fieldMapInfo);
        this.userLabelPrinting.setFieldMapInfoList(fieldMapInfoList);
        this.userLabelPrinting.setBarcodeNeeded("0");
        this.userLabelPrinting.setIncludeColumnHeadinginNonPdf("1");
        this.userLabelPrinting.setNumberOfLabelPerRow("3");

        this.userLabelPrinting.setFirstBarcodeField("");
        this.userLabelPrinting.setSecondBarcodeField("");
        this.userLabelPrinting.setThirdBarcodeField("");
        this.userLabelPrinting.setFieldMapsExisting(true);

        this.userLabelPrinting.setFilename(
                generateDefaultFilename(this.userLabelPrinting, userFieldmap.isTrial()));
        form.setUserLabelPrinting(this.userLabelPrinting);

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
            if (this.userLabelPrinting.getFieldMapInfoList() != null) {
                fileName = "Trial-Field-Map-Labels-" + currentDate;
            } else {
            	//changed selected name to block name for now
                fileName += "-" + userLabelPrinting.getNumberOfInstances()
                        + "-" + currentDate;
            }
        } else {
            if (this.userLabelPrinting.getFieldMapInfoList() != null) {
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

        String fileName = this.userLabelPrinting.getFilenameDL();

        if(fileName.indexOf(".pdf") != -1) {
        	response.setContentType("application/pdf");
        } else {
        	response.setContentType("application/vnd.ms-excel");
        }
        response.setHeader("Content-disposition", "attachment; filename=" + fileName);

        // the selected name + current date
        File xls = new File(this.userLabelPrinting.getFilenameDLLocation());
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

        this.userLabelPrinting.setBarcodeNeeded(form.getUserLabelPrinting().getBarcodeNeeded());
        this.userLabelPrinting.setSizeOfLabelSheet(
                form.getUserLabelPrinting().getSizeOfLabelSheet());
        this.userLabelPrinting.setNumberOfLabelPerRow(
                form.getUserLabelPrinting().getNumberOfLabelPerRow());
        this.userLabelPrinting.setNumberOfRowsPerPageOfLabel(
                form.getUserLabelPrinting().getNumberOfRowsPerPageOfLabel());
        this.userLabelPrinting.setLeftSelectedLabelFields(
                form.getUserLabelPrinting().getLeftSelectedLabelFields());
        this.userLabelPrinting.setRightSelectedLabelFields(
                form.getUserLabelPrinting().getRightSelectedLabelFields());
        this.userLabelPrinting.setMainSelectedLabelFields(
                form.getUserLabelPrinting().getMainSelectedLabelFields());
        this.userLabelPrinting.setIncludeColumnHeadinginNonPdf(
                form.getUserLabelPrinting().getIncludeColumnHeadinginNonPdf());
        this.userLabelPrinting.setSettingsName(form.getUserLabelPrinting().getSettingsName());
        this.userLabelPrinting.setFirstBarcodeField(
                form.getUserLabelPrinting().getFirstBarcodeField());
        this.userLabelPrinting
                .setSecondBarcodeField(form.getUserLabelPrinting().getSecondBarcodeField());
        this.userLabelPrinting.setThirdBarcodeField(
                form.getUserLabelPrinting().getThirdBarcodeField());
        this.userLabelPrinting.setFilename(form.getUserLabelPrinting().getFilename());
        this.userLabelPrinting.setGenerateType(form.getUserLabelPrinting().getGenerateType());

        List<FieldMapInfo> fieldMapInfoList = this.userLabelPrinting.getFieldMapInfoList();
        Workbook workbook = userSelection.getWorkbook();

        if (workbook != null) {
            labelPrintingService.populateUserSpecifiedLabelFields(this.userLabelPrinting.getFieldMapInfo().getDatasets().get(0).getTrialInstances()
                    , workbook, this.userLabelPrinting.getMainSelectedLabelFields(), form.getIsTrial());
        }

        List<StudyTrialInstanceInfo> trialInstances = null;

        if (fieldMapInfoList != null) {
            trialInstances = generateTrialInstancesFromSelectedFieldMaps(fieldMapInfoList, form);
        } else {
            // initial implementation of BMS-186 will be for single studies only, not for cases where multiple studies partcipating in a single fieldmap
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
	    	if(this.userLabelPrinting.getGenerateType().equalsIgnoreCase(AppConstants.LABEL_PRINTING_PDF.getString())){
	        	getFileNameAndSetFileLocations(".pdf");
	            fileName = labelPrintingService.generatePDFLabels(trialInstances,
                        userLabelPrinting, baos);
	        } else if (this.userLabelPrinting.getGenerateType().equalsIgnoreCase(AppConstants.LABEL_PRINTING_EXCEL.getString())) {
	        	getFileNameAndSetFileLocations(".xls");
	            fileName = labelPrintingService.generateXlSLabels(trialInstances,
                        userLabelPrinting, baos);
	        } else {
	        	getFileNameAndSetFileLocations(".csv");
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

		return new ArrayList<>();
	}

	@ResponseBody
	@RequestMapping(value = "/presets/{presetType}/{presetId}", method = RequestMethod.GET, produces = "application/json")
	public LabelPrintingSetting getLabelPrintingSetting(@PathVariable int presetType,
			@PathVariable int presetId,HttpServletRequest request) {
		try {
            final Unmarshaller parseXML = JAXBContext.newInstance(LabelPrintingSetting.class)
					.createUnmarshaller();

			// retrieve appropriate setting
			String xmlToRead = labelPrintingService.getLabelPrintingPresetConfig(presetId,presetType);

			return (LabelPrintingSetting) parseXML
					.unmarshal(new StringReader(xmlToRead));

		} catch (JAXBException e) {
			LOG.error(e.getMessage(), e);
		} catch (LabelPrintingException e) {
			LOG.error(e.getMessage(), e);
		}

		return null;
	}

    /**
     *  Search program-preset,
     * @param presetName
     * @param request
     * @return list of presets that matches presetName
     */
    @ResponseBody
    @RequestMapping(value = "/presets/searchLabelPrintingPresetByName", method = RequestMethod.GET)
    public List<LabelPrintingPresets> searchLabelPrintingPresetByName(@RequestParam("name") String presetName,HttpServletRequest request) {
        ContextInfo contextInfo = (ContextInfo) WebUtils
                .getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

        try {

            List<LabelPrintingPresets> standardPresets = labelPrintingService.getAllLabelPrintingPresetsByName(presetName,contextInfo.getSelectedProjectId().intValue(),LabelPrintingPresets.STANDARD_PRESET);

            if (!standardPresets.isEmpty()) {
                return standardPresets;
            }
            else {
                return labelPrintingService.getAllLabelPrintingPresetsByName(presetName,contextInfo.getSelectedProjectId().intValue(),LabelPrintingPresets.PROGRAM_PRESET);
            }
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(),e);
            return new ArrayList<>();
        }

    }

    /**
     * Delete's program preset
     * @param programPresetId
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/presets/delete", method= RequestMethod.GET)
    public Boolean deleteLabelPrintingPreset(@RequestParam("programPresetId") Integer programPresetId) {

        try {
            labelPrintingService.deleteProgramPreset(programPresetId);

            return true;

        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(),e);
        }

        return false;
    }

    /**
     * Saves the label printing setting.
     * Note that the fields should be pre-validated before calling this service
     * @param labelPrintingPresetSetting
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/presets/save", method = RequestMethod.POST)
    public Boolean saveLabelPrintingSetting(@ModelAttribute("labelPrintingForm") LabelPrintingForm labelPrintingPresetSetting,HttpServletRequest request) {
        UserLabelPrinting rawSettings = labelPrintingPresetSetting.getUserLabelPrinting();

        // save or update
        try {
            final ContextInfo contextInfo = (ContextInfo) WebUtils
                    .getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

            labelPrintingService.saveOrUpdateLabelPrintingPresetConfig(rawSettings.getSettingsName(),this.transformLabelPrintingSettingsToXML(rawSettings),contextInfo.getSelectedProjectId().intValue());

        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(),e);
            return false;
        }

        return true;
    }
    @ResponseBody
    @RequestMapping(value = "/presets/isModified/{presetType}/{presetId}", method = RequestMethod.POST)
    public Boolean isLabelPrintingIsModified(@ModelAttribute("labelPrintingForm") LabelPrintingForm labelPrintingPresetSetting,@PathVariable Integer presetType, @PathVariable Integer presetId,HttpServletRequest request) {
        LabelPrintingSetting lbSetting = this.getLabelPrintingSetting(presetType,presetId,request);
        LabelPrintingSetting modifiedSetting;
        final Unmarshaller parseXML;
        try {

            parseXML = JAXBContext.newInstance(LabelPrintingSetting.class)
					.createUnmarshaller();

            // retrieve appropriate setting
            String xmlToRead = transformLabelPrintingSettingsToXML(
                    labelPrintingPresetSetting.getUserLabelPrinting());

            modifiedSetting = (LabelPrintingSetting) parseXML
                    .unmarshal(new StringReader(xmlToRead));


            return !modifiedSetting.equals(lbSetting);

        } catch (JAXBException e) {
            LOG.error(e.getMessage(),e);
        }

        return false;
    }


    /**
     *
     * @param rawSettings
     * @return
     */
    private String transformLabelPrintingSettingsToXML(UserLabelPrinting rawSettings) {
        //Preparation, convert the form into appropriate pojos for easy access
        CSVExcelLabelPrintingSetting nonPDFSettings = null;
        PDFLabelPrintingSetting pdfSettings = null;
        BarcodeLabelPrintingSetting barcodeSettings = new BarcodeLabelPrintingSetting("1".equals(rawSettings.getBarcodeNeeded()),"Barcode",
                StringUtil.stringify(new String[]{rawSettings.getFirstBarcodeField(),rawSettings.getSecondBarcodeField(),rawSettings.getThirdBarcodeField()},","));

        String[] generatedTypes = {"PDF","EXCEL","CSV"};

        //FIXME the constant: is PDF
        if ("1".equals(rawSettings.getGenerateType())) {
            pdfSettings = new PDFLabelPrintingSetting(rawSettings.getSizeOfLabelSheet(),Integer.parseInt(rawSettings.getNumberOfRowsPerPageOfLabel(),10),rawSettings.getLeftSelectedLabelFields(),rawSettings.getRightSelectedLabelFields());
        } else {
            nonPDFSettings = new CSVExcelLabelPrintingSetting("1".equals(rawSettings.getIncludeColumnHeadinginNonPdf()),rawSettings.getMainSelectedLabelFields());
        }

        // get the xml value
        String xmlConfig = "";
        try {
            xmlConfig = this.generateXMLFromLabelPrintingSettings(rawSettings.getSettingsName(),generatedTypes[Integer.valueOf(rawSettings.getGenerateType(),10) - 1],nonPDFSettings,pdfSettings,barcodeSettings);
        } catch (JAXBException e) {
            LOG.error(e.getMessage(), e);
        }

        return xmlConfig;
    }

    private String generateXMLFromLabelPrintingSettings(String name, String outputType, CSVExcelLabelPrintingSetting csvSettings,PDFLabelPrintingSetting pdfSettings,BarcodeLabelPrintingSetting barcodeSettings)
            throws JAXBException {
        LabelPrintingSetting labelPrintingSetting = new LabelPrintingSetting(name, outputType,
                csvSettings, pdfSettings, barcodeSettings);

        JAXBContext context = JAXBContext.newInstance(LabelPrintingSetting.class);
        Marshaller marshaller = context.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(labelPrintingSetting, writer);

        return writer.toString();
    }

    private String getFileNameAndSetFileLocations(String extension) {
    	String fileName = this.userLabelPrinting.getFilename().replaceAll(" ",  "-") + extension;
    	String fileNameLocation  = System.getProperty( "user.home" ) + "/"+fileName;

        this.userLabelPrinting.setFilenameDL(fileName);
        this.userLabelPrinting.setFilenameDLLocation(fileNameLocation);
    	return fileName;
	}

	/**
     * Generate trial instances from field map.
     *
     * @return the list
     */
    private List<StudyTrialInstanceInfo> generateTrialInstancesFromFieldMap() {
        List<FieldMapTrialInstanceInfo> trialInstances =
                this.userLabelPrinting.getFieldMapInfo().getDatasets().get(0).getTrialInstances();
        List<StudyTrialInstanceInfo> studyTrial = new ArrayList<>();

        for (FieldMapTrialInstanceInfo trialInstance : trialInstances) {
            StudyTrialInstanceInfo studyTrialInstance = new StudyTrialInstanceInfo(
                        trialInstance, this.userLabelPrinting.getFieldMapInfo().getFieldbookName());
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
     * Sets the user label printing.
     *
     * @param userLabelPrinting the new user label printing
     */
    public void setUserLabelPrinting(UserLabelPrinting userLabelPrinting) {
        this.userLabelPrinting = userLabelPrinting;
    }
}