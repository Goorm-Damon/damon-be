package damon.backend.dto.response.calendar;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CalendarCreateResponseDto {
    private Long calendarId;

    public static CalendarCreateResponseDto from(Long calendarId) {
        return new CalendarCreateResponseDto(calendarId);
    }
}
