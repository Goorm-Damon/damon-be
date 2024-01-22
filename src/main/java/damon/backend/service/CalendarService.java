package damon.backend.service;

import damon.backend.dto.request.CalendarCreateRequestDto;
import damon.backend.entity.Calendar;
import damon.backend.entity.Travel;
import damon.backend.entity.User;
import damon.backend.repository.CalendarRepository;
import damon.backend.repository.TravelRepository;
import damon.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;

    /**
     * 일정 글을 생성합니다.
     * @param memberId : 해당 멤버의 아이디
     * @param requestDto : 일정 글 생성에 필요한 정보
     */
    @Transactional
    public void createCalendar(Long memberId, CalendarCreateRequestDto requestDto) {
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 일정 글 생성
        Calendar calendar = Calendar.builder()
                .user(user)
                .title(requestDto.getTitle())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .area(requestDto.getArea())
                .build();

        // 여행지 추가
        requestDto.getTravels().forEach(travelDto -> {
            Travel newTravel = Travel.builder()
                    .calendar(calendar) // 여행지와 일정 연결
                    .locationName(travelDto.getLocationName())
                    .latitude(travelDto.getLatitude())
                    .longitude(travelDto.getLongitude())
                    .orderNum(travelDto.getOrderNum()) // TODO: orderNum 순서를 어떻게 관리 할 것인가?
                    .build();
            // 생명 주기를 수동으로 관리하기 위해 여행지를 저장할 때마다 일정 글에도 저장(추후에 cascade를 고려합니다.)
            travelRepository.save(newTravel);
        });
        calendarRepository.save(calendar);
    }
}
