package damon.backend.service;

import damon.backend.entity.Review;
import damon.backend.entity.ReviewImage;
import damon.backend.exception.ReviewException;
import damon.backend.repository.ReviewImageRepository;
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

    // 리뷰 생성 시 이미지 처리
    public List<ReviewImage> postImage(Review review, List<MultipartFile> images) {
        List<ReviewImage> savedImages = new ArrayList<>();
        try {
            if (images != null && !images.isEmpty()) {
                for (MultipartFile file : images) {
                    String url = awsS3Service.uploadImage(file); // S3에 이미지 업로드
                    ReviewImage newImage = ReviewImage.createImage(url, review); // 이미지 생성
                    savedImages.add(reviewImageRepository.save(newImage)); // DB에 저장
                }
            }
            return savedImages; // 저장된 이미지 리스트 반환
        } catch (IOException e) {
            throw ReviewException.imageUploadFailed();
        } catch (MaxUploadSizeExceededException e) {
            throw ReviewException.imageSizeExceeded();
        }
    }

    // 리뷰 업데이트 및 이미지 삭제 처리

    public void handleImage(Review review, List<MultipartFile> newImages, List<Long> imageIdsToDelete) {
        // 삭제할 이미지 처리
        deleteReviewImage(imageIdsToDelete);

        // 새 이미지 추가
        postImage(review, newImages);
    }

    // 이미지 삭제
    private void deleteReviewImage(List<Long> imageIdsToDelete) {
        if (imageIdsToDelete != null && !imageIdsToDelete.isEmpty()) {
            List<ReviewImage> imagesToDelete = reviewImageRepository.findAllById(imageIdsToDelete);
            imagesToDelete.forEach(image -> {
                // S3에서 이미지 파일 삭제
                awsS3Service.deleteImage(image.getUrl().substring(image.getUrl().lastIndexOf("/") + 1));
                // DB에서 이미지 정보 삭제
                reviewImageRepository.delete(image);
            });
        }
    }

}