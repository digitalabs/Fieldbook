package com.efficio.fieldbook.web.demo;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.form.demo.Test3JavaForm;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping({"/test3"})
public class Test3Controller extends AbstractBaseFieldbookController{
	
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("test3Form") Test3JavaForm testForm,  Model model) {
    		return super.show(model);
    }

    @Override
    public String getContentName() {
        return "demo/test3";
    }
   
}