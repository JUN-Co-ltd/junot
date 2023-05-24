package jp.co.jun.edi.schedule;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.schedule.MaterialOrderLinkingComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.extended.ExtendedTFOrderLinkingEntity;

/**
 * 資材発注連携スケジュール.
 */
@Component
@ConditionalOnProperty(value = PropertyName.ROOT + ".schedule.material-order-linking-schedule.enabled", matchIfMissing = true)
public class MateralOrderLinkingSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.material-order-linking-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private MaterialOrderLinkingComponent materialOrderLinkingComponent;

    /**
     * フクキタル連携実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {

        // ユーザID取得
        final BigInteger userId = materialOrderLinkingComponent.getAdminUserId();

        // 連携対象のフクキタル発注情報取得
        final List<ExtendedTFOrderLinkingEntity> listLinkingOrderInfo = materialOrderLinkingComponent.getOrderInfo(userId);

        for (final ExtendedTFOrderLinkingEntity linkingOrderInfoEntity : listLinkingOrderInfo) {
            materialOrderLinkingComponent.execute(userId, linkingOrderInfoEntity);
        }
    }


}
