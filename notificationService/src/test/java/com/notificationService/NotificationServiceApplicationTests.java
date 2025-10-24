package com.notificationService;

import com.notificationService.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
//@SpringBootTest
//@ActiveProfiles("test")
//class NotificationServiceApplicationTests {
//	@MockBean
//	private KafkaTemplate<?, ?> kafkaTemplate;
//
//	@MockBean
//	private ProducerFactory<?, ?> producerFactory;
//	@Test
//	void contextLoads() {
//	}
//
//}
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@EnableAutoConfiguration(exclude = {
//		DataSourceAutoConfiguration.class,
//		HibernateJpaAutoConfiguration.class,
//		EurekaClientAutoConfiguration.class,
//		KafkaAutoConfiguration.class
//})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		KafkaAutoConfiguration.class
})
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

	@MockBean
	private NotificationRepository notificationRepository;

	@Test
	void contextLoads() {

	}
}