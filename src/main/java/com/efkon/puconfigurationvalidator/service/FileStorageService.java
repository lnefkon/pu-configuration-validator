package com.efkon.puconfigurationvalidator.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.efkon.puconfigurationvalidator.ex.NotFoundException;
import com.efkon.puconfigurationvalidator.ex.UploadException;
import com.efkon.puconfigurationvalidator.ex.ValidationException;

public interface FileStorageService<T> {

	public static Path createFolder(String folderLocation) {
		final Path folderPath = Paths.get(folderLocation).toAbsolutePath().normalize();
		try {
			Files.createDirectories(folderPath);
			return folderPath;
		} catch (Exception ex) {
			throw new UploadException("Could not create the directory where the uploaded files will be stored.", ex);
		}
	}

	public static Resource fetchFile(String filePath) {
		try {
			Resource resource = new UrlResource(Paths.get(filePath).toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new NotFoundException("File not found");
			}
		} catch (MalformedURLException e) {
			throw new ValidationException("Error in reading file");
		}
	}

	public String storeFile(T file, String filename, String filepath, Boolean changeFileName);

	public Resource loadFileAsResource(String fileName) throws IOException;

	public byte[] loadFileAsByte(String filename) throws IOException;

	public boolean isExist(String filepath);

	public void delete(String filepath);

}
