
package com.efficio.fieldbook.web.study;

import com.efficio.fieldbook.web.study.service.ImportStudyService;
import com.efficio.fieldbook.web.study.service.impl.CsvImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.DataKaptureImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.ExcelImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.FieldroidImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.KsuCsvImportStudyServiceImpl;
import com.efficio.fieldbook.web.study.service.impl.KsuExcelImportStudyServiceImpl;

import javax.annotation.Resource;

public class StudyServiceFactory {

	@Resource
	private CsvImportStudyServiceImpl csvImportStudyService;

	@Resource
	private DataKaptureImportStudyServiceImpl dataKaptureImportStudyService;

	@Resource
	private ExcelImportStudyServiceImpl excelImportStudyService;

	@Resource
	private FieldroidImportStudyServiceImpl fieldroidImportStudyService;

	@Resource
	private KsuCsvImportStudyServiceImpl ksuCsvImportStudyService;

	@Resource
	private KsuExcelImportStudyServiceImpl ksuExcelImportStudyService;

	public ImportStudyService retrieveStudyImporter(final ImportStudyType importStudyType) {
		switch (importStudyType) {
			case IMPORT_DATAKAPTURE:
				return dataKaptureImportStudyService;
			case IMPORT_KSU_CSV:
				return ksuCsvImportStudyService;
            case IMPORT_KSU_EXCEL:
                return ksuExcelImportStudyService;
            case IMPORT_NURSERY_CSV:
                return csvImportStudyService;
            case IMPORT_NURSERY_EXCEL:
                return excelImportStudyService;
            case IMPORT_NURSERY_FIELDLOG_FIELDROID:
                return fieldroidImportStudyService;
            default :
                return null;
		}
	}

}
