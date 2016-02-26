
package com.efficio.fieldbook.web.study;

import com.efficio.fieldbook.web.study.service.ImportStudyService;
import com.efficio.fieldbook.web.study.service.impl.CsvImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.DataKaptureImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.ExcelImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.FieldroidImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.KsuCsvImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.KsuExcelImportStudyServiceImpl;
import org.generationcp.middleware.domain.etl.Workbook;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import javax.annotation.Resource;

public class StudyServiceFactory {

    @Resource
    protected AutowireCapableBeanFactory beanFactory;

	public ImportStudyService createStudyImporter(final ImportStudyType importStudyType, final Workbook workbook, final String currentFile, final String originalFileName) {
        ImportStudyService studyService = null;
		switch (importStudyType) {
			case IMPORT_DATAKAPTURE:
				studyService =  new DataKaptureImportStudyServiceImpl(workbook, currentFile, originalFileName);
                break;
			case IMPORT_KSU_CSV:
				studyService = new KsuCsvImportStudyServiceImpl(workbook, currentFile, originalFileName);
                break;
            case IMPORT_KSU_EXCEL:
                studyService = new KsuExcelImportStudyServiceImpl(workbook, currentFile, originalFileName);
                break;
            case IMPORT_NURSERY_CSV:
                studyService = new CsvImportStudyServiceImpl(workbook, currentFile, originalFileName);
                break;
            case IMPORT_NURSERY_EXCEL:
                studyService = new ExcelImportStudyServiceImpl(workbook, currentFile, originalFileName);
                break;
            case IMPORT_NURSERY_FIELDLOG_FIELDROID:
                studyService = new FieldroidImportStudyServiceImpl(workbook, currentFile, originalFileName);
                break;
            default :

		}

        if (studyService != null) {
            beanFactory.autowireBeanProperties(studyService, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        }

        return studyService;
	}

}