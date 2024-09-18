package shop.shopbot.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name_en")
    private String productNameEn;

    @Column(name = "product_name_ro")
    private String productNameRo;

    @Column(name = "product_price")
    private BigDecimal productPrice;

    @Column(name = "product_label")
    private String productLabel;

    @Column(name = "product_descr_en", columnDefinition = "TEXT")
    private String productDescriptionEn;

    @Column(name = "product_descr_ro", columnDefinition = "TEXT")
    private String productDescriptionRo;

    @Column(name = "product_available")
    private boolean productAvailable;

    @ManyToOne
    @JoinColumn(name = "day_of_week_id", nullable = false)
    private DayOfWeek dayOfWeekId;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category categories;


    public Product(Long productId) {
        this.productId = productId;
    }
}
