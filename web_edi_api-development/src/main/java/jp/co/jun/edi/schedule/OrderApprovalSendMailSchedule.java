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

import jp.co.jun.edi.component.OrderApprovalStatusComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.schedule.OrderApprovalScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TOrderApprovalSendMailEntity;
import jp.co.jun.edi.repository.TOrderApprovalSendMailRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 発注承認PDF（即時）生成およびメール送信スケジュール.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.order-approval-send-mail-schedule.enabled", matchIfMissing = true)
public class OrderApprovalSendMailSchedule {

    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.order-approval-send-mail-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TOrderApprovalSendMailRepository tOrderApprovalSendMailRepository;

    @Autowired
    private OrderApprovalScheduleComponent orderApprovalScheduleComponent;

    @Autowired
    private OrderApprovalStatusComponent orderApprovalStatusComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;
    /**
     * 発注承認PDF（即時）メール送信実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        try {
            // ユーザID
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // メール情報取得
            final List<TOrderApprovalSendMailEntity> listTOrderSendMailEntity = getOrderSendMail().getContent();

            // ステータスを 処理中 に更新
            orderApprovalStatusComponent.updateStatusForBeingProcessed(listTOrderSendMailEntity, userId);

            // メール情報のループ
            listTOrderSendMailEntity.stream().forEach(tOrderSendMailEntity -> {
                orderApprovalScheduleComponent.execute(userId, tOrderSendMailEntity);
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 発注承認メール送信情報を取得.
     * @return 発注承認メール送信情報
     * @throws Exception 例外
     */
    private Page<TOrderApprovalSendMailEntity> getOrderSendMail() throws Exception {
        final Integer maxProcesses = propertyComponent.getBatchProperty().getOrderReceiveMaxProcesses();
        return tOrderApprovalSendMailRepository.findBySendStatus(PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));
    }

}
