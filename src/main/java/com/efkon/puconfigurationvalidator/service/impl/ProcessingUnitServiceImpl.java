package com.efkon.puconfigurationvalidator.service.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import com.efkon.puconfigurationvalidator.ex.ValidationException;
import com.efkon.puconfigurationvalidator.service.FileStorageService;
import com.efkon.puconfigurationvalidator.service.ProcessingUnitService;
import com.efkon.puconfigurationvalidator.view.SuccessResponse;

@Service("processingUnitServiceImpl")
public class ProcessingUnitServiceImpl implements ProcessingUnitService {

	@Autowired
	private FileStorageService<MultipartFile> fileStorageService;

	@Value("${efkon.ees.code}")
	private String eesCode;

	@Value("${efkon.va.code}")
	private String vaCode;

	@Value("${tcp.server.hostname}")
	private String tcpServerHostName;

	@Value("${tcp.server.port}")
	private Integer tcpServerPort;

	@Value("${tcp.server.timeout.socket.millis}")
	private Integer tcpServerSocketTimeoutMillis;

	@Value("${file.upload-dir}")
	private String fileDir;

	private static Integer tcpRequestIndex = 0;

	private static Logger logger = LoggerFactory.getLogger(ProcessingUnitServiceImpl.class);

	@Override
	public SuccessResponse saveVaConfig(HttpServletRequest request, HttpServletResponse response, String productCode,
			MultipartFile file) {
		if (!productCode.equals(eesCode) && !productCode.equals(vaCode))
			throw new ValidationException(String.format("Invalid product code (%s)", productCode));

		String filePath = fileStorageService.storeFile(file, file.getOriginalFilename(), null, false);
		System.out.println("filePath : " + filePath);

		try (Socket clientSocket = new Socket(tcpServerHostName, tcpServerPort);
				PrintWriter pr = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {
//			Thread.sleep(5000);
			String opCode = "001";
			tcpRequestIndex = tcpRequestIndex + 1 > 999 ? 0 : tcpRequestIndex + 1;
			String coreMessage = String.format("%s,%s,%s", opCode, String.format("%03d", tcpRequestIndex), filePath);
			String checkSum = Integer.toHexString(xorAscii(coreMessage, coreMessage.length())).toUpperCase();
			String finalMessage = String.format("$%s*%s", coreMessage, checkSum);
			clientSocket.setSoTimeout(tcpServerSocketTimeoutMillis);
			System.out.print(finalMessage);
			pr.println(finalMessage);
//			pr.flush();
			String ack = in.readLine();
//			if (ack == null || !Pattern.compile("^\\$[0-9][0-9][1-9],[0-9][0-9][1-9],[0-3]\\*[a-fA-F0-9]+$")
//					.matcher(ack).matches()) {
//				throw new ValidationException(
//						String.format("Invalid format for Acknowledgement (%s) received from tcp server.", ack));
//			}
			if (ack == null || !ack.startsWith("$") || ack.split(",").length != 4) {
				throw new ValidationException(
						String.format("Invalid format for Acknowledgement (%s) received from tcp server.", ack));
			}
			System.out.print("Ack : " + ack);
			String responseCoreMessage = ack.substring(1, ack.lastIndexOf("*"));
			String[] serverMessageArr = responseCoreMessage.split(",");

			String responseCheckSum = Integer.toHexString(xorAscii(responseCoreMessage, responseCoreMessage.length()))
					.toUpperCase();
			if (!responseCheckSum.equals(ack.substring(ack.lastIndexOf("*") + 1)))
				throw new ValidationException("Acknowledgement from tcp server is corrupted.");

			if (serverMessageArr[0].equals(opCode)
					&& serverMessageArr[1].equals(String.format("%03d", tcpRequestIndex))) {
				switch (serverMessageArr[2]) {
				case "0":
					return new SuccessResponse(200,
							String.format("Configuration sent successfully.", file.getOriginalFilename()));
				case "1":
					throw new ValidationException(
							String.format("Acknowledgement from tcp server has checksum error for file (%s)",
									file.getOriginalFilename()));
				case "2":
					throw new ValidationException(String.format(
							"File path not found for file (%s), sent on tcp server.", file.getOriginalFilename()));
				case "3":
					String errorMessage = getConfigFileErrorByPath(serverMessageArr[3], file.getOriginalFilename());
					if (errorMessage.isEmpty()) {
						errorMessage = "Something went wrong, please contact system provider.";
						logger.info(errorMessage);
					}
					throw new ValidationException(errorMessage);
				default:
					throw new ValidationException(String.format("Invalid Error code (%s)", serverMessageArr[2]));
				}
			} else {
				throw new ValidationException(
						"Acknowledgement from server has either wrong command type or index number.");
			}
		} catch (ConnectException ce) {
			ce.printStackTrace();
			throw new ValidationException("Tcp server might be down, or tcp host/port are wrong in PU-application");
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage().split(",").length > 1 && e.getMessage().split(",")[1].contains("message"))
				throw new ValidationException(
						e.getMessage().split(",")[1].replaceAll("\"", "").replace("message", "").replaceFirst(":", ""));
			throw new ValidationException(e.getMessage());
		}
//		return new SuccessResponse(200, "Successfully consumed configuration file " + file.getOriginalFilename());
	}

	private int xorAscii(String str, int len) {
		int ans = (str.charAt(0));

		for (int i = 1; i < len; i++) {
			ans = (ans ^ ((str.charAt(i))));
		}
		return ans;
	}

	private String getConfigFileErrorByPath(String errorFilePath, String configFileName) {
		StringBuffer sb = new StringBuffer();
		Boolean debug = false, critical = false;
		try (FileReader fr = new FileReader(errorFilePath); BufferedReader br = new BufferedReader(fr);) {
			String line;
			logger.info("Error in config file {}", configFileName);

			while ((line = br.readLine()) != null) {
				String errorMsg = null;
				if (debug = line.contains("DEBUG")) {
					errorMsg = line.substring(line.indexOf("DEBUG") + 7);
					errorMsg = errorMsg.replaceFirst("-", "").trim();
					sb.append(String.format("%s\n ", errorMsg));
				} else if (critical = line.contains("CRITICAL")) {
					errorMsg = line.substring(line.indexOf("CRITICAL") + 10);
					errorMsg = errorMsg.replaceFirst("-", "").trim();
				}
				logger.info(errorMsg);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return String.format("Error file (%s) not found or can't be read.", errorFilePath);
		}
		String err = sb.toString();
		err = err.contains("\n") ? err.substring(0, err.length() - 2) : err;
		err = debug && critical ? String.format("%s*", err) : err;
		return err;
	}

}
