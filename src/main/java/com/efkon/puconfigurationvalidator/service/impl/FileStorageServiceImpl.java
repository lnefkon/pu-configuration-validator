package com.efkon.puconfigurationvalidator.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import com.efkon.puconfigurationvalidator.config.FileStorageProperties;
import com.efkon.puconfigurationvalidator.ex.NotFoundException;
import com.efkon.puconfigurationvalidator.ex.UploadException;
import com.efkon.puconfigurationvalidator.ex.ValidationException;
import com.efkon.puconfigurationvalidator.service.FileStorageService;

@Profile(value = "default")

@Service("fileStorageServiceImpl")
public class FileStorageServiceImpl<T> implements FileStorageService<T> {

	private final Path fileStorageLocation;
	private final String supportedExtension;

	@Autowired
	public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
		this.supportedExtension = fileStorageProperties.getSupportedExtension();
		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new UploadException("Could not create the directory where the uploaded files will be stored.", ex);
		}
	}

	private String getFileExtension(String fileOriginalName) {
		String fileExtension = fileOriginalName.substring(fileOriginalName.lastIndexOf("."));
		if (!supportedExtension.contains(fileExtension)) {
			throw new ValidationException(String.format("this format[%s] not supported", fileExtension));
		}
		return fileExtension;
	}

	@Override
	public Resource loadFileAsResource(String filepath) {
		try {
			Path filePath = Paths.get(filepath).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new NotFoundException("File not found " + filepath);
			}
		} catch (MalformedURLException ex) {
			throw new NotFoundException("File not found " + filepath, ex);
		}
	}

	@Override
	public byte[] loadFileAsByte(String filepath) throws IOException {
		try {
			Path filePath = Paths.get(filepath).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				FileInputStream in = new FileInputStream(resource.getFile());
				byte[] fileBytes = IOUtils.toByteArray(in);
				in.close();
				return fileBytes;
			} else {
				throw new NotFoundException("File not found " + filepath);
			}
		} catch (MalformedURLException ex) {
			throw new NotFoundException("File not found " + filepath, ex);
		}
	}

	@Override
	public boolean isExist(String filepath) {
		Path filePath = Paths.get(filepath).normalize();
		Resource resource;
		try {
			resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return true;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String storeFile(T file, String filename, String filepath, Boolean changeFileName) {
		String nFilename = null;
		String fileExtension = getFileExtension(filename);
		Path targetLocation = Paths.get(this.fileStorageLocation.toString()).toAbsolutePath().normalize();
		try {
			Files.createDirectories(targetLocation);
			if (changeFileName != null && changeFileName) {
				nFilename = UUID.randomUUID().toString() + fileExtension;
				targetLocation = targetLocation.resolve(nFilename);
			} else {
				targetLocation = targetLocation.resolve(filename);
			}
			if (file instanceof MultipartFile) {
				Files.copy(((MultipartFile) file).getInputStream(), targetLocation,
						StandardCopyOption.REPLACE_EXISTING);
			} else if (file instanceof Resource) {
				Files.copy(((Resource) file).getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			} else if (file instanceof Document) {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer;
				transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				DOMSource source = new DOMSource((Document) file);
				StreamResult result = new StreamResult(new File(targetLocation.toString()));
				transformer.transform(source, result);
			} else {
				throw new RuntimeException(
						String.format("For this type of file (%s) implementation not found", file.getClass()));
			}
			return targetLocation.toString();
		} catch (IOException io) {
			io.printStackTrace();
			throw new RuntimeException(io.getMessage());
		} catch (TransformerException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		}
	}

	@Override
	public void delete(String filepath) {
		Path targetLocation = Paths.get(filepath).toAbsolutePath().normalize();
		try {
			FileUtils.cleanDirectory(new File(targetLocation.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
