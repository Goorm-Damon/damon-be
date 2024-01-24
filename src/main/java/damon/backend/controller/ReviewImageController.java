package damon.backend.controller;

import damon.backend.service.ReviewImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewImageController {

    private final ReviewImageService reviewImageService;

    @PostMapping("/image/upload")
    @ResponseBody
    public Map<String, Object> imageUpload(@RequestParam("upload") MultipartFile file) {
        Map<String, Object> responseData = new HashMap<>();

        if (file.isEmpty()) {
            responseData.put("uploaded", false);
            responseData.put("error", Map.of("message", "Cannot upload empty file"));
            return responseData;
        }

        try {
            String s3Url = reviewImageService.uploadImage(file);
            responseData.put("uploaded", true);
            responseData.put("url", s3Url);
        } catch (IOException e) {
            responseData.put("uploaded", false);
            responseData.put("error", Map.of("message", "Could not upload image: " + e.getMessage()));
        }

        return responseData;
    }
}
