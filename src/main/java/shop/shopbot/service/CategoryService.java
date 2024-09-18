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


    public List<Category> findAllByDayOfWeek(String dayOfWeek) {
        List<Category> categories = categoryRepository.findAllByDayOfWeeks(dayOfWeek);
        log.info("findAllByDayOfWeek : [ " + categories + " ] , dayOfWeek : [" + dayOfWeek + "] ]");
        return categories;
    }


    public InlineKeyboardMarkup buildKeyboardMenuCategory(List<Category> categoryList, Language languageProperties, String callbackData) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine;
        InlineKeyboardButton keyboardButton;

        for (Category category : categoryList) {
            rowInLine = new ArrayList<>();
            String name = UtilityService.getLanguageCategory(category, languageProperties.getLanguage());
            String callbackDataResponse = "category_" + category.getCategoryId() + "_" + callbackData;
            keyboardButton = UtilityService.buildKeyboardButton(name, callbackDataResponse);
            rowInLine.add(keyboardButton);
            rowsInLine.add(rowInLine);
        }

        rowInLine = new ArrayList<>();
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBack(), "backDayOfWeek");
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);


        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardMenuCategory [ Categories : [ " + categoryList + " ] , UserLanguage : [" + languageProperties.getLanguage() + "] ; RowsInLine : [" + rowsInLine + " ]]");
        return markupInline;

    }
}
