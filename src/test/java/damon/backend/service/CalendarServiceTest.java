package damon.backend.service;

import damon.backend.dto.request.CalendarCreateRequestDto;
import damon.backend.dto.request.TravelCreateRequestDto;
import damon.backend.dto.response.CalendarResponseDto;
import damon.backend.dto.response.CalendarsResponseDto;
import damon.backend.entity.Area;
import damon.backend.entity.Calendar;
import damon.backend.entity.Member;
import damon.backend.entity.Travel;
import damon.backend.repository.CalendarRepository;
import damon.backend.repository.MemberRepository;
import damon.backend.repository.TravelRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {
    @Mock
    CalendarRepository calendarRepository;
    @Mock
    private TravelRepository travelRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CalendarService calendarService;

    private Member member;

    private CalendarCreateRequestDto requestDto;



    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId("testId");
        member.setNickname("test nickname");
        member.setProfileImgUrl("/test/url");
        ArrayList<TravelCreateRequestDto> travelDtoList = new ArrayList<>();

        TravelCreateRequestDto travelDto1 = TravelCreateRequestDto.of("test LocalName1", "38.1231231", "128.123123", 1, "test memo", 1);
        TravelCreateRequestDto travelDto2 = TravelCreateRequestDto.of("test LocalName2", "38.1231231", "128.123123", 1, "test memo", 2);
        TravelCreateRequestDto travelDto3 = TravelCreateRequestDto.of("test LocalName3", "38.1231231", "128.123123", 1, "test memo", 3);

        TravelCreateRequestDto travelDto4 = TravelCreateRequestDto.of("test LocalName1", "38.1231231", "128.123123", 2, "test memo", 1);
        TravelCreateRequestDto travelDto5 = TravelCreateRequestDto.of("test LocalName2", "38.1231231", "128.123123", 2, "test memo",2);
        TravelCreateRequestDto travelDto6 = TravelCreateRequestDto.of("test LocalName3", "38.1231231", "128.123123", 2, "test memo", 3);

        travelDtoList.add(travelDto1);
        travelDtoList.add(travelDto2);
        travelDtoList.add(travelDto3);
        travelDtoList.add(travelDto4);
        travelDtoList.add(travelDto5);
        travelDtoList.add(travelDto6);

        requestDto = CalendarCreateRequestDto.of("제주 여행 2박 3일", LocalDate.of(2021, 8, 1), LocalDate.of(2021, 8, 3), Area.JEJU, travelDtoList);

        when(memberRepository.findById(any())).thenReturn(Optional.of(member));
    }

    @DisplayName("createCalendar: 일정 글을 생성한다.")
    @Test
    public void createCalendar() throws Exception {
        when(calendarRepository.save(any(Calendar.class))).then(returnsFirstArg());
        when(travelRepository.save(any(Travel.class))).then(returnsFirstArg());

        // Action
        calendarService.createCalendar(member.getId(), requestDto);

        // Verify
        verify(memberRepository).findById(member.getId());
        verify(calendarRepository).save(any(Calendar.class));
        verify(travelRepository, times(requestDto.getTravels().size())).save(any(Travel.class));
    }

    @DisplayName("getCalendars: 멤버의 일정 글 리스트를 조회한다.")
    @Test
    public void getCalendarList() throws Exception {
        int page = 0;
        int size = 10;

        // 여러개의 캘린더 생성(11개)
        List<Calendar> calendars = Arrays.asList(
                Calendar.builder().build(),
                Calendar.builder().build(),
                Calendar.builder().build(),
                Calendar.builder().build(),
                Calendar.builder().build(),
                Calendar.builder().build(),
                Calendar.builder().build(),
                Calendar.builder().build(),
                Calendar.builder().build(),
                Calendar.builder().build(),
                Calendar.builder().build()
                );

        // 페이징 처리 수동
        PageRequest pageRequest = PageRequest.of(page, size);
        int start = 0;
        int end = Math.min(start + size, calendars.size());

        List<Calendar> subList = calendars.subList(start, end);
        Page<Calendar> mockPage = new PageImpl<>(subList, pageRequest, calendars.size());
        when(calendarRepository.findByMember(any(Member.class), any(PageRequest.class))).thenReturn(mockPage);

        // Action
        Page<CalendarsResponseDto> result = calendarService.getCalendars(member.getId(), page, size);

        // Verify
        assertNotNull(result);
        assertEquals(size, result.getContent().size());
        verify(calendarRepository).findByMember(any(Member.class), any(PageRequest.class));
    }

    @DisplayName("getCalendar: 멤버의 일정 글 상세 조회한다.")
    @Test
    public void getCalender() throws Exception {
        Long calendarId = 1L;
        Calendar calendar = Calendar.builder()
                .member(member)
                .title("제주 여행 2박 3일")
                .startDate(LocalDate.of(2021, 8, 1))
                .endDate(LocalDate.of(2021, 8, 3))
                .area(Area.JEJU)
                .build();
        when(calendarRepository.findById(any())).thenReturn(Optional.of(calendar));

        // Action
        CalendarResponseDto result = calendarService.getCalendar(member.getId(), calendarId);

        // Verify
        assertNotNull(result);
        verify(memberRepository).findById(member.getId());  // 멤버 조회 확인
        assertEquals(calendar.getTitle(), result.getTitle());
    }
}