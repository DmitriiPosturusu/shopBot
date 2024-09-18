package shop.shopbot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import shop.shopbot.model.Category;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = "select * from categories  where category_day_of_week_ids like CONCAT('%',:dayOfWeek,'%') order by category_id",nativeQuery = true)
    List<Category> findAllByDayOfWeeks(String dayOfWeek);

}
