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

import jp.co.jun.edi.component.DirectDeliveryItemStatusComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.schedule.DirectDeliveryItemScheduleComponent;
import jp.co.jun.edi.component.schedule.PickingListItemScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TDeliveryVoucherFileInfoEntity;
import jp.co.jun.edi.repository.TDeliveryVoucherFileInfoRepository;
import jp.co.jun.edi.type.DeliveryVoucherCategoryType;
import jp.co.jun.edi.type.FileInfoStatusType;
import lombok.extern.slf4j.Slf4j;


/**
 * 直送（伝票）PDF生成スケジュール.
 *
 * ・出荷配分伝票
 * ・ピッキングリスト
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.direct-delivery-item-schedule.enabled", matchIfMissing = true)
public class DirectDeliveryItemSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.direct-delivery-item-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    //-------------------
    // 納品出荷伝票
    @Autowired
    private DirectDeliveryItemScheduleComponent directDeliveryItemScheduleComponent;

    //-------------------
    // ピッキングリスト
    @Autowired
    private PickingListItemScheduleComponent pickingListItemScheduleComponent;

    //-------------------
    // 共通
    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TDeliveryVoucherFileInfoRepository voucherRepository;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private DirectDeliveryItemStatusComponent itemStatusComponent;

    /**
     * 直送（伝票）PDF作成実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        // ユーザID
        final BigInteger userId = scheduleBusinessComponent.getUserId();

        taskDeliveryItem(userId);

        taskPickingListItem(userId);
    }

    /**
     * 納品出荷伝票ファイル作成処理.
     *
     * @param userId ユーザID
     */
    private void taskDeliveryItem(final BigInteger userId) {
        try {
            // 伝票管理情報取得
            final List<TDeliveryVoucherFileInfoEntity> deliveryEntities =
                    getVoucher(DeliveryVoucherCategoryType.SHIPPING_DISTRIBUTION_VOUCHER).getContent();

            // ステータスを 処理中 に更新
            itemStatusComponent.updateStatusForBeingProcessed(deliveryEntities, userId);

            // 伝票管理情報のループ
            deliveryEntities.stream().forEach(entity -> {
                directDeliveryItemScheduleComponent.execute(entity);
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * ピッキングリストファイル作成処理.
     *
     * @param userId ユーザID
     */
    private void taskPickingListItem(final BigInteger userId) {
        try {
            // 伝票管理情報取得
            final List<TDeliveryVoucherFileInfoEntity> pickingListEntities =
                    getVoucher(DeliveryVoucherCategoryType.PICKING_LIST).getContent();

            // ステータスを 処理中 に更新
            itemStatusComponent.updateStatusForBeingProcessed(pickingListEntities, userId);

            // 伝票管理情報のループ
            pickingListEntities.stream().forEach(entity -> {
                pickingListItemScheduleComponent.execute(entity);
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 直送（伝票）管理を取得.
     *
     * @param categoryType 伝票の種類
     * @return 伝票情報
     * @throws Exception 例外
     */
    private Page<TDeliveryVoucherFileInfoEntity> getVoucher(
            final DeliveryVoucherCategoryType categoryType
            ) throws Exception {
        final Integer maxProcesses = propertyComponent.getBatchProperty().getReturnItemMaxProcesses();
        // 処理前のデータを取得する
        final FileInfoStatusType statusType = FileInfoStatusType.convertToType(FileInfoStatusType.FILE_UNPROCESSED.getValue());
        return voucherRepository.findBySendStatus(statusType, categoryType, PageRequest.of(0, maxProcesses, Sort.by(Order.asc("id"))));
    }
}
