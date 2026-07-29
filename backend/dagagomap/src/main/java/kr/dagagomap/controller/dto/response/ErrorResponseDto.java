package kr.dagagomap.controller.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
public class ErrorResponseDto {

	private final LocalDateTime timestamp;
	private final String errorCode;
	private final String message;
	private final String path;

	public ErrorResponseDto(String errorCode, String message, String path) {
		this.timestamp = LocalDateTime.now();
		this.errorCode = errorCode;
		this.message = message;
		this.path = path;
	}

}
