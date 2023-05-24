package jp.co.jun.edi.component;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.TDeliveryPlanEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.model.mail.DeliveryPlanSendModel;
import jp.co.jun.edi.repository.TDeliveryPlanCutRepository;
import jp.co.jun.edi.repository.TDeliveryPlanSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 納品予定のメール送信用データ作成を行うコンポーネント.
 */
@Component
@Slf4j
public class DeliveryPlanCreateMailDataComponent extends GenericComponent {

    @Autowired
    private ExtendedTItemRepository itemRepository;

    @Autowired
    private TDeliveryPlanSkuRepository deliveryPlanSkuRepository;

    @Autowired
    private ExtendedTOrderRepository exOrderRepository;

    @Autowired
    private TDeliveryPlanCutRepository deliveryPlanCutRepository;

    /**
     * 納品予定のメール送信用のデータを作成.
     * @param deliveryPlanEntity TDeliveryPlanEntity
     * @return DeliveryPlanSendModel
     */
    public DeliveryPlanSendModel getSendModel(final TDeliveryPlanEntity deliveryPlanEntity) {

        DeliveryPlanSendModel sendModel = new DeliveryPlanSendModel();

        // 発注情報を取得し、データが存在しない場合は空のEntityを用意する。
        final ExtendedTOrderEntity orderEntity =
                exOrderRepository.findById(deliveryPlanEntity.getOrderId()).orElse(new ExtendedTOrderEntity());

        // 品番情報を取得し、データが存在しない場合は空のEntityを用意する。
        final ExtendedTItemEntity itemEntity =
                itemRepository.findById(orderEntity.getPartNoId()).orElse(new ExtendedTItemEntity());

        log.debug("発注数：" + orderEntity.getQuantity());

        // 裁断数(生産数)合計を取得
        BigDecimal sumDeliveryPlanCutLot = deliveryPlanCutRepository.getSumDeliveryPlanCutLotByDeliveryPlanId(deliveryPlanEntity.getId());
        log.debug("裁断数：" + sumDeliveryPlanCutLot);

        // 納品予定数の合計を取得
        BigDecimal sumDeliveryPlanLot =  deliveryPlanSkuRepository.getSumDeliveryPlanLotByDeliveryPlanId(deliveryPlanEntity.getId());
        log.debug("納品予定数合計：" + sumDeliveryPlanLot);

        // 納品予定数の合計が取得できなければ 納品予定数の合計=0
        if (sumDeliveryPlanLot == null) {
            sumDeliveryPlanLot = BigDecimal.ZERO;
        }

        // 増減産数を算出
        BigDecimal increaseOrDecreaseLot = sumDeliveryPlanLot.subtract(orderEntity.getQuantity());
        log.debug("増減産数：" + increaseOrDecreaseLot);

        // スケール(小数点以下の桁数)の設定
        final int scale = 3;
        // 増減産率を算出
        // 小数点2桁で四捨五入しパーセント表示で小数点1桁まで表示されるようにする
        BigDecimal increaseOrDecreaseRate = increaseOrDecreaseLot.divide(orderEntity.getQuantity(), scale, BigDecimal.ROUND_DOWN)
                                                        .scaleByPowerOfTen(2).stripTrailingZeros();

        // 小数点以下の0削除によりスケールがなくなる場合は、明示的にスケールをセットする
        if (increaseOrDecreaseRate.scale() < 0) {
                increaseOrDecreaseRate = increaseOrDecreaseRate.setScale(0);
        }

        log.debug("増減産率：" + increaseOrDecreaseRate);

        // Modelに値をSetする
        // 企画担当
        sendModel.setPlannerCode(itemEntity.getPlannerCode());

        // 製造担当
        sendModel.setMdfStaffCode(orderEntity.getMdfStaffCode());

        // パターンナー
        sendModel.setPatanerCode(itemEntity.getPatanerCode());

        // 生産メーカー担当：生産メーカー宛には送信しないため、データをセットしない。

        // 品番
        sendModel.setPartNo(itemEntity.getPartNo());

        // 品名
        sendModel.setProductName(itemEntity.getProductName());

        // 生産メーカーコード
        sendModel.setMdfMakerCode(orderEntity.getMdfMakerCode());

        // 生産メーカー名
        sendModel.setMdfMakerName(orderEntity.getMdfMakerName());

        // 発注No
        sendModel.setOrderNumber(orderEntity.getOrderNumber());

        // 発注数
        sendModel.setQuantity(orderEntity.getQuantity());

        // 納期
        sendModel.setProductDeliveryAt(orderEntity.getProductDeliveryAt());

        // 裁断数合計
        sendModel.setSumDeliveryPlanCutLot(sumDeliveryPlanCutLot);

        // 増減産数
        sendModel.setIncreaseOrDecreaseLot(increaseOrDecreaseLot);

        // 増減産率
        sendModel.setIncreaseOrDecreaseRate(increaseOrDecreaseRate);

        return sendModel;
    }
}
