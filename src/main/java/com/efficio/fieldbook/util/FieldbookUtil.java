package com.efficio.fieldbook.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.ListDataProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.efficio.fieldbook.web.util.AppConstants;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class FieldbookUtil {

	private static final Logger LOG = LoggerFactory.getLogger(FieldbookUtil.class);
	private static FieldbookUtil instance;

	static {
		FieldbookUtil.instance = new FieldbookUtil();
	}

	private FieldbookUtil() {
		// empty constructor
	}

	public static FieldbookUtil getInstance() {
		return FieldbookUtil.instance;
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
		if (crosses.isPlotDupe() && crosses.getDuplicateEntries() != null && crosses.getEntryId() > crosses.getDuplicateEntries().iterator()
				.next()) {
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
		} else if (userAgent.indexOf("Safari") != -1) {
			// Safari does not support url-encoded filename, so instead we use the Safari standard of writing the file name directly in
			// utf-8 encoded header
			httpHeaders.set("Content-disposition", "attachment; filename=\"" + FileUtils.sanitizeFileName(filename) + "\";");
		} else {
			// Those user agents that do not support the RFC 5987 encoding ignore "filename*" when it occurs after "filename".
			httpHeaders.set("Content-disposition",
					"attachment; filename=\"" + encodedFilename + "\"; filename*=\"UTF-8''" + encodedFilename + "\";");
		}

	}

	/**
	 * Creates ResponseEntity to download a file from a controller.
	 *
	 * @param pathname  - path of the file to be downloaded
	 * @param filename  - the filename that will be set in the http response header
	 * @param userAgent - the user agent string
	 * @return
	 */
	public static ResponseEntity<FileSystemResource> createResponseEntityForFileDownload(String pathname, String filename,
			String userAgent) {

		final File resource = new File(pathname);
		final FileSystemResource fileSystemResource = new FileSystemResource(resource);

		final HttpHeaders respHeaders = new HttpHeaders();

		FieldbookUtil.resolveContentDisposition(filename, respHeaders, userAgent);
		respHeaders.set("Content-Type", FileUtils.detectMimeType(filename) + "; charset=UTF-8");

		return new ResponseEntity<>(fileSystemResource, respHeaders, HttpStatus.OK);

	}

	/**
	 * Creates ResponseEntity to download a file from a controller.
	 *
	 * @param file      - The file to be downloaded
	 * @param userAgent - the user agent string
	 * @return
	 */
	public static ResponseEntity<FileSystemResource> createResponseEntityForFileDownload(File file, String userAgent) {
		return FieldbookUtil.createResponseEntityForFileDownload(file.getAbsoluteFile().toString(), file.getName(), userAgent);
	}

	public List<Integer> buildVariableIDList(String idList) {
		List<Integer> requiredVariables = new ArrayList<Integer>();
		StringTokenizer token = new StringTokenizer(idList, ",");
		while (token.hasMoreTokens()) {
			requiredVariables.add(Integer.valueOf(token.nextToken()));
		}
		return requiredVariables;
	}
}
