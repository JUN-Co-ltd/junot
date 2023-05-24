package jp.co.jun.edi.schedule;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.MKanmstComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.MKanmstEntity;
import jp.co.jun.edi.repository.MKanmstRepository;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

//#PRD_0139 #10681 add JFE end
/**
 * 管理マスタ日計日更新スケジュール.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.kanmst-update-schedule.enabled", matchIfMissing = true)
public class KanmstUpdateSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.kanmst-update-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private MKanmstComponent MKanmstComponent;

    @Autowired
    private MKanmstRepository MKanmstRepository;

    /**
     * 管理マスタ更新実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        try {
            // ユーザID
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            log.info(LogStringUtil.of("executeSystemTodayUpdate")
                    .message("Start processing of KanmstUpdateSchedule.")
                    .build());

            //エンティティ作成
            final MKanmstEntity MKanmstEntity = MKanmstComponent.generateMKanmstEntity(userId);

            // 日計日を今日の日付に更新
            MKanmstRepository.save(MKanmstEntity);

            log.info(LogStringUtil.of("executeSystemTodayUpdate")
                    .message("End processing of KanmstUpdateSchedule.")
                    .build());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
//#PRD_0139 #10681 add JFE end
