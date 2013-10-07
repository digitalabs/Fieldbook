package com.efficio.fieldbook.web.demo.controller;


import javax.annotation.Resource;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.bean.TestJavaBean;
import com.efficio.fieldbook.web.demo.form.Test2JavaForm;
import com.efficio.fieldbook.web.demo.validation.TestValidator;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */

@Controller
@RequestMapping({"/test2"})
public class Test2Controller extends AbstractBaseFieldbookController{
	@Resource
	private GermplasmDataManager germplasmDataManager;
	@Resource
	private TestJavaBean testJavaBean;
	
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("test2Form") Test2JavaForm testForm,  Model model) {

    	
    	try {
    		System.out.println(testJavaBean.getName());
			testForm.setLocationList(germplasmDataManager.getAllBreedingLocations());
			testForm.setMethodList(germplasmDataManager.getAllMethods());
			
			
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return super.show(model);
    }

    @RequestMapping(value="doSubmit", method = RequestMethod.POST)
    public String uploadFile(@ModelAttribute("test2Form") Test2JavaForm testForm, BindingResult result, Model model) {
        //FileUploadFormValidator validator = new FileUploadFormValidator();
        //validator.validate(uploadForm, result);

    	//for adding of error
    	//result.reject("testForm.username", "test error msg");
    	
    	TestValidator validator = new TestValidator();
    	validator.validate(testForm, result);
    	
        if (result.hasErrors()) {
            /**
             * Return the user back to form to show errors
             */
            return show(testForm,model);
        } else {

            
            // at this point, we can assume that program has reached an error condition. we return user to the form

            return show(testForm,model);
        }
    }

    @Override
    public String getContentName() {
        return "demo/test2";
    }
   
}