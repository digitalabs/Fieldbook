
package com.efficio.fieldbook.web.common.service;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Germplasm;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by cyrus on 1/23/15.
 */
public interface CrossingService {

	ImportedCrossesList parseFile(MultipartFile file) throws FileParsingException;

	String getCross(Germplasm germplasm, ImportedCrosses crosses, String separator);

	void applyCrossSetting(CrossSetting crossSetting, GermplasmOriginGenerationParameters germplasmOriginGenerationParameters,
			ImportedCrossesList importedCrossesList, Integer userId) throws MiddlewareQueryException;
}
