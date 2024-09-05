package shop.shopbot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import shop.shopbot.model.User;
import shop.shopbot.repository.UserRepository;
import shop.shopbot.utility.UtilityService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User findById(long chatId) {
        Optional<User> optionalUser = userRepository.findById(chatId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            log.info("findById [ UserId : [" + chatId + "] ; User : [" + user + "]");
            return user;
        }
        log.info("findById not found User with Id : [" + chatId + "]");
        return null;

    }

    public List<User> findAll() {
        List<User> users = userRepository.findAll();
        log.info("findAll [ Users : [" + users + "]]");
        return users;
    }

    public void updateUserPhoneNumber(long chatId, String phoneNumber) {
        log.info("updateUserPhoneNumber [ UserId : [" + chatId + "] , PhoneNumber : [" + phoneNumber + "]]");
        userRepository.updateUserPhoneNumber(chatId, phoneNumber);
    }

    public String getUserLanguage(long chatId) {
        String userLanguage = userRepository.getUserLanguage(chatId);
        log.info("getUserLanguage [ UserId : [" + chatId + "] ; UserLanguage : [" + userLanguage + "]]");
        return userLanguage;
    }

    public void updateUserLanguage(long chatId, String language) {
        log.info("updateUserLanguage [ UserId : [" + chatId + "] , Language : [" + language + "]");
        userRepository.updateUserLanguage(chatId, language);
    }
    public void saveUser(Message message) {
        var chatID = message.getChatId();
        var chat = message.getChat();

        User user = new User();
        user.setChatId(chatID);
        user.setFirstName(chat.getFirstName());
        user.setUserName(chat.getUserName());
        user.setLastName(chat.getLastName());
        user.setPrivilege(0);
        user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
        user.setLanguage("ro");
        user.setPhoneNumber("0");
        log.info("saveUser [ Message : [" + message + "] ; User : [" + user + "]");
        userRepository.save(user);

    }


    public void registerUser(Message message) {
        if (findById(message.getChatId()) == null) {
            saveUser(message);
        }
    }

    public InlineKeyboardMarkup buildKeyboardSetting() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton;
        keyboardButton = UtilityService.buildKeyboardButton("English", "setLanguageEn");
        rowInLine.add(keyboardButton);
        keyboardButton = UtilityService.buildKeyboardButton("Romana", "setLanguageRo");
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        keyboardButton = UtilityService.buildKeyboardButton("Set phone number", "setPhoneNumber");
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardSetting [ RowsInLine : [" + rowsInLine + "]");
        return markupInline;

    }


}
