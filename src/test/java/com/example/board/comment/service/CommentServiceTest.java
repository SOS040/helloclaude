package com.example.board.comment.service;

import java.util.List;
import java.util.Optional;

import com.example.board.comment.domain.Comment;
import com.example.board.comment.dto.CommentCreateRequest;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.comment.dto.CommentUpdateRequest;
import com.example.board.comment.repository.CommentRepository;
import com.example.board.global.common.PageResponse;
import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import com.example.board.post.domain.Post;
import com.example.board.post.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private PostService postService;

	@InjectMocks
	private CommentService commentService;

	private final Post post = Post.create("제목", "내용", "작성자");

	@Test
	void create() {
		given(postService.findPostById(1L)).willReturn(post);
		given(commentRepository.save(any(Comment.class))).willAnswer((invocation) -> invocation.getArgument(0));

		CommentResponse response = commentService.create(1L, new CommentCreateRequest("댓글", "댓글러"));

		assertThat(response.content()).isEqualTo("댓글");
		assertThat(response.author()).isEqualTo("댓글러");
	}

	@Test
	void createWhenPostNotFoundThrows() {
		given(postService.findPostById(1L)).willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

		assertThatThrownBy(() -> commentService.create(1L, new CommentCreateRequest("댓글", "댓글러")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.POST_NOT_FOUND);
		then(commentRepository).should(never()).save(any(Comment.class));
	}

	@Test
	void getComments() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
		given(postService.findPostById(1L)).willReturn(post);
		given(commentRepository.findByPostId(1L, pageable))
			.willReturn(new PageImpl<>(List.of(Comment.create(post, "댓글", "댓글러")), pageable, 1));

		PageResponse<CommentResponse> response = commentService.getComments(1L, pageable);

		assertThat(response.content()).extracting(CommentResponse::content).containsExactly("댓글");
	}

	@Test
	void getCommentsWithUnsupportedSortPropertyThrows() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by("content"));

		assertThatThrownBy(() -> commentService.getComments(1L, pageable)).isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_SORT_PROPERTY);
	}

	@Test
	void update() {
		given(commentRepository.findByIdAndPostId(10L, 1L))
			.willReturn(Optional.of(Comment.create(post, "댓글", "댓글러")));

		CommentResponse response = commentService.update(1L, 10L, new CommentUpdateRequest("수정된 댓글"));

		assertThat(response.content()).isEqualTo("수정된 댓글");
	}

	@Test
	void updateWhenCommentNotInPostThrows() {
		given(commentRepository.findByIdAndPostId(10L, 2L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> commentService.update(2L, 10L, new CommentUpdateRequest("수정")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
	}

	@Test
	void delete() {
		Comment comment = Comment.create(post, "댓글", "댓글러");
		given(commentRepository.findByIdAndPostId(10L, 1L)).willReturn(Optional.of(comment));

		commentService.delete(1L, 10L);

		then(commentRepository).should().delete(comment);
	}

}
