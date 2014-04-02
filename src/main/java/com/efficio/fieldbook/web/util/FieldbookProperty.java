package com.efficio.fieldbook.web.util;

import java.io.IOException;
import java.util.Properties;

public class FieldbookProperty {

	private static Properties fieldbookProperties;
	
	private static final String FIELDBOOK_PROPERTIES_FILE = "fieldbook.properties";
	
	private static final String PROPERTY_PATH_NAME = "upload.directory";
	
	static {
        fieldbookProperties = new Properties();
        try {
        	fieldbookProperties.load(AppConstants.class.getClassLoader().getResourceAsStream(FIELDBOOK_PROPERTIES_FILE));

        } catch (IOException e) {
            e.printStackTrace();
            //do nothing
        }
	}

	public static String getPathProperty() {
		return fieldbookProperties.getProperty(PROPERTY_PATH_NAME);
	}
	
}
