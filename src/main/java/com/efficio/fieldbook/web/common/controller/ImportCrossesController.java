
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ImportCrossesForm;
import com.efficio.fieldbook.web.common.service.CrossingService;

@Controller
@RequestMapping(ImportCrossesController.URL)
public class ImportCrossesController extends AbstractBaseFieldbookController {

	public static final String URL = "/import/crosses";

	@Resource
	private UserSelection studySelection;

	@Resource
	private CrossingService crossingService;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/germplasm", method = RequestMethod.POST, produces = "application/json")
	public Map<String, Object> importFile(Model model, @ModelAttribute("importCrossesForm") ImportCrossesForm form) {

		Map<String, Object> resultsMap = new HashMap<>();

		// 1. PARSE the file into an ImportCrosses List 
		ImportedCrossesList parseResults = null;
		try {
			parseResults = this.crossingService.parseFile(form.getFile());

			this.studySelection.setimportedCrossesList(parseResults);

			resultsMap.put("isSuccess", 1);
		} catch (FileParsingException e) {
			resultsMap.put("isSuccess", 0);
			// error messages is still in .prop format,
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/getImportedCrossesList", method = RequestMethod.GET)
	public List<Map<String, Object>> getImportedCrossesList() {

		List<Map<String, Object>> masterList = new ArrayList<>();

		if (null == this.studySelection.getImportedCrossesList()) {
			return masterList;
		}

		for (ImportedCrosses cross : this.studySelection.getImportedCrossesList().getImportedCrosses()) {
			masterList.add(this.generateDatatableDataMap(cross));
		}

		return masterList;
	}

	protected Map<String, Object> generateDatatableDataMap(ImportedCrosses importedCrosses) {

		Map<String, Object> dataMap = new HashMap<>();

		dataMap.put(this.getTermNameFromOntology(ColumnLabels.ENTRY_ID), importedCrosses.getEntryId());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.PARENTAGE), importedCrosses.getCross());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE), importedCrosses.getEntryCode());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.FEMALE_PARENT), importedCrosses.getFemaleDesig());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.FGID), importedCrosses.getFemaleGid());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.MALE_PARENT), importedCrosses.getMaleDesig());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.MGID), importedCrosses.getMaleGid());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE), importedCrosses.getSource());

		return dataMap;
	}

	protected String getTermNameFromOntology(ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	public String show(Model model, boolean isTrial) {
		this.setupModelInfo(model);
		model.addAttribute(AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, this.getContentName(isTrial));
		return AbstractBaseFieldbookController.BASE_TEMPLATE_NAME;
	}

	private String getContentName(boolean isTrial) {
		return isTrial ? "TrialManager/openTrial" : "NurseryManager/addOrRemoveTraits";
	}

	public void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

}
