package com.dev.repository_service.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void initBucket(){
        try{
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if(!found){
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("minio bucket created",bucketName);
            }else{
                log.info("bucket already exists!", bucketName);
            }
        }catch(Exception e){
            log.error("error connecting to minio warehouse");
            throw new RuntimeException("failed to initialize minio bucket",e);
        }
    }

    public String uploadFile(String ownerEmail, String name, MultipartFile file){
        try{
            String objectName = ownerEmail + "/" + name + "/" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(),file.getSize(), -1)
                            .contentType(file.getContentType())
                    .build());

            log.info("code uploaded: {}",objectName);
            return objectName;
        }
        catch(Exception e){
            log.error("failed to upload the new file");
            throw new RuntimeException("error uploading file: " + e.getMessage());
        }
    }

}
