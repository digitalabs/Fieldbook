package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ImportCrossesForm;
import com.efficio.fieldbook.web.nursery.bean.ImportedCrosses;
import com.efficio.fieldbook.web.nursery.bean.ImportedCrossesList;
import com.efficio.fieldbook.web.nursery.service.CrossingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping(ImportCrossesController.URL)
public class ImportCrossesController extends AbstractBaseFieldbookController {

	public static final String URL = "/import/crosses";
	private static final Logger LOG = LoggerFactory.getLogger(ImportCrossesController.class);
	@Resource
	private UserSelection studySelection;

	@Resource
	private CrossingService crossingService;

	/**
	 * The message source.
	 */
	@Resource
	private ResourceBundleMessageSource messageSource;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/germplasm", method = RequestMethod.POST, produces = "application/json")
	public Map<String, Object> importFile(Model model,
			@ModelAttribute("importCrossesForm") ImportCrossesForm form) {

		Map<String, Object> resultsMap = new HashMap<>();

		// 1. PARSE the file into an ImportCrosses List REF: deprecated: CrossingManagerUploader.java
		ImportedCrossesList parseResults = crossingService.parseFile(form.getFile());

		// 2. Store the crosses to study selection if all validated
		if (parseResults.getErrorMessages().isEmpty()) {
			studySelection.setimportedCrossesList(parseResults);

			resultsMap.put("isSuccess", 1);

		} else {
			resultsMap.put("isSuccess", 0);

			// error messages is still in .prop format,
			Set<String> errorMessages = new HashSet<>();
			for (String error : parseResults.getErrorMessages()) {
				errorMessages.add(messageSource.getMessage(error,new String[]{},error,
						LocaleContextHolder.getLocale()));
			}

			resultsMap.put("error",errorMessages);
		}

		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/getImportedCrossesList", method = RequestMethod.GET)
	public List<Map<String, Object>> getImportedCrossesList() {

		List<Map<String, Object>> masterList = new ArrayList<>();

		if (null == studySelection.getImportedCrossesList()) {
			return masterList;
		}

		for (ImportedCrosses cross : studySelection.getImportedCrossesList().getImportedCrosses()) {
			masterList.add(generateDatatableDataMap(cross));
		}

		return masterList;
	}

	protected Map<String, Object> generateDatatableDataMap(ImportedCrosses importedCrosses) {

		Map<String, Object> dataMap = new HashMap<>();

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

	public String show(Model model, boolean isTrial) {
		setupModelInfo(model);
		model.addAttribute(TEMPLATE_NAME_ATTRIBUTE, getContentName(isTrial));
		return BASE_TEMPLATE_NAME;
	}

	private String getContentName(boolean isTrial) {
		return isTrial ? "TrialManager/openTrial" : "NurseryManager/addOrRemoveTraits";
	}

}