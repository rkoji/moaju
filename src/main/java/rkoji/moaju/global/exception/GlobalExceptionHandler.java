package rkoji.moaju.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.global.response.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ErrorResponse.of(errorCode.name(), errorCode.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("Unexpected error", e);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ErrorResponse.of(errorCode.name(), errorCode.getMessage()));
	}
}
