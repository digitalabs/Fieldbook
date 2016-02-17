package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.service.CsvImportStudyService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.ImportStudyUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Service
@Transactional
public class CsvImportStudyServiceImpl implements CsvImportStudyService{
	
	@Resource
	protected FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;
	
	@Resource
	private ValidationService validationService;
	
	@Resource
	protected AutowireCapableBeanFactory beanFactory;
	
	@Override
	public ImportResult importWorkbook(final Workbook workbook, String filename, String originalFilename) throws WorkbookParserException {
		final String trialInstanceNo = ImportStudyUtil.getTrialInstanceNo(workbook,originalFilename);
		final Map<String,MeasurementRow> rowsMap = ImportStudyUtil.createMeasurementRowsMap(workbook.getObservations(), trialInstanceNo, workbook.isNursery());
		
		try {
			CsvWorkbookParser csvWorkbookParser = new CsvWorkbookParser(workbook, trialInstanceNo, rowsMap);
			
			beanFactory.autowireBean(csvWorkbookParser);
			
			csvWorkbookParser.parseFile(filename);

			SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), true, ontologyService);

			this.validationService.validateObservationValues(workbook, trialInstanceNo);
			return new ImportResult(csvWorkbookParser.getModes(), new ArrayList<GermplasmChangeDetail>());		
		} catch (Exception e) {
			WorkbookUtil.resetWorkbookObservations(workbook);
			throw new WorkbookParserException(e.getMessage(),e);
		}
	}
}
