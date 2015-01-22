package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ImportCrossesForm;
import com.efficio.fieldbook.web.common.service.FieldroidImportStudyService;
import com.efficio.fieldbook.web.nursery.bean.ImportedCrosses;

@Controller
@RequestMapping(ImportCrossesController.URL)
public class ImportCrossesController extends AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(ImportCrossesController.class);

    public static final String URL = "/Common/ImportCrosses";

    @Resource
    private UserSelection studySelection;
    
    @Resource
    private FileService fileService;
    
    @Resource
    private FieldroidImportStudyService fieldroidImportStudyService;
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private OntologyService ontologyService;

    
    /** The message source. */
    @Resource
    private ResourceBundleMessageSource messageSource;
    

    @Resource
    private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
    
    @Override
	public String getContentName() {
		return null;
	}
    
    @ResponseBody
    @RequestMapping(value="/germplasm", method = RequestMethod.POST)
    public String importFile(Model model, @ModelAttribute("importCrossesForm") ImportCrossesForm form) {

    	Map<String, Object> resultsMap = new HashMap<String,Object>();
    	resultsMap.put("isSuccess", "1");
    	return super.convertObjectToJson(resultsMap);
    }

    
    @ResponseBody
    @RequestMapping(value="/data/table/ajax", method = RequestMethod.GET)
    public List<Map<String, Object>> getPageDataTablesAjax() {
    	
    	List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();
    	
    	List<ImportedCrosses> importedCrossesList = new ArrayList<>();
    	
    	for (int x = 0; x < 300; x++){
    		ImportedCrosses test = new ImportedCrosses();
        	test.setEntryId(x);
        	test.setEntryCode(String.valueOf(x));
        	test.setFemaleDesig("FEMALE PARENT " + x);
        	test.setFemaleGid(String.valueOf(123456+x));
        	test.setMaleDesig("MALE PARENT " + x);
        	test.setMaleGid(String.valueOf(654321+x));
        	test.setSource("FILENAME SOURCE");
        	
        	importedCrossesList.add(test);
    	}
    	
    	
    	
		for (ImportedCrosses importedCrosses : importedCrossesList) {
			Map<String, Object> dataMap = generateDatatableDataMap(importedCrosses);
			masterList.add(dataMap);
		}

		return masterList;
    }
        
    protected Map<String, Object> generateDatatableDataMap(ImportedCrosses importedCrosses) {

		Map<String, Object> dataMap = new HashMap<String, Object>();
	
		dataMap.put("ENTRY", importedCrosses.getEntryId());
		dataMap.put("PARENTAGE", importedCrosses.getCross());
		dataMap.put("ENTRY CODE", importedCrosses.getEntryCode());
		dataMap.put("FEMALE PARENT", importedCrosses.getFemaleDesig());
		dataMap.put("FGID", importedCrosses.getFemaleGid());
		dataMap.put("MALE PARENT", importedCrosses.getMaleDesig());
		dataMap.put("MGID", importedCrosses.getMaleGid());
		dataMap.put("SOURCE", importedCrosses.getSource());
		
		return dataMap;

	}
    
    private UserSelection getUserSelection(boolean isTrial) {
    	return this.studySelection;
    }
    
    public String show(Model model, boolean isTrial) {
        setupModelInfo(model);
        model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName(isTrial));        
        return BASE_TEMPLATE_NAME;
    }
    
    private String getContentName(boolean isTrial) {
    	return isTrial ? "TrialManager/openTrial" : "NurseryManager/addOrRemoveTraits";
    }
    
    
}
