package shop.shopbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "shop.shopbot")
public class ShopBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopBotApplication.class, args);
    }

}
