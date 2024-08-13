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


}
