package ru.otus.hw.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.domain.jpa.AuthorEntity;
import ru.otus.hw.domain.jpa.BookEntity;
import ru.otus.hw.domain.jpa.GenreEntity;
import ru.otus.hw.domain.mongo.AuthorDoc;
import ru.otus.hw.domain.mongo.BookDoc;
import ru.otus.hw.domain.mongo.GenreDoc;
import ru.otus.hw.mapper.AuthorMapper;
import ru.otus.hw.mapper.GenreMapper;
import ru.otus.hw.processor.BookProcessor;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private static final int CHUNK_SIZE = 100;

    private final AuthorMapper authorMapper;

    private final GenreMapper genreMapper;

    private final BookProcessor bookProcessor;

    @Bean
    public Job migrateJob(JobRepository jobRepository, Flow mainFlow) {
        return new JobBuilder("migrateJob", jobRepository)
                .start(mainFlow)
                .end()
                .build();
    }

    @Bean
    public Flow mainFlow(Step cleanupStep, Flow parallelFlow, Step booksStep) {
        return new FlowBuilder<Flow>("mainFlow")
                .start(cleanupStep)
                .next(parallelFlow)
                .next(booksStep)
                .build();
    }

    @Bean
    public Flow parallelFlow(TaskExecutor batchTaskExecutor, Step authorsStep, Step genresStep) {
        Flow authorsFlow = new FlowBuilder<Flow>("authorsFlow").start(authorsStep).build();
        Flow genresFlow = new FlowBuilder<Flow>("genresFlow").start(genresStep).build();
        return new FlowBuilder<Flow>("parallelFlow")
                .split(batchTaskExecutor)
                .add(authorsFlow, genresFlow)
                .build();
    }

    @Bean
    public Step cleanupStep(JobRepository jobRepository, PlatformTransactionManager tm,
                            MongoTemplate mongo) {
        return new StepBuilder("cleanupStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    mongo.dropCollection("books");
                    mongo.dropCollection("authors");
                    mongo.dropCollection("genres");
                    return RepeatStatus.FINISHED;
                }, tm)
                .build();
    }

    @Bean
    public Step authorsStep(JobRepository jobRepository, PlatformTransactionManager tm,
                            JpaPagingItemReader<AuthorEntity> authorReader,
                            MongoItemWriter<AuthorDoc> authorWriter) {
        return new StepBuilder("authorsStep", jobRepository)
                .<AuthorEntity, AuthorDoc>chunk(CHUNK_SIZE, tm)
                .reader(authorReader)
                .processor(authorMapper::toDoc)
                .writer(authorWriter)
                .build();
    }

    @Bean
    public Step genresStep(JobRepository jobRepository, PlatformTransactionManager tm,
                           JpaPagingItemReader<GenreEntity> genreReader,
                           MongoItemWriter<GenreDoc> genreWriter) {
        return new StepBuilder("genresStep", jobRepository)
                .<GenreEntity, GenreDoc>chunk(CHUNK_SIZE, tm)
                .reader(genreReader)
                .processor(genreMapper::toDoc)
                .writer(genreWriter)
                .build();
    }

    @Bean
    public Step booksStep(JobRepository jobRepository, PlatformTransactionManager tm,
                          JpaPagingItemReader<BookEntity> bookReader,
                          MongoItemWriter<BookDoc> bookWriter) {
        return new StepBuilder("booksStep", jobRepository)
                .<BookEntity, BookDoc>chunk(CHUNK_SIZE, tm)
                .reader(bookReader)
                .processor(bookProcessor)
                .writer(bookWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<AuthorEntity> authorReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<AuthorEntity>()
                .name("authorReader")
                .entityManagerFactory(emf)
                .queryString("select a from AuthorEntity a order by a.id")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<GenreEntity> genreReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<GenreEntity>()
                .name("genreReader")
                .entityManagerFactory(emf)
                .queryString("select g from GenreEntity g order by g.id")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<BookEntity> bookReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<BookEntity>()
                .name("bookReader")
                .entityManagerFactory(emf)
                .queryString("select b from BookEntity b order by b.id")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public MongoItemWriter<AuthorDoc> authorWriter(MongoTemplate mongo) {
        return new MongoItemWriterBuilder<AuthorDoc>()
                .template(mongo).collection("authors").build();
    }

    @Bean
    public MongoItemWriter<GenreDoc> genreWriter(MongoTemplate mongo) {
        return new MongoItemWriterBuilder<GenreDoc>()
                .template(mongo).collection("genres").build();
    }

    @Bean
    public MongoItemWriter<BookDoc> bookWriter(MongoTemplate mongo) {
        return new MongoItemWriterBuilder<BookDoc>()
                .template(mongo).collection("books").build();
    }

    @Bean
    public TaskExecutor batchTaskExecutor() {
        var exec = new SimpleAsyncTaskExecutor("migrate-");
        exec.setConcurrencyLimit(2);
        return exec;
    }
}
