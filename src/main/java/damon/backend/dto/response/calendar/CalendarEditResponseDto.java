package damon.backend.dto.response.calendar;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CalendarEditResponseDto {
    private Long calendarId;

    public static CalendarEditResponseDto from(Long calendarId) {
        return new CalendarEditResponseDto(calendarId);
    }
}
