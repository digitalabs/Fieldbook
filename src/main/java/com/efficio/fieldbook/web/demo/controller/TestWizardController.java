package com.efficio.fieldbook.web.demo.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.form.Test2JavaForm;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping({"/testWizard"})
public class TestWizardController extends AbstractBaseFieldbookController{
	
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("test2JavaForm") Test2JavaForm testForm,  Model model) {
    	return super.show(model);
    }
    

    @RequestMapping(value="doSubmit", method = RequestMethod.POST)
    public String submit(@ModelAttribute("test2JavaForm") Test2JavaForm testForm, BindingResult result, Model model) {
        return show(testForm,model);
    }

    @Override
    public String getContentName() {
        return "demo/testWizard";
    }
   
}