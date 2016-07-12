package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import javax.annotation.Resource;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.service.KsuCsvImportStudyService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class KsuCsvImportStudyServiceImpl extends KsuExcelImportStudyServiceImpl implements KsuCsvImportStudyService {

	@Resource
	protected FieldbookService fieldbookMiddlewareService;

	@Resource
	protected OntologyService ontologyService;

	@Resource
	protected ValidationService validationService;
	
	@Resource
	ContextUtil contextUtil;

	@Resource
	protected AutowireCapableBeanFactory beanFactory;

	@Override
	public ImportResult importWorkbook(final Workbook workbook, String filename, String originalFilename) throws WorkbookParserException {

		final String trialInstanceNo = getTrialInstanceNo(workbook,originalFilename);
		final Map<String,MeasurementRow> rowsMap = createMeasurementRowsMap(workbook.getObservations(), trialInstanceNo, workbook.isNursery());

		try {
			KsuCsvWorkbookParser ksuCsvWorkbookParser = new KsuCsvWorkbookParser(this, workbook, trialInstanceNo, rowsMap);
			// make this a spring obj!
			beanFactory.autowireBean(ksuCsvWorkbookParser);

			ksuCsvWorkbookParser.parseFile(filename);

			SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), true, ontologyService, contextUtil.getCurrentProgramUUID());

			this.validationService.validateObservationValues(workbook, trialInstanceNo);

			return new ImportResult(new HashSet<ChangeType>(), new ArrayList<GermplasmChangeDetail>());

		} catch (FileParsingException e) {
			WorkbookUtil.resetWorkbookObservations(workbook);
			throw new WorkbookParserException(e.getMessage(),e);
		}

	}

}
