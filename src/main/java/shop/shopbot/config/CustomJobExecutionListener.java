package shop.shopbot.config;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import shop.shopbot.service.TelegramBot;

@Component
public class CustomJobExecutionListener implements JobExecutionListener {


    @Autowired
    @Lazy
    private TelegramBot telegramBot;

    @Override
    public void afterJob(JobExecution jobExecution) {
        String chatId = jobExecution.getJobParameters().getString("chatId");
        String text;
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            text = "Job import Product Csv completed successfully";
        } else {
            text = "Job import Product Csv failed";
        }
        telegramBot.sendMessage(Long.parseLong(chatId), text);
    }
}
