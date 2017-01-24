package com.efficio.fieldbook.service.internal;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class BVDesignLicenseUtilTest {

	@Mock
	private WorkbenchService workbenchService;

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

		bvDesignLicenseInfo.getStatus().getLicense().setExpiryDays("AAA");
		Assert.assertTrue(bvDesignLicenseUtil.isExpiringWithinThirtyDays(bvDesignLicenseInfo));

		bvDesignLicenseInfo.getStatus().getLicense().setExpiryDays("31");
		Assert.assertFalse(bvDesignLicenseUtil.isExpiringWithinThirtyDays(bvDesignLicenseInfo));

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
