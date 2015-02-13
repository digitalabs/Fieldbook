package com.efficio.fieldbook.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

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
			workbook.setColumnOrderedLists(columnOrdersList);				
   }		
}
