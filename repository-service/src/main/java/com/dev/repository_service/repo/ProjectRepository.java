package com.dev.repository_service.repo;

import com.dev.repository_service.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

     List<Project> findByOwnerEmail(String ownerEmail);

     boolean existsByOwnerEmailAndName(String ownerEmail, String name);

     boolean deleteRepo(String ownerEmail, String name);

}
