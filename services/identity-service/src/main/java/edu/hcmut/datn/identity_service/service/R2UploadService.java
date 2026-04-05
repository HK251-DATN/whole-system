package edu.hcmut.datn.identity_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface R2UploadService {
    String upload(MultipartFile file);
}
