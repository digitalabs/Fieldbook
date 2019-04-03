package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.PRepDesignService;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class PRepDesignServiceImpl implements PRepDesignService {

	private static final Logger LOG = LoggerFactory.getLogger(ResolvableIncompleteBlockDesignServiceImpl.class);

	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	protected WorkbenchService workbenchService;

	@Resource
	protected FieldbookProperties fieldbookProperties;

	@Resource
	public FieldbookService fieldbookService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	public ExperimentDesignGenerator experimentDesignGenerator;

	@Override
	public List<MeasurementRow> generateDesign(
		final List<ImportedGermplasm> germplasmList, final ExpDesignParameterUi parameter, final List<MeasurementVariable> trialVariables,
		final List<MeasurementVariable> factors, final List<MeasurementVariable> nonTrialFactors, final List<MeasurementVariable> variates,
		final List<TreatmentVariable> treatmentVariables) throws BVDesignException {
		List<MeasurementRow> measurementRowList = new ArrayList<>();

		final int nTreatments = germplasmList.size();
		final String blockSize = parameter.getBlockSize();
		final int replicationPercentage = parameter.getReplicationPercentage();
		final int replicationNumber = Integer.parseInt(parameter.getReplicationsCount());
		final int environments = Integer.parseInt(parameter.getNoOfEnvironments());
		final int environmentsToAdd = Integer.parseInt(parameter.getNoOfEnvironmentsToAdd());

		try {

			final StandardVariable stdvarTreatment =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(), contextUtil.getCurrentProgramUUID());

			final Map<Integer, StandardVariable> requiredVariablesMap = this.getRequiredDesignVariablesMap();
			final StandardVariable stdvarBlock = requiredVariablesMap.get(TermId.BLOCK_NO.getId());
			final StandardVariable stdvarPlot = requiredVariablesMap.get(TermId.PLOT_NO.getId());

			final Integer plotNo = StringUtil.parseInt(parameter.getStartingPlotNo(), null);
			Integer entryNo = StringUtil.parseInt(parameter.getStartingEntryNo(), null);

			if (!Objects.equals(stdvarTreatment.getId(), TermId.ENTRY_NO.getId())) {
				entryNo = null;
			}

			final List<ListItem> replicationListItems =
				experimentDesignGenerator.createReplicationListItemForPRepDesign(germplasmList, replicationPercentage, replicationNumber);
			final MainDesign mainDesign = experimentDesignGenerator
				.createPRepDesign(blockSize, Integer.toString(nTreatments), replicationListItems, stdvarTreatment.getName(),
					stdvarBlock.getName(), stdvarPlot.getName(), plotNo, entryNo, "");

			measurementRowList = experimentDesignGenerator
				.generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
					variates, treatmentVariables, new ArrayList<StandardVariable>(requiredVariablesMap.values()), germplasmList, mainDesign,
					stdvarTreatment.getName(), null,
					new HashMap<Integer, Integer>());

		} catch (final BVDesignException e) {
			throw e;
		} catch (final Exception e) {
			PRepDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return measurementRowList;
	}

	@Override
	public List<StandardVariable> getRequiredDesignVariables() {
		final List<StandardVariable> varList = new ArrayList<>();
		try {
			final StandardVariable stdvarBlock =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), contextUtil.getCurrentProgramUUID());
			final StandardVariable stdvarPlot =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), contextUtil.getCurrentProgramUUID());

			stdvarBlock.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
			stdvarPlot.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);

			varList.add(stdvarBlock);
			varList.add(stdvarPlot);
		} catch (final MiddlewareException e) {
			PRepDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return varList;
	}

	@Override
	public ExpDesignValidationOutput validate(
		final ExpDesignParameterUi expDesignParameter, final List<ImportedGermplasm> germplasmList) {
		return null;
	}

	@Override
	public List<Integer> getExperimentalDesignVariables(final ExpDesignParameterUi params) {
		return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId());
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}

	private Map<Integer, StandardVariable> getRequiredDesignVariablesMap() {
		final Map<Integer, StandardVariable> map = new HashMap<>();
		final List<StandardVariable> requiredDesignVariables = this.getRequiredDesignVariables();
		for (final StandardVariable standardVariable : requiredDesignVariables) {
			map.put(standardVariable.getId(), standardVariable);
		}
		return map;
	}

}
