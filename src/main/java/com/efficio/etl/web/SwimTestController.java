
package com.efficio.etl.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.CategorizationForm;
import com.efficio.etl.web.bean.UserSelection;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@Controller
@RequestMapping("/test/swimlanes")
public class SwimTestController {

    private static final Logger LOG = LoggerFactory.getLogger(SwimTestController.class);

	@Resource
	private ETLService etlService;

	// user selection is no longer tied to session in this test controller. instead, it uses hardcoded values located in this class def
	private UserSelection userSelection;

	public void provideTestUserSelection() {
		this.userSelection = new UserSelection();
		this.userSelection.setActualFileName("Population114_Pheno_DSI_1.xls");
		this.userSelection.setServerFileName("Population114_Pheno_DSI_1.xls");
		this.userSelection.setSelectedSheet(1);
		this.userSelection.setHeaderRowIndex(0);
		this.userSelection.setContentRowIndex(1);
		this.userSelection.setIndexColumnIndex(1);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String show(Model model) {

		model.addAttribute("templateName", "/etl/categorizeHeaders");

		return "etl/base-template";

	}

	@ModelAttribute("headerList")
	public List<String> getHeaderList() {
		try {
			this.provideTestUserSelection();
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			List<String> headers = this.etlService.retrieveColumnHeaders(workbook, this.userSelection, Boolean.FALSE);

			if (headers != null) {
				return headers;
			}
		} catch (IOException e) {
		  	SwimTestController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<String>();
	}

	@ModelAttribute("categorizationForm")
	public CategorizationForm getForm() {
		return new CategorizationForm();
	}
}
