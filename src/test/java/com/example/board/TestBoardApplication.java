package com.example.board;

import com.example.board.support.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

/**
 * IDE에서 실행하면 Testcontainers로 PostgreSQL을 띄운 뒤 애플리케이션을 기동한다 (docker compose 불필요).
 */
public class TestBoardApplication {

	public static void main(String[] args) {
		SpringApplication.from(BoardApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
