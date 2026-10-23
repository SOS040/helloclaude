package com.example.board.comment.repository;

import com.example.board.comment.domain.Comment;
import com.example.board.global.config.JpaAuditingConfig;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import com.example.board.support.TestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ TestcontainersConfiguration.class, JpaAuditingConfig.class })
@ActiveProfiles("test")
class CommentRepositoryTest {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void findByPostIdReturnsOnlyCommentsOfThatPost() {
		Post post = postRepository.save(Post.create("제목", "내용", "작성자"));
		Post otherPost = postRepository.save(Post.create("다른 글", "내용", "작성자"));
		commentRepository.save(Comment.create(post, "첫 댓글", "a"));
		commentRepository.save(Comment.create(post, "둘째 댓글", "b"));
		commentRepository.save(Comment.create(otherPost, "다른 글 댓글", "c"));

		Page<Comment> page = commentRepository.findByPostId(post.getId(), PageRequest.of(0, 10, Sort.by("id")));

		assertThat(page.getContent()).extracting(Comment::getContent).containsExactly("첫 댓글", "둘째 댓글");
	}

	@Test
	void findByIdAndPostIdWithOtherPostIsEmpty() {
		Post post = postRepository.save(Post.create("제목", "내용", "작성자"));
		Post otherPost = postRepository.save(Post.create("다른 글", "내용", "작성자"));
		Comment comment = commentRepository.save(Comment.create(post, "댓글", "a"));

		assertThat(commentRepository.findByIdAndPostId(comment.getId(), post.getId())).isPresent();
		assertThat(commentRepository.findByIdAndPostId(comment.getId(), otherPost.getId())).isEmpty();
	}

	@Test
	void deletingPostCascadesToComments() {
		Post post = postRepository.save(Post.create("제목", "내용", "작성자"));
		commentRepository.save(Comment.create(post, "댓글1", "a"));
		commentRepository.save(Comment.create(post, "댓글2", "b"));
		entityManager.flush();
		entityManager.clear();

		postRepository.deleteById(post.getId());
		entityManager.flush();
		entityManager.clear();

		assertThat(commentRepository.countByPostId(post.getId())).isZero();
	}

}
