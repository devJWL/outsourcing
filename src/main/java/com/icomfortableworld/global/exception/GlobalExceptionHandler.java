package com.icomfortableworld.global.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.icomfortableworld.domain.feed.exception.CustomFeedException;
import com.icomfortableworld.domain.member.exception.CustomMemberException;
import com.icomfortableworld.global.exception.dto.ErrorResponseDto;
import com.icomfortableworld.jwt.exception.CustomJwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ErrorResponseDto<List<String>> methodArgumentNotValidException(
			MethodArgumentNotValidException e) {
		List<String> errorList = new ArrayList<>();
		for (FieldError error : e.getBindingResult().getFieldErrors()) {
			errorList.add(error.getDefaultMessage());
		}
		return ErrorResponseDto.of(HttpStatus.BAD_REQUEST, errorList);
	}

	@ExceptionHandler(CustomJwtException.class)
	public ErrorResponseDto<String> customJwtExceptionHandler(CustomJwtException e) {
		return ErrorResponseDto.of(HttpStatus.UNAUTHORIZED, e.getMessage());
	}

	@ExceptionHandler(CustomMemberException.class)
	public ErrorResponseDto<String> customMemberExceptionHandler(CustomMemberException e) {
		return ErrorResponseDto.of(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(CustomFeedException.class)
	public ErrorResponseDto<String> customFeedExceptionHandler(CustomFeedException e) {
		return ErrorResponseDto.of(HttpStatus.BAD_REQUEST, e.getMessage());
	}
}