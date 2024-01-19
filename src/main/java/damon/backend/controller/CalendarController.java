package damon.backend.controller;

import damon.backend.dto.request.CalendarCreateRequestDto;
import damon.backend.dto.request.CalendarEditRequestDto;
import damon.backend.dto.response.CalendarCreateResponseDto;
import damon.backend.dto.response.CalendarEditResponseDto;
import damon.backend.dto.response.CalendarResponseDto;
import damon.backend.dto.response.CalendarsResponseDto;
import damon.backend.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "일정 API", description = "일정 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;


    @PostMapping("/calendar")
    @Operation(summary = "내 일정 등록", description = "내 일정을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "일정 등록 성공")
//    @ApiResponse(responseCode = "400", description = "일정 등록 실패")
    public CalendarCreateResponseDto createCalendar(@RequestBody CalendarCreateRequestDto calendarCreateRequestDto) {
        // 로그인 구현 후 member Id 추후 수정
        String memberId = "1";

        return calendarService.createCalendar(memberId, calendarCreateRequestDto);
    }

    @Operation(summary = "내 일정 리스트 조회", description = "내 일정리스트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "일정 리스트 조회 성공")
    @GetMapping("/my/calendar")
    public Page<CalendarsResponseDto> getCalendars(
            @Schema(description = "페이지 번호(0부터 N까지)", defaultValue = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Schema(description = "페이지에 출력할 개수를 입력합니다.", defaultValue = "10")
            @RequestParam(name = "size",defaultValue = "10") int size
    ) {
        // 로그인 구현 후 member Id 추후 수정
        String memberId = "1";
        return calendarService.getCalendars(memberId, page, size);
    }

    @Operation(summary = "내 일정 상세 조회", description = "내 일정 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "일정 상세 조회 성공")
    @GetMapping("/my/calendar/{calendarId}")
    public CalendarResponseDto getCalendar(
            @Schema(description = "조회 할 일정 상세 페이지 ID", example="1")
            @PathVariable("calendarId") Long calendarId
    ) {
        // 로그인 구현 후 member Id 추후 수정
        String memberId = "1";
        return calendarService.getCalendar(memberId, calendarId);
    }

    @Operation(summary = "내 일정 상세 수정", description = "내 일정 상세 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "일정 수정 성공")
    @PutMapping("/calendar/{calendarId}")
    public CalendarEditResponseDto updateCalendar(
            @Schema(description = "수정 할 일정 상세 페이지 ID", example="1")
            @PathVariable("calendarId") Long calendarId,

            @RequestBody CalendarEditRequestDto calendarEditRequestDto) {
        // 로그인 구현 후 member Id 추후 수정
        String memberId = "1";
        return calendarService.updateCalendar(memberId, calendarId, calendarEditRequestDto);
    }

    @Operation(summary = "내 일정 삭제", description = "내 일정을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "일정 삭제 성공")
    @DeleteMapping("/calendar/{calendarId}")
    public void deleteCalendar(
            @Schema(description = "삭제 할 일정 상세 페이지 ID", example="1")
            @PathVariable("calendarId") Long calendarId
    ) {
        // 로그인 구현 후 member Id 추후 수정
        String memberId = "1";
        calendarService.deleteCalendar(memberId, calendarId);
    }
}