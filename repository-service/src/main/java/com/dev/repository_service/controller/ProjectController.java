package com.dev.repository_service.controller;

import com.dev.repository_service.entity.Project;
import com.dev.repository_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    public record createProjectRequest(String name, String ownerEmail, String description, boolean isPrivate);

    public ResponseEntity<Project> createRepository(@RequestBody createProjectRequest request){
        String createRecord =
    }
}
