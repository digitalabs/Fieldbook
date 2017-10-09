package com.efficio.fieldbook.service.internal.impl;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
import com.efficio.fieldbook.service.internal.breedingview.BVLicenseParseException;
import com.efficio.fieldbook.service.internal.breedingview.License;
import com.efficio.fieldbook.service.internal.breedingview.Status;
import com.efficio.fieldbook.service.internal.impl.BVDesignLicenseUtil;
import com.efficio.fieldbook.web.util.AppConstants;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@RunWith(MockitoJUnitRunner.class)
public class BVDesignLicenseUtilTest {

	public static final String GENERIC_ERROR = "BVDesign returned an error: ";
	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private BVDesignLicenseUtil bvDesignLicenseUtil;

	@Test
	public void testIsExpiredWithNoDate() {

		BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().getLicense().setExpiry("");

		final boolean isExpired = bvDesignLicenseUtil.isExpired(bvDesignLicenseInfo);
		Assert.assertFalse("It should not be expired as there's no expiration date", isExpired);
	}

	@Test
	public void testIsExpiredWithExpiredDate() {

		final Calendar calendar = DateUtil.getCalendarInstance();
		calendar.setTime(DateUtil.getCurrentDateWithZeroTime());
		calendar.add(Calendar.DAY_OF_WEEK, -1);

		SimpleDateFormat df = new SimpleDateFormat(BVDesignLicenseUtil.LICENSE_DATE_FORMAT);
		BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().getLicense().setExpiry(df.format(calendar.getTime()));

		final boolean isExpired = bvDesignLicenseUtil.isExpired(bvDesignLicenseInfo);
		Assert.assertTrue("It should be expired", isExpired);
	}

	@Test
	public void testIsExpiredWithCurrentDate() {

		SimpleDateFormat df = new SimpleDateFormat(BVDesignLicenseUtil.LICENSE_DATE_FORMAT);
		BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().getLicense().setExpiry(df.format(DateUtil.getCurrentDateWithZeroTime()));

		final boolean isExpired = bvDesignLicenseUtil.isExpired(bvDesignLicenseInfo);

		Assert.assertFalse("It should not be expired", isExpired);
	}

	@Test
	public void testIsExpiredWithDateASecondAfterCurrentDate() {
		final Calendar calendar = DateUtil.getCalendarInstance();
		calendar.setTime(DateUtil.getCurrentDateWithZeroTime());
		calendar.add(Calendar.SECOND, 1);

		SimpleDateFormat df = new SimpleDateFormat(BVDesignLicenseUtil.LICENSE_DATE_FORMAT);
		BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().getLicense().setExpiry(df.format(calendar.getTime()));

		final boolean isExpired = bvDesignLicenseUtil.isExpired(bvDesignLicenseInfo);
		Assert.assertFalse("It should not be expired", isExpired);
	}

	@Test
	public void testIsExpiringWithinThirtyDays() {

		BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();

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

		BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		Mockito.when(this.messageSource.getMessage(Mockito.anyString(), Mockito.any(Object[].class), Mockito.any(Locale.class))).thenReturn("");
		bvDesignLicenseInfo.getStatus().getLicense().setExpiryDays("AAA");

		Assert.assertTrue(bvDesignLicenseUtil.isExpiringWithinThirtyDays(bvDesignLicenseInfo));
		Mockito.verify(this.messageSource).getMessage("bv.design.error.expiry.days.not.numeric", null, LocaleContextHolder.getLocale());

	}

	@Test
	public void testReadLicenseInfoFromJsonFileSuccess() throws IOException {


		File file = Mockito.mock(File.class);
		BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		bvDesignLicenseInfo.getStatus().setReturnCode(BVDesignLicenseUtil.LICENSE_SUCCESS_CODE);
		Mockito.when(objectMapper.readValue(file, BVDesignLicenseInfo.class)).thenReturn(bvDesignLicenseInfo);

		try {

			bvDesignLicenseUtil.readLicenseInfoFromJsonFile(file);

		} catch (BVLicenseParseException e) {

			Assert.fail("The method should not throw an exception");

		}

	}

	@Test
	public void testReadLicenseInfoFromJsonFileException() throws IOException {


		File file = Mockito.mock(File.class);
		BVDesignLicenseInfo bvDesignLicenseInfo = this.createBVDesignLicenseInfo();

		Mockito.when(this.messageSource.getMessage(Mockito.anyString(), Mockito.any(Object[].class), Mockito.any(Locale.class))).thenReturn(
				GENERIC_ERROR);


		// If BVDesign failed in generating the license file the return code value will be a non-zero (-1).
		String errorStatusCode = "-1";
		String errorMessage = "There is an error.";
		bvDesignLicenseInfo.getStatus().setReturnCode(errorStatusCode);
		bvDesignLicenseInfo.getStatus().setAppStatus(errorMessage);
		Mockito.when(objectMapper.readValue(file, BVDesignLicenseInfo.class)).thenReturn(bvDesignLicenseInfo);

		try {

			bvDesignLicenseUtil.readLicenseInfoFromJsonFile(file);
			Assert.fail("The method should throw an exception");

		} catch (BVLicenseParseException e) {

			Assert.assertEquals(GENERIC_ERROR + errorMessage, e.getMessage());

		}

	}


	private BVDesignLicenseInfo createBVDesignLicenseInfo() {
		BVDesignLicenseInfo bvDesignLicenseInfo = new BVDesignLicenseInfo();
		Status status = new Status();
		License license = new License();
		status.setLicense(license);
		bvDesignLicenseInfo.setStatus(status);
		return bvDesignLicenseInfo;
	}



}
