/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import com.efficio.fieldbook.web.trial.bean.PossibleValuesCache;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The Class FieldbookServiceImpl.
 */
public class FieldbookServiceImpl implements FieldbookService {

	private static final Logger LOG = LoggerFactory.getLogger(FieldbookServiceImpl.class);

	/**
	 * The file service.
	 */
	@Resource
	private FileService fileService;

	@Autowired
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Autowired
	private OntologyService ontologyService;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private PossibleValuesCache possibleValuesCache;

	@Resource
	private NamingConventionService namingConventionService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private UserService userService;

	public FieldbookServiceImpl() {
	}

	public FieldbookServiceImpl(
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService,
		final PossibleValuesCache possibleValuesCache) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
		this.possibleValuesCache = possibleValuesCache;
	}

	static boolean inHideVariableFields(final Integer stdVarId, final String variableList) {
		final StringTokenizer token = new StringTokenizer(variableList, ",");
		boolean inList = false;
		while (token.hasMoreTokens()) {
			if (stdVarId.equals(Integer.parseInt(token.nextToken()))) {
				inList = true;
				break;
			}
		}
		return inList;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.efficio.fieldbook.service.api.FieldbookService#storeUserWorkbook(java
	 * .io.InputStream)
	 */
	@Override
	public String storeUserWorkbook(final InputStream in) throws IOException {
		return this.getFileService().saveTemporaryFile(in);
	}

	/**
	 * Gets the file service.
	 *
	 * @return the file service
	 */
	public FileService getFileService() {
		return this.fileService;
	}

	/**
	 * Advance Study
	 *
	 * @throws RuleException
	 * @throws FieldbookException
	 */
	@Override
	public AdvanceResult advanceStudy(final AdvancingStudy advanceInfo, final Workbook workbook)
		throws RuleException, FieldbookException {

		return this.namingConventionService.advanceStudy(advanceInfo, workbook);
	}

	@Override
	public List<StandardVariableReference> filterStandardVariablesForSetting(
		final int mode,
		final Collection<SettingDetail> selectedList) {

		List<StandardVariableReference> result = new ArrayList<>();

		final Set<Integer> selectedIds = new HashSet<>();
		if (selectedList != null && !selectedList.isEmpty()) {
			for (final SettingDetail settingDetail : selectedList) {
				selectedIds.add(settingDetail.getVariable().getCvTermId());
			}
		}

		final List<Integer> storedInIds = this.getStoredInIdsByMode(mode);
		final List<Integer> propertyIds = this.getPropertyIdsByMode(mode);

		final List<StandardVariableReference> dbList = this.fieldbookMiddlewareService.filterStandardVariablesByMode(
			storedInIds, propertyIds,
			mode == VariableType.TRAIT.getId() || mode == VariableType.STUDY_CONDITION.getId());

		if (dbList != null && !dbList.isEmpty()) {

			for (final StandardVariableReference ref : dbList) {
				if (!selectedIds.contains(ref.getId())) {

					if (mode == VariableType.STUDY_DETAIL.getId()) {
						if (FieldbookServiceImpl.inHideVariableFields(
							ref.getId(),
							AppConstants.FILTER_STUDY_FIELDS.getString())
							|| ref.getId() == TermId.DATASET_NAME.getId()
							|| ref.getId() == TermId.DATASET_TITLE.getId()
							|| FieldbookServiceImpl.inHideVariableFields(
							ref.getId(),
							AppConstants.HIDE_ID_VARIABLES.getString())) {
							continue;
						}

					} else if (mode == VariableType.SELECTION_METHOD.getId()) {
						if (FieldbookServiceImpl.inHideVariableFields(
							ref.getId(),
							AppConstants.HIDE_ID_VARIABLES.getString())) {
							continue;
						}
					} else if (mode == VariableType.ENVIRONMENT_DETAIL.getId()) {
						if (FieldbookServiceImpl.inHideVariableFields(
							ref.getId(),
							AppConstants.HIDE_STUDY_VARIABLES.getString())) {
							continue;
						}
					} else {
						if (FieldbookServiceImpl.inHideVariableFields(
							ref.getId(),
							AppConstants.HIDE_PLOT_FIELDS.getString())) {
							continue;
						}
					}

					result.add(ref);
				}
			}
		}
		result = this.fieldbookMiddlewareService.filterStandardVariablesByIsAIds(
			result,
			FieldbookUtil.getFilterForMeansAndStatisticalVars());
		Collections.sort(result);

		return result;
	}

	private List<Integer> getStoredInIdsByMode(final int mode) {
		final List<Integer> list = new ArrayList<>();
		if (mode == VariableType.STUDY_DETAIL.getId()) {
			list.addAll(PhenotypicType.STUDY.getTypeStorages());
		} else if (mode == VariableType.TRAIT.getId() || mode == VariableType.SELECTION_METHOD.getId()
			|| mode == VariableType.STUDY_CONDITION.getId()) {
			list.addAll(PhenotypicType.VARIATE.getTypeStorages());
		} else if (mode == VariableType.ENVIRONMENT_DETAIL.getId()) {
			list.addAll(PhenotypicType.TRIAL_ENVIRONMENT.getTypeStorages());
		} else if (mode == VariableType.TREATMENT_FACTOR.getId()) {
			list.addAll(PhenotypicType.TRIAL_DESIGN.getTypeStorages());
		} else if (mode == VariableType.GERMPLASM_DESCRIPTOR.getId()) {
			list.addAll(PhenotypicType.GERMPLASM.getTypeStorages());
		}
		return list;
	}

	private List<Integer> getPropertyIdsByMode(final int mode) {
		final List<Integer> list = new ArrayList<>();

		if (mode == VariableType.SELECTION_METHOD.getId() || mode == VariableType.TRAIT.getId()
			|| mode == VariableType.STUDY_CONDITION.getId()) {

			final StringTokenizer token = new StringTokenizer(
				AppConstants.SELECTION_VARIATES_PROPERTIES.getString(),
				",");

			while (token.hasMoreTokens()) {
				list.add(Integer.valueOf(token.nextToken()));
			}
		}
		return list;
	}

	@Override
	public List<ValueReference> getAllPossibleValues(final int id) {
		final Variable variable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
			id, true);

		assert !Objects.equals(variable, null);

		return this.getAllPossibleValues(variable);
	}

	@Override
	public List<ValueReference> getAllPossibleValues(final int id, final boolean isGetAllRecords) {
		final Variable variable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
			id, true);

		assert !Objects.equals(variable, null);

		List<ValueReference> possibleValues = this.getCachedValues(isGetAllRecords, variable);

		if (possibleValues.isEmpty()) {
			if (DataType.LOCATION.equals(variable.getScale().getDataType())) {
				// for location, we get all since it is for saving, so we would
				// be able to set the name properly
				possibleValues = this.getLocations(isGetAllRecords);
			} else {
				possibleValues = this.getAllPossibleValues(variable);
			}
		}
		return possibleValues;
	}

	@Override
	public List<ValueReference> getAllPossibleValues(final Variable variable) {
		return this.getAllPosibleValues(variable, true);
	}

	@Override
	public List<ValueReference> getAllPossibleValuesWithFilter(final int id, final boolean filtered) {
		final Variable variable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
			id, true);
		return this.getAllPosibleValues(variable, filtered);
	}

	private List<ValueReference> getAllPosibleValues(final Variable variable, final boolean filtered) {
		List<ValueReference> possibleValues = this.getCachedValues(!filtered, variable);

		if (possibleValues.isEmpty()) {
			DataType dataType = variable.getScale().getDataType();

			// hacks to override the dataType(s)
			if (TermId.BREEDING_METHOD_CODE.getId() == variable.getId()
				|| TermId.BREEDING_METHOD_VARIATE_CODE.getId() == variable.getId()) {
				dataType = DataType.BREEDING_METHOD;
			}

			switch (dataType) {
				case BREEDING_METHOD:
					possibleValues.add(new ValueReference(0, AppConstants.PLEASE_CHOOSE.getString(),
						AppConstants.PLEASE_CHOOSE.getString()));
					final List<ValueReference> allBreedingMethods = this.getAllBreedingMethods(
						filtered,
						this.contextUtil.getCurrentProgramUUID());
					possibleValues.addAll(allBreedingMethods);
					this.possibleValuesCache.addPossibleValuesByDataType(DataType.BREEDING_METHOD, allBreedingMethods);
					break;
				case LOCATION:
					possibleValues = this.getLocations(filtered);
					this.possibleValuesCache.addLocations(filtered, possibleValues);
					break;
				case PERSON:
					possibleValues = this.convertPersonsToValueReferences(
						this.userService.getPersonsByCrop(this.contextUtil.getProjectInContext().getCropType()));
					this.possibleValuesCache.addPossibleValuesByDataType(DataType.PERSON, possibleValues);
					break;
				case CATEGORICAL_VARIABLE:
					// note as noticed: NURERY_TYPE is a categorical, has special
					// handling in prev but we'll treat it as categorical type
					// from now on
					for (final TermSummary value : variable.getScale().getCategories()) {
						possibleValues.add(new ValueReference(value));
					}
					this.possibleValuesCache.addPossibleValues(variable.getId(), possibleValues);
					break;
				default:
					break;
			}
		}

		return possibleValues;
	}

	private List<ValueReference> getCachedValues(final boolean isGetAllRecords, final Variable variable) {
		List<ValueReference> possibleValues = new ArrayList<>();
		if (!variable.getScale().getDataType().isSystemDataType()) {
			if (DataType.LOCATION.equals(variable.getScale().getDataType())) {
				possibleValues = this.possibleValuesCache.getLocationsCache(!isGetAllRecords);
			} else {
				possibleValues = this.possibleValuesCache
					.getPossibleValuesByDataType(variable.getScale().getDataType());
			}
		} else if (DataType.CATEGORICAL_VARIABLE.equals(variable.getScale().getDataType())) {
			possibleValues = this.possibleValuesCache.getPossibleValues(variable.getId());
		}
		return possibleValues != null ? possibleValues : new ArrayList<ValueReference>();
	}

	private List<Location> getAllBreedingLocationsByUniqueID(final String programUUID) {
		final List<Location> breedingLocationsOfCurrentProgram = new ArrayList<>();

		try {
			final List<Location> breedingLocations = this.fieldbookMiddlewareService.getAllBreedingLocations();

			for (final Location location : breedingLocations) {
				if (location.getUniqueID() == null || location.getUniqueID().equals(programUUID)) {
					breedingLocationsOfCurrentProgram.add(location);
				}
			}

		} catch (final MiddlewareQueryException e) {
			FieldbookServiceImpl.LOG.error(e.getMessage(), e);
		}

		return breedingLocationsOfCurrentProgram;
	}

	@Override
	public List<ValueReference> getAllPossibleValuesFavorite(
		final int id, final String programUUID,
		final Boolean filtered) {
		final Variable variable = this.ontologyVariableDataManager.getVariable(programUUID, id, true);

		assert !Objects.equals(variable, null);

		List<ValueReference> possibleValuesFavorite = new ArrayList<>();
		DataType dataType = variable.getScale().getDataType();

		// hacks to override the dataType(s)
		if (TermId.BREEDING_METHOD_CODE.getId() == variable.getId()) {
			dataType = DataType.BREEDING_METHOD;
		}

		if (DataType.BREEDING_METHOD.equals(dataType)) {
			final List<Integer> methodIds = this.fieldbookMiddlewareService.getFavoriteProjectMethods(programUUID);
			final List<ValueReference> list = new ArrayList<>();
			list.add(new ValueReference(0, AppConstants.PLEASE_CHOOSE.getString(),
				AppConstants.PLEASE_CHOOSE.getString()));
			possibleValuesFavorite = list;

			if (methodIds != null && !methodIds.isEmpty()) {
				possibleValuesFavorite.addAll(this.getFavoriteBreedingMethods(methodIds, filtered));
			}

		} else if (DataType.LOCATION.equals(dataType)) {
			final List<Integer> locationIds = this.fieldbookMiddlewareService
				.getFavoriteProjectLocationIds(programUUID);

			if (locationIds != null && !locationIds.isEmpty()) {
				possibleValuesFavorite = this.convertLocationsToValueReferences(
					this.fieldbookMiddlewareService.getFavoriteLocationByLocationIDs(locationIds, filtered));

			}

		}
		return possibleValuesFavorite;
	}

	public List<ValueReference> getFavoriteBreedingMethods(
		final List<Integer> methodIDList,
		final Boolean isFilterOutGenerative) {
		final List<Method> methods = this.fieldbookMiddlewareService.getFavoriteMethods(
			methodIDList,
			isFilterOutGenerative);
		return this.convertMethodsToValueReferences(methods);
	}

	private List<ValueReference> convertMethodsToValueReferences(final List<Method> methods) {
		final List<ValueReference> list = new ArrayList<>();
		if (methods != null && !methods.isEmpty()) {
			for (final Method method : methods) {
				if (method != null) {
					final ValueReference valueReference = new ValueReference(method.getMid(), method.getMdesc(),
						method.getMname() + " - " + method.getMcode());
					valueReference.setKey(method.getMcode());
					list.add(valueReference);
				}
			}
		}
		return list;
	}

	@Override
	public List<ValueReference> getAllBreedingMethods(final boolean isFilterOutGenerative, final String programUUID) {
		final List<ValueReference> list = new ArrayList<>();
		final List<Method> methods = this.fieldbookMiddlewareService.getAllBreedingMethods(isFilterOutGenerative);
		if (methods != null && !methods.isEmpty()) {
			for (final Method method : methods) {
				if (method != null && (method.getUniqueID() == null || method.getUniqueID().equals(programUUID))) {
					final ValueReference valueReference = new ValueReference(method.getMid(), method.getMdesc(),
						method.getMname() + " - " + method.getMcode());
					valueReference.setKey(method.getMcode());
					list.add(valueReference);
				}
			}
		}
		return list;
	}

	List<ValueReference> getLocations(final boolean isBreedingMethodOnly) {
		final String currentProgramUUID = this.contextUtil.getCurrentProgramUUID();
		if (isBreedingMethodOnly) {
			return this.convertLocationsToValueReferences(this.getAllBreedingLocationsByUniqueID(currentProgramUUID));
		}

		final List<Location> locations = this.fieldbookMiddlewareService.getLocationsByProgramUUID(currentProgramUUID);
		return this.convertLocationsToValueReferences(locations);
	}

	private List<ValueReference> convertLocationsToValueReferences(final List<Location> locations) {
		final List<ValueReference> list = new ArrayList<>();
		if (locations != null && !locations.isEmpty()) {
			for (final Location loc : locations) {
				if (loc != null) {
					final String locNameDisplay = this.getDisplayName(loc);
					list.add(new ValueReference(loc.getLocid(), locNameDisplay, locNameDisplay));
				}
			}
		}
		return list;
	}

	String getDisplayName(final Location loc) {
		String locNameDisplay = loc.getLname();
		if (loc.getLabbr() != null && !"".equalsIgnoreCase(loc.getLabbr())) {
			locNameDisplay += " - (" + loc.getLabbr() + ")";
		}
		return locNameDisplay;
	}

	@Override
	public List<ValueReference> getAllPossibleValuesByPSMR(
		final String property, final String scale,
		final String method, final PhenotypicType phenotypeType) {
		List<ValueReference> list = new ArrayList<>();
		final Integer standardVariableId = this.fieldbookMiddlewareService
			.getStandardVariableIdByPropertyScaleMethodRole(property, scale, method, phenotypeType);
		if (standardVariableId != null) {
			list = this.getAllPossibleValues(standardVariableId);
		}
		return list;
	}

	private List<ValueReference> convertPersonsToValueReferences(final List<Person> persons) {
		final List<ValueReference> list = new ArrayList<>();
		if (persons != null && !persons.isEmpty()) {
			for (final Person person : persons) {
				if (person != null) {
					list.add(new ValueReference(person.getId(), person.getDisplayName(), person.getDisplayName()));
				}
			}
		}
		return list;
	}

	@Override
	public String getValue(final int id, final String valueOrId, final boolean isCategorical) {
		final Variable variable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
			id, true);
		assert !Objects.equals(variable, null);

		final List<ValueReference> possibleValues = this.possibleValuesCache.getPossibleValues(id);
		if (!NumberUtils.isNumber(valueOrId) && TermId.BREEDING_METHOD_CODE.getId() != id
			&& TermId.BREEDING_METHOD.getId() != id) {
			return valueOrId;
		}

		// TODO : for investigation, check for best fix, since ValueReference
		// object should NOT be compared to a String
		if (possibleValues != null && !possibleValues.isEmpty()) {
			for (final ValueReference possibleValue : possibleValues) {
				if (possibleValue.equals(valueOrId)) {
					return possibleValue.getName();
				}
			}
		}

		Double valueId = null;
		if (NumberUtils.isNumber(valueOrId)) {
			valueId = Double.valueOf(valueOrId);
		}

		if (TermId.BREEDING_METHOD_ID.getId() == id) {
			return this.getBreedingMethodById(valueId != null ? valueId.intValue() : 0);
		} else if (TermId.BREEDING_METHOD_CODE.getId() == id) {
			return this.getBreedingMethodByCode(valueOrId);
		} else if (TermId.BREEDING_METHOD.getId() == id) {
			return this.getBreedingMethodByName(valueOrId);
		} else if (DataType.LOCATION.equals(variable.getScale().getDataType())) {
			return this.getLocationById(valueId.intValue());
		} else if (DataType.PERSON.equals(variable.getScale().getDataType())) {
			return this.userService.getPersonNameForPersonId(valueId.intValue());
		} else if (isCategorical) {
			final Term term = this.ontologyService.getTermById(valueId.intValue());
			if (term != null) {
				return term.getName();
			}
		} else {
			return valueOrId;
		}
		return null;
	}

	private String getBreedingMethodById(final int id) {
		final Method method = this.fieldbookMiddlewareService.getBreedingMethodById(id);
		if (method != null) {
			return method.getMname() + " - " + method.getMcode();
		}
		return null;
	}

	protected String getBreedingMethodByCode(final String code) {
		final Method method = this.fieldbookMiddlewareService.getMethodByCode(
			code,
			this.contextUtil.getCurrentProgramUUID());
		if (method != null) {
			return method.getMname() + " - " + method.getMcode();
		}
		return "";
	}

	private String getBreedingMethodByName(final String name) {
		final Method method = this.fieldbookMiddlewareService.getMethodByName(name);
		if (method != null) {
			return method.getMname() + " - " + method.getMcode();
		}
		return "";
	}

	private String getLocationById(final int id) {
		final Location location = this.fieldbookMiddlewareService.getLocationById(id);
		if (location != null) {
			return location.getLname();
		}
		return null;
	}

	@Override
	public Term getTermById(final int termId) {
		return this.ontologyService.getTermById(termId);
	}

	@Override
	public void setAllPossibleValuesInWorkbook(final Workbook workbook) {
		final List<MeasurementVariable> allVariables = workbook.getAllVariables();
		for (final MeasurementVariable variable : allVariables) {
			if (variable.getPossibleValues() == null || variable.getPossibleValues().isEmpty()) {

				if (DataType.BREEDING_METHOD.getId().equals(variable.getDataTypeId())) {
					final List<ValueReference> list = new ArrayList<>();
					final List<Method> methodList = this.fieldbookMiddlewareService.getAllBreedingMethods(true);
					// since we only need the name for the display
					// special handling for breeding methods
					if (methodList != null && !methodList.isEmpty()) {
						for (final Method method : methodList) {
							if (method != null) {
								list.add(new ValueReference(
									method.getMid(),
									method.getMname() + " - " + method.getMcode(),
									method.getMname() + " - " + method.getMcode()));
							}
						}
					}
					variable.setPossibleValues(list);
				}
			}
		}
	}

	@Override
	public List<Enumeration> getCheckTypeList() {
		return this.ontologyService.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID())
			.getEnumerations();
	}

	@Override
	public Map<String, String> getIdNamePairForRetrieveAndSave() {
		final String idNamePairs = AppConstants.ID_NAME_COMBINATION.getString();
		final StringTokenizer tokenizer = new StringTokenizer(idNamePairs, ",");
		final Map<String, String> idNameMap = new HashMap<>();
		if (tokenizer.hasMoreTokens()) {
			// we iterate it
			while (tokenizer.hasMoreTokens()) {
				final String pair = tokenizer.nextToken();
				final StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
				final String idTermId = tokenizerPair.nextToken();
				final String nameTermId = tokenizerPair.nextToken();
				idNameMap.put(idTermId, nameTermId);
			}
		}
		return idNameMap;
	}

	@Override
	public MeasurementVariable createMeasurementVariable(
		final String idToCreate, final String value,
		final Operation operation, final PhenotypicType role) {
		final StandardVariable stdvar = this.fieldbookMiddlewareService.getStandardVariable(
			Integer.valueOf(idToCreate),
			this.contextUtil.getCurrentProgramUUID());
		stdvar.setPhenotypicType(role);
		final MeasurementVariable var = new MeasurementVariable(Integer.valueOf(idToCreate), stdvar.getName(),
			stdvar.getDescription(), stdvar.getScale().getName(), stdvar.getMethod().getName(),
			stdvar.getProperty().getName(), stdvar.getDataType().getName(), value,
			stdvar.getPhenotypicType().getLabelList().get(0));
		var.setRole(role);
		var.setDataTypeId(stdvar.getDataType().getId());
		var.setFactor(false);
		var.setOperation(operation);
		return var;

	}

	@Override
	public void createIdCodeNameVariablePairs(final Workbook workbook, final String idCodeNamePairs) {
		final Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		if (workbook != null && idCodeNamePairs != null && !"".equalsIgnoreCase(idCodeNamePairs)) {
			// we get a map so we can check easily instead of traversing it
			// again
			for (final MeasurementVariable var : workbook.getConditions()) {
				if (var != null) {
					studyConditionMap.put(Integer.toString(var.getTermId()), var);
				}
			}

			final StringTokenizer tokenizer = new StringTokenizer(idCodeNamePairs, ",");
			if (tokenizer.hasMoreTokens()) {
				// we iterate it
				while (tokenizer.hasMoreTokens()) {
					final String pair = tokenizer.nextToken();
					final StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
					final String idTermId = tokenizerPair.nextToken();
					final String codeTermId = tokenizerPair.nextToken();
					final String nameTermId = tokenizerPair.nextToken();

					if (studyConditionMap.get(codeTermId) == null) {
						// case when nursery comes from old fieldbook and has id
						// variable saved
						if (studyConditionMap.get(idTermId) != null) {
							final MeasurementVariable measurementVar = studyConditionMap.get(idTermId);
							final Method method = studyConditionMap.get(idTermId).getValue().isEmpty() ? null
								: this.fieldbookMiddlewareService.getMethodById(
								Double.valueOf(studyConditionMap.get(idTermId).getValue()).intValue());

							// add code if it is not yet in the list
							workbook.getConditions().add(this.createMeasurementVariable(codeTermId,
								method == null ? "" : method.getMcode(), Operation.ADD, measurementVar.getRole()));

							// add name if it is not yet in the list
							if (studyConditionMap.get(nameTermId) == null) {
								workbook.getConditions()
									.add(this.createMeasurementVariable(nameTermId,
										method == null ? "" : method.getMname(), Operation.ADD,
										measurementVar.getRole()));
							}

							// set the correct value of the name and id for
							// update operation
							for (final MeasurementVariable var : workbook.getConditions()) {
								if (var.getTermId() == Integer.parseInt(nameTermId)) {
									var.setValue(method == null ? "" : method.getMname());
								}
							}
						}
					} else {
						final Method method;
						if (studyConditionMap.get(idTermId) != null) {
							method = studyConditionMap.get(idTermId).getValue().isEmpty() ? null
								: this.fieldbookMiddlewareService.getMethodById(
								Double.valueOf(studyConditionMap.get(idTermId).getValue()).intValue());
						} else {
							method = studyConditionMap.get(codeTermId).getValue().isEmpty() ? null
								: this.fieldbookMiddlewareService.getMethodById(
								Integer.parseInt(studyConditionMap.get(codeTermId).getValue()));
						}

						// add name variable if it is not yet in the list
						if (studyConditionMap.get(nameTermId) == null
							&& studyConditionMap.get(codeTermId).getOperation().equals(Operation.ADD)) {
							final MeasurementVariable codeTermVar = studyConditionMap.get(codeTermId);
							workbook.getConditions().add(this.createMeasurementVariable(nameTermId,
								method == null ? "" : method.getMname(), Operation.ADD, codeTermVar.getRole()));
						}

						// set correct values of id, code and name before saving
						if (studyConditionMap.get(nameTermId) != null || studyConditionMap.get(codeTermId) != null
							|| studyConditionMap.get(idTermId) != null) {
							if (workbook.getConditions() != null) {
								for (final MeasurementVariable var : workbook.getConditions()) {
									if (var.getTermId() == Integer.parseInt(nameTermId)) {
										var.setValue(method == null ? "" : method.getMname());
									} else if (var.getTermId() == Integer.parseInt(codeTermId)) {
										var.setValue(method == null ? "" : method.getMcode());
									}
								}
							}
						}
					}
				}
			}

			SettingsUtil.resetBreedingMethodValueToCode(this.fieldbookMiddlewareService, workbook.getObservations(),
				false, this.ontologyService, this.contextUtil.getCurrentProgramUUID());
		}
	}

	@Override
	public void createIdNameVariablePairs(
		final Workbook workbook, final List<SettingDetail> settingDetails,
		final String idNamePairs, final boolean deleteNameWhenIdNotExist) {

		final Map<String, MeasurementVariable> studyConditionMap = new HashMap<>();
		final Map<String, List<MeasurementVariable>> studyConditionMapList = new HashMap<>();
		if (workbook != null && idNamePairs != null && !"".equalsIgnoreCase(idNamePairs)) {
			// we get a map so we can check easily instead of traversing it
			// again
			for (final MeasurementVariable var : workbook.getConditions()) {
				if (var != null) {
					studyConditionMap.put(Integer.toString(var.getTermId()), var);
					List<MeasurementVariable> varList = new ArrayList<>();
					if (studyConditionMapList.get(Integer.toString(var.getTermId())) != null) {
						varList = studyConditionMapList.get(Integer.toString(var.getTermId()));
					}
					varList.add(var);
					studyConditionMapList.put(Integer.toString(var.getTermId()), varList);
				}
			}

			final StringTokenizer tokenizer = new StringTokenizer(idNamePairs, ",");
			if (tokenizer.hasMoreTokens()) {
				// we iterate it
				while (tokenizer.hasMoreTokens()) {
					final String pair = tokenizer.nextToken();
					final StringTokenizer tokenizerPair = new StringTokenizer(pair, "|");
					final String idTermId = tokenizerPair.nextToken();
					final String nameTermId = tokenizerPair.nextToken();
					if (studyConditionMap.get(idTermId) != null && studyConditionMap.get(nameTermId) != null) {
						/*
						 * means both are existing we need to get the value from
						 * the id and save it in the name
						 */
						final MeasurementVariable tempVarId = studyConditionMap.get(idTermId);
						final MeasurementVariable tempVarName = studyConditionMap.get(nameTermId);
						tempVarId.setName(tempVarName.getName() + AppConstants.ID_SUFFIX.getString());
						tempVarName.setValue(this.resolveNameVarValue(tempVarId));
						tempVarName.setOperation(tempVarId.getOperation());
						if (tempVarId.getOperation() != null && Operation.DELETE == tempVarId.getOperation()
							&& studyConditionMapList.get(tempVarName.getTermId()) != null) {
							final List<MeasurementVariable> varList = studyConditionMapList
								.get(tempVarName.getTermId());
							for (final MeasurementVariable var : varList) {
								var.setOperation(Operation.DELETE);
							}
						}
					} else if (studyConditionMap.get(idTermId) != null && studyConditionMap.get(nameTermId) == null) {
						/*
						 * means only id is existing we need to create another
						 * variable of the name
						 */
						final MeasurementVariable tempVarId = studyConditionMap.get(idTermId);
						final String actualNameVal = this.resolveNameVarValue(tempVarId);

						final StandardVariable stdvar = this.fieldbookMiddlewareService.getStandardVariable(
							Integer.valueOf(nameTermId), this.contextUtil.getCurrentProgramUUID());
						stdvar.setPhenotypicType(tempVarId.getRole());
						final MeasurementVariable tempVarName = new MeasurementVariable(Integer.valueOf(nameTermId),
							tempVarId.getName(), stdvar.getDescription(), stdvar.getScale().getName(),
							stdvar.getMethod().getName(), stdvar.getProperty().getName(),
							stdvar.getDataType().getName(), actualNameVal,
							stdvar.getPhenotypicType().getLabelList().get(0));
						tempVarName.setRole(tempVarId.getRole());
						tempVarName.setDataTypeId(stdvar.getDataType().getId());
						tempVarName.setFactor(false);
						tempVarId.setName(tempVarId.getName() + AppConstants.ID_SUFFIX.getString());
						if (tempVarId.getOperation() != Operation.DELETE) {
							tempVarName.setOperation(tempVarId.getOperation());//set same operation as the id
							workbook.getConditions().add(tempVarName);
							workbook.resetTrialConditions();
							final SettingVariable svar = new SettingVariable(tempVarName.getName(),
								stdvar.getDescription(), stdvar.getProperty().getName(),
								stdvar.getScale().getName(), stdvar.getMethod().getName(), null,
								stdvar.getDataType().getName(), stdvar.getDataType().getId(),
								stdvar.getConstraints() != null && stdvar.getConstraints().getMinValue() != null
									? stdvar.getConstraints().getMinValue() : null,
								stdvar.getConstraints() != null && stdvar.getConstraints().getMaxValue() != null
									? stdvar.getConstraints().getMaxValue() : null);
							svar.setCvTermId(stdvar.getId());
							svar.setCropOntologyId(
								stdvar.getCropOntologyId() != null ? stdvar.getCropOntologyId() : "");
							svar.setTraitClass(stdvar.getIsA() != null ? stdvar.getIsA().getName() : "");
							svar.setOperation(Operation.UPDATE);
							final SettingDetail settingDetail = new SettingDetail(svar, null, actualNameVal, true);
							settingDetail.setRole(tempVarName.getRole());
							settingDetails.add(settingDetail);
						}

						// get value only gets the id, we need to get the value
						// here

					} else if (studyConditionMap.get(idTermId) == null && studyConditionMap.get(nameTermId) != null) {
						/*
						 * means only name is existing we need to create the
						 * variable of the id
						 */

						final MeasurementVariable tempVarName = studyConditionMap.get(nameTermId);
						String actualIdVal = "";
						if (tempVarName.getValue() != null && !"".equalsIgnoreCase(tempVarName.getValue())) {
							final List<ValueReference> possibleValues = this
								.getAllPossibleValues(Integer.valueOf(idTermId), true);

							for (final ValueReference ref : possibleValues) {

								if (ref.getId() != null && ref.getName().equalsIgnoreCase(tempVarName.getValue())) {
									actualIdVal = ref.getId().toString();
									break;
								}
							}
						}

						if (deleteNameWhenIdNotExist) {
							// we need to delete the name
							tempVarName.setOperation(Operation.DELETE);
							// to be sure, we check all record and mark it as
							// delete
							if (studyConditionMapList.get(tempVarName.getTermId()) != null) {
								final List<MeasurementVariable> varList = studyConditionMapList
									.get(tempVarName.getTermId());
								for (final MeasurementVariable var : varList) {
									var.setOperation(Operation.DELETE);
								}
							}
						} else {
							final StandardVariable stdvar = this.fieldbookMiddlewareService.getStandardVariable(
								Integer.valueOf(idTermId), this.contextUtil.getCurrentProgramUUID());
							final MeasurementVariable tempVarId = new MeasurementVariable(Integer.valueOf(idTermId),
								tempVarName.getName() + AppConstants.ID_SUFFIX.getString(), stdvar.getDescription(),
								stdvar.getScale().getName(), stdvar.getMethod().getName(),
								stdvar.getProperty().getName(), stdvar.getDataType().getName(), actualIdVal,
								stdvar.getPhenotypicType().getLabelList().get(0));
							tempVarId.setRole(tempVarName.getRole());
							tempVarId.setDataTypeId(stdvar.getDataType().getId());
							tempVarId.setFactor(false);
							tempVarId.setOperation(Operation.ADD);
							workbook.getConditions().add(tempVarId);
							workbook.resetTrialConditions();
						}
					}

				}
			}
		}
		if (workbook != null) {
			// to be only done when it is a trial
			this.addConditionsToTrialObservationsIfNecessary(workbook);
		} else {
			// no adding, just setting of data
			if (!workbook.getTrialObservations().isEmpty() && workbook.getTrialConditions() != null
				&& !workbook.getTrialConditions().isEmpty()) {
				final MeasurementVariable cooperatorNameVar = WorkbookUtil
					.getMeasurementVariable(workbook.getTrialConditions(), AppConstants.COOPERATOR_NAME.getInt());

				if (cooperatorNameVar != null) {
					// we set it to the trial observation level

					for (final MeasurementRow row : workbook.getTrialObservations()) {
						final MeasurementData data = row.getMeasurementData(cooperatorNameVar.getTermId());
						if (data != null) {
							data.setValue(cooperatorNameVar.getValue());
						}
					}

				}
			}
		}
	}

	String resolveNameVarValue(final MeasurementVariable tempVarId) {
		String actualNameVal = "";
		if (tempVarId.getValue() != null && !"".equalsIgnoreCase(tempVarId.getValue())) {

			final List<ValueReference> possibleValues = this.getAllPossibleValues(tempVarId.getTermId(), true);

			for (final ValueReference ref : possibleValues) {
				if (ref.getId() != null && ref.getId().toString().equalsIgnoreCase(tempVarId.getValue())) {
					actualNameVal = ref.getName();
					break;
				}
			}
		}
		return actualNameVal;
	}

	@Override
	public void addConditionsToTrialObservationsIfNecessary(final Workbook workbook) {
		if (!workbook.getTrialObservations().isEmpty() && workbook.getTrialConditions() != null
			&& !workbook.getTrialConditions().isEmpty()) {

			final Map<String, String> idNameMap = AppConstants.ID_NAME_COMBINATION.getMapOfValues();
			final Set<String> keys = idNameMap.keySet();
			final Map<String, String> nameIdMap = new HashMap<>();
			for (final String key : keys) {
				final String entry = idNameMap.get(key);
				nameIdMap.put(entry, key);
			}

			for (final MeasurementVariable variable : workbook.getTrialConditions()) {
				for (final MeasurementRow row : workbook.getTrialObservations()) {
					final MeasurementData data = row.getMeasurementData(variable.getTermId());
					if (data == null) {

						String actualNameVal = "";
						final Integer idTerm;
						String pairId = idNameMap.get(String.valueOf(variable.getTermId()));
						if (pairId == null) {
							pairId = nameIdMap.get(String.valueOf(variable.getTermId()));

							if (pairId != null) {

								idTerm = Integer.valueOf(pairId);

								final MeasurementData pairData = row.getMeasurementData(Integer.valueOf(pairId));

								final MeasurementData idData = row.getMeasurementData(idTerm);

								if (idData != null) {
									final List<ValueReference> possibleValues = this
										.getVariablePossibleValues(idData.getMeasurementVariable());
									for (final ValueReference ref : possibleValues) {
										if (ref.getId() != null
											&& ref.getId().toString().equalsIgnoreCase(pairData.getValue())) {
											actualNameVal = ref.getName();
											break;
										}
									}
								}
							}

						}

						final MeasurementData newData = new MeasurementData(variable.getName(), actualNameVal);
						newData.setMeasurementVariable(variable);
						row.getDataList().add(row.getDataList().size() - 1, newData);

					} else if (nameIdMap.get(String.valueOf(variable.getTermId())) != null) {
						final Integer idTerm = Integer.valueOf(nameIdMap.get(String.valueOf(variable.getTermId())));
						final MeasurementData idData = row.getMeasurementData(idTerm);
						if (idData != null) {
							final List<ValueReference> possibleValues = this
								.getVariablePossibleValues(idData.getMeasurementVariable());
							if (possibleValues != null) {
								for (final ValueReference ref : possibleValues) {
									if (ref.getId() != null && ref.getId().toString().equals(idData.getValue())) {
										data.setValue(ref.getName());
										break;
									}
								}
							}
						}
					}
				}

			}
		}
	}

	@Override
	public List<ValueReference> getVariablePossibleValues(final MeasurementVariable var) {
		List<ValueReference> possibleValues = new ArrayList<>();
		// we need to get all possible values so we can check the favorites as
		// well, since if we depend on the variable possible values, its
		// already filtered, so it can be wrong
		if (DataType.LOCATION.getId().equals(var.getDataTypeId())) {
			possibleValues = this.getLocations(false);
		} else {
			possibleValues = var.getPossibleValues();
		}
		return possibleValues;
	}

	@Override
	public void manageCheckVariables(final UserSelection userSelection, final ImportGermplasmListForm form) {
		if (userSelection.getImportedCheckGermplasmMainInfo() != null && form.getImportedCheckGermplasm() != null) {
			if (!form.getImportedCheckGermplasm().isEmpty()
				&& !this.hasCheckVariables(userSelection.getWorkbook().getConditions())) {
				// add check variables
				this.addCheckVariables(userSelection.getWorkbook().getConditions(), form);
			} else if (!form.getImportedCheckGermplasm().isEmpty()
				&& this.hasCheckVariables(userSelection.getWorkbook().getConditions())) {
				// update values of check variables
				this.updateCheckVariables(userSelection.getWorkbook().getConditions(), form);
				this.updateChecksInTrialObservations(userSelection.getWorkbook().getTrialObservations(), form);
			} else if (form.getImportedCheckGermplasm().isEmpty()
				&& this.hasCheckVariables(userSelection.getWorkbook().getConditions())) {
				// delete check variables
				this.deleteCheckVariables(userSelection.getWorkbook().getConditions());
			}
		}
	}

	private void updateChecksInTrialObservations(
		final List<MeasurementRow> trialObservations,
		final ImportGermplasmListForm form) {
		if (trialObservations != null) {
			for (final MeasurementRow row : trialObservations) {
				this.setMeasurementDataInList(row, form);
			}
		}
	}

	private void setMeasurementDataInList(final MeasurementRow row, final ImportGermplasmListForm form) {
		if (row.getDataList() != null) {
			for (final MeasurementData data : row.getDataList()) {
				if (AppConstants.CHECK_VARIABLES.getString()
					.contains(String.valueOf(data.getMeasurementVariable().getTermId()))) {
					this.setMeasurementData(data, form, data.getMeasurementVariable().getTermId());
				}
			}
		}
	}

	private void setMeasurementData(final MeasurementData data, final ImportGermplasmListForm form, final int id) {
		data.setValue(SettingsUtil.getSettingDetailValue(form.getCheckVariables(), id));
		if (data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())) {
			data.setcValueId(data.getValue());
		}
	}

	private void addCheckVariables(final List<MeasurementVariable> conditions, final ImportGermplasmListForm form) {
		conditions.add(this.createMeasurementVariable(String.valueOf(TermId.CHECK_START.getId()),
			SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_START.getId()), Operation.ADD,
			VariableType.ENVIRONMENT_DETAIL.getRole()));
		conditions.add(this.createMeasurementVariable(String.valueOf(TermId.CHECK_INTERVAL.getId()),
			SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_INTERVAL.getId()),
			Operation.ADD, VariableType.ENVIRONMENT_DETAIL.getRole()));
		conditions.add(this.createMeasurementVariable(String.valueOf(TermId.CHECK_PLAN.getId()),
			SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_PLAN.getId()), Operation.ADD,
			VariableType.ENVIRONMENT_DETAIL.getRole()));
	}

	private void updateCheckVariables(final List<MeasurementVariable> conditions, final ImportGermplasmListForm form) {
		if (conditions != null && !conditions.isEmpty()) {
			for (final MeasurementVariable var : conditions) {
				if (var.getTermId() == TermId.CHECK_START.getId()) {
					var.setValue(
						SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_START.getId()));
				} else if (var.getTermId() == TermId.CHECK_INTERVAL.getId()) {
					var.setValue(SettingsUtil.getSettingDetailValue(
						form.getCheckVariables(),
						TermId.CHECK_INTERVAL.getId()));
				} else if (var.getTermId() == TermId.CHECK_PLAN.getId()) {
					var.setValue(
						SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_PLAN.getId()));
				}
			}
		}
	}

	private void deleteCheckVariables(final List<MeasurementVariable> conditions) {
		final String checkVariables = AppConstants.CHECK_VARIABLES.getString();
		if (checkVariables != null && !checkVariables.isEmpty()) {
			final StringTokenizer tokenizer = new StringTokenizer(checkVariables, ",");
			if (tokenizer.hasMoreTokens()) {
				while (tokenizer.hasMoreTokens()) {
					this.setCheckVariableToDelete(tokenizer.nextToken(), conditions);
				}
			}
		}
	}

	private void setCheckVariableToDelete(final String id, final List<MeasurementVariable> conditions) {
		if (conditions != null && !conditions.isEmpty()) {
			for (final MeasurementVariable var : conditions) {
				if (var.getTermId() == Integer.parseInt(id)) {
					var.setOperation(Operation.DELETE);
				}
			}
		}
	}

	protected boolean hasCheckVariables(final List<MeasurementVariable> conditions) {
		if (conditions != null && !conditions.isEmpty()) {
			for (final MeasurementVariable var : conditions) {
				if (this.isCheckVariable(var.getTermId())) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isCheckVariable(final int termId) {
		final StringTokenizer tokenizer = new StringTokenizer(AppConstants.CHECK_VARIABLES.getString(), ",");
		if (tokenizer.hasMoreTokens()) {
			while (tokenizer.hasMoreTokens()) {
				if (Integer.parseInt(tokenizer.nextToken()) == termId) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void saveStudyImportedCrosses(final List<Integer> crossesIds, final Integer studyId) {
		if (crossesIds != null && !crossesIds.isEmpty()) {
			for (final Integer crossesId : crossesIds) {
				this.fieldbookMiddlewareService.updateGermlasmListInfoStudy(crossesId, studyId != null ? studyId : 0);
			}
		}

	}

	@Override
	public void saveStudyColumnOrdering(
		final Integer studyId, final String columnOrderDelimited, final Workbook workbook) {
		final List<Integer> columnOrdersList = FieldbookUtil.getColumnOrderList(columnOrderDelimited);
		if (studyId != null && !columnOrdersList.isEmpty()) {
			this.fieldbookMiddlewareService.saveStudyColumnOrdering(studyId, columnOrdersList);
			workbook.setColumnOrderedLists(columnOrdersList);
		} else {
			if (studyId != null && workbook.getStudyDetails() != null) {
				workbook.getStudyDetails().setId(studyId);
			}
			this.fieldbookMiddlewareService.setOrderVariableByRank(workbook);
		}

	}

	@Override
	public void addMeasurementVariableToList(
		final MeasurementVariable measurementVariable,
		final List<MeasurementVariable> measurementVariables) {

		if (!this.isVariableExistsInList(measurementVariable.getTermId(), measurementVariables)) {
			measurementVariables.add(measurementVariable);

		}

	}

	@Override
	public void addStudyUUIDConditionAndObsUnitIDFactorToWorkbook(
		final Workbook workbook,
		final boolean addObsUnitIdToMeasurementRows) {
		final MeasurementVariable obsUnitIdMeasurementVariable = this.createMeasurementVariable(
			String.valueOf(TermId.OBS_UNIT_ID.getId()), "", Operation.ADD, PhenotypicType.GERMPLASM);
		obsUnitIdMeasurementVariable.setFactor(true);

		// OBS_UNIT_ID is not required in processing the Fieldbook data file,
		// but we need to add it in the background
		// if it is not available as it is necessary in displaying the
		// OBS_UNIT_ID column in measurements table.
		this.addMeasurementVariableToList(obsUnitIdMeasurementVariable, workbook.getFactors());

		// Skip addition of Observation Unit ID to measurement rows for Import Excel using
		// Data Import Wizard option. It will be added in the later steps.
		if (addObsUnitIdToMeasurementRows) {
			// It is important to add the OBS_UNIT_ID measurement data in
			// measurement rows to make sure that variables
			// in Workbook match the variables in measurement rows. This will
			// initially creates blank values for OBS_UNIT_ID
			// but the generation of Observation Unit IDs will be handled during the saving
			// of Workbook.
			this.addMeasurementVariableToMeasurementRows(obsUnitIdMeasurementVariable, workbook.getObservations());
		}
	}

	@Override
	public void addMeasurementVariableToMeasurementRows(
		final MeasurementVariable measurementVariable,
		final List<MeasurementRow> observations) {

		for (final MeasurementRow measurementRow : observations) {
			final MeasurementData measurementData = new MeasurementData();
			measurementData.setLabel(measurementVariable.getName());
			measurementData.setValue("");
			measurementData.setMeasurementVariable(measurementVariable);
			measurementRow.getDataList().add(measurementData);
		}

	}

	boolean isVariableExistsInList(final int termId, final List<MeasurementVariable> measurementVariables) {

		for (final MeasurementVariable measurementVariable : measurementVariables) {

			if (measurementVariable.getTermId() == termId) {
				return true;
			}

		}
		return false;
	}

	protected void setFieldbookMiddlewareService(
		final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	void setPossibleValuesCache(final PossibleValuesCache possibleValuesCache) {
		this.possibleValuesCache = possibleValuesCache;
	}

	void setOntologyVariableDataManager(final OntologyVariableDataManager ontologyVariableDataManager) {
		this.ontologyVariableDataManager = ontologyVariableDataManager;
	}

	public OntologyService getOntologyService() {
		return this.ontologyService;
	}

	public void setOntologyService(final OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	protected void setUserService(final UserService userService) {
		this.userService = userService;
	}

	@Override
	public StandardVariable getStandardVariable(final Integer termId) {
		return this.getOntologyService().getStandardVariable(termId, this.contextUtil.getCurrentProgramUUID());
	}

	@Override
	public long getGermplasmListChecksSize(final int germplasmListId) {
		final List<ValueReference> entryTypes = this.getAllPossibleValues(TermId.ENTRY_TYPE.getId(), true);
		final List<Integer> checkEntryTypeIds = new ArrayList<>();
		for (final ValueReference entryType : entryTypes) {
			if (SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() != entryType.getId()) {
				checkEntryTypeIds.add(entryType.getId());
			}
		}
		return this.fieldbookMiddlewareService.countListDataProjectByListIdAndEntryTypeIds(germplasmListId, checkEntryTypeIds);
	}
}
