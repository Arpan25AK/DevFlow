package com.dev.ci_cd_service.service;

import com.dev.ci_cd_service.entity.BuildStatus;
import com.dev.ci_cd_service.entity.PipelineRun;
import com.dev.ci_cd_service.repo.PipelineRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineService {

    private final PipelineRunRepository pipelineRunRepository;
    private final ObjectMapper objectMapper;

    public record fileUploadEvent(String ownerEmail, String name, String fileName){}

    @KafkaListener(topics = "code-uploaded-topic", groupId = "cicd-group")
    public void handleCodeUpload(String message) {
        try {
            fileUploadEvent event = objectMapper.readValue(message, fileUploadEvent.class);
            log.info("🚀 Triggering CI/CD Pipeline for {} in {}", event.fileName(), event.name());

            PipelineRun run = new PipelineRun();
            run.setOwnerEmail(event.ownerEmail());
            run.setName(event.name());
            run.setFileName(event.fileName());
            run.setStatus(BuildStatus.IN_PROGRESS);
            run.setBuildLogs("Initializing secure build environment...\nDownloading source code from MinIO Vault...\nExecuting compile phase...");
            pipelineRunRepository.save(run);

            simulateBuildProcess(run);
        }catch(Exception e){
            log.error("Failed to process CI/CD event!");
        }
    }

    private void simulateBuildProcess(PipelineRun run) {
        new Thread(() -> {
            try {
                // Fake a 5-second build and test process
                Thread.sleep(5000);

                // Randomize the result (80% chance the code passes, 20% chance it fails)
                boolean buildSuccess = new Random().nextInt(10) > 2;

                if (buildSuccess) {
                    run.setStatus(BuildStatus.SUCCESS);
                    run.setBuildLogs(run.getBuildLogs() + "\n✅ Compilation successful. All 142 automated tests passed.");
                    log.info("✅ Pipeline SUCCESS for {}", run.getFileName());
                } else {
                    run.setStatus(BuildStatus.FAILED);
                    run.setBuildLogs(run.getBuildLogs() + "\n❌ Compilation failed. Syntax error detected on line 42. Missing semicolon.");
                    log.warn("❌ Pipeline FAILED for {}", run.getFileName());
                }

                pipelineRunRepository.save(run);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
