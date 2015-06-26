
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 1/2/2015 Time: 9:48 AM
 */
public class SettingsServiceImpl implements SettingsService {

	private static final Logger LOG = LoggerFactory.getLogger(SettingsServiceImpl.class);
	

	/**
	 * The fieldbook service.
	 */
	@Resource
	protected FieldbookService fieldbookService;

	/**
	 * The fieldbook middleware service.
	 */
	@Resource
	protected org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Override
	public List<SettingDetail> retrieveTrialSettings(Workbook workbook) {
		throw new UnsupportedOperationException("Currently in the works");
	}

	@Override
	public List<LabelFields> retrieveTrialSettingsAsLabels(Workbook workbook) {
		List<LabelFields> details = new ArrayList<>();
		FieldbookUtil util = FieldbookUtil.getInstance();

		List<Integer> hiddenFields = util.buildVariableIDList(AppConstants.HIDE_TRIAL_VARIABLE_DBCV_FIELDS.getString());
		List<Integer> basicDetailIDList = util.buildVariableIDList(AppConstants.HIDE_TRIAL_FIELDS.getString());
		List<MeasurementVariable> measurementVariables = workbook.getStudyConditions();
		Map<String, MeasurementVariable> settingsMap = SettingsUtil.buildMeasurementVariableMap(measurementVariables);
		for (MeasurementVariable var : measurementVariables) {
			if (!basicDetailIDList.contains(var.getTermId()) && !hiddenFields.contains(var.getTermId())) {
				LabelFields field =
						new LabelFields(var.getName(), var.getTermId(), this.isGermplasmListField(var.getTermId(), workbook.isNursery()));

				// set local name of id variable to local name of name variable
				String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
				if (settingsMap.get(nameTermId) != null) {
					field.setName(settingsMap.get(nameTermId).getName());
				}

				details.add(field);
			}
		}

		return details;
	}

	public boolean isGermplasmListField(Integer id, boolean isNursery) {

		List<StandardVariableReference> stdVars = new ArrayList<StandardVariableReference>();
		try {
			if (isNursery) {
				stdVars =
						this.fieldbookService.filterStandardVariablesForSetting(VariableType.GERMPLASM_DESCRIPTOR.getId(),
								new ArrayList<SettingDetail>());
				stdVars.addAll(
						this.fieldbookService.filterStandardVariablesForSetting(VariableType.EXPERIMENTAL_DESIGN.getId(),
								new ArrayList<SettingDetail>()));
			} else {
				stdVars =
						this.fieldbookService.filterStandardVariablesForTrialSetting(VariableType.GERMPLASM_DESCRIPTOR.getId(),
								new ArrayList<SettingDetail>());
			}

		} catch (MiddlewareQueryException e) {
			SettingsServiceImpl.LOG.error(e.getMessage(), e);
		}

		List<Integer> germplasmDescriptorIds = new ArrayList<Integer>();
		for (StandardVariableReference stdVar : stdVars) {
			germplasmDescriptorIds.add(stdVar.getId());
		}

		if (germplasmDescriptorIds.contains(id)) {
			return true;
		}

		return false;
	}

	@Override
	public List<LabelFields> retrieveNurseryManagementDetailsAsLabels(Workbook workbook) {
		List<LabelFields> details = new ArrayList<>();
		FieldbookUtil util = FieldbookUtil.getInstance();

		List<Integer> hiddenFields = util.buildVariableIDList(AppConstants.NURSERY_BASIC_DETAIL_FIELDS_HIDDEN_LABELS.getString());
		List<MeasurementVariable> measurementVariables = workbook.getStudyConditions();
		measurementVariables.addAll(workbook.getTrialConditions());
		Map<String, MeasurementVariable> settingsMap = SettingsUtil.buildMeasurementVariableMap(measurementVariables);
		for (MeasurementVariable var : measurementVariables) {
			if (!hiddenFields.contains(var.getTermId())) {
				LabelFields field =
						new LabelFields(var.getName(), var.getTermId(), this.isGermplasmListField(var.getTermId(), workbook.isNursery()));

				// set local name of id variable to local name of name variable
				String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
				if (settingsMap.get(nameTermId) != null) {
					field.setName(settingsMap.get(nameTermId).getName());
				}

				details.add(field);
			}
		}

		return details;
	}

	@Override
	public List<LabelFields> retrieveTraitsAsLabels(Workbook workbook) {
		List<LabelFields> traitList = new ArrayList<>();
		for (MeasurementVariable var : workbook.getVariates()) {
			traitList
					.add(new LabelFields(var.getName(), var.getTermId(), this.isGermplasmListField(var.getTermId(), workbook.isNursery())));
		}

		return traitList;
	}

	@Override
	public List<LabelFields> retrieveGermplasmDescriptorsAsLabels(Workbook workbook) {
		List<LabelFields> detailList = new ArrayList<>();
		FieldbookUtil util = FieldbookUtil.getInstance();

		List<Integer> experimentalDesignVariables = util.buildVariableIDList(AppConstants.EXP_DESIGN_REQUIRED_VARIABLES.getString());

		for (MeasurementVariable var : workbook.getFactors()) {
			// this condition is required so that treatment factors are not included in the list of factors for the germplasm tab
			if (var.getTreatmentLabel() != null && !var.getTreatmentLabel().isEmpty()
					|| experimentalDesignVariables.contains(var.getTermId()) || var.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				continue;
			}

			// set all variables with trial design role to hidden
			if (var.getRole() != PhenotypicType.TRIAL_DESIGN) {

				LabelFields field =
						new LabelFields(var.getName(), var.getTermId(), this.isGermplasmListField(var.getTermId(), workbook.isNursery()));
				detailList.add(field);
			}

		}

		return detailList;
	}

	@Override
	public List<LabelFields> retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(Workbook workbook) {

		List<LabelFields> managementDetailList = new ArrayList<>();
		FieldbookUtil util = FieldbookUtil.getInstance();
		List<Integer> hiddenFields = util.buildVariableIDList(AppConstants.HIDE_TRIAL_VARIABLE_DBCV_FIELDS.getString());

		Map<String, MeasurementVariable> factorsMap = SettingsUtil.buildMeasurementVariableMap(workbook.getTrialConditions());

		for (MeasurementVariable var : workbook.getTrialConditions()) {

			if (!hiddenFields.contains(var.getTermId()) && TermId.EXPERIMENT_DESIGN_FACTOR.getId() != var.getTermId()) {
				LabelFields field =
						new LabelFields(var.getName(), var.getTermId(), this.isGermplasmListField(var.getTermId(), workbook.isNursery()));

				// set local name of id variable to local name of name variable
				String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
				if (factorsMap.get(nameTermId) != null) {
					field.setName(factorsMap.get(nameTermId).getName());
				}
				managementDetailList.add(field);
			}

		}

		return managementDetailList;
	}

}
