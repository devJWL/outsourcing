package com.icomfortableworld.domain.comment.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.icomfortableworld.domain.comment.dto.CommentRequestUpdateDto;
import com.icomfortableworld.domain.comment.entity.Comment;
import com.icomfortableworld.domain.comment.exception.CommentErrorCode;
import com.icomfortableworld.domain.comment.exception.CustomCommentException;
import com.icomfortableworld.domain.comment.model.CommentModel;
import com.icomfortableworld.domain.feed.repository.FeedRepository;
import com.icomfortableworld.domain.member.repository.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor

public class CommentRepositoryImpl implements CommentRepository {
	private final CommentJpaRepository commentJpaRepository;
	private final FeedRepository feedRepository;
	private final MemberRepository memberRepository;
	private List<Comment> comments = new ArrayList<>();

	@Override
	public CommentModel save(Comment comment) {

		return commentJpaRepository.save(comment).toModel();
	}
	public void deleteByFeedId(Long feedId) {
		commentJpaRepository.deleteAllByFeedId(feedId);
	}

	@Override
	public List<CommentModel> findByFeedId(Long feedId) {
		return commentJpaRepository.findByFeedId(feedId);
	}

	@Override
	public CommentModel update(Long commentId, CommentRequestUpdateDto commentRequestUpdateDto) {

		Comment comment = commentJpaRepository.findById(commentId).get();
		comment.update(commentRequestUpdateDto);
		return comment.toModel();
	}

	@Override
	public Optional<CommentModel> findById(Long commentId) {
		return commentJpaRepository.findById(commentId).map(Comment::toModel);
	}

	@Override
	public CommentModel findByIdOrElseThrow(Long commentId) {
		return findById(commentId).orElseThrow(
			() -> new CustomCommentException(CommentErrorCode.COMMNET_ERROR_CODE_NOT_FOUND_USER)
		);
	}

	// @Override
	// public CommentModel delete(Comment comment) {
	// 	return commentJpaRepository.delete(comment);
	//
	// }
}






