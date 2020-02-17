
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.service.ExportDataCollectionOrderService;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;

@Service
@Transactional
public class ExportOrderingSerpentineOverRangeImpl extends ExportDataCollectionOrderService {

	private static final Logger LOG = LoggerFactory.getLogger(ExportOrderingSerpentineOverRangeImpl.class);

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

				List<MeasurementRow> observationsPerInstance = new ArrayList<MeasurementRow>();
				List<Integer> indexes = new ArrayList<Integer>();
				indexes.add(trialInstanceNum);

				List<MeasurementRow> observations =
						ExportImportStudyUtil.getApplicableObservations(workbook, workbook.getObservations(), indexes);
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

					boolean leftToRight = true;
					for (int y = 1; y <= ranges; y++) {
						if (leftToRight) {
							for (int x = 0; x <= columns; x++) {
								// for left to right planting
								String coordinateKey = Integer.toString(x) + ":" + Integer.toString(y);
								MeasurementRow rowExperiment = fieldMapExperimentMap.get(coordinateKey);
								if (rowExperiment != null) {
									observationsPerInstance.add(rowExperiment);
								}
							}
						} else {
							for (int x = columns; x >= 0; x--) {
								// for right to left planting
								String coordinateKey = Integer.toString(x) + ":" + Integer.toString(y);
								MeasurementRow rowExperiment = fieldMapExperimentMap.get(coordinateKey);
								if (rowExperiment != null) {
									observationsPerInstance.add(rowExperiment);
								}
							}
						}

						leftToRight = !leftToRight;
					}
					arrangedExportObservations.addAll(observationsPerInstance);
				}
			}
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			ExportOrderingSerpentineOverRangeImpl.LOG.error("Ordering of the workbook was not successful", e);
		}

		workbook.setExportArrangedObservations(arrangedExportObservations);

	}

}
