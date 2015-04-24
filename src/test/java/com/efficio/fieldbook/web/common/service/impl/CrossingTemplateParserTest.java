package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.common.exception.FileParsingException;
import com.efficio.fieldbook.web.nursery.bean.*;
import com.efficio.fieldbook.web.util.DateUtil;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrossingTemplateParserTest {

}
