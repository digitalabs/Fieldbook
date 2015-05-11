package com.efficio.fieldbook.web.nursery.controller;

/**
 * Created by cyrus on 5/8/15.
 */

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The Class CreateNurseryController.
 */
@Controller
@RequestMapping(DesignImportController.URL)
public class DesignImportController extends AbstractBaseFieldbookController {

	public static final String URL = "/DesignImport";

	/* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#show(org.springframework.ui.Model)
     */
	@RequestMapping(method = RequestMethod.GET)
	public String show(Model model) {
		return super.showAngularPage(model);
	}

	@Override public String getContentName() {
		return String.format("%s/designImportMain",URL);
	}
}
