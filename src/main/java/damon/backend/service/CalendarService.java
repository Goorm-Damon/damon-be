package damon.backend.service;

import damon.backend.dto.request.CalendarCreateRequestDto;
import damon.backend.dto.request.CalendarEditRequestDto;
import damon.backend.dto.request.CalendarsDeleteRequestDto;
import damon.backend.dto.request.TravelEditRequestDto;
import damon.backend.dto.response.CalendarCreateResponseDto;
import damon.backend.dto.response.CalendarEditResponseDto;
import damon.backend.dto.response.CalendarResponseDto;
import damon.backend.dto.response.CalendarsResponseDto;
import damon.backend.entity.Calendar;
import damon.backend.entity.Travel;
import damon.backend.entity.user.User;
import damon.backend.exception.custom.DataNotFoundException;
import damon.backend.repository.CalendarRepository;
import damon.backend.repository.TravelRepository;
import damon.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;

    private User getUserEntity(String identifier) {
        return userRepository.findByIdentifier(identifier).orElseThrow(DataNotFoundException::new);
    }

    private Calendar getCalendarEntityWithTravelsAndUser(Long calendarId, String identifier) {
        return calendarRepository.findByIdWithTravelAndUser(calendarId, identifier).orElseThrow(DataNotFoundException::new);
    }

    /**
     * 일정 글을 생성합니다.
     * @param identifier : 카카오에서 지정해 준 유저의 식별자
     * @param requestDto : 일정 글 생성에 필요한 정보
     */
    @Transactional
    public CalendarCreateResponseDto createCalendar(String identifier, CalendarCreateRequestDto requestDto) {
        User user = getUserEntity(identifier);
        // 일정 글 생성
        Calendar calendar = Calendar.builder()
                .user(user)
                .title(requestDto.getTitle())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .area(requestDto.getArea())
                .build();

        Calendar savedCalendar = calendarRepository.save(calendar);
        // 여행지 추가
        requestDto.getTravels().forEach(travelDto -> {
            Travel newTravel = Travel.builder()
                    .calendar(calendar) // 여행지와 일정 연결
                    .locationName(travelDto.getLocationName())
                    .latitude(travelDto.getLatitude())
                    .longitude(travelDto.getLongitude())
                    .memo(travelDto.getMemo())
                    .travelDay(travelDto.getDay())
                    .orderNumber(travelDto.getOrder())
                    .build();

            travelRepository.save(newTravel);
        });
        return CalendarCreateResponseDto.from(savedCalendar.getId());
    }


    /**
     * 상위 5개의 일정 글 리스트를 조회합니다.
     * @return : 상위 5개의 일정 목록을 반환
     */
    @Transactional(readOnly = true)
    public List<CalendarsResponseDto> getCalendarsTop5()  {
        List<Calendar> calendarList = calendarRepository.findTop5();
        return CalendarsResponseDto.listFrom(calendarList);
    }

    /**
     * 내 일정 글 리스트를 조회합니다.
     * @param identifier : 카카오에서 지정해 준 유저의 식별자
     * @param page : 페이지 번호
     * @param size : 페이지 사이즈
     * @return : 요청한 페이징에 맞는 일정 목록을 반환
     */
    @Transactional(readOnly = true)
    public Page<CalendarsResponseDto> getCalendars(String identifier, int page, int size)  {
        User user = getUserEntity(identifier);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<Calendar> calendarPage = calendarRepository.findPageByUser(user.getId(), pageable);

        return calendarPage.map(CalendarsResponseDto::from);
    }

    /**
     * 내 일정 글 상세 조회합니다.
     * @param identifier : 카카오에서 지정해 준 유저의 식별자
     * @param calendarId : 해당 일정 글의 아이디
     * @return : 요청한 일정 글의 상세 정보를 반환
     */
    @Transactional(readOnly = true)
    public CalendarResponseDto getCalendar(String identifier, Long calendarId) {
        Calendar calendar = getCalendarEntityWithTravelsAndUser(calendarId, identifier);

        return CalendarResponseDto.from(calendar);
    }

    /**
     * 일정 글을 수정합니다.
     * @param identifier : 카카오에서 지정해 준 유저의 식별자
     * @param calendarId : 해당 일정 글의 아이디
     * @param requestDto : 일정 글 수정에 필요한 정보
     */
    @Transactional
    public CalendarEditResponseDto updateCalendar(String identifier, Long calendarId, CalendarEditRequestDto requestDto) {
        Calendar calendar = getCalendarEntityWithTravelsAndUser(calendarId, identifier);

        // 일정 글 업데이트 로직
        calendar.update(requestDto.getTitle(), requestDto.getStartDate(), requestDto.getEndDate(), requestDto.getArea());

        // 여행지 업테이트 로직
        calendar.getTravels().forEach(travel -> requestDto.getTravels().stream()
                .filter(travelEditRequestDto -> travelEditRequestDto.getTravelId() != null && !travelEditRequestDto.isDeleted())
                .filter(travelEditRequestDto -> travelEditRequestDto.getTravelId().equals(travel.getId()))
                .findFirst()
                .ifPresent(travelEditRequestDto -> travel.update(travelEditRequestDto.getLocationName(), travelEditRequestDto.getLatitude(), travelEditRequestDto.getLongitude(), travelEditRequestDto.getMemo(), travelEditRequestDto.getDay(), travelEditRequestDto.getOrder())));

        // 새로운 여행지 추가 로직
        List<Travel> newTravels = requestDto.getTravels().stream()
                .filter(travelEditRequestDto -> travelEditRequestDto.getTravelId() == null)
                .map(travelEditRequestDto -> Travel.builder()
                        .calendar(calendar) // 여행지와 일정 연결
                        .locationName(travelEditRequestDto.getLocationName())
                        .latitude(travelEditRequestDto.getLatitude())
                        .longitude(travelEditRequestDto.getLongitude())
                        .memo(travelEditRequestDto.getMemo())
                        .travelDay(travelEditRequestDto.getDay())
                        .orderNumber(travelEditRequestDto.getOrder())
                        .build())
                .collect(Collectors.toList());

        travelRepository.saveAll(newTravels);

        // 삭제된 여행지 로직 처리
        List<Long> deletedTravles = requestDto.getTravels().stream()
                .filter(travelEditRequestDto -> travelEditRequestDto.getTravelId() != null && travelEditRequestDto.isDeleted())
                .map(TravelEditRequestDto::getTravelId)
                .collect(Collectors.toList());

        if(!deletedTravles.isEmpty()) {
            travelRepository.deleteAllByIdIn(deletedTravles);
        }

        return CalendarEditResponseDto.from(calendarId);
    }


    /**
     * 일정 글을 삭제합니다.
     * @param identifier : 카카오에서 지정해 준 유저의 식별자
     * @param calendarId : 해당 일정 글의 아이디
     */
    @Transactional
    public void deleteCalendar(String identifier, Long calendarId) {
        User user = getUserEntity(identifier);

        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정을 찾을 수 없습니다."));

        if (!calendar.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 일정을 삭제할 수 없습니다.");
        }
        // cascde로 삭제합니다.
        calendarRepository.delete(calendar);
    }

    /**
     * 일정 글을 선택 삭제합니다.
     * @param identifier : 카카오에서 지정해 준 유저의 식별자
     * @param requestDto : 선택 삭제할 일정 글의 아이디
     */
    @Transactional
    public void deleteCalendars(String identifier, CalendarsDeleteRequestDto requestDto) {
        User user = getUserEntity(identifier);

        List<Calendar> calendars = calendarRepository.findAllById(requestDto.getCalendarIds());

        if(calendars.size() != requestDto.getCalendarIds().size()) {
            throw new IllegalArgumentException("요청된 일정 중 일부가 존재하지 않습니다.");
        }

        calendarRepository.deleteAllByIn(requestDto.getCalendarIds());
    }
}