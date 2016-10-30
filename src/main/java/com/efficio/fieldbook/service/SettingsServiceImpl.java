
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
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
	@Resource
	private ContextUtil contextUtil;

	/**
	 * The fieldbook middleware service.
	 */
	@Resource
	protected org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Override
	public List<SettingDetail> retrieveTrialSettings(final Workbook workbook) {
		throw new UnsupportedOperationException("Currently in the works");
	}

	@Override
	public SettingDetail createSettingDetail(final int id, final String name, final UserSelection userSelection,
			final int currentIbDbUserId, final String programUUID) throws MiddlewareException {

		String variableName;
		final StandardVariable stdVar = this.fieldbookMiddlewareService.getStandardVariable(id, programUUID);

		if (name != null && !name.isEmpty()) {
			variableName = name;
		} else {
			variableName = stdVar.getName();
		}

		if (stdVar != null && stdVar.getName() != null) {
			final SettingVariable svar =
					new SettingVariable(variableName, stdVar.getDescription(), stdVar.getProperty().getName(), stdVar.getScale().getName(),
							stdVar.getMethod().getName(), null, stdVar.getDataType().getName(), stdVar.getDataType().getId(),
							stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints()
									.getMinValue() : null,
							stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints()
									.getMaxValue() : null);
			svar.setCvTermId(stdVar.getId());
			svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
			svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
			svar.setOperation(Operation.ADD);

			final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(id);
			final SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
			if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
				settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
			} else if (id == TermId.STUDY_UID.getId()) {
				settingDetail.setValue(String.valueOf(currentIbDbUserId));
			} else if (id == TermId.STUDY_UPDATE.getId()) {
				settingDetail.setValue(DateUtil.getCurrentDateAsStringValue());
			}
			settingDetail.setPossibleValuesToJson(possibleValues);
			final List<ValueReference> possibleValuesFavorite = this.fieldbookService.getAllPossibleValuesFavorite(id, programUUID, false);
			settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
			settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
			return settingDetail;
		} else {
			final SettingVariable svar = new SettingVariable();
			svar.setCvTermId(stdVar.getId());
			return new SettingDetail(svar, null, null, false);
		}
	}

	@Override
	public List<LabelFields> retrieveTrialSettingsAsLabels(final Workbook workbook) {
		final List<LabelFields> details = new ArrayList<>();
		final FieldbookUtil util = FieldbookUtil.getInstance();

		final List<Integer> hiddenFields = util.buildVariableIDList(AppConstants.HIDE_TRIAL_VARIABLE_DBCV_FIELDS.getString());
		final List<Integer> basicDetailIDList = util.buildVariableIDList(AppConstants.HIDE_TRIAL_FIELDS.getString());
		final List<MeasurementVariable> measurementVariables = workbook.getStudyConditions();
		final Map<String, MeasurementVariable> settingsMap = SettingsUtil.buildMeasurementVariableMap(measurementVariables);
		for (final MeasurementVariable var : measurementVariables) {
			if (!basicDetailIDList.contains(var.getTermId()) && !hiddenFields.contains(var.getTermId())) {
				final LabelFields field =
						new LabelFields(var.getName(), var.getTermId(), this.isGermplasmListField(var.getTermId(), workbook.isNursery()));

				// set local name of id variable to local name of name variable
				final String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
				if (settingsMap.get(nameTermId) != null) {
					field.setName(settingsMap.get(nameTermId).getName());
				}

				details.add(field);
			}
		}

		return details;
	}

	public boolean isGermplasmListField(final Integer id, final boolean isNursery) {

		try {
			final StandardVariable stdVar =
					this.fieldbookMiddlewareService.getStandardVariable(id, this.contextUtil.getCurrentProgramUUID());
			if (isNursery
					&& (SettingsUtil.hasVariableType(VariableType.GERMPLASM_DESCRIPTOR, stdVar.getVariableTypes()) || SettingsUtil
							.hasVariableType(VariableType.EXPERIMENTAL_DESIGN, stdVar.getVariableTypes()))) {
				return true;
			} else if (!isNursery && SettingsUtil.hasVariableType(VariableType.GERMPLASM_DESCRIPTOR, stdVar.getVariableTypes())) {
				return true;

			}

		} catch (final MiddlewareException e) {
			SettingsServiceImpl.LOG.error(e.getMessage(), e);
		}

		return false;
	}

	@Override
	public List<LabelFields> retrieveNurseryManagementDetailsAsLabels(final Workbook workbook) {
		final List<LabelFields> details = new ArrayList<>();
		final FieldbookUtil util = FieldbookUtil.getInstance();

		final List<Integer> hiddenFields = util.buildVariableIDList(AppConstants.NURSERY_BASIC_DETAIL_FIELDS_HIDDEN_LABELS.getString());
		final List<MeasurementVariable> measurementVariables = workbook.getStudyConditions();
		measurementVariables.addAll(workbook.getTrialConditions());
		final Map<String, MeasurementVariable> settingsMap = SettingsUtil.buildMeasurementVariableMap(measurementVariables);
		for (final MeasurementVariable var : measurementVariables) {
			if (!hiddenFields.contains(var.getTermId())) {
				final LabelFields field =
						new LabelFields(var.getName(), var.getTermId(), this.isGermplasmListField(var.getTermId(), workbook.isNursery()));

				// set local name of id variable to local name of name variable
				final String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
				if (settingsMap.get(nameTermId) != null) {
					field.setName(settingsMap.get(nameTermId).getName());
				}

				details.add(field);
			}
		}

		return details;
	}

	@Override
	public List<LabelFields> retrieveTraitsAsLabels(final Workbook workbook) {
		final List<LabelFields> traitList = new ArrayList<>();
		for (final MeasurementVariable var : workbook.getVariates()) {
			traitList
					.add(new LabelFields(var.getName(), var.getTermId(), this.isGermplasmListField(var.getTermId(), workbook.isNursery())));
		}

		return traitList;
	}

	@Override
	public List<LabelFields> retrieveGermplasmDescriptorsAsLabels(final Workbook workbook) {
		final List<LabelFields> detailList = new ArrayList<>();
		final FieldbookUtil util = FieldbookUtil.getInstance();

		final List<Integer> expDesignVariablesMinusPlotNumber =
				util.buildVariableIDList(AppConstants.EXP_DESIGN_REQUIRED_WITHOUT_PLOT_NO_VARIABLES.getString());

		for (final MeasurementVariable var : workbook.getFactors()) {
			// don't include the following types as labels: treatment factor, trial instance, trial design except plot number
			if (var.getTreatmentLabel() != null && !var.getTreatmentLabel().isEmpty()
					|| expDesignVariablesMinusPlotNumber.contains(var.getTermId())
					|| var.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				continue;
			}

			// set all variables with trial design role to hidden except for PLOT_NO
			if (var.getTermId() == TermId.PLOT_NO.getId() || var.getRole() != PhenotypicType.TRIAL_DESIGN) {

				final LabelFields field =
						new LabelFields(var.getName(), var.getTermId(), this.isGermplasmListField(var.getTermId(), workbook.isNursery()));
				detailList.add(field);
			}

		}

		return detailList;
	}

	@Override
	public List<LabelFields> retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(final Workbook workbook) {

		final List<LabelFields> managementDetailList = new ArrayList<>();
		final FieldbookUtil util = FieldbookUtil.getInstance();
		final List<Integer> hiddenFields = util.buildVariableIDList(AppConstants.HIDE_TRIAL_VARIABLE_DBCV_FIELDS.getString());

		final Map<String, MeasurementVariable> factorsMap = SettingsUtil.buildMeasurementVariableMap(workbook.getTrialConditions());

		for (final MeasurementVariable var : workbook.getTrialConditions()) {

			if (!hiddenFields.contains(var.getTermId()) && TermId.EXPERIMENT_DESIGN_FACTOR.getId() != var.getTermId()) {
				final LabelFields field =
						new LabelFields(var.getName(), var.getTermId(), this.isGermplasmListField(var.getTermId(), workbook.isNursery()));

				// set local name of id variable to local name of name variable
				final String nameTermId = SettingsUtil.getNameCounterpart(var.getTermId(), AppConstants.ID_NAME_COMBINATION.getString());
				if (factorsMap.get(nameTermId) != null) {
					field.setName(factorsMap.get(nameTermId).getName());
				}
				managementDetailList.add(field);
			}

		}

		return managementDetailList;
	}
}
