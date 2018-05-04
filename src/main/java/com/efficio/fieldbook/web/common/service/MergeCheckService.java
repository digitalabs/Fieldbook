
package com.efficio.fieldbook.web.common.service;

import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;

import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;

public interface MergeCheckService {

	List<ImportedGermplasm> mergeGermplasmList(List<ImportedGermplasm> primaryList, List<ImportedGermplasm> checkList, int startIndex,
			int interval, int manner);

	void updatePrimaryListAndChecksBeforeMerge(ImportGermplasmListForm form);
}
