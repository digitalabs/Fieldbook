
package com.efficio.fieldbook.web.importdesign.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

public class DesignImportMeasurementRowGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(DesignImportMeasurementRowGenerator.class);

	private Workbook workbook;
	private Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders;
	private List<String> rowValues;
	private List<ImportedGermplasm> importedGermplasm;
	private Map<Integer, StandardVariable> germplasmStandardVariables;
	private Set<String> trialInstancesFromUI;
	private boolean isPreview;
	private Map<String, Integer> availableCheckTypes;

	public DesignImportMeasurementRowGenerator() {
		super();
	}

	public DesignImportMeasurementRowGenerator(final Workbook workbook, final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders,
			final List<ImportedGermplasm> importedGermplasm, final Map<Integer, StandardVariable> germplasmStandardVariables,
			final Set<String> trialInstancesFromUI, final boolean isPreview, final Map<String, Integer> availableCheckTypes) {
		super();
		this.workbook = workbook;
		this.mappedHeaders = mappedHeaders;
		this.importedGermplasm = importedGermplasm;
		this.germplasmStandardVariables = germplasmStandardVariables;
		this.trialInstancesFromUI = trialInstancesFromUI;
		this.isPreview = isPreview;
		this.availableCheckTypes = availableCheckTypes;
	}

	public MeasurementRow createMeasurementRow(final List<String> rowValues, final FieldbookService fieldbookService) {

		final MeasurementRow measurement = new MeasurementRow();

		final List<MeasurementData> dataList = new ArrayList<>();

		for (final Entry<PhenotypicType, List<DesignHeaderItem>> entry : this.mappedHeaders.entrySet()) {
			for (final DesignHeaderItem headerItem : entry.getValue()) {

				// do not add the trial instance record from file if it is not selected in environment tab
				if (headerItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId()
						&& !this.trialInstancesFromUI.contains(rowValues.get(headerItem.getColumnIndex()))) {
					return null;
				}

				if (headerItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId()
						&& this.workbook.getStudyDetails().getStudyType() == StudyType.N) {

					// do not add the trial instance to measurement data list if the workbook is Nursery
					continue;
				}

				if (headerItem.getVariable().getId() == TermId.ENTRY_NO.getId()) {

					final Integer entryNo = Integer.parseInt(rowValues.get(headerItem.getColumnIndex()));
					this.addGermplasmDetailsToDataList(this.importedGermplasm, this.germplasmStandardVariables, dataList, entryNo,
							fieldbookService);
				}

				if (headerItem.getVariable().getId() == TermId.ENTRY_TYPE.getId()) {
					final String checkType = String.valueOf(rowValues.get(headerItem.getColumnIndex()));
					final String checkTypeId = String.valueOf(this.availableCheckTypes.get(checkType));
					dataList.add(this.createMeasurementData(this.germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()), checkTypeId,
							fieldbookService));
				}

				if (headerItem.getVariable().getPhenotypicType() == PhenotypicType.TRIAL_ENVIRONMENT && this.isPreview) {

					// only add the trial environment factors in measurement row ONLY in PREVIEW mode
					final String value = rowValues.get(headerItem.getColumnIndex());
					dataList.add(this.createMeasurementData(headerItem.getVariable(), value, fieldbookService));
				}

				if (headerItem.getVariable().getPhenotypicType() == PhenotypicType.TRIAL_DESIGN
						|| headerItem.getVariable().getPhenotypicType() == PhenotypicType.VARIATE
						|| headerItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {

					final String value = rowValues.get(headerItem.getColumnIndex());
					dataList.add(this.createMeasurementData(headerItem.getVariable(), value, fieldbookService));

				}

			}
		}

		measurement.setDataList(dataList);
		return measurement;
	}

	protected void addGermplasmDetailsToDataList(final List<ImportedGermplasm> importedGermplasm,
			final Map<Integer, StandardVariable> germplasmStandardVariables, final List<MeasurementData> dataList, final Integer entryNo,
			final FieldbookService fieldbookService) {

		final ImportedGermplasm germplasmEntry = importedGermplasm.get(entryNo - 1);

		if (germplasmStandardVariables.get(TermId.ENTRY_NO.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_NO.getId()), germplasmEntry.getEntryId()
					.toString(), fieldbookService));
		}
		if (germplasmStandardVariables.get(TermId.GID.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.GID.getId()), germplasmEntry.getGid(),
					fieldbookService));
		}
		if (germplasmStandardVariables.get(TermId.DESIG.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.DESIG.getId()), germplasmEntry.getDesig(),
					fieldbookService));
		}
		if (germplasmStandardVariables.get(TermId.CROSS.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.CROSS.getId()), germplasmEntry.getCross(),
					fieldbookService));
		}
		if (germplasmStandardVariables.get(TermId.ENTRY_CODE.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_CODE.getId()),
					germplasmEntry.getEntryCode(), fieldbookService));
		}
		if (germplasmStandardVariables.get(TermId.GERMPLASM_SOURCE.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.GERMPLASM_SOURCE.getId()),
					germplasmEntry.getSource(), fieldbookService));
		}
		if (germplasmStandardVariables.get(TermId.SEED_SOURCE.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.SEED_SOURCE.getId()), germplasmEntry.getSource(),
					fieldbookService));
		}
	}

	protected MeasurementData createMeasurementData(final StandardVariable standardVariable, final String value,
			final FieldbookService fieldbookService) {
		final MeasurementData data = new MeasurementData();
		data.setMeasurementVariable(this.createMeasurementVariable(standardVariable, fieldbookService));
		data.setValue(value);
		data.setLabel(data.getMeasurementVariable().getName());
		data.setDataType(data.getMeasurementVariable().getDataType());
		return data;
	}

	protected MeasurementData createMeasurementData(final MeasurementVariable measurementVariable, final String value) {
		final MeasurementData data = new MeasurementData();
		data.setMeasurementVariable(measurementVariable);
		data.setValue(value);
		data.setLabel(data.getMeasurementVariable().getName());
		data.setDataType(data.getMeasurementVariable().getDataType());
		return data;
	}

	protected MeasurementVariable createMeasurementVariable(final StandardVariable standardVariable, final FieldbookService fieldbookService) {
		final MeasurementVariable variable =
				ExpDesignUtil.convertStandardVariableToMeasurementVariable(standardVariable, Operation.ADD, fieldbookService);
		return variable;
	}

	public void addFactorsToMeasurementRows(final List<MeasurementRow> measurements) {

		if (this.workbook.getStudyDetails().getStudyType() == StudyType.N) {
			for (final MeasurementVariable factor : this.workbook.getFactors()) {
				for (final MeasurementRow row : measurements) {
					this.addFactorToDataListIfNecessary(factor, row.getDataList());
				}

			}
		}
	}

	protected void addFactorToDataListIfNecessary(final MeasurementVariable factor, final List<MeasurementData> dataList) {
		for (final MeasurementData data : dataList) {
			if (data.getMeasurementVariable().equals(factor)) {
				return;
			}
		}
		dataList.add(this.createMeasurementData(factor, ""));
	}

	public void addVariatesToMeasurementRows(final List<MeasurementRow> measurements, final UserSelection userSelection,
			final FieldbookService fieldbookService, final OntologyService ontologyService, final ContextUtil contextUtil) {
		try {
			final Set<MeasurementVariable> temporaryList = new HashSet<>();
			for (final MeasurementVariable mvar : this.workbook.getVariates()) {
				if (mvar.getOperation() == Operation.ADD || mvar.getOperation() == Operation.UPDATE) {
					final MeasurementVariable copy = mvar.copy();
					temporaryList.add(copy);
				}
			}

			WorkbookUtil.addMeasurementDataToRowsIfNecessary(new ArrayList<MeasurementVariable>(temporaryList), measurements, true,
					userSelection, ontologyService, fieldbookService, contextUtil.getCurrentProgramUUID());
		} catch (final MiddlewareException e) {
			DesignImportMeasurementRowGenerator.LOG.error(e.getMessage(), e);
		}
	}

	public Workbook getWorkbook() {
		return this.workbook;
	}

	public void setWorkbook(final Workbook workbook) {
		this.workbook = workbook;
	}

	public Map<PhenotypicType, List<DesignHeaderItem>> getMappedHeaders() {
		return this.mappedHeaders;
	}

	public void setMappedHeaders(final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders) {
		this.mappedHeaders = mappedHeaders;
	}

	public List<String> getRowValues() {
		return this.rowValues;
	}

	public void setRowValues(final List<String> rowValues) {
		this.rowValues = rowValues;
	}

	public List<ImportedGermplasm> getImportedGermplasm() {
		return this.importedGermplasm;
	}

	public void setImportedGermplasm(final List<ImportedGermplasm> importedGermplasm) {
		this.importedGermplasm = importedGermplasm;
	}

	public Map<Integer, StandardVariable> getGermplasmStandardVariables() {
		return this.germplasmStandardVariables;
	}

	public void setGermplasmStandardVariables(final Map<Integer, StandardVariable> germplasmStandardVariables) {
		this.germplasmStandardVariables = germplasmStandardVariables;
	}

	public Set<String> getTrialInstancesFromUI() {
		return this.trialInstancesFromUI;
	}

	public void setTrialInstancesFromUI(final Set<String> trialInstancesFromUI) {
		this.trialInstancesFromUI = trialInstancesFromUI;
	}

	public boolean isPreview() {
		return this.isPreview;
	}

	public void setPreview(final boolean isPreview) {
		this.isPreview = isPreview;
	}

	public Map<String, Integer> getAvailableCheckTypes() {
		return this.availableCheckTypes;
	}

	public void setAvailableCheckTypes(final Map<String, Integer> availableCheckTypes) {
		this.availableCheckTypes = availableCheckTypes;
	}

}
