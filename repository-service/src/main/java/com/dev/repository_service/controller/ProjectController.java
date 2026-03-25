package com.dev.repository_service.controller;

import com.dev.repository_service.entity.Project;
import com.dev.repository_service.service.MinioService;
import com.dev.repository_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final MinioService minioService;

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
    public ResponseEntity<Boolean> doesUserRepoExists(@PathVariable String ownerEmail, @PathVariable String name){
        return ResponseEntity.ok(projectService.userProjectExists(ownerEmail, name));
    }

    @PostMapping("/upload/{ownerEmail}/{name}")
    public ResponseEntity<String> uploadFile(@PathVariable String ownerEmail,
                                             @PathVariable String name,
                                             @RequestParam("file")MultipartFile file){

        if(!projectService.userProjectExists(ownerEmail,name)){
            return ResponseEntity.badRequest().body("repository dosen't exists ");
        }

        String savedPath = minioService.uploadFile(ownerEmail, name, file);
        return ResponseEntity.ok().body("file successfully pushed to :" + savedPath);
    }

    @PostMapping("/download/{ownerEmail}/{name}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String ownerEmail,
                                                            @PathVariable String name,
                                                            @RequestParam("fileName")String fileName){

        if(!projectService.userProjectExists(ownerEmail,name)){
            return ResponseEntity.badRequest().body("");
        }

        InputStream stream = minioService.downloadFile(ownerEmail, name, fileName);
        InputStreamResource resource = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
