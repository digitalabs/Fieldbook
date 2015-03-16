package com.efficio.fieldbook.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      try{
          if (request.getHeader("User-Agent").indexOf("MSIE") != -1) {
              return '\"' + java.net.URLEncoder.encode(filename, "UTF-8") + '\"';
          }
          byte[] bytes = filename.getBytes("UTF-8");
          StringBuilder buff = new StringBuilder(bytes.length << 2);
          buff.append("=?UTF-8?Q?");
          for (byte b : bytes) {
              int unsignedByte = b & 0xFF;
              buff.append('=').append(HEX_CHARS[unsignedByte >> 4]).append(HEX_CHARS[unsignedByte & 0xF]);
          }
          return buff.append("?=").toString();
      }catch(UnsupportedEncodingException e){
          return filename;
      }
  }

  
}
