package shop.shopbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.shopbot.model.DayOfWeek;

@Repository
public interface DayOfWeekRepository extends JpaRepository<DayOfWeek, Long> {

}
