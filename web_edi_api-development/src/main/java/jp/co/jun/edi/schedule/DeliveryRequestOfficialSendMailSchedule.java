package jp.co.jun.edi.schedule;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.DeliveryRequestOfficialStatusComponent;
import jp.co.jun.edi.component.schedule.DeliveryRequestOfficialScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TDeliveryOfficialSendMailEntity;
import jp.co.jun.edi.repository.TDeliveryOfficialSendMailRepository;
import jp.co.jun.edi.type.SendMailStatusType;
import lombok.extern.slf4j.Slf4j;

/**
 * 納品依頼書PDF生成およびメール送信スケジュール.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.delivery-request-official-mail-schedule.enabled", matchIfMissing = true)
public class DeliveryRequestOfficialSendMailSchedule {

    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.delivery-request-official-mail-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Value("${" + PROPERTY_NAME_PREFIX + ".status}")
    private int status;

    @Autowired
    private TDeliveryOfficialSendMailRepository tDeliveryOfficialSendMailRepository;

    @Autowired
    private DeliveryRequestOfficialScheduleComponent deliveryRequestOfficialSendMailComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private DeliveryRequestOfficialStatusComponent deliveryRequestOfficialStatusComponent;

    /**
     * 受注確定書PDFメール送信実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        try {
            // ユーザID
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // メール情報取得
            final List<TDeliveryOfficialSendMailEntity> listTDeliveryOfficialSendMailEntity = getOrderSendMail().getContent();

            // ステータスを 処理中 に更新
            deliveryRequestOfficialStatusComponent.updateStatusForBeingProcessed(listTDeliveryOfficialSendMailEntity, userId);

            // 生産メーカーと生産工場でグルーピングする
            final Map<String, List<TDeliveryOfficialSendMailEntity>> mapTDeliveryOfficialSendMailEntity = listTDeliveryOfficialSendMailEntity
                    .stream()
                    .collect(Collectors.groupingBy(
                            entity -> entity.getMdfMakerCode() + "_" + Optional.ofNullable(entity.getMdfMakerFactoryCode()).orElse(StringUtils.EMPTY)));

            // 生産メーカーと生産工場でグルーピングしたリストごとに、PDF作成、メール送信の処理をする
            mapTDeliveryOfficialSendMailEntity.entrySet().stream().forEach(data -> {

                if (log.isDebugEnabled()) {
                    log.debug("処理単位（生産メーカー_生産工場）:" + data.getKey());
                }
                deliveryRequestOfficialSendMailComponent.execute(data.getValue(), userId);
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
    private Page<TDeliveryOfficialSendMailEntity> getOrderSendMail() throws Exception {
        // 実行するステータス
        final SendMailStatusType statusType = SendMailStatusType.convertToType(status);
        return tDeliveryOfficialSendMailRepository.findBySendStatus(statusType, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("createdAt"))));
    }

}
