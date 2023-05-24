package jp.co.jun.edi.schedule;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.component.schedule.TagdatSendMailScheduleComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TagdatEntity;
import jp.co.jun.edi.repository.TagdatRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * タグデータメール送信スケジュール.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.tagdat-send-mail-schedule.enabled", matchIfMissing = true)
public class TagdatSendMailSchedule {

    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.tagdat-send-mail-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private TagdatRepository tagdatRepository;

    @Autowired
    private TagdatSendMailScheduleComponent tagdatSendMailScheduleComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    /**
     * タグデータメール送信実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        try {
            // ユーザID
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // TAGDAT情報取得
            final List<TagdatEntity> listTagdatEntity = getTagdatInfo().getContent();

            if (!CollectionUtils.isEmpty(listTagdatEntity)) {
	            // CSV作成、送信処理実行
            	tagdatSendMailScheduleComponent.execute(listTagdatEntity, userId);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * TAGDAT情報を取得.
     * @return TAGDAT情報
     * @throws Exception 例外
     */
    private Page<TagdatEntity> getTagdatInfo() {

        return tagdatRepository.findBySendStatus(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("brkg")).and(Sort.by(Order.asc("seq")))));
    }

}
