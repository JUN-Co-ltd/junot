package jp.co.jun.edi;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 *
 */
// CHECKSTYLE:OFF HideUtilityClassConstructor
@EnableAsync
@SpringBootApplication
public class Application {
    /**
     * デフォルトThreadサイズ.
     */
    @Value("${properties.jp.co.jun.edi.thread-delische.core-pool-size}")
    private int corePoolSize;

    /**
     * 待ちのキューのサイズ.
     */
    @Value("${properties.jp.co.jun.edi.thread-delische.queue-capacity}")
    private int queueCapacity;

    /**
     * 最大Threadサイズ.
     */
    @Value("${properties.jp.co.jun.edi.thread-delische.max-pool-size}")
    private int maxPoolSize;

    /**
     * Threadのプレフィクス.
     */
    @Value("${properties.jp.co.jun.edi.thread-delische.thread-name-prefix}")
    private String threadNamePrefix;

    /**
     * 一括登録のデフォルトThreadサイズ.
     */
    @Value("${properties.jp.co.jun.edi.thread-bulk-regist.core-pool-size}")
    private int bulkRegistCorePoolSize;

    /**
     * 一括登録の待ちのキューのサイズ.
     */
    @Value("${properties.jp.co.jun.edi.thread-bulk-regist.queue-capacity}")
    private int bulkRegistQueueCapacity;

    /**
     * 一括登録の最大Threadサイズ.
     */
    @Value("${properties.jp.co.jun.edi.thread-bulk-regist.max-pool-size}")
    private int bulkRegistMaxPoolSize;

    /**
     * 一括登録のThreadのプレフィクス.
     */
    @Value("${properties.jp.co.jun.edi.thread-bulk-regist.thread-name-prefix}")
    private String bulkRegistThreadNamePrefix;

    /**
     * @param args args
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * ここで設定した"ThreadDelische"を＠Asyncに設定するとその設定が利用される.
     * @return executor
     */
    @Bean("ThreadDelische")
    public Executor taskExecutorDelische() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }

    /**
     * ここで設定した"ThreadBulkRegist"を＠Asyncに設定するとその設定が利用される.
     * @return executor
     */
    @Bean("ThreadBulkRegist")
    public Executor taskExecutorBulkRegist() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(bulkRegistCorePoolSize);
        executor.setQueueCapacity(bulkRegistQueueCapacity);
        executor.setMaxPoolSize(bulkRegistMaxPoolSize);
        executor.setThreadNamePrefix(bulkRegistThreadNamePrefix);
        executor.initialize();
        return executor;
    }
}
// CHECKSTYLE:ON HideUtilityClassConstructor
