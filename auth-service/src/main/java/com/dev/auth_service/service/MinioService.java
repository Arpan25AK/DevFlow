package com.dev.auth_service.service;

import io.minio.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif"
    );

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

    private final MinioClient minioClient;

    @Value("${minio.avatar-bucket-name}")
    private String bucketName;

    public record AvatarObject(java.io.InputStream stream, String contentType){}

    @PostConstruct
    public void initBucket(){
        try{
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if(!found){
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("minio avatar bucket created: {}", bucketName);
            }else{
                log.info("avatar bucket already exists! : {}", bucketName);
            }
        }catch(Exception e){
            log.error("error connecting to minio warehouse");
            throw new RuntimeException("failed to initialize minio avatar bucket", e);
        }
    }

    public String uploadAvatar(String userId, MultipartFile file){
        if(file == null || file.isEmpty()){
            throw new IllegalArgumentException("file is required");
        }

        String contentType = file.getContentType();
        if(contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())){
            throw new IllegalArgumentException("only png, jpeg, webp or gif images are allowed");
        }

        if(file.getSize() > MAX_FILE_SIZE){
            throw new IllegalArgumentException("file must be 5MB or smaller");
        }

        try{
            // wipe any previous avatar (possibly a different extension) before writing the new one
            deleteAvatar(userId);

            String extension = extensionFor(contentType);
            String objectName = "avatars/" + userId + "/" + "avatar" + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build());

            log.info("avatar uploaded: {}", objectName);
            return objectName;
        }catch(IllegalArgumentException e){
            throw e;
        }catch(Exception e){
            log.error("failed to upload avatar for user {}", userId, e);
            throw new RuntimeException("error uploading avatar: " + e.getMessage());
        }
    }

    public AvatarObject getAvatar(String objectName){
        try{
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(objectName).build());

            java.io.InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(objectName).build());

            return new AvatarObject(stream, stat.contentType());
        }catch(Exception e){
            log.error("failed to fetch avatar: {}", objectName, e);
            throw new RuntimeException("avatar not found");
        }
    }

    public void deleteAvatar(String userId){
        try{
            String prefix = "avatars/" + userId + "/";

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(true)
                            .build());

            List<Result<Item>> items = new java.util.ArrayList<>();
            results.forEach(items::add);

            for(Result<Item> result : items){
                Item item = result.get();
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(item.objectName())
                                .build());
            }
        }catch(Exception e){
            log.error("failed to delete avatar for user {}", userId, e);
            throw new RuntimeException("error deleting avatar: " + e.getMessage());
        }
    }

    private String extensionFor(String contentType){
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}