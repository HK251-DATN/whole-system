package edu.hcmut.datn.back_office_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface R2UploadService {

    String upload(MultipartFile file, String bucket);
    
    void delete(String key, String bucket);
}
