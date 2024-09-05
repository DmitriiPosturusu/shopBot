package shop.shopbot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import shop.shopbot.config.Language;
import shop.shopbot.model.Category;
import shop.shopbot.repository.CategoryRepository;
import shop.shopbot.utility.UtilityService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    public List<Category> findAll() {
        List<Category> categories = categoryRepository.findAll();
        log.info("findAllCategory : [ " + categories + " ]");
        return categories;
    }


    public List<Category> findAllByDayOfWeek(boolean dayOfWeek) {
        List<Category> categories = categoryRepository.findAllByDayOfWeek(dayOfWeek);
        log.info("findAllByDayOfWeek : [ " + categories + " ] , dayOfWeek : [" + dayOfWeek + "] ]");
        return categories;
    }


    public InlineKeyboardMarkup buildKeyboardMenu(List<Category> categoryList, Language languageProperties, boolean daysOfWeek) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton;
        if (!daysOfWeek) {
            keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getWeekOffer(), "getWeekOffer");
            rowInLine.add(keyboardButton);
            rowsInLine.add(rowInLine);
        }


        for (Category category : categoryList) {
            rowInLine = new ArrayList<>();
            String name = UtilityService.getLanguageCategory(category, languageProperties.getLanguage());
            String callbackData = "category_" + category.getCategoryId();
            keyboardButton = UtilityService.buildKeyboardButton(name, callbackData);
            rowInLine.add(keyboardButton);
            rowsInLine.add(rowInLine);
        }


        if (daysOfWeek){
            rowInLine = new ArrayList<>();
            keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBack(), "backCategory");
            rowInLine.add(keyboardButton);
            rowsInLine.add(rowInLine);
        }


        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardMenuCategory [ Categories : [ " + categoryList + " ] , UserLanguage : [" + languageProperties.getLanguage() + "] ; RowsInLine : [" + rowsInLine + " ]]");
        return markupInline;

    }
}
