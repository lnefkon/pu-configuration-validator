package com.efkon.puconfigurationvalidator.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.efkon.puconfigurationvalidator.view.SuccessResponse;

public interface ProcessingUnitService {

	/**
	 * This method is used to save the file on a configured path and set status code
	 * in response.
	 * 
	 * @param request
	 * @param response
	 * @param file
	 * 
	 * @return {@link SuccessResponse}
	 */
	public SuccessResponse saveVaConfig(HttpServletRequest request, HttpServletResponse response, String productCode,
			MultipartFile file);
}
