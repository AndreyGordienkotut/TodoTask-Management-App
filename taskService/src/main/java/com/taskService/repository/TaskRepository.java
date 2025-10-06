package com.taskService.repository;

import com.taskService.model.Status;
import com.taskService.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Page<Task> findAllByUserId(Long userId, Pageable pageable);
    List<Task> findAllByStatus(Status status);
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Task> searchByKeyword(@Param("userId") Long userId,
                               @Param("keyword") String keyword,
                               Pageable pageable);
    Page<Task> findAllByUserIdAndStatusNot(Long userId, Status status, Pageable pageable);

    Page<Task> findAllByUserIdAndStatus(Long userId, Status status, Pageable pageable);

    List<Task> findByDueDateBetweenAndStatus(LocalDateTime start, LocalDateTime end, Status status);

    List<Task> findByDueDateBeforeAndStatus(LocalDateTime dateTime, Status status);
    List<Task> findByDueDateBetweenAndStatusAndNearlyOverdueNotified(
            LocalDateTime start, LocalDateTime end, Status status, boolean nearlyOverdueNotified);
    List<Task> findByDueDateBeforeAndStatusAndIsRepeat(LocalDateTime dateTime, Status status, boolean repeat);
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND (t.id = :groupId OR t.parentTaskId = :groupId)")
    List<Task> findRepeatGroupTasks(@Param("groupId") Long groupId, @Param("userId") Long userId);
}