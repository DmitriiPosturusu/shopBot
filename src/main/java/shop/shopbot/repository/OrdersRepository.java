package shop.shopbot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import shop.shopbot.model.Order;
import shop.shopbot.model.Product;
import shop.shopbot.model.User;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Order, Long> {

    boolean existsByProductAndUserAndStatus(Product productId, User chatId, String status);


    List<Order> findAllByUserAndStatusEquals(User user, String status);


    //clear cache
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update orders set quantity=:quantity,total_price=:totalPrice where order_id=:orderId", nativeQuery = true)
    void updateQuantityByOrderId(@Param(value = "orderId") long orderId, @Param(value = "quantity") String quantity, @Param(value = "totalPrice") BigDecimal totalPrice);


    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update orders set status=:status, finished_at=now() where order_id=:orderId", nativeQuery = true)
    void updateOrderStatusByOrderId(@Param(value = "orderId") long orderId, @Param(value = "status") String status);


}
