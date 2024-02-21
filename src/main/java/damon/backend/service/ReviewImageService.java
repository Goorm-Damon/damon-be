package damon.backend.service;

import damon.backend.entity.Review;
import damon.backend.entity.ReviewImage;
import damon.backend.exception.ReviewException;
import damon.backend.repository.ReviewImageRepository;
import damon.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewImageService {
    private final ReviewImageRepository reviewImageRepository;
    private final AwsS3Service awsS3Service;
    private final ReviewRepository reviewRepository;


    // 리뷰 생성 및 이미지 업로드 통합 처리
    public void attachImagesToReview(Review review, List<MultipartFile> images) throws IOException {
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                // dirType을 기반으로 이미지 업로드 처리
                String dirType = "review"; // 또는 "community" 등 context에 맞게 설정
                String imageUrl = awsS3Service.uploadFiles(file, dirType);

                ReviewImage reviewImage = ReviewImage.createImage(imageUrl, review);
                reviewImageRepository.save(reviewImage);
            }
        }
    }

    // 이미지 삭제 처리 로직
    public void deleteReviewImage(Long reviewImageId) {
        reviewImageRepository.findById(reviewImageId).ifPresent(reviewImage -> {
            // S3에서 이미지 삭제
            String fileName = extractFileNameFromUrl(reviewImage.getUrl());
            awsS3Service.deleteImageFromS3(fileName);

            reviewImageRepository.delete(reviewImage);
        });
    }

    private String extractFileNameFromUrl(String fileUrl) {
        // URL에서 파일 이름을 추출하는 로직 구현
        // 예: "https://s3.amazonaws.com/bucket-name/review-images/uuid-filename.jpg"에서 "review-images/uuid-filename.jpg" 추출
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}