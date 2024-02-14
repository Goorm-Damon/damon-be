package damon.backend.dto.response.calendar;

import damon.backend.entity.Area;
import damon.backend.entity.Calendar;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CalendarResponseDto {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Area area;

    private List<TravelDetailDto> travels;

    public static CalendarResponseDto from(Calendar calendar) {
        return new CalendarResponseDto(
                calendar.getTitle(),
                calendar.getStartDate(),
                calendar.getEndDate(),
                calendar.getArea(),
                TravelDetailDto.listFrom(calendar.getTravels())
        );
    }
}
