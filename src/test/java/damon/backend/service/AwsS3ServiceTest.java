//package damon.backend.service;
//
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.mock.web.MockMultipartFile;
//import software.amazon.awssdk.services.s3.S3Client;
//
//import java.io.ByteArrayInputStream;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//
//@SpringBootTest
//@Transactional
//public class AwsS3ServiceTest {
//
//    @Mock
//    private S3Client s3Client;
//
//    @InjectMocks
//    private AwsS3Service awsS3Service;
//
//    @Test
//    public void testUploadImage() throws Exception {
//        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
//        awsS3Service.uploadImage(file, awsS3Service.reviewPrefix);
//        verify(s3Client).putObject(any(), any());
//    }
//
//    @Test
//    public void testUploadImages() throws Exception {
//        MockMultipartFile file1 = new MockMultipartFile("file1", "test1.txt", "text/plain", "test data 1".getBytes());
//        MockMultipartFile file2 = new MockMultipartFile("file2", "test2.txt", "text/plain", "test data 2".getBytes());
//        List<String> urls = awsS3Service.uploadImages(List.of(file1, file2), awsS3Service.reviewPrefix);
//        verify(s3Client).putObject(any(), any());
//        // 이 테스트는 실제 업로드된 URL 목록을 검증하는 더 구체적인 검증이 필요할 수 있습니다.
//    }
//
//    @Test
//    public void testDeleteImageByUrl() {
//        String testUrl = "https://goorm-damon-s3.s3.ap-northeast-2.amazonaws.com/review/test.txt";
//        awsS3Service.deleteImageByUrl(testUrl);
//        verify(s3Client).deleteObject(any());
//    }
//}
