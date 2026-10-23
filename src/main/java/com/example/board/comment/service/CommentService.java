package com.example.board.comment.service;

import java.util.Set;

import com.example.board.comment.domain.Comment;
import com.example.board.comment.dto.CommentCreateRequest;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.comment.dto.CommentUpdateRequest;
import com.example.board.comment.repository.CommentRepository;
import com.example.board.global.common.PageResponse;
import com.example.board.global.common.SortValidator;
import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import com.example.board.post.domain.Post;
import com.example.board.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

	private static final Set<String> SORTABLE_PROPERTIES = Set.of("id", "createdAt");

	private final CommentRepository commentRepository;

	private final PostService postService;

	@Transactional
	public CommentResponse create(Long postId, CommentCreateRequest request) {
		Post post = postService.findPostById(postId);
		Comment comment = Comment.create(post, request.content(), request.author());
		return CommentResponse.from(commentRepository.save(comment));
	}

	public PageResponse<CommentResponse> getComments(Long postId, Pageable pageable) {
		SortValidator.validate(pageable.getSort(), SORTABLE_PROPERTIES);
		postService.findPostById(postId);
		return PageResponse.from(commentRepository.findByPostId(postId, pageable).map(CommentResponse::from));
	}

	@Transactional
	public CommentResponse update(Long postId, Long commentId, CommentUpdateRequest request) {
		Comment comment = findComment(postId, commentId);
		comment.update(request.content());
		// 응답에 갱신된 updatedAt을 담기 위해 커밋 전에 flush한다
		commentRepository.flush();
		return CommentResponse.from(comment);
	}

	@Transactional
	public void delete(Long postId, Long commentId) {
		commentRepository.delete(findComment(postId, commentId));
	}

	private Comment findComment(Long postId, Long commentId) {
		return commentRepository.findByIdAndPostId(commentId, postId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
	}

}
