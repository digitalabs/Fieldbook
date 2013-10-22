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
package com.efficio.fieldbook.web.ontology.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TraitReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.validation.ImportGermplasmListValidator;
import com.efficio.fieldbook.web.ontology.form.OntologyBrowserForm;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;


/**
 * This controller handles the ontology screen.
 * 
 * @author Daniel Jao
 */
@Controller
@RequestMapping(OntologyController.URL)
public class OntologyController extends AbstractBaseFieldbookController{
    
    private static final Logger LOG = LoggerFactory.getLogger(OntologyController.class);
    
    /** The Constant URL. */
    public static final String URL = "/OntologyBrowser/";
    
    
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "OntologyBrowser/main";
    }
    
    public List<StandardVariableReference> getDummyStandardVariableReference(int i){
        List<StandardVariableReference> list = new ArrayList();
        int count = 1;
        StandardVariableReference ref1 = new StandardVariableReference((i*100)+count++, i + " Variable 1");
        StandardVariableReference ref2 = new StandardVariableReference((i*100)+count++, i + " Variable 2");
        StandardVariableReference ref3 = new StandardVariableReference((i*100)+count++, i + " Variable 3");
        
        
       list.add(ref1);
       list.add(ref2);
       list.add(ref3);
        return list;
    }
    
    public List<PropertyReference> getDummyPropertyReference(int i){
        List<PropertyReference> propList = new ArrayList();
        int count = 1;
        PropertyReference propRef1 = new PropertyReference((i*10)+count++, i + " Prop 1");
        PropertyReference propRef2 = new PropertyReference((i*10)+count++, i + " Prop 2");
        PropertyReference propRef3 = new PropertyReference((i*10)+count++, i + " Prop 3");
        
        propRef1.setStandardVariables(getDummyStandardVariableReference(1));
        propRef2.setStandardVariables(getDummyStandardVariableReference(2));
        propRef3.setStandardVariables(getDummyStandardVariableReference(3));
        
        propList.add(propRef1);
        propList.add(propRef2);
        propList.add(propRef3);
        return propList;
    }
    
    public List<TraitReference> getDummyData(){
        List<TraitReference> refList = new ArrayList();
        TraitReference ref1 = new TraitReference(1, "Test 1");
        TraitReference ref2 = new TraitReference(2, "Test 2");
        TraitReference ref3 = new TraitReference(3, "Test 3");
        
                
        ref1.setProperties(getDummyPropertyReference(1));
        ref2.setProperties(getDummyPropertyReference(2));
        ref3.setProperties(getDummyPropertyReference(3));
        
        refList.add(ref1);
        refList.add(ref2);
        refList.add(ref3);
        return refList;
    }
   
    /**
     * Show the main import page
     *
     * @param form the form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("ontologyBrowserForm") OntologyBrowserForm form, Model model) {
        //this set the necessary info from the session variable
        //OntologyDataManager.getTraitGroups()
        try {
            List<TraitReference> traitRefList = getDummyData();
            form.setTraitReferenceList(traitRefList);
            form.setTreeData(TreeViewUtil.convertOntologyTraitsToJson(traitRefList));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	return super.show(model);
    }
  

}
