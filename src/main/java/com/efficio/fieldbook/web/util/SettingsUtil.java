/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package com.efficio.fieldbook.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Constant;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.ParentDataset;
import org.generationcp.middleware.pojos.workbench.settings.TreatmentFactor;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.PairedVariable;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.common.bean.TreatmentFactorDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.TreatmentFactorData;

/**
 * The Class SettingsUtil.
 */
public class SettingsUtil {

	/**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SettingsUtil.class);

    private SettingsUtil() {
		// do nothing
	}
    
    public static String cleanSheetAndFileName(String name) {
        if (name == null) {
            return null;
        }
        
    	name = org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName(name, '_');
    	//http://www.rgagnon.com/javadetails/java-0662.html
    	name = name.replaceAll("[:\\\\/*?|<>]", "_");
        /*
        name = name.replaceAll("[^a-zA-Z0-9-_.=^&'@{}$!-#()%.+~_\\[\\]]", "_");
        name = name.replaceAll("\"", "_");
        */
        return name;
    }


    /**
     * Get standard variable.
     *
     * @param id                         the id
     * @param userSelection              the user selection
     * @param fieldbookMiddlewareService the fieldbook middleware service
     * @return the standard variable
     */
    private static StandardVariable getStandardVariable(int id, UserSelection userSelection,
                                                        org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {

        StandardVariable variable = userSelection.getCacheStandardVariable(id);
        if (variable == null) {
            try {
                variable = fieldbookMiddlewareService.getStandardVariable(id);
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
            }
            if (variable != null) {
                userSelection.putStandardVariableInCache(variable);
            }
        }

        return variable;
    }

    /**
     * Convert pojo to xml dataset.
     *
     * @param fieldbookMiddlewareService the fieldbook middleware service
     * @param name                       the name
     * @param nurseryLevelConditions     the nursery level conditions
     * @param plotsLevelList             the plots level list
     * @param baselineTraitsList         the baseline traits list
     * @param userSelection              the user selection
     * @return the dataset
     */
    public static ParentDataset convertPojoToXmlDataset(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, String name, List<SettingDetail> nurseryLevelConditions,
                                                        List<SettingDetail> plotsLevelList, List<SettingDetail> baselineTraitsList, UserSelection userSelection, List<SettingDetail> nurseryConditions) {
        return convertPojoToXmlDataset(fieldbookMiddlewareService, name, nurseryLevelConditions, plotsLevelList, baselineTraitsList, userSelection, null, null, null, nurseryConditions, null, true);
    }

    protected static List<Condition> convertDetailsToConditions(List<SettingDetail> details, UserSelection userSelection,
                                                                org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
        List<Condition> conditions = new ArrayList<Condition>();

        if (details != null) {
            for (SettingDetail settingDetail : details) {
                SettingVariable variable = settingDetail.getVariable();
                if (userSelection != null) {
                    StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);

                    //if the standard variable exists in the database
                    if (standardVariable.getName() != null) {
                        variable.setPSMRFromStandardVariable(standardVariable);

                        if ((variable.getCvTermId().equals(Integer.valueOf(TermId.BREEDING_METHOD_ID.getId())) ||
                                variable.getCvTermId().equals(Integer.valueOf(TermId.BREEDING_METHOD_CODE.getId())))
                                && ("0").equals(settingDetail.getValue())) {
                            settingDetail.setValue("");
                        }

                        Condition condition = new Condition(variable.getName(), variable.getDescription(), variable.getProperty(),
                                variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(),
                                DateUtil.convertToDBDateFormat(variable.getDataTypeId(), HtmlUtils.htmlEscape(settingDetail.getValue())),
                                variable.getDataTypeId(), variable.getMinRange(), variable.getMaxRange());
                        condition.setOperation(variable.getOperation());
                        condition.setStoredIn(standardVariable.getStoredIn().getId());
                        condition.setId(variable.getCvTermId());
                        conditions.add(condition);
                    }
                }
            }
        }
        return conditions;
    }

    protected static List<Variate> convertBaselineTraitsToVariates(List<SettingDetail> baselineTraits, UserSelection userSelection,
                                                                   org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
        List<Variate> variateList = new ArrayList<Variate>();

        if (baselineTraits != null && !baselineTraits.isEmpty()) {
            for (SettingDetail settingDetail : baselineTraits) {
                SettingVariable variable = settingDetail.getVariable();
                if (userSelection != null) {
                    StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
                    variable.setPSMRFromStandardVariable(standardVariable);

                    Variate variate = new Variate(variable.getName(), variable.getDescription(), variable.getProperty(),
                            variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(), variable.getDataTypeId(),
                            settingDetail.getPossibleValues(), variable.getMinRange(), variable.getMaxRange());
                    variate.setOperation(variable.getOperation());
                    variate.setStoredIn(standardVariable.getStoredIn().getId());
                    variate.setId(variable.getCvTermId());
                    variateList.add(variate);
                }
            }
        }

        return variateList;
    }

    protected static List<Factor> convertDetailsToFactors(List<SettingDetail> plotLevelDetails, UserSelection userSelection,
                                                          org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
        List<Factor> factors = new ArrayList<Factor>();
        if (plotLevelDetails != null && !plotLevelDetails.isEmpty()) {
            for (SettingDetail settingDetail : plotLevelDetails) {
                SettingVariable variable = settingDetail.getVariable();
                if (userSelection != null) {
                    StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
                    variable.setPSMRFromStandardVariable(standardVariable);

                    Factor factor = convertStandardVariableToFactor(standardVariable);
                    factor.setOperation(variable.getOperation());
                    factor.setPossibleValues(settingDetail.getPossibleValues());
                    factor.setMinRange(variable.getMinRange());
                    factor.setMaxRange(variable.getMaxRange());
                    factor.setName(variable.getName());
                    factors.add(factor);
                }
            }
        }

        return factors;
    }

    protected static Factor convertStandardVariableToFactor(StandardVariable variable) {
        Factor factor = new Factor(variable.getName(), variable.getDescription(), variable.getProperty().getName(),
                variable.getScale().getName(), variable.getMethod().getName(), variable.getPhenotypicType().name(), variable.getDataType().getName(), variable.getId());

        factor.setStoredIn(variable.getStoredIn().getId());
        factor.setId(variable.getId());
        factor.setDataTypeId(variable.getDataType().getId());

        return factor;
    }

    protected static List<Constant> convertConditionsToConstants(List<SettingDetail> nurseryConditions, UserSelection userSelection,
                                                                 org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, boolean isTrial) {
        List<Constant> constants = new ArrayList<Constant>();
        if (nurseryConditions != null && !nurseryConditions.isEmpty()) {
            for (SettingDetail settingDetail : nurseryConditions) {
                SettingVariable variable = settingDetail.getVariable();
                if (userSelection != null) {
                    StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);

                    variable.setPSMRFromStandardVariable(standardVariable);
                    //need to get the name from the session

                    Constant constant = new Constant(variable.getName(), variable.getDescription(), variable.getProperty(),
                            variable.getScale(), variable.getMethod(), variable.getRole(), variable.getDataType(),
                            DateUtil.convertToDBDateFormat(variable.getDataTypeId(), HtmlUtils.htmlEscape(settingDetail.getValue())), 
                            variable.getDataTypeId(), variable.getMinRange(), variable.getMaxRange(), isTrial);
                    constant.setOperation(variable.getOperation());
                    constant.setStoredIn(standardVariable.getStoredIn().getId());
                    constant.setId(variable.getCvTermId());
                    constants.add(constant);
                }
            }
        }

        return constants;
    }

    protected static void setNameAndOperationFromSession(List<SettingDetail> listWithValue, List<SettingDetail> listFromSession) {
        if (listWithValue != null && listFromSession != null) {
            for (SettingDetail detailWithValue : listWithValue) {
                for (SettingDetail detailFromSession : listFromSession) {
                    if (detailFromSession.getVariable().getCvTermId().equals(detailWithValue.getVariable().getCvTermId())) {
                        SettingVariable variable = detailWithValue.getVariable();
                        detailWithValue.setPossibleValues(detailFromSession.getPossibleValues());
                        variable.setName(detailFromSession.getVariable().getName());
                        variable.setOperation(detailFromSession.getVariable().getOperation());
                    }
                }
            }
        }
    }

    protected static List<TreatmentFactor> processTreatmentFactorItems(List<SettingDetail> treatmentFactorDetails, Map<String, TreatmentFactorData> treatmentFactorItems, List<Factor> factorList,
                                                                       UserSelection userSelection, org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
        List<TreatmentFactor> treatmentFactors = new ArrayList<TreatmentFactor>();
        if (treatmentFactorItems != null && treatmentFactorDetails != null) {

            for (SettingDetail detail : treatmentFactorDetails) {
                Integer termId = detail.getVariable().getCvTermId();
                StandardVariable levelVariable = getStandardVariable(termId, userSelection, fieldbookMiddlewareService);
                Factor levelFactor = convertStandardVariableToFactor(levelVariable);
                levelFactor.setName(detail.getVariable().getName());
                Factor valueFactor = null;
                levelFactor.setOperation(detail.getVariable().getOperation());
                levelFactor.setTreatmentLabel(detail.getVariable().getName());

                TreatmentFactorData data = treatmentFactorItems.get(termId.toString());
                
                if (data != null) {
                    StandardVariable valueVariable = getStandardVariable(data.getVariableId(),
                            userSelection, fieldbookMiddlewareService);
        
                    valueFactor = convertStandardVariableToFactor(valueVariable);
                    valueFactor.setOperation(detail.getVariable().getOperation());
                    valueFactor.setTreatmentLabel(detail.getVariable().getName());
                    
                    int index = 1;
                    for (String labelValue : data.getLabels()) {
                        TreatmentFactor treatmentFactor = new TreatmentFactor(levelFactor, valueFactor, index, labelValue);
                        treatmentFactors.add(treatmentFactor);
                        index++;
                    }
                    factorList.add(valueFactor);
                }
                factorList.add(levelFactor);
            }

        }


        return treatmentFactors;
    }

    /**
     * Convert pojo to xml dataset.
     *
     * @param fieldbookMiddlewareService the fieldbook middleware service
     * @param name                       the name
     * @param studyLevelConditions     the nursery level conditions
     * @param plotsLevelList             the plots level list
     * @param baselineTraitsList         the baseline traits list
     * @param userSelection              the user selection
     * @param trialLevelVariablesList    the trial level variables list
     * @return the parent dataset
     */
    public static ParentDataset convertPojoToXmlDataset(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
                                                        String name, List<SettingDetail> studyLevelConditions, List<SettingDetail> plotsLevelList, List<SettingDetail> baselineTraitsList,
                                                        UserSelection userSelection, List<SettingDetail> trialLevelVariablesList, List<SettingDetail> treatmentFactorDetails,
                                                        Map<String, TreatmentFactorData> treatmentFactorItems,
                                                        List<SettingDetail> nurseryConditions, List<SettingDetail> trialLevelConditions, boolean fromNursery) {

        // this block is necessary for the previous nursery code because the setting details passed in from nursery are mostly empty except for properties
        // also stored in the HTML form; e.g., value
        if (fromNursery) {
            setNameAndOperationFromSession(studyLevelConditions, userSelection.getStudyLevelConditions());
            setNameAndOperationFromSession(plotsLevelList, userSelection.getPlotsLevelList());
            setNameAndOperationFromSession(baselineTraitsList, userSelection.getBaselineTraitsList());
            setNameAndOperationFromSession(nurseryConditions, userSelection.getNurseryConditions());

            // name and operation setting are no longer performed on the other setting lists provided as params in this method
            // because those are only defined for trials
            // assumption is that params provided from trial management do not need this operation
        }

        List<Condition> conditions = convertDetailsToConditions(studyLevelConditions, userSelection, fieldbookMiddlewareService);
        List<Factor> factors = convertDetailsToFactors(plotsLevelList, userSelection, fieldbookMiddlewareService);
        List<Variate> variates = convertBaselineTraitsToVariates(baselineTraitsList, userSelection, fieldbookMiddlewareService);
        List<Constant> constants = convertConditionsToConstants(nurseryConditions, userSelection, fieldbookMiddlewareService, false);
        List<Factor> trialLevelVariables = convertDetailsToFactors(trialLevelVariablesList, userSelection, fieldbookMiddlewareService);

        List<TreatmentFactor> treatmentFactors = processTreatmentFactorItems(treatmentFactorDetails, treatmentFactorItems, factors, userSelection, fieldbookMiddlewareService);

        constants.addAll(convertConditionsToConstants(trialLevelConditions, userSelection, fieldbookMiddlewareService, true));

        ParentDataset realDataset;
        if (trialLevelVariablesList != null) {

            //this is a trial dataset
            Dataset dataset = new Dataset(trialLevelVariables);
            dataset.setConditions(conditions);
            dataset.setFactors(factors);
            dataset.setVariates(variates);
            dataset.setConstants(constants);
            dataset.setName(name);
            dataset.setTrialLevelFactor(trialLevelVariables);
            dataset.setTreatmentFactors(treatmentFactors);
            realDataset = dataset;
        } else {
            Dataset dataset = new Dataset();
            dataset.setConditions(conditions);
            dataset.setFactors(factors);
            dataset.setVariates(variates);
            dataset.setConstants(constants);
            dataset.setName(name);
            realDataset = dataset;
        }

        return realDataset;
    }


    /**
     * Gets the field possible vales.
     *
     * @param fieldbookService   the fieldbook service
     * @param standardVariableId the standard variable id
     * @return the field possible vales
     */
    public static List<ValueReference> getFieldPossibleVales(FieldbookService fieldbookService, Integer standardVariableId) {
        List<ValueReference> possibleValueList = new ArrayList<ValueReference>();

        try {
            possibleValueList = fieldbookService.getAllPossibleValues(standardVariableId);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        return possibleValueList;
    }

    /**
     * Gets the field possible values favorite.
     *
     * @param fieldbookService   the fieldbook service
     * @param standardVariableId the standard variable id
     * @param programUUID          the project id
     * @return the field possible values favorite
     */
    private static List<ValueReference> getFieldPossibleValuesFavorite(FieldbookService fieldbookService, Integer standardVariableId, String programUUID) {
        List<ValueReference> possibleValueList = new ArrayList<ValueReference>();

        try {
            possibleValueList = fieldbookService.getAllPossibleValuesFavorite(standardVariableId, programUUID);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        return possibleValueList;
    }

    /**
     * Checks if is setting variable deletable.
     *
     * @param standardVariableId the standard variable id
     * @param requiredFields     the required fields
     * @return true, if is setting variable deletable
     */
    public static boolean isSettingVariableDeletable(Integer standardVariableId, String requiredFields) {
        //need to add the checking here if the specific PSM-R is deletable, for the nursery level details
        StringTokenizer token = new StringTokenizer(requiredFields, ",");
        while (token.hasMoreTokens()) {
            if (standardVariableId.equals(Integer.parseInt(token.nextToken()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convert xml dataset to pojo.
     *
     * @param fieldbookMiddlewareService the fieldbook middleware service
     * @param fieldbookService           the fieldbook service
     * @param dataset                    the dataset
     * @param userSelection              the user selection
     * @param programUUID                  the project id
     * @throws MiddlewareQueryException the middleware query exception
     */
    public static void convertXmlDatasetToPojo(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, com.efficio.fieldbook.service.api.FieldbookService fieldbookService, ParentDataset dataset, UserSelection userSelection, String programUUID, boolean isUsePrevious, boolean isTrial) throws MiddlewareQueryException {
        if (!isTrial) {
            convertXmlNurseryDatasetToPojo(fieldbookMiddlewareService, fieldbookService, (Dataset) dataset, userSelection, programUUID, isUsePrevious);
        } else {
            convertXmlTrialDatasetToPojo(fieldbookMiddlewareService, fieldbookService, (Dataset) dataset, userSelection, programUUID);
        }
    }

    private static boolean idCounterPartInList(Integer stdVar, HashMap<String, String> idCodeNameMap, List<Condition> conditions) {
        boolean inList = false;

        if (idCodeNameMap.get(String.valueOf(stdVar)) != null) {
            StringTokenizer tokenizerPair = new StringTokenizer(idCodeNameMap.get(String.valueOf(stdVar)), "|");
            String idTermId = tokenizerPair.nextToken();
            for (Condition condition : conditions) {
                if (Integer.parseInt(idTermId) == condition.getId()) {
                    inList = true;
                }
            }
        }
        return inList;
    }
    public static HashMap<String, MeasurementVariable> buildMeasurementVariableMap(List<MeasurementVariable> factors) {
        HashMap<String, MeasurementVariable> factorsMap = new HashMap<String, MeasurementVariable>();
        for (MeasurementVariable factor : factors) {
        	factorsMap.put(String.valueOf(factor.getTermId()), factor);
        }
        return factorsMap;
    }
    
    private static HashMap<String, Condition> buildConditionsMap(List<Condition> conditions) {
        HashMap<String, Condition> conditionsMap = new HashMap<String, Condition>();
        for (Condition condition : conditions) {
            conditionsMap.put(String.valueOf(condition.getId()), condition);
        }
        return conditionsMap;
    }

    public static String getNameCounterpart(Integer idTermId, String idNameCombination) {
        StringTokenizer tokenizer = new StringTokenizer(idNameCombination, ",");
        while (tokenizer.hasMoreTokens()) {
            String pair = tokenizer.nextToken();
            StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
            if (tokenizerPair.nextToken().equals(String.valueOf(idTermId))) {
                return tokenizerPair.nextToken();
            }
        }
        return "";
    }

    public static List<Integer> parseFieldListAndConvert(String selectedFieldList) {
        List<Integer> selectedFieldIDs = new ArrayList<>();
        String[] split = selectedFieldList.split(",");
        for (String s : split) {
            try {
                if ("".equals(s)) {
					continue;
				}

                Integer termID = new Integer(s);
                String nameTermID = SettingsUtil
						.getNameCounterpart(termID, AppConstants.ID_NAME_COMBINATION.getString());

                if ("".equals(nameTermID)) {
					selectedFieldIDs.add(termID);
				} else {
					selectedFieldIDs.add(new Integer(nameTermID));
				}
            } catch (NumberFormatException e) {
                LOG.error(e.getMessage());
            }
        }

        return selectedFieldIDs;
    }

    /**
     * Convert xml nursery dataset to pojo.
     *
     * @param fieldbookMiddlewareService the fieldbook middleware service
     * @param fieldbookService           the fieldbook service
     * @param dataset                    the dataset
     * @param userSelection              the user selection
     * @param programUUID                  the project id
     * @throws MiddlewareQueryException the middleware query exception
     */
    private static void convertXmlNurseryDatasetToPojo(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
                                                       com.efficio.fieldbook.service.api.FieldbookService fieldbookService, Dataset dataset, UserSelection userSelection,
                                                       String programUUID, boolean isUsePrevious) throws MiddlewareQueryException {
        Operation operation = isUsePrevious ? Operation.ADD : Operation.UPDATE;
        if (dataset != null && userSelection != null) {
            //we copy it to User session object
            //nursery level
            List<SettingDetail> studyLevelConditions = new ArrayList<SettingDetail>();
            List<SettingDetail> plotsLevelList = new ArrayList<SettingDetail>();
            List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
            List<SettingDetail> nurseryConditions = new ArrayList<SettingDetail>();
            List<SettingDetail> selectionVariates = new ArrayList<SettingDetail>();
            List<SettingDetail> removedFactors = new ArrayList<SettingDetail>();
            List<SettingDetail> removedConditions = new ArrayList<SettingDetail>();
            if (dataset.getConditions() != null) {
                //create a map of code and its id-code-name combination
                HashMap<String, String> idCodeNameMap = new HashMap<String, String>();
                String idCodeNameCombination = AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString();
                if (idCodeNameCombination != null & !idCodeNameCombination.isEmpty()) {
                    StringTokenizer tokenizer = new StringTokenizer(idCodeNameCombination, ",");
                    if (tokenizer.hasMoreTokens()) {
                        while (tokenizer.hasMoreTokens()) {
                            String pair = tokenizer.nextToken();
                            StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
                            tokenizerPair.nextToken();
                            idCodeNameMap.put(tokenizerPair.nextToken(), pair);
                        }
                    }
                }

                HashMap<String, Condition> conditionsMap = buildConditionsMap(dataset.getConditions());

                for (Condition condition : dataset.getConditions()) {
                    SettingVariable variable = new SettingVariable(condition.getName(), condition.getDescription(), condition.getProperty(),
                            condition.getScale(), condition.getMethod(), condition.getRole(), condition.getDatatype(), condition.getDataTypeId(),
                            condition.getMinRange(), condition.getMaxRange());
                    variable.setOperation(operation);
                    Integer stdVar = null;
                    if (condition.getId() != 0) {
                        stdVar = condition.getId();
                    } else {
                        stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()),
                                HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
                    }

                    variable.setCvTermId(stdVar);
                    List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, stdVar);
                    SettingDetail settingDetail = new SettingDetail(variable,
                            possibleValues, HtmlUtils.htmlUnescape(condition.getValue()), isSettingVariableDeletable(stdVar, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()));

                    settingDetail.setPossibleValuesToJson(possibleValues);
                    List<ValueReference> possibleValuesFavorite = getFieldPossibleValuesFavorite(fieldbookService, stdVar, programUUID);
                    settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);

                    if (userSelection != null) {
                        StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
                        variable.setPSMRFromStandardVariable(standardVariable);
                        Enumeration enumerationByDescription = standardVariable.getEnumerationByDescription(condition.getValue());

                        if (!inHideVariableFields(stdVar, AppConstants.HIDE_NURSERY_FIELDS.getString()) && !idCounterPartInList(stdVar, idCodeNameMap, dataset.getConditions())) {
                            if (enumerationByDescription != null) {
                            	if(standardVariable.getId() != TermId.NURSERY_TYPE.getId()){
                            		settingDetail.setValue(enumerationByDescription.getName());
                            	}else{
                            		if(enumerationByDescription.getId() != null && NumberUtils.isNumber(Integer.toString(enumerationByDescription.getId()))){
                            			settingDetail.setValue(Integer.toString(enumerationByDescription.getId()));
                            		}
                            	}
                            }
                            if ((variable.getCvTermId().equals(Integer.valueOf(TermId.BREEDING_METHOD_ID.getId()))
                                    || variable.getCvTermId().equals(Integer.valueOf(TermId.BREEDING_METHOD_CODE.getId()))) &&
                                    (condition.getValue() == null || condition.getValue().isEmpty())) {
                                //if method has no value, auto select the Please Choose option
                                settingDetail.setValue(AppConstants.PLEASE_CHOOSE.getString());
                            } else if (variable.getCvTermId().equals(Integer.valueOf(TermId.BREEDING_METHOD_CODE.getId()))
                                    && condition.getValue() != null && !condition.getValue().isEmpty()) {
                                //set the value of code to ID for it to be selected in the popup
                            	Method method = fieldbookMiddlewareService.getMethodByCode(condition.getValue());
                            	if(method != null){
                            		settingDetail.setValue(String.valueOf(method.getMid()));
                            	}else{
                            		settingDetail.setValue("");
                            	}
                            }

                            //set local name of id variable to local name of name variable
                            String nameTermId = getNameCounterpart(variable.getCvTermId(), AppConstants.ID_NAME_COMBINATION.getString());
                            if (conditionsMap.get(nameTermId) != null) {
                                settingDetail.getVariable().setName(conditionsMap.get(nameTermId).getName());
                            }

                            studyLevelConditions.add(settingDetail);
                        } else {
                            if (enumerationByDescription != null) {
                                settingDetail.setValue(enumerationByDescription.getId().toString());
                            }
                            removedConditions.add(settingDetail);
                        }
                    }
                    if (settingDetail.getVariable().getDataTypeId() != null && settingDetail.getVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
                        settingDetail.setValue(DateUtil.convertToUIDateFormat(variable.getDataTypeId(), HtmlUtils.htmlUnescape(condition.getValue())));
                    }
                }
            }
            //plot level
            //always allowed to be deleted
            if (dataset.getFactors() != null) {
                for (Factor factor : dataset.getFactors()) {
                    SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(), factor.getProperty(),
                            factor.getScale(), factor.getMethod(), factor.getRole(), factor.getDatatype());
                    variable.setOperation(operation);
                    Integer stdVar = null;
                    if (factor.getTermId() != null) {
                        stdVar = factor.getTermId();
                    } else {
                        stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
                    }

                    variable.setCvTermId(stdVar);
                    SettingDetail settingDetail = new SettingDetail(variable,
                            null, null, isSettingVariableDeletable(stdVar, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()));

                    if (factor.getRole() != null && !factor.getRole().equals(PhenotypicType.TRIAL_ENVIRONMENT.name())) {
                        if (!inHideVariableFields(stdVar, AppConstants.HIDE_PLOT_FIELDS.getString())) {
                            plotsLevelList.add(settingDetail);
                        } else {
                            removedFactors.add(settingDetail);
                        }
                    } else {
                        removedFactors.add(settingDetail);
                    }
                }
            }
            //baseline traits
            //always allowed to be deleted
            if (dataset.getVariates() != null) {
                for (Variate variate : dataset.getVariates()) {

                    SettingVariable variable = new SettingVariable(variate.getName(), variate.getDescription(), variate.getProperty(),
                            variate.getScale(), variate.getMethod(), variate.getRole(), variate.getDatatype());
                    variable.setOperation(operation);
                    Integer stdVar = null;
                    if (variate.getId() != 0) {
                        stdVar = variate.getId();
                    } else {
                        stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
                    }
                    variable.setCvTermId(stdVar);
                    
                    StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);

                    List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, stdVar);
                    
                    SettingDetail settingDetail = new SettingDetail(variable,
                            possibleValues, null, true);
                    
                    settingDetail.setPossibleValuesToJson(possibleValues);
                    List<ValueReference> possibleValuesFavorite = getFieldPossibleValuesFavorite(fieldbookService, stdVar, programUUID);
                    settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                    
                    if (inPropertyList(standardVariable.getProperty().getId())) {
                        selectionVariates.add(settingDetail);
                    } else {
                        baselineTraitsList.add(settingDetail);
                    }
                }
            }

            //nursery conditions/constants
            if (dataset.getConstants() != null) {
                for (Constant constant : dataset.getConstants()) {
                    SettingVariable variable = new SettingVariable(constant.getName(), constant.getDescription(), constant.getProperty(),
                            constant.getScale(), constant.getMethod(), constant.getRole(), constant.getDatatype(), constant.getDataTypeId(),
                            constant.getMinRange(), constant.getMaxRange());
                    variable.setOperation(operation);
                    Integer stdVar = null;
                    if (constant.getId() != 0) {
                        stdVar = constant.getId();
                    } else {
                        stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()),
                                HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.VARIATE);
                    }

                    variable.setCvTermId(stdVar);

                    List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, stdVar);
                    SettingDetail settingDetail = new SettingDetail(variable,
                            possibleValues, HtmlUtils.htmlUnescape(constant.getValue()), true);

                    settingDetail.setPossibleValuesToJson(possibleValues);
                    List<ValueReference> possibleValuesFavorite = getFieldPossibleValuesFavorite(fieldbookService, stdVar, programUUID);
                    settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                    nurseryConditions.add(settingDetail);
                    if (userSelection != null) {
                        StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
                        variable.setPSMRFromStandardVariable(standardVariable);
                        Enumeration enumerationByDescription = standardVariable.getEnumerationByDescription(constant.getValue());
                        if (enumerationByDescription != null) {
                            settingDetail.setValue(enumerationByDescription.getName());
                        }
                    }
                    if (settingDetail.getVariable().getDataTypeId() != null && settingDetail.getVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
                        settingDetail.setValue(DateUtil.convertToUIDateFormat(variable.getDataTypeId(), HtmlUtils.htmlUnescape(constant.getValue())));
                    }
                }
            }

            userSelection.setStudyLevelConditions(studyLevelConditions);
            userSelection.setPlotsLevelList(plotsLevelList);
            userSelection.setBaselineTraitsList(baselineTraitsList);
            userSelection.setNurseryConditions(nurseryConditions);
            userSelection.setSelectionVariates(selectionVariates);
            userSelection.setRemovedFactors(removedFactors);
            userSelection.setRemovedConditions(removedConditions);
        }
    }

    public static boolean inPropertyList(int propertyId) {
        StringTokenizer token = new StringTokenizer(AppConstants.SELECTION_VARIATES_PROPERTIES.getString(), ",");
        while (token.hasMoreTokens()) {
            int propId = Integer.parseInt(token.nextToken());

            if (propId == propertyId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert xml trial dataset to pojo.
     *
     * @param fieldbookMiddlewareService the fieldbook middleware service
     * @param fieldbookService           the fieldbook service
     * @param dataset                    the dataset
     * @param userSelection              the user selection
     * @param programUUID                  the project id
     * @throws MiddlewareQueryException the middleware query exception
     */
    private static void convertXmlTrialDatasetToPojo(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, com.efficio.fieldbook.service.api.FieldbookService fieldbookService, Dataset dataset, UserSelection userSelection, String programUUID) throws MiddlewareQueryException {
        if (dataset != null && userSelection != null) {
            //we copy it to User session object
            //nursery level
            List<SettingDetail> studyLevelConditions = new ArrayList<SettingDetail>();
            List<SettingDetail> plotsLevelList = new ArrayList<SettingDetail>();
            List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
            List<SettingDetail> trialLevelVariableList = new ArrayList<SettingDetail>();
            List<SettingDetail> treatmentFactors = new ArrayList<SettingDetail>();
            List<SettingDetail> trialConditions = new ArrayList<SettingDetail>();
            
            HashMap<String, Condition> conditionsMap = buildConditionsMap(dataset.getConditions());
            
            if (dataset.getConditions() != null) {
                for (Condition condition : dataset.getConditions()) {

                    SettingVariable variable = new SettingVariable(condition.getName(), condition.getDescription(), condition.getProperty(),
                            condition.getScale(), condition.getMethod(), condition.getRole(), condition.getDatatype(), condition.getDataTypeId(),
                            condition.getMinRange(), condition.getMaxRange());
                    Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));

                    if (!inHideVariableFields(stdVar, AppConstants.HIDE_NURSERY_FIELDS.getString()) || !inHideVariableFields(stdVar, AppConstants.HIDE_TRIAL_VARIABLE_DBCV_FIELDS.getString())) {
                        variable.setCvTermId(stdVar);
                        List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, stdVar);
                        SettingDetail settingDetail = new SettingDetail(variable,
                                possibleValues, HtmlUtils.htmlUnescape(condition.getValue()), isSettingVariableDeletable(stdVar, AppConstants.CREATE_NURSERY_REQUIRED_FIELDS.getString()));

                        settingDetail.setPossibleValuesToJson(possibleValues);
                        List<ValueReference> possibleValuesFavorite = getFieldPossibleValuesFavorite(fieldbookService, stdVar, programUUID);
                        settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);
                        
                        String nameTermId = getNameCounterpart(variable.getCvTermId(), AppConstants.ID_NAME_COMBINATION.getString());
                        if (conditionsMap.get(nameTermId) != null) {
                            settingDetail.getVariable().setName(conditionsMap.get(nameTermId).getName());
                        }
                        
                        studyLevelConditions.add(settingDetail);
                        if (userSelection != null) {
                            StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
                            variable.setPSMRFromStandardVariable(standardVariable);
                        }
                    }
                }
            }
            //plot level
            //always allowed to be deleted
            if (dataset.getFactors() != null) {
                for (Factor factor : dataset.getFactors()) {

                    if (factor.getTreatmentLabel() == null || "".equals(factor.getTreatmentLabel())
                            && factor.getRole() != null && !factor.getRole().equals(PhenotypicType.TRIAL_ENVIRONMENT.name())) {

                        SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(), factor.getProperty(),
                                factor.getScale(), factor.getMethod(), factor.getRole(), factor.getDatatype());
                        Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
                        if (!inHideVariableFields(stdVar, AppConstants.HIDE_PLOT_FIELDS.getString())) {
                            variable.setCvTermId(stdVar);
                            SettingDetail settingDetail = new SettingDetail(variable,
                                    null, null, isSettingVariableDeletable(stdVar, AppConstants.CREATE_TRIAL_PLOT_REQUIRED_FIELDS.getString()));
                            plotsLevelList.add(settingDetail);
                        }
                    }
                }
            }
            //baseline traits
            //always allowed to be deleted
            if (dataset.getVariates() != null) {
                for (Variate variate : dataset.getVariates()) {

                    SettingVariable variable = new SettingVariable(variate.getName(), variate.getDescription(), variate.getProperty(),
                            variate.getScale(), variate.getMethod(), variate.getRole(), variate.getDatatype());
                    Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
                    variable.setCvTermId(stdVar);
                    SettingDetail settingDetail = new SettingDetail(variable,
                            null, null, true);
                    baselineTraitsList.add(settingDetail);
                }
            }

            
            
            if (dataset.getTrialLevelFactor() != null) {
                for (Factor factor : dataset.getTrialLevelFactor()) {
                    String variableName = factor.getName();
                    SettingVariable variable = new SettingVariable(variableName, factor.getDescription(), factor.getProperty(),
                            factor.getScale(), factor.getMethod(), factor.getRole(), factor.getDatatype());
                    Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
                    if (!inHideVariableFields(stdVar, AppConstants.HIDE_TRIAL_ENVIRONMENT_FIELDS.getString())) {
                        variable.setCvTermId(stdVar);

                        List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, stdVar);
                        SettingDetail settingDetail = new SettingDetail(variable,
                                possibleValues, null, isSettingVariableDeletable(stdVar, AppConstants.CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS.getString()));

                        settingDetail.setPossibleValuesToJson(possibleValues);
                        List<ValueReference> possibleValuesFavorite = getFieldPossibleValuesFavorite(fieldbookService, stdVar, programUUID);
                        settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);

                        if (TermId.TRIAL_INSTANCE_FACTOR.getId() == variable.getCvTermId()) {
                            settingDetail.setDeletable(false);
                        }

                      
                        trialLevelVariableList.add(settingDetail);

                        if (userSelection != null) {
                            StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
                            variable.setPSMRFromStandardVariable(standardVariable);
                        }
                    }
                }
            }

            if (dataset.getTreatmentFactors() != null && !dataset.getTreatmentFactors().isEmpty()) {
                int group = 1;
                for (TreatmentFactor treatmentFactor : dataset.getTreatmentFactors()) {

                    treatmentFactors.add(createTreatmentFactor(treatmentFactor.getLevelFactor(), fieldbookMiddlewareService, fieldbookService, group, userSelection));
                    treatmentFactors.add(createTreatmentFactor(treatmentFactor.getValueFactor(), fieldbookMiddlewareService, fieldbookService, group, userSelection));

                    group++;
                }
            }
            
            if (dataset.getConstants() != null && !dataset.getConstants().isEmpty()) {
            	for (Constant constant : dataset.getConstants()) {
                    SettingVariable variable = new SettingVariable(constant.getName(), constant.getDescription(), constant.getProperty(),
                    		constant.getScale(), constant.getMethod(), constant.getRole(), constant.getDatatype());
                    Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
                    variable.setCvTermId(stdVar);
                    SettingDetail settingDetail = new SettingDetail(variable, null, null, true);
                    trialConditions.add(settingDetail);
            	}
            }

            userSelection.setStudyLevelConditions(studyLevelConditions);
            userSelection.setPlotsLevelList(plotsLevelList);
            userSelection.setBaselineTraitsList(baselineTraitsList);
            userSelection.setTrialLevelVariableList(trialLevelVariableList);
            userSelection.setTreatmentFactors(treatmentFactors);
            userSelection.setNurseryConditions(trialConditions);
        }
    }

    /**
     * In hide variable fields.
     *
     * @param stdVarId     the std var id
     * @param variableList the variable list
     * @return true, if successful
     */
    public static boolean inHideVariableFields(Integer stdVarId, String variableList) {
        StringTokenizer token = new StringTokenizer(variableList, ",");
        boolean inList = false;
        while (token.hasMoreTokens()) {
            if (stdVarId.equals(Integer.parseInt(token.nextToken()))) {
                inList = true;
                break;
            }
        }
        return inList;
    }

    /**
     * Generate dummy condition.
     *
     * @param limit the limit
     * @return the list
     */
    public static List<Condition> generateDummyCondition(int limit) {
        List<Condition> conditions = new ArrayList<Condition>();
        for (int i = 0; i < limit; i++) {
            Condition condition = new Condition();

            condition.setName(i + "name");
            condition.setDescription(i + " description");
            condition.setProperty(i + " property");
            condition.setScale(i + " scale");
            condition.setMethod(i + " method");
            condition.setRole(i + " role");
            condition.setDatatype(i + "Test Data Type");
            condition.setValue(i + " value");
            conditions.add(condition);
        }
        return conditions;
    }

    /**
     * Generate dummy factor.
     *
     * @param limit the limit
     * @return the list
     */
    public static List<Factor> generateDummyFactor(int limit) {
        List<Factor> factors = new ArrayList<Factor>();
        for (int i = 0; i < limit; i++) {
            Factor factor = new Factor();

            factor.setName(i + "name");
            factor.setDescription(i + " description");
            factor.setProperty(i + " property");
            factor.setScale(i + " scale");
            factor.setMethod(i + " method");
            factor.setRole(i + " role");
            factor.setDatatype(i + "Test Data Type");
            factors.add(factor);
        }
        return factors;
    }

    /**
     * Generate dummy variate.
     *
     * @param limit the limit
     * @return the list
     */
    public static List<Variate> generateDummyVariate(int limit) {
        List<Variate> variates = new ArrayList<Variate>();
        for (int i = 0; i < limit; i++) {
            Variate variate = new Variate();

            variate.setName(i + "name");
            variate.setDescription(i + " description");
            variate.setProperty(i + " property");
            variate.setScale(i + " scale");
            variate.setMethod(i + " method");
            variate.setRole(i + " role");
            variate.setDatatype(i + "Test Data Type");
            variates.add(variate);
        }
        return variates;
    }


    public static Workbook convertXmlDatasetToWorkbook(ParentDataset dataset, boolean isNursery) {
    	return convertXmlDatasetToWorkbook(dataset, isNursery, null, null, null, null);
    }
    /**
     * Convert xml dataset to workbook.
     *
     * @param dataset the dataset
     * @return the workbook
     */
    public static Workbook convertXmlDatasetToWorkbook(ParentDataset dataset, boolean isNursery,
    		ExpDesignParameterUi param, List<Integer> variables, org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
    		List<MeasurementVariable> allExpDesignVariables) {
    	
        Workbook workbook = new Workbook();

        if (isNursery) {
            Dataset nurseryDataset = (Dataset) dataset;
            workbook.setConditions(convertConditionsToMeasurementVariables(nurseryDataset.getConditions()));
            workbook.setFactors(convertFactorsToMeasurementVariables(nurseryDataset.getFactors()));
            workbook.setVariates(convertVariatesToMeasurementVariables(nurseryDataset.getVariates()));
            workbook.setConstants(convertConstantsToMeasurementVariables(nurseryDataset.getConstants()));
        } else {
            Dataset trialDataset = (Dataset) dataset;
            workbook.setConditions(convertConditionsToMeasurementVariables(trialDataset.getConditions()));
            workbook.setFactors(convertFactorsToMeasurementVariables(trialDataset.getFactors()));
            workbook.setVariates(convertVariatesToMeasurementVariables(trialDataset.getVariates()));
            workbook.getConditions().addAll(convertFactorsToMeasurementVariables(trialDataset.getTrialLevelFactor()));
            workbook.setConstants(convertConstantsToMeasurementVariables(trialDataset.getConstants()));
            if (workbook.getTreatmentFactors() == null) {
                workbook.setTreatmentFactors(new ArrayList<TreatmentVariable>());
            }
            workbook.getTreatmentFactors().addAll(convertTreatmentFactorsToTreatmentVariables(trialDataset.getTreatmentFactors()));
        	try {
        		setExperimentalDesignToWorkbook(param, variables, workbook, allExpDesignVariables, fieldbookMiddlewareService);
        	} catch (MiddlewareQueryException e) {
        		//do nothing
        	}
        }

        return workbook;
    }

    /**
     * Convert workbook to xml dataset.
     *
     * @param workbook the workbook
     * @return the dataset
     */
    public static ParentDataset convertWorkbookToXmlDataset(Workbook workbook) {
        return convertWorkbookToXmlDataset(workbook, true);
    }

    public static ParentDataset convertWorkbookToXmlDataset(Workbook workbook, boolean isNursery) {
        ParentDataset dataset = null;

        if (isNursery) {
            Dataset nurseryDataset = new Dataset();
            List<Condition> conditions = convertMeasurementVariablesToConditions(workbook.getConditions());
            List<Factor> factors = convertMeasurementVariablesToFactors(workbook.getFactors());
            List<Variate> variates = convertMeasurementVariablesToVariates(workbook.getVariates());
            List<Constant> constants = convertMeasurementVariablesToConstants(workbook.getConstants(), !isNursery);

            nurseryDataset.setConditions(conditions);
            nurseryDataset.setFactors(factors);
            nurseryDataset.setVariates(variates);
            nurseryDataset.setConstants(constants);
            dataset = nurseryDataset;
        } else {
            Dataset trialDataset = new Dataset();

            List<Condition> conditions = convertMeasurementVariablesToConditions(workbook.getStudyConditions());
            List<Factor> factors = convertMeasurementVariablesToFactors(workbook.getFactors());
            List<Variate> variates = convertMeasurementVariablesToVariates(workbook.getVariates());
            List<Constant> constants = convertMeasurementVariablesToConstants(workbook.getConstants(), !isNursery);
            List<TreatmentFactor> treatmentFactors = convertTreatmentVariablesToTreatmentFactors(workbook.getTreatmentFactors());

            trialDataset.setConditions(conditions);
            trialDataset.setFactors(factors);
            trialDataset.setVariates(variates);
            trialDataset.setConstants(constants);
            trialDataset.setTrialLevelFactor(convertMeasurementVariablesToFactors(workbook.getTrialConditions()));
            trialDataset.setTreatmentFactors(treatmentFactors);

            dataset = trialDataset;
        }
        return dataset;
    }

    /**
     * Convert measurement variables to conditions.
     *
     * @param mlist the mlist
     * @return the list
     */
    private static List<Condition> convertMeasurementVariablesToConditions(List<MeasurementVariable> mlist) {
        List<Condition> conditions = new ArrayList<Condition>();

        if (mlist != null && !mlist.isEmpty()) {
            for (MeasurementVariable mvar : mlist) {
                Condition condition = new Condition(
                        mvar.getName(),
                        mvar.getDescription(),
                        mvar.getProperty(),
                        mvar.getScale(),
                        mvar.getMethod(),
                        PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(),
                        mvar.getDataType(),
                        mvar.getValue(), null, null, null);
                condition.setId(mvar.getTermId());
                conditions.add(condition);
            }
        }

        return conditions;
    }

    private static List<Constant> convertMeasurementVariablesToConstants(List<MeasurementVariable> mlist, boolean isTrial) {
        List<Constant> constants = new ArrayList<Constant>();

        if (mlist != null && !mlist.isEmpty()) {

            for (MeasurementVariable mvar : mlist) {
                Constant constant = new Constant(
                        mvar.getName(),
                        mvar.getDescription(),
                        mvar.getProperty(),
                        mvar.getScale(),
                        mvar.getMethod(),
                        PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(),
                        mvar.getDataType(),
                        mvar.getValue(), null, null, null, isTrial);
                constant.setId(mvar.getTermId());
                constants.add(constant);
            }
        }

        return constants;
    }

    /**
     * Convert measurement variables to factors.
     *
     * @param mlist the mlist
     * @return the list
     */
    private static List<Factor> convertMeasurementVariablesToFactors(List<MeasurementVariable> mlist) {
        List<Factor> factors = new ArrayList<Factor>();

        if (mlist != null && !mlist.isEmpty()) {
            for (MeasurementVariable mvar : mlist) {
                Factor factor = new Factor(
                        mvar.getName(),
                        mvar.getDescription(),
                        mvar.getProperty(),
                        mvar.getScale(),
                        mvar.getMethod(),
                        PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(),
                        mvar.getDataType(), mvar.getTermId());
                factor.setTreatmentLabel(mvar.getTreatmentLabel());
                factor.setId(mvar.getTermId());
                factors.add(factor);
            }
        }

        return factors;
    }

    private static List<TreatmentFactor> convertTreatmentVariablesToTreatmentFactors(List<TreatmentVariable> mlist) {
        List<TreatmentFactor> factors = new ArrayList<TreatmentFactor>();

        if (mlist != null && !mlist.isEmpty()) {
            Factor levelFactor, valueFactor;
            for (TreatmentVariable var : mlist) {
                MeasurementVariable mvar = var.getLevelVariable();
                MeasurementVariable vvar = var.getValueVariable();
                levelFactor = new Factor(
                        mvar.getName(),
                        mvar.getDescription(),
                        mvar.getProperty(),
                        mvar.getScale(),
                        mvar.getMethod(),
                        PhenotypicType.getPhenotypicTypeForLabel(mvar.getLabel()).toString(),
                        mvar.getDataType(), mvar.getTermId());
                valueFactor = new Factor(
                        vvar.getName(),
                        vvar.getDescription(),
                        vvar.getProperty(),
                        vvar.getScale(),
                        vvar.getMethod(),
                        PhenotypicType.getPhenotypicTypeForLabel(vvar.getLabel()).toString(),
                        vvar.getDataType(), vvar.getTermId());
                factors.add(new TreatmentFactor(levelFactor, valueFactor));
            }
        }

        return factors;
    }

    /**
     * Convert measurement variables to variates.
     *
     * @param mlist the mlist
     * @return the list
     */
    private static List<Variate> convertMeasurementVariablesToVariates(List<MeasurementVariable> mlist) {
        List<Variate> variates = new ArrayList<Variate>();

        if (mlist != null && !mlist.isEmpty()) {
            for (MeasurementVariable mvar : mlist) {
                Variate variate = new Variate(
                        mvar.getName(),
                        mvar.getDescription(),
                        mvar.getProperty(),
                        mvar.getScale(),
                        mvar.getMethod(),
                        PhenotypicType.VARIATE.toString(),
                        mvar.getDataType(),
                        mvar.getDataTypeId(),
                        mvar.getPossibleValues(),
                        mvar.getMinRange(),
                        mvar.getMaxRange());
                variate.setId(mvar.getTermId());
                variates.add(variate);
            }
        }

        return variates;
    }

    /**
     * Convert conditions to measurement variables.
     *
     * @param conditions the conditions
     * @return the list
     */
    private static List<MeasurementVariable> convertConditionsToMeasurementVariables(List<Condition> conditions) {
        List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
        if (conditions != null && !conditions.isEmpty()) {
            for (Condition condition : conditions) {
                list.add(convertConditionToMeasurementVariable(condition));
            }
        }
        return list;
    }

    private static List<MeasurementVariable> convertConstantsToMeasurementVariables(List<Constant> constants) {
        List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
        if (constants != null && !constants.isEmpty()) {
            for (Constant constant : constants) {
                list.add(convertConstantToMeasurementVariable(constant));
            }
        }
        return list;
    }

    /**
     * Convert condition to measurement variable.
     *
     * @param condition the condition
     * @return the measurement variable
     */
    private static MeasurementVariable convertConditionToMeasurementVariable(Condition condition) {
        String label = null;
        label = PhenotypicType.valueOf(condition.getRole()).getLabelList().get(0);
        MeasurementVariable mvar = new MeasurementVariable(
                condition.getName(), condition.getDescription(), condition.getScale(), condition.getMethod(), condition.getProperty(), condition.getDatatype(),
                condition.getValue(), label,
                condition.getMinRange(), condition.getMaxRange());
        mvar.setOperation(condition.getOperation());
        mvar.setTermId(condition.getId());
        mvar.setStoredIn(condition.getStoredIn());
        mvar.setFactor(true);
        mvar.setDataTypeId(condition.getDataTypeId());
        return mvar;
    }

    private static MeasurementVariable convertConstantToMeasurementVariable(Constant constant) {
        String label = null;

        //currently if operation is add, then it's always a trial constant
        if (constant.getOperation() == Operation.ADD) {
        	label = PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0);
        } else {
        	label = constant.getLabel();
        }

        MeasurementVariable mvar = new MeasurementVariable(
                constant.getName(), constant.getDescription(), constant.getScale(), constant.getMethod(), constant.getProperty(), constant.getDatatype(),
                constant.getValue(), label,
                constant.getMinRange(), constant.getMaxRange());

        mvar.setOperation(constant.getOperation());
        mvar.setTermId(constant.getId());
        mvar.setStoredIn(constant.getStoredIn());
        mvar.setFactor(false);
        mvar.setDataTypeId(constant.getDataTypeId());
        mvar.setPossibleValues(constant.getPossibleValues());
        return mvar;
    }

    /**
     * Convert factors to measurement variables.
     *
     * @param factors the factors
     * @return the list
     */
    private static List<MeasurementVariable> convertFactorsToMeasurementVariables(List<Factor> factors) {
        List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
        if (factors != null && !factors.isEmpty()) {
            for (Factor factor : factors) {
                list.add(convertFactorToMeasurementVariable(factor));
            }
        }
        return list;
    }

    /**
     * Convert factor to measurement variable.
     *
     * @param factor the factor
     * @return the measurement variable
     */
    private static MeasurementVariable convertFactorToMeasurementVariable(Factor factor) {
        MeasurementVariable mvar = new MeasurementVariable(
                factor.getName(), factor.getDescription(), factor.getScale(), factor.getMethod(), factor.getProperty(), factor.getDatatype(), null,
                PhenotypicType.valueOf(factor.getRole()).getLabelList().get(0));
        mvar.setFactor(true);
        mvar.setOperation(factor.getOperation());
        mvar.setStoredIn(factor.getStoredIn());
        mvar.setTermId(factor.getId());
        mvar.setTreatmentLabel(factor.getTreatmentLabel());
        mvar.setDataTypeId(factor.getDataTypeId());
        mvar.setPossibleValues(factor.getPossibleValues());
        mvar.setMinRange(factor.getMinRange());
        mvar.setMaxRange(factor.getMaxRange());
        return mvar;
    }

    private static List<TreatmentVariable> convertTreatmentFactorsToTreatmentVariables(List<TreatmentFactor> factors) {
        List<TreatmentVariable> list = new ArrayList<TreatmentVariable>();
        if (factors != null && !factors.isEmpty()) {
            for (TreatmentFactor factor : factors) {
                list.add(convertTreatmentFactorToTreatmentVariable(factor));
            }
        }
        return list;
    }

    private static TreatmentVariable convertTreatmentFactorToTreatmentVariable(TreatmentFactor factor) {
        TreatmentVariable mvar = new TreatmentVariable();
        MeasurementVariable levelVariable = convertFactorToMeasurementVariable(factor.getLevelFactor());
        MeasurementVariable valueVariable = convertFactorToMeasurementVariable(factor.getValueFactor());
        levelVariable.setValue(factor.getLevelNumber() != null ? factor.getLevelNumber().toString() : null);
        levelVariable.setTreatmentLabel(factor.getLevelFactor().getName());
        valueVariable.setValue(factor.getValue());
        valueVariable.setTreatmentLabel(factor.getLevelFactor().getName());
        mvar.setLevelVariable(levelVariable);
        mvar.setValueVariable(valueVariable);
        return mvar;
    }

    /**
     * Convert variates to measurement variables.
     *
     * @param variates the variates
     * @return the list
     */
    private static List<MeasurementVariable> convertVariatesToMeasurementVariables(List<Variate> variates) {
        List<MeasurementVariable> list = new ArrayList<MeasurementVariable>();
        if (variates != null && !variates.isEmpty()) {
            for (Variate variate : variates) {
                list.add(convertVariateToMeasurementVariable(variate));
            }
        }
        return list;
    }

    /**
     * Convert variate to measurement variable.
     *
     * @param variate the variate
     * @return the measurement variable
     */
    private static MeasurementVariable convertVariateToMeasurementVariable(Variate variate) {
    	//because variates are mostly PLOT variables
        MeasurementVariable mvar = new MeasurementVariable(
                variate.getName(), variate.getDescription(), variate.getScale(), variate.getMethod(), variate.getProperty(), variate.getDatatype(), null,
                PhenotypicType.TRIAL_DESIGN.getLabelList().get(0), variate.getMinRange(), variate.getMaxRange()); 
        mvar.setOperation(variate.getOperation());
        mvar.setTermId(variate.getId());
        mvar.setStoredIn(variate.getStoredIn());
        mvar.setFactor(false);
        mvar.setDataTypeId(variate.getDataTypeId());
        mvar.setPossibleValues(variate.getPossibleValues());
        return mvar;
    }

    private static SettingDetail createTreatmentFactor(Factor factor, org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
                                                       FieldbookService fieldbookService, int group, UserSelection userSelection) throws MiddlewareQueryException {

        SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(),
                factor.getProperty(), factor.getScale(), factor.getMethod(), factor.getRole(),
                factor.getDatatype());
        StandardVariable standardVariable = getStandardVariable(factor.getTermId(), userSelection, fieldbookMiddlewareService);
        variable.setPSMRFromStandardVariable(standardVariable);
        variable.setCvTermId(standardVariable.getId());
        List<ValueReference> possibleValues = getFieldPossibleVales(fieldbookService, standardVariable.getId());
        SettingDetail settingDetail = new SettingDetail(variable, possibleValues, null, true);
        settingDetail.setPossibleValuesToJson(possibleValues);
        settingDetail.setGroup(group);
        settingDetail.setDeletable(true);

        return settingDetail;
    }

    public static StudyDetails convertWorkbookToStudyDetails(Workbook workbook, org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
                                                             FieldbookService fieldbookService, UserSelection userSelection)
            throws MiddlewareQueryException {

        StudyDetails studyDetails = convertWorkbookStudyLevelVariablesToStudyDetails(workbook,
                fieldbookMiddlewareService, fieldbookService, userSelection, workbook.getStudyId().toString());

        if(workbook.getTrialDatasetId() != null){
        	studyDetails.setNumberOfEnvironments(Long.valueOf(fieldbookMiddlewareService.countObservations(workbook.getTrialDatasetId())).intValue());
        }else{
        	studyDetails.setNumberOfEnvironments(0);
        }

        List<SettingDetail> factors = convertWorkbookFactorsToSettingDetails(workbook.getNonTrialFactors(), fieldbookMiddlewareService);
        if (!workbook.isNursery()) {
            List<SettingDetail> germplasmDescriptors = new ArrayList<SettingDetail>();
            rearrangeSettings(factors, germplasmDescriptors, PhenotypicType.GERMPLASM);
            studyDetails.setGermplasmDescriptors(germplasmDescriptors);
            List<TreatmentFactorDetail> treatmentFactorDetails = convertWorkbookFactorsToTreatmentDetailFactors(workbook.getTreatmentFactors());
            studyDetails.setTreatmentFactorDetails(treatmentFactorDetails);
        }
        studyDetails.setFactorDetails(factors);
        List<SettingDetail> traits = new ArrayList<SettingDetail>();
        List<SettingDetail> selectionVariateDetails = new ArrayList<SettingDetail>();
        convertWorkbookVariatesToSettingDetails(workbook.getVariates(), fieldbookMiddlewareService, fieldbookService, traits, selectionVariateDetails);
        studyDetails.setVariateDetails(traits);
        studyDetails.setSelectionVariateDetails(selectionVariateDetails);
        studyDetails.setExperimentalDesignDetails(workbook.getExperimentalDesignVariables());

        return studyDetails;
    }

    private static StudyDetails convertWorkbookStudyLevelVariablesToStudyDetails(Workbook workbook,
                                                                                 org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
                                                                                 FieldbookService fieldbookService, UserSelection userSelection, String projectId)
            throws MiddlewareQueryException {

        StudyDetails details = new StudyDetails();
        details.setId(workbook.getStudyId());
        List<MeasurementVariable> conditions = workbook.getConditions();
        List<MeasurementVariable> constants = workbook.getConstants();

        List<SettingDetail> basicDetails = new ArrayList<SettingDetail>();
        List<SettingDetail> managementDetails = new ArrayList<SettingDetail>();
        List<SettingDetail> nurseryConditionDetails = new ArrayList<SettingDetail>();

        List<String> basicFields;
        if (workbook.isNursery()) {
            basicFields = Arrays.asList(AppConstants.NURSERY_BASIC_REQUIRED_FIELDS.getString().split(","));
        } else {
            basicFields = Arrays.asList(AppConstants.TRIAL_BASIC_REQUIRED_FIELDS.getString().split(","));
        }

        if (conditions != null) {
            MeasurementVariable studyName = WorkbookUtil.getMeasurementVariable(conditions, TermId.STUDY_NAME.getId());
            if (studyName != null) {
                details.setName(studyName.getValue());
            }
            basicDetails = convertWorkbookToSettingDetails(basicFields, conditions, fieldbookMiddlewareService, fieldbookService, userSelection, workbook);
            managementDetails = convertWorkbookOtherStudyVariablesToSettingDetails(conditions, managementDetails.size(), userSelection, fieldbookMiddlewareService, fieldbookService);
            nurseryConditionDetails = convertWorkbookOtherStudyVariablesToSettingDetails(constants, 1, userSelection, fieldbookMiddlewareService, fieldbookService, true);
        }

        if (!workbook.isNursery()) {
            List<SettingDetail> environmentManagementDetails = new ArrayList<SettingDetail>();
            rearrangeSettings(managementDetails, environmentManagementDetails, PhenotypicType.TRIAL_ENVIRONMENT);
            details.setEnvironmentManagementDetails(environmentManagementDetails);
        }
        details.setBasicStudyDetails(basicDetails);
        details.setManagementDetails(managementDetails);
        details.setNurseryConditionDetails(nurseryConditionDetails);
        return details;
    }

    private static List<SettingDetail> convertWorkbookOtherStudyVariablesToSettingDetails(List<MeasurementVariable> conditions, int index,
                                                                                          UserSelection userSelection,
                                                                                          org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, FieldbookService fieldbookService)
            throws MiddlewareQueryException {
        return convertWorkbookOtherStudyVariablesToSettingDetails(conditions, index, userSelection, fieldbookMiddlewareService, fieldbookService, false);
    }

    private static List<SettingDetail> convertWorkbookOtherStudyVariablesToSettingDetails(List<MeasurementVariable> conditions, int index,
                                                                                          UserSelection userSelection,
                                                                                          org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, FieldbookService fieldbookService, boolean isVariate)
            throws MiddlewareQueryException {

        List<SettingDetail> details = new ArrayList<SettingDetail>();
        List<String> basicFields;
        if (userSelection.isTrial()) {
            basicFields = Arrays.asList(AppConstants.TRIAL_BASIC_REQUIRED_FIELDS.getString().split(","));
        } else {
            basicFields = Arrays.asList(AppConstants.NURSERY_BASIC_REQUIRED_FIELDS.getString().split(","));
        }
        List<String> hiddenFields = Arrays.asList(AppConstants.HIDDEN_FIELDS.getString().split(","));

        HashMap<String, MeasurementVariable> variableMap = new HashMap<String, MeasurementVariable>();

        for (MeasurementVariable condition : conditions) {
            variableMap.put(String.valueOf(condition.getTermId()), condition);
        }

        if (conditions != null) {
            for (MeasurementVariable condition : conditions) {
                String id = String.valueOf(condition.getTermId());
                String role = (isVariate) ? PhenotypicType.VARIATE.toString() : PhenotypicType.getPhenotypicTypeForLabel(condition.getLabel()).toString();
                if (!basicFields.contains(id) && !hiddenFields.contains(id)
                		//do not show breeding method id if code exists
                        && !(condition.getTermId() == TermId.BREEDING_METHOD_ID.getId() && variableMap.get(String.valueOf(TermId.BREEDING_METHOD_CODE.getId())) != null) 
                        && !(condition.getTermId() == TermId.BREEDING_METHOD.getId() && (variableMap.get(String.valueOf(TermId.BREEDING_METHOD_CODE.getId())) != null ||
                        variableMap.get(String.valueOf(TermId.BREEDING_METHOD_ID.getId())) != null))) { 
                	//do not name if code or id exists
                    SettingVariable variable = getSettingVariable(getDisplayName(conditions, condition.getTermId(), condition.getName()), condition.getDescription(), condition.getProperty(),
                            condition.getScale(), condition.getMethod(), role,
                            condition.getDataType(), condition.getDataTypeId(), condition.getMinRange(), condition.getMaxRange(), userSelection, fieldbookMiddlewareService);
                    variable.setCvTermId(condition.getTermId());
                    variable.setStoredInId(condition.getStoredIn());
                    String value = fieldbookService.getValue(variable.getCvTermId(), HtmlUtils.htmlUnescape(condition.getValue()),
                            condition.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId());
                    SettingDetail settingDetail = new SettingDetail(variable, null, HtmlUtils.htmlUnescape(value), false);
                    settingDetail.setPossibleValues(fieldbookService.getAllPossibleValues(condition.getTermId()));
                    index = addToList(details, settingDetail, index, null, null);
                }
            }
        }
        return details;
    }

    private static String getDisplayName(List<MeasurementVariable> variables, int termId, String name) {
        if (AppConstants.getString(String.valueOf(termId) + AppConstants.LABEL.getString()) != null) {
            return AppConstants.getString(String.valueOf(termId) + AppConstants.LABEL.getString());
        } else {
            Map<String, String> map = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
            String pair = map.get(String.valueOf(termId));
            if (pair != null) {
                for (MeasurementVariable variable : variables) {
                    if (pair.equals(String.valueOf(variable.getTermId()))) {
                        return variable.getName();
                    }
                }
            }
        }
        return name;
    }

    private static List<SettingDetail> convertWorkbookToSettingDetails(List<String> fields, List<MeasurementVariable> conditions,
                                                                       org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService, FieldbookService fieldbookService,
                                                                       UserSelection userSelection, Workbook workbook)
            throws MiddlewareQueryException {

        List<SettingDetail> details = new ArrayList<SettingDetail>();
        int index = fields != null ? fields.size() : 0;
        MeasurementVariable studyNameVar = WorkbookUtil.getMeasurementVariable(workbook.getConditions(), TermId.STUDY_NAME.getId());
        String studyName = studyNameVar != null ? studyNameVar.getValue() : "";
        Integer datasetId = workbook.getMeasurementDatesetId();
        if (datasetId == null) {
            datasetId = fieldbookMiddlewareService.getMeasurementDatasetId(workbook.getStudyId(), studyName);
        }
        for (String strFieldId : fields) {
            if (strFieldId != null && !"".equals(strFieldId)) {
                boolean found = false;
                String label = AppConstants.getString(strFieldId.toUpperCase() + "_LABEL");
                if (conditions != null) {
                    for (MeasurementVariable condition : conditions) {
                        if (NumberUtils.isNumber(strFieldId)) {
                            if (condition.getTermId() == Integer.valueOf(strFieldId)) {
                                if (label == null || "".equals(label.trim())) {
                                    label = condition.getName();
                                }
                                SettingVariable variable = getSettingVariable(label, condition.getDescription(), condition.getProperty(),
                                        condition.getScale(), condition.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(condition.getLabel()).toString(),
                                        condition.getDataType(), condition.getDataTypeId(), condition.getMinRange(), condition.getMaxRange(), userSelection, fieldbookMiddlewareService);
                                variable.setCvTermId(Integer.valueOf(strFieldId));
                                String value = fieldbookService.getValue(variable.getCvTermId(), HtmlUtils.htmlUnescape(condition.getValue()),
                                        condition.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId());
                                SettingDetail settingDetail = new SettingDetail(variable, null,
                                        HtmlUtils.htmlUnescape(value), false);
                                index = addToList(details, settingDetail, index, fields, strFieldId);
                                found = true;
                                break;
                            }
                        } else { 
                        	//special field
                            SettingVariable variable = new SettingVariable(label, null, null, null, null, null, null, null, null, null);
                            String value = getSpecialFieldValue(strFieldId, datasetId, fieldbookMiddlewareService, workbook);
                            SettingDetail settingDetail = new SettingDetail(variable, null, value, false);
                            if (strFieldId.equals(AppConstants.SPFLD_ENTRIES.getString())) {
                                String plotValue = getSpecialFieldValue(AppConstants.SPFLD_PLOT_COUNT.getString(), datasetId, fieldbookMiddlewareService, workbook);
                                PairedVariable pair = new PairedVariable(AppConstants.getString(AppConstants.SPFLD_PLOT_COUNT.getString() + "_LABEL"), plotValue);
                                settingDetail.setPairedVariable(pair);
                            }
                            index = addToList(details, settingDetail, index, fields, strFieldId);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) { 
                	//required field but has no value
                    SettingVariable variable = new SettingVariable(label, null, null, null, null, null, null, null, null, null);
                    SettingDetail settingDetail = new SettingDetail(variable, null, "", false);
                    index = addToList(details, settingDetail, index, fields, strFieldId);
                }
            }
        }
        return details;
    }

    private static String getSpecialFieldValue(String specialFieldLabel, Integer datasetId,
                                               org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
                                               Workbook workbook)
            throws MiddlewareQueryException {

        if (AppConstants.SPFLD_ENTRIES.getString().equals(specialFieldLabel)) {
            long count = fieldbookMiddlewareService.countStocks(datasetId);
            return String.valueOf(count);
        } else if (AppConstants.SPFLD_HAS_FIELDMAP.getString().equals(specialFieldLabel)) {
            return fieldbookMiddlewareService.hasFieldMap(datasetId) ? "Yes" : "No";
        } else if (AppConstants.SPFLD_COUNT_VARIATES.getString().equals(specialFieldLabel)) {
            List<Integer> variateIds = new ArrayList<Integer>();
            if (workbook.getVariates() != null) {
                for (MeasurementVariable variate : workbook.getVariates()) {
                    variateIds.add(variate.getTermId());
                }
            }
            long count = 0;
            if (datasetId != null) {
                count = fieldbookMiddlewareService.countVariatesWithData(datasetId, variateIds);
            }
            return count + " of " + variateIds.size();
        } else if (AppConstants.SPFLD_PLOT_COUNT.getString().equals(specialFieldLabel)) {
            return String.valueOf(fieldbookMiddlewareService.countObservations(datasetId));
        }
        return "";
    }

    private static List<SettingDetail> convertWorkbookFactorsToSettingDetails(List<MeasurementVariable> factors,
                                                                              org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService)
            throws MiddlewareQueryException {

        List<SettingDetail> plotsLevelList = new ArrayList<SettingDetail>();
        if (factors != null) {
            for (MeasurementVariable factor : factors) {
                SettingVariable variable = new SettingVariable(factor.getName(), factor.getDescription(), factor.getProperty(),
                        factor.getScale(), factor.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(factor.getLabel()).toString(),
                        factor.getDataType());
                Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(
                        HtmlUtils.htmlUnescape(variable.getProperty()), HtmlUtils.htmlUnescape(variable.getScale()),
                        HtmlUtils.htmlUnescape(variable.getMethod()), PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));

                if (!inHideVariableFields(stdVar, AppConstants.HIDE_PLOT_FIELDS.getString())
                        && (factor.getTreatmentLabel() == null || "".equals(factor.getTreatmentLabel()))) {

                    variable.setCvTermId(stdVar);
                    variable.setStoredInId(factor.getStoredIn());
                    SettingDetail settingDetail = new SettingDetail(variable,
                            null, null, isSettingVariableDeletable(stdVar, AppConstants.CREATE_PLOT_REQUIRED_FIELDS.getString()));
                    plotsLevelList.add(settingDetail);
                }
            }
        }
        return plotsLevelList;
    }

    public static void convertWorkbookVariatesToSettingDetails(List<MeasurementVariable> variates,
                                                               org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
                                                               FieldbookService fieldbookService, List<SettingDetail> traits, List<SettingDetail> selectedVariates)
            throws MiddlewareQueryException {

        List<String> svProperties = getSelectedVariatesPropertyNames(fieldbookService);

        if (variates != null) {
            for (MeasurementVariable variate : variates) {
                SettingVariable variable = new SettingVariable(variate.getName(), variate.getDescription(), variate.getProperty(),
                        variate.getScale(), variate.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(variate.getLabel()).toString(),
                        variate.getDataType());
                Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(HtmlUtils.htmlUnescape(variable.getProperty()),
                        HtmlUtils.htmlUnescape(variable.getScale()), HtmlUtils.htmlUnescape(variable.getMethod()),
                        PhenotypicType.VARIATE);
                variable.setCvTermId(stdVar);
                SettingDetail settingDetail = new SettingDetail(variable, null, null, true);
                if (svProperties.contains(variate.getProperty())) {
                    selectedVariates.add(settingDetail);
                } else {
                    traits.add(settingDetail);
                }
            }
        }
    }

    private static List<String> getSelectedVariatesPropertyNames(FieldbookService fieldbookService) throws MiddlewareQueryException {
        List<String> names = new ArrayList<String>();
        List<String> ids = Arrays.asList(AppConstants.SELECTION_VARIATES_PROPERTIES.getString().split(","));
        for (String id : ids) {
            Term term = fieldbookService.getTermById(Integer.valueOf(id));
            if (term != null) {
                names.add(term.getName());
            }
        }
        return names;
    }

    private static SettingVariable getSettingVariable(String name, String description, String property, String scale, String method, String role, String dataType,
                                                      Integer dataTypeId, Double minRange, Double maxRange, UserSelection userSelection,
                                                      org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService)
            throws MiddlewareQueryException {

        SettingVariable variable = new SettingVariable(name, description, property, scale, method, role, dataType, dataTypeId, minRange, maxRange);

        Integer stdVar = fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(
                HtmlUtils.htmlUnescape(variable.getProperty()),
                HtmlUtils.htmlUnescape(variable.getScale()),
                HtmlUtils.htmlUnescape(variable.getMethod()),
                PhenotypicType.valueOf(HtmlUtils.htmlUnescape(variable.getRole())));
        variable.setCvTermId(stdVar);
        if (userSelection != null) {
            StandardVariable standardVariable = getStandardVariable(variable.getCvTermId(), userSelection, fieldbookMiddlewareService);
            variable.setPSMRFromStandardVariable(standardVariable);
            stdVar = standardVariable.getId();
        }

        return variable;
    }

    private static int addToList(List<SettingDetail> list, SettingDetail settingDetail, int index, List<String> fields, String idString) {
        int order = -1;
        if (fields != null) {
            order = fields.indexOf(idString);
        }
        settingDetail.setOrder(order > -1 ? order : index++);
        list.add(settingDetail);

        return index;
    }

    private static List<Integer> getBreedingMethodIndeces(List<MeasurementRow> observations, OntologyService ontologyService, boolean isResetAll) throws MiddlewareQueryException {
        List<Integer> indeces = new ArrayList<Integer>();
        MeasurementRow mrow = observations.get(0);
        int index = 0;
        for (MeasurementData data : mrow.getDataList()) {
            if ((data.getMeasurementVariable().getTermId() != TermId.BREEDING_METHOD_VARIATE.getId()
                    && ontologyService.getProperty(data.getMeasurementVariable().getProperty()).getTerm().getId() == TermId.BREEDING_METHOD_PROP.getId() && isResetAll)
                    || (!isResetAll && data.getMeasurementVariable().getTermId() == TermId.BREEDING_METHOD_VARIATE_CODE.getId())) {
                indeces.add(index);
            }
            index++;
        }
        return indeces;
    }

    public static void resetBreedingMethodValueToId(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
                                                    List<MeasurementRow> observations, boolean isResetAll, OntologyService ontologyService) throws MiddlewareQueryException {
        if (observations != null && !observations.isEmpty()) {
            List<Integer> indeces = getBreedingMethodIndeces(observations, ontologyService, isResetAll);
            if (!indeces.isEmpty()) {
                List<Method> methods = fieldbookMiddlewareService.getAllBreedingMethods(false);
                HashMap<String, Method> methodMap = new HashMap<String, Method>();
                //create a map to get method id based on given code
                if (methods != null) {
                    for (Method method : methods) {
                        methodMap.put(method.getMcode(), method);
                    }
                }

                //set value back to id
                for (MeasurementRow row : observations) {
                    for (Integer i : indeces) {
                        Method method = methodMap.get(row.getDataList().get(i).getValue());
                        row.getDataList().get(i).setValue(method == null ? row.getDataList().get(i).getValue() : String.valueOf(method.getMid()));
                    }
                }
            }
        }
    }

    public static void resetBreedingMethodValueToCode(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
                                                      List<MeasurementRow> observations, boolean isResetAll, OntologyService ontologyService) throws MiddlewareQueryException {
        //set value of breeding method code in selection variates to code instead of id
        if (observations != null && !observations.isEmpty()) {
            List<Integer> indeces = getBreedingMethodIndeces(observations, ontologyService, isResetAll);
            if (!indeces.isEmpty()) {
                List<Method> methods = fieldbookMiddlewareService.getAllBreedingMethods(false);
                HashMap<Integer, Method> methodMap = new HashMap<Integer, Method>();

                if (methods != null) {
                    for (Method method : methods) {
                        methodMap.put(method.getMid(), method);
                    }
                }
                for (MeasurementRow row : observations) {
                    for (Integer i : indeces) {
                        Integer value = null;
                        
                        if(row.getDataList().get(i).getValue() == null
                                || row.getDataList().get(i).getValue().isEmpty()) {
                        	value = null;
                        }else if(NumberUtils.isNumber(row.getDataList().get(i).getValue())){
                        	value = Integer.parseInt(row.getDataList().get(i).getValue());
                        }
                            
                        Method method = methodMap.get(value);
                        row.getDataList().get(i).setValue(method == null ? row.getDataList().get(i).getValue() : method.getMcode());
                    }
                }
            }
        }
    }


    public static List<Integer> buildVariates(List<MeasurementVariable> variates) {
        List<Integer> variateList = new ArrayList<Integer>();
        if (variates != null) {
            for (MeasurementVariable var : variates) {
                variateList.add(new Integer(var.getTermId()));
            }
        }
        return variateList;
    }

    private static void rearrangeSettings(List<SettingDetail> sourceList, List<SettingDetail> trialList, PhenotypicType type) {
        if (sourceList != null && !sourceList.isEmpty()) {
            for (SettingDetail source : sourceList) {
                if (source.getVariable().getStoredInId() != null
                        && type.getTypeStorages().contains(source.getVariable().getStoredInId())) {
                    trialList.add(source);
                }
            }
            sourceList.removeAll(trialList);
        }
    }

    private static List<TreatmentFactorDetail> convertWorkbookFactorsToTreatmentDetailFactors(List<TreatmentVariable> factors)
            throws MiddlewareQueryException {
        List<TreatmentFactorDetail> details = new ArrayList<TreatmentFactorDetail>();
        if (factors != null && !factors.isEmpty()) {
            MeasurementVariable levelFactor, amountFactor;
            ObjectMapper objectMapper = new ObjectMapper();
            for (TreatmentVariable factor : factors) {
                try {
                    levelFactor = factor.getLevelVariable();
                    amountFactor = factor.getValueVariable();
                    int levels = factor.getValues() != null ? factor.getValues().size() : 0;

                    TreatmentFactorDetail detail = new TreatmentFactorDetail(
                            levelFactor.getTermId(),
                            amountFactor.getTermId(),
                            String.valueOf(levels),
                            amountFactor.getValue(),
                            levelFactor.getName(),
                            amountFactor.getName(),
                            amountFactor.getDataTypeId(),
                            objectMapper.writeValueAsString(amountFactor.getPossibleValues()),
                            amountFactor.getMinRange(),
                            amountFactor.getMaxRange());
                    detail.setLevelDescription(levelFactor.getDescription());
                    details.add(detail);

                } catch (Exception e) {
                    throw new MiddlewareQueryException(e.getMessage(), e);
                }
            }
        }
        return details;
    }
    
    /**
     * Adds the deleted settings list.
     *
     * @param formList the form list
     * @param deletedList the deleted list
     * @param sessionList the session list
     */
    public static void addDeletedSettingsList(List<SettingDetail> formList, List<SettingDetail> deletedList, List<SettingDetail> sessionList) {
        if (deletedList != null) {
            List<SettingDetail> newDeletedList = new ArrayList<SettingDetail>();
            for (SettingDetail setting : deletedList) {
                if (setting.getVariable().getOperation().equals(Operation.UPDATE)) {
                    setting.getVariable().setOperation(Operation.DELETE);
                    newDeletedList.add(setting);
                }
            }
            if (!newDeletedList.isEmpty()) {
                if (formList == null) {
                	formList = new ArrayList<SettingDetail>();
                }
                formList.addAll(newDeletedList);
                if (sessionList == null) {
                	sessionList = new ArrayList<SettingDetail>();
                }
                sessionList.addAll(newDeletedList);
            }
        }
    }
    
    
    
    /**
     * Removes the basic details variables.
     *
     * @param nurseryLevelConditions the nursery level conditions
     */
    public static void removeBasicDetailsVariables(List<SettingDetail> nurseryLevelConditions) {
        Iterator<SettingDetail> iter = nurseryLevelConditions.iterator();
        while (iter.hasNext()) {
            if (inFixedNurseryList(iter.next().getVariable().getCvTermId())) {
                iter.remove();
            }
        }
    }
    
    /**
     * In fixed nursery list.
     *
     * @param propertyId the property id
     * @return true, if successful
     */
    protected static boolean inFixedNurseryList(int propertyId) {
        StringTokenizer token = new StringTokenizer(AppConstants.FIXED_NURSERY_VARIABLES.getString() + AppConstants.CHECK_VARIABLES.getString(), ",");
        while(token.hasMoreTokens()){
            if (Integer.parseInt(token.nextToken()) == propertyId) {
                return true;
            }
        }
        return false;
    }
        
    public static void findAndUpdateVariableName(List<SettingDetail> traitList, MeasurementVariable currentVar){
    	if(traitList != null && !traitList.isEmpty()){
    		for(SettingDetail detail : traitList){
    			if(detail != null && detail.getVariable() != null && detail.getVariable().getName() != null && detail.getVariable().getCvTermId() != null && 
    					detail.getVariable().getCvTermId().intValue() == currentVar.getTermId()){
    				
    				currentVar.setName(detail.getVariable().getName());
    				break;
    			}
    		}
    	}
    }
    
    public static void setConstantLabels(Dataset dataset, List<MeasurementVariable> constants) throws MiddlewareQueryException {
    	if (constants != null && !constants.isEmpty() && dataset != null && dataset.getConstants() != null && !dataset.getConstants().isEmpty()) { 
    		for (Constant constant : dataset.getConstants()) {
    			for (MeasurementVariable mvar : constants) {
    				if (constant.getId() == mvar.getTermId()) {
    					if (constant.getOperation() != Operation.ADD) {
    						constant.setLabel(mvar.getLabel());
    					}
    					if (mvar.getPossibleValues() != null) {
    						constant.setPossibleValues(mvar.getPossibleValues());
    					}
    					break;
    				}
    			}
    		}
    	}
    }
    
    private static void addTrialCondition(TermId termId, ExpDesignParameterUi param, Workbook workbook,
    		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) throws MiddlewareQueryException {
    	
    	String value = getExperimentalDesignValue(param, termId);
    	MeasurementVariable mvar = null;
    	if (workbook.getTrialConditions() != null && !workbook.getTrialConditions().isEmpty()) {
    		for (MeasurementVariable var : workbook.getTrialConditions()) {
    			if (var.getTermId() == termId.getId()) {
    				mvar = var;
    	        	mvar.setValue(value);
    	        	mvar.setOperation(Operation.UPDATE);
    	        	mvar.setLabel(PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0));
    				break;
    			}
    		}
    	}
    	if (mvar == null) {
	    	StandardVariable stdvar = fieldbookMiddlewareService.getStandardVariable(termId.getId());
	    	if (stdvar != null) {
		    	mvar = new MeasurementVariable(stdvar.getId()
		    			, stdvar.getName()
		    			, stdvar.getDescription()
		    			, stdvar.getScale().getName()
		    			, stdvar.getMethod().getName()
		    			, stdvar.getProperty().getName()
		    			, stdvar.getDataType().getName()
		    			, value
		    			, PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0)
		    			, stdvar.getConstraints() != null ? stdvar.getConstraints().getMinValue() : null
		    			, stdvar.getConstraints() != null ? stdvar.getConstraints().getMaxValue() : null
		    			);
		    	mvar.setOperation(Operation.ADD);
		    	mvar.setStoredIn(stdvar.getStoredIn().getId());
		    	mvar.setDataTypeId(stdvar.getDataType().getId());
		    	workbook.getConditions().add(mvar);
		    	workbook.resetTrialConditions();
	    	}
    	}
    }
    
    private static void removeTrialConditions(List<Integer> ids,Workbook workbook) throws MiddlewareQueryException {
    	
    	if (workbook.getTrialConditions() != null && !workbook.getTrialConditions().isEmpty()) {
    		for (MeasurementVariable var : workbook.getConditions()) {
    			if (ids.contains(var.getTermId())) {
    				var.setOperation(Operation.DELETE);
    			}
    		}
    	}
    }

    private static void addOldExperimentalDesignToCurrentWorkbook(Workbook workbook, List<MeasurementVariable> allExpDesignVariables)	{
    	List<Integer> expDesignConstants = AppConstants.EXP_DESIGN_VARIABLES.getIntegerList();
    	if (allExpDesignVariables != null && !allExpDesignVariables.isEmpty()) {
    		for (MeasurementVariable condition : allExpDesignVariables) {
    			if (expDesignConstants.contains(condition.getTermId())) {
    				boolean found = false;
    				if (workbook.getConditions() != null && !workbook.getConditions().isEmpty()) {
    					for (MeasurementVariable currentCondition : workbook.getConditions()) {
    						if (currentCondition.getTermId() == condition.getTermId()) {
    							found = true;
    							break;
    						}
    					}
    				}
    				if (!found) { 
    					//for deletion
    					workbook.getConditions().add(condition);
    					workbook.resetTrialConditions();
    				}
    			}
    		}
    	}
    }

    private static void setExperimentalDesignToWorkbook(ExpDesignParameterUi param, List<Integer> included, Workbook workbook, List<MeasurementVariable> allExpDesignVariables,
    		org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) 
    				throws MiddlewareQueryException {
    	
    	addOldExperimentalDesignToCurrentWorkbook(workbook, allExpDesignVariables);
    	if (param != null && included != null) {
	    	for (Integer id : included) {
	    		TermId termId = TermId.getById(id);
	    		addTrialCondition(termId, param, workbook, fieldbookMiddlewareService);
	    	}
	    	
			List<Integer> excluded = new ArrayList<Integer>();
			if (workbook.getTrialConditions() != null && !workbook.getTrialConditions().isEmpty()) {
				for (MeasurementVariable var : workbook.getTrialConditions()) {
					if (!included.contains(var.getTermId()) && AppConstants.EXP_DESIGN_VARIABLES.getIntegerList().contains(var.getTermId())) {
						excluded.add(var.getTermId());
					}
				}
			}
			SettingsUtil.removeTrialConditions(excluded, workbook);
    	}
    }
    
    @SuppressWarnings("incomplete-switch")
	public static String getExperimentalDesignValue(ExpDesignParameterUi param, TermId termId) {
    	switch (termId) {
    		case EXPERIMENT_DESIGN_FACTOR : 
    			if (param.getDesignType() != null) {
    				if (param.getDesignType().equals(0)) {
    					return String.valueOf(TermId.RANDOMIZED_COMPLETE_BLOCK.getId());
    				} else if (param.getDesignType().equals(1)) {
    					if (param.getUseLatenized() != null && param.getUseLatenized()) {
    						return String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK_LATIN.getId());
    					} else {
    						return String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId());
    					}
    				} else if (param.getDesignType().equals(2)) {
    					if (param.getUseLatenized() != null && param.getUseLatenized()) {
    						return String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL_LATIN.getId());
    					} else {
    						return String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId());
    					}
    				}
    			}
    			break;
    		case NUMBER_OF_REPLICATES : return String.valueOf(param.getReplicationsCount());
    		case BLOCK_SIZE : return String.valueOf(param.getBlockSize());
    		case REPLICATIONS_MAP : 
    			if (param.getReplicationsArrangement() != null) {
    				switch (param.getReplicationsArrangement()) {
    				case 1 : return String.valueOf(TermId.REPS_IN_SINGLE_COL.getId());
    				case 2 : return String.valueOf(TermId.REPS_IN_SINGLE_ROW.getId());
    				case 3 : return String.valueOf(TermId.REPS_IN_ADJACENT_COLS.getId());
    				default : 
    				}
    			}
    			break;
    		case NO_OF_REPS_IN_COLS : return param.getReplatinGroups();
    		case NO_OF_CBLKS_LATINIZE : return String.valueOf(param.getNblatin());
    		case NO_OF_ROWS_IN_REPS : return String.valueOf(param.getRowsPerReplications());
    		case NO_OF_COLS_IN_REPS : return String.valueOf(param.getColsPerReplications());
    		case NO_OF_CCOLS_LATINIZE : return param.getNclatin();
    		case NO_OF_CROWS_LATINIZE : return param.getNrlatin();
    		default : 
    	}
    	return "";
    }

    public static ExpDesignParameterUi convertToExpDesignParamsUi(List<MeasurementVariable> expDesigns) {
    	ExpDesignParameterUi param = new ExpDesignParameterUi();
    	if (expDesigns != null && !expDesigns.isEmpty()) {
    		for (MeasurementVariable var : expDesigns) {
    			if (var.getTermId() == TermId.BLOCK_SIZE.getId()) {
    				param.setBlockSize(var.getValue());
    			} else if (var.getTermId() == TermId.NO_OF_COLS_IN_REPS.getId()) {
    				param.setColsPerReplications(var.getValue());
    			} else if (var.getTermId() == TermId.NO_OF_ROWS_IN_REPS.getId()) {
    				param.setRowsPerReplications(var.getValue());
    			} else if (var.getTermId() == TermId.EXPERIMENT_DESIGN_FACTOR.getId()) {
    				if (var.getValue() != null) {
    					if (String.valueOf(TermId.RANDOMIZED_COMPLETE_BLOCK.getId()).equals(var.getValue())) {
    						param.setDesignType(0);
    					} else if (String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK.getId()).equals(var.getValue())
    							|| String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK_LATIN.getId()).equals(var.getValue())) {
    						param.setDesignType(1);
    					} else if (String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL.getId()).equals(var.getValue())
    							|| String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL_LATIN.getId()).equals(var.getValue())) {
    						param.setDesignType(2);
    					}
    					if (String.valueOf(TermId.RESOLVABLE_INCOMPLETE_BLOCK_LATIN.getId()).equals(var.getValue())
    							|| String.valueOf(TermId.RESOLVABLE_INCOMPLETE_ROW_COL_LATIN.getId()).equals(var.getValue())) {
    						param.setUseLatenized(true);
    					}
    				}
    			} else if (var.getTermId() == TermId.NO_OF_CBLKS_LATINIZE.getId()) {
    	    		param.setNblatin(var.getValue());
    			} else if (var.getTermId() == TermId.NO_OF_CCOLS_LATINIZE.getId()) {
    	    		param.setNclatin(var.getValue());
    			} else if (var.getTermId() == TermId.NO_OF_CROWS_LATINIZE.getId()) {
    	    		param.setNrlatin(var.getValue());
    			} else if (var.getTermId() == TermId.NO_OF_REPS_IN_COLS.getId()) {
    	    		param.setReplatinGroups(var.getValue());
    			} else if (var.getTermId() == TermId.REPLICATIONS_MAP.getId()) {
    				if (String.valueOf(TermId.REPS_IN_SINGLE_COL.getId()).equals(var.getValue())) {
    					param.setReplicationsArrangement(1);
    				} else if (String.valueOf(TermId.REPS_IN_SINGLE_ROW.getId()).equals(var.getValue())) {
    					param.setReplicationsArrangement(2);
    				} else if (String.valueOf(TermId.REPS_IN_ADJACENT_COLS.getId()).equals(var.getValue())) {
    					param.setReplicationsArrangement(3);
    				}
    			} else if (var.getTermId() == TermId.NUMBER_OF_REPLICATES.getId()) {
    	    		param.setReplicationsCount(var.getValue());
    			}
    		}
    		
    	}
    	return param;
    }
    
    /**
     * Gets the setting detail value.
     *
     * @param details the details
     * @param termId the term id
     * @return the setting detail value
     */
    public static String getSettingDetailValue(List<SettingDetail> details, int termId) {
    	String value = null;
    	
    	for (SettingDetail detail : details) {
    		if (detail.getVariable().getCvTermId().equals(termId)) {
    			value = detail.getValue();
    			break;
    		}
    	}
    	
    	return value;
    }
    
    public static int getCodeValue(String settingDetailValue, List<SettingDetail> removedConditions,
			int termId) {
    	if (removedConditions != null) {
			for (SettingDetail detail : removedConditions) {
	    		if (detail.getVariable().getCvTermId().equals(termId)) {
	    			return getCodeInPossibleValues(detail, settingDetailValue);
	    		}
	    	}
    	}
		return 0;
	}

	private static int getCodeInPossibleValues(SettingDetail detail, String settingDetailValue) {
		if (detail.getPossibleValues() != null && !detail.getPossibleValues().isEmpty()) {
			for (ValueReference valueRef : detail.getPossibleValues()) {
				if (valueRef.getId().equals(Integer.parseInt(settingDetailValue))) {
					return Integer.parseInt(valueRef.getName());
				}
			}
		}
		return 0;
	}

	public static boolean checkVariablesHaveValues(List<SettingDetail> checkVariables) {
		if (checkVariables != null && !checkVariables.isEmpty()) {
			for (SettingDetail setting : checkVariables) {
				if (setting.getValue() == null) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	

    public static List<Integer> parseVariableIds(String variableIds){
    	List<Integer> variableIdList = new ArrayList<Integer>();
		StringTokenizer tokenizer = new StringTokenizer(variableIds, "|");
		while (tokenizer.hasMoreTokens()) {
			variableIdList.add(Integer.valueOf(tokenizer.nextToken()));
		}
		return variableIdList;
    }
}
