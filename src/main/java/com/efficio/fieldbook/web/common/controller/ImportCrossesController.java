
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ImportCrossesForm;
import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.util.CrossesListUtil;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(ImportCrossesController.URL)
public class ImportCrossesController extends AbstractBaseFieldbookController {

	public static final String URL = "/import/crosses";

	@Resource
	private UserSelection studySelection;

	@Resource
	private CrossingService crossingService;

	@Resource
	private CrossesListUtil crossesListUtil;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/germplasm", method = RequestMethod.POST, produces = "application/json")
	public Map<String, Object> importFile(final Model model, @ModelAttribute("importCrossesForm") final ImportCrossesForm form) {

		final Map<String, Object> resultsMap = new HashMap<>();

		// 1. PARSE the file into an ImportCrosses List 
		ImportedCrossesList parseResults;
		try {
			parseResults = this.crossingService.parseFile(form.getFile());

			this.studySelection.setImportedCrossesList(parseResults);

			resultsMap.put("isSuccess", 1);
		} catch (final FileParsingException e) {
			resultsMap.put("isSuccess", 0);
			// error messages is still in .prop format,
			resultsMap.put("error", new String[] {e.getMessage()});
		}

		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/getImportedCrossesList", method = RequestMethod.GET)
	public List<Map<String, Object>> getImportedCrossesList() {

		final List<Map<String, Object>> masterList = new ArrayList<>();

		if (null == this.studySelection.getImportedCrossesList()) {
			return masterList;
		}

		for (final ImportedCrosses cross : this.studySelection.getImportedCrossesList().getImportedCrosses()) {
			masterList.add(crossesListUtil.generateCrossesTableDataMap(cross));
		}

		return masterList;
	}

	public void setCrossesListUtil(final CrossesListUtil crossesListUtil) {
		this.crossesListUtil = crossesListUtil;
	}
}
