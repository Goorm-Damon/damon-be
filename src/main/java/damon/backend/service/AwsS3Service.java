
package damon.backend.service;

import damon.backend.exception.custom.ImageCountExceededException;
import damon.backend.exception.custom.ImageSizeExceededException;
import damon.backend.util.Log;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
//@RequiredArgsConstructor
public class AwsS3Service {
    private final S3Client s3Client; // S3Client 주입

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.s3.review-prefix}")
    private String reviewPrefix;

    public AwsS3Service(S3Client s3Client, @Value("${cloud.aws.s3.review-prefix}") String reviewPrefix) {
        this.s3Client = s3Client;
        this.reviewPrefix = reviewPrefix;
        Log.info("Review Prefix: " + reviewPrefix);
    }

    public class UploadResult {
        private final String fileKey;
        private final String fileUrl;

        public UploadResult(String fileKey, String fileUrl) {
            this.fileKey = fileKey;
            this.fileUrl = fileUrl;
        }

        public String getFileKey() {
            return fileKey;
        }

        public String getFileUrl() {
            return fileUrl;
        }
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file");
        }

        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf("."));
        String uuidFileName = UUID.randomUUID().toString() + ext;

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(reviewPrefix + uuidFileName)
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        String result = s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(reviewPrefix + uuidFileName)).toString();
        Log.info("AwsS3Service uploadImage return:" + result);
        return result;
    }

    public List<String> uploadImages(List<MultipartFile> files) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        final int MAX_IMAGE_COUNT = 10;
        final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB

        if (files.size() > MAX_IMAGE_COUNT) {
            throw new ImageCountExceededException();
        }

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_IMAGE_SIZE) {
                throw new ImageSizeExceededException();
            }

            try {
                String imageUrl = uploadImage(file);
                imageUrls.add(imageUrl);
            } catch (IOException e) {

            }
        }
        return imageUrls;
    }

    // AwsS3Service 내 이미지 삭제 메서드
    public void deleteImageByUrl(String imageUrl) {
        // 이미지 URL 로깅
        Log.info("Received image URL for deletion: " + imageUrl);

        // 파일 키 추출 및 로깅
        String[] parts = imageUrl.split("review/");
        String fileKey = parts[1];
//        String fileKey = imageUrl.substring(imageUrl.indexOf(reviewPrefix));
        Log.info("Extracted file key for deletion: " + fileKey);

        // S3 객체 삭제 요청 전송 및 로깅
        try {
            Log.info("Sending request to delete object from S3: " + fileKey);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build());
            Log.info("Successfully deleted object from S3: " + fileKey);
        } catch (Exception e) {
            Log.error("Error occurred while deleting object from S3: " + fileKey);
            throw e;
        }
    }

    private String extractFileKeyFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.indexOf(bucket) + bucket.length() + 1);
    }
}