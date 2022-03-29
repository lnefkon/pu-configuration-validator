package com.efkon.puconfigurationvalidator.ex;

@SuppressWarnings("serial")
public class XmlFileException extends RuntimeException {

	public XmlFileException() {
	}

	public XmlFileException(String message) {
		super(message);
	}

	public XmlFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public XmlFileException(Throwable cause) {
		super(cause);
	}

	public XmlFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
