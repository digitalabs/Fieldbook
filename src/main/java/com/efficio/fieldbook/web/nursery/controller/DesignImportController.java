package com.efficio.fieldbook.web.nursery.controller;

/**
 * Created by cyrus on 5/8/15.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.form.ImportDesignForm;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.util.parsing.DesignImportParser;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The Class DesignImportController.
 */
@Controller
@RequestMapping(DesignImportController.URL)
public class DesignImportController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(DesignImportController.class);
	
	public static final String URL = "/DesignImport";
	public static final String REVIEW_DETAILS_PAGINATION_TEMPLATE = "/DesignImport/reviewDetailsPagination";

	@Resource
	private DesignImportParser parser;
	
	@Resource
	private UserSelection studySelection;
	
	@Resource
	private DesignImportService designImportService;
	
	/* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#show(org.springframework.ui.Model)
     */
	@RequestMapping(method = RequestMethod.GET)
	public String show(Model model) {
		return super.showAngularPage(model);
	}

	@Override public String getContentName() {
		return String.format("%s/designImportMain",URL);
	}


	@ResponseBody
	@RequestMapping(value = "/import", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	public Map<String, Object> importFile(Model model,
			@ModelAttribute("importDesignForm") ImportDesignForm form) {

		Map<String, Object> resultsMap = new HashMap<>();
		
		try {
			
			DesignImportData designImportData = parser.parseFile(form.getFile());
			
			studySelection.setDesignImportData(designImportData);
			
			List<MeasurementRow> measurementRows = designImportService.generateDesign();
			
			resultsMap.put("isSuccess", 1);
			
		} catch (Exception e) {
			
			LOG.error(e.getMessage(), e);
			
			resultsMap.put("isSuccess", 0);
			// error messages is still in .prop format,
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		return resultsMap;
	}
	
	@RequestMapping(value = "/showDetails", method = RequestMethod.GET)
	public String showDetails(Model model) {
			
			List<MeasurementVariable> measurementVariables = designImportService.getDesignMeasurementVariables();
	    	
			model.addAttribute("measurementVariables", measurementVariables);
			
	    	return super.showAjaxPage(model, REVIEW_DETAILS_PAGINATION_TEMPLATE);
		
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/showDetails/data", method = RequestMethod.GET)
	public List<Map<String, Object>> showDetailsData(Model model,
			@ModelAttribute("importDesignForm") ImportDesignForm form) {
			
			List<MeasurementRow> measurementRows = new ArrayList<>();
			try {
				measurementRows = designImportService.generateDesign();
			} catch (DesignValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();
	    	
	    	for(MeasurementRow row : measurementRows){
	    		    		
	    		Map<String, Object> dataMap = generateDatatableDataMap(row, null);
	    		
	    		masterList.add(dataMap);
	    	}
	   	
	    	return masterList;
		
	}
	
	private Map<String, Object> generateDatatableDataMap(MeasurementRow row, String suffix){
    	Map<String, Object> dataMap = new HashMap<String, Object>();
    	//the 4 attributes are needed always
		for(MeasurementData data : row.getDataList()){
			String displayVal = data.getDisplayValue();
			if(suffix != null) {
				displayVal += suffix;
			}
			
			if (data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId()) ||
					data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())){
				Object[] categArray = new Object[] {displayVal, data.isAccepted()};
				dataMap.put(data.getMeasurementVariable().getName(), categArray);
			} else{
				dataMap.put(data.getMeasurementVariable().getName(), displayVal);
			}
		}
		return dataMap;
    }
}
