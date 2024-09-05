package shop.shopbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("classpath:application.properties")
public class BotConfig {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.key}")
    private String botKey;

    @Value("${app.aws.s3-backet-name}")
    private String bucketName;


}
