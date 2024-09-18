package shop.shopbot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import shop.shopbot.model.Product;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT * from products p where p.day_of_week_id = (select day_of_week_id from day_of_week where lower(day_of_week_name_en) like CONCAT('%',:dayOfWeek,'%'))",nativeQuery = true)
    List<Product> findAllByDayOfWeek(String dayOfWeek);


    @Query(value = "select * from products p where p.day_of_week_id=:dayOfWeekId and p.category_id=:categoryId and p.product_available = true",nativeQuery = true)
    List<Product> findAllByDayOfWeekIdAndCategoryId(Long dayOfWeekId, Long categoryId);

    @Query(value = "SELECT * from products p where lower(p.product_name_en) like CONCAT('%',:productName,'%')",nativeQuery = true)
    List<Product> findAllByName(String productName);

    @Query(value = "SELECT * from products p where lower(p.product_label) like CONCAT('%',:label,'%')",nativeQuery = true)
    List<Product> findAllByProductLabelIsContainingIgnoreCase(String label);

    @Query(value = "SELECT * FROM products p WHERE p.category_id=:category_id and p.product_available=true", nativeQuery = true)
    List<Product> findAllByCategories(Long category_id);

    @Query(value = "SELECT category_id FROM products p WHERE p.product_id=:productId", nativeQuery = true)
    Long getCategoryByProductId(Long productId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update products set product_available=:productAvailable where product_id=:productId", nativeQuery = true)
    void updateProductsByProductAvailable(@Param(value = "productId") long productId, @Param(value = "productAvailable") boolean productAvailable);



}
