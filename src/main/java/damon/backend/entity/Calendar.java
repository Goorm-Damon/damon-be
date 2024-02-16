package damon.backend.entity;

import damon.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "calendar")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Calendar extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private Area area;

    @OneToMany(mappedBy = "calendar")
    private List<Travel> travels = new ArrayList<>();

    @Builder
    public Calendar(User user, String title, LocalDate startDate, LocalDate endDate, Area area) {
        this.user = user;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.area = area;
    }
    public void update(String title, LocalDate startDate, LocalDate endDate, Area area) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.area = area;
    }

    public void addTravel(Travel travel){
        this.travels.add(travel);
        if (travel.getCalendar() != this) {
            travel.setCalendar(this);
        }
    }
}