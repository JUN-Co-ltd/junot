package jp.co.jun.edi.schedule;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.DeliveryRequestStatusComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.schedule.DeliveryRequestScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TDeliverySendMailEntity;
import jp.co.jun.edi.repository.TDeliverySendMailRepository;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 納品依頼書用即時メールスケジュール.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.delivery-request-realtime-mail-schedule.enabled", matchIfMissing = true)
public class DeliveryRequestRealTimeMailSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.delivery-request-realtime-mail-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private DeliveryRequestStatusComponent deliveryRequestGetInfoComponet;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TDeliverySendMailRepository tDeliverySendMailRepository;

    @Autowired
    private DeliveryRequestScheduleComponent deliveryRequestSendMailComponent;
    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    /**
     * 納品依頼書用即時メール連携実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        try {
            // ユーザID取得
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // メール情報取得
            final List<TDeliverySendMailEntity> listTDeliverySendMailEntity = getDeliverySendMail().getContent();

            // ステータスを 処理中 に更新
            deliveryRequestGetInfoComponet.updateStatusForBeingProcessed(listTDeliverySendMailEntity, userId);

            // メール情報のループ
            for (TDeliverySendMailEntity tDeliverySendMailEntity : listTDeliverySendMailEntity) {
                deliveryRequestSendMailComponent.execute(tDeliverySendMailEntity, userId);

            }
        } catch (RuntimeException e) {
            log.error(LogStringUtil.of("task").message(e.getMessage()).exception(e).build(), e);
        } catch (Exception e) {
            log.error(LogStringUtil.of("task").message(e.getMessage()).exception(e).build(), e);
        }
    }

    /**
     * 納品依頼（即時）メール送信情報を取得.
     * @return 納品依頼（即時）メール送信情報
     * @throws Exception 例外
     */
    private Page<TDeliverySendMailEntity> getDeliverySendMail() throws Exception {
        final Integer maxProcesses = propertyComponent.getBatchProperty().getDeliveryRequestMaxProcesses();
        return tDeliverySendMailRepository.findBySendStatus(PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));
    }
}
