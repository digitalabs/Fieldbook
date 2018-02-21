package com.efficio.fieldbook.service.internal.impl;

import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
import com.efficio.fieldbook.service.internal.DesignLicenseUtil;
import com.efficio.fieldbook.service.internal.breedingview.BVLicenseParseException;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

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
	public static final String LICENSE_SUCCESS_CODE = "0";
	private static final Logger LOG = LoggerFactory.getLogger(BVDesignLicenseUtil.class);

	public static final String BVDESIGN_STATUS_OUTPUT_FILENAME = "son";

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private MessageSource messageSource;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public boolean isExpired(final BVDesignLicenseInfo bvDesignLicenseInfo) {

		try {

			final Format formatter = new SimpleDateFormat(LICENSE_DATE_FORMAT);
			final Date expiryDate = (Date) formatter.parseObject(bvDesignLicenseInfo.getStatus().getLicense().getExpiry());
			final Date currentDate = DateUtil.getCurrentDateWithZeroTime();
			if (currentDate.compareTo(expiryDate) > 0) {
				return true;
			}
		} catch (final ParseException e) {
			BVDesignLicenseUtil.LOG.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean isExpiringWithinThirtyDays(final BVDesignLicenseInfo bvDesignLicenseInfo) {

		int expiryDays = 0;

		try {
			expiryDays = Integer.parseInt(bvDesignLicenseInfo.getStatus().getLicense().getExpiryDays());
		} catch (final NumberFormatException e) {

			final String errorMessage = this.messageSource.getMessage("bv.design.error.expiry.days.not.numeric", null, LocaleContextHolder.getLocale());
			BVDesignLicenseUtil.LOG.error(errorMessage, e);
			return true;
		}

		return expiryDays <= 30;

	}

	@Override
	public BVDesignLicenseInfo retrieveLicenseInfo() throws BVLicenseParseException {

		final String bvDesignLocation = this.fieldbookProperties.getBvDesignPath();

		this.generateBVDesignLicenseJsonFile(bvDesignLocation);

		final String jsonPathFile = new File(bvDesignLocation).getParent() + File.separator + BVDESIGN_STATUS_OUTPUT_FILENAME;
		return this.readLicenseInfoFromJsonFile(new File(jsonPathFile));
	}

	protected BVDesignLicenseInfo readLicenseInfoFromJsonFile(final File file) throws BVLicenseParseException {

		BVDesignLicenseInfo bvDesignLicenseInfo = new BVDesignLicenseInfo();

		try {

			bvDesignLicenseInfo = objectMapper.readValue(file, BVDesignLicenseInfo.class);

		} catch (final IOException e) {

			final String errorMessage = this.messageSource.getMessage("bv.design.error.cannot.read.license.file", null, LocaleContextHolder.getLocale());
			BVDesignLicenseUtil.LOG.error(errorMessage + ":" + e.getMessage(), e);
			throw new BVLicenseParseException(errorMessage);
		}

		if (!LICENSE_SUCCESS_CODE.equals(bvDesignLicenseInfo.getStatus().getReturnCode())) {
			final String errorMessage = this.messageSource.getMessage("bv.design.error.generic", null, LocaleContextHolder.getLocale());
			throw new BVLicenseParseException(errorMessage + bvDesignLicenseInfo.getStatus().getAppStatus());
		}

		return bvDesignLicenseInfo;

	}

	protected void generateBVDesignLicenseJsonFile(final String bvDesignLocation) throws BVLicenseParseException {

		Process p = null;
		final String errorMessage = this.messageSource.getMessage("bv.design.error.failed.license.generation", null, LocaleContextHolder.getLocale());

		try {

			final String bvDesignDirectory = new File(bvDesignLocation).getParent();
			final ProcessBuilder processBuilder = new ProcessBuilder(bvDesignLocation, "-status", "-json");
			processBuilder.directory(new File(bvDesignDirectory));
			p = processBuilder.start();
			p.waitFor();

		} catch (final Exception e) {

			BVDesignLicenseUtil.LOG.error(errorMessage + ":" + e.getMessage(), e);
			throw new BVLicenseParseException(errorMessage);
		}

	}

}
