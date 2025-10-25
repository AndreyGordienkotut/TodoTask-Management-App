package com.taskService;

import com.taskService.config.JwtAuthenticationFilter;
import com.taskService.config.JwtUtil;
import com.taskService.config.TestSecurityConfig;
import com.taskService.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		KafkaAutoConfiguration.class
})
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class TaskServiceApplicationTests {
	@MockBean
	private TaskRepository taskRepository;
	@MockBean
	private JwtUtil jwtUtil;
	@MockBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	@MockBean
	private UserDetailsService userDetailsService;
	@Test
	void contextLoads() {
	}
}
