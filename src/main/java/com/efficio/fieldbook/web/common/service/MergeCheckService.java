package com.efficio.fieldbook.web.common.service;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;

import java.util.List;

public interface MergeCheckService {

	List<ImportedGermplasm> mergeGermplasmList(List<ImportedGermplasm> primaryList, 
			List<ImportedGermplasm> checkList, int startIndex, int interval, 
			int manner, String defaultTestCheckId);
}
