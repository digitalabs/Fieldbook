package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Daniel Villafuerte on 6/12/2015.
 */

@Component
public class ChangeLocationExpression implements Expression {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeLocationExpression.class);

    public static final String KEY = "[CLABBR]";

    @Resource
    private GermplasmDataManager germplasmDataManager;

    @Override
    public void apply(List<StringBuilder> values, AdvancingSource source) {
        for (StringBuilder value : values) {
            int startIndex = value.toString().toUpperCase().indexOf(ChangeLocationExpression.KEY);
            int endIndex = startIndex + ChangeLocationExpression.KEY.length();

            try {
                Germplasm originalGermplasm = germplasmDataManager.getGermplasmByGID(Integer.valueOf(source.getGermplasm().getGid()));
                String suffixValue = "";
                if (source.getLocationId() != null && !originalGermplasm.getLocationId().equals(source.getLocationId())) {
                    suffixValue = source.getLocationAbbreviation();
                }

                value.replace(startIndex, endIndex, suffixValue);
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String getExpressionKey() {
        return KEY;
    }
}
