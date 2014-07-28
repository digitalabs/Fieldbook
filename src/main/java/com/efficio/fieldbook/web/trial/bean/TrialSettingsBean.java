package com.efficio.fieldbook.web.trial.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 7/9/2014
 * Time: 5:32 PM
 */
public class TrialSettingsBean implements TabInfoBean{
    private Map<String, String> userInput;

    public TrialSettingsBean() {
        userInput = new HashMap<String, String>();
    }

    public Map<String, String> getUserInput() {
        return userInput;
    }

    public void setUserInput(Map<String, String> userInput) {
        this.userInput = userInput;
    }
}
