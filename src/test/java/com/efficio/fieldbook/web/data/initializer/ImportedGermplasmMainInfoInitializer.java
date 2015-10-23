
package com.efficio.fieldbook.web.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;

public class ImportedGermplasmMainInfoInitializer {

	public static ImportedGermplasmMainInfo createImportedGermplasmMainInfo() {
		final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
		final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
		importedGermplasmList.setImportedGermplasms(createImportedGermplasmList());
		mainInfo.setImportedGermplasmList(importedGermplasmList);
		return mainInfo;
	}

	public static List<ImportedGermplasm> createImportedGermplasmList() {
		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		for (int x = 1; x <= DesignImportTestDataInitializer.NO_OF_TEST_ENTRIES; x++) {
			importedGermplasmList.add(createImportedGermplasm(x));
		}

		return importedGermplasmList;
	}

	public static ImportedGermplasm createImportedGermplasm(final int entryNo) {
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setEntryId(entryNo);
		importedGermplasm.setEntryCode(String.valueOf(entryNo));
		importedGermplasm.setDesig("DESIG" + entryNo);
		importedGermplasm.setSource("SOURCE" + entryNo);
		importedGermplasm.setBreedingMethodId(0);
		importedGermplasm.setCheck("");
		importedGermplasm.setGid("");
		importedGermplasm.setCheckId(0);
		importedGermplasm.setCheckName("");
		importedGermplasm.setCross("");
		importedGermplasm.setGnpgs(0);
		importedGermplasm.setGpid1(0);
		importedGermplasm.setGpid2(0);
		importedGermplasm.setGroupName("");
		importedGermplasm.setIndex(0);
		importedGermplasm.setNames(null);

		return importedGermplasm;
	}
}
