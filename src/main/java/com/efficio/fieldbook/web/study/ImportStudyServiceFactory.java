
package com.efficio.fieldbook.web.study;

import com.efficio.fieldbook.web.study.service.ImportStudyService;
import com.efficio.fieldbook.web.study.service.impl.CsvImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.ExcelImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.KsuCsvImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.KsuExcelImportStudyServiceImpl;
import org.generationcp.middleware.domain.etl.Workbook;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import javax.annotation.Resource;

public class ImportStudyServiceFactory {

    @Resource
    protected AutowireCapableBeanFactory beanFactory;

	public ImportStudyService createStudyImporter(final ImportStudyType importStudyType, final Workbook workbook, final String currentFile, final String originalFileName) {
        ImportStudyService studyService = null;
		switch (importStudyType) {
			case IMPORT_KSU_CSV:
				studyService = new KsuCsvImportStudyServiceImpl(workbook, currentFile, originalFileName);
                break;
            case IMPORT_KSU_EXCEL:
                studyService = new KsuExcelImportStudyServiceImpl(workbook, currentFile, originalFileName);
                break;
            case IMPORT_NURSERY_CSV:
                studyService = new CsvImportStudyServiceImpl(workbook, currentFile, originalFileName);
                break;
            // we use Nursery Excel as the default import study service
            case IMPORT_NURSERY_EXCEL:

            default :
                studyService = new ExcelImportStudyServiceImpl(workbook, currentFile, originalFileName);
                break;

		}

        if (studyService != null) {
            beanFactory.autowireBeanProperties(studyService, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        }

        return studyService;
	}

}
