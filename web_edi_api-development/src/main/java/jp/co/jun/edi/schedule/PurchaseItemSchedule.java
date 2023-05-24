package jp.co.jun.edi.schedule;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.PurchaseItemStatusComponent;
import jp.co.jun.edi.component.schedule.PurchaseItemScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TPurchasesVoucherEntity;
import jp.co.jun.edi.repository.TPurchasesVoucherRepository;
import jp.co.jun.edi.type.SendMailStatusType;
import lombok.extern.slf4j.Slf4j;

//PRD_0134 #10654 add JEF start
/**
 * 仕入明細（伝票）PDF生成スケジュール.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.purchase-item-schedule.enabled", matchIfMissing = true)
public class PurchaseItemSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.purchase-item-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TPurchasesVoucherRepository tPurchasesVoucherRepository;

    @Autowired
    private PurchaseItemScheduleComponent purchaseItemScheduleComponent;

    @Autowired
    private PurchaseItemStatusComponent purchaseItemStatusComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    /**
     * 仕入明細（伝票）PDF作成実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        try {
            // ユーザID
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // 仕入伝票管理情報取得
            final List<TPurchasesVoucherEntity> listTPurchasesVoucherEntity = getPurchaseVoucher().getContent();

            // ステータスを 処理中 に更新
            purchaseItemStatusComponent.updateStatusForBeingProcessed(listTPurchasesVoucherEntity, userId);

            // 仕入先コードでグルーピングする
            final Map<String, List<TPurchasesVoucherEntity>> maplistTPurchasesVoucherEntity = listTPurchasesVoucherEntity
                    .stream()
                    .collect(Collectors.groupingBy(
                            entity -> entity.getSupplierCode()));

            // 仕入情報の仕入先コードでグルーピングしたリストごとに、PDF作成、メール送信の処理をする
            maplistTPurchasesVoucherEntity.entrySet().stream().forEach(data -> {

                if (log.isDebugEnabled()) {
                    log.debug("処理単位（仕入先コード）:" + data.getKey());
                }
                purchaseItemScheduleComponent.execute(data.getValue());
            });

//            // 仕入伝票管理情報のループ
//            listTPurchasesVoucherEntity.stream().forEach(tReturnVoucherEntity -> {
//            	purchaseItemScheduleComponent.execute(tReturnVoucherEntity);
//            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 仕入伝票管理を取得.
     * @return 仕入伝票管理情報
     * @throws Exception 例外
     */
    private Page<TPurchasesVoucherEntity> getPurchaseVoucher() throws Exception {
        final Integer maxProcesses = propertyComponent.getBatchProperty().getPurchaseItemMaxProcesses();
        // 処理前のデータを取得する
        final SendMailStatusType statusType = SendMailStatusType.convertToType(SendMailStatusType.UNPROCESSED.getValue());
        return tPurchasesVoucherRepository.findBySendStatus(statusType, PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));
    }
}
//PRD_0134 #10654 add JEF end
