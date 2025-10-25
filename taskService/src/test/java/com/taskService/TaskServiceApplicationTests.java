package com.taskService;

import com.taskService.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

//@SpringBootTest
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		// КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: Добавляем свойства JWT
		// Эти свойства необходимы для создания JwtUtil, даже если мы не используем безопасность в тесте
		properties = {
				"application.security.jwt.secret-key=a-long-test-secret-key-for-jwt-initialization",
				"application.security.jwt.expiration=3600000",
				"application.security.jwt.refresh-token.expiration=604800000"
		}
)
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		KafkaAutoConfiguration.class
})
@ActiveProfiles("test")
class TaskServiceApplicationTests {
	@MockBean
	private TaskRepository taskRepository;

	@Test
	void contextLoads() {
	}
}
