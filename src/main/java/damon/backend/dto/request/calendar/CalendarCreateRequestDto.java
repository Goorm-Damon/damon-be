package damon.backend.dto.request.calendar;

import damon.backend.entity.Area;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행 일정 생성 요청에 대한 데이터 전송 객체(DTO).
 * <p>이 클래스는 여행 일정을 추가하는 기능에서 사용자 입력을 캡슐화 합니다.</p>
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
public class CalendarCreateRequestDto {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Area area;
    private List<TravelCreateRequestDto> travels;

    /**
     * 여행 일정 생성 요청 객체를 생성합니다.
     * @param title 여행 일정 제목, 예: "서울 여행 계획".
     * @param startDate 여행 일정 시작 날짜, 예: LocalDate.of(2021, 10, 1).
     * @param endDate 여행 일정 종료 날짜, 예: LocalDate.of(2021, 10, 3).
     * @param area 여행 일정 지역, 예: Area.SEOUL.
     * @param travels 여행 일정에 포함된 여행지 목록, 각 여행지는 TravelCreateRequestDto 인스턴스로 표현됩니다.
     * @return 새로운 CalendarCreateRequestDto 인스턴스
     *
     * <p>사용 예제:</p>
     * <pre>
     * List<TravelCreateRequestDto> travels = Arrays.asList(
     *        TravelCreateRequestDto.of("서울 여행 남산타워", "37.5665", "126.9780", 1, "남산 타워 방문 예정", 1),
     *        TravelCreateRequestDto.of("서울 여행 경복궁", "37.5796", "126.9770", 2, "경복궁 방문 예정", 2)
     * );
     *
     *
     * CalendarCreateRequestDto dto = CalendarCreateRequestDto.of("서울 여행 계획", LocalDate.of(2021, 10, 1), LocalDate.of(2021, 10, 3), Area.SEOUL, travels);
     * </pre>
     */
    public static CalendarCreateRequestDto of(String title, LocalDate startDate, LocalDate endDate, Area area, List<TravelCreateRequestDto> travels) {
        return new CalendarCreateRequestDto(title, startDate, endDate, area, travels);
    }
}
