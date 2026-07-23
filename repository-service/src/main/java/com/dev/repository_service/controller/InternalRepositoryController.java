package com.dev.repository_service.controller;

import com.dev.repository_service.entity.Project;
import com.dev.repository_service.repo.ProjectRepository;
import com.dev.repository_service.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal, service-to-service only. Do NOT add a gateway route for this path.
 * The api-gateway only proxies /api/repositories/** to this service, so as long as this
 * stays outside that prefix, the browser can never reach it directly.
 *
 * In production, this service's port should additionally not be reachable from
 * outside the internal docker/network - the path prefix alone is not a security
 * boundary, just a convenience for local dev where everything shares a network.
 */
@RestController
@RequestMapping("/internal/repositories")
@RequiredArgsConstructor
@Slf4j
public class InternalRepositoryController {

    private final ProjectRepository projectRepository;
    private final MinioService minioService;

    @DeleteMapping("/purge/{email}")
    public ResponseEntity<Void> purgeAllForUser(@PathVariable String email) {

        List<Project> projects = projectRepository.findByOwnerEmail(email);

        // Wipe every object under this user's prefix in one sweep - covers all
        // their repos at once, rather than looping deleteUserFiles per repo name.
        minioService.deleteAllUserFiles(email);

        projectRepository.deleteByOwnerEmail(email);

        log.info("purged {} repositories and all files for deleted account: {}", projects.size(), email);

        return ResponseEntity.noContent().build();
    }
}