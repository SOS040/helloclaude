package com.example.board.comment.domain;

import com.example.board.global.entity.BaseEntity;
import com.example.board.post.domain.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "comment")
public class Comment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Post post;

	@Column(nullable = false, length = 1000)
	private String content;

	@Column(nullable = false, length = 50)
	private String author;

	private Comment(Post post, String content, String author) {
		this.post = post;
		this.content = content;
		this.author = author;
	}

	public static Comment create(Post post, String content, String author) {
		return new Comment(post, content, author);
	}

	public void update(String content) {
		this.content = content;
	}

}
