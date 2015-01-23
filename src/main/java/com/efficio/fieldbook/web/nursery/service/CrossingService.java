package com.efficio.fieldbook.web.nursery.service;

import com.efficio.fieldbook.web.nursery.bean.ImportedCrossesList;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by cyrus on 1/23/15.
 */
public interface CrossingService {

	ImportedCrossesList parseFile(MultipartFile file);
}
