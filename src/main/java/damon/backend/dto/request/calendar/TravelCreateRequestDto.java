package damon.backend.dto.request.calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 여행지 생성 요청에 대한 데이터 전송 객체(DTO).
 * <p>이 클래스는 여행 계획을 추가하는 기능에서 사용자 입력을 캡슐화 합니다.</p>
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
public class TravelCreateRequestDto {
    private String locationName;
    private String latitude;
    private String longitude;
    private int day;
    private String memo;
    private int order;

    /**
     * 여행지 생성 요청 객체를 생성합니다.
     * @param locationName 여행지 제목
     * @param latitude 여행지의 위도
     * @param longitude 여행지의 경도
     * @param day 여행지의 날짜
     * @param memo  여행지의 내용
     * @param order 여행지의 순서
     * @return 새로운 TravelCreateRequestDto 인스턴스
     *
     * <p>사용 예제:</p>
     * <pre>
     * TravelCreateRequestDto dto = TravelCreateRequestDto.of("서울 여행 남산타워", "37.5665", "126.9780", 1, "남산 타워 방문 예정", 1);
     *</pre>
     */
    public static TravelCreateRequestDto of(String locationName, String latitude, String longitude, int day, String memo, int order){
        return new TravelCreateRequestDto(locationName, latitude, longitude, day, memo, order);
    }
}
