package com.dev.repository_service.service;

import com.dev.repository_service.entity.Project;
import com.dev.repository_service.repo.ProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public Project createProject(String name, String ownerEmail, String description, boolean isPrivate){
        log.info("checking if a repo already exists for user : ", name, ownerEmail);

        if(projectRepository.existsByOwnerEmailAndName(ownerEmail,name)){
            throw new RuntimeException("repo already exists for the user");
        }

        Project newProject = Project.builder().
                name(name).
                ownerEmail(ownerEmail).
                description(description).
                isPrivate(isPrivate).
                build();

        Project savedProject = projectRepository.save(newProject);
        log.info("a repo has be created for user: " , name, ownerEmail);

        return savedProject;
    }


    public List<Project> getUserProject(String ownerEmail){
        return projectRepository.findByOwnerEmail(ownerEmail);
    }

    // this method exists check if the user already has a repo
    public boolean userProjectExists(String ownerEmail, String name){
        return projectRepository.existsByOwnerEmailAndName(ownerEmail, name);
    }

}
