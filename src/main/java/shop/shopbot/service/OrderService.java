package shop.shopbot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import shop.shopbot.config.Language;
import shop.shopbot.model.Order;
import shop.shopbot.model.Product;
import shop.shopbot.model.User;
import shop.shopbot.repository.OrdersRepository;
import shop.shopbot.utility.UtilityService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class OrderService {
    private final OrdersRepository ordersRepository;

    public OrderService(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    public boolean existsByProductAndUserAndStatus(Product productId, User chatId, String status) {
        boolean exist = ordersRepository.existsByProductAndUserAndStatus(productId, chatId, status);
        log.info("existsByProductAndUserAndStatus [ Product : [" + productId + "] , User : [" + chatId + "] , Status : [" + status + "] ; Exist : [" + exist + "]]");
        return exist;
    }

    public List<Order> findAllByUserAndStatusEquals(User user, String status) {
        List<Order> orders = ordersRepository.findAllByUserAndStatusEquals(user, status);
        log.info("findAllByUserAndStatusEquals [ User : [" + user + "] , Status : [" + status + "] ; Orders : [" + orders + "]]");
        return orders;
    }

    public void updateQuantityByOrderId(long orderId, String quantity, BigDecimal finalPrice) {
        log.info("updateQuantityByOrderId [ OrderId : [" + orderId + "] , Quantity : [" + quantity + "]]");

        ordersRepository.updateQuantityByOrderId(orderId, quantity, finalPrice);
    }

    public void updateOrderStatusByOrderId(long userId, String status) {
        log.info("updateStatusByUserId [ UserId : [" + userId + "] , Status : [" + status + "]]");
        ordersRepository.updateOrderStatusByOrderId(userId, status);
    }

    public Order findById(Long orderId) {
        Optional<Order> optionalOrder = ordersRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            log.info("findById [ OrderId : [" + orderId + "] ; Order : [" + order + "]]");
            return order;
        }
        log.info("findById not found Order with id :[" + orderId + "]");
        return null;
    }

    public void deleteById(long orderId) {
        log.info("deleteById [ OrderId : [" + orderId + " ]]");
        ordersRepository.deleteById(orderId);
    }


    public boolean createOrder(User user, Product product, String quantity) {
        String status = "CREATED";
        if (existsByProductAndUserAndStatus(product, user, status)) {
            return false;
        }
        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setStatus(status);
        BigDecimal qnt = new BigDecimal(quantity);
        BigDecimal totalPrice = qnt.multiply(product.getProductPrice());
        order.setTotalPrice(totalPrice);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        ordersRepository.save(order);
        log.info("createOrder [ User : [" + user + "] , Product : [" + product + "] , Quantity : [" + quantity + "] ; Order : [" + order + "]]");
        return true;
    }

    public InlineKeyboardMarkup buildKeyboardOrderPreConfirm(Language languageProperties) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonConfirm(), "confirmOrder");
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonEdit(), "editOrders");
        rowInLine = new ArrayList<>();
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardOrderPreConfirm [ RowsInLine : [" + rowsInLine + "]]");
        return markupInline;
    }

    public InlineKeyboardMarkup buildKeyboardOrderConfirm(Language languageProperties, String orderId, String quantity) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonConfirm(), "confirmProd_" + quantity + "_" + orderId);
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBack(), "backQuant_" + orderId);
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardOrderConfirm [ OrderId : [" + orderId + "] , Quantity : [" + quantity + "] ; RowsInLine : [" + rowsInLine + "]]");
        return markupInline;
    }

    public InlineKeyboardMarkup buildKeyboardProductInOrderEditableList(Language languageProperties, String orderId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonEditQuantity(), "editQuantityProduct_" + orderId);
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonDelete(), "confirmRemoveProd_" + orderId);
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBack(), "editOrders");
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardProductInOrderEditableList [ OrderId : [" + orderId + "] ; RowsInLine : [" + rowsInLine + "]]");
        return markupInline;
    }

    public InlineKeyboardMarkup buildKeyboardOrderEditableList(List<Order> orderList, Language languageProperties) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton;
        List<InlineKeyboardButton> rowInLine;
        for (Order order : orderList) {
            rowInLine = new ArrayList<>();
            String text = UtilityService.getLanguageProduct(order.getProduct(), languageProperties.getLanguage());
            keyboardButton = UtilityService.buildKeyboardButton(text , "editProd_" + order.getOrderId());
            rowInLine.add(keyboardButton);
            rowsInLine.add(rowInLine);
        }
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBack(), "backBag");
        rowInLine = new ArrayList<>();
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardOrderEditableList [ Orders : [" + orderList + "] ; RowsInLine : [" + rowsInLine + "]]");

        return markupInline;
    }

    public InlineKeyboardMarkup buildKeyboardProductInOrderConfirm(Language languageProperties, String orderId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonConfirm(), "removeProd_" + orderId);
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBack(), "editOrders");
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardProductInOrderConfirm [ OrderId : [" + orderId + "] ; RowsInLine : [" + rowsInLine + "]]");
        return markupInline;
    }

    public InlineKeyboardMarkup buildKeyboardQuantityEdit(String orderId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton;
        for (int i = 1; i < 4; i++) {
            keyboardButton = UtilityService.buildKeyboardButton(String.valueOf(i), "editQuantityOrder_" + i + "_" + orderId);
            rowInLine.add(keyboardButton);
        }
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        for (int i = 4; i < 7; i++) {
            keyboardButton = UtilityService.buildKeyboardButton(String.valueOf(i), "editQuantityOrder_" + i + "_" + orderId);
            rowInLine.add(keyboardButton);
        }
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardQuantityEdit [ OrderId : [" + orderId + "] ; RowsInLine : [" + rowsInLine + "]]");

        return markupInline;
    }

    public InlineKeyboardMarkup buildKeyboardQuantityConfirm(Language languageProperties, String orderId, String quantity) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton keyboardButton;
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonConfirm(), "confirmEditQuantity_" + quantity + "_" + orderId);
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        rowInLine = new ArrayList<>();
        keyboardButton = UtilityService.buildKeyboardButton(languageProperties.getButtonBack(), "backBag");
        rowInLine.add(keyboardButton);
        rowsInLine.add(rowInLine);
        markupInline.setKeyboard(rowsInLine);
        log.info("buildKeyboardOrderConfirm [ OrderId : [" + orderId + "] , Quantity : [" + quantity + "] ; RowsInLine : [" + rowsInLine + "]]");
        return markupInline;
    }

}
