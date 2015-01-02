package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.trial.bean.TrialSettingsBean;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 1/2/2015
 * Time: 9:48 AM
 */
public class SettingsServiceImpl implements SettingsService {
	@Override public List<SettingDetail> retrieveTrialSettings(Workbook workbook) {
		List<SettingDetail> details = new ArrayList<>();
		FieldbookUtil util = FieldbookUtil.getInstance();

		List<Integer> hiddenFields = util.buildVariableIDList(
				AppConstants.HIDE_TRIAL_VARIABLE_DBCV_FIELDS.getString());
		List<Integer> basicDetailIDList = util.buildVariableIDList(
				AppConstants.HIDE_TRIAL_FIELDS.getString());
		List<MeasurementVariable> measurementVariables = workbook.getStudyConditions();
		HashMap<String, MeasurementVariable> settingsMap = SettingsUtil
				.buildMeasurementVariableMap(measurementVariables);
		for (MeasurementVariable var : measurementVariables) {
			if (!basicDetailIDList.contains(var.getTermId())) {
				SettingDetail detail = createSettingDetail(var.getTermId(), var.getName());
				detail.setDeletable(true);
				details.add(detail);

				if (hiddenFields.contains(var.getTermId())) {
					detail.setHidden(true);
				} else {
					detail.setHidden(false);
				}

				//set local name of id variable to local name of name variable
				String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(),
						AppConstants.ID_NAME_COMBINATION.getString());
				if (settingsMap.get(nameTermId) != null) {
					detail.getVariable().setName(settingsMap.get(nameTermId).getName());
				}
			}
		}

		return details;
	}

	protected SettingDetail createSettingDetail(int id, String name, boolean includePossibleValues) throws
				MiddlewareQueryException {
	            String variableName = "";
	            StandardVariable stdVar = getStandardVariable(id);
	            if (name != null && !name.isEmpty()) {
	                variableName = name;
	            } else {
	                variableName = stdVar.getName();
	            }

	            if (stdVar != null && stdVar.getName() != null) {
	                SettingVariable svar = new SettingVariable(
	                        variableName, stdVar.getDescription(), stdVar.getProperty().getName(),
	                        stdVar.getScale().getName(), stdVar.getMethod().getName(), stdVar.getStoredIn().getName(),
	                        stdVar.getDataType().getName(), stdVar.getDataType().getId(),
	                        stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints().getMinValue() : null,
	                        stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints().getMaxValue() : null);
	                svar.setCvTermId(stdVar.getId());
	                svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
	                svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
	                svar.setOperation(Operation.ADD);

	                    List<ValueReference> possibleValues = fieldbookService.getAllPossibleValues(id);
	                    SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
	                    if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
	                        settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
	                    } else if (id == TermId.STUDY_UID.getId()) {
	                        settingDetail.setValue(this.getCurrentIbdbUserId().toString());
	                    } else if (id == TermId.STUDY_UPDATE.getId()) {
	                        DateFormat dateFormat = new SimpleDateFormat(DateUtil.DB_DATE_FORMAT);
	                        Date date = new Date();
	                        settingDetail.setValue(dateFormat.format(date));
	                    }
	                    settingDetail.setPossibleValuesToJson(possibleValues);
	                    List<ValueReference> possibleValuesFavorite = fieldbookService.getAllPossibleValuesFavorite(id, this.getCurrentProjectId());
	                    settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
	                    settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
	                    return settingDetail;
	            } else {
	                SettingVariable svar = new SettingVariable();
	                svar.setCvTermId(stdVar.getId());
	                return new SettingDetail(svar, null, null, false);
	            }
	    }
}
