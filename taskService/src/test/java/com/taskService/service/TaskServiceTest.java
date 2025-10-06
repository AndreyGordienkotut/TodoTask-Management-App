package com.taskService.service;

import com.taskService.dto.*;
import com.taskService.exception.ResourceNotFoundException;
import com.taskService.model.Frequency_repeat;
import com.taskService.model.Priority;
import com.taskService.model.Task;
import com.taskService.model.Status;
import com.taskService.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskRequestDto taskRequestDto;
    private Pageable pageable;
    private List<Task> taskList;
    private Long userId;
    private Task repeatingTask;
    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setUserId(1L);
        task.setTitle("Test task");
        task.setDescription("Test description");
        task.setStatus(Status.COMPLETED);
        taskRequestDto= new TaskRequestDto();
        taskRequestDto.setTitle("Test task2");
        taskRequestDto.setDescription("Test description2");
        taskRequestDto.setStatus(Status.COMPLETED);
        taskRequestDto.setPriority(Priority.HIGH);
        //pageable
        userId = 1L;
        pageable = PageRequest.of(0, 10);

        Task task1 = Task.builder()
                .id(1L).userId(userId).title("Task A").description("Desc A")
                .date(LocalDateTime.now()).dueDate(LocalDateTime.now().plusDays(1))
                .status(Status.NOT_COMPLETED).priority(Priority.HIGH).build();

        Task task2 = Task.builder()
                .id(2L).userId(userId).title("Task B").description("Desc B")
                .date(LocalDateTime.now()).dueDate(LocalDateTime.now().plusDays(2))
                .status(Status.COMPLETED).priority(Priority.MEDIUM).build();

        taskList = List.of(task1, task2);
        repeatingTask = Task.builder()
                .id(10L)
                .userId(userId)
                .title("Repeating Task")
                .status(Status.ARCHIVED)
                .isRepeat(true)
                .frequencyRepeat(Frequency_repeat.DAY)
                .parentTaskId(null)
                .build();
    }

    @Test
    @DisplayName("Success - getTaskById")
    void successGetTaskById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponseDto result = taskService.getTaskById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test task", result.getTitle());

        verify(taskRepository, times(1)).findById(1L);
    }
    @Test
    @DisplayName("Task not found - getTaskById")
    void taskNotFoundGetTaskById() {
        when(taskRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(9L, 1L));
        verify(taskRepository, times(1)).findById(9L);
    }
    @Test
    @DisplayName("AccessDeniedException - getTaskById")
    void notHavePermissionGetTaskById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(1L, 2L));
        verify(taskRepository, times(1)).findById(1L);
    }
    @Test
    @DisplayName("Success - getAllTasks")
    void successGetAllTasks() {
        Page<Task> taskPage = new PageImpl<>(taskList, pageable, taskList.size());
        when(taskRepository.findAllByUserId(userId,pageable)).thenReturn(taskPage);

        Page<TaskResponseDto> result = taskService.getAllTasks(userId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Task A");
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(taskRepository, times(1)).findAllByUserId(userId,pageable);
    }
    @Test
    @DisplayName("Success - searchTasks")
    void searchTasks_Success() {
        String keyword = "Task A";
        Page<Task> taskPage = new PageImpl<>(List.of(taskList.get(0)), pageable, 1);
        when(taskRepository.searchByKeyword(userId, keyword, pageable)).thenReturn(taskPage);

        Page<TaskResponseDto> result = taskService.searchTasks(userId, keyword, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Task A");

        verify(taskRepository, times(1)).searchByKeyword(userId, keyword, pageable);
    }
    @Test
    @DisplayName("Success - createTask")
    void successCreateTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        var result = taskService.createTask(taskRequestDto,userId);
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(task.getTitle());
        assertThat(result.getDescription()).isEqualTo(task.getDescription());
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    @Test
    @DisplayName("Success - updateTask")
    void successUpdateTask(){
        UpdateTaskRequestDto updatedTaskRequestDto = new UpdateTaskRequestDto();
        updatedTaskRequestDto.setTitle("Updated Title");
        updatedTaskRequestDto.setDescription("Updated Description");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        var result = taskService.updateTask(1L,updatedTaskRequestDto,1L);
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(updatedTaskRequestDto.getTitle());
        assertThat(result.getDescription()).isEqualTo(updatedTaskRequestDto.getDescription());
        verify(taskRepository,times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    @Test
    @DisplayName("ResourceNotFoundException - updateTask")
    void resourceNotFoundUpdateTask(){
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1L,new UpdateTaskRequestDto(),1L));
        verify(taskRepository,never()).save(any(Task.class));
    }
    @Test
    @DisplayName("AccessDeniedException - updateTask")
    void accessDeniedExceptionTask(){
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThrows(AccessDeniedException.class, () -> taskService.updateTask(1L,new UpdateTaskRequestDto(),2L));
        verify(taskRepository, never()).save(any(Task.class));
    }
    @Test
    @DisplayName("Success - updateStatus")
    void successUpdateStatus(){
        UpdateStatusRequestDto updatedStatusRequestDto = new UpdateStatusRequestDto();
        updatedStatusRequestDto.setStatus(Status.ARCHIVED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        var result = taskService.updateStatus(1L,updatedStatusRequestDto,1L);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(updatedStatusRequestDto.getStatus());

        verify(taskRepository,times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    @Test
    @DisplayName("ResourceNotFoundException - updateStatus")
    void resourceNotFoundUpdateStatus(){
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.updateStatus(1L,new UpdateStatusRequestDto(),1L));
        verify(taskRepository,never()).save(any(Task.class));
    }
    @Test
    @DisplayName("AccessDeniedException - updateStatus")
    void accessDeniedExceptionStatus(){
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThrows(AccessDeniedException.class, () -> taskService.updateStatus(1L,new UpdateStatusRequestDto(),2L));
        verify(taskRepository, never()).save(any(Task.class));
    }
    @Test
    @DisplayName("Success - updatePriority")
    void successUpdatePriority() {
        UpdatePriorityRequestDto dto = new UpdatePriorityRequestDto();
        dto.setPriority(Priority.HIGH);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        var result = taskService.updatePriority(1L, dto, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("ResourceNotFoundException - updatePriority")
    void resourceNotFoundUpdatePriority() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.updatePriority(1L, new UpdatePriorityRequestDto(), 1L));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("AccessDeniedException - updatePriority")
    void accessDeniedUpdatePriority() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThrows(AccessDeniedException.class, () -> taskService.updatePriority(1L, new UpdatePriorityRequestDto(), 2L));
        verify(taskRepository, never()).save(any(Task.class));
    }


    @Test
    @DisplayName("Success - archiveTask")
    void successArchiveTask(){
        UpdateStatusRequestDto updatedStatusRequestDto = new UpdateStatusRequestDto();
        updatedStatusRequestDto.setStatus(Status.ARCHIVED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        var result = taskService.archiveTask(1L,1L);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(updatedStatusRequestDto.getStatus());

        verify(taskRepository,times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    @Test
    @DisplayName("IllegalStateException - archiveTask (Is Repeating)")
    void illegalStateArchiveTask_IsRepeating() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(repeatingTask));

        assertThrows(IllegalStateException.class, () -> taskService.archiveTask(10L, userId));

        verify(taskRepository, never()).save(any(Task.class));
    }
    @Test
    @DisplayName("ResourceNotFoundException - archiveTask")
    void resourceNotFoundArchiveTask(){
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.archiveTask(1L,1L));
        verify(taskRepository,never()).save(any(Task.class));
    }
    @Test
    @DisplayName("SecurityException - archiveTask")
    void securityExceptionArchiveTask(){
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThrows(SecurityException.class, () -> taskService.archiveTask(1L,2L));
        verify(taskRepository, never()).save(any(Task.class));
    }
    @Test
    @DisplayName("Success - getArchivedTasks")
    void successGetArchivedTasks() {
        Page<Task> taskPage = new PageImpl<>(List.of(task), pageable, 1);
        when(taskRepository.findAllByUserIdAndStatus(userId, Status.ARCHIVED, pageable)).thenReturn(taskPage);

        Page<TaskResponseDto> result = taskService.getArchivedTasks(userId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(task.getId());

        verify(taskRepository, times(1)).findAllByUserIdAndStatus(userId, Status.ARCHIVED, pageable);
    }
    @Test
    @DisplayName("Success - archiveTaskSeries")
    void successArchiveTaskSeries() {
        Task child1 = Task.builder().id(11L).userId(userId).status(Status.ARCHIVED).isRepeat(true).parentTaskId(10L).build();
        Task child2 = Task.builder().id(12L).userId(userId).status(Status.ARCHIVED).isRepeat(true).parentTaskId(10L).build();
        List<Task> series = List.of(repeatingTask, child1, child2);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(repeatingTask));
        when(taskRepository.findRepeatGroupTasks(10L, userId)).thenReturn(series);
        when(taskRepository.saveAll(anyList())).thenReturn(series.stream()
                .peek(t -> t.setStatus(Status.ARCHIVED))
                .collect(Collectors.toList()));

        List<TaskResponseDto> results = taskService.archiveTaskSeries(10L, userId);

        assertThat(results).hasSize(3);
        assertThat(results.stream().allMatch(r -> r.getStatus() == Status.ARCHIVED)).isTrue();
        verify(taskRepository, times(1)).saveAll(anyList());
    }
    @Test
    @DisplayName("ResourceNotFoundException - archiveTaskSeries - initial task not found")
    void resourceNotFoundArchiveTaskSeries() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.archiveTaskSeries(99L, userId));
        verify(taskRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("SecurityException - archiveTaskSeries - initial task access denied")
    void securityExceptionArchiveTaskSeries() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(repeatingTask));
        assertThrows(SecurityException.class, () -> taskService.archiveTaskSeries(10L, 99L));
        verify(taskRepository, never()).saveAll(anyList());
    }
    @Test
    @DisplayName("Success - deleteTask")
    void successDeleteTask() {
        task.setStatus(Status.ARCHIVED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        taskService.deleteTask(1L,1L);
        verify(taskRepository, times(1)).delete(any(Task.class));
    }
    @Test
    @DisplayName("ResourceNotFoundException - deleteTask")
    void resourceNotFoundDeleteTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(1L,1L));
        verify(taskRepository, never()).delete(any(Task.class));
    }
    @Test
    @DisplayName("AccessDeniedException - deleteTask")
    void accessDeniedDeleteTask() {
        task.setStatus(Status.ARCHIVED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(1L,2L));
        verify(taskRepository, never()).delete(any(Task.class));
    }
    @Test
    @DisplayName("IllegalStateException - deleteTask - not ARCHIVED)")
    void illegalStateDeleteTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThrows(IllegalStateException.class, () -> taskService.deleteTask(1L,1L));
        verify(taskRepository, never()).delete(any(Task.class));
    }
    @Test
    @DisplayName("IllegalStateException - deleteTask - is Repeating")
    void illegalStateDeleteTask_IsRepeat() {
        repeatingTask.setStatus(Status.ARCHIVED);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(repeatingTask));
        assertThrows(IllegalStateException.class, () -> taskService.deleteTask(10L, userId));
        verify(taskRepository, never()).delete(any(Task.class));
    }
    @Test
    @DisplayName("Success - deleteRepeatTaskSeries")
    void successDeleteRepeatTaskSeries() {
        Task parentArchived = Task.builder().id(10L).userId(userId).status(Status.ARCHIVED).isRepeat(true).parentTaskId(null).build();
        Task childArchived = Task.builder().id(11L).userId(userId).status(Status.ARCHIVED).isRepeat(true).parentTaskId(10L).build();
        List<Task> archivedSeries = List.of(parentArchived, childArchived);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(parentArchived));
        when(taskRepository.findRepeatGroupTasks(10L, userId)).thenReturn(archivedSeries);

        taskService.deleteRepeatTaskSeries(10L, userId);

        verify(taskRepository, times(1)).deleteAllInBatch(archivedSeries);
    }
    @Test
    @DisplayName("IllegalStateException - deleteRepeatTaskSeries - not all ARCHIVED)")
    void illegalStateDeleteRepeatTaskSeries_NotAllArchived() {
        Task parentArchived = Task.builder().id(10L).userId(userId).status(Status.ARCHIVED).isRepeat(true).parentTaskId(null).build();
        Task childActive = Task.builder().id(11L).userId(userId).status(Status.NOT_COMPLETED).isRepeat(true).parentTaskId(10L).build();
        List<Task> mixedSeries = List.of(parentArchived, childActive);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(parentArchived));
        when(taskRepository.findRepeatGroupTasks(10L, userId)).thenReturn(mixedSeries);
        assertThrows(IllegalStateException.class, () -> taskService.deleteRepeatTaskSeries(10L, userId));
        verify(taskRepository, never()).deleteAllInBatch(anyList());
    }
    @Test
    @DisplayName("AccessDeniedException - deleteRepeatTaskSeries")
    void accessDeniedDeleteRepeatTaskSeries() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(repeatingTask));
        assertThrows(AccessDeniedException.class, () -> taskService.deleteRepeatTaskSeries(10L, 99L));
        verify(taskRepository, never()).deleteAllInBatch(anyList());
    }
    @Test
    @DisplayName("Success - deleteTasksBulk")
    void successDeleteTasksBulk() {
        task.setStatus(Status.ARCHIVED);
        task.setUserId(userId);
        when(taskRepository.findAllById(List.of(1L))).thenReturn(List.of(task));

        taskService.deleteTasksBulk(List.of(1L), userId);

        verify(taskRepository, times(1)).deleteAllInBatch(List.of(task));
    }
    @Test
    @DisplayName("IllegalArgumentException - deleteTasksBulk empty list")
    void illegalArgumentDeleteTasksBulk() {
        assertThrows(IllegalArgumentException.class, () -> taskService.deleteTasksBulk(List.of(), userId));
        verify(taskRepository, never()).deleteAllInBatch(any());
    }

    @Test
    @DisplayName("SecurityException - deleteTasksBulk other user")
    void securityExceptionDeleteTasksBulk() {
        task.setStatus(Status.ARCHIVED);
        task.setUserId(2L);
        when(taskRepository.findAllById(List.of(1L))).thenReturn(List.of(task));

        assertThrows(SecurityException.class, () -> taskService.deleteTasksBulk(List.of(1L), userId));
        verify(taskRepository, never()).deleteAllInBatch(any());
    }

    @Test
    @DisplayName("IllegalStateException - deleteTasksBulk not archived")
    void illegalStateDeleteTasksBulk() {
        task.setStatus(Status.COMPLETED);
        task.setUserId(userId);
        when(taskRepository.findAllById(List.of(1L))).thenReturn(List.of(task));

        assertThrows(IllegalStateException.class, () -> taskService.deleteTasksBulk(List.of(1L), userId));
        verify(taskRepository, never()).deleteAllInBatch(any());
    }
    @Test
    @DisplayName("Success - filterTasks by status and priority")
    void successFilterTasks() {
        task.setStatus(Status.COMPLETED);
        task.setPriority(Priority.HIGH);
        task.setUserId(userId);
        when(taskRepository.findAll(any(Specification.class))).thenReturn(List.of(task));
        var result = taskService.filterTasks(userId,Status.COMPLETED,null,null,Priority.HIGH);
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(taskRepository, times(1)).findAll(any(Specification.class));
    }
    @Test
    @DisplayName("Success - filterTasks by date range")
    void successFilterTasksByDateRange() {
        task.setDate(LocalDateTime.of(2023, 1, 5, 10, 0));
        task.setUserId(userId);

        when(taskRepository.findAll(any(Specification.class))).thenReturn(List.of(task));

        var result = taskService.filterTasks(
                userId,
                null,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 10),
                null
        );

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(taskRepository, times(1)).findAll(any(Specification.class));
    }



}

