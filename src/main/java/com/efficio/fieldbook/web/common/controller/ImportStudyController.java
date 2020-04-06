
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
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
@RequestMapping(ImportStudyController.URL)
@Deprecated
public class ImportStudyController extends AbstractBaseFieldbookController {

	private static final String SUCCESS = "success";
	public static final String URL = "/ImportManager";

	@Resource
	private UserSelection studySelection;

	@Override
	public String getContentName() {
		return null;
	}


	private UserSelection getUserSelection() {
		return this.studySelection;
	}

	@ResponseBody
	@RequestMapping(value = "/revert/data", method = RequestMethod.GET)
	public Map<String, Object> revertData(@ModelAttribute("createTrialForm") final CreateTrialForm form,
			final Model model) {

		this.doRevertData(form);

		final Map<String, Object> result = new HashMap<>();
		result.put(ImportStudyController.SUCCESS, "1");
		return result;
	}

	private void doRevertData(final CreateTrialForm form) {
		final UserSelection userSelection = this.getUserSelection();
		// we should remove here the newly added traits
		final List<MeasurementVariable> newVariableList = new ArrayList<>(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
		form.setMeasurementVariables(newVariableList);
		final List<MeasurementRow> list = new ArrayList<>();
		if (userSelection.getWorkbook().getOriginalObservations() != null) {
			for (final MeasurementRow row : userSelection.getWorkbook().getOriginalObservations()) {
				list.add(row.copy());
			}
		}
		userSelection.getWorkbook().setObservations(list);
		userSelection.setMeasurementRowList(list);

		WorkbookUtil.revertImportedConditionAndConstantsData(userSelection.getWorkbook());
	}

}
