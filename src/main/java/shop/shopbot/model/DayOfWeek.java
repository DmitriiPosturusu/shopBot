package shop.shopbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "day_of_week")
public class DayOfWeek {

    @Id
    @Column(name = "day_of_week_id")
    private Long dayOfWeekId;

    @Column(name = "day_of_week_name_en")
    private String dayOfWeekNameEn;

    @Column(name = "day_of_week_name_ro")
    private String dayOfWeekNameRo;


    public DayOfWeek(Long dayOfWeekId) {
        this.dayOfWeekId = dayOfWeekId;
    }
}
