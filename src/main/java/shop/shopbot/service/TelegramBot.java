package shop.shopbot.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import shop.shopbot.config.BotConfig;
import shop.shopbot.config.Language;
import shop.shopbot.config.LanguageEn;
import shop.shopbot.config.LanguageRo;
import shop.shopbot.model.Order;
import shop.shopbot.model.User;
import shop.shopbot.utility.UtilityService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final AmazonS3 s3Client;


    private final BotConfig config;

    private final LanguageEn languageEn;
    private final LanguageRo languageRo;

    private final JobLauncher jobLauncher;
    private final Job csvImporterJob;

    private final UserService userService;

    private final OrderService orderService;

    private final ProductService productService;

    private final CategoryService categoryService;

    private final TelegramService telegramService;


    public TelegramBot(BotConfig config, LanguageEn languageEn, LanguageRo languageRo, JobLauncher jobLauncher, Job csvImporterJob, UserService userService, OrderService orderService, ProductService productService, CategoryService categoryService, TelegramService telegramService, AmazonS3 s3Client) {
        this.config = config;
        this.languageEn = languageEn;
        this.languageRo = languageRo;
        this.jobLauncher = jobLauncher;
        this.csvImporterJob = csvImporterJob;
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "Get a welcome message"));
        botCommandList.add(new BotCommand("/menu", "Get a menu"));
        botCommandList.add(new BotCommand("/shop", "Get your shopping bag"));
        botCommandList.add(new BotCommand("/setting", "Get settings"));
        botCommandList.add(new BotCommand("/help", "Get helped"));
        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Exception :", e);
        }

        this.categoryService = categoryService;
        this.telegramService = telegramService;
        this.s3Client = s3Client;
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotKey();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            log.info("Received message from UserId : [" + chatId + "] , Message : [" + message + "]]");

            if (message.startsWith("/send")) {
                var owner = userService.findById(chatId);
                if (owner != null && owner.getPrivilege() > 0) {
                    String msg = message.substring(message.indexOf(" "));
                    var users = userService.findAll();
                    for (User user : users) {
                        sendMessage(user.getChatId(), msg);
                    }
                }
            } else if (message.startsWith("/sendme")) {
                sendMessage(chatId, message.substring(message.indexOf(" ")));
            } else if (message.startsWith("+44")) {
                updateUserPhoneNumber(chatId, message);
                settingCommandReceived(chatId);
            }


            switch (message) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "Pagina principala", "Main":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/menu", "Meniu", "Menu":
                    menuCommandReceived(chatId);
                    break;
                case "/shop", "Cos de cumparaturi", "Shopping bag":
                    shoppingCommandReceived(chatId);
                    break;
                case "/setting", "Setting":
                    settingCommandReceived(chatId);
                    break;
                case "/help", "Support":
                    helpCommandReceived(chatId);
                    break;
                case "/admin":
                    adminCommandReceived(chatId);
                    break;
                case "/import":
                    runBatchJobImportProductFromCsv(chatId);
                    adminCommandReceived(chatId);
                    break;
                case "/importPictures":
                    importPicturesFromAws(chatId);
                    break;
                default:
                    //to do

            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            var callbackId = update.getCallbackQuery().getId();
            var chatId = update.getCallbackQuery().getMessage().getChatId();

            log.info("Received update from UserId :[" + chatId + "] , CallBackData : [" + callbackData + "] , CallBackId :[" + callbackId + "]]");

            if (callbackData.startsWith("category")) {
                buildProductsList(chatId, callbackId, callbackData);
            } else if (callbackData.startsWith("product")) {
                buyProduct(chatId, callbackId, callbackData);
            } else if (callbackData.startsWith("backCategory")) {
                sendMessageCallback(callbackId, "Loading…", false);
                menuCommandReceived(chatId);
            } else if (callbackData.startsWith("backProd")) {
                buildCategoryByProduct(chatId, callbackId, callbackData);
            } else if (callbackData.startsWith("buy")) {
                buildQuantity(chatId, callbackData, callbackId);
            } else if (callbackData.startsWith("quantity")) {
                confirmQuantityOrders(chatId, callbackId, callbackData);
            } else if (callbackData.startsWith("confirmProd")) {
                buildOrder(chatId, callbackData, callbackId);
            } else if (callbackData.startsWith("backQuant")) {
                buildQuantity(chatId, callbackData, callbackId);
            } else if (callbackData.startsWith("editOrders")) {
                buildEditableListOrders(chatId, callbackId);
            } else if (callbackData.startsWith("editProd")) {
                buildEditableListProducts(chatId, callbackData, callbackId);
            } else if (callbackData.startsWith("backBag")) {
                sendMessageCallback(callbackId, "Loading…", false);
                shoppingCommandReceived(chatId);
            } else if (callbackData.startsWith("confirmRemoveProd")) {
                confirmProductFromOrder(chatId, callbackData, callbackId);
            } else if (callbackData.startsWith("removeProd")) {
                removeProductFromOrder(chatId, callbackData, callbackId);
                shoppingCommandReceived(chatId);
            } else if (callbackData.startsWith("editQuantityProduct")) {
                editQuantityProduct(chatId, callbackData, callbackId);
            } else if (callbackData.startsWith("editQuantityOrder")) {
                confirmQuantityProduct(chatId, callbackData, callbackId);
            } else if (callbackData.startsWith("confirmEditQuantity")) {
                updateOrder(chatId, callbackData, callbackId);
                shoppingCommandReceived(chatId);
            } else if (callbackData.startsWith("confirmOrder")) {
                confirmQuantityOrder(chatId, callbackId);
                startCommandReceived(chatId, update.getCallbackQuery().getMessage().getChat().getFirstName());
            } else if (callbackData.startsWith("setLanguageEn")) {
                updateUserLanguage(chatId, "en", callbackId);
                settingCommandReceived(chatId);
            } else if (callbackData.startsWith("setLanguageRo")) {
                updateUserLanguage(chatId, "ro", callbackId);
                settingCommandReceived(chatId);
            } else if (callbackData.startsWith("editAvailableProducts")) {
                editProducts(chatId, callbackData, callbackId);
            } else if (callbackData.startsWith("enableProduct")) {
                enableProduct(chatId, callbackData, callbackId);
                adminCommandReceived(chatId);
            } else if (callbackData.startsWith("disableProduct")) {
                disableProduct(chatId, callbackData, callbackId);
                adminCommandReceived(chatId);
            } else if (callbackData.startsWith("setPhoneNumber")) {
                setPhoneNumberCallback(chatId, callbackId);
            } else if (callbackData.startsWith("getWeekOffer") || callbackData.startsWith("backWeekOffer")) {
                buildWeekOffer(chatId, callbackId);
            }


        }

        if (update.hasMessage() && update.getMessage().hasDocument()) {
            //To do
            //check if is admin
            var document = update.getMessage().getDocument();
            if (document.getMimeType().equals("text/csv")) {
                GetFile getFile = new GetFile();
                getFile.setFileId(document.getFileId());
                try {
                    String filePath = execute(getFile).getFilePath();
                    File outputFile = new File("tmpProducts.csv");
                    boolean status = outputFile.createNewFile();
                    log.info("New tmp file was created : [" + status + "] , in location [" + outputFile.getPath() + "]");
                    downloadFile(filePath, outputFile);
                    sendMessage(update.getMessage().getChatId(), "Success");
                } catch (Exception e) {
                    log.error("Exception :", e);
                }
            }

        }
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            //TODO
            //check if is admin
            var photos = update.getMessage().getPhoto();
            if (photos.isEmpty()) {
                return;
            }
            var photo = photos.get(3);
            String fileName = update.getMessage().getCaption() + ".png";
            GetFile getFile = new GetFile(photo.getFileId());
            try {
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                downloadFile(file, new File(fileName));

            } catch (TelegramApiException e) {
                log.error("Exception :", e);
            }

        }

    }

    private void importPicturesFromAws(long chatId) {
        ObjectListing objectListing = s3Client.listObjects(config.getBucketName());
        List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        log.info("Bucket with name :[" + config.getBucketName() + "] have : [" + objectSummaries.toString() + "]");
        if (objectSummaries.isEmpty()) {
            sendMessage(chatId, "Folder is empty");
        }
        for (S3ObjectSummary objectSummary : objectSummaries) {
            S3Object s3Object = s3Client.getObject(config.getBucketName(), objectSummary.getKey());
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            try (FileOutputStream fos = new FileOutputStream(objectSummary.getKey())) {
                fos.write(inputStream.readAllBytes());
                log.info("Create file with name : [" + objectSummary.getKey() + "]");
            } catch (IOException e) {
                log.error("Exception :", e);
            }
        }
        sendMessage(chatId, "Success");

    }

    private void buildWeekOffer(Long chatId, String callbackId) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        InlineKeyboardMarkup markupInline = buildWeekOfferKeyboard(languageProperties, true);
        String answer = languageProperties.getWeekDays();
        sendMessageKeyboard(chatId, answer, markupInline);
        sendMessageCallback(callbackId, "Loading…", false);
    }

    private InlineKeyboardMarkup buildWeekOfferKeyboard(Language languageProperties, boolean daysOfWeek) {
        var categories = categoryService.findAllByDayOfWeek(daysOfWeek);
        return categoryService.buildKeyboardMenu(categories, languageProperties, daysOfWeek);

    }

    private void updateUserPhoneNumber(long chatId, String message) {
        userService.updateUserPhoneNumber(chatId, message);
    }

    private void setPhoneNumberCallback(Long chatId, String callbackId) {
        sendMessageCallback(callbackId, "Loading…", false);
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        sendMessage(chatId, languageProperties.getTextPhoneNumber());
    }


    private void runBatchJobImportProductFromCsv(long chatId) {
        //TODO
        //check if is admin
        try {
            //

            jobLauncher.run(csvImporterJob, new JobParameters());
            sendMessage(chatId, "Success");
        } catch (Exception e) {
            log.error("Exception :", e);
            sendMessage(chatId, "Failed");
        }
    }

    private void disableProduct(Long chatId, String callbackData, String callbackId) {
        //to do
        //check if is admin

        String productId = getIdFromCallback(callbackData);
        productService.updateProductsByProductAvailable(Long.parseLong(productId), false);
        sendMessageCallback(callbackId, "Success", true);
    }

    private void enableProduct(Long chatId, String callbackData, String callbackId) {
        //to do
        //check if is admin

        String productId = getIdFromCallback(callbackData);
        productService.updateProductsByProductAvailable(Long.parseLong(productId), true);
        sendMessageCallback(callbackId, "Success", true);

    }

    private void editProducts(Long chatId, String callbackData, String callbackId) {
        //to do
        //check if is admin
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String productId = getIdFromCallback(callbackData);
        InlineKeyboardMarkup markupInline = productService.buildKeyboardProductEditAdmin(languageProperties, productId);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getAdminCommandEdit();
        sendMessageKeyboard(chatId, answer, markupInline);
    }


    private void adminCommandReceived(long chatId) {
        //to do
        //check if is admin
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        var productList = productService.findAll();
        InlineKeyboardMarkup markupInline = productService.buildKeyboardProductListAdmin(productList, language);
        String answer = languageProperties.getAdminCommand();
        sendMessageKeyboard(chatId, answer, markupInline);
    }

    private boolean checkUserIsAdmin(Long chatId) {
        var user = userService.findById(chatId);
        return user.getPrivilege() > 0;
    }

    private String getIdFromCallback(String callbackData) {
        return callbackData.substring(callbackData.lastIndexOf("_") + 1);
    }

    private void updateUserLanguage(Long chatId, String language, String callbackId) {
        userService.updateUserLanguage(chatId, language);
        sendMessageCallback(callbackId, "Success", false);
    }

    private void helpCommandReceived(Long chatId) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String answer = languageProperties.getTextSupport();
        sendMessage(chatId, answer);
    }

    private void settingCommandReceived(Long chatId) {
        var user = userService.findById(chatId);
        InlineKeyboardMarkup markupInline = userService.buildKeyboardSetting();
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);

        String answer = languageProperties.getSettingCommand() + " " + user.getLanguage().toUpperCase() + " " + languageProperties.getSettingPhoneNumber() + " " + user.getPhoneNumber();
        sendMessageKeyboard(chatId, answer, markupInline);

    }

    private void confirmQuantityOrder(Long chatId, String callback_id) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);

        if (userService.findById(chatId).getPhoneNumber().startsWith("+44")) {
            orderService.updateStatusByOrderId(chatId, "FINISHED");
            sendMessageCallback(callback_id, "Success", true);
        } else {

            String answer = languageProperties.getAlertPhoneNumber();
            sendMessageCallback(callback_id, answer, true);
        }

    }

    private void updateOrder(Long chatId, String callbackData, String callback_id) {
        String orderId = getIdFromCallback(callbackData);
        String quantity = getQuantityFromCallback(callbackData);
        var order = orderService.findById(Long.valueOf(orderId));
        BigDecimal finalPrice = order.getProduct().getProductPrice().multiply(new BigDecimal(quantity));
        orderService.updateQuantityByOrderId(Long.parseLong(orderId), quantity, finalPrice);
        sendMessageCallback(callback_id, "Loading…", false);
    }

    private String getQuantityFromCallback(String callbackData) {
        return callbackData.substring(callbackData.indexOf("_") + 1, callbackData.lastIndexOf("_"));
    }

    private void confirmQuantityProduct(Long chatId, String callbackData, String callbackId) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String orderId = getIdFromCallback(callbackData);
        String quantity = getQuantityFromCallback(callbackData);
        InlineKeyboardMarkup markupInline = orderService.buildKeyboardQuantityConfirm(languageProperties, orderId, quantity);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getConfirmQuantity();
        sendMessageKeyboard(chatId, answer, markupInline);
    }


    private void editQuantityProduct(Long chatId, String callbackData, String callbackId) {
        String orderId = getIdFromCallback(callbackData);
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        InlineKeyboardMarkup markupInline = orderService.buildKeyboardQuantityEdit(orderId);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getEditQuantity();
        sendMessageKeyboard(chatId, answer, markupInline);

    }

    private void confirmProductFromOrder(Long chatId, String callbackData, String callbackId) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String orderId = getIdFromCallback(callbackData);
        InlineKeyboardMarkup markupInline = orderService.buildKeyboardProductInOrderConfirm(languageProperties, orderId);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getConfirmProductRemove();
        sendMessageKeyboard(chatId, answer, markupInline);
    }

    private void removeProductFromOrder(Long chatId, String callbackData, String callback_id) {
        String orderId = getIdFromCallback(callbackData);
        orderService.deleteById(Long.parseLong(orderId));
        sendMessageCallback(callback_id, "Loading…", false);
        //editMsg


    }

    private void buildEditableListProducts(Long chatId, String callbackData, String callbackId) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String orderId = getIdFromCallback(callbackData);
        InlineKeyboardMarkup markupInline = orderService.buildKeyboardProductInOrderEditableList(languageProperties, orderId);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getEditProducts();
        sendMessageKeyboard(chatId, answer, markupInline);
    }

    private void buildEditableListOrders(Long chatId, String callbackId) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        var user = userService.findById(chatId);
        var orderList = orderService.findAllByUserAndStatusEquals(user, "CREATED");
        InlineKeyboardMarkup markupInline = orderService.buildKeyboardOrderEditableList(orderList, languageProperties);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getEditOrders();
        sendMessageKeyboard(chatId, answer, markupInline);

    }


