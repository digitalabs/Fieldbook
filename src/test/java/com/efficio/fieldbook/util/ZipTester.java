/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.web.util.ZipUtil;

/**
 * The Class CropOntologyServiceTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class ZipTester extends AbstractJUnit4SpringContextTests {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ZipTester.class);
    List<String> filenameList;
    String zipFilename = "test.zip";
  
    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
    	filenameList = new ArrayList();
    	filenameList.add("test1.txt");
    	filenameList.add("test2.txt");
    	try {
	    	for(String fName : filenameList){
	    		File f = new File(fName);
	    		
					f.createNewFile();
				
	    	}
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private void deleteFiles(){
    	for(String fName : filenameList){
    		File f = new File(fName);
    		f.delete();			
    	}
    	File zipFile = new File(zipFilename);
    	zipFile.deleteOnExit();
    }
	
   
	
	/**
	 * Test file zipping.
	 */
	@Test
	public void testFileZipping() {
		ZipUtil.zipIt(zipFilename, filenameList);
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(zipFilename);
		

		    Enumeration<? extends ZipEntry> entries = zipFile.entries();
		    int size = 0;
		    while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        System.out.println(entry.getName());
		        assertFalse(!filenameList.contains(entry.getName()));
		        size++;
		    }
		    assertEquals(filenameList.size(), size);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		deleteFiles();
	}
}
