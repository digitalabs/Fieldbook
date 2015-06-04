
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.service.MergeCheckService;
import com.efficio.fieldbook.web.util.AppConstants;

@Service
public class MergeCheckServiceImpl implements MergeCheckService {

	@Override
	public List<ImportedGermplasm> mergeGermplasmList(List<ImportedGermplasm> primaryList, List<ImportedGermplasm> checkList,
			int startEntry, int interval, int manner, String defaultTestCheckId) {

		if (checkList == null || checkList.isEmpty() || startEntry < 1 || startEntry > primaryList.size() || primaryList == null
				|| primaryList.isEmpty() || interval < 1) {

			return primaryList;
		}

		List<ImportedGermplasm> newList = new ArrayList<ImportedGermplasm>();

		int primaryEntry = 1;
		int newEntry = 1;
		boolean isStarted = false;
		boolean shouldInsert = false;
		int checkIndex = 0;
		int intervalEntry = 0;
		for (ImportedGermplasm primaryGermplasm : primaryList) {
			if (primaryEntry == startEntry || intervalEntry == interval) {
				isStarted = true;
				shouldInsert = true;
				intervalEntry = 0;
			}

			if (isStarted) {
				intervalEntry++;
			}

			if (shouldInsert) {
				shouldInsert = false;
				List<ImportedGermplasm> checks = this.generateChecksToInsert(checkList, checkIndex, manner, newEntry);
				checkIndex++;
				newEntry += checks.size();
				intervalEntry += checks.size();

				newList.addAll(checks);
			}
			ImportedGermplasm primaryNewGermplasm = this.assignNewGermplasm(primaryGermplasm, newEntry);
			if (defaultTestCheckId != null && !defaultTestCheckId.equalsIgnoreCase("")) {
				primaryNewGermplasm.setCheck(defaultTestCheckId);
				primaryNewGermplasm.setCheckId(Integer.valueOf(defaultTestCheckId));
			}
			newList.add(primaryNewGermplasm);
			newEntry++;

			primaryEntry++;
		}

		return newList;
	}

	private List<ImportedGermplasm> generateChecksToInsert(List<ImportedGermplasm> checkList, int checkIndex, int manner, int newEntry) {
		List<ImportedGermplasm> newList = new ArrayList<ImportedGermplasm>();
		if (manner == AppConstants.MANNER_PER_LOCATION.getInt()) {
			for (ImportedGermplasm checkGerm : checkList) {
				newList.add(this.assignNewGermplasm(checkGerm, newEntry));
				newEntry++;
			}
		} else {
			checkIndex = checkIndex % checkList.size();
			ImportedGermplasm checkGerm = checkList.get(checkIndex);
			newList.add(this.assignNewGermplasm(checkGerm, newEntry));
		}
		return newList;
	}

	private ImportedGermplasm assignNewGermplasm(ImportedGermplasm source, int entryNumber) {
		ImportedGermplasm germplasm = source.copy();
		germplasm.setEntryId(entryNumber);
		germplasm.setEntryCode(String.valueOf(entryNumber));
		return germplasm;
	}
}
