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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.nursery.bean.NurseryDetails;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Controller
@RequestMapping(ReviewNurseryDetailsController.URL)
public class ReviewNurseryDetailsController extends AbstractBaseFieldbookController {

    public static final String URL = "/NurseryManager/reviewNurseryDetails";
    
    private static final Logger LOG = LoggerFactory.getLogger(ReviewNurseryDetailsController.class);

    @Resource
    private UserSelection userSelection;
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
    
    @Override
	public String getContentName() {
		return "NurseryManager/ver2.0/reviewNurseryDetails";
	}

    @RequestMapping(value = "/show/{id}", method = RequestMethod.GET)
    public String show(@PathVariable int id, @ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, Model model) throws MiddlewareQueryException {
    	
        if (id != 0) {     
            Workbook workbook = fieldbookMiddlewareService.getStudyVariableSettings(id, true);
            workbook.setStudyId(id);
            NurseryDetails details = SettingsUtil.convertWorkbookToNurseryDetails(workbook, fieldbookMiddlewareService, fieldbookService, userSelection);
            rearrangeDetails(details);
            
            model.addAttribute("nurseryDetails", details);
        }    	
    	return showAjaxPage(model, getContentName());
    }
    
    @ResponseBody
    @RequestMapping(value="/datasets/{nurseryId}")
    public List<DatasetReference> loadDatasets(@PathVariable int nurseryId) throws MiddlewareQueryException {
    	List<DatasetReference> datasetList = new ArrayList<DatasetReference>();
    	Database database = nurseryId > 0 ? Database.CENTRAL : Database.LOCAL;
    	StudyDetails studyDetails = fieldbookMiddlewareService.getStudyDetails(database, StudyType.N, nurseryId);
    	int datasetId = fieldbookMiddlewareService.getMeasurementDatasetId(nurseryId, studyDetails.getStudyName()); 
    	List<DatasetReference> datasets = fieldbookMiddlewareService.getDatasetReferences(nurseryId);
    	if (datasets != null && !datasets.isEmpty()) {
    		for (DatasetReference dataset : datasets) {
    			if (dataset.getId() == datasetId) {
    				datasetList.add(dataset);
    			}
    		}
    	}
    	return datasetList;
    }
    
    private void rearrangeDetails(NurseryDetails details) {
    	details.setBasicStudyDetails(rearrangeSettingDetails(details.getBasicStudyDetails()));
    	details.setManagementDetails(rearrangeSettingDetails(details.getManagementDetails()));
    }
    
    private List<SettingDetail> rearrangeSettingDetails(List<SettingDetail> list) {
    	List<SettingDetail> newList = new ArrayList<SettingDetail>();
    	final int COLS = 3;
    	
    	if (list != null && !list.isEmpty()) {
    		int rows = Double.valueOf(Math.ceil(list.size()/(double)COLS)).intValue();
    		for (int i = 0; i < list.size(); i++) {
    			int computedIndex = (i % COLS) * rows + i / COLS;
    			if (computedIndex < list.size()) {
    				newList.add(list.get(computedIndex));
    			}
    			else {
    				newList.add(list.get(computedIndex - 1));
    			}
    		}
    	}
    	return newList;
    }
    
}
