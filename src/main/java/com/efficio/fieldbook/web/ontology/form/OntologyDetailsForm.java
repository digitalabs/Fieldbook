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
package com.efficio.fieldbook.web.ontology.form;

import java.util.List;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TraitReference;


// TODO: Auto-generated Javadoc
/**
 * The Class OntologyBrowserForm.
 *
 * @author Efficio.Daniel
 */
public class OntologyDetailsForm{
    
    StandardVariable variable;
    String projectCount;
    String observationCount;
    private String traitClassName;
    private String propertyName;
    private String roleName;
    private String methodName;
    private String scaleName;
    private String dataTypeName;
        
    
    
    public String getTraitClassName() {
        return traitClassName;
    }



    
    public void setTraitClassName(String traitClassName) {
        this.traitClassName = traitClassName;
    }



    
    public String getPropertyName() {
        return propertyName;
    }



    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }



    
    public String getRoleName() {
        return roleName;
    }



    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }



    
    public String getMethodName() {
        return methodName;
    }



    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }



    
    public String getScaleName() {
        return scaleName;
    }



    
    public void setScaleName(String scaleName) {
        this.scaleName = scaleName;
    }



    
    public String getDataTypeName() {
        return dataTypeName;
    }



    
    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }



    public String getProjectCount() {
        return projectCount;
    }


    
    public void setProjectCount(String projectCount) {
        this.projectCount = projectCount;
    }


    
    public String getObservationCount() {
        return observationCount;
    }


    
    public void setObservationCount(String observationCount) {
        this.observationCount = observationCount;
    }


    public StandardVariable getVariable() {
        return variable;
    }

    
    public void setVariable(StandardVariable variable) {
        this.variable = variable;
        if(variable != null){
            setTraitClassName(variable.getIsA().getName());
            setPropertyName(variable.getProperty().getName());
            setRoleName(variable.getPhenotypicType().toString());
            setMethodName(variable.getMethod().getName());
            setScaleName(variable.getScale().getName());
            setDataTypeName(variable.getDataType().getName());                                      
        }
    }
    
    
 }
