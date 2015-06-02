package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.controller.SettingsController;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.TreeViewUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * <p/>
 * This controller class handles back end functionality that are common to the operations that require management of settings (Create/Edit Nursery/Trial)
 */

@Controller
@RequestMapping(value = ManageSettingsController.URL)
public class ManageSettingsController extends SettingsController {
	public static final String URL = "/manageSettings";

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ManageSettingsController.class);

	@Resource
	private OntologyService ontologyService;

	/**
	 * Displays the Add Setting popup.
	 *
	 * @param mode the mode
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/displayAddSetting/{mode}", method = RequestMethod.GET)
	public Map<String, Object> showAddSettingPopup(@PathVariable int mode) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			List<StandardVariableReference> standardVariableList =
					fieldbookService
							.filterStandardVariablesForSetting(mode, getSettingDetailList(mode));

			try {
				// TODO : question when the trait ref list is set to null
				if (userSelection.getTraitRefList() == null) {
					List<TraitClassReference> traitRefList = ontologyService
							.getAllTraitGroupsHierarchy(true);
					userSelection.setTraitRefList(traitRefList);
				}

				List<TraitClassReference> traitRefList = userSelection.getTraitRefList();

				//we convert it to map so that it would be easier to chekc if there is a record or not
				Map<String, StandardVariableReference> mapVariableRef = new HashMap<String, StandardVariableReference>();
				if (standardVariableList != null && !standardVariableList.isEmpty()) {
					for (StandardVariableReference varRef : standardVariableList) {
						mapVariableRef.put(varRef.getId().toString(), varRef);
					}
				}

				// TODO : question purpose of mapVariableRef, as well as traitRefList
				String treeData = TreeViewUtil
						.convertOntologyTraitsToJson(traitRefList, mapVariableRef);
				String searchTreeData = TreeViewUtil
						.convertOntologyTraitsToSearchSingleLevelJson(traitRefList, mapVariableRef);
				result.put("treeData", treeData);
				result.put("searchTreeData", searchTreeData);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Adds the settings.
	 *
	 * @param form the form
	 * @param mode the mode
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/addSettings/{mode}", method = RequestMethod.POST)
	public List<SettingDetail> addSettings(@RequestBody CreateNurseryForm form,
			@PathVariable int mode) {
		List<SettingDetail> newSettings = new ArrayList<SettingDetail>();
		try {
			List<SettingVariable> selectedVariables = form.getSelectedVariables();
			if (selectedVariables != null && !selectedVariables.isEmpty()) {
				for (SettingVariable var : selectedVariables) {
					Operation operation = removeVarFromDeletedList(var, mode);

					var.setOperation(operation);
					populateSettingVariable(var);
					List<ValueReference> possibleValues =
							fieldbookService.getAllPossibleValues(var.getCvTermId());
					SettingDetail newSetting = new SettingDetail(var, possibleValues, null, true);
					List<ValueReference> possibleValuesFavorite = fieldbookService
							.getAllPossibleValuesFavorite(var.getCvTermId(),
									this.getCurrentProject().getUniqueID());
					newSetting.setPossibleValuesFavorite(possibleValuesFavorite);
					newSettings.add(newSetting);
				}
			}

			if (newSettings != null && !newSettings.isEmpty()) {
				addNewSettingDetails(mode, newSettings);
				return newSettings;
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return new ArrayList<SettingDetail>();
	}

	/**
	 * Adds the new setting details.
	 *
	 * @param mode       the mode
	 * @param newDetails the new details
	 * @return the string
	 * @throws Exception the exception
	 */
	private void addNewSettingDetails(int mode
			, List<SettingDetail> newDetails) throws Exception {
		
		SettingsUtil.addNewSettingDetails(mode, newDetails, userSelection);
		
	}

	private Operation removeVarFromDeletedList(SettingVariable var, int mode) {
		List<SettingDetail> settingsList = new ArrayList<SettingDetail>();
		if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
			settingsList = userSelection.getDeletedStudyLevelConditions();
		} else if (mode == AppConstants.SEGMENT_PLOT.getInt()
				|| mode == AppConstants.SEGMENT_GERMPLASM.getInt()) {
			settingsList = userSelection.getDeletedPlotLevelList();
		} else if (mode == AppConstants.SEGMENT_TRAITS.getInt()
				|| mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
			settingsList = userSelection.getDeletedBaselineTraitsList();
		} else if (mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
			settingsList = userSelection.getDeletedNurseryConditions();
		} else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
			settingsList = userSelection.getDeletedTreatmentFactors();
		} else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
			settingsList = userSelection.getDeletedTrialLevelVariables();
		}

		Operation operation = Operation.ADD;
		if (settingsList != null) {
			Iterator<SettingDetail> iter = settingsList.iterator();
			while (iter.hasNext()) {
				SettingVariable deletedVariable = iter.next().getVariable();
				if (deletedVariable.getCvTermId().equals(Integer.valueOf(var.getCvTermId()))) {
					operation = deletedVariable.getOperation();
					iter.remove();
				}
			}
		}
		return operation;
	}

	/**
	 * Gets the setting detail list.
	 *
	 * @param mode the mode
	 * @return the setting detail list
	 */
	private List<SettingDetail> getSettingDetailList(int mode) {
		if (mode == AppConstants.SEGMENT_STUDY.getInt()) {
			return userSelection.getStudyLevelConditions();
		} else if (mode == AppConstants.SEGMENT_PLOT.getInt()
				|| mode == AppConstants.SEGMENT_GERMPLASM.getInt()) {
			return userSelection.getPlotsLevelList();
		} else if (mode == AppConstants.SEGMENT_TRAITS.getInt()
				|| mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
			List<SettingDetail> newList = new ArrayList<SettingDetail>();

			if (userSelection.getBaselineTraitsList() != null) {
				for (SettingDetail setting : userSelection.getBaselineTraitsList()) {
					newList.add(setting);
				}
			}
			if (userSelection.getNurseryConditions() != null) {
				for (SettingDetail setting : userSelection.getNurseryConditions()) {
					newList.add(setting);
				}
			}
			return newList;
		} else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
			return userSelection.getSelectionVariates();
		} else if (mode == AppConstants.SEGMENT_TRIAL_ENVIRONMENT.getInt()) {
			return userSelection.getTrialLevelVariableList();
		} else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
			return userSelection.getTreatmentFactors();
		}
		return new ArrayList<SettingDetail>();
	}

	@ResponseBody
	@RequestMapping(value = "/deleteVariable/{mode}", method = RequestMethod.POST)
	public boolean deleteVariable(@PathVariable int mode, @RequestBody List<Integer> ids) {

		for (Integer id : ids) {
			this.deleteVariable(mode, id);
		}

		return true;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteVariable/{mode}/{variableId}", method = RequestMethod.POST)
	public String deleteVariable(@PathVariable int mode, @PathVariable int variableId) {
		Map<String, String> idNameRetrieveSaveMap = fieldbookService
				.getIdNamePairForRetrieveAndSave();
		if (mode == AppConstants.SEGMENT_STUDY.getInt()) {

			addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode, variableId,true);
			SettingsUtil.deleteVariableInSession(userSelection.getStudyLevelConditions(),
					variableId);
			if (idNameRetrieveSaveMap.get(variableId) != null) {
				//special case so we must delete it as well
				addVariableInDeletedList(userSelection.getStudyLevelConditions(), mode,
						Integer.parseInt(idNameRetrieveSaveMap.get(variableId)),true);
				SettingsUtil.deleteVariableInSession(userSelection.getStudyLevelConditions(),
						Integer.parseInt(idNameRetrieveSaveMap.get(variableId)));
			}
		} else if (mode == AppConstants.SEGMENT_PLOT.getInt()
				|| mode == AppConstants.SEGMENT_GERMPLASM.getInt()) {
			addVariableInDeletedList(userSelection.getPlotsLevelList(), mode, variableId,true);
			SettingsUtil.deleteVariableInSession(userSelection.getPlotsLevelList(), variableId);
		} else if (mode == AppConstants.SEGMENT_TRAITS.getInt()) {
			addVariableInDeletedList(userSelection.getBaselineTraitsList(), mode, variableId,true);
			SettingsUtil.deleteVariableInSession(userSelection.getBaselineTraitsList(), variableId);
		} else if (mode == AppConstants.SEGMENT_SELECTION_VARIATES.getInt()) {
			addVariableInDeletedList(userSelection.getSelectionVariates(), mode, variableId,true);
			SettingsUtil.deleteVariableInSession(userSelection.getSelectionVariates(), variableId);
		} else if (mode == AppConstants.SEGMENT_NURSERY_CONDITIONS.getInt()) {
			addVariableInDeletedList(userSelection.getNurseryConditions(), mode, variableId,true);
			SettingsUtil.deleteVariableInSession(userSelection.getNurseryConditions(), variableId);
		} else if (mode == AppConstants.SEGMENT_TREATMENT_FACTORS.getInt()) {
			addVariableInDeletedList(userSelection.getTreatmentFactors(), mode, variableId,true);
			SettingsUtil.deleteVariableInSession(userSelection.getTreatmentFactors(), variableId);
		} else {
			addVariableInDeletedList(userSelection.getTrialLevelVariableList(), mode, variableId,true);
			SettingsUtil.deleteVariableInSession(userSelection.getTrialLevelVariableList(),
					variableId);
		}
		return "";
	}

	@ResponseBody
	@RequestMapping(value = "/deleteTreatmentFactorVariable", method = RequestMethod.POST)
	public String deleteTreatmentFactorVariable(@RequestBody Map<String, Integer> ids) {
		Integer levelID = ids.get("levelID");
		Integer valueID = ids.get("valueID");
		if (levelID != null && levelID != 0) {
			deleteVariable(AppConstants.SEGMENT_TREATMENT_FACTORS.getInt(), levelID);
		}

		if (valueID != null && valueID != 0) {
			deleteVariable(AppConstants.SEGMENT_TREATMENT_FACTORS.getInt(), valueID);
		}

		return "";
	}


	@ResponseBody
	@RequestMapping(value = "/hasMeasurementData/{mode}", method = RequestMethod.POST)
	public boolean hasMeasurementData(@RequestBody List<Integer> ids, @PathVariable int mode) {
		for (Integer id : ids) {
			if (checkModeAndHasMeasurementData(mode, id)) {
				return true;
			}
		}
		return false;
	}
	
	@ResponseBody
	@RequestMapping(value = "/hasMeasurementData/environmentNo/{environmentNo}", method = RequestMethod.POST)
	public boolean hasMeasurementDataOnEnvironment(@RequestBody List<Integer> ids,@PathVariable int environmentNo) {
		Workbook workbook = userSelection.getWorkbook();
		List<MeasurementRow> observationsOnEnvironment = getObservationsOnEnvironment(workbook,environmentNo);
		
		for(Integer variableId : ids){
			if(hasMeasurementDataEntered(variableId,observationsOnEnvironment)){
				return true;
			}
		}
		
		return false;
	}

	protected List<MeasurementRow> getObservationsOnEnvironment(Workbook workbook, int environmentNo) {
		List<MeasurementRow> observations = workbook.getObservations();
		List<MeasurementRow> filteredObservations = new ArrayList<MeasurementRow>();
		
        //we do a matching of the name here so there won't be a problem in the data table
        for(MeasurementRow row : observations){	
        	List<MeasurementData> dataList = row.getDataList();
        	for(MeasurementData data : dataList){
        		if(isEnvironmentNotDeleted(data, environmentNo)){
        			filteredObservations.add(row);                    	
                	break;
        		}
        		
        	}
        }
		return filteredObservations;
	}

	private boolean isEnvironmentNotDeleted(MeasurementData data, int environmentNo) {
		if (data.getMeasurementVariable() != null) {
			MeasurementVariable var = data.getMeasurementVariable();
            if (var != null && var.getName() != null 
            		&& ("TRIAL_INSTANCE".equalsIgnoreCase(var.getName()) || "TRIAL".equalsIgnoreCase(var.getName()))
            		&& data.getValue().equals(String.valueOf(environmentNo)) ) {
            	return true;
            }
        }
		return false;
	}

	protected boolean checkModeAndHasMeasurementData(int mode, int variableId) {
		return mode == AppConstants.SEGMENT_TRAITS.getInt() &&
				userSelection.getMeasurementRowList() != null &&
				!userSelection.getMeasurementRowList().isEmpty() &&
				hasMeasurementDataEntered(variableId);
	}

	@Override
	public String getContentName() {
		return null;
	}
}
