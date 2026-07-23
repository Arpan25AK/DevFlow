package com.dev.auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "RepoConfig")
public interface RepositoryServiceClient {

    @DeleteMapping("/internal/repositories/purge/{email}")
    void purgeAllForUser(@PathVariable("email") String email);
}