package com.efficio.fieldbook.web.demo;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.bean.demo.TestJavaBean;
import com.efficio.fieldbook.web.form.demo.TestJavaForm;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */

@Controller
@RequestMapping({"/","/fieldbook"})
public class TestFieldbookController extends AbstractBaseFieldbookController{
	/*
    @Resource
    private ETLService etlService;

    @Resource
    private UserSelection userSelection;
	*/
	@Resource
	private TestJavaBean bean;
	
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("testForm") TestJavaForm testForm, Model model) {
    	return super.show(model);
    }

    @RequestMapping(method = RequestMethod.POST)
    public String uploadFile(@ModelAttribute("testForm") TestJavaForm testForm, BindingResult result, Model model) {
        //FileUploadFormValidator validator = new FileUploadFormValidator();
        //validator.validate(uploadForm, result);

    	//for adding of error
    	result.reject("testForm.username", "test error msg");
    	bean.setAge("10");
    	bean.setName("Hello"+System.currentTimeMillis());
    	
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
        return "demo/testPage";
    }
   
}