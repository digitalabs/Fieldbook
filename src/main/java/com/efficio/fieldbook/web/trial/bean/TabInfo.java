package com.efficio.fieldbook.web.trial.bean;

import com.efficio.fieldbook.web.common.bean.SettingDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 7/8/2014
 * Time: 5:42 PM
 */
public class TabInfo {
    private TabInfoBean data;
    private List<SettingDetail> settings;
    private Map<String, Object> settingMap;

    public TabInfo() {
    }

    public TabInfoBean getData() {
        return data;
    }

    public void setData(TabInfoBean data) {
        this.data = data;
    }

    public List<SettingDetail> getSettings() {
        return settings;
    }

    public void setSettings(List<SettingDetail> settings) {
        this.settings = settings;
    }

    public Map<String, Object> getSettingMap() {
        return settingMap;
    }

    public void setSettingMap(Map<String, Object> settingMap) {
        this.settingMap = settingMap;
    }
}
