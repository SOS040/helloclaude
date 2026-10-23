package com.example.board.global.error;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * ResponseEntityExceptionHandler를 상속하므로 Spring MVC 기본 예외도 ProblemDetail로 응답한다.
 * (이 Bean이 있으면 Boot의 기본 ProblemDetailsExceptionHandler는 등록되지 않는다.)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ProblemDetail handleBusinessException(BusinessException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		log.warn("Business exception: code={}, message={}", errorCode, ex.getMessage());
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), ex.getMessage());
		problemDetail.setProperty("code", errorCode.name());
		return problemDetail;
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<FieldErrorDetail> errors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map((fieldError) -> new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
			.toList();
		ProblemDetail problemDetail = ex.getBody();
		problemDetail.setDetail("입력값이 올바르지 않습니다.");
		problemDetail.setProperty("errors", errors);
		return handleExceptionInternal(ex, problemDetail, headers, status, request);
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpectedException(Exception ex) {
		log.error("Unexpected exception", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
	}

	public record FieldErrorDetail(String field, String message) {

	}

}
