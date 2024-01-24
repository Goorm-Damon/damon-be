package damon.backend.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import damon.backend.config.S3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewImageService {
    private final S3Config s3Config;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.review-prefix}")
    private String reviewPrefix;

    private String localLocation = "/Users/user/Desktop/";

    // 이미지 업로드 메서드 (MultipartFile 직접 사용)
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file");
        }

        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf("."));

        String uuidFileName = UUID.randomUUID().toString() + ext;

        // 로컬 파일 저장 대신 InputStream을 사용하여 S3에 바로 업로드
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        s3Config.amazonS3Client().putObject(new PutObjectRequest(bucket, reviewPrefix + uuidFileName, file.getInputStream(), metadata));

        String s3Url = s3Config.amazonS3Client().getUrl(bucket, reviewPrefix + uuidFileName).toString();

        return s3Url;
    }
    // 여러 이미지를 업로드하고 URL 목록을 반환하는 메서드
    public List<String> uploadImages(List<MultipartFile> files) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String imageUrl = uploadImage(file);  // 각 파일을 업로드하고 URL을 받음
            imageUrls.add(imageUrl); // 받은 URL을 리스트에 추가
        }

        return imageUrls; // 모든 URL을 담은 리스트를 반환
    }

}
