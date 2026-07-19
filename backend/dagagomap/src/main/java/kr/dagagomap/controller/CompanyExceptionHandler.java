package kr.dagagomap.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.dagagomap.controller.dto.response.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = CompanyExceptionHandlerScopeMarker.class)
public class CompanyExceptionHandler {

	@ExceptionHandler({
			MissingServletRequestParameterException.class
	})
	public ResponseEntity<ErrorResponseDto> handleMissingParameterException(HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(
				"MISSING_REQUEST_PARAM",
				"필수 요청 파라미터가 없습니다.",
				request.getRequestURI());
		return ResponseEntity.badRequest()
				.body(response);
	}

}
