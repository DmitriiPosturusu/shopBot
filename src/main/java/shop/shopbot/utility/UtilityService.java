package shop.shopbot.utility;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import shop.shopbot.model.Category;
import shop.shopbot.model.Product;

public final class UtilityService {

    public static InlineKeyboardButton buildKeyboardButton(String text, String callbackData) {
        InlineKeyboardButton keyboardButton = new InlineKeyboardButton();
        keyboardButton.setText(text);
        keyboardButton.setCallbackData(callbackData);
        return keyboardButton;
    }


    public static String getLanguageCategory(Category category, String userLanguage) {
        if (userLanguage.equals("ro")) {
            return category.getCategoryNameRo();
        } else
            return category.getCategoryNameEn();
    }

    public static String buildDescriptionLanguage(String language, Product product) {
        if (language.equals("ro")) {
            return product.getProductDescriptionRo();
        } else return product.getProductDescriptionEn();

    }

    public static String getLanguageProduct(Product product, String language) {
        if (language.equals("ro")) {
            return product.getProductNameRo();
        } else {
            return product.getProductNameEn();
        }
    }

}
