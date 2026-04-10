package com.dev.code_review_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "repository-service")
public interface RepositoryServiceClient {

    @GetMapping("/api/repositories/{repositoryId}/exists")
    boolean checkRepositoryExists(@PathVariable("repositoryId") Long repositoryId);
}
