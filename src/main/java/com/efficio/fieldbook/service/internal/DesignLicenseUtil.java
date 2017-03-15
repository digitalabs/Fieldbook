package com.efficio.fieldbook.service.internal;

import com.efficio.fieldbook.service.internal.breedingview.BVDesignLicenseInfo;
import com.efficio.fieldbook.service.internal.breedingview.BVLicenseParseException;

public interface DesignLicenseUtil {

	public boolean isExpired(BVDesignLicenseInfo bvDesignLicenseInfo);

	public boolean isExpiringWithinThirtyDays(BVDesignLicenseInfo bvDesignLicenseInfo);

	public BVDesignLicenseInfo retrieveLicenseInfo() throws BVLicenseParseException;

}
