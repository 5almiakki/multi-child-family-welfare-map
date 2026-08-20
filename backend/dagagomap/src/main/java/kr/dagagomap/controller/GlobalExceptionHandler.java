package kr.dagagomap.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.dagagomap.controller.dto.response.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponseDto> handleMissingParameterException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"MISSING_REQUEST_PARAM",
				"필수 요청 파라미터가 없습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ErrorResponseDto> handleMissingRequestHeaderException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"MISSING_REQUEST_HEADER",
				"필수 요청 헤더가 없습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(MissingPathVariableException.class)
	public ResponseEntity<ErrorResponseDto> handleMissingPathVariableException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"MISSING_PATH_VARIABLE",
				"필수 경로 변수가 누락되었습니다.",
				request.getRequestURI());
		return ResponseEntity.internalServerError().body(response);
	}

	@ExceptionHandler(MissingMatrixVariableException.class)
	public ResponseEntity<ErrorResponseDto> handleMissingMatrixVariableException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"MISSING_MATRIX_VARIABLE",
				"필수 매트릭스 변수가 없습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(MissingRequestCookieException.class)
	public ResponseEntity<ErrorResponseDto> handleMissingRequestCookieException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"MISSING_REQUEST_COOKIE",
				"필수 요청 쿠키가 없습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<ErrorResponseDto> handleMissingServletRequestPartException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"MISSING_SERVLET_REQUEST_PART",
				"필수 멀티파트 요청의 일부가 누락되었습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
	public ResponseEntity<ErrorResponseDto> handleUnsatisfiedServletRequestParameterException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"UNSATISFIED_SERVLET_REQUEST_PARAMETER",
				"요청 파라미터 조건이 충족되지 않았습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"METHOD_ARGUMENT_NOT_VALID",
				"요청 데이터 검증에 실패했습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErrorResponseDto> handleHandlerMethodValidationException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"HANDLER_METHOD_VALIDATION_FAILED",
				"요청 값 검증에 실패했습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(TypeMismatchException.class)
	public ResponseEntity<ErrorResponseDto> handleTypeMismatchException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"TYPE_MISMATCH",
				"요청 값의 타입이 올바르지 않습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"HTTP_MESSAGE_NOT_READABLE",
				"요청 본문을 읽을 수 없습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(HttpMessageNotWritableException.class)
	public ResponseEntity<ErrorResponseDto> handleHttpMessageNotWritableException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"HTTP_MESSAGE_NOT_WRITABLE",
				"응답 본문을 작성할 수 없습니다.",
				request.getRequestURI());
		return ResponseEntity.internalServerError().body(response);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponseDto> handleHttpRequestMethodNotSupportedException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"HTTP_REQUEST_METHOD_NOT_SUPPORTED",
				"지원하지 않는 HTTP 메서드입니다.",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponseDto> handleHttpMediaTypeNotSupportedException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"MEDIA_TYPE_NOT_SUPPORTED",
				"지원하지 않는 미디어 타입입니다.",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
	}

	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public ResponseEntity<ErrorResponseDto> handleHttpMediaTypeNotAcceptableException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"MEDIA_TYPE_NOT_ACCEPTABLE",
				"요청한 미디어 타입을 생성할 수 없습니다.",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
	}

	@ExceptionHandler(ConversionNotSupportedException.class)
	public ResponseEntity<ErrorResponseDto> handleConversionNotSupportedException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"CONVERSION_NOT_SUPPORTED",
				"요청 속성을 변환할 수 없습니다.",
				request.getRequestURI());
		return ResponseEntity.internalServerError().body(response);
	}

	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public ResponseEntity<ErrorResponseDto> handleAsyncRequestTimeoutException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"ASYNC_REQUEST_TIMEOUT",
				"비동기 요청 처리 시간이 초과되었습니다.",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleNoHandlerFoundException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"NO_HANDLER_FOUND",
				"요청 경로에 해당하는 핸들러를 찾을 수 없습니다.",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleNoResourceFoundException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"NO_RESOURCE_FOUND",
				"요청된 자원이 없습니다.",
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDto> handleUnExpectedException(Exception e, HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"INTERNAL_SERVER_ERROR",
				"일시적인 오류가 발생했습니다.",
				request.getRequestURI());
		log.error(e.getMessage(), e);
		return ResponseEntity.internalServerError().body(response);
	}

}
