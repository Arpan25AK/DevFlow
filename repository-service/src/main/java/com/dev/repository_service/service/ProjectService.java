package com.dev.repository_service.service;

import com.dev.repository_service.entity.Project;
import com.dev.repository_service.repo.ProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final KafkaProducerService producerService;
    private final MinioService minioService;

    @Transactional
    public Project createProject(String name, String ownerEmail, String description, boolean isPrivate){
        log.info("checking if a repo already exists for user : {}/ {} ", name, ownerEmail);

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
        log.info("a repo has be created for user: {} / {}" , name, ownerEmail);
        producerService.projectCreatedEvent(ownerEmail,name);
        return savedProject;
    }


    public List<Project> getUserProject(String ownerEmail){
        return projectRepository.findByOwnerEmail(ownerEmail);
    }

    // this method exists check if the user already has a repo
    public boolean userProjectExists(String ownerEmail, String name){
        return projectRepository.existsByOwnerEmailAndName(ownerEmail, name);
    }

    public boolean deleteUserRepo(String ownerEmail, String name){
        if(!projectRepository.existsByOwnerEmailAndName(ownerEmail, name)){
            return false;
        }

        minioService.deleteUserFiles(ownerEmail, name);

        Long deleteCount = projectRepository.deleteByOwnerEmailAndName(ownerEmail,name);

        return deleteCount > 0;
    }

    public boolean projectExistsById(long repositoryId){
        return projectRepository.existsById(repositoryId);
    }

}
