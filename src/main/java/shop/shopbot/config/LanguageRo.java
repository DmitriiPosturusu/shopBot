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
}
