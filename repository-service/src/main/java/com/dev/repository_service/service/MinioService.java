package com.dev.repository_service.service;
import io.minio.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.minio.messages.Item;
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
                log.info("minio bucket created: {}",bucketName);
            }else{
                log.info("bucket already exists! : {}", bucketName);
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

    public InputStream downloadFile(String ownerEmail, String name, String fileName) {
        try {
            String objectName = ownerEmail + "/" + name + "/" + fileName;

            return minioClient.getObject(
                    GetObjectArgs.
                            builder().
                            bucket(bucketName).
                            object(objectName)
                    .build()
            );
        }catch(Exception e){
            log.error("couldn't download file : {}", fileName, e);
            throw new RuntimeException("download failed" + e.getMessage());
        }
    }

    public List<String> fileList(String ownerEmail, String name){
        try{
            String prefix = ownerEmail + "/" + name + "/";

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().
                            bucket(bucketName).
                            prefix(prefix).
                            recursive(true)
                            .build()
            );

            List<String> fileName = new ArrayList<>();
            for(Result<Item> result : results){
                Item item = result.get();

                String fullObjName = item.objectName();
                String cleanObjName = fullObjName.substring(prefix.length());
                fileName.add(cleanObjName);
            }
            return fileName;
        }catch(Exception e){
            log.error("User repo is Empty!");
            throw new RuntimeException("Error during repo data retrival" + e.getMessage());
        }
    }
}
