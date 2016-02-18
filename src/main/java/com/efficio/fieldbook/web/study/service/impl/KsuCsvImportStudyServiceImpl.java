
package com.efficio.fieldbook.web.study.service.impl;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.study.KsuCsvWorkbookParser;
import com.efficio.fieldbook.web.study.service.ImportStudyService;
import org.generationcp.commons.parsing.FileParsingException;
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
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.ImportStudyUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

@Service
@Transactional
public class KsuCsvImportStudyServiceImpl implements ImportStudyService {

	@Resource
	protected FieldbookService fieldbookMiddlewareService;

	@Resource
	protected OntologyService ontologyService;

	@Resource
	protected ValidationService validationService;

	@Resource
	protected AutowireCapableBeanFactory beanFactory;

	@Override
	public ImportResult importWorkbook(final Workbook workbook, final String filename, final String originalFilename)
			throws WorkbookParserException {

		final String trialInstanceNo = ImportStudyUtil.getTrialInstanceNo(workbook, originalFilename);
		final Map<String, MeasurementRow> rowsMap =
				ImportStudyUtil.createMeasurementRowsMap(workbook.getObservations(), trialInstanceNo, workbook.isNursery());

		try {
			final KsuCsvWorkbookParser ksuCsvWorkbookParser = new KsuCsvWorkbookParser(workbook, trialInstanceNo, rowsMap);

			this.beanFactory.autowireBean(ksuCsvWorkbookParser);

			ksuCsvWorkbookParser.parseFile(filename);

			SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), true,
					this.ontologyService);

			this.validationService.validateObservationValues(workbook, trialInstanceNo);

			return new ImportResult(ksuCsvWorkbookParser.getModes(), new ArrayList<GermplasmChangeDetail>());

		} catch (final FileParsingException e) {
			WorkbookUtil.resetWorkbookObservations(workbook);
			throw new WorkbookParserException(e.getMessage(), e);
		}

	}

}
