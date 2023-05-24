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

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.ReturnItemStatusComponent;
import jp.co.jun.edi.component.schedule.ReturnItemScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TReturnVoucherEntity;
import jp.co.jun.edi.repository.TReturnVoucherRepository;
import jp.co.jun.edi.type.SendMailStatusType;
import lombok.extern.slf4j.Slf4j;


/**
 * 返品明細（伝票）PDF生成スケジュール.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.retuen-item-schedule.enabled", matchIfMissing = true)
public class ReturnItemSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.retuen-item-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TReturnVoucherRepository tReturnVoucherRepository;

    @Autowired
    private ReturnItemScheduleComponent returnItemScheduleComponent;

    @Autowired
    private ReturnItemStatusComponent returnItemStatusComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    /**
     * 返品明細（伝票）PDF作成実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        try {
            // ユーザID
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // 返品伝票管理情報取得
            final List<TReturnVoucherEntity> listTReturnVoucherEntity = getReturnVoucher().getContent();

            // ステータスを 処理中 に更新
            returnItemStatusComponent.updateStatusForBeingProcessed(listTReturnVoucherEntity, userId);

            // 返品伝票管理情報のループ
            listTReturnVoucherEntity.stream().forEach(tReturnVoucherEntity -> {
                returnItemScheduleComponent.execute(tReturnVoucherEntity);
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 返品伝票管理を取得.
     * @return 返品伝票管理情報
     * @throws Exception 例外
     */
    private Page<TReturnVoucherEntity> getReturnVoucher() throws Exception {
        final Integer maxProcesses = propertyComponent.getBatchProperty().getReturnItemMaxProcesses();
        // 処理前のデータを取得する
        final SendMailStatusType statusType = SendMailStatusType.convertToType(SendMailStatusType.UNPROCESSED.getValue());
        return tReturnVoucherRepository.findBySendStatus(statusType, PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));
    }
}
