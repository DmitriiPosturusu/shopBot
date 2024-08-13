package shop.shopbot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import shop.shopbot.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
