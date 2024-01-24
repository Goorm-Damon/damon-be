package damon.backend.dto.request;

import damon.backend.entity.Area;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReviewRequest {

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Area area;
    private Long cost;
    private List<String> suggests;
    private List<String> freeTags;
    private List<MultipartFile> images;
    private String content;

}
