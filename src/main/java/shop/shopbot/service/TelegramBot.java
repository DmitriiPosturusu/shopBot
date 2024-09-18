package shop.shopbot.service;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import shop.shopbot.config.*;
import shop.shopbot.model.Order;
import shop.shopbot.model.ProductState;
import shop.shopbot.model.User;
import shop.shopbot.utility.UtilityService;
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextResponse;
import software.amazon.awssdk.services.lexruntimev2.model.Slot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private final LexService lexService;

    private final DayOfWeekService dayOfWeekService;


    public TelegramBot(BotConfig config, LanguageEn languageEn, LanguageRo languageRo, JobLauncher jobLauncher, Job csvImporterJob, UserService userService, OrderService orderService, ProductService productService, CategoryService categoryService, TelegramService telegramService, AmazonS3 s3Client, LexService lexService, DayOfWeekService dayOfWeekService) {
        this.config = config;
        this.languageEn = languageEn;
        this.languageRo = languageRo;
        this.jobLauncher = jobLauncher;
        this.csvImporterJob = csvImporterJob;
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.telegramService = telegramService;
        this.s3Client = s3Client;
        this.lexService = lexService;
        this.dayOfWeekService = dayOfWeekService;

        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/menu", "Get a menu"));
        botCommandList.add(new BotCommand("/start", "Get a welcome message"));
        botCommandList.add(new BotCommand("/shop", "Get your shopping bag"));
        botCommandList.add(new BotCommand("/setting", "Get settings"));
        botCommandList.add(new BotCommand("/help", "Get helped"));
        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Exception :", e);
        }


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
                if (userService.checkIsAdmin(chatId)) {
                    String msg = message.substring(message.indexOf(" "));
                    var users = userService.findAll();
                    for (User user : users) {
                        sendMessage(user.getChatId(), msg);
                    }
                } else {
                    sendMessage(chatId, "Only admin can use send command");
                }
            } else if (message.startsWith("/sendme")) {
                sendMessage(chatId, message.substring(message.indexOf(" ")));
            } else if (message.startsWith("+44")) {
                updateUserPhoneNumber(chatId, message);
                settingCommandReceived(chatId);
            } else if (message.startsWith("@")) {
                aiCommandReceived(chatId, message);
            }


            switch (message) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId);
                    break;
                case "Pagina principala", "Main":
                    startCommandReceived(chatId);
                    break;
                case "/menu", "Meniu", "Menu":
                    menuCommandReceived(chatId);
                    break;
                case "/shop", "Cos de cumparaturi", "Shopping Cart":
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
                case "/importProduct":
                    runBatchJobImportProductFromCsv(chatId);
                    adminCommandReceived(chatId);
                    break;
                case "/importPictures":
                    importPicturesFromAws(chatId);
                    break;
                default:
                    break;

            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            var callbackId = update.getCallbackQuery().getId();
            var chatId = update.getCallbackQuery().getMessage().getChatId();

            log.info("Received update from UserId :[" + chatId + "] , CallBackData : [" + callbackData + "] , CallBackId :[" + callbackId + "]]");

            if (callbackData.startsWith("day_of_week")) {
                buildCategoryByDayOfWeek(chatId, callbackId, callbackData);
            } else if (callbackData.startsWith("category")) {
                buildProductByCategoryAndDayOfWeek(chatId, callbackId, callbackData);
            } else if (callbackData.startsWith("backDayOfWeek")) {
                sendMessageCallback(callbackId, "Loading…", false);
                menuCommandReceived(chatId);
            } else if (callbackData.startsWith("product")) {
                buyProduct(chatId, callbackId, callbackData);
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
                //startCommandReceived(chatId, update.getCallbackQuery().getMessage().getChat().getFirstName());
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
            }

        }

        if (update.hasMessage() && update.getMessage().hasDocument()) {
            long chatId = update.getMessage().getChatId();
            if (userService.checkIsAdmin(chatId)) {
                var document = update.getMessage().getDocument();
                log.info("Received document from UserId :[" + chatId + "] with type : [" + document.getMimeType() + "]");
                if (document.getMimeType().equals("text/csv")) {
                    GetFile getFile = new GetFile();
                    getFile.setFileId(document.getFileId());
                    try {
                        String filePath = execute(getFile).getFilePath();
                        File outputFile = new File("tmpProducts.csv");
                        boolean status = outputFile.createNewFile();
                        log.info("New tmp file was created : [" + status + "] , in location [" + outputFile.getPath() + "]");
                        downloadFile(filePath, outputFile);
                        sendMessage(chatId, "Success");
                    } catch (Exception e) {
                        log.error("Exception :", e);
                    }
                }
            } else {
                sendMessage(chatId, "Only admin can send document");
            }


        }
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            long chatId = update.getMessage().getChatId();
            if (userService.checkIsAdmin(chatId)) {
                var photos = update.getMessage().getPhoto();
                if (photos.isEmpty()) {
                    return;
                }
                //get photo 3 with max resolution
                var photo = photos.get(3);
                String fileName = update.getMessage().getCaption() + ".png";
                GetFile getFile = new GetFile(photo.getFileId());
                try {
                    org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                    downloadFile(file, new File(fileName));
                    log.info("New photo was created in location [" + file + "]");
                } catch (TelegramApiException e) {
                    log.error("Exception :", e);
                }

            } else {
                sendMessage(chatId, "Only admin can send photo");
            }


        }

        if (update.hasPreCheckoutQuery()) {
            //TODO
            var response = update.getPreCheckoutQuery().getOrderInfo();
            var amount = update.getPreCheckoutQuery().getTotalAmount();


            AnswerPreCheckoutQuery answerPreCheckoutQuery = new AnswerPreCheckoutQuery(update.getPreCheckoutQuery().getId(), true);

            try {
                execute(answerPreCheckoutQuery);
            } catch (TelegramApiException e) {
                log.error("Exception :", e);
            }

        }
        if (update.hasMessage() && update.getMessage().hasSuccessfulPayment()) {
            String[] list = update.getMessage().getSuccessfulPayment().getInvoicePayload().split("_");
            for (String orderId : list) {
                orderService.updateOrderStatusByOrderId(Long.parseLong(orderId), "FINISHED");
            }
            log.info("Payment success with payload [" + update.getMessage().getSuccessfulPayment().getInvoicePayload() + "] and info [" + update.getMessage().getSuccessfulPayment().getOrderInfo() + "]");
            System.out.println("Payment success");


        }


    }


    private void aiCommandReceived(long chatId, String message) {
        RecognizeTextResponse recognizeTextResponse = lexService.detectKeyPhrases(config.getBotId(), config.getBotAliasId(), config.getBotLocaleId(), chatId, message);
        log.info("ChatId :[" + chatId + "] , Message :[" + message + "] ,RecognizeTextResponse :[" + recognizeTextResponse + "]");
        if (recognizeTextResponse.sessionState().intent() != null) {
            String intentName = recognizeTextResponse.sessionState().intent().name();
            Map<String, Slot> slots = recognizeTextResponse.sessionState().intent().slots();
            String language = userService.getUserLanguage(chatId);
            Language languageProperties = getLanguageProperties(language);

            if (intentName.equals("FallbackIntent")) {
                var suggestedIntent = recognizeTextResponse.interpretations();
                var suggestedIntentName = suggestedIntent.stream().filter(s -> !s.intent().name().equals("FallbackIntent")).findFirst();
                if (suggestedIntentName.isPresent()) {
                    var intent = suggestedIntentName.get().intent();
                    InlineKeyboardMarkup keyboardMarkup = lexService.getInlineKeyboardByIntentName(intent.name(), intent.slots(), languageProperties);
                    if (keyboardMarkup == null) {
                        sendMessage(chatId, " Sorry I don't get it. ");
                    } else {
                        sendMessageKeyboard(chatId, "Sorry I don't get it ,but here I found something similar", keyboardMarkup);
                    }

                } else {
                    sendMessage(chatId, " Sorry I don't get it. ");
                }

            } else if (intentName.equals("showAllCreatedOrder")) {
                shoppingCommandReceived(chatId);
            } else if (intentName.equals("addToShoppingCart")) {
                ProductState productState = lexService.checkProductNameReceived(slots);
                if (productState.equals(ProductState.NOT_FOUND)) {
                    sendMessage(chatId, "Product not fount or quantity is blank");
                } else if (productState.equals(ProductState.FOUND)) {
                    if (lexService.createOrder(slots, chatId)) {
                        shoppingCommandReceived(chatId);
                    } else {
                        sendMessage(chatId, "Failed to create new order. Product already exist in your Shopping Cart");
                        shoppingCommandReceived(chatId);
                    }

                } else {
                    InlineKeyboardMarkup markupInlineFinal = lexService.getInlineKeyboardByIntentName(intentName, slots, languageProperties);
                    sendMessageKeyboard(chatId, "Found more products : ", markupInlineFinal);
                }

            } else {

                InlineKeyboardMarkup markupInlineFinal = lexService.getInlineKeyboardByIntentName(intentName, slots, languageProperties);
                if (markupInlineFinal.getKeyboard().isEmpty()) {
                    sendMessage(chatId, "Sorry I don't found records based on your search");

                } else {
                    sendMessageKeyboard(chatId, "Your search returned the following: : ", markupInlineFinal);
                }


            }


        }

    }

    private void importPicturesFromAws(long chatId) {
        if (userService.checkIsAdmin(chatId)) {
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
                    sendMessage(chatId, "Failed");
                    log.error("Exception :", e);
                }
            }
            sendMessage(chatId, "Success");
        } else {
            sendMessage(chatId, "Only admin can send importPictures command");
        }


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
        if (userService.checkIsAdmin(chatId)) {
            try {
                JobParameters jobParameters = new JobParametersBuilder()
                        .addString("chatId", String.valueOf(chatId))
                        .addLong("currentTime", System.currentTimeMillis())
                        .toJobParameters();
                jobLauncher.run(csvImporterJob, jobParameters);
            } catch (Exception e) {
                log.error("Exception :", e);
                sendMessage(chatId, "Failed");
            }
        } else {
            sendMessage(chatId, "Only admin can send importProduct command");
        }

    }

    private void disableProduct(Long chatId, String callbackData, String callbackId) {
        if (userService.checkIsAdmin(chatId)) {
            String productId = getIdFromCallback(callbackData);
            productService.updateProductsByProductAvailable(Long.parseLong(productId), false);
            sendMessageCallback(callbackId, "Success", true);
        } else {
            sendMessageCallback(callbackId, "Only admin can disable product", true);
        }

    }

    private void enableProduct(Long chatId, String callbackData, String callbackId) {
        if (userService.checkIsAdmin(chatId)) {
            String productId = getIdFromCallback(callbackData);
            productService.updateProductsByProductAvailable(Long.parseLong(productId), true);
            sendMessageCallback(callbackId, "Success", true);
        } else {
            sendMessageCallback(callbackId, "Only admin can enable product", true);
        }


    }

    private void editProducts(Long chatId, String callbackData, String callbackId) {
        if (userService.checkIsAdmin(chatId)) {
            String language = userService.getUserLanguage(chatId);
            Language languageProperties = getLanguageProperties(language);
            String productId = getIdFromCallback(callbackData);
            InlineKeyboardMarkup markupInline = productService.buildKeyboardProductEditAdmin(languageProperties, productId);
            sendMessageCallback(callbackId, "Loading…", false);
            String answer = languageProperties.getAdminCommandEdit();
            sendMessageKeyboard(chatId, answer, markupInline);
        } else {
            sendMessageCallback(callbackId, "Only admin can edit product", true);
        }

    }


    private void adminCommandReceived(long chatId) {
        if (userService.checkIsAdmin(chatId)) {
            String language = userService.getUserLanguage(chatId);
            Language languageProperties = getLanguageProperties(language);
            var productList = productService.findAll();
            InlineKeyboardMarkup markupInline = productService.buildKeyboardProductListAdmin(productList, language);
            String answer = languageProperties.getAdminCommand();
            sendMessageKeyboard(chatId, answer, markupInline);
        } else {
            sendMessage(chatId, "Only admin can send admin command");
        }
    }


    private String getIdFromCallback(String callbackData) {
        return callbackData.substring(callbackData.lastIndexOf("_") + 1);
    }

    private String getCategoryId(String callbackData) {
        return callbackData.substring(0, callbackData.lastIndexOf("_day"));
    }


    private void updateUserLanguage(Long chatId, String language, String callbackId) {
        userService.updateUserLanguage(chatId, language);
        sendMessageCallback(callbackId, "Success", false);
    }

    private void helpCommandReceived(Long chatId) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String answer = languageProperties.getTextSupport().replaceAll("/n", System.lineSeparator());
        sendMessage(chatId, answer);
    }

    private void settingCommandReceived(Long chatId) {
        var user = userService.findById(chatId);
        InlineKeyboardMarkup markupInline = userService.buildKeyboardSetting();
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String answerTextLanguage = "English";
        if (language.equals("ro")) {
            answerTextLanguage = "Romana";
        }
        StringBuilder answer = new StringBuilder();
        answer.append(languageProperties.getSettingCommand()).append("  <i>").append(answerTextLanguage).append("</i>").append(System.lineSeparator()).append(System.lineSeparator()).append(languageProperties.getSettingPhoneNumber()).append(" <i>").append(user.getPhoneNumber()).append("</i>").append(System.lineSeparator()).append(System.lineSeparator()).append(languageProperties.getSettingText());

        sendMessageKeyboard(chatId, answer.toString(), markupInline);

    }

    private void confirmQuantityOrder(Long chatId, String callback_id) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        var user = userService.findById(chatId);
        if (userService.findById(chatId).getPhoneNumber().startsWith("+44")) {
            //orderService.updateStatusByOrderId(chatId, "FINISHED");
            var orders = orderService.findAllByUserAndStatusEquals(user, "CREATED");
            sendMessageCallback(callback_id, "Success", true);
            List<LabeledPrice> productsLabel = new ArrayList<>();
            for (Order order : orders) {
                String[] product = order.getProduct().getProductNameEn().split(" ");
                StringBuilder productName = new StringBuilder();
                if (product.length > 0) {
                    productName.append(product[0]).append(" ");
                }
                if (product.length > 1) {
                    productName.append(product[1]).append(" ");
                }
                if (product.length > 2) {
                    productName.append(product[2]).append(" ...");
                }

                productsLabel.add(new LabeledPrice(productName.toString(), order.getTotalPrice().intValue() * 100));
            }
            sendInvoice(chatId, productsLabel, orders);
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

    private void buildCategoryByDayOfWeek(Long chatId, String callbackId, String callbackData) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);

        String categoryId = getIdFromCallback(callbackData);
        var categoryList = categoryService.findAllByDayOfWeek(categoryId);

        InlineKeyboardMarkup markupInline = categoryService.buildKeyboardMenuCategory(categoryList, languageProperties, callbackData);
        String answer = languageProperties.getMenuCommand().replaceAll("/n", System.lineSeparator());
        sendMessageCallback(callbackId, "Loading…", false);
        sendPhoto(chatId, "menu", answer, markupInline);

    }

    private void confirmQuantityProduct(Long chatId, String callbackData, String callbackId) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String orderId = getIdFromCallback(callbackData);
        String quantity = getQuantityFromCallback(callbackData);
        InlineKeyboardMarkup markupInline = orderService.buildKeyboardQuantityConfirm(languageProperties, orderId, quantity);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getConfirmQuantity() + " <b>" + quantity + "</b>";
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
        String answer = languageProperties.getConfirmProductRemove().replaceAll("/n", System.lineSeparator());
        sendMessageKeyboard(chatId, answer, markupInline);
    }

    private void removeProductFromOrder(Long chatId, String callbackData, String callback_id) {
        String orderId = getIdFromCallback(callbackData);
        orderService.deleteById(Long.parseLong(orderId));
        sendMessageCallback(callback_id, "Loading…", false);

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
        String answer = languageProperties.getEditOrders().replaceAll("/n", System.lineSeparator());
        sendMessageKeyboard(chatId, answer, markupInline);

    }


    private void confirmQuantityOrders(Long chatId, String callbackId, String callbackData) {
        String orderId = getIdFromCallback(callbackData);
        String quantity = getQuantityFromCallback(callbackData);
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        InlineKeyboardMarkup markupInline = orderService.buildKeyboardOrderConfirm(languageProperties, orderId, quantity);
        sendMessageCallback(callbackId, "Loading…", false);
        String answer = languageProperties.getConfirmQuantityOrders() + "  <b>" + quantity + "</b>";
        answer = answer.replaceAll("/n", System.lineSeparator());
        sendMessageKeyboard(chatId, answer, markupInline);

    }

    private void shoppingCommandReceived(Long chatId) {
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);


        var user = userService.findById(chatId);
        var ordersList = orderService.findAllByUserAndStatusEquals(user, "CREATED");
        if (ordersList.isEmpty()) {
            String answer = languageProperties.getShoppingCommandEmpty().replaceAll("/n", System.lineSeparator());
            sendMessage(chatId, answer);
            return;
        }

        StringBuilder text = new StringBuilder();

        text.append("\uD83D\uDED2 <b>Order Details</b> \uD83D\uDED2 \nHere's a summary of your order: \n\n");
        BigDecimal finalPrice = new BigDecimal(0);
        for (Order order : ordersList) {
            var product = order.getProduct();
            finalPrice = finalPrice.add(order.getTotalPrice());
            //TODO
            //build text
            text.append("<strong>Product:</strong> ").append(UtilityService.getLanguageProduct(product, user.getLanguage())).append("\n \uD83D\uDD22 Quantity: <b>").append(order.getQuantity()).append("</b> \uD83D\uDD22 \n \uD83D\uDCB8 Price : <b>").append(order.getProduct().getProductPrice()).append(" MDL</b> \uD83D\uDCB8 \n\n");
        }
        text.append("\uD83D\uDCB0 <strong>Order Total:</strong> <strong>").append(finalPrice).append(" MDL </strong> \uD83D\uDE32");
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
        String answer = languageProperties.getTextQuantity().replaceAll("/n", System.lineSeparator());
        sendMessageKeyboard(chatId, answer, markupInline);
    }


    private void buyProduct(Long chatId, String callbackId, String callbackData) {
        String productId = getIdFromCallback(callbackData);
        var user = userService.findById(chatId);
        var product = productService.findById(Long.parseLong(productId));
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        InlineKeyboardMarkup markupInline = productService.buildKeyboardProductButton(product, languageProperties);
        String textCaption = UtilityService.buildDescriptionLanguage(user.getLanguage(), product) + languageProperties.getFinalPrice() + " <b>" + product.getProductPrice() + " MDL</b>/n";
        textCaption = textCaption.replaceAll("/n", System.lineSeparator());

        sendMessageCallback(callbackId, "Loading…", false);

        String textMessage = languageProperties.getProductCommand();
        textMessage = textMessage.replaceAll("/n", System.lineSeparator());
        textCaption = textCaption + System.lineSeparator() + textMessage;
        sendPhoto(chatId, callbackData, textCaption, markupInline);


    }

    private void buildProductByCategoryAndDayOfWeek(Long chatId, String callback_id, String callbackData) {
        String dayOfWeekId = getIdFromCallback(callbackData);
        String idCategory = getCategoryId(callbackData);
        idCategory = getIdFromCallback(idCategory);
        var productList = productService.findAllByDayAndCategory(Long.parseLong(dayOfWeekId), Long.parseLong(idCategory));
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        if (productList.isEmpty()) {
            String answer = languageProperties.getProductListCommandEmpty();
            sendMessageCallback(callback_id, answer, true);
            return;
        }
        String answer = languageProperties.getProductListCommand().replaceAll("/n", System.lineSeparator());
        InlineKeyboardMarkup markupInline = productService.buildKeyboardProducts(productList, languageProperties, dayOfWeekId);
        sendMessageCallback(callback_id, "Loading…", false);
        sendMessageKeyboard(chatId, answer, markupInline);
    }


    private void menuCommandReceived(long chatId) {

        var daysOfWeekList = dayOfWeekService.getAllDaysOfWeek();
        String language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String answer = languageProperties.getMenuCommand().replaceAll("/n", System.lineSeparator());

        InlineKeyboardMarkup markupInline = dayOfWeekService.buildKeyboardMenuDayOfWeek(daysOfWeekList, languageProperties);
        sendPhoto(chatId, "menu", answer, markupInline);

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

    private void startCommandReceived(long chatId) {
        var language = userService.getUserLanguage(chatId);
        Language languageProperties = getLanguageProperties(language);
        String answer = languageProperties.getStartCommand().replaceAll("/n", System.lineSeparator());
        ReplyKeyboard replyKeyboardMarkup = telegramService.buildKeyboardButtons(languageProperties);
        sendMessageKeyboard(chatId, answer, replyKeyboardMarkup);


    }

    public void sendInvoice(long chatId, List<LabeledPrice> products, List<Order> orders) {
        SendInvoice invoice = new SendInvoice();

        invoice.setChatId(chatId);
        List<LabeledPrice> list = new ArrayList<>();
        list.add(new LabeledPrice(" Pret ", 100));
        invoice.setPrices(products);
        invoice.setCurrency("MDL");
        invoice.setProviderToken("6395449203:TEST:3bcdc331975ee9040578");
        invoice.setTitle("Order Summary.");
        invoice.setDescription("Kindly note that Telegram does not process payments directly from users, but instead works with various payment providers globally.");

        StringBuilder payload = new StringBuilder();
        for (Order order : orders) {
            payload.append(order.getOrderId()).append("_");
        }

        invoice.setPayload(payload.toString());
        invoice.setNeedShippingAddress(true);
        invoice.setNeedName(true);
        try {
            execute(invoice);
        } catch (TelegramApiException e) {
            log.error("Exception :", e);
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setParseMode("HTML");
        try {
            execute(message);
            log.info("Send message : [" + message + "] , to UserId : [" + chatId + "]");
        } catch (TelegramApiException e) {
            log.error("Exception :", e);
        }
    }

    private void sendPhoto(long chatId, String productId, String caption, InlineKeyboardMarkup markupInline) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("HTML");
        sendPhoto.setChatId(chatId);
        sendPhoto.setReplyMarkup(markupInline);
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
        message.setParseMode("HTML");
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
