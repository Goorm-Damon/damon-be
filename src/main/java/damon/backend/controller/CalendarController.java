package damon.backend.controller;

import damon.backend.dto.Result;
import damon.backend.dto.request.calendar.CalendarCreateRequestDto;
import damon.backend.dto.request.calendar.CalendarEditRequestDto;
import damon.backend.dto.request.calendar.CalendarsDeleteRequestDto;
import damon.backend.dto.response.calendar.CalendarCreateResponseDto;
import damon.backend.dto.response.calendar.CalendarEditResponseDto;
import damon.backend.dto.response.calendar.CalendarResponseDto;
import damon.backend.dto.response.calendar.CalendarsResponseDto;
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
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CalendarController {
    private final CalendarService calendarService;


    @PostMapping("/calendar")
    @Operation(summary = "내 일정 등록", description = "내 일정을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "일정 등록 성공")
//    @ApiResponse(responseCode = "400", description = "일정 등록 실패")
    public Result<CalendarCreateResponseDto> createCalendar(@RequestBody CalendarCreateRequestDto calendarCreateRequestDto) {
        // 로그인 구현 후 member Id 추후 수정
        Long memberId = 1L;

        CalendarCreateResponseDto calendarCreateResponseDto = calendarService.createCalendar(memberId, calendarCreateRequestDto);

        return Result.success(calendarCreateResponseDto);
    }

    @Operation(summary = "내 일정 리스트 조회", description = "내 일정리스트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "일정 리스트 조회 성공")
    @GetMapping("/my/calendar")
    public Result<Page<CalendarsResponseDto>> getCalendars(
            @Schema(description = "페이지 번호(0부터 N까지)", defaultValue = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Schema(description = "페이지에 출력할 개수를 입력합니다.", defaultValue = "10")
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
//        TODO: 로그인 구현 후 member Id 추후 수정
        Long memberId = 1L;
        Page<CalendarsResponseDto> calendarPages = calendarService.getCalendars(memberId, page, size);

        return Result.success(calendarPages);
    }

    @Operation(summary = "내 일정 상세 조회", description = "내 일정 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "일정 상세 조회 성공")
    @GetMapping("/my/calendar/{calendarId}")
    public Result<CalendarResponseDto> getCalendar(
            @Schema(description = "조회 할 일정 상세 페이지 ID", example = "1")
            @PathVariable("calendarId") Long calendarId
    ) {
//         TODO: 로그인 구현 후 member Id 추후 수정
        Long memberId = 1L;
        CalendarResponseDto calendarResponseDto = calendarService.getCalendar(memberId, calendarId);

        return Result.success(calendarResponseDto);
    }

    @Operation(summary = "내 일정 상세 수정", description = "내 일정 상세 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "일정 수정 성공")
    @PutMapping("/calendar/{calendarId}")
    public Result<CalendarEditResponseDto> updateCalendar(
            @Schema(description = "수정 할 일정 상세 페이지 ID", example = "1")
            @PathVariable("calendarId") Long calendarId,

            @RequestBody CalendarEditRequestDto calendarEditRequestDto
    ) {
//        TODO: 로그인 구현 후 member Id 추후 수정
        Long memberId = 1L;
        CalendarEditResponseDto calendarEditResponseDto = calendarService.updateCalendar(memberId, calendarId, calendarEditRequestDto);

        return Result.success(calendarEditResponseDto);
    }

    @Operation(summary = "내 일정 삭제", description = "내 일정을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "일정 삭제 성공")
    @DeleteMapping("/calendar/{calendarId}")
    public Result<String> deleteCalendar(
            @Schema(description = "삭제 할 일정 상세 페이지 ID", example = "1")
            @PathVariable("calendarId") Long calendarId
    ) {
//        TODO: 로그인 구현 후 member Id 추후 수정
        Long memberId = 1L;
        calendarService.deleteCalendar(memberId, calendarId);

        return Result.success("내 일정 삭제 성공");
    }

    @Operation(summary = "내 일정 선택 삭제", description = "내 일정을 선택 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "일정 선택 삭제 성공")
    @DeleteMapping("/calendar")
    public Result<String> deleteCalendars(
            @RequestBody CalendarsDeleteRequestDto calendarsDeleteRequestDto
    ) {
//        TODO: 로그인 구현 후 member Id 추후 수정
        Long memberId = 1L;
        calendarService.deleteCalendars(memberId, calendarsDeleteRequestDto);

        return Result.success("내 일정 선택 삭제 성공");
    }
}