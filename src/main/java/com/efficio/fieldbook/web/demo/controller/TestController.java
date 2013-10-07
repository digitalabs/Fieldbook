package com.efficio.fieldbook.web.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.bean.demo.TestJavaBean;
import com.efficio.fieldbook.web.form.demo.TestJavaForm;

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
@RequestMapping({"/test"})
public class TestController extends AbstractBaseFieldbookController{
	
	
	
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("testForm") TestJavaForm testForm,  BindingResult result, Model model) {

    	
    	model.addAttribute("testList", getDummyList());
    
    	return super.show(model);
    }

    private List getDummyList(){
    	List l = new ArrayList();
    	l.add("Hello 1");
    	l.add("Hello 2");
    	l.add("Hello 3");
    	return l;
    }

    @Override
    public String getContentName() {
        return "demo/test";
    }
   
}