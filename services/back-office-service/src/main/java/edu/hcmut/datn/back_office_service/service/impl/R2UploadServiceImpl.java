package edu.hcmut.datn.back_office_service.service.impl;

import java.util.UUID;

import edu.hcmut.datn.back_office_service.exception.r2service.DeletionFailedException;
import edu.hcmut.datn.back_office_service.exception.r2service.ObjectNotFoundException;
import edu.hcmut.datn.back_office_service.exception.r2service.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.hcmut.datn.back_office_service.service.R2UploadService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class R2UploadServiceImpl implements R2UploadService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${app.user-avatar-public-bucket-url}")
    private String userAvtPublicBucketUrl;
    
    @Value("${app.product-general-image-public-bucket-url}")
    private String productGeneralImgPublicBucketUrl;

    @Override
    public String upload(MultipartFile file, String bucket) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            
            log.info("Get filename success");

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();
            
            log.info("Create PutObjectRequest success");

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return buildFileUrl(fileName, bucket);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Upload failed", e);
        }
    }

    private String buildFileUrl(String key, String bucket) {
        String buckerPublicUrl = getPublicUrl(bucket);

        return buckerPublicUrl + "/" + key;
    }

    private String getPublicUrl (String bucket) {
        String publicUrl = "";
        switch (bucket) {
            case "back-office-user-avts" ->
                publicUrl = userAvtPublicBucketUrl;
            case "product-general-img" ->
                publicUrl = productGeneralImgPublicBucketUrl;
            default ->
                throw new AssertionError();
        }
        return publicUrl;
    }
    
    public void delete(String key, String bucket) {
        // Input validation
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (bucket == null || bucket.trim().isEmpty()) {
            throw new IllegalArgumentException("Bucket cannot be null or empty");
        }
        
        try {
            // Step 1: Verify object exists before attempting deletion
            if (!objectExists(key, bucket)) {
                throw new ObjectNotFoundException(
                        String.format("Object not found - Key: '%s', Bucket: '%s'", key, bucket)
                );
            }
            
            // Step 2: Perform deletion
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            
            DeleteObjectResponse response = s3Client.deleteObject(request);
            
            // Step 3: Verify deletion succeeded (optional but recommended)
            if (objectExists(key, bucket)) {
                throw new DeletionFailedException(
                        String.format("Object still exists after deletion - Key: '%s', Bucket: '%s'", key, bucket)
                );
            }
            
            // Optional: Log successful deletion
            log.info("Successfully deleted object - Key: '{}', Bucket: '{}'", key, bucket);
            
        } catch (ObjectNotFoundException | DeletionFailedException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (S3Exception e) {
            // Handle S3-specific errors with more context
            throw new StorageException(
                    String.format("S3 error while deleting object - Key: '%s', Bucket: '%s', Error: %s",
                            key, bucket, e.awsErrorDetails().errorMessage()),
                    e
            );
        } catch (Exception e) {
            // Handle unexpected errors
            throw new StorageException(
                    String.format("Unexpected error deleting object - Key: '%s', Bucket: '%s'", key, bucket),
                    e
            );
        }
    }
    
    /**
     * Helper method to check if object exists
     */
    private boolean objectExists(String key, String bucket) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            
            s3Client.headObject(headRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            // If we get a permission error or other S3 error, we should propagate it
            // Don't silently treat it as "doesn't exist"
            throw new StorageException(
                    String.format("Error checking object existence - Key: '%s', Bucket: '%s'", key, bucket),
                    e
            );
        }
    }

}
