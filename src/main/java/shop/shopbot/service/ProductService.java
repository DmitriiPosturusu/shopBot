package shop.shopbot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import shop.shopbot.config.Language;
import shop.shopbot.model.Product;
import shop.shopbot.repository.ProductsRepository;
import shop.shopbot.utility.UtilityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProductService {

    private final ProductsRepository productsRepository;

    public ProductService(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }


    public List<Product> findAllByCategories(long categoryId) {
        return productsRepository.findAllByCategories(categoryId);
    }

    public List<Product> findAllByLabel(String label) {
        return productsRepository.findAllByProductLabelIsContainingIgnoreCase(label);
    }

    public Long getCategoryByProductId(long productId) {
        return productsRepository.getCategoryByProductId(productId);
    }

    public void updateProductsByProductAvailable(long productId, boolean productAvailable) {
        log.info("updateProductsByProductAvailable [ ProductId : [" + productId + "] , ProductAvailable : [" + productAvailable + "]");
        productsRepository.updateProductsByProductAvailable(productId, productAvailable);
    }

    public Product findById(long productId) {
        Optional<Product> optionalProduct = productsRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            log.info("findById [ ProductId : [" + productId + "] ; Product : [" + product + "]]");
            return product;
        }
        log.info("findById not found Product with id :[" + productId + "]");
        return null;
    }

    public List<Product> findAll() {
        List<Product> products = productsRepository.findAll();
        log.info("findAll [ Products : [" + products + "]");
        return products;
    }

    public InlineKeyboardMarkup buildKeyboardProducts(List<Product> productList, Language languageProperties, String callbackData) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine;
        InlineKeyboardButton keyboardButton;

        for (Product product : productList) {
            rowInLine = new ArrayList<>();
            String name = UtilityService.getLanguageProduct(product, languageProperties.getLanguage());
            keyboardButton = UtilityService.buildKeyboardButton(name, "product_" + product.getProductId());
            rowInLine.add(keyboardButton);
            rowsInLine.add(rowInLine);
        }
        rowInLine = new ArrayList<>();
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBack(), "day_of_week_"+callbackData);
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardProducts [ Products : [" + productList + "] ; RowsInLine : [" + rowsInLine + "]]");
        return markupInline;
    }

    public InlineKeyboardMarkup buildKeyboardProductButton(Product product, Language languageProperties) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton;
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBuy(), "buy_" + product.getProductId());
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBack(), "category_" + product.getCategories().getCategoryId()+"_day_of_week_"+product.getDayOfWeekId().getDayOfWeekId());
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardProductButton [ ProductId : [" + product.getProductId() + "] ; RowsInLine : [" + rowsInLine + "]]");

        return markupInline;
    }

    public InlineKeyboardMarkup buildKeyboardProductList(List<Product> productList, Language languageProperties) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton;
        for (Product product : productList) {
            List<InlineKeyboardButton> rowInLine = new ArrayList<>();
            String name = UtilityService.getLanguageProduct(product, languageProperties.getLanguage());
            keyboardButton = UtilityService.buildKeyboardButton(name, "product_" + product.getProductId());
            rowInLine.add(keyboardButton);
            rowsInLine.add(rowInLine);
        }
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardProductList [ Products : [" + productList + "] ; RowsInLine : [" + rowsInLine + "]]");

        return markupInline;
    }


    public InlineKeyboardMarkup buildKeyboardProductQuantity(String productId, Language languageProperties) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton;
        for (int i = 1; i < 4; i++) {
            keyboardButton = UtilityService.buildKeyboardButton(String.valueOf(i), "quantity_" + i + "_" + productId);
            rowInLine.add(keyboardButton);
        }
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        for (int i = 4; i < 7; i++) {
            keyboardButton = UtilityService.buildKeyboardButton(String.valueOf(i), "quantity_" + i + "_" + productId);
            rowInLine.add(keyboardButton);
        }
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBack(), "product_" + productId);
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardProductQuantity [ ProductId : [" + productId + "] ; RowsInLine : [" + rowsInLine + "]]");

        return markupInline;
    }


    public InlineKeyboardMarkup buildKeyboardProductListAdmin(List<Product> productList, String language) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        for (Product product : productList) {
            List<InlineKeyboardButton> rowInLine = new ArrayList<>();
            InlineKeyboardButton keyboardButton = UtilityService.buildKeyboardButton(UtilityService.getLanguageProduct(product, language), "editAvailableProducts_" + product.getProductId());
            rowInLine.add(keyboardButton);
            rowsInLine.add(rowInLine);
        }
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardProductListAdmin [ Products : [" + productList + "] ; RowsInLine : [" + rowsInLine + "]]");
        return markupInline;
    }

    public InlineKeyboardMarkup buildKeyboardProductEditAdmin(Language languageProperties, String productId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonAdminEnable(), "enableProduct_" + productId);
        rowInLine.add(keyboardButton);
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonAdminDisable(), "disableProduct_" + productId);
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardProductEditAdmin [ ProductId : [" + productId + "] ; RowsInLine : [" + rowsInLine + "]]");
        return markupInline;
    }

    public List<Product> findAllByDay(String dayOfWeek) {
        return productsRepository.findAllByDayOfWeek(dayOfWeek);
    }

    public List<Product> findAllByName(String productName) {
        return productsRepository.findAllByName(productName);
    }

    public List<Product> findAllByDayAndCategory(Long dayOfWeekId, Long categoryId) {
        return productsRepository.findAllByDayOfWeekIdAndCategoryId(dayOfWeekId, categoryId);
    }
}
