
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
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
	public List<SettingDetail> retrieveTrialSettings(Workbook workbook) {
		throw new UnsupportedOperationException("Currently in the works");
	}

	@Override
	public SettingDetail createSettingDetail(int id, String name, UserSelection userSelection, int currentIbDbUserId, String programUUID) throws MiddlewareException {
        
		String variableName;
		StandardVariable stdVar = this.getCachedStandardVariable(id, userSelection, programUUID);
		
         if (name != null && !name.isEmpty()) {
             variableName = name;
         } else {
             variableName = stdVar.getName();
         }
         
         if (stdVar != null && stdVar.getName() != null) {
			SettingVariable svar =
					new SettingVariable(variableName, stdVar.getDescription(), stdVar.getProperty().getName(), stdVar.getScale().getName(),
							stdVar.getMethod().getName(), null, stdVar.getDataType().getName(), stdVar
									.getDataType().getId(), stdVar.getConstraints() != null
									&& stdVar.getConstraints().getMinValue() != null ? stdVar.getConstraints().getMinValue() : null,
							stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ? stdVar.getConstraints()
									.getMaxValue() : null);
             svar.setCvTermId(stdVar.getId());
             svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
             svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
             svar.setOperation(Operation.ADD);

			List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(id);
                 SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
                 if (id == TermId.BREEDING_METHOD_ID.getId() || id == TermId.BREEDING_METHOD_CODE.getId()) {
                     settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
                 } else if (id == TermId.STUDY_UID.getId()) {
                     settingDetail.setValue(String.valueOf(currentIbDbUserId));
                 } else if (id == TermId.STUDY_UPDATE.getId()) {
                     settingDetail.setValue(DateUtil.getCurrentDateAsStringValue());
                 }
                 settingDetail.setPossibleValuesToJson(possibleValues);
			List<ValueReference> possibleValuesFavorite =
					this.fieldbookService.getAllPossibleValuesFavorite(id, programUUID);
                 settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
                 settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                 return settingDetail;
         } else {
             SettingVariable svar = new SettingVariable();
             svar.setCvTermId(stdVar.getId());
             return new SettingDetail(svar, null, null, false);
         }
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

		try {
			StandardVariable stdVar = this.fieldbookMiddlewareService.getStandardVariable(id,  contextUtil.getCurrentProgramUUID());
			if (isNursery && (SettingsUtil.hasVariableType(VariableType.GERMPLASM_DESCRIPTOR,
						stdVar.getVariableTypes()) || SettingsUtil.hasVariableType(VariableType.EXPERIMENTAL_DESIGN,
								stdVar.getVariableTypes()))){
					return true;
			} else if(!isNursery && SettingsUtil.hasVariableType(VariableType.GERMPLASM_DESCRIPTOR,
						stdVar.getVariableTypes())){
					return true;
				
			}

		} catch (MiddlewareException e) {
			SettingsServiceImpl.LOG.error(e.getMessage(), e);
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

	 /**
     * Get standard variable.
     *
     * @param id the id
     * @return the standard variable
     * @throws MiddlewareQueryException the middleware query exception
     */
    protected StandardVariable getCachedStandardVariable(int id, UserSelection userSelection, String programUUID) throws MiddlewareException {
		StandardVariable variable = userSelection.getCacheStandardVariable(id);
    	if (variable == null) {
			variable = this.fieldbookMiddlewareService.getStandardVariable(id, programUUID);
    		if (variable != null) {
				userSelection.putStandardVariableInCache(variable);
    		}
    	}
    	
    	return variable;
    }
}
