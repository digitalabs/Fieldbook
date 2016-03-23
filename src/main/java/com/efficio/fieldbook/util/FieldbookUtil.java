
package com.efficio.fieldbook.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.controller.ExportStudyController;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.AppConstants;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class FieldbookUtil {

	private static FieldbookUtil instance;

	private static final Logger LOG = LoggerFactory.getLogger(FieldbookUtil.class);
	static {
		FieldbookUtil.instance = new FieldbookUtil();
	}

	private FieldbookUtil() {
		// empty constructor
	}

	public static FieldbookUtil getInstance() {
		return FieldbookUtil.instance;
	}

	public List<Integer> buildVariableIDList(String idList) {
		List<Integer> requiredVariables = new ArrayList<Integer>();
		StringTokenizer token = new StringTokenizer(idList, ",");
		while (token.hasMoreTokens()) {
			requiredVariables.add(Integer.valueOf(token.nextToken()));
		}
		return requiredVariables;
	}

	public static List<Integer> getColumnOrderList(String columnOrders) {
		if (columnOrders != null && !"".equalsIgnoreCase(columnOrders)) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				Integer[] columnsOrderList;
				columnsOrderList = mapper.readValue(columnOrders, Integer[].class);
				return Arrays.asList(columnsOrderList);
			} catch (JsonParseException e) {
				FieldbookUtil.LOG.error(e.getMessage(), e);
			} catch (JsonMappingException e) {
				FieldbookUtil.LOG.error(e.getMessage(), e);
			} catch (IOException e) {
				FieldbookUtil.LOG.error(e.getMessage(), e);
			}

		}
		return new ArrayList<Integer>();
	}

	public static void setColumnOrderingOnWorkbook(Workbook workbook, String columnOrderDelimited) {
		List<Integer> columnOrdersList = FieldbookUtil.getColumnOrderList(columnOrderDelimited);
		if (!columnOrdersList.isEmpty()) {
			workbook.setColumnOrderedLists(columnOrdersList);
		}
	}

	public static String generateEntryCode(int index) {
		return AppConstants.ENTRY_CODE_PREFIX.getString() + String.format("%04d", index);
	}

	public static boolean isPlotDuplicateNonFirstInstance(ImportedCrosses crosses) {
		if (crosses.isPlotDupe() && crosses.getDuplicateEntries() != null
				&& crosses.getEntryId() > crosses.getDuplicateEntries().iterator().next()) {
			return true;
		}
		return false;
	}

	public static void mergeCrossesPlotDuplicateData(ImportedCrosses crosses, List<ImportedCrosses> importedGermplasmList) {
		if (FieldbookUtil.isPlotDuplicateNonFirstInstance(crosses)) {
			// get the 1st instance of duplicate from the list
			Integer firstInstanceDuplicate = crosses.getDuplicateEntries().iterator().next();
			// needed to minus 1 since a list is 0 based
			ImportedCrosses firstInstanceCrossGermplasm = importedGermplasmList.get(firstInstanceDuplicate - 1);
			crosses.setGid(firstInstanceCrossGermplasm.getGid());
			crosses.setCross(firstInstanceCrossGermplasm.getCross());
			crosses.setDesig(firstInstanceCrossGermplasm.getDesig());
		}
	}

	public static boolean isContinueCrossingMerge(boolean hasPlotDuplicate, boolean isPreservePlotDuplicate, ImportedCrosses cross) {
		if (hasPlotDuplicate && !isPreservePlotDuplicate && FieldbookUtil.isPlotDuplicateNonFirstInstance(cross)) {
			return true;
		}
		return false;
	}

	public static void copyDupeNotesToListDataProject(List<ListDataProject> dataProjectList, List<ImportedCrosses> importedCrosses) {
		if (dataProjectList != null && importedCrosses != null && dataProjectList.size() == importedCrosses.size()) {
			for (int i = 0; i < dataProjectList.size(); i++) {
				dataProjectList.get(i).setDuplicate(importedCrosses.get(i).getDuplicate());
			}
		}
	}

	public static List<Integer> getFilterForMeansAndStatisticalVars() {

		List<Integer> isAIds = new ArrayList<Integer>();
		StringTokenizer token = new StringTokenizer(AppConstants.FILTER_MEAN_AND_STATISCAL_VARIABLES_IS_A_IDS.getString(), ",");
		while (token.hasMoreTokens()) {
			isAIds.add(Integer.valueOf(token.nextToken()));
		}
		return isAIds;
	}

	public static boolean isFieldmapColOrRange(MeasurementVariable var) {
		if (var.getTermId() == TermId.COLUMN_NO.getId() || var.getTermId() == TermId.RANGE_NO.getId()) {
			return true;
		}
		return false;
	}

	/**
	 * Sets the Content Disposition response header based on the user agent.
	 *
	 * @param filename
	 * @param httpHeaders
	 * @param userAgent
	 */
	public static void resolveContentDisposition(String filename, HttpHeaders httpHeaders, String userAgent) {

		String encodedFilename = FileUtils.encodeFilenameForDownload(filename);

		if (userAgent.indexOf("MSIE") != -1 || userAgent.indexOf("Trident") != -1) {
			// Internet Explorer has problems reading the Content-disposition header if it contains "filename*"
			httpHeaders.set("Content-disposition", "attachment; filename=\"" + encodedFilename + "\";");
		} else {
			// Those user agents that do not support the RFC 5987 encoding ignore "filename*" when it occurs after "filename".
			httpHeaders.set("Content-disposition", "attachment; filename=\"" + encodedFilename + "\"; filename*=\"UTF-8''"
					+ encodedFilename + "\";");
		}

	}

	/**
	 * Sets the Content Disposition response header based on the user agent.
	 *
	 * @param filename
	 * @param response
	 * @param userAgent
	 */
	public static void resolveContentDisposition(String filename, HttpServletResponse response, String userAgent) {

		String encodedFilename = FileUtils.encodeFilenameForDownload(filename);

		if (userAgent.indexOf("MSIE") != -1 || userAgent.indexOf("Trident") != -1) {
			// Internet Explorer has problems reading the Content-disposition header if it contains "filename*"
			response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFilename + "\";");
		} else {
			// Those user agents that do not support the RFC 5987 encoding ignore "filename*" when it occurs after "filename".
			response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFilename + "\"; filename*=\"UTF-8''"
					+ encodedFilename + "\";");
		}

	}

	public static void writeXlsToOutputStream(File xls, final HttpServletResponse response) {
		try {
			FileInputStream in = new FileInputStream(xls);
			final OutputStream out = response.getOutputStream();

			final byte[] buffer = new byte[ExportStudyController.BUFFER_SIZE];
			int length = 0;

			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		} catch(IOException e) {
			FieldbookUtil.LOG.error(e.getMessage(), e);
		}
	}

	public static void processEnvironmentData(final EnvironmentData data) {
		for (int i = 0; i < data.getEnvironments().size(); i++) {
			final Map<String, String> values = data.getEnvironments().get(i).getManagementDetailValues();
			if (!values.containsKey(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()))) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			} else if (values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())) == null
					|| values.get(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId())).isEmpty()) {
				values.put(Integer.toString(TermId.TRIAL_INSTANCE_FACTOR.getId()), Integer.toString(i + 1));
			}
		}
	}

	public static MeasurementVariable createMeasurementVariable(final String idToCreate, final String value, final Operation operation,
			final PhenotypicType role, final FieldbookService fieldbookMiddlewareService, final ContextUtil contextUtil) {
		final StandardVariable stdvar = fieldbookMiddlewareService.getStandardVariable(Integer.valueOf(idToCreate), contextUtil.getCurrentProgramUUID());
		stdvar.setPhenotypicType(role);
		final MeasurementVariable var =
				new MeasurementVariable(Integer.valueOf(idToCreate), stdvar.getName(), stdvar.getDescription(),
						stdvar.getScale().getName(), stdvar.getMethod().getName(), stdvar.getProperty().getName(), stdvar.getDataType()
						.getName(), value, stdvar.getPhenotypicType().getLabelList().get(0));
		var.setRole(role);
		var.setDataTypeId(stdvar.getDataType().getId());
		var.setFactor(false);
		var.setOperation(operation);
		return var;
	}

	public static Operation getDeletedVariableOperation(List<SettingDetail> settingsList, SettingVariable var, Operation operation) {
		final Iterator<SettingDetail> settingDetailIterator = settingsList.iterator();
		while (settingDetailIterator.hasNext()) {
			final SettingVariable deletedVariable = settingDetailIterator.next().getVariable();
			if (deletedVariable.getCvTermId().equals(var.getCvTermId())) {
				operation = deletedVariable.getOperation();
				settingDetailIterator.remove();
			}
		}

		return operation;
	}

	/**
	 * Convert enumerations and standard variable to json.
	 *
	 * @param enumerations the enumerations
	 * @param stdVariable the Standard Variable
	 * @return the string
	 */
	public static String convertEnumerationsAndStandardVariableToJSON(List<Enumeration> enumerations, StandardVariable stdVariable) {
		try {
			if (enumerations != null) {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(enumerations);
			} else if (stdVariable != null) {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(stdVariable);
			}
		} catch (Exception e) {
			FieldbookUtil.LOG.error(e.getMessage(), e);
		}
		return "";
	}
}
