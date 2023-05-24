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

import jp.co.jun.edi.component.OrderReceiveStatusComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.schedule.OrderReceiveScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TOrderSendMailEntity;
import jp.co.jun.edi.repository.TOrderSendMailRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 受注確定書PDF生成およびメール送信スケジュール.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.order-receive-send-mail-schedule.enabled", matchIfMissing = true)
public class OrderReceiveSendMailSchedule {

    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.order-receive-send-mail-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TOrderSendMailRepository tOrderSendMailRepository;

    @Autowired
    private OrderReceiveScheduleComponent orderReceiveSendMailComponent;

    @Autowired
    private OrderReceiveStatusComponent orderReceiveStatusComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;
    /**
     * 受注確定書PDFメール送信実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        try {
            // ユーザID
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // メール情報取得
            final List<TOrderSendMailEntity> listTOrderSendMailEntity = getOrderSendMail().getContent();

            // ステータスを 処理中 に更新
            orderReceiveStatusComponent.updateStatusForBeingProcessed(listTOrderSendMailEntity, userId);

            // メール情報のループ
            listTOrderSendMailEntity.stream().forEach(tOrderSendMailEntity -> {
                orderReceiveSendMailComponent.execute(tOrderSendMailEntity);
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 受注確定メール送信情報を取得.
     * @return 受信確定メール送信情報
     * @throws Exception 例外
     */
    private Page<TOrderSendMailEntity> getOrderSendMail() throws Exception {
        final Integer maxProcesses = propertyComponent.getBatchProperty().getOrderReceiveMaxProcesses();
        return tOrderSendMailRepository.findBySendStatus(PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));
    }

}
