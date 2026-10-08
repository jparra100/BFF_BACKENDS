package cl.duoc.bancoxyz.batch.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;

class BatchJobsConfigTest {

    @Test
    void configuresFourWorkersForParallelProcessing() {
        TaskExecutor taskExecutor = new BatchJobsConfig().batchTaskExecutor();

        assertThat(taskExecutor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor threadPool = (ThreadPoolTaskExecutor) taskExecutor;
        assertThat(threadPool.getCorePoolSize()).isEqualTo(4);
        assertThat(threadPool.getMaxPoolSize()).isEqualTo(4);
        assertThat(threadPool.getThreadNamePrefix()).isEqualTo("batch-worker-");

        threadPool.shutdown();
    }
}
