package com.efficio.fieldbook.web.nursery.service.impl;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.nursery.bean.ImportedCrossesList;
import com.efficio.fieldbook.web.nursery.service.CrossingService;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * Created by cyrus on 1/23/15.
 */
public class CrossingServiceImpl implements CrossingService {

	@Resource
	private FileService fileService;

	@Override
	public ImportedCrossesList parseFile(MultipartFile file) {
		CrossingTemplateParser crossingTemplateParser = new CrossingTemplateParser(fileService);

		return crossingTemplateParser.parseFile(file);
	}
}
