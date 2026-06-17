package com.dev.repository_service.controller;

import com.dev.repository_service.entity.Project;
import com.dev.repository_service.exception.UnauthorizedException;
import com.dev.repository_service.service.KafkaProducerService;
import com.dev.repository_service.service.MinioService;
import com.dev.repository_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    private final KafkaProducerService kafkaProducerService;
    private final MinioService minioService;

    public record CreateProjectRequest(String name, String ownerEmail, String description, boolean isPrivate){}

    @PostMapping("/create")
    public ResponseEntity<?> createRepository(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestBody CreateProjectRequest request){
        
        if (request.name() == null || request.name().isEmpty()) {
            throw new RuntimeException("Repository name required");
        }

        if(!userEmail.equals(request.ownerEmail())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to create repo under another users email");
        }

        Project createdProject = projectService.createProject(
                request.name(),
                request.ownerEmail(),
                request.description(),
                request.isPrivate()
        );

        return ResponseEntity.ok(createdProject);
    }
    @GetMapping("/getrepos/{ownerEmail}")
    public ResponseEntity<List<Project>> getUserRepositories(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String ownerEmail){
        


        return ResponseEntity.ok(projectService.getUserProject(ownerEmail));
    }

    @GetMapping("/repoexists/{ownerEmail}/{name}")
    public ResponseEntity<Boolean> doesUserRepoExists(@PathVariable String ownerEmail, @PathVariable String name){
        return ResponseEntity.ok(projectService.userProjectExists(ownerEmail, name));
    }

    @PostMapping("/upload/{ownerEmail}/{name}")
    public ResponseEntity<String> uploadFile(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @PathVariable String ownerEmail,
            @PathVariable String name,
            @RequestParam("file")MultipartFile file){



        if(!projectService.userProjectExists(ownerEmail,name)){
            return ResponseEntity.badRequest().body("Repository doesn't exist");
        }

        if(!userEmail.equals(ownerEmail)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to upload files to other user's repo");
        }
        
        String savedPath = minioService.uploadFile(ownerEmail, name, file);

        String actualFile = file.getOriginalFilename();
        if(actualFile == null) actualFile = "unknown-file";
        kafkaProducerService.fileUploadEvent(ownerEmail,name,actualFile);
        return ResponseEntity.ok().body("file successfully pushed to :" + savedPath);
    }

    @GetMapping("/download/{ownerEmail}/{name}")
    public ResponseEntity<?> downloadFile(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @PathVariable String ownerEmail,
            @PathVariable String name,
            @RequestParam("fileName")String fileName){

        Project project = projectService.getProject(ownerEmail, name);

        if(project == null){
            return ResponseEntity.notFound().build();
        }

        if(project.isPrivate() && !userEmail.equals(ownerEmail)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("The mentioned repo is private, cannot download");
        }

        InputStream stream = minioService.downloadFile(ownerEmail, name, fileName);
        InputStreamResource resource = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/{repositoryId}/exists")
    public ResponseEntity<Boolean> checkRepositoryExistsById(@PathVariable Long repositoryId) {
        boolean exists = projectService.projectExistsById(repositoryId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/getfiles/{ownerEmail}/{name}")
    public ResponseEntity<?> userFiles(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @PathVariable String ownerEmail,
            @PathVariable String name){
        
        Project project  = projectService.getProject(ownerEmail, name);

        if(project == null){
            return ResponseEntity.notFound().build();
        }

        if(project.isPrivate() && !userEmail.equals(ownerEmail)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("This repository is private");
        }

        List<String> files = minioService.fileList(ownerEmail,name);
        return ResponseEntity.ok(files);
    }
    @DeleteMapping("/{ownerEmail}/{name}")
    public ResponseEntity<?> deleteUserRepo(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @PathVariable String ownerEmail,
            @PathVariable String name){

            if(!userEmail.equals(ownerEmail)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unable to delete the repo as ur not the owner");
            }

            boolean isDeleted = projectService.deleteUserRepo(ownerEmail, name);

            if(isDeleted){
                return ResponseEntity.ok(true);
            }else{
                return ResponseEntity.badRequest().body(false);
            }

    }

    @DeleteMapping("/deletefile/{ownerEmail}/{name}")
    public ResponseEntity<String> deleteFile(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @PathVariable String ownerEmail,
            @PathVariable String name,
            @RequestParam("fileName") String fileName){

        // only owner can delete their files
        if(!userEmail.equals(ownerEmail)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("not your repo");
        }

        if(!projectService.userProjectExists(ownerEmail, name)){
            return ResponseEntity.notFound().build();
        }

        minioService.deleteSingleFile(ownerEmail, name, fileName);
        return ResponseEntity.ok("file deleted: " + fileName);
    }
}
