package cl.duoc.bancoxyz.batch.config;

import cl.duoc.bancoxyz.batch.model.AnnualStatementLine;
import cl.duoc.bancoxyz.batch.model.InterestResult;
import cl.duoc.bancoxyz.batch.model.ProcessedTransaction;
import cl.duoc.bancoxyz.batch.service.LegacyBatchProcessors;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchJobsConfig {

    @Bean
    TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("batch-worker-");
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(16);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean
    Job dailyTransactionsJob(JobRepository repository, Step dailyTransactionsStep) {
        return new JobBuilder("dailyTransactionsJob", repository)
                .start(dailyTransactionsStep)
                .build();
    }

    @Bean
    Job monthlyInterestJob(JobRepository repository, Step monthlyInterestStep) {
        return new JobBuilder("monthlyInterestJob", repository)
                .start(monthlyInterestStep)
                .build();
    }

    @Bean
    Job annualStatementsJob(JobRepository repository, Step annualStatementsStep) {
        return new JobBuilder("annualStatementsJob", repository)
                .start(annualStatementsStep)
                .build();
    }

    @Bean
    Step dailyTransactionsStep(JobRepository repository, PlatformTransactionManager transactionManager,
                               LegacyBatchProcessors processors, DataSource dataSource,
                               @Qualifier("batchTaskExecutor") TaskExecutor batchTaskExecutor,
                               @Value("${legacy.data-dir}") String dataDir) {
        return new StepBuilder("dailyTransactionsStep", repository)
                .<String[], ProcessedTransaction>chunk(100, transactionManager)
                .reader(csvReader("transactionsReader", dataDir + "/transacciones.csv", 4))
                .processor(processors::transaction)
                .writer(transactionWriter(dataSource))
                .faultTolerant().skip(IllegalArgumentException.class).skipLimit(1000)
                .retry(TransientDataAccessException.class).retryLimit(3)
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    @Bean
    Step monthlyInterestStep(JobRepository repository, PlatformTransactionManager transactionManager,
                             LegacyBatchProcessors processors, DataSource dataSource,
                             @Qualifier("batchTaskExecutor") TaskExecutor batchTaskExecutor,
                             @Value("${legacy.data-dir}") String dataDir) {
        return new StepBuilder("monthlyInterestStep", repository)
                .<String[], InterestResult>chunk(100, transactionManager)
                .reader(csvReader("interestReader", dataDir + "/intereses.csv", 5))
                .processor(processors::interest)
                .writer(interestWriter(dataSource))
                .faultTolerant().skip(IllegalArgumentException.class).skipLimit(1000)
                .retry(TransientDataAccessException.class).retryLimit(3)
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    @Bean
    Step annualStatementsStep(JobRepository repository, PlatformTransactionManager transactionManager,
                              LegacyBatchProcessors processors, DataSource dataSource,
                              @Qualifier("batchTaskExecutor") TaskExecutor batchTaskExecutor,
                              @Value("${legacy.data-dir}") String dataDir) {
        return new StepBuilder("annualStatementsStep", repository)
                .<String[], AnnualStatementLine>chunk(100, transactionManager)
                .reader(csvReader("statementsReader", dataDir + "/cuentas_anuales.csv", 5))
                .processor(processors::statement)
                .writer(statementWriter(dataSource))
                .faultTolerant().skip(IllegalArgumentException.class).skipLimit(1000)
                .retry(TransientDataAccessException.class).retryLimit(3)
                .taskExecutor(batchTaskExecutor)
                .build();
    }

    private ItemReader<String[]> csvReader(String name, String path, int columns) {
        String[] names = new String[columns];
        for (int i = 0; i < columns; i++) {
            names[i] = "column" + i;
        }
        FlatFileItemReader<String[]> reader = new FlatFileItemReaderBuilder<String[]>()
                .name(name)
                .resource(new FileSystemResource(path))
                .linesToSkip(1)
                .delimited().delimiter(",").names(names)
                .fieldSetMapper(fieldSet -> fieldSet.getValues())
                .build();
        reader.setSaveState(true);
        SynchronizedItemStreamReader<String[]> synchronizedReader =
                new SynchronizedItemStreamReaderBuilder<String[]>()
                        .delegate(reader)
                        .build();
        return synchronizedReader;
    }

    private JdbcBatchItemWriter<ProcessedTransaction> transactionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<ProcessedTransaction>()
                .dataSource(dataSource)
                .sql("insert into processed_transaction(id, transaction_date, amount, transaction_type) values (?, ?, ?, ?) on conflict (id) do nothing")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, item.id()); ps.setObject(2, item.date());
                    ps.setBigDecimal(3, item.amount()); ps.setString(4, item.type());
                }).build();
    }

    private JdbcBatchItemWriter<InterestResult> interestWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<InterestResult>()
                .dataSource(dataSource)
                .sql("insert into interest_result(account_id, holder_name, account_type, balance, monthly_interest) values (?, ?, ?, ?, ?) on conflict (account_id) do update set balance=excluded.balance, monthly_interest=excluded.monthly_interest")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, item.accountId()); ps.setString(2, item.holderName());
                    ps.setString(3, item.accountType()); ps.setBigDecimal(4, item.balance());
                    ps.setBigDecimal(5, item.monthlyInterest());
                }).build();
    }

    private JdbcBatchItemWriter<AnnualStatementLine> statementWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<AnnualStatementLine>()
                .dataSource(dataSource)
                .sql("insert into annual_statement(account_id, movement_date, movement_type, amount, description) values (?, ?, ?, ?, ?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, item.accountId()); ps.setObject(2, item.date());
                    ps.setString(3, item.type()); ps.setBigDecimal(4, item.amount());
                    ps.setString(5, item.description());
                }).build();
    }
}