//    private void runTest(long chatId, Integer messageId) {
//
//
//        EditMessageText editMessageText = new EditMessageText();
//        editMessageText.setChatId(chatId);
//        editMessageText.setMessageId(messageId);
//    }

    private void confirmQuantityOrders(Long chatId, String callbackId, String callbackData) {
        String orderId = getIdFromCallback(callbackData);
        String quantity = getQuantityFromCallback(callbackData);
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        InlineKeyboardMarkup markupInline = orderService.buildKeyboardOrderConfirm(languageProperties, orderId, quantity);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getConfirmQuantityOrders() + " : " + quantity;
        sendMessageKeyboard(chatId, answer, markupInline);

    }

    private void shoppingCommandReceived(Long chatId) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        var user = userService.findById(chatId);
        var ordersList = orderService.findAllByUserAndStatusEquals(user, "CREATED");
        StringBuilder text = new StringBuilder();
        if (ordersList.isEmpty()) {
            String answer = languageProperties.getShoppingCommandEmpty();
            sendMessage(chatId, answer);
            return;
        }
        BigDecimal finalPrice = new BigDecimal(0);
        for (Order order : ordersList) {
            var product = order.getProduct();
            finalPrice = finalPrice.add(order.getTotalPrice());
            //TODO
            //build text
            text.append(UtilityService.getLanguageProduct(product, user.getLanguage())).append(" nr: ").append(order.getQuantity()).append(" price : ").append(order.getTotalPrice()).append("\n");
        }
        text.append(languageProperties.getFinalPrice()).append(finalPrice);
        InlineKeyboardMarkup markupInline = orderService.buildKeyboardOrderPreConfirm(languageProperties);
        sendMessageKeyboard(chatId, text.toString(), markupInline);
    }


    private void buildOrder(Long chatId, String callbackData, String callbackId) {
        String productId = getIdFromCallback(callbackData);
        String quantity = getQuantityFromCallback(callbackData);
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        if (createOrder(chatId, productId, quantity)) {
            sendMessageCallback(callbackId, languageProperties.getAlertOrder(), true);
        } else {
            sendMessageCallback(callbackId, languageProperties.getAlertOrderExist(), true);
        }
        String answer = languageProperties.getTextBuildOrder();
        sendMessage(chatId, answer);
    }

    private boolean createOrder(Long chatId, String productId, String quantity) {
        var user = userService.findById(chatId);
        var product = productService.findById(Long.parseLong(productId));
        return orderService.createOrder(user, product, quantity);

    }

    private void buildQuantity(Long chatId, String callbackData, String callbackId) {
        String productId = getIdFromCallback(callbackData);
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        InlineKeyboardMarkup markupInline = productService.buildKeyboardProductQuantity(productId, languageProperties);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getTextQuantity();
        sendMessageKeyboard(chatId, answer, markupInline);
    }

    private void buildCategoryByProduct(Long chatId, String callbackId, String callbackData) {
        String productId = getIdFromCallback(callbackData);
        var categoryId = productService.getCategoryByProductId(Long.parseLong(productId));
        var productList = productService.findAllByCategories(categoryId);
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        InlineKeyboardMarkup markupInline = productService.buildKeyboardProductByCategory(productList, languageProperties);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getCategoryByProduct();
        sendMessageKeyboard(chatId, answer, markupInline);
    }

    private void buyProduct(Long chatId, String callbackId, String callbackData) {
        String productId = getIdFromCallback(callbackData);
        var user = userService.findById(chatId);
        var product = productService.findById(Long.parseLong(productId));
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        InlineKeyboardMarkup markupInline = productService.buildKeyboardProductButton(productId, languageProperties);
        String text = UtilityService.buildDescriptionLanguage(user.getLanguage(), product);
        text = text.replaceAll("/n",System.lineSeparator());
        sendPhoto(chatId, callbackData);
        sendMessageCallback(callbackId, "Loading…", false);
        sendMessageKeyboard(chatId, text, markupInline);

    }

    private void buildProductsList(Long chatId, String callback_id, String callbackData) {
        String idCategory = getIdFromCallback(callbackData);
        var productList = productService.findAllByCategories(Long.parseLong(idCategory));
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        if (productList.isEmpty()) {
            String answer = languageProperties.getProductListCommandEmpty();
            sendMessageCallback(callback_id, answer, true);
            return;
        }
        String answer = languageProperties.getProductListCommand();
        InlineKeyboardMarkup markupInline = productService.buildKeyboardProducts(productList, languageProperties);
        sendMessageCallback(callback_id, "Loading…", false);
        sendMessageKeyboard(chatId, answer, markupInline);
    }


    private void menuCommandReceived(long chatId) {
        var categoryList = categoryService.findAllByDayOfWeek(false);
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String answer = languageProperties.getMenuCommand();
        boolean daysOfWeek = false;
        InlineKeyboardMarkup markupInline = categoryService.buildKeyboardMenu(categoryList, languageProperties, daysOfWeek);
        sendMessageKeyboard(chatId, answer, markupInline);
    }

    public Language getLanguageProperties(String language) {
        if (language.equals("en")) {
            return languageEn;
        } else {
            return languageRo;
        }
    }


    private void registerUser(Message message) {
        userService.registerUser(message);
    }

    private void startCommandReceived(long chatId, String firstName) {
        var language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String answer = languageProperties.getStartCommand() + " " + firstName;
        ReplyKeyboard replyKeyboardMarkup = telegramService.buildKeyboardButtons(languageProperties);
        sendMessageKeyboard(chatId, answer, replyKeyboardMarkup);

    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(message);
            log.info("Send message : [" + message + "] , to UserId : [" + chatId + "]");
        } catch (TelegramApiException e) {
            log.error("Exception :", e);
        }
    }

    private void sendPhoto(long chatId, String productId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        String fileName = productId + ".png";
        File file = new File(fileName);
        sendPhoto.setPhoto(new InputFile(file));
        try {
            execute(sendPhoto);
            log.info("Send photo : [ ] , to UserId : [" + chatId + "]");
        } catch (TelegramApiException e) {
            log.error("Exception :", e);
        }
    }

    private void sendMessageKeyboard(long chatId, String text, ReplyKeyboard markupInline) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setReplyMarkup(markupInline);
        try {
            execute(message);
            log.info("Send message : " + message + ", to UserId : [" + chatId + "]");
        } catch (TelegramApiException e) {
            log.error("Exception :", e);
        }
    }

    private void sendMessageCallback(String callback_id, String txt, boolean alert) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder().callbackQueryId(callback_id).text(txt).showAlert(alert).build();
        try {
            execute(answer);
            log.info("Send callback message  : to CallBackId : [" + callback_id + "] , answer : [" + answer + "]");
        } catch (TelegramApiException e) {
            log.error("Exception :", e);
        }
    }
}
