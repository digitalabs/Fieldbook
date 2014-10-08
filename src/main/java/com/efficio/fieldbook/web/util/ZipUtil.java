package com.efficio.fieldbook.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(ZipUtil.class);
    /**
     * Zip it
     * @param zipFile output ZIP file location
     */
    public static void zipIt(String zipFile, List<String> filenameList){
 
     byte[] buffer = new byte[1024];
 
     try{
 
    	FileOutputStream fos = new FileOutputStream(zipFile);
    	ZipOutputStream zos = new ZipOutputStream(fos);
 
    	LOG.debug("Output to Zip : " + zipFile);
 
    	for(String file : filenameList){
 
    		File f = new File(file);
    		ZipEntry ze= new ZipEntry(f.getName());
        	zos.putNextEntry(ze);
 
        	FileInputStream in =  new FileInputStream(file);
 
        	int len;
        	while ((len = in.read(buffer)) > 0) {
        		zos.write(buffer, 0, len);
        	}
 
        	in.close();
    	}
 
    	zos.closeEntry();
    	//remember close it
    	zos.close();
 
    	LOG.debug("Done");
    }catch(IOException ex){
       ex.printStackTrace();   
    }
   }
}
