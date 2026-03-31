package com.dev.ci_cd_service.controller;

import com.dev.ci_cd_service.entity.PipelineRun;
import com.dev.ci_cd_service.repo.PipelineRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/pipeline")
@RequiredArgsConstructor
@RestController
public class PipelineController {

    private final PipelineRunRepository repository;

    public ResponseEntity<List<PipelineRun>> getProjectPipelineHistory(@PathVariable String ownerEmail,
                                                                       @PathVariable String name){
        List<PipelineRun> history = repository.findByOwnerEmailAndNameOrderByTimeStampDesc(ownerEmail, name);
        return ResponseEntity.ok(history);
    }
}
