package com.dev.repository_service.controller;

import com.dev.repository_service.entity.Project;
import com.dev.repository_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    public record CreateProjectRequest(String name, String ownerEmail, String description, boolean isPrivate){}

    @PostMapping("/create")
    public ResponseEntity<Project> createRepository(@RequestBody CreateProjectRequest request){
        Project createdProject = projectService.createProject(
                request.name(),
                request.ownerEmail(),
                request.description(),
                request.isPrivate()
        );

        return ResponseEntity.ok(createdProject);
    }
    @GetMapping("/getrepos/{ownerEmail}")
    public ResponseEntity<List<Project>> getUserRepositories(@PathVariable String ownerEmail){
        return ResponseEntity.ok(projectService.getUserProject(ownerEmail));
    }

    @GetMapping("/repoexists/{ownerEmail}/{name}")
    public ResponseEntity<Boolean> doesUserRepoExists(@PathVariable String ownerEmail, String name){
        return ResponseEntity.ok(projectService.userProjectExists(ownerEmail, name));
    }
}
