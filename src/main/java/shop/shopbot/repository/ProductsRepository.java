package shop.shopbot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import shop.shopbot.model.Product;

import java.util.List;

public interface ProductsRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT * FROM products p WHERE p.category_id=:category_id and p.product_available=true", nativeQuery = true)
    List<Product> findAllByCategories(Long category_id);

    @Query(value = "SELECT category_id FROM products p WHERE p.product_id=:productId", nativeQuery = true)
    Long getCategoryByProductId(Long productId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update products set product_available=:productAvailable where product_id=:productId", nativeQuery = true)
    void updateProductsByProductAvailable(@Param(value = "productId") long productId, @Param(value = "productAvailable") boolean productAvailable);


}
