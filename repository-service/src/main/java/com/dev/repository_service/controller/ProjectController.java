package com.dev.repository_service.controller;

import com.dev.repository_service.entity.Project;
import com.dev.repository_service.exception.UnauthorizedException;
import com.dev.repository_service.service.KafkaProducerService;
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
    private final KafkaProducerService kafkaProducerService;
    private final MinioService minioService;

    public record CreateProjectRequest(String name, String ownerEmail, String description, boolean isPrivate){}

    @PostMapping("/create")
    public ResponseEntity<Project> createRepository(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CreateProjectRequest request){
        
        if (request.name() == null || request.name().isEmpty()) {
            throw new RuntimeException("Repository name required");
        }
        
        if (!userId.equals(request.ownerEmail())) {
            throw new UnauthorizedException("Cannot create repo for another user");
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
        
        if (!userId.equals(ownerEmail)) {
            throw new UnauthorizedException("Cannot access repos for another user");
        }

        return ResponseEntity.ok(projectService.getUserProject(ownerEmail));
    }

    @GetMapping("/repoexists/{ownerEmail}/{name}")
    public ResponseEntity<Boolean> doesUserRepoExists(@PathVariable String ownerEmail, @PathVariable String name){
        return ResponseEntity.ok(projectService.userProjectExists(ownerEmail, name));
    }

    @PostMapping("/upload/{ownerEmail}/{name}")
    public ResponseEntity<String> uploadFile(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String ownerEmail,
            @PathVariable String name,
            @RequestParam("file")MultipartFile file){

        if (!userId.equals(ownerEmail)) {
            throw new UnauthorizedException("Cannot upload to another user's repo");
        }

        if(!projectService.userProjectExists(ownerEmail,name)){
            return ResponseEntity.badRequest().body("Repository doesn't exist");
        }
        
        String savedPath = minioService.uploadFile(ownerEmail, name, file);

        String actualFile = file.getOriginalFilename();
        if(actualFile == null) actualFile = "unknown-file";
        kafkaProducerService.fileUploadEvent(ownerEmail,name,actualFile);
        return ResponseEntity.ok().body("file successfully pushed to :" + savedPath);
    }

    @GetMapping("/download/{ownerEmail}/{name}")
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String ownerEmail,
            @PathVariable String name,
            @RequestParam("fileName")String fileName){

        if (!userId.equals(ownerEmail)) {
            throw new UnauthorizedException("Cannot download from another user's repo");
        }

        if(!projectService.userProjectExists(ownerEmail,name)){
            return ResponseEntity.badRequest().build();
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
    public ResponseEntity<List<String>> userFiles(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String ownerEmail,
            @PathVariable String name){
        
        if (!userId.equals(ownerEmail)) {
            throw new UnauthorizedException("Cannot access files from another user's repo");
        }

        if(!projectService.userProjectExists(ownerEmail, name)){
            return ResponseEntity.badRequest().build();
        }

        List<String> files = minioService.fileList(ownerEmail,name);
        return ResponseEntity.ok(files);
    }
    @DeleteMapping("/{ownerEmail}/{name}")
    public ResponseEntity<Boolean> deleteUserRepo(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String ownerEmail,
            @PathVariable String name){
       
       if (!userId.equals(ownerEmail)) {
           throw new UnauthorizedException("Cannot delete another user's repo");
       }

       boolean isDeleted = projectService.deleteUserRepo(ownerEmail, name);

       if(isDeleted){
           return ResponseEntity.ok(true);
       }else{
           return ResponseEntity.badRequest().body(false);
       }

    }
}
