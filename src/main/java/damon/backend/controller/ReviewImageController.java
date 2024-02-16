package damon.backend.controller;

import damon.backend.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewImageController {

    private final AwsS3Service awsS3Service;

    @PostMapping("/api/upload")
    public ResponseEntity<Map<String, Object>> imageUpload(@RequestParam("upload") MultipartFile file) {
        Map<String, Object> responseData = new HashMap<>();

        if (file.isEmpty()) {
            responseData.put("uploaded", false);
            responseData.put("error", Map.of("message", "Cannot upload empty file"));
            return ResponseEntity.badRequest().body(responseData);
        }

        try {
            String s3Url = awsS3Service.uploadImage(file);
            responseData.put("uploaded", true);
            responseData.put("url", s3Url);
        } catch (IOException e) {
            responseData.put("uploaded", false);
            responseData.put("error", Map.of("message", "Could not upload image: " + e.getMessage()));
        }
        return ResponseEntity.internalServerError().body(responseData);
    }
}