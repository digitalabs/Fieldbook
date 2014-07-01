package com.efficio.fieldbook.web.trial.form;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import org.generationcp.middleware.domain.dms.StandardVariable;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 6/25/2014
 * Time: 6:26 PM
 */
public class EnvironmentForm {
    public Integer noOfEnvironments;

    public Map<Integer, StandardVariable> managementDetails;
    public Map<Integer, StandardVariable> trialConditionDetails;


}
