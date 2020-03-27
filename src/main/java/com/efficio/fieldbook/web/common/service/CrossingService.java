
package com.efficio.fieldbook.web.common.service;

import com.efficio.fieldbook.web.common.exception.InvalidInputException;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.pojos.Germplasm;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CrossingService {

	ImportedCrossesList parseFile(MultipartFile file) throws FileParsingException;

	String getCross(Germplasm germplasm, ImportedCross crosses, String separator, String cropName);

	boolean applyCrossSetting(CrossSetting crossSetting, ImportedCrossesList importedCrossesList, Integer userId, Workbook workbook);

	boolean applyCrossSettingWithNamingRules(CrossSetting crossSetting, ImportedCrossesList importedCrossesList, Integer userId, Workbook
			workbook);

	void processCrossBreedingMethod(CrossSetting crossSetting, ImportedCrossesList importedCrossesList);

	void populateSeedSource(ImportedCrossesList importedCrossesList, final Workbook workbook);

	String getNextNameInSequence(final CrossNameSetting setting) throws InvalidInputException;
}
