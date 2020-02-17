
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldmapBlockInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.service.ExportDataCollectionOrderService;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;

@Service
@Transactional
public class ExportOrderingSerpentineOverColImpl extends ExportDataCollectionOrderService {

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Override
	public void reorderWorkbook(Workbook workbook) {
		List<MeasurementRow> arrangedExportObservations = new ArrayList<MeasurementRow>();
		try {
			Integer numberOfTrialInstance = workbook.getTrialObservations().size();
			for (int trialInstanceNum = 1; trialInstanceNum <= numberOfTrialInstance; trialInstanceNum++) {

				String blockId =
						this.fieldbookMiddlewareService.getBlockId(workbook.getTrialDatasetId(), trialInstanceNum);
				List<Integer> indexes = new ArrayList<Integer>();
				indexes.add(trialInstanceNum);

				List<MeasurementRow> observations =
						ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getObservations(), indexes);
				List<MeasurementRow> observationsPerInstance = new ArrayList<MeasurementRow>();
				if (blockId == null) {
					// meaning no fieldmap
					// we just set the normal observations
					arrangedExportObservations.addAll(observations);
				} else {
					FieldmapBlockInfo blockInfo = this.fieldbookMiddlewareService.getBlockInformation(Integer.valueOf(blockId));
					int ranges = blockInfo.getRangesInBlock();
					int columns = blockInfo.getRowsInBlock() / blockInfo.getNumberOfRowsInPlot();

					// we now need to arrange
					// we set it to map first then we iterate now

					Map<String, MeasurementRow> fieldMapExperimentMap = this.getFieldMapExperimentsMap(observations);

					boolean downToUp = true;
					for (int x = 1; x <= columns; x++) {
						if (downToUp) {
							for (int y = 0; y <= ranges; y++) {
								// for left to right planting
								String coordinateKey = Integer.toString(x) + ":" + Integer.toString(y);
								MeasurementRow rowExperiment = fieldMapExperimentMap.get(coordinateKey);
								if (rowExperiment != null) {
									observationsPerInstance.add(rowExperiment);
								}
							}
						} else {
							for (int y = ranges; y >= 0; y--) {
								// for right to left planting
								String coordinateKey = Integer.toString(x) + ":" + Integer.toString(y);
								MeasurementRow rowExperiment = fieldMapExperimentMap.get(coordinateKey);
								if (rowExperiment != null) {
									observationsPerInstance.add(rowExperiment);
								}
							}
						}

						downToUp = !downToUp;
					}
					arrangedExportObservations.addAll(observationsPerInstance);
				}
			}
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		workbook.setExportArrangedObservations(arrangedExportObservations);

	}

}
