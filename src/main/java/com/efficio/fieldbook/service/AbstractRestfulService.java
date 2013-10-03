package com.efficio.fieldbook.service;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.efficio.pojos.cropontology.CropTerm;

/**
 * This is the abstract class that implements basic calls
 * to client RESTful web services.
 */
public abstract class AbstractRestfulService {

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * GET call to the RESTful Web Service.
	 * 
	 * @param url
	 * @param responseType
	 * @return
	 */
	public <T> T get(String url, Class<T> responseType) {
		return (T) restTemplate.getForObject(url, responseType);
	}
	
	/**
	 * GET call to the RESTful Web Service.
	 * 
	 * @param url
	 * @param responseType
	 * @param param
	 * @return
	 */
	public <T> T get(String url, Class<T> responseType, Object... param) {
		return (T) restTemplate.getForObject(url, responseType, param);
	}
	
	/**
	 * GET call to the RESTful Web Service that returns a List.
	 * 
	 * @param url
	 * @param responseType
	 * @return
	 */
	public <T> List<T> getList(String url, Class<T> responseType) {
		T[] results = (T[]) restTemplate.getForObject(url, Array.newInstance(responseType, 0).getClass());
		return Arrays.asList(results);
	}
	
	/**
	 * GET call to the RESTful Web Service that returns a List.
	 * 
	 * @param url
	 * @param responseType
	 * @param param
	 * @return
	 */
	public <T> List<T> getList(String url, Class<T> responseType, Object... param) {
		T[] results = (T[]) restTemplate.getForObject(url, Array.newInstance(responseType, 0).getClass(), param);
		return Arrays.asList(results);
	}
}
