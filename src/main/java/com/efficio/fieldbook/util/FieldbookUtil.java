package com.efficio.fieldbook.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

	public static final String UTF_8 = "UTF-8";
	public static final String ISO_8859_1 = "iso-8859-1";
	private static final Logger LOG = LoggerFactory.getLogger(FieldbookUtil.class);
	public static final String DESCRIPTION = "Description";
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
		return new ArrayList<>();
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

		List<Integer> isAIds = new ArrayList<>();
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
	 * Creates ResponseEntity to download a file from a controller.
	 *
	 * @param fileWithFullPath  - path of the file to be downloaded
	 * @param filename  - the filename that will be set in the http response header
	 * @return
	 */
	public static ResponseEntity<FileSystemResource> createResponseEntityForFileDownload(String fileWithFullPath, String filename) throws
		UnsupportedEncodingException {
		final HttpHeaders respHeaders = new HttpHeaders();

		final File resource = new File(fileWithFullPath);
		final FileSystemResource fileSystemResource = new FileSystemResource(resource);

		final String mimeType = FileUtils.detectMimeType(filename);
		final String sanitizedFilename = FileUtils.sanitizeFileName(filename);

		respHeaders.set(FileUtils.CONTENT_TYPE,String.format("%s;charset=utf-8",mimeType));
		respHeaders.set(FileUtils.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"; filename*=utf-8\'\'%s", sanitizedFilename, FileUtils.encodeFilenameForDownload(sanitizedFilename)));

		return new ResponseEntity<>(fileSystemResource, respHeaders, HttpStatus.OK);

	}

	public List<Integer> buildVariableIDList(String idList) {
		List<Integer> requiredVariables = new ArrayList<>();
		StringTokenizer token = new StringTokenizer(idList, ",");
		while (token.hasMoreTokens()) {
			final String s = token.nextToken();
			// FIXME BMS-4397
			if (!DESCRIPTION.equals(s)) {
				requiredVariables.add(Integer.valueOf(s));
			}
		}
		return requiredVariables;
	}
}
