package com.efficio.fieldbook.web;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/variableCache")
public class VariableCacheController {
	
	@Resource(name = "ontologyVariableManager")
	private OntologyVariableDataManager ontologyVariableDataManager;

	@ResponseBody
	@RequestMapping(value = "/deleteVariablesFromCache", method = RequestMethod.DELETE)
	public String deleteVariablesFromCache(@RequestBody final List<Integer> variablesIds) {
		this.ontologyVariableDataManager.deleteVariablesFromCache(variablesIds);
		return "OK";
	}
}
