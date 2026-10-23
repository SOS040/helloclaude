package com.example.board.post.service;

import java.util.List;
import java.util.Optional;

import com.example.board.global.common.PageResponse;
import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import com.example.board.post.domain.Post;
import com.example.board.post.dto.PostCreateRequest;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.dto.PostSummaryResponse;
import com.example.board.post.dto.PostUpdateRequest;
import com.example.board.post.repository.PostRepository;
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
class PostServiceTest {

	@Mock
	private PostRepository postRepository;

	@InjectMocks
	private PostService postService;

	@Test
	void create() {
		given(postRepository.save(any(Post.class))).willAnswer((invocation) -> invocation.getArgument(0));

		PostResponse response = postService.create(new PostCreateRequest("제목", "내용", "작성자"));

		assertThat(response.title()).isEqualTo("제목");
		assertThat(response.content()).isEqualTo("내용");
		assertThat(response.author()).isEqualTo("작성자");
	}

	@Test
	void getPostsWithKeywordSearchesByTitle() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by("id").descending());
		given(postRepository.findByTitleContainingIgnoreCase("spring", pageable))
			.willReturn(new PageImpl<>(List.of(Post.create("Spring 4", "내용", "작성자")), pageable, 1));

		PageResponse<PostSummaryResponse> response = postService.getPosts("  spring ", pageable);

		assertThat(response.content()).extracting(PostSummaryResponse::title).containsExactly("Spring 4");
		assertThat(response.totalElements()).isEqualTo(1);
		then(postRepository).should(never()).findAll(any(Pageable.class));
	}

	@Test
	void getPostsWithoutKeywordFindsAll() {
		Pageable pageable = PageRequest.of(0, 20);
		given(postRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

		PageResponse<PostSummaryResponse> response = postService.getPosts(" ", pageable);

		assertThat(response.content()).isEmpty();
	}

	@Test
	void getPostsWithUnsupportedSortPropertyThrows() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by("content"));

		assertThatThrownBy(() -> postService.getPosts(null, pageable)).isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_SORT_PROPERTY);
	}

	@Test
	void getPostWhenNotFoundThrows() {
		given(postRepository.findById(1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> postService.getPost(1L)).isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.POST_NOT_FOUND);
	}

	@Test
	void update() {
		Post post = Post.create("제목", "내용", "작성자");
		given(postRepository.findById(1L)).willReturn(Optional.of(post));

		PostResponse response = postService.update(1L, new PostUpdateRequest("새 제목", "새 내용"));

		assertThat(response.title()).isEqualTo("새 제목");
		assertThat(response.content()).isEqualTo("새 내용");
		assertThat(response.author()).isEqualTo("작성자");
	}

	@Test
	void delete() {
		Post post = Post.create("제목", "내용", "작성자");
		given(postRepository.findById(1L)).willReturn(Optional.of(post));

		postService.delete(1L);

		then(postRepository).should().delete(post);
	}

	@Test
	void deleteWhenNotFoundThrows() {
		given(postRepository.findById(1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> postService.delete(1L)).isInstanceOf(BusinessException.class);
		then(postRepository).should(never()).delete(any(Post.class));
	}

}
