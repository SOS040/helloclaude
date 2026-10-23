package com.example.board.post.service;

import java.util.Set;

import com.example.board.global.common.PageResponse;
import com.example.board.global.common.SortValidator;
import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import com.example.board.post.domain.Post;
import com.example.board.post.dto.PostCreateRequest;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.dto.PostSummaryResponse;
import com.example.board.post.dto.PostUpdateRequest;
import com.example.board.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private static final Set<String> SORTABLE_PROPERTIES = Set.of("id", "title", "createdAt");

	private final PostRepository postRepository;

	@Transactional
	public PostResponse create(PostCreateRequest request) {
		Post post = Post.create(request.title(), request.content(), request.author());
		return PostResponse.from(postRepository.save(post));
	}

	public PageResponse<PostSummaryResponse> getPosts(String keyword, Pageable pageable) {
		SortValidator.validate(pageable.getSort(), SORTABLE_PROPERTIES);
		Page<Post> posts = StringUtils.hasText(keyword)
				? postRepository.findByTitleContainingIgnoreCase(keyword.strip(), pageable)
				: postRepository.findAll(pageable);
		return PageResponse.from(posts.map(PostSummaryResponse::from));
	}

	public PostResponse getPost(Long id) {
		return PostResponse.from(findPostById(id));
	}

	/**
	 * 다른 도메인(댓글 등)에서 게시글 엔티티가 필요할 때 사용한다.
	 */
	public Post findPostById(Long id) {
		return postRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
	}

	@Transactional
	public PostResponse update(Long id, PostUpdateRequest request) {
		Post post = findPostById(id);
		post.update(request.title(), request.content());
		// 응답에 갱신된 updatedAt을 담기 위해 커밋 전에 flush한다
		postRepository.flush();
		return PostResponse.from(post);
	}

	/**
	 * 댓글은 DB FK(on delete cascade)로 함께 삭제된다.
	 */
	@Transactional
	public void delete(Long id) {
		postRepository.delete(findPostById(id));
	}

}
