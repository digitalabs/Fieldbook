package com.efficio.fieldbook.web.stock;

import org.generationcp.commons.service.StockService;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 4/24/2015
 * Time: 4:38 PM
 */
@Controller
@RequestMapping(value = StockController.URL)
public class StockController {
	private static final Logger LOG = LoggerFactory.getLogger(StockController.class);

	public static final String URL = "/stock";
	public static final String IS_SUCCESS = "isSuccess";
	public static final String FAILURE = "0";
	public static final String SUCCESS = "1";

	@Resource
	private StockService stockService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private StockIDGenerationSettings generationSettings;

	@ResponseBody
	@RequestMapping(value = "/retrieveNextStockPrefix", method = RequestMethod.POST)
	public Map<String, String> retrieveNextStockIDPrefix(@RequestBody StockIDGenerationSettings generationSettings) {
		Map<String, String> resultMap = new HashMap<>();
		try {

			validateGenerationSettings(resultMap, generationSettings);
			if (resultMap.containsKey(IS_SUCCESS) && resultMap.get(IS_SUCCESS).equals(FAILURE)) {
				return resultMap;
			}

			String prefix = stockService.calculateNextStockIDPrefix(generationSettings.getBreederIdentifier(), generationSettings.getSeparator());
			// for UI purposes, we remove the separator from the generated prefix
			prefix = prefix.substring(0, prefix.length() -1);
			resultMap.put(IS_SUCCESS, SUCCESS);
			resultMap.put("prefix", prefix);

			// store the current generation settings to session for later use
			this.generationSettings.copy(generationSettings);
		} catch (MiddlewareException e) {
			LOG.error(e.getMessage(), e);
			resultMap.put(IS_SUCCESS, FAILURE);
		}

		return resultMap;
	}

	protected void validateGenerationSettings(Map<String, String> resultMap, StockIDGenerationSettings settings) {
		// verify that only character letters are present in breeder identifier

		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher matcher = pattern.matcher(settings.getBreederIdentifier());

		if (matcher.find()) {
			resultMap.put(IS_SUCCESS, FAILURE);
			resultMap.put("errorMessage", messageSource.getMessage(
					"stock.generate.id.breeder.identifier.error.numbers.found", new Object[]{},
					Locale.getDefault()));
		}
	}
}
