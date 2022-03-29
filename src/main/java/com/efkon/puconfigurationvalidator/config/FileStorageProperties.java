package com.efkon.puconfigurationvalidator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The @ConfigurationProperties(prefix = "file") annotation does its job on
 * application startup and binds all the properties with prefix file to the
 * corresponding fields.
 */
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

	private String uploadDir;
	private String supportedExtension;

	public String getUploadDir() {
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}

	public String getSupportedExtension() {
		return supportedExtension;
	}

	public void setSupportedExtension(String supportedExtension) {
		this.supportedExtension = supportedExtension;
	}
}
