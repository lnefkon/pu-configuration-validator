package com.efkon.puconfigurationvalidator.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.efkon.puconfigurationvalidator.service.impl.ProcessingUnitServiceImpl;
import com.efkon.puconfigurationvalidator.view.SuccessResponse;

@RestController
@RequestMapping("/api/")
public class ProcessingUnitController {

	@Autowired
	private ProcessingUnitServiceImpl processingUnitServiceImpl;

	@PostMapping(value = "configuration", produces = { "application/json" }, consumes = { "multipart/form-data" })
	public SuccessResponse saveVaConfig(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name = "file") MultipartFile file, @RequestParam(name = "productCode") String productCode) {
		return processingUnitServiceImpl.saveVaConfig(request, response, productCode, file);
	}
}
