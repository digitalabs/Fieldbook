
package com.efficio.fieldbook.web.common.service.impl;

import org.generationcp.middleware.domain.etl.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.service.ExportDataCollectionOrderService;

@Service
@Transactional
public class ExportOrderingRowColImpl extends ExportDataCollectionOrderService {

	@Override
	public void reorderWorkbook(Workbook workbook) {
		// for now, nothing needs to be done
		workbook.setExportArrangedObservations(workbook.getObservations());
	}

}
