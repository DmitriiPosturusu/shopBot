package shop.shopbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationPropertiesScan
@PropertySource("classpath:language_ro.properties")
public class LanguageRo implements Language {

    @Value("${ro.language}")
    private String language;

    @Value("${ro.button.main}")
    private String buttonMain;

    @Value("${ro.button.menu}")
    private String buttonMenu;

    @Value("${ro.button.bag}")
    private String buttonBag;

    @Value("${ro.button.support}")
    private String buttonSupport;

    @Value("${ro.button.setting}")
    private String buttonSetting;

    @Value("${ro.button.edit}")
    private String buttonEdit;

    @Value("${ro.button.edit.quantity}")
    private String buttonEditQuantity;

    @Value("${ro.button.confirm}")
    private String buttonConfirm;

    @Value("${ro.button.delete}")
    private String buttonDelete;

    @Value("${ro.button.back}")
    private String buttonBack;

    @Value("${ro.button.buy}")
    private String buttonBuy;

    @Value("${ro.button.admin.enable}")
    private String buttonAdminEnable;

    @Value("${ro.button.admin.disable}")
    private String buttonAdminDisable;

    @Value("${ro.alert.empty}")
    private String alertEmpty;

    @Value("${ro.alert.order.exist}")
    private String alertOrderExist;

    @Value("${ro.alert.order}")
    private String alertOrder;

    @Value("${ro.text.phoneNumber}")
    private String textPhoneNumber;

    @Value("${ro.text.weekOffer}")
    private String weekOffer;

    @Value("${ro.text.startCommand}")
    private String startCommand;

    @Value("${ro.text.menuCommand}")
    private String menuCommand;

    @Value("${ro.text.productListCommand}")
    private String productListCommand;

    @Value("${ro.text.productListCommandEmpty}")
    private String productListCommandEmpty;

    @Value("${ro.text.categoryByProduct}")
    private String categoryByProduct;

    @Value("${ro.text.quantity}")
    private String textQuantity;

    @Value("${ro.text.buildOrder}")
    private String textBuildOrder;

    @Value("${ro.text.shoppingCommandEmpty}")
    private String shoppingCommandEmpty;

    @Value("${ro.text.finalPrice}")
    private String finalPrice;

    @Value("${ro.text.confirmQuantityOrders}")
    private String confirmQuantityOrders;

    @Value("${ro.text.editOrders}")
    private String editOrders;

    @Value("${ro.text.editProducts}")
    private String editProducts;

    @Value("${ro.text.confirmProductRemove}")
    private String confirmProductRemove;

    @Value("${ro.text.editQuantity}")
    private String editQuantity;

    @Value("${ro.text.confirmQuantity}")
    private String confirmQuantity;

    @Value("${ro.alert.phoneNumber}")
    private String alertPhoneNumber;

    @Value("${ro.text.settingCommand}")
    private String settingCommand;


    @Value("${ro.text.settingPhoneNumber}")
    private String settingPhoneNumber;


    @Value("${ro.text.support}")
    private String textSupport;

    @Value("${ro.text.adminCommand}")
    private String adminCommand;

    @Value("${ro.text.adminCommandEdit}")
    private String adminCommandEdit;

    @Value("${ro.text.weekDays}")
    private String weekDays;


}
