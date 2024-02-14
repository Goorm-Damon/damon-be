package damon.backend.dto.response.calendar;

import damon.backend.entity.Travel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TravelDetailDto {
    private Long travelId;
    private int day;
    private String locationName;
    private String latitude;
    private String longitude;
    private String memo;
    private int order;

    public static TravelDetailDto form(Travel travel) {
        return new TravelDetailDto(
                travel.getId(),
                travel.getTravelDay(),
                travel.getLocationName(),
                travel.getLatitude(),
                travel.getLongitude(),
                travel.getMemo(),
                travel.getOrderNumber()
        );
    }

    public static List<TravelDetailDto> listFrom(List<Travel> travels) {
        return travels.stream().map(TravelDetailDto::form).toList();
    }
}
