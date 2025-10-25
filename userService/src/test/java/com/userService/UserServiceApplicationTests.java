package com.userService;

import com.userService.repository.EmailVerificationTokensRepository;
import com.userService.repository.RefreshTokenRepository;
import com.userService.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		KafkaAutoConfiguration.class
})
@ActiveProfiles("test")
class UserServiceApplicationTests {
	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RefreshTokenRepository refreshTokenRepository;

	@MockBean
	private EmailVerificationTokensRepository emailVerificationTokensRepository;
	@Test
	void contextLoads() {
	}

}
