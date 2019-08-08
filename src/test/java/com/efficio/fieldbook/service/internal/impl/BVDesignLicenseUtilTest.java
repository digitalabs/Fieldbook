package com.efficio.fieldbook.service.internal.impl;

import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
import com.efficio.fieldbook.service.internal.breedingview.BVLicenseParseException;
import com.efficio.fieldbook.service.internal.breedingview.License;
import com.efficio.fieldbook.service.internal.breedingview.Status;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.junit.Before;

import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BVDesignLicenseUtilTest {

	public static final String GENERIC_ERROR = "BVDesign returned an error: ";
	public static final String FAILED_LICENSE_GENERATION = "failed license generation";

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private MessageSource messageSource;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private BVDesignLicenseUtil.BVDesignLicenseProcessRunner processRunner;

	@InjectMocks
	private BVDesignLicenseUtil bvDesignLicenseUtil;

	@Before
	public void init() {

		bvDesignLicenseUtil.setBvDesignLicenseProcessRunner(this.processRunner);

	}

	@Test
	public void testIsExpiredWithNoDate() {

		final BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().getLicense().setExpiry("");

		final boolean isExpired = bvDesignLicenseUtil.isExpired(bvDesignLicenseInfo);
		Assert.assertFalse("It should not be expired as there's no expiration date", isExpired);
	}

	@Test
	public void testIsExpiredWithExpiredDate() {

		final Calendar calendar = DateUtil.getCalendarInstance();
		calendar.setTime(DateUtil.getCurrentDateWithZeroTime());
		calendar.add(Calendar.DAY_OF_WEEK, -1);

		final SimpleDateFormat df = new SimpleDateFormat(BVDesignLicenseUtil.LICENSE_DATE_FORMAT);
		final BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().getLicense().setExpiry(df.format(calendar.getTime()));

		final boolean isExpired = bvDesignLicenseUtil.isExpired(bvDesignLicenseInfo);
		Assert.assertTrue("It should be expired", isExpired);
	}

	@Test
	public void testIsExpiredWithCurrentDate() {

		final SimpleDateFormat df = new SimpleDateFormat(BVDesignLicenseUtil.LICENSE_DATE_FORMAT);
		final BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().getLicense().setExpiry(df.format(DateUtil.getCurrentDateWithZeroTime()));

		final boolean isExpired = bvDesignLicenseUtil.isExpired(bvDesignLicenseInfo);

		Assert.assertFalse("It should not be expired", isExpired);
	}

	@Test
	public void testIsExpiredWithDateASecondAfterCurrentDate() {
		final Calendar calendar = DateUtil.getCalendarInstance();
		calendar.setTime(DateUtil.getCurrentDateWithZeroTime());
		calendar.add(Calendar.SECOND, 1);

		final SimpleDateFormat df = new SimpleDateFormat(BVDesignLicenseUtil.LICENSE_DATE_FORMAT);
		final BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().getLicense().setExpiry(df.format(calendar.getTime()));

		final boolean isExpired = bvDesignLicenseUtil.isExpired(bvDesignLicenseInfo);
		Assert.assertFalse("It should not be expired", isExpired);
	}

	@Test
	public void testIsExpiringWithinThirtyDays() {

		final BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();

		bvDesignLicenseInfo.getStatus().getLicense().setExpiryDays("30");
		Assert.assertTrue(bvDesignLicenseUtil.isExpiringWithinThirtyDays(bvDesignLicenseInfo));

		bvDesignLicenseInfo.getStatus().getLicense().setExpiryDays("0");
		Assert.assertTrue(bvDesignLicenseUtil.isExpiringWithinThirtyDays(bvDesignLicenseInfo));

		bvDesignLicenseInfo.getStatus().getLicense().setExpiryDays("-1");
		Assert.assertTrue(bvDesignLicenseUtil.isExpiringWithinThirtyDays(bvDesignLicenseInfo));

		bvDesignLicenseInfo.getStatus().getLicense().setExpiryDays("31");
		Assert.assertFalse(bvDesignLicenseUtil.isExpiringWithinThirtyDays(bvDesignLicenseInfo));

	}

	@Test
	public void testIsExpiringWithinThirtyDaysExpirayDaysNotNumeric() {

		final BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();

		bvDesignLicenseInfo.getStatus().getLicense().setExpiryDays("AAA");

		Assert.assertTrue(bvDesignLicenseUtil.isExpiringWithinThirtyDays(bvDesignLicenseInfo));
		Mockito.verify(this.messageSource).getMessage("bv.design.error.expiry.days.not.numeric", null, LocaleContextHolder.getLocale());

	}

	@Test
	public void testReadLicenseInfoFromJsonFileSuccess() throws IOException {

		final File file = Mockito.mock(File.class);
		final BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().setReturnCode(BVDesignLicenseUtil.LICENSE_SUCCESS_CODE);
		Mockito.when(objectMapper.readValue(file, BVDesignLicenseInfo.class)).thenReturn(bvDesignLicenseInfo);

		try {

			bvDesignLicenseUtil.readLicenseInfoFromJsonFile(file);

		} catch (final BVLicenseParseException e) {

			Assert.fail("The method should not throw an exception");

		}

	}

	@Test
	public void testReadLicenseInfoFromJsonFileException() throws IOException {

		final File file = Mockito.mock(File.class);
		final BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();

		Mockito.when(this.messageSource.getMessage(Mockito.anyString(), ArgumentMatchers.<Object[]>isNull(), Mockito.any(Locale.class)))
				.thenReturn(GENERIC_ERROR);

		// If BVDesign failed in generating the license file the return code value will be a non-zero (-1).
		final String errorStatusCode = "-1";
		final String errorMessage = "There is an error.";
		bvDesignLicenseInfo.getStatus().setReturnCode(errorStatusCode);
		bvDesignLicenseInfo.getStatus().setAppStatus(errorMessage);
		Mockito.when(objectMapper.readValue(file, BVDesignLicenseInfo.class)).thenReturn(bvDesignLicenseInfo);

		try {

			bvDesignLicenseUtil.readLicenseInfoFromJsonFile(file);
			Assert.fail("The method should throw an exception");

		} catch (final BVLicenseParseException e) {

			Assert.assertEquals(GENERIC_ERROR + errorMessage, e.getMessage());

		}

	}

	@Test
	public void testGenerateBVDesignLicenseJsonFileSuccess() throws IOException {

		final String bvDesignLocation = "parentDirectory/bvDesign";

		try {
			bvDesignLicenseUtil.generateBVDesignLicenseJsonFile(bvDesignLocation);
			verify(this.processRunner).setDirectory("parentDirectory");
			verify(this.processRunner).run(bvDesignLocation, "-status", "-json");
		} catch (final BVLicenseParseException e) {
			fail("generateBVDesignLicenseJsonFile should not throw a BVLicenseParseException");
		}

	}

	@Test
	public void testGenerateBVDesignLicenseJsonFileFailed() throws IOException {

		when(this.messageSource.getMessage("bv.design.error.failed.license.generation", null, LocaleContextHolder.getLocale()))
				.thenReturn(FAILED_LICENSE_GENERATION);

		final String bvDesignLocation = "parentDirectory/bvDesign";

		when(processRunner.run(bvDesignLocation, "-status", "-json")).thenThrow(new IOException());

		try {
			bvDesignLicenseUtil.generateBVDesignLicenseJsonFile(bvDesignLocation);
			fail("generateBVDesignLicenseJsonFile should throw a BVLicenseParseException");
		} catch (final BVLicenseParseException e) {
			assertEquals(FAILED_LICENSE_GENERATION, e.getMessage());
		}

	}

	@Test
	public void testRetrieveLicenseInfo() throws IOException {

		final String bvDesignLocation = "parentDirectory/bvdesign";
		final BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().setReturnCode(BVDesignLicenseUtil.LICENSE_SUCCESS_CODE);

		when(objectMapper.readValue(any(File.class), eq(BVDesignLicenseInfo.class))).thenReturn(bvDesignLicenseInfo);
		when(fieldbookProperties.getBvDesignPath()).thenReturn(bvDesignLocation);

		try {

			final BVDesignLicenseInfo licenseInfo = this.bvDesignLicenseUtil.retrieveLicenseInfo();
			verify(this.processRunner).setDirectory("parentDirectory");
			verify(this.processRunner).run(bvDesignLocation, "-status", "-json");
			verify(this.objectMapper).readValue(any(File.class), eq(BVDesignLicenseInfo.class));
			assertEquals(licenseInfo, bvDesignLicenseInfo);

		} catch (final BVLicenseParseException e) {
			fail("retrieveLicenseInfo should not throw a BVLicenseParseException");
		}

	}

	private BVDesignLicenseInfo createBVDesignLicenseInfo() {
		final BVDesignLicenseInfo bvDesignLicenseInfo = new BVDesignLicenseInfo();
		final Status status = new Status();
		final License license = new License();
		status.setLicense(license);
		bvDesignLicenseInfo.setStatus(status);
		return bvDesignLicenseInfo;
	}

}
