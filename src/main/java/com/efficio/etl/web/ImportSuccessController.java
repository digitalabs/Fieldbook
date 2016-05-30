
package com.efficio.etl.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.etl.web.bean.UserSelection;

/**
 * Created by IntelliJ IDEA. User: Aldrin Batac
 */

@Controller
@RequestMapping(ImportSuccessController.URL)
public class ImportSuccessController extends AbstractBaseETLController {

	public static final String URL = "/etl/workbook/importSuccess";

	@Override
	@RequestMapping(method = RequestMethod.GET)
	public String show(Model model) {
		return this.getContentName();
	}

	@Override
	public String getContentName() {
		return "etl/importSuccess";
	}

	@Override
	public UserSelection getUserSelection() {
		// TODO Auto-generated method stub
		return null;
	}

}
