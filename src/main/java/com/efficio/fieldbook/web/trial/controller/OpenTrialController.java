package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.settings.TrialDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.trial.bean.TrialSelection;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Controller
@RequestMapping(OpenTrialController.URL)
public class OpenTrialController extends
		AbstractBaseFieldbookController {

    private static final Logger LOG = LoggerFactory.getLogger(OpenTrialController.class);
    public static final String URL = "/TrialManager/addOrRemoveTraits";

    @Resource
	private TrialSelection trialSelection;
	
	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	@Resource
	protected FieldbookService fieldbookService;
	
	@Resource
    protected UserSelection userSelection; 
	
	
	@Override
	public String getContentName() {
        return "TrialManager/openTrial";
	}

    @RequestMapping(value="/viewTrial/{trialId}", method = RequestMethod.GET)
    public String viewNursery(@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form, Model model, 
            @PathVariable int trialId, HttpSession session) {
    	session.invalidate();
        Workbook workbook = null;
        
        try { 
            workbook = fieldbookMiddlewareService.getTrialDataSet(trialId);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        if (workbook != null) {
        	trialSelection.setMeasurementRowList(workbook.getObservations());
            form.setMeasurementRowList(trialSelection.getMeasurementRowList());
            MeasurementVariable trialFactor = null;
            if (workbook.getTrialFactors() != null) {
            	for (MeasurementVariable var : workbook.getTrialFactors()) {
            		if (var.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
            			trialFactor = var;
            			break;
            		}
            	}
            }
            List<MeasurementVariable> variables = new ArrayList<MeasurementVariable>();
            if (trialFactor != null) {
            	variables.add(trialFactor);
            }
            variables.addAll(workbook.getMeasurementDatasetVariables());
            form.setMeasurementVariables(variables);
            form.setStudyName(workbook.getStudyDetails().getStudyName());
            form.changePage(1);
            form.setNumberOfInstances(workbook.getTotalNumberOfInstances());
            trialSelection.setCurrentPage(form.getCurrentPage());
            trialSelection.setWorkbook(workbook);
            
            TrialDataset dataset = (TrialDataset)SettingsUtil.convertWorkbookToXmlDataset(workbook, false);
            try {
				SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, this.getCurrentProjectId());
			} catch (MiddlewareQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           
            
            List<SettingDetail> trialLevelVariableList = addDefaultTrialVariables();
            for(SettingDetail settingDetail : trialLevelVariableList){
            	String name = AppConstants.getString(settingDetail.getVariable().getCvTermId().intValue() + AppConstants.LABEL.getString());
            	if(name != null){
            		settingDetail.getVariable().setName(name);
            	}
            }
            form.setTrialLevelVariables(trialLevelVariableList);
            int numberOfInstance = workbook.getTotalNumberOfInstances();
            List<List<ValueReference>> trialEnvList = createTrialEnvValueList(trialLevelVariableList, numberOfInstance, false);
            form.setTrialEnvironmentValues(trialEnvList);
        }
        form.setLocationId(AppConstants.LOCATION_ID.getString());
        form.setLocationUrl(AppConstants.LOCATION_URL.getString());
        
        return super.show(model);
    }
    
    private List<SettingDetail> addDefaultTrialVariables() {
        List<SettingDetail> trialLevelVariableList = userSelection.getTrialLevelVariableList();
        /*
        StringTokenizer token = new StringTokenizer(AppConstants.TRIAL_ENVIRONMENT_DEFAULT_VARIABLES.getString(), ",");

        while (token.hasMoreTokens()) {
            Integer dataTypeId = Integer.valueOf(TermId.CATEGORICAL_VARIABLE.getId());
            
            String variableName = token.nextToken();
            
            if (variableName.equals(AppConstants.BLOCK_PER_REPLICATE.getString())) {
                dataTypeId = Integer.valueOf(TermId.NUMERIC_VARIABLE.getId());
            }
            
            SettingVariable variable = new SettingVariable(variableName, variableName, "",
                    "", "", "", "", dataTypeId, null, null);
            SettingDetail settingDetail;
            if (variableName.equals(AppConstants.BLOCK_PER_REPLICATE.getString())) {
                settingDetail = new SettingDetail(variable, null, null, false);
            } else {
                List<ValueReference> possibleValues = getPossibleValuesOfDefaultVariable(variableName);
                settingDetail = new SettingDetail(variable, possibleValues, null, false);
                
                settingDetail.setPossibleValuesToJson(possibleValues);
                settingDetail.setPossibleValuesFavoriteToJson(null);
            }
            
            trialLevelVariableList.add(settingDetail);
        }
		*/
        //set orderBy
        StringTokenizer tokenOrder = new StringTokenizer(AppConstants.TRIAL_ENVIRONMENT_ORDER.getString(), ",");
        int i=0;
        int tokenSize = tokenOrder.countTokens();
        while (tokenOrder.hasMoreTokens()) {
            String variableName = tokenOrder.nextToken();
            for (SettingDetail settingDetail : trialLevelVariableList) {
                if (settingDetail.getVariable().getName().equals(variableName)) {
                    settingDetail.setOrder((tokenSize-i)*-1);
                }
            }
            i++;
        }

        Collections.sort(trialLevelVariableList, new  Comparator<SettingDetail>() {
            @Override
            public int compare(SettingDetail o1, SettingDetail o2) {
                    return o1.getOrder() - o2.getOrder();
            }
        });
        
        return trialLevelVariableList;
    }
    private List<List<ValueReference>> createTrialEnvValueList(List<SettingDetail> trialLevelVariableList, int trialInstances, boolean addDefault) {
        List<List<ValueReference>> trialEnvValueList = new ArrayList<List<ValueReference>>();
        for (int i=0; i<trialInstances; i++) {
            List<ValueReference> trialInstanceVariables = new ArrayList<ValueReference>();
            for (SettingDetail detail : trialLevelVariableList) {
                if (detail.getVariable().getCvTermId() != null) {
                    if (detail.getVariable().getCvTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
                        trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), String.valueOf(i+1)));
                    } else {
                        trialInstanceVariables.add(new ValueReference(detail.getVariable().getCvTermId(), ""));
                    }
                } else {
                    trialInstanceVariables.add(new ValueReference(0, ""));
                }
            }
            trialEnvValueList.add(trialInstanceVariables);
        }
        userSelection.setTrialEnvironmentValues(trialEnvValueList);
        return trialEnvValueList;
    }
    
}
