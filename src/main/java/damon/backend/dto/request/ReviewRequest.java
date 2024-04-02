package damon.backend.dto.request;

import damon.backend.entity.Area;
import lombok.Data;

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
    private List<String> tags;
    private String content;
    private List<String> imageUrls;
    private List<String> newImageUrls;
    private List<String> deleteImageUrls;
}
