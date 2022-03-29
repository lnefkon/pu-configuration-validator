package com.efkon.puconfigurationvalidator.ex;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestErrorHandler extends ResponseEntityExceptionHandler {

	private static Logger logger = LoggerFactory.getLogger(RestErrorHandler.class);

	@ExceptionHandler({ AuthException.class })
	public ResponseEntity<Object> handleAuthenticationError(Exception exception, WebRequest request) {
		logger.warn("Auth error:" + exception.getMessage());
		final ApiError apierror = message(HttpStatus.UNAUTHORIZED, exception);
		return handleExceptionInternal(exception, apierror, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler({ AccessDeniedException.class })
	public ResponseEntity<Object> handleAuthorisationError(Exception exception, WebRequest request) {
		logger.warn("Authorisation error:" + exception.getMessage());
		final ApiError apierror = message(HttpStatus.FORBIDDEN, exception);
		return handleExceptionInternal(exception, apierror, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
	}

	@ExceptionHandler({ NotFoundException.class })
	public ResponseEntity<Object> handleMissing(NotFoundException exception, WebRequest request) {
		logger.error("NOT FOUND:" + exception.getMessage());
		final ApiError apierror = message(HttpStatus.NOT_FOUND, exception);
		return handleExceptionInternal(exception, apierror, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}

	@ExceptionHandler({ ValidationException.class })
	public ResponseEntity<Object> handleLogical(ValidationException exception, WebRequest request) {
		logger.error("Validation ERROR:" + exception.getMessage());
		final ApiError apierror = message(HttpStatus.BAD_REQUEST, exception);
		return handleExceptionInternal(exception, apierror, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler({ XmlFileException.class })
	public ResponseEntity<Object> handleXmlFileValidation(XmlFileException exception, WebRequest request) {
		logger.error("ParseXmlFileException ERROR:" + exception.getMessage());
		final ApiError apierror = message(HttpStatus.BAD_REQUEST, exception);
		return handleExceptionInternal(exception, apierror, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler
	public ResponseEntity<Object> handleJavaxValidator(ConstraintViolationException exception, WebRequest request) {
		logger.error("Constratint Violation:", exception.getMessage());
		final ApiError apierror = message(HttpStatus.BAD_REQUEST, exception.getConstraintViolations());
		return handleExceptionInternal(exception, apierror, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		logger.error("Method Argument Error:", exception);
		final ApiError apierror = new ApiError(HttpStatus.BAD_REQUEST.value(),
				bindingResultMessageToString(exception.getBindingResult()), "Validation Failed");
		return new ResponseEntity<Object>(apierror, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler
	public ResponseEntity<Object> handleOther(RuntimeException exception, WebRequest request) {
		logger.error("ERROR:", exception);
		final ApiError apierror = message(HttpStatus.INTERNAL_SERVER_ERROR, exception);
		return handleExceptionInternal(exception, apierror, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
				request);
	}

	private ApiError message(final HttpStatus httpStatus, final Exception ex) {
		final String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
		return new ApiError(httpStatus.value(), message, message);
	}

	private ApiError message(final HttpStatus httpStatus, final Set<ConstraintViolation<?>> contraintViolations) {
		final String message = contraintViolationsToString(contraintViolations);
		return new ApiError(httpStatus.value(), message, message);
	}

	private String contraintViolationsToString(final Set<ConstraintViolation<?>> contraintViolations) {
		StringBuilder builder = new StringBuilder();
		for (ConstraintViolation<?> contraintViolation : contraintViolations) {
			builder.append(contraintViolation.getPropertyPath() + " : " + contraintViolation.getMessage() + ". ");
		}
		return builder.toString();
	}

	private String bindingResultMessageToString(final BindingResult bindingResult) {
		Set<String> errors = new HashSet<>();
		for (ObjectError error : bindingResult.getAllErrors()) {
			errors.add(error.getDefaultMessage());
		}
		return String.join(", ", errors);
	}
}
