package com.teamflow.repository;

import com.teamflow.domain.Task;
import com.teamflow.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    Page<Task> findByAssigneeId(UUID assigneeId, Pageable pageable);

    Page<Task> findByProjectIdAndStatus(UUID projectId, TaskStatus status, Pageable pageable);

    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id = :projectId
          AND (:status IS NULL OR t.status = :status)
          AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
                    AND (COALESCE(:title, '') = ''
                             OR LOWER(t.title) LIKE CONCAT('%', LOWER(CAST(:title as string)), '%'))
        """)
    Page<Task> search(
        @Param("projectId") UUID projectId,
        @Param("status") TaskStatus status,
        @Param("assigneeId") UUID assigneeId,
        @Param("title") String title,
        Pageable pageable
    );

    @Query("""
        SELECT t.status, COUNT(t) FROM Task t
        WHERE t.project.id = :projectId
        GROUP BY t.status
        """)
    List<Object[]> countByStatusForProject(@Param("projectId") UUID projectId);

    @Query("""
        SELECT t.assignee.id, COUNT(t) FROM Task t
        WHERE t.project.id = :projectId AND t.assignee IS NOT NULL
        GROUP BY t.assignee.id
        """)
    List<Object[]> countByAssigneeForProject(@Param("projectId") UUID projectId);
}
