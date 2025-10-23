package com.notificationService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceApplicationTests {
	@MockBean
	private KafkaTemplate<?, ?> kafkaTemplate;

	@MockBean
	private ProducerFactory<?, ?> producerFactory;
	@Test
	void contextLoads() {
	}

}
