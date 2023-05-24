package jp.co.jun.edi.schedule;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.component.schedule.TagdatCreateScheduleComponent;
import jp.co.jun.edi.config.PropertyName;

/**
 * TAGDAT作成スケジュール.
 */

@Component
@ConditionalOnProperty(value = PropertyName.ROOT
        + ".schedule.tagdat-create-schedule.enabled", matchIfMissing = true)
public class TagdatCreateSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.tagdat-create-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private TagdatCreateScheduleComponent tagdatCreateScheduleComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    /**
     * TAGDAT作成実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {

    	// ユーザID取得
        final BigInteger userId = scheduleBusinessComponent.getUserId();

        // TAGDAT作成
        tagdatCreateScheduleComponent.execute(userId);
    }

}
