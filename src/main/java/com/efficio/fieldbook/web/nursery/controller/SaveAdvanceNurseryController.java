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
package com.efficio.fieldbook.web.nursery.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;

@Controller
@RequestMapping(SaveAdvanceNurseryController.URL)
public class SaveAdvanceNurseryController extends AbstractBaseFieldbookController {

	public static final String URL = "/NurseryManager/saveAdvanceNursery";
	
	public static final String PAGINATION_TEMPLATE = "/NurseryManager/showSaveAdvanceNurseryPagination";

	@Override
	public String getContentName() {
		return "NurseryManager/saveAdvanceNursery";
	}


	@RequestMapping(value = "/page/{pageNum}", method = RequestMethod.GET)
	public String getPaginatedList(@PathVariable int pageNum, @ModelAttribute("advancingNurseryform") AdvancingNurseryForm form, 
				Model model, HttpServletRequest req) {
		
		String listIdentifier = req.getParameter("listIdentifier");
		AdvancingNurseryForm formFromSession = getPaginationListSelection().getAdvanceDetails(listIdentifier);
		List<ImportedGermplasm> importedAdvanceGermplasmList = formFromSession.getGermplasmList();

		if (importedAdvanceGermplasmList != null) {
			form.setGermplasmList(importedAdvanceGermplasmList);
			form.setEntries(importedAdvanceGermplasmList.size());
			form.changePage(pageNum);
		}
		
		return super.showAjaxPage(model, PAGINATION_TEMPLATE);
	}
}