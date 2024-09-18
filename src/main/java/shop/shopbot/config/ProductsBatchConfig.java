package shop.shopbot.config;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import shop.shopbot.model.Category;
import shop.shopbot.model.DayOfWeek;
import shop.shopbot.model.Product;


@Configuration
@EnableBatchProcessing
public class ProductsBatchConfig {

    private final CustomJobExecutionListener jobExecutionListener;

    public ProductsBatchConfig(CustomJobExecutionListener jobExecutionListener) {
        this.jobExecutionListener = jobExecutionListener;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Product> reader() {
        return new FlatFileItemReaderBuilder<Product>()
                .linesToSkip(1)
                .name("csvItemReader")
                .resource(new FileSystemResource("tmpProducts.csv"))
                .delimited()
                .delimiter(",")
                .names("product_id", "product_available", "product_name_en", "product_name_ro", "product_price", "product_label", "product_descr_en", "product_descr_ro", "category_id", "day_of_week_id")
                .fieldSetMapper(fieldSet -> Product.builder()
                        .productId(fieldSet.readLong("product_id"))
                        .productAvailable(fieldSet.readBoolean("product_available"))
                        .productNameEn(fieldSet.readString("product_name_en"))
                        .productNameRo(fieldSet.readString("product_name_ro"))
                        .productPrice(fieldSet.readBigDecimal("product_price"))
                        .productLabel(fieldSet.readString("product_label"))
                        .productDescriptionEn(fieldSet.readString("product_descr_en"))
                        .productDescriptionRo(fieldSet.readString("product_descr_ro"))
                        .categories(new Category(fieldSet.readLong("category_id")))
                        .dayOfWeekId(new DayOfWeek(fieldSet.readLong("day_of_week_id")))
                        .build()

                ).build();
    }

    @Bean
    public JpaItemWriter<Product> writer(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Product> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Job csvImporterProductJob(Step customerStep, JobRepository jobRepository) {
        return new JobBuilder("csvImporterProductJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(customerStep)
                .end()
                .listener(jobExecutionListener)
                .build();
    }

    @Bean
    public Step csvImporterProductStep(ItemReader<Product> csvReader, ItemWriter<Product> csvWriter,
                                       JobRepository jobRepository, PlatformTransactionManager tx) {

        return new StepBuilder("csvImporterProductStep", jobRepository)
                .<Product, Product>chunk(10, tx)
                .reader(csvReader)
                .writer(csvWriter)
                .allowStartIfComplete(true)
                .build();
    }


}
