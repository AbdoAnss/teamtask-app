package com.teamflow.repository;

import com.teamflow.domain.Project;
import com.teamflow.domain.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN p.members m
        WHERE p.owner.id = :userId OR m.id = :userId
        """)
    Page<Project> findByMemberOrOwner(@Param("userId") UUID userId, Pageable pageable);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN p.members m
            WHERE (p.owner.id = :userId OR m.id = :userId)
              AND (COALESCE(:name, '') = ''
                   OR LOWER(p.name) LIKE CONCAT('%', LOWER(CAST(:name as string)), '%'))
            """)
    Page<Project> searchByMemberAndName(
        @Param("userId") UUID userId,
        @Param("name") String name,
        Pageable pageable
    );
}
