package com.example.board.comment.dto;

import java.time.LocalDateTime;

import com.example.board.comment.domain.Comment;

public record CommentResponse(
		Long id,
		Long postId,
		String content,
		String author,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	// post는 LAZY 프록시지만 getId()는 초기화 없이 식별자를 반환한다
	public static CommentResponse from(Comment comment) {
		return new CommentResponse(comment.getId(), comment.getPost().getId(), comment.getContent(),
				comment.getAuthor(), comment.getCreatedAt(), comment.getUpdatedAt());
	}

}
