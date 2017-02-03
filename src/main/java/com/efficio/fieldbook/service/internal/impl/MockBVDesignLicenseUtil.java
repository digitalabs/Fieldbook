package com.efficio.fieldbook.service.internal.impl;

import com.efficio.fieldbook.service.internal.DesignLicenseUtil;
import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
import com.efficio.fieldbook.service.internal.breedingview.BVLicenseParseException;

import java.io.IOException;

public class MockBVDesignLicenseUtil implements DesignLicenseUtil {

	@Override
	public boolean isExpired(final BVDesignLicenseInfo bvDesignLicenseInfo) {
		return false;
	}

	@Override
	public boolean isExpiringWithinThirtyDays(final BVDesignLicenseInfo bvDesignLicenseInfo) {
		return false;
	}

	@Override
	public BVDesignLicenseInfo retrieveLicenseInfo() throws BVLicenseParseException {

		return null;
	}
}
