package damon.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {
    private final S3Client s3Client; // S3Client 주입

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.review-prefix}")
    private String reviewPrefix;

    @Value("${cloud.aws.s3.community-prefix}")
    private String communityPrefix;


    public String uploadFiles(MultipartFile multipartFile, String dirType) throws IOException {
        String dirPrefix = "";
        switch (dirType) {
            case "review":
                dirPrefix = reviewPrefix;
                break;
            case "community":
                dirPrefix = communityPrefix;
                break;
            default:
                throw new IllegalArgumentException("Invalid directory type");
        }

        File uploadFile = convert(multipartFile).orElseThrow(() ->
                new IllegalArgumentException("MultipartFile -> File convert fail"));
        String fileName = dirPrefix + UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();
        return upload(uploadFile, fileName);
    }

    private String upload(File uploadFile, String filePath) {
        String uploadImageUrl = putS3(uploadFile, filePath);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    private String putS3(File uploadFile, String fileName) {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .build(),
                RequestBody.fromFile(uploadFile));
        return s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucket).key(fileName).build()).toExternalForm();
    }


    private void removeNewFile(File targetFile) {
        if (!targetFile.delete()) {
            System.out.println("Failed to delete the file: " + targetFile.getPath());
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        // 파일 변환 로직에 사용할 임시 디렉터리 경로
        String tempDirPath = System.getProperty("java.io.tmpdir");
        File convertFile = new File(tempDirPath + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename());

        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            } catch (IOException e) {
                return Optional.empty();
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
    }

    // AWS S3에서 특정 파일을 삭제하는 메서드
    public void deleteImageFromS3(String fileKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build());
            System.out.println("Successfully deleted " + fileKey + " from S3 bucket " + bucket);
        } catch (Exception e) {
            System.err.println("Error occurred while trying to delete " + fileKey + " from S3 bucket " + bucket);
            e.printStackTrace();
        }
    }
}