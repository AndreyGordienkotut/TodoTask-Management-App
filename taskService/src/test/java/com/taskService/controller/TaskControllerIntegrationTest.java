package com.taskService.controller;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.taskService.TaskServiceApplication;
import com.taskService.config.TestSecurityConfig;
import com.taskService.dto.TaskRequestDto;
import com.taskService.dto.UpdatePriorityRequestDto;
import com.taskService.dto.UpdateStatusRequestDto;
import com.taskService.dto.UpdateTaskRequestDto;
import com.taskService.model.Frequency_repeat;
import com.taskService.model.Priority;
import com.taskService.model.Task;
import com.taskService.model.Status;
import com.taskService.repository.TaskRepository;
import com.taskService.service.TaskService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.LongStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.Matchers.is;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest(classes = TaskServiceApplication.class,properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestSecurityConfig.class)
@EnableAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class,
        EurekaClientAutoConfiguration.class
})
public class TaskControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TaskRepository taskRepository;
    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;
    @MockBean
    private ProducerFactory<?, ?> producerFactory;
    private Long preloadedTaskId;
    private final Long USER_ID_1 = 1L;
    private final Long USER_ID_2 = 2L;
    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14.8-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("application.security.jwt.secret-key", () -> "12345t7vPfsdf42f8yQ9z3a5b6c7d8eF0g1H2i3J4k5L6m7N8o9p0q1R2s3T4u5V6w7X8y9Z0A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7R8S9T0U1V2W3X4Y5Z");
        registry.add("application.security.jwt.expiration", () -> "3600000");
        registry.add("application.security.jwt.refresh-token.expiration", () -> "604800000");
        registry.add("application.security.jwt.cookie-name", () -> "jwt_token");

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");

        registry.add("KAFKA_BOOTSTRAP_SERVERS", () -> "localhost:9092");
    }
    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }
    //Creates a parent task and several child tasks, forming a series for testing
    private Task createRepeatSeries(Long userId, int count, Status initialStatus) {
        Task parentTask = Task.builder()
                .title("Parent Repeating Task")
                .description("Series master task")
                .priority(Priority.HIGH)
                .status(initialStatus)
                .userId(userId)
                .date(LocalDateTime.now())
                .isRepeat(true)
                .frequencyRepeat(Frequency_repeat.WEEK)
                .parentTaskId(null)
                .build();
        parentTask = taskRepository.save(parentTask);
        final Long parentId = parentTask.getId();
        LongStream.rangeClosed(1, count)
                .mapToObj(i -> Task.builder()
                        .title("Child Task " + i)
                        .description("Part of the series")
                        .priority(Priority.MEDIUM)
                        .status(initialStatus)
                        .userId(userId)
                        .date(LocalDateTime.now().plusDays(i * 7L))
                        .isRepeat(true)
                        .frequencyRepeat(Frequency_repeat.WEEK)
                        .parentTaskId(parentId)
                        .build())
                .forEach(taskRepository::save);

        return parentTask;
    }

    @Test
    @DisplayName("Success - getAllTasks")
    void successGetAllTasks() throws Exception {
        Task task = Task.builder()
                .title("Test Task")
                .description("Integration test description")
                .priority(Priority.HIGH)
                .status(Status.COMPLETED)
                .userId(1L)
                .date(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Task"))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    @Test
    @DisplayName("400 wrong sort - getAllTasks")
    void wrongSortGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("sort", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "1"))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("Success - searchTasks")
    void successSearchTasks() throws Exception {
        Task task = Task.builder()
                .title("Test Task")
                .description("Integration test description")
                .priority(Priority.HIGH)
                .status(Status.COMPLETED)
                .userId(1L)
                .date(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();
        taskRepository.save(task);
        mockMvc.perform(get("/api/tasks/search")
                        .param("keyword", "Test")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Task"))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    @Test
    @DisplayName("200 not found - searchTasks")
    void notFoundSearchTasks() throws Exception {
        Task task = Task.builder()
                .title("Test Task")
                .description("Integration test description")
                .priority(Priority.HIGH)
                .status(Status.COMPLETED)
                .userId(1L)
                .date(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();
        taskRepository.save(task);
        mockMvc.perform(get("/api/tasks/search")
                        .param("keyword", "Tttttest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
    @Test
    @DisplayName("Success - getTaskById")
    void successGetTaskById() throws Exception {
        Task task = Task.builder()
                .title("Test Task")
                .description("Integration test description")
                .priority(Priority.HIGH)
                .status(Status.COMPLETED)
                .userId(1L)
                .date(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();
        taskRepository.save(task);
        mockMvc.perform(get("/api/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "1"))
                .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Test Task"));
    }
    @Test
    @DisplayName("404 not found - getTaskById")
    void notFoundGetTaskById() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "1"))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("403 foreign task - getTaskById")
    void foreignTaskGetTaskById() throws Exception {
        Task task = Task.builder()
                .title("Test Task")
                .description("Integration test description")
                .priority(Priority.HIGH)
                .status(Status.COMPLETED)
                .userId(2L)
                .date(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();
        taskRepository.save(task);
        mockMvc.perform(get("/api/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "1"))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName("Success - filterTask by date range")
    void successFilterTaskByDate() throws Exception {
        Task task = Task.builder()
                .title("Test Task")
                .description("Integration test description")
                .priority(Priority.HIGH)
                .status(Status.COMPLETED)
                .userId(1L)
                .date(LocalDateTime.of(2025, 11, 12, 10, 0))
                .dueDate(LocalDateTime.of(2025, 11, 14, 10, 0))
                .build();
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/filter")
                        .principal(() -> "1")
                        .param("fromDate", "2025-11-11")
                        .param("toDate", "2025-11-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Success - filterTask by status")
    void successFilterTaskByStatus() throws Exception {
        Task task = Task.builder()
                .title("Status Task")
                .description("Integration test description")
                .priority(Priority.LOW)
                .status(Status.COMPLETED)
                .userId(1L)
                .date(LocalDateTime.now())
                .build();
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/filter")
                        .principal(() -> "1")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Status Task"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Success - filterTask by priority")
    void successFilterTaskByPriority() throws Exception {
        Task task = Task.builder()
                .title("Priority Task")
                .description("Integration test description")
                .priority(Priority.MEDIUM)
                .status(Status.NOT_COMPLETED)
                .userId(1L)
                .date(LocalDateTime.now())
                .build();
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/filter")
                        .principal(() -> "1")
                        .param("priority", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Priority Task"))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"));
    }
    @Test
    @DisplayName("Success - createTask")
    void successCreateTask() throws Exception {
        TaskRequestDto taskRequestDto = TaskRequestDto.builder()
                .title("Test Task")
                .description("Integration test description")
                .priority(Priority.HIGH)
                .date(LocalDateTime.now())
                .status(Status.COMPLETED)
                .build();

        mockMvc.perform(post("/api/tasks")
                .principal(() -> "1")
        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequestDto)))
                .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Test Task"))
        .andExpect(jsonPath("$.description").value("Integration test description"));

    }
    @Test
    @DisplayName("400 - createTask empty title")
    void emptyTitleCreateTask() throws Exception {
        TaskRequestDto taskRequestDto = TaskRequestDto.builder()

                .description("Integration test description")
                .priority(Priority.HIGH)
                .status(Status.COMPLETED)
                .build();

        mockMvc.perform(post("/api/tasks")
                        .principal(() -> "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequestDto)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("Success - updateTask")
    void successUpdateTask() throws Exception {
        Task task = Task.builder()
                .title("Priority Task")
                .description("Integration test description")
                .priority(Priority.MEDIUM)
                .status(Status.NOT_COMPLETED)
                .userId(1L)
                .date(LocalDateTime.now())
                .build();
        taskRepository.save(task);

        UpdateTaskRequestDto updateDto = UpdateTaskRequestDto.builder()
                .title("Update Task")
                .description("Update Integration test description")
                .date(LocalDateTime.now())
                .build();
        mockMvc.perform(patch("/api/tasks/{1}",task.getId()) .principal(() -> "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
    .andExpect(jsonPath("$.title").value("Update Task"))
                .andExpect(jsonPath("$.description").value("Update Integration test description"));
    }
    @Test
    @DisplayName("404 - updateTask not found")
    void updateTaskNotFound() throws Exception {
        UpdateTaskRequestDto updateDto = UpdateTaskRequestDto.builder()
                .title("Updated Task")
                .build();
        mockMvc.perform(patch("/api/tasks/{id}", 99L)
                        .principal(() -> "1")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("403 - updateTask foreign user")
    void updateTaskForeignUser() throws Exception {
        Task task = Task.builder()
                .title("Priority Task")
                .description("Integration test description")
                .priority(Priority.MEDIUM)
                .status(Status.NOT_COMPLETED)
                .userId(2L)
                .date(LocalDateTime.now())
                .build();
        taskRepository.save(task);

        UpdateTaskRequestDto updateDto = UpdateTaskRequestDto.builder()
                .title("Update Task")
                .description("Update Integration test description")
                .date(LocalDateTime.now())
                .build();
        mockMvc.perform(patch("/api/tasks/{1}",task.getId()) .principal(() -> "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName("Success - updateStatus")
    void updateStatusSuccess() throws Exception {
        Task task = Task.builder()
                .title("Priority Task")
                .description("Integration test description")
                .priority(Priority.MEDIUM)
                .status(Status.NOT_COMPLETED)
                .userId(1L)
                .date(LocalDateTime.now())
                .build();
        taskRepository.save(task);
        UpdateStatusRequestDto dto = new UpdateStatusRequestDto(Status.COMPLETED);

        mockMvc.perform(patch("/api/tasks/{id}/status", task.getId())
                        .principal(() -> "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    @Test
    @DisplayName("Success - updatePriority")
    void updatePrioritySuccess() throws Exception {
        Task task = Task.builder()
                .title("Priority Task")
                .description("Integration test description")
                .priority(Priority.MEDIUM)
                .status(Status.NOT_COMPLETED)
                .userId(1L)
                .date(LocalDateTime.now())
                .build();
        taskRepository.save(task);
        UpdatePriorityRequestDto dto = new UpdatePriorityRequestDto(Priority.HIGH);

        mockMvc.perform(patch("/api/tasks/{id}/priority", task.getId())
                        .principal(() -> "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }
    @Test
    @DisplayName("Success - archiveTask")
    void archiveTaskSuccess() throws Exception {
        Task task = Task.builder()
                .title("Priority Task")
                .description("Integration test description")
                .priority(Priority.MEDIUM)
                .status(Status.COMPLETED)
                .userId(1L)
                .date(LocalDateTime.now())
                .isRepeat(false)
                .build();
        taskRepository.save(task);
        mockMvc.perform(delete("/api/tasks/{id}/archive", task.getId())
                        .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }
    @Test
    @DisplayName("409 - archiveTask - Attempt on Repeating Task)")
    void archiveTaskIllegalState() throws Exception {
        // Создаем повторяющуюся задачу, которую нельзя архивировать как одиночную
        Task task = createRepeatSeries(USER_ID_1, 0, Status.NOT_COMPLETED);

        mockMvc.perform(delete("/api/tasks/{id}/archive", task.getId())
                        .principal(() -> USER_ID_1.toString()))
                .andExpect(status().isConflict());
    }
    @Test
    @DisplayName("403 - archiveTask foreign user")
    void archiveTaskForeignUser() throws Exception {
        Task task = Task.builder()
                .title("Priority Task")
                .description("Integration test description")
                .priority(Priority.MEDIUM)
                .status(Status.COMPLETED)
                .userId(99L)
                .date(LocalDateTime.now())
                .isRepeat(false)
                .build();
        taskRepository.save(task);
        mockMvc.perform(delete("/api/tasks/{id}/archive", task.getId())
                        .principal(() -> "1"))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName("Success - archiveSeries")
    void archiveSeriesSuccess() throws Exception {
        Task parentTask = createRepeatSeries(USER_ID_1, 2, Status.NOT_COMPLETED);

        mockMvc.perform(delete("/api/tasks/{id}/series/archive", parentTask.getId())
                        .principal(() -> USER_ID_1.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].status").value("ARCHIVED"))
                .andExpect(jsonPath("$[1].status").value("ARCHIVED"));

        List<Task> updatedTasks = taskRepository.findAll();
        assertThat(updatedTasks).hasSize(3);
        assertThat(updatedTasks.stream().allMatch(t -> t.getStatus() == Status.ARCHIVED)).isTrue();
    }

    @Test
    @DisplayName("403 - archiveSeries foreign user")
    void archiveSeriesForeignUser() throws Exception {
        Task parentTask = createRepeatSeries(USER_ID_2, 1, Status.NOT_COMPLETED);

        mockMvc.perform(delete("/api/tasks/{id}/series/archive", parentTask.getId())
                        .principal(() -> USER_ID_1.toString()))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName("Success - getArchivedTasks")
    void successGetArchivedTasks() throws Exception {
        Task task = Task.builder()
                .title("Test Task")
                .description("Integration test description")
                .priority(Priority.HIGH)
                .status(Status.ARCHIVED)
                .userId(1L)
                .date(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();
        taskRepository.save(task);
        mockMvc.perform(get("/api/tasks/archived")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Task"))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    @Test
    @DisplayName("200 not found - getArchivedTasks")
    void notFoundGetArchivedTasks() throws Exception {
        mockMvc.perform(get("/api/tasks/archived")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
    @Test
    @DisplayName("200 - deleteTask")
    void successDeleteTask() throws Exception {
        Task task = Task.builder()
                .title("Test Task")
                .description("Integration test description")
                .priority(Priority.HIGH)
                .status(Status.ARCHIVED)
                .userId(1L)
                .date(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();
        taskRepository.save(task);
        mockMvc.perform(delete("/api/tasks/{id}/permanent", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> "1"))
                .andExpect(status().isNoContent());
    }
    @Test
    @DisplayName("404 - deleteTask no found")
    void noFoundDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}/permanent", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "1"))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("Success - deleteTasks")
    void successDeleteTasks() throws Exception {
        List<Long> ids = List.of(1L,2L);
        mockMvc.perform(delete("/api/tasks/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> "1")
                .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isNoContent());
    }
    @Test
    @DisplayName("204 - deleteArchivedTaskSeries")
    void successDeleteArchivedTaskSeries() throws Exception {
        Task parentTask = createRepeatSeries(USER_ID_1, 2, Status.ARCHIVED);
        Long seriesId = parentTask.getId();

        mockMvc.perform(delete("/api/tasks/{id}/series/permanent", seriesId)
                        .principal(() -> USER_ID_1.toString()))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findRepeatGroupTasks(seriesId, USER_ID_1)).isEmpty();
        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("409 - deleteArchivedTaskSeries )")
    void illegalStateDeleteArchivedTaskSeries() throws Exception {
        Task parentTask = createRepeatSeries(USER_ID_1, 2, Status.NOT_COMPLETED);
        Long seriesId = parentTask.getId();

        mockMvc.perform(delete("/api/tasks/{id}/series/permanent", seriesId)
                        .principal(() -> USER_ID_1.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("403 - deleteArchivedTaskSeries foreign user")
    void deleteArchivedTaskSeriesForeignUser() throws Exception {
        Task parentTask = createRepeatSeries(USER_ID_2, 1, Status.ARCHIVED);
        Long seriesId = parentTask.getId();

        mockMvc.perform(delete("/api/tasks/{id}/series/permanent", seriesId)
                        .principal(() -> USER_ID_1.toString()))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName("400 - deleteTasksBulk - empty list")
    void illegalArgumentDeleteTasksBulk() throws Exception {
        mockMvc.perform(delete("/api/tasks/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> USER_ID_1.toString())
                        .content(objectMapper.writeValueAsString(List.of())))
                .andExpect(status().isBadRequest());
    }

}