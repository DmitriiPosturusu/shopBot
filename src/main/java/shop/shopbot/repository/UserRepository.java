package shop.shopbot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import shop.shopbot.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT language from users u where u.chat_id=:chatId", nativeQuery = true)
    String getUserLanguage(Long chatId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update users set language=:language where chat_id=:chat_id", nativeQuery = true)
    void updateUserLanguage(@Param(value = "chat_id") Long chat_id, @Param(value = "language") String language);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update users set phone_number=:phone_number where chat_id=:chat_id", nativeQuery = true)
    void updateUserPhoneNumber(@Param(value = "chat_id") Long chat_id, @Param(value = "phone_number") String phoneNumber);


}
