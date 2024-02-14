package damon.backend.dto.request.calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
public class CalendarsDeleteRequestDto {
    private List<Long> calendarIds;

    public static CalendarsDeleteRequestDto of(List<Long> calendarIds) {
        return new CalendarsDeleteRequestDto(calendarIds);
    }
}
