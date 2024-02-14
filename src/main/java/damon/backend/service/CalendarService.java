package damon.backend.service;

import damon.backend.dto.request.calendar.CalendarCreateRequestDto;
import damon.backend.dto.request.calendar.CalendarEditRequestDto;
import damon.backend.dto.request.calendar.CalendarsDeleteRequestDto;
import damon.backend.dto.request.calendar.TravelEditRequestDto;
import damon.backend.dto.response.calendar.CalendarCreateResponseDto;
import damon.backend.dto.response.calendar.CalendarEditResponseDto;
import damon.backend.dto.response.calendar.CalendarResponseDto;
import damon.backend.dto.response.calendar.CalendarsResponseDto;
import damon.backend.entity.Calendar;
import damon.backend.entity.Member;
import damon.backend.entity.Travel;
import damon.backend.exception.NotFoundException;
import damon.backend.exception.NotMeException;
import damon.backend.repository.CalendarRepository;
import damon.backend.repository.MemberRepository;
import damon.backend.repository.TravelRepository;
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
    private final MemberRepository memberRepository;

    /**
     * 일정 글을 생성합니다.
     * @param memberId : 해당 멤버의 아이디
     * @param requestDto : 일정 글 생성에 필요한 정보
     */
    @Transactional
    public CalendarCreateResponseDto createCalendar(Long memberId, CalendarCreateRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다. id : " + memberId));

        // 일정 글 생성
        Calendar calendar = Calendar.builder()
                .member(member)
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
            // 생명 주기를 수동으로 관리하기 위해 여행지를 저장할 때마다 일정 글에도 저장(추후에 cascade를 고려합니다.)
            travelRepository.save(newTravel);
        });
        return CalendarCreateResponseDto.from(savedCalendar.getId());
    }

    /**
     * 내 일정 글 리스트를 조회합니다.
     * @param memberId : 해당 멤버의 아이디
     * @param page : 페이지 번호
     * @param size : 페이지 사이즈
     * @return : 요청한 페이징에 맞는 일정 목록을 반환
     */
    @Transactional(readOnly = true)
    public Page<CalendarsResponseDto> getCalendars(Long memberId, int page, int size)  {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다. id : " + memberId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<Calendar> calendarPage = calendarRepository.findPageByMember(member.getId(), pageable);

        return calendarPage.map(CalendarsResponseDto::from);
    }

    /**
     * 내 일정 글 상세 조회합니다.
     * @param memberId : 해당 멤버의 아이디
     * @param calendarId : 해당 일정 글의 아이디
     * @return : 요청한 일정 글의 상세 정보를 반환
     */
    @Transactional(readOnly = true)
    public CalendarResponseDto getCalendar(Long memberId, Long calendarId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다. id : " + memberId));

        Calendar calendar = calendarRepository.findByIdWithTravel(calendarId)
                .orElseThrow(() -> new NotFoundException("해당 일정을 찾을 수 없습니다. id : " + calendarId));

        if (!calendar.getMember().getId().equals(member.getId())) {
            throw new NotMeException();
        }

        return CalendarResponseDto.from(calendar);
    }

    /**
     * 일정 글을 수정합니다.
     * @param memberId : 해당 멤버의 아이디
     * @param calendarId : 해당 일정 글의 아이디
     * @param requestDto : 일정 글 수정에 필요한 정보
     */
    @Transactional
    public CalendarEditResponseDto updateCalendar(Long memberId, Long calendarId, CalendarEditRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다. id : " + memberId));

        Calendar calendar = calendarRepository.findByIdWithTravel(calendarId)
                .orElseThrow(() -> new NotFoundException("해당 일정을 찾을 수 없습니다. id : " + calendarId));

        if (!calendar.getMember().getId().equals(member.getId())) {
            throw new NotMeException();
        }

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
                        .travelDay(travelEditRequestDto.getDay()) // orderNum 순서를 어떻게 관리 할 것인가? => day로 관리
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
     * @param memberId : 해당 멤버의 아이디
     * @param calendarId : 해당 일정 글의 아이디
     */
    @Transactional
    public void deleteCalendar(Long memberId, Long calendarId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다. id : " + memberId));

        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new NotFoundException("해당 일정을 찾을 수 없습니다. id : " + calendarId));

        if (!calendar.getMember().getId().equals(member.getId())) {
            throw new NotMeException();
        }
        // cascde로 삭제합니다.
        calendarRepository.delete(calendar);
    }

    /**
     * 일정 글을 선택 삭제합니다.
     * @param memberId : 해당 멤버의 아이디
     * @param requestDto : 선택 삭제할 일정 글의 아이디
     */
    @Transactional
    public void deleteCalendars(Long memberId, CalendarsDeleteRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다. id : " + memberId));

        List<Calendar> calendars = calendarRepository.findAllById(requestDto.getCalendarIds());

        if(calendars.size() != requestDto.getCalendarIds().size()) {
            throw new NotFoundException("요청된 일정 중 일부가 존재하지 않습니다.");
        }

        calendarRepository.deleteAllByIn(requestDto.getCalendarIds());
    }
}