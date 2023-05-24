package jp.co.jun.edi.schedule;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import jp.co.jun.edi.config.PropertyName;

/**
 * スケジュール用の設定クラス.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.enabled", matchIfMissing = true)
public class ScheduleConfig {
}
