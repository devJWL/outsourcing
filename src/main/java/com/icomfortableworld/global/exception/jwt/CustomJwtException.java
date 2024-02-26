package com.icomfortableworld.global.exception.jwt;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CustomJwtException extends RuntimeException {
	private final HttpStatus status;
	private final String message;

	public CustomJwtException(final JwtErrorCode code) {
		this.status = HttpStatus.UNAUTHORIZED;
		this.message = code.getMessage();
	}

	@Override
	public String getMessage() {
		return String.format("[JWT ERROR] %s", message);
	}
}
