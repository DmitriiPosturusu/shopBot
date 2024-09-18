package shop.shopbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import shop.shopbot.config.Language;
import shop.shopbot.model.DayOfWeek;
import shop.shopbot.repository.DayOfWeekRepository;
import shop.shopbot.utility.UtilityService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DayOfWeekService {

    private final DayOfWeekRepository dayOfWeekRepository;

    public DayOfWeekService(DayOfWeekRepository dayOfWeekRepository) {
        this.dayOfWeekRepository = dayOfWeekRepository;
    }

    public List<DayOfWeek> getAllDaysOfWeek() {
        List<DayOfWeek> dayOfWeekList = dayOfWeekRepository.findAll();
        log.info("getAllDaysOfWeek : [ " + dayOfWeekList + " ]");
        return dayOfWeekList;
    }

    public InlineKeyboardMarkup buildKeyboardMenuDayOfWeek(List<DayOfWeek> daysOfWeekList, Language languageProperties) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine;
        InlineKeyboardButton keyboardButton;

        for (DayOfWeek dayOfWeek : daysOfWeekList) {
            if (dayOfWeek.getDayOfWeekId() == 0) {
                continue;
            }
            rowInLine = new ArrayList<>();
            keyboardButton = UtilityService.buildKeyboardButton(getDayOfWeekName(dayOfWeek, languageProperties.getLanguage()), "day_of_week_" + dayOfWeek.getDayOfWeekId());
            rowInLine.add(keyboardButton);
            rowsInLine.add(rowInLine);

        }

        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardMenuDayOfWeek [ Categories : [ " + daysOfWeekList + " ] , UserLanguage : [" + languageProperties.getLanguage() + "] ; RowsInLine : [" + rowsInLine + " ]]");

        return markupInline;

    }

    private String getDayOfWeekName(DayOfWeek dayOfWeek, String language) {
        if (language.equals("en")) {
            return dayOfWeek.getDayOfWeekNameEn();
        } else {
            return dayOfWeek.getDayOfWeekNameRo();
        }
    }
}
