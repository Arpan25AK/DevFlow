package com.dev.repository_service.repo;

import com.dev.repository_service.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwnerEmail(String ownerEmail);

    List<Project> findByOwnerEmailAndIsPrivateFalse(String ownerEmail);

    Optional<Project> findByOwnerEmailAndName(String ownerString, String name);

    boolean existsByOwnerEmailAndName(String ownerEmail, String name);

    @Transactional
    Long deleteByOwnerEmailAndName(String ownerEmail, String name);

    @Transactional
    Long deleteByOwnerEmail(String ownerEmail);

}