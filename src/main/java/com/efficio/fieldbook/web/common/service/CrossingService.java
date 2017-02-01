
package com.efficio.fieldbook.web.common.service;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.pojos.Germplasm;
import org.springframework.web.multipart.MultipartFile;

public interface CrossingService {

	ImportedCrossesList parseFile(MultipartFile file) throws FileParsingException;

	String getCross(Germplasm germplasm, ImportedCrosses crosses, String separator);

	boolean applyCrossSetting(CrossSetting crossSetting, ImportedCrossesList importedCrossesList, Integer userId, Workbook workbook);

	void updateCrossSetting(CrossSetting crossSetting, ImportedCrossesList importedCrossesList);

	boolean applyCrossSettingWithNamingRules(CrossSetting crossSetting, ImportedCrossesList importedCrossesList, Integer userId, Workbook
			workbook);

	void processCrossBreedingMethod(CrossSetting crossSetting, ImportedCrossesList importedCrossesList);

	void populateSeedSource(ImportedCrosses importedCross, Workbook workbook);
}
