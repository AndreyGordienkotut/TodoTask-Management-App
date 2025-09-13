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

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Page<Task> findAllByUserId(Long userId, Pageable pageable);
    List<Task> findAllByUserId(Long userId);
    Optional<Task> findByUserId(Long userId);
    List<Task> findAllByStatus(Status status);

    Page<Task> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);

    Page<Task> findByUserIdAndDescriptionContainingIgnoreCase(Long userId, String description, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Task> searchByKeyword(@Param("userId") Long userId,
                               @Param("keyword") String keyword,
                               Pageable pageable);
    Page<Task> findAllByUserIdAndStatusNot(Long userId, Status status, Pageable pageable);

    Page<Task> findAllByUserIdAndStatus(Long userId, Status status, Pageable pageable);
}