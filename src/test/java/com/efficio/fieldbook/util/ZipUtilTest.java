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

package com.efficio.fieldbook.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.generationcp.middleware.util.Debug;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.web.util.ZipUtil;

public class ZipUtilTest {

	private static final Logger LOG = LoggerFactory.getLogger(ZipUtilTest.class);
	List<String> filenameList;
	String zipFilename = "test.zip";

	@Before
	public void setUp() {
		this.filenameList = new ArrayList<String>();
		this.filenameList.add("test1.txt");
		this.filenameList.add("test2.txt");
		try {
			for (String fName : this.filenameList) {
				File f = new File(fName);

				f.createNewFile();

			}
		} catch (IOException e) {
			ZipUtilTest.LOG.error(e.getMessage(), e);
		}
	}

	private void deleteFiles() {
		for (String fName : this.filenameList) {
			File f = new File(fName);
			f.delete();
		}
		File zipFile = new File(this.zipFilename);
		zipFile.deleteOnExit();
	}

	/**
	 * Test file zipping.
	 */
	@Test
	public void testFileZipping() {
		ZipUtil.zipIt(this.zipFilename, this.filenameList);
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(this.zipFilename);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			int size = 0;
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				Debug.println(0, entry.getName());
				Assert.assertFalse(!this.filenameList.contains(entry.getName()));
				size++;
			}
			Assert.assertEquals(this.filenameList.size(), size);
		} catch (IOException e) {
			ZipUtilTest.LOG.error(e.getMessage(), e);
		}

		this.deleteFiles();
	}
}
