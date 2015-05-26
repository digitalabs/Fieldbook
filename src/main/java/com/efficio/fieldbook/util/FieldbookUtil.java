package com.efficio.fieldbook.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.pojos.ListDataProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.web.util.AppConstants;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public class FieldbookUtil {

	private static FieldbookUtil instance;
	
	private static final Logger LOG = LoggerFactory.getLogger(FieldbookUtil.class);
	private static final char[] HEX_CHARS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
        'C', 'D', 'E', 'F' };
	
	static {
		instance = new FieldbookUtil();
	}

	private FieldbookUtil() {
		// empty constructor
	}

	public static FieldbookUtil getInstance() {
		return instance;
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
		if(columnOrders != null && !"".equalsIgnoreCase(columnOrders)){			 
			try {
				ObjectMapper mapper = new ObjectMapper();
			 	Integer[] columnsOrderList;
				columnsOrderList = mapper.readValue(columnOrders, Integer[].class);
				return Arrays.asList(columnsOrderList);
			} catch (JsonParseException e) {
				LOG.error(e.getMessage(), e);
			} catch (JsonMappingException e) {
				LOG.error(e.getMessage(), e);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		 	 
		}
		return new ArrayList<Integer>();
	}
	public static void setColumnOrderingOnWorkbook(Workbook workbook, String columnOrderDelimited){
	    	List<Integer> columnOrdersList = FieldbookUtil.getColumnOrderList(columnOrderDelimited);
	    	if(!columnOrdersList.isEmpty()){
	    		workbook.setColumnOrderedLists(columnOrdersList);
	    	}
   }
	
    /**
    return encoded file name
     */
  public static String getDownloadFileName(String filename, HttpServletRequest request) {
	  String newFilename = filename;
      try{
          if (request.getHeader("User-Agent").indexOf("MSIE") != -1 || request.getHeader("User-Agent").indexOf("Trident") != -1) {
            URI uri = new URI(null, null, filename, null);
      	  	newFilename = uri.toASCIIString();
            return newFilename;
          }
          byte[] bytes = filename.getBytes("UTF-8");
          StringBuilder buff = new StringBuilder(bytes.length << 2);
          buff.append("=?UTF-8?Q?");
          for (byte b : bytes) {
              int unsignedByte = b & 0xFF;
              buff.append('=').append(HEX_CHARS[unsignedByte >> 4]).append(HEX_CHARS[unsignedByte & 0xF]);
          }
          return buff.append("?=").toString();
          
          
          
      }catch (URISyntaxException e) {
		LOG.error(e.getMessage(), e);
	} catch (UnsupportedEncodingException e) {
		LOG.error(e.getMessage(), e);
	}
      return newFilename;
  }

  public static String generateEntryCode(int index) {
      return AppConstants.ENTRY_CODE_PREFIX.getString() + String.format("%04d", index);
  }
  
  public static boolean isPlotDuplicateNonFirstInstance(ImportedCrosses crosses){
	  if(crosses.isPlotDupe() && crosses.getDuplicateEntries() != null &&  crosses.getEntryId() > crosses.getDuplicateEntries().iterator().next()){
		  return true;
	  }
	  return false;
  }
  public static void mergeCrossesPlotDuplicateData(ImportedCrosses crosses, List<ImportedCrosses> importedGermplasmList){
	  if(isPlotDuplicateNonFirstInstance(crosses)){
		  //get the 1st instance of duplicate from the list
		  Integer firstInstanceDuplicate = crosses.getDuplicateEntries().iterator().next();
		  // needed to minus 1 since a list is 0 based
		  ImportedCrosses firstInstanceCrossGermplasm = importedGermplasmList.get(firstInstanceDuplicate-1);
		  crosses.setGid(firstInstanceCrossGermplasm.getGid());
		  crosses.setCross(firstInstanceCrossGermplasm.getCross());
		  crosses.setDesig(firstInstanceCrossGermplasm.getDesig());		  
	  }
  }
  public static boolean isContinueCrossingMerge(boolean hasPlotDuplicate, boolean isPreservePlotDuplicate, ImportedCrosses cross){
	  if(hasPlotDuplicate && !isPreservePlotDuplicate && FieldbookUtil.isPlotDuplicateNonFirstInstance(cross)){
		  return true;
	  }
	  return false;
  }
  
  public static void copyDupeNotesToListDataProject(List<ListDataProject> dataProjectList, List<ImportedCrosses> importedCrosses){
	  if(dataProjectList != null && importedCrosses != null && dataProjectList.size() == importedCrosses.size()){
		  for(int i = 0 ; i < dataProjectList.size() ; i++){
			  dataProjectList.get(i).setDuplicate(importedCrosses.get(i).getDuplicate()); 
		  }
	  }
  }
  
  public static List<Integer> getFilterForMeansAndStatisticalVars(){
	  
	  List<Integer> isAIds = new ArrayList<Integer>();
	  StringTokenizer token = new StringTokenizer(AppConstants.FILTER_MEAN_AND_STATISCAL_VARIABLES_IS_A_IDS.getString(), ",");
	  while (token.hasMoreTokens()) {
		  isAIds.add(Integer.valueOf(token.nextToken()));
	  }
	  return isAIds;
  }
}
