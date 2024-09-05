package shop.shopbot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name_en")
    private String categoryNameEn;

    @Column(name = "category_name_ro")
    private String categoryNameRo;

    @Column(name = "category_day_of_week")
    private Boolean dayOfWeek;

    public Category(Long categoryId) {
        this.categoryId = categoryId;
    }


}
