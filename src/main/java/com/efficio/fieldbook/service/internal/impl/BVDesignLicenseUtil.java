package com.efficio.fieldbook.service.internal.impl;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
import com.efficio.fieldbook.service.internal.DesignLicenseUtil;
import com.efficio.fieldbook.web.util.AppConstants;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Configurable
public class BVDesignLicenseUtil implements DesignLicenseUtil {

	public static final String LICENSE_DATE_FORMAT = "dd-MMM-yyyy";
	private static final Logger LOG = LoggerFactory.getLogger(BVDesignLicenseUtil.class);

	private static final String BREEDING_VIEW_EXE = "BreedingView.exe";
	private static final String BVDESIGN_EXE = "BVDesign.exe";
	public static String BVDESIGN_STATUS_OUTPUT_FILENAME = "son";

	@Resource
	private WorkbenchService workbenchService;

	@Override
	public boolean isExpired(final BVDesignLicenseInfo bvDesignLicenseInfo) {

		try {

			Format formatter = new SimpleDateFormat(LICENSE_DATE_FORMAT);
			Date expiryDate = (Date) formatter.parseObject(bvDesignLicenseInfo.getStatus().getLicense().getExpiry());
			final Date currentDate = DateUtil.getCurrentDateWithZeroTime();
			if (expiryDate.compareTo(currentDate) > 0) {
				return true;
			}
		} catch (ParseException e) {
			BVDesignLicenseUtil.LOG.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean isExpiringWithinThirtyDays(final BVDesignLicenseInfo bvDesignLicenseInfo) {

		int expiryDays = 0;

		try {
			expiryDays = Integer.parseInt(bvDesignLicenseInfo.getStatus().getLicense().getExpiryDays());
		} catch (NumberFormatException e) {
			BVDesignLicenseUtil.LOG.error("Expiration days from BVDesign's license file is not a number", e);
			return true;
		}

		return expiryDays <= 30;

	}

	@Override
	public BVDesignLicenseInfo retrieveLicenseInfo() throws IOException {

		final String bvDesignLocation = this.getBreedingViewExeLocation();

		this.generateBVDesignLicenseJsonFile(bvDesignLocation);

		return this.readLicenseInfoFromJsonFile(bvDesignLocation);
	}

	protected BVDesignLicenseInfo readLicenseInfoFromJsonFile(String bvDesignLocation) throws IOException {

		final ObjectMapper mapper = new ObjectMapper();
		BVDesignLicenseInfo bvDesignLicenseInfo = new BVDesignLicenseInfo();
		bvDesignLicenseInfo = mapper.readValue(new File(bvDesignLocation + BVDESIGN_STATUS_OUTPUT_FILENAME), BVDesignLicenseInfo.class);

		return bvDesignLicenseInfo;

	}

	protected void generateBVDesignLicenseJsonFile(String bvDesignLocation) throws IOException {

		Process p = null;

		try {

			p = new ProcessBuilder(bvDesignLocation + BVDesignLicenseUtil.BVDESIGN_EXE, "-status -json").start();
			p.waitFor();

		} catch (final IOException e) {

			if (p != null) {
				// missing these was causing the mass amounts of open 'files'
				p.getInputStream().close();
				p.getOutputStream().close();
				p.getErrorStream().close();
			}

		} catch (InterruptedException e) {
			BVDesignLicenseUtil.LOG.error(e.getMessage(), e);
		}

	}

	protected String getBreedingViewExeLocation() {
		String bvDesignLocation = null;
		Tool bvTool = null;

		bvTool = this.workbenchService.getToolWithName(AppConstants.TOOL_NAME_BREEDING_VIEW.getString());

		if (bvTool != null) {
			// write xml to temp file
			final File absoluteToolFile = new File(bvTool.getPath()).getAbsoluteFile();
			bvDesignLocation = absoluteToolFile.getAbsolutePath().replaceAll(BVDesignLicenseUtil.BREEDING_VIEW_EXE, "");
		}
		return bvDesignLocation;
	}
}
