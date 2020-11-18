
package com.efficio.fieldbook.service.api;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import org.generationcp.middleware.exceptions.MiddlewareException;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public interface SettingsService {

	public SettingDetail createSettingDetail(int id, String name, UserSelection userSelection,
			int currentIbDbUserId, String programUUID) throws MiddlewareException;

	void populateSettingVariable(SettingVariable var);

	void addNewSettingDetails(int mode, List<SettingDetail> newDetails) throws Exception;
}
