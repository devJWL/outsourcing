package com.icomfortableworld.domain.comment.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.icomfortableworld.domain.comment.dto.CommentRequestDto;
import com.icomfortableworld.domain.comment.dto.CommentResponseDto;
import com.icomfortableworld.domain.comment.service.CommentService;
import com.icomfortableworld.jwt.security.MemberDetailsImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

RestController
@RequestMapping("/feeds/{feedId}/comments")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@PostMapping("")
	public CommentResponseDto createComment(
		@PathVariable Long feedId,
		@Valid @RequestBody CommentRequestDto commentRequestDto,
		@AuthenticationPrincipal MemberDetailsImpl memberDetails) {

		return commentService.createComment(feedId, commentRequestDto, memberDetails);
	}

	@GetMapping("")
	public List<CommentResponseDto> readComment(@PathVariable Long feedId) {

		return commentService.readComment(boardId);
	}

	@PatchMapping("/{commentId}")
	public CommentResponseDto updateComment(
		@PathVariable Long commentId,
		@Valid @RequestBody CommentRequestDto commentRequestDto,
		@AuthenticationPrincipal MemberDetailsImpl memberDetails) {

		return commentService.updateComment(commentId, commentRequestDto, memberDetails);
	}

	@DeleteMapping("/{commentId}")
	public ExceptionDto deleteComment(
		@PathVariable Long commentId,
		@AuthenticationPrincipal MemberDetailsImpl memberDetails) {

		return commentService.deleteComment(commentId, memberDetails);
	}


}

