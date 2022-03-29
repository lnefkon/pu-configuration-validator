package com.efkon.puconfigurationvalidator.ex;

public class ApiError {

	private int status;
	private String message;
	private String error;

	public ApiError(int status, String message, String error) {
		this.status = status;
		this.message = message;
		this.error = error;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
