package com.efficio.fieldbook.web.common.service;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

public interface MergeCheckService {

	List<ImportedGermplasm> mergeGermplasmList(List<ImportedGermplasm> primaryList, 
			List<ImportedGermplasm> checkList, int startIndex, int interval, 
			int manner);
}
