package shop.shopbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shop.shopbot.config.StringListConverter;

import java.util.List;


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


    @Convert(converter = StringListConverter.class)
    @Column(name = "category_day_of_week_ids")
    private List<String> dayOfWeeks;


    public Category(Long categoryId) {
        this.categoryId = categoryId;
    }


}
