package damon.backend.dto.request.calendar;

import damon.backend.entity.Area;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
public class CalendarEditRequestDto {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Area area;
    private List<TravelEditRequestDto> travels;

    public static CalendarEditRequestDto of(String title, LocalDate startDate, LocalDate endDate, Area area, List<TravelEditRequestDto> travels) {
        return new CalendarEditRequestDto(title, startDate, endDate, area, travels);
    }
}
