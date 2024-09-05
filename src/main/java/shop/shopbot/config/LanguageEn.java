package shop.shopbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationPropertiesScan
@PropertySource("classpath:language_en.properties")
public class LanguageEn implements Language {

    @Value("${en.language}")
    private String language;

    @Value("${en.button.main}")
    private String buttonMain;

    @Value("${en.button.menu}")
    private String buttonMenu;

    @Value("${en.button.bag}")
    private String buttonBag;

    @Value("${en.button.support}")
    private String buttonSupport;

    @Value("${en.button.setting}")
    private String buttonSetting;

    @Value("${en.button.edit}")
    private String buttonEdit;

    @Value("${en.button.edit.quantity}")
    private String buttonEditQuantity;

    @Value("${en.button.confirm}")
    private String buttonConfirm;

    @Value("${en.button.delete}")
    private String buttonDelete;

    @Value("${en.button.back}")
    private String buttonBack;


    @Value("${en.button.admin.enable}")
    private String buttonAdminEnable;

    @Value("${en.button.admin.disable}")
    private String buttonAdminDisable;

    @Value("${en.button.buy}")
    private String buttonBuy;

    @Value("${en.alert.empty}")
    private String alertEmpty;

    @Value("${en.alert.order.exist}")
    private String alertOrderExist;

    @Value("${en.alert.order}")
    private String alertOrder;

    @Value("${en.text.phoneNumber}")
    private String textPhoneNumber;

    @Value("${en.text.weekOffer}")
    private String weekOffer;

    @Value("${en.text.startCommand}")
    private String startCommand;

    @Value("${en.text.menuCommand}")
    private String menuCommand;

    @Value("${en.text.productListCommand}")
    private String productListCommand;

    @Value("${en.text.productListCommandEmpty}")
    private String productListCommandEmpty;

    @Value("${en.text.categoryByProduct}")
    private String categoryByProduct;

    @Value("${en.text.quantity}")
    private String textQuantity;

    @Value("${en.text.buildOrder}")
    private String textBuildOrder;

    @Value("${en.text.shoppingCommandEmpty}")
    private String shoppingCommandEmpty;

    @Value("${en.text.finalPrice}")
    private String finalPrice;

    @Value("${en.text.confirmQuantityOrders}")
    private String confirmQuantityOrders;

    @Value("${en.text.editOrders}")
    private String editOrders;

    @Value("${en.text.editProducts}")
    private String editProducts;

    @Value("${en.text.confirmProductRemove}")
    private String confirmProductRemove;

    @Value("${en.text.editQuantity}")
    private String editQuantity;

    @Value("${en.text.confirmQuantity}")
    private String confirmQuantity;

    @Value("${en.alert.phoneNumber}")
    private String alertPhoneNumber;

    @Value("${en.text.settingCommand}")
    private String settingCommand;

    @Value("${en.text.settingPhoneNumber}")
    private String settingPhoneNumber;

    @Value("${en.text.support}")
    private String textSupport;


    @Value("${en.text.adminCommand}")
    private String adminCommand;

    @Value("${en.text.adminCommandEdit}")
    private String adminCommandEdit;

    @Value("${en.text.weekDays}")
    private String weekDays;

}
