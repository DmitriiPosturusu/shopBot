package shop.shopbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import shop.shopbot.config.Language;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TelegramService {
    public ReplyKeyboard buildKeyboardButtons(Language languageProperties) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton(languageProperties.getButtonMenu());
        keyboardRow.add(keyboardButton);
        keyboardButton = new KeyboardButton(languageProperties.getButtonBag());
        keyboardRow.add(keyboardButton);
        keyboardRows.add(keyboardRow);

        keyboardRow = new KeyboardRow();
        keyboardButton = new KeyboardButton(languageProperties.getButtonSupport());
        keyboardRow.add(keyboardButton);
        keyboardButton = new KeyboardButton(languageProperties.getButtonSetting());
        keyboardRow.add(keyboardButton);
        keyboardRows.add(keyboardRow);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        log.info("buildKeyboardButtons [ KeyboardRows : [" + keyboardRows + "]]");
        return replyKeyboardMarkup;
    }


}
