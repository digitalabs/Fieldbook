
package com.efficio.fieldbook.web.trial.form;

import java.util.Map;

import org.generationcp.middleware.domain.dms.StandardVariable;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 6/25/2014 Time: 6:26 PM
 */
public class EnvironmentForm {

	public Integer noOfEnvironments;

	public Map<Integer, StandardVariable> managementDetails;
	public Map<Integer, StandardVariable> trialConditionDetails;

}
