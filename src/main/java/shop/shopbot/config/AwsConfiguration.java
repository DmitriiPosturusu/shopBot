package shop.shopbot.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AwsConfiguration {

    @Value("${app.aws.access-key-id}")
    private String accessKeyId;
    @Value("${app.aws.secret-access-key}")
    private String secretAccessKey;
    @Value("${app.aws.s3-region}")
    private String region;

    @Bean
    public AmazonS3 s3Client() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();
    }
}