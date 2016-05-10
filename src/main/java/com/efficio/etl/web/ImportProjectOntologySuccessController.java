
package com.efficio.etl.web;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.UserSelection;

@Controller
@RequestMapping(ImportProjectOntologySuccessController.URL)
public class ImportProjectOntologySuccessController extends AbstractBaseETLController {

	public static final String URL = "/workbook/importProjectOntology";

	@Resource
	private ETLService etlService;

  	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Override
	@RequestMapping(method = RequestMethod.GET)
	public String show(Model model) {
		return this.getContentName();
	}

	@Override
	public String getContentName() {
		return "importProjectOntology";
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

}
