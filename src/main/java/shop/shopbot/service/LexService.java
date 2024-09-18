package shop.shopbot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import shop.shopbot.config.Language;
import shop.shopbot.model.Product;
import shop.shopbot.model.ProductState;
import software.amazon.awssdk.services.lexruntimev2.LexRuntimeV2Client;
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextRequest;
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextResponse;
import software.amazon.awssdk.services.lexruntimev2.model.Slot;
import software.amazon.awssdk.services.lexruntimev2.model.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LexService {

    private final LexRuntimeV2Client lexV2Client;

    private final ProductService productService;
    private final OrderService orderService;

    private final UserService userService;

    public LexService(LexRuntimeV2Client lexV2Client, ProductService productService, OrderService orderService, UserService userService) {
        this.lexV2Client = lexV2Client;
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }


    public RecognizeTextResponse detectKeyPhrases(String botId, String botAliasId, String localeId, long chatId, String text) {
        RecognizeTextRequest recognizeTextRequest = getRecognizeTextRequest(botId, botAliasId, localeId, String.valueOf(chatId), text);
        return lexV2Client.recognizeText(recognizeTextRequest);


    }

    private RecognizeTextRequest getRecognizeTextRequest(String botId, String botAliasId, String localeId, String sessionId, String userInput) {
        return RecognizeTextRequest.builder()
                .botAliasId(botAliasId)
                .botId(botId)
                .localeId(localeId)
                .sessionId(sessionId)
                .text(userInput)
                .build();
    }

    public InlineKeyboardMarkup getInlineKeyboardByIntentName(String intentName, Map<String, Slot> slots, Language languageProperties) {
        return switch (intentName) {
            case "showAllProduct" -> getMarkupInlineAllProduct(slots, languageProperties);
            case "showProductsByCategory" -> getMarkupInlineProductByCategory(slots, languageProperties);
            case "showAllProductByDayOfWeek" -> getMarkupInlineProductByDayOfWeek(slots, languageProperties);
            case "showProductsByName" -> getMarkupInlineProductByName(slots, languageProperties);
            case "showAllCreatedOrder" -> getMarkupInlineAllCreatedOrder(slots, languageProperties);
            case "addToShoppingCart" -> getMarkupInlineCreateOrder(slots, languageProperties);
            default -> null;
        };

    }

    private InlineKeyboardMarkup getMarkupInlineCreateOrder(Map<String, Slot> slots, Language languageProperties) {
        var slotProduct = slots.get("ProductName").value();
        List<Product> productList;
        if (!slotProduct.resolvedValues().isEmpty()) {
            productList = productService.findAllByName(slotProduct.resolvedValues().get(0).toLowerCase());
        } else {
            productList = productService.findAllByName(slotProduct.interpretedValue().toLowerCase());
        }
        var slotQuantity = slots.get("ProductQuantity").value().interpretedValue();
        if (productList.isEmpty() && slotQuantity.isBlank()) {
            return null;
        } else {
            return productService.buildKeyboardProductList(productList, languageProperties);
        }
    }


    private InlineKeyboardMarkup getMarkupInlineAllCreatedOrder(Map<String, Slot> slots, Language languageProperties) {
        return orderService.buildKeyboardOrderPreConfirm(languageProperties);
    }

    private InlineKeyboardMarkup getMarkupInlineProductByName(Map<String, Slot> slots, Language languageProperties) {
        var productNameSlot = slots.get("ProductName");
        if (productNameSlot == null) {
            return null;
        }
        List<Product> productList = productService.findAllByName(productNameSlot.value().interpretedValue().toLowerCase());

        return productService.buildKeyboardProductList(productList, languageProperties);
    }

    private InlineKeyboardMarkup getMarkupInlineProductByDayOfWeek(Map<String, Slot> slots, Language languageProperties) {
        var slot = slots.get("DayOfWeek");
        List<Product> productList = new ArrayList<>();
        for (Slot value : slot.values()) {
            productList.addAll(productService.findAllByDay(value.value().interpretedValue().toLowerCase()));
        }
        return productService.buildKeyboardProductList(productList, languageProperties);
    }

    private InlineKeyboardMarkup getMarkupInlineProductByCategory(Map<String, Slot> slots, Language languageProperties) {
        var slot = slots.get("Category");
        List<Product> productList = new ArrayList<>();
        for (Slot value : slot.values()) {
            if (!value.value().resolvedValues().isEmpty()) {
                productList.addAll(productService.findAllByLabel(value.value().resolvedValues().get(0).toLowerCase()));
            } else {
                productList.addAll(productService.findAllByLabel(value.value().interpretedValue().toLowerCase()));
            }
        }
        return productService.buildKeyboardProductList(productList, languageProperties);
    }

    private InlineKeyboardMarkup getMarkupInlineAllProduct(Map<String, Slot> slots, Language languageProperties) {
        var productList = productService.findAll();
        return productService.buildKeyboardProductList(productList, languageProperties);
    }

    public ProductState checkProductNameReceived(Map<String, Slot> slots) {
        var slotProduct = slots.get("ProductName").value();

        List<Product> productList = getProductListByName(slotProduct);


        if (productList.isEmpty() || slots.get("ProductQuantity") == null) {
            return ProductState.NOT_FOUND;
        } else if (productList.size() == 1) {
            return ProductState.FOUND;
        } else {
            return ProductState.FOUND_MORE;
        }


    }

    public List<Product> getProductListByName(Value slotProduct) {
        return productService.findAllByName(slotProduct.interpretedValue().toLowerCase());

    }

    public boolean createOrder(Map<String, Slot> slots, long chatId) {
        var slotProduct = slots.get("ProductName").value();
        var slotQuantity = slots.get("ProductQuantity").value().interpretedValue();

        List<Product> productList = getProductListByName(slotProduct);
        if (productList.size() == 1) {
            var user = userService.findById(chatId);
            return orderService.createOrder(user, productList.get(0), slotQuantity);
        }
        return false;

    }
}
